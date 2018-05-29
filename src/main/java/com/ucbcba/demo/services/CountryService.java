package com.ucbcba.demo.services;

import com.ucbcba.demo.entities.Country;

import java.util.List;

public interface CountryService {

    Iterable<Country> listAllCountries();

    void saveCountry(Country country);

    Country getCountry(Integer id);

    void deleteCountry(Integer id);

}
