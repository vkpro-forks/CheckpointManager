package ru.ac.checkpointmanager.security.authfacade;

import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.repository.car.CarRepository;

import java.util.UUID;

@Component("carAuthFacade")
public final class CarAuthFacade implements AuthFacade {
    private final CarRepository carRepository;

    public CarAuthFacade(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    /**
     * Проверка, есть ли у пользователя хотя бы одни пропуск, в котором
     * фигурирует машина с переданным Id
     *
     * @param carId Id машины
     * @return true - если есть связь с машиной, false - если нет
     */
    @Override
    public boolean isIdMatch(UUID carId) {
        UUID userId = getCurrentUser().getId();
        return carRepository.checkIfUserHasPassByCarId(userId, carId);
    }
}
