package ru.ac.checkpointmanager.it.controller;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.result.ModelResultMatchers;
import ru.ac.checkpointmanager.config.RedisAndPostgresTestContainersConfiguration;
import ru.ac.checkpointmanager.dto.VisitorDTO;
import ru.ac.checkpointmanager.repository.VisitorRepository;
import ru.ac.checkpointmanager.util.TestUtils;
import ru.ac.checkpointmanager.util.UrlConstants;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = {"ADMIN"})
@Slf4j
class VisitorControllerIntegrationTest extends RedisAndPostgresTestContainersConfiguration {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    VisitorRepository visitorRepository;

    @AfterEach
    void clear() {
        visitorRepository.deleteAll();
    }

    @Test
    @SneakyThrows
    void addVisitor_AllOk_CreateAndRetudnVisitor() {
        VisitorDTO visitorDTO = TestUtils.getVisitorDTO();
        String visitorDTOToSend = TestUtils.jsonStringFromObject(visitorDTO);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.VISITOR_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(visitorDTOToSend));
        resultActions.andExpect(MockMvcResultMatchers.status().isCreated());


    }


}
