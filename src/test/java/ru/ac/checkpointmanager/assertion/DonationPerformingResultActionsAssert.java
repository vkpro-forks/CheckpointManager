package ru.ac.checkpointmanager.assertion;

import org.hamcrest.Matchers;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.ac.checkpointmanager.model.payment.CurrencyEnum;

import java.math.BigDecimal;

public class DonationPerformingResultActionsAssert extends ResultActionsAssert {

    public static DonationPerformingResultActionsAssert assertThat(ResultActions resultActions) {
        return new DonationPerformingResultActionsAssert(resultActions);
    }

    protected DonationPerformingResultActionsAssert(ResultActions resultActions) {
        super(resultActions);
    }

    public DonationPerformingResultActionsAssert returnUrlMatches(String returnUrl) throws Exception {
        isNotNull();
        actual.andExpect(MockMvcResultMatchers.jsonPath("$.paymentUrl", Matchers.equalTo(returnUrl)));
        return this;
    }

    public DonationPerformingResultActionsAssert amountMatches(BigDecimal amount, CurrencyEnum currency) throws Exception {
        isNotNull();
        actual.andExpect(MockMvcResultMatchers.jsonPath("$.amount.value", Matchers.equalTo(String.valueOf(amount))))
                .andExpect(MockMvcResultMatchers.jsonPath("$.amount.currency", Matchers.equalTo(currency.name())));
        return this;
    }

    public DonationPerformingResultActionsAssert descriptionMatches(String description) throws Exception {
        isNotNull();
        actual.andExpect(MockMvcResultMatchers.jsonPath("$.description", Matchers.equalTo(description)));
        return this;
    }


}
