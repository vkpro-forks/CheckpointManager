package ru.ac.checkpointmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;
import ru.ac.checkpointmanager.exception.CarBrandNotFoundException;
import ru.ac.checkpointmanager.exception.CarModelNotFoundException;
import ru.ac.checkpointmanager.model.Car;
import ru.ac.checkpointmanager.model.CarBrand;
import ru.ac.checkpointmanager.model.CarModel;
import ru.ac.checkpointmanager.service.CarBrandService;
import ru.ac.checkpointmanager.service.CarModelService;
import ru.ac.checkpointmanager.service.CarService;

import static org.hamcrest.Matchers.is;


import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(CarController.class)
@ActiveProfiles("test")
public class CarControllerTestMvc {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CarService carService;

    @MockBean
    private CarBrandService carBrandService;

    @MockBean
    private CarModelService carModelService;

    @InjectMocks
    private CarController controller;

    private ObjectMapper objectMapper = new ObjectMapper();

    //======================Testing each endpoint=======================/

    @Test
    public void testGetAllCars_returnsNoContent_whenNoCarsAvailable() throws Exception {
        when(carService.getAllCars()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/car"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testGetAllCars_returnsListOfCars_whenCarsAvailable() throws Exception {
        List<Car> cars = List.of(new Car());
        when(carService.getAllCars()).thenReturn(cars);

        mockMvc.perform(get("/car"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    //==================================Test for invalid license plate length============//

    @Test
    public void testAddCar_returnsBadRequest_whenBrandIsNull() throws Exception {
        Car car = new Car();
        car.setLicensePlate("ABCD123");

        mockMvc.perform(post("/car")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(car)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Brand is missing in the request"));
    }

    @Test
    public void testAddCar_returnsBadRequest_whenModelIsNull() throws Exception {
        Car car = new Car();
        CarBrand validBrand = new CarBrand();
        validBrand.setId(1L);
        car.setBrand(validBrand);
        car.setLicensePlate("ABCD123");

        mockMvc.perform(post("/car")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(car)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid brand ID"));
    }

    @Test
    public void testAddCar_returnsBadRequest_whenInvalidLicensePlate() throws Exception {
        Car car = new Car();
        CarBrand validBrand = new CarBrand();
        validBrand.setId(1L);
        car.setBrand(validBrand);
        CarModel validModel = new CarModel();
        validModel.setId(1L);
        car.setModel(validModel);
        car.setLicensePlate("йцг");

        when(carBrandService.getBrandById(1L)).thenReturn(validBrand);
        when(carModelService.getModelById(1L)).thenReturn(validModel);

        mockMvc.perform(post("/car")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(car)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid license plate"));
    }

    //=============================Test for invalid brand and model================================//

    @Test
    public void testAddCar_returnsBadRequest_whenInvalidModelId() throws Exception {
        Car car = new Car();
        CarBrand validBrand = new CarBrand();
        validBrand.setId(1L);
        car.setBrand(validBrand);
        CarModel invalidModel = new CarModel();
        invalidModel.setId(999L);
        car.setModel(invalidModel);

        when(carBrandService.getBrandById(1L)).thenReturn(validBrand);
        when(carModelService.getModelById(999L)).thenReturn(null);

        mockMvc.perform(post("/car")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(car)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid model ID"));
    }

    @Test
    public void testAddCar_returnsBadRequest_whenInvalidBrandId() throws Exception {
        Car car = new Car();
        CarBrand brand = new CarBrand();
        brand.setId(999L);
        car.setBrand(brand);

        when(carBrandService.getBrandById(999L)).thenReturn(null);

        mockMvc.perform(post("/car")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(car)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid brand ID"));
    }

    //=====================Testing car methods controllers================================//



    @Test
    public void testUpdateCar_returnsUpdatedCar() throws Exception {
        UUID carId = UUID.randomUUID();
        Car updateCar = new Car();
        Car updatedCar = new Car();

        when(carService.updateCar(eq(carId), any(Car.class))).thenReturn(updatedCar);

        mockMvc.perform(put("/car/" + carId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCar)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testDeleteCar_returnsNoContent() throws Exception {
        UUID carId = UUID.randomUUID();

        doNothing().when(carService).deleteCar(carId);

        mockMvc.perform(delete("/car/" + carId))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testAddCar_returnsOkResponse_whenValidCar() throws Exception {
        Car carRequest = new Car();

        CarBrand brand = new CarBrand();
        brand.setId(1L);
        carRequest.setBrand(brand);

        CarModel model = new CarModel();
        model.setId(1L);
        carRequest.setModel(model);

        carRequest.setLicensePlate("ABCD123");

        Car addedCar = new Car();
        addedCar.setId(UUID.randomUUID());

        when(carBrandService.getBrandById(anyLong())).thenReturn(brand);
        when(carModelService.getModelById(anyLong())).thenReturn(model);
        when(carService.addCar(any(Car.class))).thenReturn(addedCar);

        mockMvc.perform(post("/car")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(carRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Car added with ID: " + addedCar.getId()));
    }

    //====================Testing CarBrand method controllers=========================//

    @Test
    public void testCreateBrand_returnsBadRequest_whenValidationFails() throws Exception {
        CarBrand brand = new CarBrand();

        mockMvc.perform(post("/car/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(brand)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Validation error"));
    }

    @Test
    public void testCreateBrand_returnsCreated_whenValidBrand() throws Exception {
        CarBrand brand = new CarBrand();
        brand.setBrand("ValidBrandName");

        CarBrand createdBrand = new CarBrand();
        createdBrand.setBrand("ValidBrandName");

        when(carBrandService.addBrand(any())).thenReturn(createdBrand);

        mockMvc.perform(post("/car/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(brand)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testGetCarBrandById_returnsBrand() throws Exception {
        CarBrand brand = new CarBrand();
        brand.setBrand("ExistingBrand");

        when(carBrandService.getBrandById(1L)).thenReturn(brand);

        mockMvc.perform(get("/car/brands/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.brand", is("ExistingBrand")));
    }

    @Test
    public void testDeleteCarBrandById_successfulDelete() throws Exception {
        doNothing().when(carBrandService).deleteBrand(1L);

        mockMvc.perform(delete("/car/brands/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testUpdateCarBrand_successfulUpdate() throws Exception {
        CarBrand updatedBrand = new CarBrand();
        updatedBrand.setBrand("UpdatedBrand");

        when(carBrandService.updateBrand(eq(1L), any())).thenReturn(updatedBrand);

        CarBrand brandToUpdate = new CarBrand();
        brandToUpdate.setBrand("UpdatedBrand");

        mockMvc.perform(put("/car/brands/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(brandToUpdate)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.brand", is("UpdatedBrand")));
    }

    @Test
    public void testUpdateCarBrand_returnsBadRequest_whenInvalidData() throws Exception {
        CarBrand brandToUpdate = new CarBrand();

        when(carBrandService.updateBrand(anyLong(), any(CarBrand.class))).thenThrow(new CarBrandNotFoundException());

        mockMvc.perform(put("/car/brand/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(brandToUpdate)))
                .andExpect(status().isNotFound());
    }


    //====================Testing CarModel method controllers=========================//

    @Test
    public void testCreateModel_returnsBadRequest_whenValidationFails() throws Exception {
        CarModel model = new CarModel();

        mockMvc.perform(post("/car/models")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(model)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Validation error"));
    }

    @Test
    public void testCreateModel_returnsCreated_whenValidModel() throws Exception {
        CarModel model = new CarModel();
        model.setModel("ValidModelName");

        CarModel createdModel = new CarModel();
        createdModel.setModel("ValidModelName");

        when(carModelService.addModel(any())).thenReturn(createdModel);

        mockMvc.perform(post("/car/models")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(model)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testGetCarModelById_returnsCarModel() throws Exception {
        CarModel model = new CarModel();
        when(carModelService.getModelById(1L)).thenReturn(model);

        mockMvc.perform(get("/car/models/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testDeleteCarModelById() throws Exception {
        doNothing().when(carModelService).deleteModel(1L);

        mockMvc.perform(delete("/car/models/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testUpdateCarModel_returnsNotFound_whenModelNotFound() throws Exception {
        CarModel modelToUpdate = new CarModel();

        when(carModelService.updateModel(anyLong(), any(CarModel.class))).thenThrow(new CarModelNotFoundException());

        mockMvc.perform(put("/car/models/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modelToUpdate)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateCarModel_returnsOk_whenValidModel() throws Exception {
        CarModel modelToUpdate = new CarModel();
        CarModel updatedModel = new CarModel();
        updatedModel.setModel("UpdatedModel");

        when(carModelService.updateModel(1L, modelToUpdate)).thenReturn(updatedModel);

        mockMvc.perform(put("/car/models/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modelToUpdate)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.model", is("UpdatedModel")));
    }

    @Test
    public void testFindCarModelByName_returnsNotFound_whenNoModelFound() throws Exception {
        when(carModelService.findByModelIgnoreCase("test")).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/car/models/search-name?name=test"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testFindCarModelByName_returnsCarModels() throws Exception {
        List<CarModel> models = Arrays.asList(new CarModel(), new CarModel());
        when(carModelService.findByModelIgnoreCase("test")).thenReturn(models);

        mockMvc.perform(get("/car/models/search-name?name=test"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
