package com.ucbcba.demo.Controllers;

import com.ucbcba.demo.entities.*;
import com.ucbcba.demo.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
//import java.util.List;

@Controller
public class RestaurantController {

    private RestaurantService restaurantService;
    private CategoryService categoryService;
    private CityService cityService;
    private CountryService countryService;
    private PhotoService photoService;
    private UserLikesService userLikesService;
    private UserService userService;

    @Autowired
    public void setRestaurantService(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @Autowired
    public void setCategoryService(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Autowired
    public void setCityService(CityService cityService) {
        this.cityService = cityService;
    }

    @Autowired
    public void setCountryService(CountryService countryService) {
        this.countryService = countryService;
    }

    @Autowired
    public void setPhotoService(PhotoService photoService) {
        this.photoService = photoService;
    }

    @Autowired
    public void setUserLikeService(UserLikesService userLikesService) {
        this.userLikesService = userLikesService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(value = "admin/restaurants", method = RequestMethod.GET)
    public String listAllRestaurants(Model model) {
        model.addAttribute("restaurants", restaurantService.listAllRestaurants());
        return "restaurants";
    }

    @RequestMapping(value = "admin/restaurant/new")
    public String newRestaurant(Model model) {
        model.addAttribute("restaurantCategories", categoryService.listAllCategories());
        model.addAttribute("cities", cityService.listAllCities());
        model.addAttribute("countries", countryService.listAllCountries());
        model.addAttribute("restaurant", new Restaurant());
        return "newRestaurant";
    }

    @RequestMapping(value = "admin/restaurant/save", method = RequestMethod.POST)
    public String saveRestaurant(Restaurant restaurant, @RequestParam("file") MultipartFile[] files) throws IOException {
        restaurantService.saveRestaurant(restaurant);
        byte[] pixel;
        for(int i=0;i<files.length;i++)
        {
            if (!files[i].isEmpty()) {
                pixel = files[i].getBytes();
                Photo photo = new Photo();
                photo.setRestaurant(restaurant);
                photo.setPhoto(pixel);
                photoService.savePhoto(photo);
            }
        }
        return "redirect:/admin/restaurants";
    }

    @RequestMapping("admin/restaurant/{id}")
    String showRestaurant(@PathVariable Integer id, Model model) throws UnsupportedEncodingException {
        Integer califs[] = {1, 2, 3, 4, 5};
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        com.ucbcba.demo.entities.User user=userService.findByUsername(((User) auth.getPrincipal()).getUsername());
        Restaurant restaurant = restaurantService.getRestaurant(id);
        model.addAttribute("restaurant", restaurant);
        model.addAttribute("calification", califs);
        model.addAttribute("user",user);
        model.addAttribute("averageScore", restaurantService.getScore(id));
        List restaurantPhotos= new ArrayList();
        Integer likes = userLikesService.getLikes(id);
        model.addAttribute("likes", likes);
        List<Photo> photos = (List<Photo>)photoService.listAllPhotosById(id);
        byte[] encodeBase64;
        String base64Encoded;
        for(int i=0;i<photos.size();i++)
        {
            encodeBase64 = Base64.encode(photos.get(i).getPhoto());
            base64Encoded = new String(encodeBase64,"UTF-8");
            restaurantPhotos.add(base64Encoded);
        }
        model.addAttribute("photos", restaurantPhotos );
        return "ShowRestaurant";
    }

    @RequestMapping(value = "admin/restaurant/edit/{id}")
    String editRestaurant(@PathVariable Integer id, Model model) throws UnsupportedEncodingException {
        model.addAttribute("restaurant", restaurantService.getRestaurant(id));
        model.addAttribute("restaurantCategories", categoryService.listAllCategories());
        model.addAttribute("cities", cityService.listAllCities());
        List restaurantPhotos= new ArrayList();
        List<Photo> photos = (List<Photo>)photoService.listAllPhotosById(id);
        byte[] encodeBase64;
        String base64Encoded;
        for(int i=0;i<photos.size();i++)
        {
            encodeBase64 = Base64.encode(photos.get(i).getPhoto());
            base64Encoded = new String(encodeBase64,"UTF-8");
            restaurantPhotos.add(base64Encoded);
        }
        model.addAttribute("photos", restaurantPhotos );
        model.addAttribute("images",photoService.listAllPhotosById(id));
        return "restaurantForm";
    }

    @RequestMapping(value = "admin/restaurant/delete/{id}")
    String delete(@PathVariable Integer id, Model model) {
        restaurantService.deleteRestaurant(id);
        return "redirect:/admin/restaurants";
    }

    @RequestMapping("/admin/delete/photo/{id}")
    String deletePhoto(@PathVariable Integer id) {
        Photo photo = photoService.getPhoto(id);
        photoService.deletePhoto(id);
        return "redirect:/admin/restaurant/edit/"+ photo.getRestaurant().getId();
    }

    @RequestMapping("restaurant/{id}")
    String showRestaurantUser(@PathVariable Integer id, Model model,com.ucbcba.demo.entities.User user) throws UnsupportedEncodingException {
        Boolean logged = false;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Restaurant restaurant = restaurantService.getRestaurant(id);
        Integer califs[] = {1, 2, 3, 4, 5};
        model.addAttribute("calification", califs);
        model.addAttribute("averageScore", restaurantService.getScore(id));
        model.addAttribute("commentError","You can only add one comment by Restaurant");
        model.addAttribute("restaurant", restaurant);
        Integer likes = userLikesService.getLikes(id);
        model.addAttribute("likes", likes);
        Boolean isLiked = false;
        if (!auth.getPrincipal().equals("anonymousUser")) {
            user=userService.findByUsername(((User) auth.getPrincipal()).getUsername());
            logged = true;
            isLiked = userLikesService.isLiked(user.getId(), id);
            model.addAttribute("user",user);
            model.addAttribute("comment", new Comment(restaurant, user));
            boolean userCommented = restaurantService.alreadyCommented(user.getId(),restaurant.getId());
            model.addAttribute("userCommented",userCommented);
        }
        model.addAttribute("isLiked", isLiked);
        model.addAttribute("logged", logged);
        List restaurantPhotos= new ArrayList();
        List<Photo> photos = (List<Photo>)photoService.listAllPhotosById(id);
        byte[] encodeBase64;
        String base64Encoded;
        for(int i=0;i<photos.size();i++)
        {
            encodeBase64 = Base64.encode(photos.get(i).getPhoto());
            base64Encoded = new String(encodeBase64,"UTF-8");
            restaurantPhotos.add(base64Encoded);
        }
        model.addAttribute("photos", restaurantPhotos );
        return "restaurantUserView";
    }

    @RequestMapping(value = "/restaurant/like/{restaurantId}")
    public String like(@PathVariable Integer restaurantId) {
        UserLike ul = new UserLike();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User u = (org.springframework.security.core.userdetails.User)auth.getPrincipal();
        com.ucbcba.demo.entities.User user = userService.findByUsername(u.getUsername());
        ul.setUser(user);
        Restaurant r = restaurantService.getRestaurant(restaurantId);
        ul.setRestaurant(r);
        userLikesService.saveUserLike(ul);
        System.out.println("entra");
        return "redirect:/restaurant/" + restaurantId;
    }

    @RequestMapping(value = "/restaurant/dislike/{restaurantId}")
    public String dislike(@PathVariable Integer restaurantId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User u = (org.springframework.security.core.userdetails.User)auth.getPrincipal();
        com.ucbcba.demo.entities.User user = userService.findByUsername(u.getUsername());
        userLikesService.deleteUserLike(user.getId(), restaurantId);
        return "redirect:/restaurant/" + restaurantId;
    }
}
