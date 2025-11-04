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

import core.Musique;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Construit et retourne la map complète (nom -> musique) des musiques du jeu.
 */
public final class ChargerMusiques {

	private ChargerMusiques() { throw new AssertionError("La classe ChargerMusiques ne doit pas être instanciée."); } // Empêche toute instanciation

	private static final List<Musique> MUSIQUES_LIST = List.of( // Création des musiques : new Musique(nomFichier)
		new Musique("Castle_1.ogg"),                            // 0
		new Musique("Sarosa.ogg"),                              // 1
		new Musique("bahamut_lagoon.ogg"),                      // 2
		new Musique("Castle_3.ogg"),                            // 3
		new Musique("2000_ordeal.ogg"),                         // 4
		new Musique("cc_viper_manor.ogg"),                      // 5
		new Musique("suikoden-ii-two-rivers.ogg"),              // 6
		new Musique("mystery3.ogg"),                            // 7
		new Musique("hunter.ogg"),                              // 8
		new Musique("illusionary_world.ogg"),                   // 9
		new Musique("chapt1medfill.ogg")                        // 10
	);

	/** Getters **/
	public static Map<String, Musique> get() {
		final Map<String, Musique> musiques = new HashMap<>(MUSIQUES_LIST.size());
		MUSIQUES_LIST.forEach(musique -> musiques.put(musique.getNom(), musique));
		return Map.copyOf(musiques);
	}
}