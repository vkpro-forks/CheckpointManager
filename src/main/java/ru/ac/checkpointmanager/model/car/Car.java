package ru.ac.checkpointmanager.model.car;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import ru.ac.checkpointmanager.model.passes.PassAuto;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cars")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"brand", "passes"})
@SQLDelete(sql = """
        UPDATE cars
        SET deleted = true
        WHERE id = ?
        """)
@Where(clause = "deleted = false")
public class Car {

    @Id
    @EqualsAndHashCode.Include
    // @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(name = "license_plate")
    @Size(min = 6, max = 10, message = "The number must be at least 6 characters and no more than 10")
    @Pattern(regexp = "^[АВЕКМНОРСТУХA-Z0-9]+$", message = "Invalid characters in license plate")
    private String licensePlate;

    @ManyToOne
    @JoinColumn(name = "brand_id")
    private CarBrand brand;


    @OneToMany(mappedBy = "car")
    private List<PassAuto> passes;

    @Column(name = "car_phone")
    private String phone;

    @Column(name = "deleted")
    private Boolean deleted = Boolean.FALSE;

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate.toUpperCase();
    }
}
