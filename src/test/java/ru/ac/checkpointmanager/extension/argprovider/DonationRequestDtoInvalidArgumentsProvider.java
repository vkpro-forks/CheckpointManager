package ru.ac.checkpointmanager.extension.argprovider;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import ru.ac.checkpointmanager.dto.payment.DonationRequestDto;
import ru.ac.checkpointmanager.model.payment.CurrencyEnum;

import java.math.BigDecimal;
import java.util.stream.Stream;

public class DonationRequestDtoInvalidArgumentsProvider implements ArgumentsProvider {

    private static final String COMMENT = "comment";
    private static final String AMOUNT = "amount";

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        DonationRequestDto negativeAmount = new DonationRequestDto(BigDecimal.valueOf(-1), CurrencyEnum.RUB, COMMENT);
        DonationRequestDto zeroAmount = new DonationRequestDto(BigDecimal.valueOf(0), CurrencyEnum.RUB, COMMENT);
        DonationRequestDto lessFiftyAmount = new DonationRequestDto(BigDecimal.TEN, CurrencyEnum.RUB, COMMENT);
        DonationRequestDto moreThanHundredThousand = new DonationRequestDto(BigDecimal.valueOf(100001), CurrencyEnum.RUB, COMMENT);
        DonationRequestDto nullAmount = new DonationRequestDto(null, CurrencyEnum.RUB, COMMENT);
        DonationRequestDto shortComment = new DonationRequestDto(BigDecimal.valueOf(50), CurrencyEnum.RUB, "hi");
        DonationRequestDto longComment = new DonationRequestDto(BigDecimal.valueOf(50), CurrencyEnum.RUB, "a".repeat(129));
        DonationRequestDto nullComment = new DonationRequestDto(BigDecimal.valueOf(50), CurrencyEnum.RUB, null);
        DonationRequestDto blankComment = new DonationRequestDto(BigDecimal.valueOf(50), CurrencyEnum.RUB, "");
        return Stream.of(
                Arguments.of(negativeAmount, AMOUNT),
                Arguments.of(zeroAmount, AMOUNT),
                Arguments.of(lessFiftyAmount, AMOUNT),
                Arguments.of(moreThanHundredThousand, AMOUNT),
                Arguments.of(nullAmount, AMOUNT),
                Arguments.of(shortComment, COMMENT),
                Arguments.of(longComment, COMMENT),
                Arguments.of(nullComment, COMMENT),
                Arguments.of(blankComment, COMMENT)
        );
    }
}
