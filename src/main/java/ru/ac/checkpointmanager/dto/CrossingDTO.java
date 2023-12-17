package ru.ac.checkpointmanager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ac.checkpointmanager.model.enums.Direction;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrossingDTO {

    @NotNull
    private UUID passId;

    @NotNull
    private UUID checkpointId;

    //нужно ли здесь это поле? Сейчас время присваивается с помощью аннотации @CreationTimestamp над полем в сущности.
    //с другой стороны тогда мы не учитываем риски задержки прохождения запроса - возможно, лучше все таки
    //передавать фактическое время пересечения с клиента, и сохранять в бд именно его (тогда убирать аннотацию из сущности)
    //п.с. еще предлагаю это поле везде переименовать, например, просто time, или crossTime
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime localDateTime;

    @NotNull
    private Direction direction;
}
