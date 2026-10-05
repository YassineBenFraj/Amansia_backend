package com.amansia.amansiabackend;
import com.amansia.amansiabackend.service.*;
import com.amansia.amansiabackend.security.JwtUtil;
import com.amansia.amansiabackend.exception.ProduitNotFoundException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.core.userdetails.*;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;
@SpringBootTest(properties="spring.jpa.hibernate.ddl-auto=validate")
class SecurityHttpTest {
 @Autowired WebApplicationContext context;@Autowired JwtUtil jwt;
 @MockitoBean com.amansia.amansiabackend.repository.UtilisateurRepository utilisateurs;
 @MockitoBean com.amansia.amansiabackend.repository.AdresseRepository adresses;
 @MockitoBean ImageProduitService images;
 @MockitoBean UserDetailsService users;
 @MockitoBean CataloguePublicService catalogue;
 @MockitoBean CategorieService categories;@MockitoBean MateriauService materiaux;@MockitoBean CouleurService couleurs;@MockitoBean AvisService avis;
 @MockitoBean CommandeService commandes;@MockitoBean PaiementService paiements;@MockitoBean StockService stock;@MockitoBean ProduitsSimilairesService similaires;
 @MockitoBean ProfileService profile;
 @MockitoBean NotificationService notifications;
 MockMvc mvc;
 @BeforeEach void setup(){mvc=MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();}
 String token(String role,boolean disabled){var u=new com.amansia.amansiabackend.entity.Utilisateur();u.setId(1);when(utilisateurs.findByEmail("test@example.test")).thenReturn(java.util.Optional.of(u));when(users.loadUserByUsername("test@example.test")).thenReturn(User.withUsername("test@example.test").password("x").roles(role).disabled(disabled).build());return "Bearer "+jwt.generateToken("test@example.test",role);}
 @Test void publicReadsNeedNoJwt() throws Exception {for(String url:new String[]{"/api/categories/actives","/api/produits","/api/produits/recherche?nom=table","/api/produits/categorie/1/actifs","/api/produits/1","/api/images-produits","/api/images-produits/1","/api/images-produits/produit/1","/api/materiaux/actifs","/api/couleurs/actives","/api/variantes-produits/produit/1/actives","/api/avis/produit/1","/api/stocks/produit/1","/api/produits/1/similaires"})mvc.perform(get(url)).andExpect(status().isOk());verify(avis).recupererAvisProduit(1,false,null);}
 @Test void adminAndWritesStayProtected() throws Exception {for(String url:new String[]{"/api/produits/admin","/api/commandes/admin","/api/paiements/admin","/api/adresses","/api/categories","/api/stocks/admin","/api/stocks/admin/mouvements"})mvc.perform(get(url)).andExpect(status().isUnauthorized());mvc.perform(post("/api/produits").contentType("application/json").content("{}")).andExpect(status().isUnauthorized());}
 @Test void clientCannotReadAdminLists() throws Exception {String t=token("CLIENT",false);for(String url:new String[]{"/api/commandes/admin","/api/paiements/admin","/api/produits/admin","/api/stocks/admin","/api/stocks/admin/mouvements"})mvc.perform(get(url).header("Authorization",t)).andExpect(status().isForbidden());}
 @Test void administratorCanReadBothLists() throws Exception {String t=token("ADMINISTRATEUR",false);mvc.perform(get("/api/commandes/admin").header("Authorization",t)).andExpect(status().isOk());mvc.perform(get("/api/paiements/admin").header("Authorization",t)).andExpect(status().isOk());verify(commandes).recupererTousLesCommandes();verify(paiements).recupererTousLesPaiements();mvc.perform(get("/api/stocks/admin").header("Authorization",t)).andExpect(status().isOk());verify(stock).stocks();}
 @Test void clientCannotModifyStock() throws Exception {mvc.perform(post("/api/stocks/admin/mouvements").header("Authorization",token("CLIENT",false)).contentType("application/json").content("{\"varianteId\":1,\"typeMouvement\":\"ENTREE\",\"quantite\":1,\"motif\":\"test\"}")).andExpect(status().isForbidden());verifyNoInteractions(stock);}
 @Test void disabledUserWithValidJwtIsRejected() throws Exception {String t=token("ADMINISTRATEUR",true);mvc.perform(get("/api/commandes/admin").header("Authorization",t)).andExpect(status().isUnauthorized());verifyNoInteractions(commandes);}
 @Test void disabledClientCannotUseAddresses() throws Exception {mvc.perform(get("/api/adresses").header("Authorization",token("CLIENT",true))).andExpect(status().isUnauthorized());}
 @Test void profileWritesRequireClientJwt() throws Exception {mvc.perform(put("/api/utilisateurs/me").contentType("application/json").content("{}")).andExpect(status().isUnauthorized());mvc.perform(put("/api/utilisateurs/me").header("Authorization",token("ADMINISTRATEUR",false)).contentType("application/json").content("{}")).andExpect(status().isForbidden());verifyNoInteractions(profile);}
 @Test void clientProfileUpdateUsesJwtIdentity() throws Exception {mvc.perform(put("/api/utilisateurs/me").header("Authorization",token("CLIENT",false)).contentType("application/json").content("{\"nom\":\"Nom\",\"prenom\":\"Prenom\",\"telephone\":\"\"}")).andExpect(status().isOk());verify(profile).update(eq("test@example.test"),any(com.amansia.amansiabackend.dto.ProfileUpdateRequest.class));}
 @Test void notificationsRequireJwtAndUseItsIdentity() throws Exception {mvc.perform(get("/api/notifications")).andExpect(status().isUnauthorized());String t=token("CLIENT",false);mvc.perform(get("/api/notifications").header("Authorization",t)).andExpect(status().isOk());verify(notifications).list("test@example.test");mvc.perform(put("/api/notifications/9/lue").header("Authorization",t)).andExpect(status().isOk());verify(notifications).read("test@example.test",9);}
 @Test void adminKeepsImageManagementAccess() throws Exception {mvc.perform(get("/api/images-produits").header("Authorization",token("ADMINISTRATEUR",false))).andExpect(status().isOk());verify(images).recupererToutesLesImages();verifyNoInteractions(catalogue);}
 @Test void clientListsRemainPersonal() throws Exception {String t=token("CLIENT",false);mvc.perform(get("/api/commandes").header("Authorization",t)).andExpect(status().isOk());mvc.perform(get("/api/paiements/mes-paiements").header("Authorization",t)).andExpect(status().isOk());verify(commandes).recupererCommandesUtilisateur(org.mockito.ArgumentMatchers.argThat(u->u.getId()==1));verify(paiements).recupererPaiementsUtilisateur(org.mockito.ArgumentMatchers.argThat(u->u.getId()==1));verify(commandes,never()).recupererTousLesCommandes();verify(paiements,never()).recupererTousLesPaiements();}
 @Test void foreignAddressRequestsReturn404() throws Exception {String t=token("CLIENT",false);when(adresses.findOwnedForUpdate(99,1)).thenReturn(java.util.Optional.empty());mvc.perform(get("/api/adresses/99").header("Authorization",t)).andExpect(status().isNotFound());mvc.perform(put("/api/adresses/99").header("Authorization",t).contentType("application/json").content("{\"adresse\":\"Rue A\",\"ville\":\"Tunis\"}")).andExpect(status().isNotFound());mvc.perform(delete("/api/adresses/99").header("Authorization",t)).andExpect(status().isNotFound());verify(adresses,never()).save(org.mockito.ArgumentMatchers.any());verify(adresses,never()).delete(org.mockito.ArgumentMatchers.any());}
 @Test void inactiveProductReturns404Anonymously() throws Exception {when(catalogue.produit(99)).thenThrow(new ProduitNotFoundException(99));mvc.perform(get("/api/produits/99")).andExpect(status().isNotFound());}
}
