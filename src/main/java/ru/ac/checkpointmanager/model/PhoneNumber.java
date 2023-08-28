package ru.ac.checkpointmanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.ac.checkpointmanager.model.enums.PhoneNumberType;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@Table(name = "phone_numbers")
public class PhoneNumber {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @NotEmpty
    @Size(min = 6, max = 11)
    @Pattern(regexp = "^\\d+$", message = "The number has to contain only numbers from 0 to 9\n" +
            "Example: \"79998885566\"")
    private String number;

    @Enumerated(EnumType.STRING)
    private PhoneNumberType type;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @NotNull
    private User user;

    private String note;
}
