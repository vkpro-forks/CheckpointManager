package ru.ac.checkpointmanager.validation.it;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.ac.checkpointmanager.config.OpenAllEndpointsTestConfiguration;
import ru.ac.checkpointmanager.config.ValidationTestConfiguration;
import ru.ac.checkpointmanager.controller.AvatarController;
import ru.ac.checkpointmanager.model.avatar.AvatarProperties;
import ru.ac.checkpointmanager.service.avatar.AvatarService;
import ru.ac.checkpointmanager.util.TestUtils;
import ru.ac.checkpointmanager.util.UrlConstants;

@WebMvcTest(AvatarController.class)
@Import({AvatarProperties.class, ValidationTestConfiguration.class, OpenAllEndpointsTestConfiguration.class})
@WithMockUser(roles = {"ADMIN"})
@ActiveProfiles("test")
class AvatarControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    AvatarService avatarService;

    @Test
    @SneakyThrows
    void shouldReturnValidationErrorForUploadAvatar() {
        MockMultipartFile file
                = new MockMultipartFile("avatarFile", "avatar.buba", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3});
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.multipart(
                HttpMethod.POST, UrlConstants.AVATAR_URL + "/" + TestUtils.USER_ID).file(file));
        TestUtils.checkCommonValidationFields(resultActions);
        resultActions.andExpect(MockMvcResultMatchers
                .jsonPath(TestUtils.JSON_VIOLATIONS_FIELD.formatted(0)).value("avatarFile"));
    }

}
