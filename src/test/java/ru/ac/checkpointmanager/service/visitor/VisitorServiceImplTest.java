package ru.ac.checkpointmanager.service.visitor;

import jakarta.persistence.EntityNotFoundException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.test.util.ReflectionTestUtils;
import ru.ac.checkpointmanager.dto.VisitorDTO;
import ru.ac.checkpointmanager.exception.ExceptionUtils;
import ru.ac.checkpointmanager.exception.VisitorNotFoundException;
import ru.ac.checkpointmanager.mapper.VisitorMapper;
import ru.ac.checkpointmanager.model.Visitor;
import ru.ac.checkpointmanager.repository.VisitorRepository;
import ru.ac.checkpointmanager.util.TestUtils;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class VisitorServiceImplTest {

    @Mock
    VisitorRepository visitorRepository;

    @InjectMocks
    VisitorServiceImpl visitorService;

    @Captor
    ArgumentCaptor<Visitor> visitorArgumentCaptor;

    VisitorMapper visitorMapper = new VisitorMapper(new ModelMapper());

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(visitorService, "visitorMapper", visitorMapper);
    }

    @Test
    void addVisitor_AllOk_SaveAndReturnDTO() {
        VisitorDTO visitorDTO = TestUtils.getVisitorDTO();
        Mockito.when(visitorRepository.save(Mockito.any())).thenReturn(TestUtils.getVisitorUnsaved());

        VisitorDTO returnedDto = visitorService.addVisitor(visitorDTO);

        Mockito.verify(visitorRepository, Mockito.times(1)).save(visitorArgumentCaptor.capture());
        Visitor captured = visitorArgumentCaptor.getValue();
        checkSavedVisitorFields(captured, visitorDTO);
        Assertions.assertThat(returnedDto).isNotNull();
    }

    @Test
    void getVisitor_AllOk_ReturnVisitorDto() {
        Visitor visitor = TestUtils.getVisitorUnsaved();
        visitor.setId(TestUtils.VISITOR_ID);
        Mockito.when(visitorRepository.findById(TestUtils.VISITOR_ID)).thenReturn(Optional.of(visitor));

        VisitorDTO returnedVisitor = visitorService.getVisitor(TestUtils.VISITOR_ID);

        Mockito.verify(visitorRepository, Mockito.times(1)).findById(TestUtils.VISITOR_ID);
        Assertions.assertThat(returnedVisitor).isEqualTo(visitorMapper.toVisitorDTO(visitor));
    }

    @Test
    void getVisitor_NoVisitor_ThrowException() {
        Mockito.when(visitorRepository.findById(TestUtils.VISITOR_ID)).thenReturn(Optional.empty());

        Assertions.assertThatExceptionOfType(VisitorNotFoundException.class)
                .isThrownBy(() -> visitorService.getVisitor(TestUtils.VISITOR_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .withMessage(ExceptionUtils.VISITOR_NOT_FOUND.formatted(TestUtils.VISITOR_ID));

        Mockito.verify(visitorRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void updateVisitor_AllOk_UpdateVisitorAndReturnDto() {
        VisitorDTO visitorDTO = TestUtils.getVisitorDTO();
        visitorDTO.setName("Huggy Wuggy");
        Visitor visitor = TestUtils.getVisitorUnsaved();
        Mockito.when(visitorRepository.findById(TestUtils.VISITOR_ID)).thenReturn(Optional.of(visitor));
        Mockito.when(visitorRepository.save(Mockito.any())).thenReturn(visitor);
        visitorService.updateVisitor(TestUtils.VISITOR_ID, visitorDTO);

        Mockito.verify(visitorRepository, Mockito.times(1)).save(visitorArgumentCaptor.capture());
        Visitor captured = visitorArgumentCaptor.getValue();
        checkSavedVisitorFields(captured, visitorDTO);
    }

    private void checkSavedVisitorFields(Visitor captured, VisitorDTO visitorDTO) {
        Assertions.assertThat(captured.getName()).isEqualTo(visitorDTO.getName());
        Assertions.assertThat(captured.getPhone()).isEqualTo(visitorDTO.getPhone());
        Assertions.assertThat(captured.getNote()).isEqualTo(visitorDTO.getNote());
    }

}