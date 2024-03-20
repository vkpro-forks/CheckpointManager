package ru.ac.checkpointmanager.model.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
@Getter
@Setter
public class Donation extends AbstractBaseEntity {

    public static final String CREATED = "created";
    @NotNull
    @Column(name = "amount")
    BigDecimal amount;

    @Type(CurrencyType.class)
    @NotNull
    @Column(name = "currency", columnDefinition = "currency_enum")
    CurrencyEnum currency;

    @NotNull
    @Column(name = "comment", length = 128) //комментарий от пользователя
    String comment;

    @NotNull
    @Column(name = "confirmed")
    Boolean confirmed;

    @NotNull
    @Column(name = "status")
    String status;

    @Column(name = "performed_at")
    ZonedDateTime performedAt;

    @Column(name = "description") //описание с сервиса оплаты
    String description;

    @Column(name = "payment_id") //идентификатор оплаты
    UUID paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;

    public Donation(BigDecimal amount, CurrencyEnum currency, String comment) {
        this.amount = amount;
        this.currency = currency;
        this.comment = comment;
        this.status = CREATED;
        this.setConfirmed(false);
    }

    public Donation(UUID id, BigDecimal amount, CurrencyEnum currency, String comment) {
        super(id);
        this.amount = amount;
        this.currency = currency;
        this.comment = comment;
        this.status = CREATED;
    }

    public Donation(UUID id, BigDecimal amount, CurrencyEnum currency, String comment, Boolean confirmed, String status,
                    ZonedDateTime performedAt, String description, UUID paymentId) {
        super(id);
        this.amount = amount;
        this.currency = currency;
        this.comment = comment;
        this.confirmed = confirmed;
        this.status = status;
        this.performedAt = performedAt;
        this.description = description;
        this.paymentId = paymentId;
    }
}
