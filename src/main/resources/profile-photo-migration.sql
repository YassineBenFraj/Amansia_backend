-- Schéma vérifié : utilisateurs ne possède pas encore de colonne photo_profil.
-- Ajout nullable : aucune ligne existante n'est supprimée ni réinitialisée.
ALTER TABLE utilisateurs ADD COLUMN photo_profil VARCHAR(255) NULL;
