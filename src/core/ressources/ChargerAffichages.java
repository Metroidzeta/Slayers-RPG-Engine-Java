/**
 * @author Alain Barbier alias "Metroidzeta"
 * Copyright © 2025 Alain Barbier (Metroidzeta) - All rights reserved.
 *
 * This file is part of the project covered by the
 * "Educational and Personal Use License / Licence d’Utilisation Personnelle et Éducative".
 *
 * Permission is granted to fork and use this code for educational and personal purposes only.
 *
 * Commercial use, redistribution, or public republishing of modified versions
 * is strictly prohibited without the express written consent of the author.
 *
 * Created by Metroidzeta.
 */

package ressources;

import core.Util;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Construit et retourne la map complète (nom -> affichage) des images d’affichage du jeu.
 */
public final class ChargerAffichages {

	private static final String DOSSIER = "img/";

	private ChargerAffichages() { throw new AssertionError("La classe ChargerAffichages ne doit pas être instanciée."); } // Empêche toute instanciation
	
	/** Structure de données pour un affichage **/
	private static record AffichageData(String nom, String nomFichier) {}

	private static final List<AffichageData> AFFICHAGES_LIST = List.of( // Création des affichages : new AffichageData(nom, nomFichier)
		new AffichageData("Fioles", "fioles.png"),                      // 0 : icônes de fioles
		new AffichageData("BarreXP", "xp.png")                          // 1 : barre d’expérience
	);

	/** Getters **/
	public static Map<String, BufferedImage> get() {
		final Map<String, BufferedImage> affichages = new HashMap<>(AFFICHAGES_LIST.size());
		AFFICHAGES_LIST.forEach(data -> {
			BufferedImage image = Util.chargerImage(DOSSIER + data.nomFichier());
			affichages.put(data.nom(), image);
		});
		return Map.copyOf(affichages);
	}
}