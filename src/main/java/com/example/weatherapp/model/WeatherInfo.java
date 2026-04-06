package com.example.weatherapp.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WeatherInfo {

    private String city;
    private String country;
    private double temperature;
    private double feelsLike;
    private double tempMin;
    private double tempMax;
    private int humidity;
    private double windSpeed;
    private String condition;
    private String description;
}
