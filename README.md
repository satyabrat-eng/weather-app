# Weather App — Spring Boot REST API

A Spring Boot application that fetches real-time weather data for any city using the **OpenWeatherMap API**.

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                        CLIENT / BROWSER                             │
│                  GET /api/weather?city=London                       │
└─────────────────────────┬───────────────────────────────────────────┘
                          │ HTTP Request
                          ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     SPRING BOOT APPLICATION                         │
│                        (Port: 8080)                                 │
│                                                                     │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │                   WeatherController                         │   │
│   │            @RestController  /api/weather                    │   │
│   │                                                             │   │
│   │   • Accepts: @RequestParam city                             │   │
│   │   • Returns: ResponseEntity<WeatherInfo> / error            │   │
│   └────────────────────────┬────────────────────────────────────┘   │
│                            │                                        │
│                            ▼                                        │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │                    WeatherService                            │   │
│   │                      @Service                               │   │
│   │                                                             │   │
│   │   • Builds API URL with city, apiKey, units                 │   │
│   │   • Calls OpenWeatherMap via RestTemplate                   │   │
│   │   • Maps WeatherResponse  ──►  WeatherInfo                  │   │
│   │   • Handles 404 (city not found) / 401 (invalid key)        │   │
│   └────────────────────────┬────────────────────────────────────┘   │
│                            │                                        │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │                      AppConfig                              │   │
│   │                   @Configuration                            │   │
│   │              RestTemplate Bean (injected)                   │   │
│   └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
│   ┌──────────────────────────┐  ┌──────────────────────────────┐   │
│   │     WeatherResponse      │  │        WeatherInfo            │   │
│   │   (Raw API response)     │  │    (Clean response DTO)       │   │
│   │                          │  │                               │   │
│   │  name, main.temp,        │  │  city, country, temperature,  │   │
│   │  main.humidity, wind,    │  │  feelsLike, tempMin, tempMax, │   │
│   │  weather[], sys.country  │  │  humidity, windSpeed,         │   │
│   │                          │  │  condition, description       │   │
│   └──────────────────────────┘  └──────────────────────────────┘   │
└─────────────────────────┬───────────────────────────────────────────┘
                          │ REST call via RestTemplate
                          ▼
┌─────────────────────────────────────────────────────────────────────┐
│                  EXTERNAL SYSTEM                                     │
│              OpenWeatherMap API                                      │
│    https://api.openweathermap.org/data/2.5/weather                  │
│                                                                     │
│    Query Params: ?q={city}&appid={apiKey}&units=metric              │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Request Flow

```
Client Request
     │
     │  GET /api/weather?city=London
     ▼
┌────────────────────┐
│  WeatherController │  ── validates request param
└────────┬───────────┘
         │  calls getWeatherByCity("London")
         ▼
┌────────────────────┐
│   WeatherService   │  ── builds URL with API key & units
└────────┬───────────┘
         │  HTTP GET via RestTemplate
         ▼
┌─────────────────────────────┐
│  OpenWeatherMap External API│
│  api.openweathermap.org     │
└────────┬────────────────────┘
         │  JSON Response
         ▼
┌────────────────────┐
│   WeatherResponse  │  ── raw JSON deserialized by Jackson
└────────┬───────────┘
         │  mapped/transformed
         ▼
┌────────────────────┐
│    WeatherInfo     │  ── clean DTO returned to controller
└────────┬───────────┘
         │  wrapped in ResponseEntity
         ▼
     Client Response
     (JSON payload)
```

---

## Project Structure

```
weather-app/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/example/weatherapp/
    │   │   ├── WeatherAppApplication.java      # Spring Boot entry point
    │   │   ├── config/
    │   │   │   └── AppConfig.java              # RestTemplate bean
    │   │   ├── controller/
    │   │   │   └── WeatherController.java      # REST endpoint
    │   │   ├── model/
    │   │   │   ├── WeatherResponse.java        # Raw API response model
    │   │   │   └── WeatherInfo.java            # Clean response DTO
    │   │   └── service/
    │   │       └── WeatherService.java         # Business logic & API call
    │   └── resources/
    │       └── application.properties          # App configuration
    └── test/
        └── java/com/example/weatherapp/
            └── WeatherAppApplicationTests.java
```

---

## Tech Stack

| Layer        | Technology                  |
|--------------|-----------------------------|
| Language     | Java 17                     |
| Framework    | Spring Boot 3.2.4           |
| HTTP Client  | RestTemplate (Spring Web)   |
| JSON Parsing | Jackson (auto-configured)   |
| Build Tool   | Maven                       |
| External API | OpenWeatherMap REST API     |

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.6+
- Free API key from [openweathermap.org](https://openweathermap.org/api)

### Configuration

Edit `src/main/resources/application.properties`:

```properties
weather.api.key=YOUR_API_KEY_HERE
weather.api.base-url=https://api.openweathermap.org/data/2.5/weather
weather.api.units=metric
```

> Set `units=imperial` for Fahrenheit or `units=standard` for Kelvin.

### Run the Application

```bash
./mvnw spring-boot:run
```

---

## API Reference

### Get Weather by City

```
GET /api/weather?city={cityName}
```

**Example Request:**
```
GET http://localhost:8080/api/weather?city=London
```

**Success Response (200 OK):**
```json
{
  "city": "London",
  "country": "GB",
  "temperature": 12.5,
  "feelsLike": 10.2,
  "tempMin": 11.0,
  "tempMax": 14.0,
  "humidity": 78,
  "windSpeed": 5.1,
  "condition": "Clouds",
  "description": "overcast clouds"
}
```

**Error Response (400 Bad Request):**
```json
{
  "error": "City not found: InvalidCity"
}
```

---

## Error Handling

| Scenario            | HTTP Status | Message                                        |
|---------------------|-------------|------------------------------------------------|
| City not found      | 400         | `City not found: {city}`                       |
| Invalid API key     | 400         | `Invalid API key. Please check your OpenWeatherMap API key.` |
| No data from API    | 400         | `No data received from weather API`            |
