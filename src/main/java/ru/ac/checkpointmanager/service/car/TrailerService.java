package ru.ac.checkpointmanager.service.car;

import ru.ac.checkpointmanager.model.car.Trailer;

import java.util.UUID;

public interface TrailerService {

    Trailer addTrailer(Trailer trailer);

    Trailer getTrailer(UUID id);

    void deleteTrailer(UUID id);

    Trailer updateTrailer(Trailer trailer, UUID id);

    boolean trailerExistsByLicensePlate(String licensePlate);


}
