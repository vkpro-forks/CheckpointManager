package ru.ac.checkpointmanager.model.passes;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;

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

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String name;

    @Enumerated(EnumType.STRING)
    private PassStatus status;

    @Enumerated(EnumType.STRING)
    private PassTypeTime typeTime;

    @ManyToOne
    @JoinColumn(name = "territory_id")
    private Territory territory;

    private String note;

    @CreationTimestamp(source = SourceType.VM)
    private LocalDateTime addedAt;

    private LocalDateTime startTime;

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