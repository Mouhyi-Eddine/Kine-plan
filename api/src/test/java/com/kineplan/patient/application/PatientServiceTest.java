package com.kineplan.patient.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kineplan.patient.domain.Patient;
import com.kineplan.patient.domain.PatientRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PatientServiceTest {
    private final PatientRepository repository = mock(PatientRepository.class);
    private final PatientService service = new PatientService(repository);

    @Test
    void anIdFromAnotherCabinetIsNotFound() {
        UUID cabinetId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        when(repository.findByIdAndCabinetId(patientId, cabinetId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(cabinetId, patientId))
                .isInstanceOf(PatientNotFoundException.class);

        verify(repository).findByIdAndCabinetId(patientId, cabinetId);
    }
}