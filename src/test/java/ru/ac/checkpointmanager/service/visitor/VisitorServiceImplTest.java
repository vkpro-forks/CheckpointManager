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

import java.util.Collections;
import java.util.List;
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
        Mockito.when(visitorRepository.save(Mockito.any())).thenReturn(TestUtils.getVisitorRandomUUID());

        VisitorDTO returnedDto = visitorService.addVisitor(visitorDTO);

        Mockito.verify(visitorRepository, Mockito.times(1)).save(visitorArgumentCaptor.capture());
        Visitor captured = visitorArgumentCaptor.getValue();
        checkSavedVisitorFields(captured, visitorDTO);
        Assertions.assertThat(returnedDto).isNotNull();
    }

    @Test
    void getVisitor_AllOk_ReturnVisitorDto() {
        Visitor visitor = TestUtils.getVisitorRandomUUID();
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
        Visitor visitor = TestUtils.getVisitorRandomUUID();
        Mockito.when(visitorRepository.findById(TestUtils.VISITOR_ID)).thenReturn(Optional.of(visitor));
        Mockito.when(visitorRepository.save(Mockito.any())).thenReturn(visitor);
        VisitorDTO returnedDto = visitorService.updateVisitor(TestUtils.VISITOR_ID, visitorDTO);

        Mockito.verify(visitorRepository, Mockito.times(1)).save(visitorArgumentCaptor.capture());
        Visitor captured = visitorArgumentCaptor.getValue();
        checkSavedVisitorFields(captured, visitorDTO);
        Assertions.assertThat(returnedDto).isNotNull();
    }

    @Test
    void updateVisitor_NoVisitor_ThrowException() {
        VisitorDTO visitorDTO = TestUtils.getVisitorDTO();
        Mockito.when(visitorRepository.findById(TestUtils.VISITOR_ID)).thenReturn(Optional.empty());

        Assertions.assertThatExceptionOfType(VisitorNotFoundException.class)
                .isThrownBy(() -> visitorService.updateVisitor(TestUtils.VISITOR_ID, visitorDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .withMessage(ExceptionUtils.VISITOR_NOT_FOUND.formatted(TestUtils.VISITOR_ID));

        Mockito.verify(visitorRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void deleteVisitor_AllOk_Delete() {
        Mockito.when(visitorRepository.existsById(TestUtils.VISITOR_ID)).thenReturn(true);

        visitorService.deleteVisitor(TestUtils.VISITOR_ID);

        Mockito.verify(visitorRepository, Mockito.times(1)).deleteById(TestUtils.VISITOR_ID);
    }

    @Test
    void deleteVisitor_NoVisitor_ThrowException() {
        Mockito.when(visitorRepository.existsById(TestUtils.VISITOR_ID)).thenReturn(false);

        Assertions.assertThatExceptionOfType(VisitorNotFoundException.class)
                .isThrownBy(() -> visitorService.deleteVisitor(TestUtils.VISITOR_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .withMessage(ExceptionUtils.VISITOR_NOT_FOUND.formatted(TestUtils.VISITOR_ID));

        Mockito.verify(visitorRepository, Mockito.never()).deleteById(TestUtils.VISITOR_ID);
    }

    @Test
    void findByNamePart_AllOk_ReturnListOfDto() {
        Visitor visitorUnsaved = TestUtils.getVisitorRandomUUID();
        Mockito.when(visitorRepository.findByNameContainingIgnoreCase(Mockito.any()))
                .thenReturn(List.of(visitorUnsaved));

        List<VisitorDTO> found = visitorService.findByNamePart("namepart");

        Assertions.assertThat(found).isEqualTo(visitorMapper.toVisitorDTOS(List.of(visitorUnsaved)));
    }

    @Test
    void findByNamePart_NoVisitors_ReturnEmptyList() {
        Mockito.when(visitorRepository.findByNameContainingIgnoreCase(Mockito.any()))
                .thenReturn(Collections.emptyList());

        List<VisitorDTO> found = visitorService.findByNamePart("namepart");

        Assertions.assertThat(found).isEmpty();
    }

    @Test
    void findByPhonePart_AllOk_ReturnListOfDto() {
        Visitor visitorUnsaved = TestUtils.getVisitorRandomUUID();
        Mockito.when(visitorRepository.findByPhoneContaining(Mockito.any()))
                .thenReturn(List.of(visitorUnsaved));

        List<VisitorDTO> found = visitorService.findByPhonePart("+7916");

        Assertions.assertThat(found).isEqualTo(visitorMapper.toVisitorDTOS(List.of(visitorUnsaved)));
    }

    @Test
    void findByPhonePart_NoVisitors_ReturnEmptyList() {
        Mockito.when(visitorRepository.findByPhoneContaining(Mockito.any()))
                .thenReturn(Collections.emptyList());

        List<VisitorDTO> found = visitorService.findByPhonePart("+7916");

        Assertions.assertThat(found).isEmpty();
    }

    @Test
    void findByPassId_AllOk_ReturnVisitorDto() {
        Visitor visitor = TestUtils.getVisitorRandomUUID();
        Mockito.when(visitorRepository.findVisitorByPasses_Id(TestUtils.PASS_ID)).thenReturn(Optional.of(visitor));

        VisitorDTO foundDto = visitorService.findByPassId(TestUtils.PASS_ID);

        Assertions.assertThat(foundDto).isEqualTo(visitorMapper.toVisitorDTO(visitor));
        Mockito.verify(visitorRepository, Mockito.times(1)).findVisitorByPasses_Id(TestUtils.PASS_ID);
    }

    @Test
    void findByPassId_NoVisitor_ThrowException() {
        Mockito.when(visitorRepository.findVisitorByPasses_Id(Mockito.any())).thenReturn(Optional.empty());

        Assertions.assertThatExceptionOfType(VisitorNotFoundException.class)
                .isThrownBy(() -> visitorService.findByPassId(TestUtils.PASS_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .withMessage(ExceptionUtils.VISITOR_BY_PASS_NOT_FOUND.formatted(TestUtils.PASS_ID));
    }

    @Test
    void findByUserId_AllOk_ReturnVisitorDto() {
        Visitor visitor = TestUtils.getVisitorRandomUUID();
        Mockito.when(visitorRepository.findVisitorsByUserId(TestUtils.USER_ID)).thenReturn(List.of(visitor));

        List<VisitorDTO> found = visitorService.findByUserId(TestUtils.USER_ID);

        Assertions.assertThat(found).isEqualTo(visitorMapper.toVisitorDTOS(List.of(visitor)));
        Mockito.verify(visitorRepository, Mockito.times(1)).findVisitorsByUserId(TestUtils.USER_ID);
    }

    @Test
    void findByUserId_NoVisitor_ReturnEmptyList() {
        Mockito.when(visitorRepository.findVisitorsByUserId(Mockito.any())).thenReturn(Collections.emptyList());

        List<VisitorDTO> found = visitorService.findByUserId(TestUtils.USER_ID);

        Assertions.assertThat(found).isEmpty();
    }


    private void checkSavedVisitorFields(Visitor captured, VisitorDTO visitorDTO) {
        Assertions.assertThat(captured.getName()).isEqualTo(visitorDTO.getName());
        Assertions.assertThat(captured.getPhone()).isEqualTo(visitorDTO.getPhone());
        Assertions.assertThat(captured.getNote()).isEqualTo(visitorDTO.getNote());
    }

}