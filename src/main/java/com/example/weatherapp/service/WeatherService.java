package com.example.weatherapp.service;

import com.example.weatherapp.model.WeatherInfo;
import com.example.weatherapp.model.WeatherResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class WeatherService {

    private final RestTemplate restTemplate;

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.api.base-url}")
    private String baseUrl;

    @Value("${weather.api.units}")
    private String units;

    public WeatherService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public WeatherInfo getWeatherByCity(String city) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("q", city)
                .queryParam("appid", apiKey)
                .queryParam("units", units)
                .toUriString();

        try {
            WeatherResponse response = restTemplate.getForObject(url, WeatherResponse.class);

            if (response == null) {
                throw new RuntimeException("No data received from weather API");
            }

            return WeatherInfo.builder()
                    .city(response.getName())
                    .country(response.getSys().getCountry())
                    .temperature(response.getMain().getTemp())
                    .feelsLike(response.getMain().getFeelsLike())
                    .tempMin(response.getMain().getTempMin())
                    .tempMax(response.getMain().getTempMax())
                    .humidity(response.getMain().getHumidity())
                    .windSpeed(response.getWind().getSpeed())
                    .condition(response.getWeather().get(0).getMain())
                    .description(response.getWeather().get(0).getDescription())
                    .build();

        } catch (HttpClientErrorException.NotFound e) {
            throw new RuntimeException("City not found: " + city);
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new RuntimeException("Invalid API key. Please check your OpenWeatherMap API key.");
        }
    }
}
