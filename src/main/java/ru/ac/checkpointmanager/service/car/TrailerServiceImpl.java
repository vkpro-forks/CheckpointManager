package ru.ac.checkpointmanager.service.car;

import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.exception.CarTrailerNotFoundException;
import ru.ac.checkpointmanager.model.car.Trailer;
import ru.ac.checkpointmanager.repository.car.TrailerRepository;
import ru.ac.checkpointmanager.service.avatar.AvatarService;

import java.util.UUID;

@Service
public class TrailerServiceImpl implements TrailerService {

    private final TrailerRepository trailerRepository;
    private final AvatarService avatarService;

    public TrailerServiceImpl(TrailerRepository trailerRepository,
                              AvatarService avatarService) {
        this.trailerRepository = trailerRepository;
        this.avatarService = avatarService;
    }

    @Override
    public Trailer addTrailer(Trailer trailer) {
        return trailerRepository.save(trailer);
    }

    @Override
    public Trailer getTrailer(UUID id) {
        return trailerRepository.findById(id).orElseThrow(() ->
                new CarTrailerNotFoundException("dTrailer not found with id: " + id));
    }

    @Override
    public void deleteTrailer(UUID id) {
        if (trailerRepository.existsById(id)) {
            trailerRepository.deleteById(id);
        } else {
            throw new CarTrailerNotFoundException("dTrailer not found with id: " + id);
        }
        avatarService.deleteAvatarIfExists(id);
    }

    @Override
    public Trailer updateTrailer(Trailer trailer, UUID id) {
        if (trailerRepository.existsById(id)) {
            trailer.setId(id);
            return trailerRepository.save(trailer);
        } else {
            throw new CarTrailerNotFoundException("dTrailer not found with id: " + id);
        }
    }

    public boolean trailerExistsByLicensePlate(String licensePlate) {
        return trailerRepository.existsByLicensePlate(licensePlate);
    }

}
