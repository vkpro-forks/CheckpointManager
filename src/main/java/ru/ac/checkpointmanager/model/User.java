package ru.ac.checkpointmanager.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.ac.checkpointmanager.model.enums.UserRole;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "full_name")
    @NotEmpty
    @Size(min = 2, max = 100)
    @Pattern(regexp = "(?:[А-ЯA-Z][а-яa-z]*)(?:\\s+[А-ЯA-Z][а-яa-z]*)*",
            message = "The name has to start with a capital letter and contain only Latin or Cyrillic letters.\n" +
                    "Example: \"Ivanov Ivan Jovanovich\"")
    private String fullName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @NotEmpty
    @Size(min = 6, max = 20)
    private String mainNumber;

    @Email
    @NotEmpty(message = "Email should not be empty")
    private String email;

    @NotEmpty
    @Pattern(regexp = "^(?!.*\\s).+$", message = "Field should not contain spaces")//чтоб пароль без пробелов был
    @Size(min = 6, max = 20)
    private String password;

    @Column(name = "is_blocked")
    private Boolean isBlocked;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Phone> numbers;

    @Column(name = "added_at")
    private Timestamp addedAt;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_territory",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "territory_id"))
    private List<Territory> territories;

    @JsonIgnore
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Pass> pass;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

