package ru.ac.checkpointmanager.it.controller;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
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
import ru.ac.checkpointmanager.config.RedisAndPostgresTestContainersConfiguration;
import ru.ac.checkpointmanager.dto.VisitorDTO;
import ru.ac.checkpointmanager.exception.ExceptionUtils;
import ru.ac.checkpointmanager.model.Visitor;
import ru.ac.checkpointmanager.repository.VisitorRepository;
import ru.ac.checkpointmanager.util.TestUtils;
import ru.ac.checkpointmanager.util.UrlConstants;

import java.util.Optional;
import java.util.UUID;

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
    void addVisitor_AllOk_CreateAndReturnVisitor() {
        VisitorDTO visitorDTO = TestUtils.getVisitorDTO();
        String visitorDTOToSend = TestUtils.jsonStringFromObject(visitorDTO);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.VISITOR_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(visitorDTOToSend));

        resultActions.andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(visitorDTO.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.phone").value(visitorDTO.getPhone()));
    }

    @Test
    @SneakyThrows
    void getVisitor_AllOk_ReturnVisitor() {
        Visitor visitor = TestUtils.getVisitorUnsaved();
        Visitor savedVisitor = visitorRepository.saveAndFlush(visitor);
        UUID visitorId = savedVisitor.getId();

        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get(UrlConstants.VISITOR_URL + "/{visitorId}", visitorId));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(visitorId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(savedVisitor.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.phone").value(savedVisitor.getPhone()));
    }

    @Test
    @SneakyThrows
    void getVisitor_VisitorNotFound_HandleErrorAndReturnNotFound() {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .get(UrlConstants.VISITOR_URL + "/" + TestUtils.VISITOR_ID));
        TestUtils.checkNotFoundFields(resultActions);
        resultActions.andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                .value(ExceptionUtils.VISITOR_NOT_FOUND.formatted(TestUtils.VISITOR_ID)));
    }

    @Test
    @SneakyThrows
    void updateVisitor_AllOk_ReturnVisitor() {
        Visitor visitor = TestUtils.getVisitorUnsaved();
        Visitor savedVisitor = visitorRepository.saveAndFlush(visitor);
        UUID visitorId = savedVisitor.getId();
        Visitor visitorForUpdate = TestUtils.getVisitorUnsaved();
        visitorForUpdate.setName("Huggy Wuggy");
        String visitorDTOToSend = TestUtils.jsonStringFromObject(visitorForUpdate);

        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.put(UrlConstants.VISITOR_URL + "/{visitorId}", visitorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(visitorDTOToSend));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(visitorId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Huggy Wuggy"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.phone").value(savedVisitor.getPhone()));
    }

    @Test
    @SneakyThrows
    void updateVisitor_VisitorNotFound_HandleErrorAndReturnNotFound() {
        String visitorDto = TestUtils.jsonStringFromObject(TestUtils.getVisitorDTO());
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .put(UrlConstants.VISITOR_URL + "/" + TestUtils.VISITOR_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(visitorDto));
        TestUtils.checkNotFoundFields(resultActions);
        resultActions.andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                .value(ExceptionUtils.VISITOR_NOT_FOUND.formatted(TestUtils.VISITOR_ID)));
    }

    @Test
    @SneakyThrows
    void deleteVisitor_AllOk_DeleteAndReturnNoContent() {
        Visitor visitor = TestUtils.getVisitorUnsaved();
        Visitor savedVisitor = visitorRepository.saveAndFlush(visitor);
        UUID visitorId = savedVisitor.getId();

        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.delete(UrlConstants.VISITOR_URL + "/{visitorId}", visitorId));

        resultActions.andExpect(MockMvcResultMatchers.status().isNoContent());
        Optional<Visitor> optionalVisitor = visitorRepository.findById(visitorId);
        Assertions.assertThat(optionalVisitor).as("Check if visitor was not in repo").isEmpty();
    }

    @Test
    @SneakyThrows
    void deleteVisitor_VisitorNotFound_HandleErrorAndReturnNotFound() {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .delete(UrlConstants.VISITOR_URL + "/" + TestUtils.VISITOR_ID));
        TestUtils.checkNotFoundFields(resultActions);
        resultActions.andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                .value(ExceptionUtils.VISITOR_NOT_FOUND.formatted(TestUtils.VISITOR_ID)));
    }


}
