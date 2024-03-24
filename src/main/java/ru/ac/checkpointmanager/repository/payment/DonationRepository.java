package ru.ac.checkpointmanager.repository.payment;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import ru.ac.checkpointmanager.model.payment.Donation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DonationRepository extends JpaRepository<Donation, UUID> {

    @EntityGraph(attributePaths = "user")
    Optional<Donation> findOneById(UUID uuid);

    @EntityGraph(attributePaths = "user")
    @NonNull
    List<Donation> findAll();
}
