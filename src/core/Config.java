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

package core;

import java.awt.Color;

public final class Config {

	private Config() { throw new AssertionError("La classe Config ne doit pas être instanciée."); } // Empêche toute instanciation

	/** Titre de la fenêtre **/
	public static final String TITRE_FENETRE = "Slayers RPG Engine JAVA";

	/** Paramètres de base **/
	public static final int WINDOW_WIDTH = 1280, WINDOW_HEIGHT = 960; // par défaut : 1280 * 960
	public static final int UPS = 30; // (multiple de 30) par défaut : 30
	public static final int FPS = 60; // par défaut : 60
	public static final int NIVEAU_MAX = 80; // par défaut : 80

	/** Constantes dérivées — NE PAS LES MODIFIER /!\ **/
	public static final int TAILLE_CASES = (WINDOW_HEIGHT / 20) - ((WINDOW_HEIGHT / 20) % 4); // par défaut : 48
	public static final double DEPLACEMENT_JOUEUR = (TAILLE_CASES / 4.0) * (30.0 / Config.UPS);
	public static final int WINDOW_WIDTH_CASES = ((WINDOW_WIDTH + TAILLE_CASES - 1) / TAILLE_CASES);
	public static final int WINDOW_HEIGHT_CASES = ((WINDOW_HEIGHT + TAILLE_CASES - 1) / TAILLE_CASES);

	/** Couleurs semi-transparentes RVB **/
	public static final Color BLEU_FONCE_TRANSPARENT = new Color(0, 0, 189, 180);
	public static final Color VERT_FONCE_TRANSPARENT = new Color(0, 100, 0, 180);
	public static final Color BORDEAUX_TRANSPARENT = new Color(109, 7, 26, 180);
	public static final Color OR_FONCE_TRANSPARENT = new Color(181, 148, 16, 180);
	public static final Color GRIS_CLAIR_TRANSPARENT = new Color(180, 190, 200, 48);
	public static final Color GRIS_FONCE_TRANSPARENT = new Color(58, 58, 58, 180);
	public static final Color VIOLET_TRANSPARENT = new Color(143, 0, 255, 128);

	/** Limites **/
	public static final int TAILLE_MAX_MSG = 45; // par défaut : 45
	public static final boolean DEBUG_MODE = false; // par défaut : false

	public static void main(String[] args) {
		Jeu jeu = new Jeu();
        jeu.jouer();
	}
}