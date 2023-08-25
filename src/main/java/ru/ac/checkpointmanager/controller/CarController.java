    package ru.ac.checkpointmanager.controller;

    import jakarta.validation.Valid;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.validation.BindingResult;
    import org.springframework.web.bind.annotation.*;
    import ru.ac.checkpointmanager.exception.CarBrandNotFoundException;
    import ru.ac.checkpointmanager.exception.CarModelNotFoundException;
    import ru.ac.checkpointmanager.model.Car;
    import ru.ac.checkpointmanager.model.CarBrand;
    import ru.ac.checkpointmanager.model.CarModel;
    import ru.ac.checkpointmanager.service.CarBrandService;
    import ru.ac.checkpointmanager.service.CarModelService;
    import ru.ac.checkpointmanager.service.CarService;

    import java.util.List;
    import java.util.Optional;
    import java.util.UUID;

    @RestController
    @RequestMapping("/car")
    @RequiredArgsConstructor
    public class CarController {

        private final CarService carService;
        private final CarBrandService carBrandService;
        private final CarModelService carModelService;


        @PostMapping
        public ResponseEntity<?> addCar(@RequestBody Car carRequest) {
            if (!validateLicensePlate(carRequest.getLicensePlate())) {
                return new ResponseEntity<>("Invalid license plate", HttpStatus.BAD_REQUEST);
            }
            CarBrand existingBrand = carBrandService.getBrandById(carRequest.getBrand().getId());
            CarModel existingModel = carModelService.getModelById(carRequest.getModel().getId());

            carRequest.setBrand(existingBrand);
            carRequest.setModel(existingModel);

            Car newCar = carService.addCar(carRequest);
            return new ResponseEntity<>(newCar, HttpStatus.CREATED);
        }

        @PutMapping("/{carId}")
        public ResponseEntity<?> updateCar(@PathVariable UUID carId, @RequestBody Car updateCar) {
            if (!validateLicensePlate(updateCar.getLicensePlate())) {
                return ResponseEntity.badRequest().body("Invalid license plate");
            }
            Car updated = carService.updateCar(carId, updateCar);
            return ResponseEntity.ok(updated);
        }

        @DeleteMapping("/{carId}")
        public ResponseEntity<Void> deletedCar(@PathVariable UUID carId) {
            carService.deleteCar(carId);
            return ResponseEntity.noContent().build();
        }

        private boolean validateLicensePlate(String licensePlate) {

            if (licensePlate == null) {
                return false;
            }

            String invalidLetters = "йцгшщзфыплджэячьъбю";
            for (char letter : invalidLetters.toCharArray()) {
                if (licensePlate.contains(String.valueOf(letter))) {
                    return false;
                }
            }
            return true;
        }

        @GetMapping
        public ResponseEntity<List<Car>> getAllCars() {
            List<Car> carList = carService.getAllCars();
            if (carList.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(carList);
        }
    }
