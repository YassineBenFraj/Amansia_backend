package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.couleur.CouleurCreationDTO;
import com.amansia.amansiabackend.exception.CodeHexadecimalInvalideException;
import com.amansia.amansiabackend.repository.CouleurRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class CouleurServiceImplTest {

    @Mock
    private CouleurRepository couleurRepository;

    @InjectMocks
    private CouleurServiceImpl couleurService;

    @Test
    void creerCouleur_shouldRejectInvalidHexadecimalCode() {
        CouleurCreationDTO dto = new CouleurCreationDTO();
        dto.setNom("Blanc");
        dto.setCodeHexadecimal("#FFF");
        dto.setActif(true);

        assertThrows(CodeHexadecimalInvalideException.class, () -> couleurService.creerCouleur(dto));
    }
}
