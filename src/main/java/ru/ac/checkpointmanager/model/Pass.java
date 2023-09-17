package ru.ac.checkpointmanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.ac.checkpointmanager.model.enums.PassStatus;
import ru.ac.checkpointmanager.model.enums.PassTypeTime;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "passes")
@Data
@RequiredArgsConstructor
public class Pass {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull()
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull()
    @Enumerated(EnumType.STRING)
    private PassStatus status;

    @NotNull()
    @Enumerated(EnumType.STRING)
    private PassTypeTime typeTime;

    @NotNull()
    @ManyToOne
    @JoinColumn(name = "territory_id")
    private Territory territory;
    private String note;

    @NotNull()
    private LocalDateTime addedAt;

    @NotNull()
    @FutureOrPresent
    private LocalDateTime startTime;

    @NotNull()
    @FutureOrPresent
    private LocalDateTime endTime;

    //добавить оба когда будут реализованы кар и персон
    //+бд и дто
//    @ManyToOne
//    @JoinColumn(name = "car_id")
//    private Car car;

//    @ManyToOne
//    @JoinColumn(name = "person_id")
//    private Person person;

}