package com.ucbcba.demo.controllers;

import com.ucbcba.demo.entities.Category;
import com.ucbcba.demo.entities.City;
import com.ucbcba.demo.entities.Country;
import com.ucbcba.demo.entities.Restaurant;
import com.ucbcba.demo.services.CityService;
import com.ucbcba.demo.services.CountryService;
import com.ucbcba.demo.services.RestaurantService;
import com.ucbcba.demo.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final UserService userService;
    private final RestaurantService restaurantService;
    private final CityService cityService;
    private final CountryService countryService;

    public HomeController(UserService userService, RestaurantService restaurantService, CityService cityService, CountryService countryService) {
        this.userService = userService;
        this.restaurantService = restaurantService;
        this.cityService = cityService;
        this.countryService = countryService;
    }

    @RequestMapping(value = {"/", "/home"}, method = RequestMethod.GET)
    public String welcome(Model model) {
        Boolean logged = false;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getPrincipal().equals("anonymousUser")) {
            logged = true;
        }
        model.addAttribute("logged", logged);
        model.addAttribute("cities", cityService.listAllCities());
        model.addAttribute("restaurants", restaurantService.listAllRestaurants());
        return "home";
    }

    @RequestMapping(value = {"/search"}, method = RequestMethod.POST)
    public String search(Model model, String searchFilter) {

        List<Restaurant> restaurantList = new ArrayList<>();
        for (Restaurant restaurant : restaurantService.listAllRestaurants()) {
            restaurantList.add(restaurant);
        }

        List<Country> countryList = new ArrayList<>();
        for (Country country : countryService.listAllCountries()) {
            countryList.add(country);
        }

        List<Restaurant> restaurants = restaurantList.stream().filter(
                p -> (p.getName().toLowerCase().contains(searchFilter.toLowerCase())
                        || searchCategories(p.getCategories(), searchFilter.toLowerCase()))
        ).collect(Collectors.toList());

        /*List<Country> countries = countryList.stream().filter(
                p -> (p.getName().toLowerCase().contains(searchFilter.toLowerCase())
                        || searchCities(p.getCities(), searchFilter.toLowerCase()))
        ).collect(Collectors.toList());*/

        Boolean logged = false;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getPrincipal().equals("anonymousUser")) {
            logged = true;
        }
        model.addAttribute("logged", logged);
        model.addAttribute("restaurants", restaurants);
        model.addAttribute("cities", cityService.listAllCities());
        model.addAttribute("countries", countryService.listAllCountries());
        return "home";
    }

    private Boolean searchCategories(Set<Category> categories, String param) {
        for (Category category : categories) {
            if (category.getName().toLowerCase().contains(param))
                return true;
        }
        return false;
    }

    private Boolean searchCities(Set<City> cities, String param) {
        for (City city : cities) {
            if (city.getName().toLowerCase().contains(param))
                return true;
        }
        return false;
    }
}
