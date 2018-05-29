package com.ucbcba.demo.repositories;

import com.ucbcba.demo.entities.Country;
import org.springframework.data.repository.CrudRepository;
import javax.transaction.Transactional;

@Transactional
public interface CountryRepository extends CrudRepository<Country, Integer> {

}
