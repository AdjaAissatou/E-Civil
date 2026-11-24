-- Un document administratif d'état civil est valable 3 mois (90 jours) à compter de sa délivrance.
-- Le livret de famille (et son duplicata) n'expire pas.
UPDATE type_document SET duree_validite_jours = 90 WHERE modele <> 'LIVRET_FAMILLE';
UPDATE type_document SET duree_validite_jours = NULL WHERE modele = 'LIVRET_FAMILLE';
