package com.example.project.Service.Impl;

import com.example.project.Entity.Location;
import com.example.project.Repository.LocationRepository;
import com.example.project.Service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationServiceImpl implements LocationService {

    @Autowired
    private LocationRepository locationRepository;

    @Override
    public List<String> getStates(String country) {
        return locationRepository.findDistinctByCountry(country);
    }

    @Override
    public List<String> getCountry() {
        return locationRepository.findDistinctCountries();
    }

    @Override
    public List<Location> getCities(String country, String state) {
        return locationRepository.findLocationByCountryAndState(country, state);
    }

    @Override
    public Location getLocation(Long locationId) {
        return locationRepository.findById(locationId).get();
    }
}
