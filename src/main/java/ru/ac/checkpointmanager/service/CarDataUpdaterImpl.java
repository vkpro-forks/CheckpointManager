package ru.ac.checkpointmanager.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.model.CarBrand;
import ru.ac.checkpointmanager.model.CarData;
import ru.ac.checkpointmanager.model.CarModel;
import ru.ac.checkpointmanager.repository.CarBrandRepository;
import ru.ac.checkpointmanager.repository.CarModelRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CarDataUpdaterImpl implements CarDataUpdater {

    private final CarBrandRepository carBrandRepository;
    private final CarModelRepository carModelRepository;
    private final CarDataApiService carDataApiService;

    @Override
    public List<String> updateCarDataFromAPI(String limit, String page) {
        String responseBody = carDataApiService.fetchCarData(limit, page);

        if (responseBody != null) {
            List<CarData> carDataList = parseResponse(responseBody);
            updateDatabase(carDataList);
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> updateCarDataFromAPIWithBrandAndModel(String limit, String page, String brand, String model) {
        String responseBody = carDataApiService.fetchCarDataWithBrandAndModel(limit, page, brand, model);

        if (responseBody != null) {
            List<CarData> carDataList = parseResponse(responseBody);
            updateDatabase(carDataList);
        }
        return Collections.emptyList();
    }

    private List<CarData> parseResponse(String responseBody) {
        System.out.println("Method parseResponse is working");
        List<CarData> carDataList = new ArrayList<>();

        JsonArray jsonArray = JsonParser.parseString(responseBody).getAsJsonArray();
        for (JsonElement element : jsonArray) {
            JsonObject carJsonObject = element.getAsJsonObject();
            String brandName = carJsonObject.get("make").getAsString();
            String modelName = carJsonObject.get("model").getAsString();
            carDataList.add(new CarData(brandName, modelName));

            System.out.println("Parsed car data: Brand=" + brandName + ", Model=" + modelName);
        }

        return carDataList;
    }

    private void updateDatabase(List<CarData> carDataList) {
        for (CarData carData : carDataList) {
            CarBrand carBrand = carBrandRepository.findByBrandContainingIgnoreCase(carData.getBrandName());
            if (carBrand == null) {
                carBrand = new CarBrand();
                carBrand.setBrand(carData.getBrandName());
                carBrandRepository.save(carBrand);
            }

            CarModel carModel = carModelRepository.findByModel(carData.getModelName());
            if (carModel == null) {
                if (!carModelRepository.existsByBrandAndModel(carBrand, carData.getModelName())) {
                    carModel = new CarModel();
                    carModel.setModel(carData.getModelName());
                    carModel.setBrand(carBrand);
                    carModelRepository.save(carModel);
                }
            }
        }
    }
}
