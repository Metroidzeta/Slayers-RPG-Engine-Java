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

import core.Bruitage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Construit et retourne la map complète (nom -> bruitage) des bruitages du jeu.
 */
public final class ChargerBruitages {

	private ChargerBruitages() { throw new AssertionError("La classe ChargerBruitages ne doit pas être instanciée."); } // Empêche toute instanciation

	private static final List<Bruitage> BRUITAGES_LIST = List.of( // Création des bruitages : new Bruitage(nomFichier)
		new Bruitage("Blow1.wav"),                                // 0
		new Bruitage("Kill1.wav")                                 // 1
	);

	/** Getters **/
	public static Map<String, Bruitage> get() {
		final Map<String, Bruitage> bruitages = new HashMap<>(BRUITAGES_LIST.size());
		BRUITAGES_LIST.forEach(bruitage -> bruitages.put(bruitage.getNom(), bruitage));
		return Map.copyOf(bruitages);
	}
}