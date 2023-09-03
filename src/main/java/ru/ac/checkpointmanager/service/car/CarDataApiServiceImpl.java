package ru.ac.checkpointmanager.service.car;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.service.car.CarDataApiService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@RequiredArgsConstructor
public class CarDataApiServiceImpl implements CarDataApiService {

    private static final String API_KEY = "dff24af1a1mshd56bcb5f8268c84p1cc70bjsna9726c66d5eb";
    private static final String API_HOST = "car-data.p.rapidapi.com";


    @Override
    public String fetchCarData(String limit, String page) {
        System.out.println("Method fetchCarData is called");

        String apiUrl = "https://car-data.p.rapidapi.com/cars?limit=" + limit + "&page=" + page;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("X-RapidAPI-Key", API_KEY)
                .header("X-RapidAPI-Host", API_HOST)
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            if (statusCode == 200) {
                return response.body();
            } else {
                System.err.println("API request failed with status code: " + statusCode);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String fetchCarDataWithBrandAndModel(String limit, String page, String brand, String model) {
        System.out.println("Method fetchCarDataWithBrandAndModel is called");

        StringBuilder apiUrlBuilder = new StringBuilder("https://car-data.p.rapidapi.com/cars?");

        apiUrlBuilder.append("limit=").append(limit).append("&page=").append(page);

        if (brand != null) {
            apiUrlBuilder.append("&make=").append(brand);
        }


        String apiUrl = apiUrlBuilder.toString();
        System.out.println("API URL: " + apiUrl);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("X-RapidAPI-Key", API_KEY)
                .header("X-RapidAPI-Host", API_HOST)
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            if (statusCode == 200) {
                return response.body();
            } else {
                System.err.println("API request failed with status code: " + statusCode);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
