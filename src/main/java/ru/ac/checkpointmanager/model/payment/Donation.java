package ru.ac.checkpointmanager.model.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Type;
import ru.ac.checkpointmanager.model.AbstractBaseEntity;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.usertype.CurrencyType;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "donations")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Getter
@Setter
public class Donation extends AbstractBaseEntity {

    @NotNull
    @Column(name = "amount")
    BigDecimal amount;

    @Type(CurrencyType.class)
    @Column(name = "currency", columnDefinition = "currency_enum")
    CurrencyEnum currency;

    @NotNull
    @Column(name = "comment", length = 128)
    String comment;

    @NotNull
    @Column(name = "confirmed")
    Boolean confirmed;

    @NotNull
    @Column(name = "status")
    String status;

    @NotNull
    @Column(name = "performed_at")
    ZonedDateTime performedAt;

    @Column(name = "description")
    String description;

    @Column(name = "payment_id")
    UUID paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;
}
