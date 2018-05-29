package com.ucbcba.demo.services;

import com.ucbcba.demo.entities.Country;
import com.ucbcba.demo.repositories.CountryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class CountryServiceImpl implements CountryService {

    private CountryRepository countryRepository;

    @Autowired
    @Qualifier(value = "countryRepository")
    public void setCountryRepository(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    @Override
    public Iterable<Country> listAllCountries() {
        return countryRepository.findAll();
    }

    @Override
    public void saveCountry(Country country) {
        countryRepository.save(country);
    }

    @Override
    public Country getCountry(Integer id) {
        return countryRepository.findOne(id);
    }

    @Override
    public void deleteCountry(Integer id) {
        countryRepository.delete(id);
    }

}
