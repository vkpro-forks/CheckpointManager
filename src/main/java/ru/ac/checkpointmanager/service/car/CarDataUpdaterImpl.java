package ru.ac.checkpointmanager.service.car;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.model.car.CarData;
import ru.ac.checkpointmanager.repository.car.CarBrandRepository;

import java.util.*;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class CarDataUpdaterImpl implements CarDataUpdater {

    private final CarBrandRepository carBrandRepository;
    private final CarDataApiService carDataApiService;

    @Override
    public List<String> updateCarDataFromAPI(String limit, String page) {
        log.info("Fetching car data with limit {} and page {}", limit, page);
        String responseBody = carDataApiService.fetchCarData(limit, page);

        if (responseBody != null) {
            List<CarData> carDataSet = parseResponse(responseBody);
            updateDatabase(carDataSet);
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

        List<CarData> carDataSet = new ArrayList<>();

        JsonArray jsonArray = JsonParser.parseString(responseBody).getAsJsonArray();
        for (JsonElement element : jsonArray) {
            JsonObject carJsonObject = element.getAsJsonObject();
            String brandName = carJsonObject.get("make").getAsString();
            String modelName = carJsonObject.get("model").getAsString();
            carDataSet.add(new CarData(brandName, modelName));

            System.out.println("Parsed car data: Brand=" + brandName + ", Model=" + modelName);
        }

        return carDataSet;
    }

    private void updateDatabase(List<CarData> carDataList) {
        log.info("Updating database");
        for (CarData carData : carDataList) {
            System.out.println(carData.toString());
            CarBrand carBrand = carBrandRepository.findByBrand(carData.getBrandName());

            if (carBrand == null) {
                carBrand = new CarBrand();
                carBrand.setBrand(carData.getBrandName());
                carBrandRepository.save(carBrand);
            }
        }
    }

}
