package com.amansia.amansiabackend.repository;

import com.amansia.amansiabackend.entity.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProduitRepository extends JpaRepository<Produit, Integer> {

    interface BornesPrix {
        BigDecimal getMinimum();
        BigDecimal getMaximum();
    }

    List<Produit> findByCategorieId(Integer categorieId);

    List<Produit> findByActifTrue();

    List<Produit> findByCategorieIdAndActifTrue(Integer categorieId);

    List<Produit> findByNomContainingIgnoreCase(String nom);

    // Same promotion conditions as PrixProduit.promotionApplicable, including legacy open dates.
    @Query("""
            select p from Produit p join fetch p.categorie c
            where p.actif = true and c.actif = true
              and (:nom is null or lower(p.nom) like lower(concat('%', :nom, '%')))
              and (:categorieId is null or c.id = :categorieId)
              and (:prixMin is null or
                case when p.promotionActive = true and p.prixPromotion is not null
                  and p.prixPromotion >= 0 and p.prixPromotion < p.prix
                  and (p.promotionDateDebut is null or p.promotionDateDebut <= current_timestamp)
                  and (p.promotionDateFin is null or p.promotionDateFin >= current_timestamp)
                then p.prixPromotion else p.prix end >= :prixMin)
              and (:prixMax is null or
                case when p.promotionActive = true and p.prixPromotion is not null
                  and p.prixPromotion >= 0 and p.prixPromotion < p.prix
                  and (p.promotionDateDebut is null or p.promotionDateDebut <= current_timestamp)
                  and (p.promotionDateFin is null or p.promotionDateFin >= current_timestamp)
                then p.prixPromotion else p.prix end <= :prixMax)
            order by p.id
            """)
    List<Produit> rechercherCatalogue(@Param("nom") String nom,
                                      @Param("categorieId") Integer categorieId,
                                      @Param("prixMin") BigDecimal prixMin,
                                      @Param("prixMax") BigDecimal prixMax);

    @Query("""
            select min(case when p.promotionActive = true and p.prixPromotion is not null
                and p.prixPromotion >= 0 and p.prixPromotion < p.prix
                and (p.promotionDateDebut is null or p.promotionDateDebut <= current_timestamp)
                and (p.promotionDateFin is null or p.promotionDateFin >= current_timestamp)
                then p.prixPromotion else p.prix end) as minimum,
              max(case when p.promotionActive = true and p.prixPromotion is not null
                and p.prixPromotion >= 0 and p.prixPromotion < p.prix
                and (p.promotionDateDebut is null or p.promotionDateDebut <= current_timestamp)
                and (p.promotionDateFin is null or p.promotionDateFin >= current_timestamp)
                then p.prixPromotion else p.prix end) as maximum
            from Produit p join p.categorie c
            where p.actif = true and c.actif = true
            """)
    BornesPrix bornesPrixCatalogue();
}
