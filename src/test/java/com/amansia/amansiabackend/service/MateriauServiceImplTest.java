package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.materiau.MateriauCreationDTO;
import com.amansia.amansiabackend.exception.MateriauNameAlreadyUsedException;
import com.amansia.amansiabackend.repository.MateriauRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MateriauServiceImplTest {

    @Mock
    private MateriauRepository materiauRepository;

    @InjectMocks
    private MateriauServiceImpl materiauService;

    @Test
    void creerMateriau_shouldRejectDuplicateNameIgnoringCase() {
        MateriauCreationDTO dto = new MateriauCreationDTO();
        dto.setNom("Bois massif");
        dto.setDescription("Description");
        dto.setActif(true);

        when(materiauRepository.existsByNomIgnoreCase("Bois massif")).thenReturn(true);

        assertThrows(MateriauNameAlreadyUsedException.class, () -> materiauService.creerMateriau(dto));
    }
}
