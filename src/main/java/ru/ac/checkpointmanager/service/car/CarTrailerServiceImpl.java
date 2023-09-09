package ru.ac.checkpointmanager.service.car;

import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.exception.CarTrailerNotFoundException;
import ru.ac.checkpointmanager.model.car.Trailer;
import ru.ac.checkpointmanager.repository.car.TrailerRepository;

@Service
public class CarTrailerServiceImpl implements CarTrailerService {

    private final TrailerRepository trailerRepository;

    public CarTrailerServiceImpl(TrailerRepository trailerRepository) {
        this.trailerRepository = trailerRepository;
    }

    @Override
    public Trailer addTrailer(Trailer trailer) {
        return trailerRepository.save(trailer);
    }

    @Override
    public Trailer getTrailer(Long id) {
        return trailerRepository.findById(id).orElseThrow(() ->
                new CarTrailerNotFoundException("dTrailer not found with id: " + id));
    }

    @Override
    public void deleteTrailer(Long id) {
        if (trailerRepository.existsById(id)) {
            trailerRepository.deleteById(id);
        } else {
            throw new CarTrailerNotFoundException("dTrailer not found with id: " + id);
        }
    }

    @Override
    public Trailer updateTrailer(Trailer trailer, Long id) {
        if (trailerRepository.existsById(id)) {
            trailer.setId(id);
            return trailerRepository.save(trailer);
        } else {
            throw new CarTrailerNotFoundException("dTrailer not found with id: " + id);
        }
    }
}
