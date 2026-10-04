package com.amansia.amansiabackend.service;
import com.amansia.amansiabackend.entity.*;
import com.amansia.amansiabackend.repository.*;
import com.amansia.amansiabackend.dto.adresse.*;
import com.amansia.amansiabackend.exception.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
@ExtendWith(MockitoExtension.class)
class AdresseServiceTest {
 @Mock AdresseRepository adresses;@Mock CommandeRepository commandes;@InjectMocks AdresseService service;
 Utilisateur u(){var u=new Utilisateur();u.setId(1);return u;}
 AdresseRequest request(){return new AdresseRequest("12 rue des Fleurs","Tunis",null,"1000",null,false);}
 @Test void createUsesConnectedOwner(){when(adresses.save(any())).thenAnswer(i->i.getArgument(0));assertEquals("Tunisie",service.creer(u(),request()).pays());var capture=ArgumentCaptor.forClass(Adresse.class);verify(adresses).save(capture.capture());assertEquals(1,capture.getValue().getUtilisateur().getId());}
 @Test void listUsesConnectedOwner(){service.lister(u());verify(adresses).findByUtilisateurIdOrderByIdDesc(1);verify(adresses,never()).findAll();}
 @Test void foreignAddressCannotBeReadUpdatedOrDeleted(){when(adresses.findOwnedForUpdate(5,1)).thenReturn(Optional.empty());assertThrows(AdresseNotFoundException.class,()->service.consulter(u(),5));assertThrows(AdresseNotFoundException.class,()->service.modifier(u(),5,request()));assertThrows(AdresseNotFoundException.class,()->service.supprimer(u(),5));verify(adresses,never()).save(any());verify(adresses,never()).delete(any());}
 @Test void usedAddressIsPreserved(){var a=new Adresse();when(adresses.findOwnedForUpdate(5,1)).thenReturn(Optional.of(a));when(commandes.existsByAdresseLivraisonId(5)).thenReturn(true);assertThrows(AdresseUtiliseeException.class,()->service.modifier(u(),5,request()));assertThrows(AdresseUtiliseeException.class,()->service.supprimer(u(),5));verify(adresses,never()).delete(any());}
 @Test void ownUnusedAddressCanBeUpdatedAndDeleted(){var a=new Adresse();when(adresses.findOwnedForUpdate(5,1)).thenReturn(Optional.of(a));when(adresses.save(a)).thenReturn(a);assertEquals("Tunis",service.modifier(u(),5,request()).ville());service.supprimer(u(),5);verify(adresses).delete(a);}
}
