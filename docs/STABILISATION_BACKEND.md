# Stabilisation du backend Amansia

Date : 1er octobre 2026. Frontend Angular inchangé.

## 1. Problèmes confirmés

- Lectures du catalogue protégées par JWT, détail produit sans filtre actif et fichiers images servis sans contrôle du produit.
- Filtre JWT sans vérification de l'état actif du compte.
- Lecture/création du panier déclarée read-only.
- Prix promotionnel ignoré dans les calculs panier/commande et prix de panier potentiellement périmé lors du checkout.
- API d'adresses absente et propriété de l'adresse non vérifiée à la commande.
- Frais de livraison fournis par le client utilisés dans le total.
- Webhook ne synchronisant pas la commande.
- Annulation sans contrôle de l'avancement et du paiement.
- Consultations globales administrateur absentes pour commandes/paiements.
- Noms matériaux/couleurs : validation 150 caractères pour colonnes de 100.
- produitId accepté mais inutilisé dans VarianteProduitUpdateDTO.

## 2. Constats infirmés ou fonctionnalités déjà correctes

- La table adresses existe dans amansia_stage : aucune nouvelle table ni migration nécessaire.
- Le serveur imposait déjà l'utilisateur, le statut initial, le sous-total et le total ; le client ne pouvait pas directement fournir ces valeurs dans CommandeCreationDTO. Le défaut concernait surtout les frais et la revalidation des prix.
- Les lignes de commande possédaient déjà des montants indépendants du catalogue ; cette conservation historique est maintenue et testée.
- Le webhook vérifiait déjà la signature, les références, le montant, la devise et payment_status. Ces contrôles sont conservés.
- L'accès aux commandes/paiements personnels était déjà limité au propriétaire ; il reste distinct des nouvelles listes administrateur.

## 3. Fichiers créés

- `src/main/java/com/amansia/amansiabackend/controller/AdresseController.java`
- `src/main/java/com/amansia/amansiabackend/dto/adresse/AdresseDTO.java`
- `src/main/java/com/amansia/amansiabackend/dto/adresse/AdresseRequest.java`
- `src/main/java/com/amansia/amansiabackend/entity/Adresse.java`
- `src/main/java/com/amansia/amansiabackend/exception/AdresseNotFoundException.java`
- `src/main/java/com/amansia/amansiabackend/exception/AdresseUtiliseeException.java`
- `src/main/java/com/amansia/amansiabackend/repository/AdresseRepository.java`
- `src/main/java/com/amansia/amansiabackend/service/AdresseService.java`
- `src/main/java/com/amansia/amansiabackend/service/CataloguePublicService.java`
- `src/main/java/com/amansia/amansiabackend/service/FraisLivraisonService.java`
- `src/main/java/com/amansia/amansiabackend/service/PrixProduit.java`
- `src/test/java/com/amansia/amansiabackend/DtoValidationTest.java`
- `src/test/java/com/amansia/amansiabackend/SecurityHttpTest.java`
- `src/test/java/com/amansia/amansiabackend/service/AdresseServiceTest.java`
- `src/test/java/com/amansia/amansiabackend/service/CataloguePublicServiceTest.java`
- `src/test/java/com/amansia/amansiabackend/service/CommandeReglesTest.java`
- `src/test/java/com/amansia/amansiabackend/service/ImageResourceTest.java`
- `src/test/java/com/amansia/amansiabackend/service/PrixPanierTest.java`
- `docs/STABILISATION_BACKEND.md` : ce rapport.

## 4. Fichiers modifiés

- `src/main/java/com/amansia/amansiabackend/config/SecurityConfig.java`
- `src/main/java/com/amansia/amansiabackend/config/UploadResourceConfig.java`
- `src/main/java/com/amansia/amansiabackend/controller/AvisController.java`
- `src/main/java/com/amansia/amansiabackend/controller/CommandeController.java`
- `src/main/java/com/amansia/amansiabackend/controller/ImageProduitController.java`
- `src/main/java/com/amansia/amansiabackend/controller/PaiementController.java`
- `src/main/java/com/amansia/amansiabackend/controller/ProduitController.java`
- `src/main/java/com/amansia/amansiabackend/controller/VarianteProduitController.java`
- `src/main/java/com/amansia/amansiabackend/dto/commande/CommandeCreationDTO.java`
- `src/main/java/com/amansia/amansiabackend/dto/couleur/CouleurCreationDTO.java`
- `src/main/java/com/amansia/amansiabackend/dto/couleur/CouleurUpdateDTO.java`
- `src/main/java/com/amansia/amansiabackend/dto/materiau/MateriauCreationDTO.java`
- `src/main/java/com/amansia/amansiabackend/dto/materiau/MateriauUpdateDTO.java`
- `src/main/java/com/amansia/amansiabackend/dto/variante/VarianteProduitUpdateDTO.java`
- `src/main/java/com/amansia/amansiabackend/exception/GlobalExceptionHandler.java`
- `src/main/java/com/amansia/amansiabackend/repository/CommandeRepository.java`
- `src/main/java/com/amansia/amansiabackend/repository/ImageProduitRepository.java`
- `src/main/java/com/amansia/amansiabackend/repository/PaiementRepository.java`
- `src/main/java/com/amansia/amansiabackend/security/JwtRequestFilter.java`
- `src/main/java/com/amansia/amansiabackend/service/CommandeService.java`
- `src/main/java/com/amansia/amansiabackend/service/CommandeServiceImpl.java`
- `src/main/java/com/amansia/amansiabackend/service/PaiementService.java`
- `src/main/java/com/amansia/amansiabackend/service/PaiementServiceImpl.java`
- `src/main/java/com/amansia/amansiabackend/service/PanierServiceImpl.java`
- `src/main/java/com/amansia/amansiabackend/service/StripePaymentService.java`
- `src/main/resources/application.properties`
- `src/test/java/com/amansia/amansiabackend/AmansiaBackendApplicationTests.java`
- `src/test/java/com/amansia/amansiabackend/service/CommandeServiceImplTest.java`
- `src/test/java/com/amansia/amansiabackend/service/StripePaymentServiceTest.java`

Aucune modification de pom.xml ni de dépendances. Une sauvegarde des sources avant intervention est disponible dans :
`C:/Users/yassine/AppData/Local/Temp/amansia-before-fixes-1790805674929/src`.

## 5. Endpoints ajoutés ou modifiés

### Adresses : authentification obligatoire, propriétaire uniquement

| Méthode | URL | Réponse |
|---|---|---|
| POST | /api/adresses | 201 AdresseDTO |
| GET | /api/adresses | 200 AdresseDTO[] personnels |
| GET | /api/adresses/{id} | 200 AdresseDTO |
| PUT | /api/adresses/{id} | 200 AdresseDTO |
| DELETE | /api/adresses/{id} | 204 |

Body POST/PUT : adresse (obligatoire, 255 caractères maximum), ville (obligatoire, 100), gouvernorat (100), codePostal (20), pays (100), adressePrincipale (booléen). Aucun utilisateurId fourni par le client. Pays par défaut : Tunisie. adressePrincipale par défaut : false. Le modèle existant n'impose pas l'unicité de ce drapeau ; aucun mécanisme supplémentaire de gestion d'adresse principale n'a été ajouté.

Réponse : id, adresse, ville, gouvernorat, codePostal, pays, adressePrincipale, dateCreation.

### Administration : ADMINISTRATEUR uniquement

- GET /api/commandes/admin : toutes les commandes, triées par date décroissante.
- GET /api/paiements/admin : tous les paiements, triés par date décroissante.

### Lectures rendues publiques

- GET /api/categories/actives
- GET /api/produits
- GET /api/produits/recherche?nom=...
- GET /api/produits/categorie/{id}/actifs
- GET /api/produits/{id} (identifiant numérique)
- GET /api/images-produits
- GET /api/images-produits/{id}
- GET /api/images-produits/produit/{id}
- GET /api/materiaux/actifs
- GET /api/couleurs/actives
- GET /api/variantes-produits/produit/{id}/actives
- GET /api/avis/produit/{id}

Les routes d'administration et les opérations d'écriture restent protégées. Les autres anciennes lectures gardent leurs protections. Un administrateur conserve la lecture du détail produit et des métadonnées images inactifs ; les visiteurs ne les voient pas.

La route historique /uploads/produits/{filename} conserve son URL, mais vérifie maintenant la présence de l'image en base, l'activité du produit et de sa catégorie, et le confinement du chemin au répertoire d'upload. Les ressources cachées ou inexistantes renvoient 404. Cache-Control: no-store évite de réutiliser une réponse publique après une désactivation.

## 6. Règles métier corrigées

- Catalogue public : seuls les produits actifs appartenant à une catégorie active sont visibles. Les variantes publiques doivent être actives et ne pas référencer de matériau/couleur désactivé.
- Avis publics : produit visible et avis approuvés ; modération administrateur inchangée.
- obtenirPanier utilise une transaction autorisant les écritures.
- Les DTO matériaux/couleurs limitent les noms à 100 caractères.
- produitId retiré du DTO de mise à jour de variante ; le produit associé n'est pas modifiable par cette opération.
- Les commandes sont recalculées côté serveur, avec remise initiale nulle, utilisateur connecté et statut EN_ATTENTE.

## 7. Promotions

PrixProduit centralise le calcul utilisé par le panier et le checkout :

1. prixPromotion s'il est non nul, positif ou nul et inférieur ou égal à prix ; sinon prix.
2. Ajout du prixSupplementaire de la variante.
3. Arrondi à deux décimales selon le comportement monétaire existant des commandes.

Exemple testé : prix 1000, promotion 800 et supplément 50 donnent 850.00. Une promotion invalide est ignorée. Les suppléments négatifs sont refusés.

L'ajout d'une nouvelle ligne, ainsi qu'un nouvel ajout au même couple produit/variante, applique ce calcul. Le checkout revalide les prix courants plutôt que de reprendre le montant mémorisé du panier. Les lignes de commande enregistrent ensuite ces montants historiques ; une modification ultérieure du catalogue ne les change pas.

## 8. Adresses

Schéma existant réutilisé : id, utilisateur_id, adresse, ville, gouvernorat, code_postal, pays, adresse_principale, date_creation.

Chaque accès par identifiant impose le propriétaire en base. Une adresse absente ou étrangère retourne 404. La commande vérifie également cette propriété avant de lire et consommer le panier.

Comme la table ne possède pas de champ actif, DELETE supprime uniquement une adresse non utilisée. Une adresse référencée par une commande ne peut être ni modifiée ni supprimée : réponse métier 409, invitant à créer une nouvelle adresse. Ce choix préserve l'adresse historique sans ajouter de colonne ou de structure concurrente. Un verrou transactionnel protège ce contrôle contre une suppression simultanée au checkout.

## 9. Frais de livraison

Configuration :

```properties
app.livraison.frais-fixes=\${AMANSIA_FRAIS_LIVRAISON:0.00}
```

Le tarif est fixe, centralisé dans FraisLivraisonService et exprimé dans la devise utilisée pour les paiements. Par défaut, la livraison est gratuite (0.00), faute de tarif métier fourni. Pour un tarif payant, définir AMANSIA_FRAIS_LIVRAISON et redémarrer le backend. Une configuration négative ou ayant une précision incompatible est refusée.

Le champ fraisLivraison du DTO est conservé avec ses validations existantes pour compatibilité, mais sa valeur n'est pas utilisée pour facturer. Le test du checkout envoie 0 et vérifie que le serveur facture le tarif configuré de 12.00.

## 10. Stripe → paiement → commande

Après vérification de la signature et des contrôles de correspondance déjà présents :

- Un événement de confirmation avec payment_status=paid fait passer le paiement admissible à PAYE.
- Si la commande est encore EN_ATTENTE, elle passe à CONFIRMEE dans la même transaction.
- Une commande ANNULEE ou dans un autre état n'est pas modifiée.
- Une répétition du webhook ne réapplique pas les écritures ni la date de paiement.
- Le webhook verrouille d'abord la commande puis le paiement, conformément à l'ordre des opérations de paiement/annulation.
- PAIEMENT_LIVRAISON reste distinct : il n'est jamais confirmé par ce webhook.
- Aucun traitement Angular ni retour sur la page succès ne confirme une commande.

Les tests utilisent des payloads signés avec un secret de test local. Aucun appel de paiement réel à Stripe n'a été effectué.

## 11. Annulation

Annulation permise uniquement pour EN_ATTENTE ou CONFIRMEE, et seulement pour le propriétaire, à condition qu'aucun paiement ne soit :

- PAYE ;
- REMBOURSE ;
- CARTE et EN_ATTENTE.

Les états EN_PREPARATION, EXPEDIEE, EN_LIVRAISON, LIVREE et ANNULEE sont refusés. Un paiement à la livraison encore EN_ATTENTE n'interdit pas l'annulation d'une commande admissible. Refus métier HTTP 400 avec message explicite. Aucun remboursement n'a été développé.

## 12. Accès administrateur

Les deux nouvelles listes globales sont protégées avant les règles générales de sécurité. Un anonyme reçoit 401, un CLIENT 403 et un ADMINISTRATEUR 200. Les anciennes listes personnelles ne deviennent pas globales, y compris lorsqu'elles sont utilisées par un administrateur.

## 13. JWT

Le filtre utilise AccountStatusUserDetailsChecker sur l'utilisateur rechargé depuis la base avant d'installer l'authentification. Les exceptions d'authentification effacent le contexte ; un JWT valide ne donne donc plus accès aux routes protégées si le compte est désactivé. Les contrôles de signature et d'expiration existants sont conservés.

## 14. Tests ajoutés et adaptés

- SecurityHttpTest : 10 tests de la chaîne de sécurité, routes publiques, accès admin, JWT désactivé, données personnelles et adresses étrangères.
- AdresseServiceTest : création avec propriétaire serveur, liste personnelle, refus inter-utilisateurs, protection des adresses utilisées, modification/suppression autorisées.
- CataloguePublicServiceTest : produits/catégories inactifs, images cachées, variantes liées à un matériau inactif.
- CommandeReglesTest : revalidation des prix, conservation historique, frais serveur, adresse étrangère, annulation et liste admin.
- PrixPanierTest : absence de promotion, promotion, promotion avec variante, promotions invalides, panier créé en transaction inscriptible, configuration de livraison.
- ImageResourceTest : fichiers présents mais inaccessibles pour un produit inactif, accès actif et chemin hors répertoire refusé.
- DtoValidationTest : limites de 100 caractères.
- StripePaymentServiceTest étendu : confirmation de commande, répétition, états incompatibles, montant incorrect et paiement livraison.
- CommandeServiceImplTest adapté à la dépendance de contrôle d'adresse, test existant conservé.
- AmansiaBackendApplicationTests : démarrage Tomcat réel sur port aléatoire, validation du schéma et requêtes HTTP réelles.

## 15. Résultat exact de la validation finale

Commande exécutée depuis le backend : .\mvnw.cmd clean test

```text
Tests run: 66, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
Total time: 45.025 s
Finished at: 2026-10-01T00:12:43+02:00
Exit code: 0
```

Les tests existants passent. Les rapports détaillés sont dans target/surefire-reports. Le journal complet de cette exécution est conservé dans le dossier temporaire : amansia-final-tests.log.

## 16. Démarrage avec ddl-auto=validate

Le test de démarrage impose spring.jpa.hibernate.ddl-auto=validate et vérifie sa valeur. Il utilise MySQL local amansia_stage, sans remplacer la base par une base en mémoire.

Observations :

- EntityManagerFactory initialisée sans erreur de mapping.
- Tomcat démarré sur le port de test 53225.
- Application démarrée en 14.55 secondes.
- GET réel /api/categories/actives : 200.
- GET réel /api/commandes/admin sans JWT : 401.

Le serveur de test s'arrête avec la JVM de test. Le port configuré de l'application reste 8090.

## 17. Schéma et périmètre

Aucune modification de schéma nécessaire ou effectuée. Aucune commande SQL destructive. L'inspection initiale utilisait exclusivement information_schema en lecture seule. Les tests de contexte utilisent validate ; les tests métier utilisent des mocks et n'insèrent pas de données métier dans MySQL.

Aucun fichier Angular modifié. Aucun système de remboursement, stock avancé, pagination, facturation, IA ou logistique avancée ajouté.
