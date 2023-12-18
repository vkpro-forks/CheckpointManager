package ru.ac.checkpointmanager.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrossingRequestDTO {

    @NotNull
    private UUID passId;

    @NotNull
    private UUID checkpointId;

    //нужно ли здесь это поле? Сейчас время присваивается с помощью аннотации @CreationTimestamp над полем в сущности.
    //с другой стороны тогда мы не учитываем риски задержки прохождения запроса - возможно, лучше все таки
    //передавать фактическое время пересечения с клиента, и сохранять в бд именно его (тогда убирать аннотацию из сущности)
    //п.с. еще предлагаю это поле везде переименовать, например, просто time, или crossTime
    //-->>
    //Crossing time будет обрабатываться на сервере, здесь это поле не нужно, как и direction
    //Если кроссинг будет помещаться в очередь - то будем ставить ему дату после пассПроцесс, учитывая что пропуску
     //время мы присваиваем сервером, то кроссингу тоже нужно присваиваьб сервером

}
