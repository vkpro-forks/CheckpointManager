package ru.ac.checkpointmanager.controller.payment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ac.checkpointmanager.dto.payment.DonationPerformingResponseDto;
import ru.ac.checkpointmanager.dto.payment.DonationRequestDto;
import ru.ac.checkpointmanager.service.payment.DonationApiService;

@RestController
@RequestMapping("/api/v1/donations")
@RequiredArgsConstructor
@Validated
@Tag(name = "Управление донатами", description = "Отправка, просмотр, управление донатами")
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
@SecurityRequirement(name = "bearerAuth")
public class DonationController {

    private final DonationApiService donationApiService;

    @Operation(summary = "Создание платежа для доната", responses = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Платеж успешно создан, ожидание оплаты на сервере ЮКассы",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DonationPerformingResponseDto.class))}
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            BAD REQUEST: Ошибки валидации:
                            Amount: значение менее 50, или не имеет цифрового формата
                            Currency: нет поддерживаемый код валюты
                            Comment: должен быть от 5 до 120 символов"""
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "INTERNAL SERVER ERROR - ошибка на стороне сервера (недоступен API платежной системы"
            )
    })
    @PostMapping
    public DonationPerformingResponseDto donate(@io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(schema = @Schema(implementation = DonationRequestDto.class),
                    mediaType = MediaType.APPLICATION_JSON_VALUE))
                                                @Valid @RequestBody DonationRequestDto donationRequestDto) {
        return donationApiService.makeDonation(donationRequestDto);
    }
}
