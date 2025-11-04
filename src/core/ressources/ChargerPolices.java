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

import core.Config;
import java.awt.Font;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Construit et retourne la map complète (nom -> police) des polices du jeu.
 */
public final class ChargerPolices {

	private ChargerPolices() { throw new AssertionError("La classe ChargerPolices ne doit pas être instanciée."); } // Empêche toute instanciation

	/** Structure de données pour une police **/
	private static record PoliceData(String nom, String nomPolice, int style, int taille) {}

	private static final List<PoliceData> POLICES_LIST = List.of( // Création des polices : new PoliceData(nom, nomPolice, style, taille)
		new PoliceData("FPS", "Courier New", Font.PLAIN, (int)(Config.TAILLE_CASES * 0.4)),    // 0 : police des FPS en jeu
		new PoliceData("Normal", "Arial", Font.PLAIN, (int)(Config.TAILLE_CASES * 0.68))       // 1 : police du texte normal
	);

	/** Getters **/
	public static Map<String, Font> get() {
		final Map<String, Font> polices = new HashMap<>(POLICES_LIST.size());
		POLICES_LIST.forEach(data -> {
			Font police = new Font(data.nomPolice(), data.style(), data.taille());
			polices.put(data.nom(), police);
		});
		return Map.copyOf(polices);
	}
}