package ru.ac.checkpointmanager.service.car;

import ru.ac.checkpointmanager.model.car.Trailer;

public interface CarTrailerService {

    Trailer addTrailer(Trailer trailer);

    Trailer getTrailer(Long id);

    void deleteTrailer(Long id);

    Trailer updateTrailer(Trailer trailer, Long id);
}
