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

import core.Util;
import events.Event;
import java.awt.Rectangle;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Map;
import java.util.HashMap;

/**
 * Représente une carte (niveau du jeu) composée de plusieurs couches de tuiles,
 * avec une largeur et une hauteur fixes, un chipset, une musique et des événements associés.
 */
public final class Carte {

	private static final int TAILLE_CARTE_MAX = 100; // par défaut : 100
	private static final int NB_COUCHES = 3; // par défaut : 3
	private static final int TUILE_VIDE = 0;
	private static final Rectangle[][] MATRICE_RECT_GLOBALE = creerMatriceRectGlobale(); // Matrice de rectangles globale représentant les cases (partagée entre toutes les cartes)

	private final String nom;
	private final int largeur, hauteur; // en cases
	private final Chipset chipset;
	private final Musique musique;
	private final int[][][] couches; // 3 couches (matrices) de numTuileChipset (C0 < C1 < Héros < C2)
	private final boolean[][] murs; // Matrice booléenne représentant les murs sur chaque case (false = pas de mur, true = mur)
	private final Map<Position, EnsembleEvents> events = new HashMap<>();

	/** Méthodes static **/
	private static Rectangle[][] creerMatriceRectGlobale() {
		final int tailleCases = Config.TAILLE_CASES;
		Rectangle[][] matrice = new Rectangle[TAILLE_CARTE_MAX][TAILLE_CARTE_MAX];
		for (int i = 0; i < TAILLE_CARTE_MAX; i++) {
			final int y = i * Config.TAILLE_CASES;
			for (int j = 0; j < TAILLE_CARTE_MAX; j++) {
				matrice[i][j] = new Rectangle(j * tailleCases, y, tailleCases, tailleCases);
			}
		}
		return matrice;
	}

	private static void validerArguments(String nom, int largeur, int hauteur, Chipset chipset) {
		if (nom == null || nom.isBlank()) throw new IllegalArgumentException("Carte: nom null ou vide");
		if (largeur < 1 || largeur > TAILLE_CARTE_MAX) throw new IllegalArgumentException("Carte (" + nom + "): largeur < 1 ou > " + TAILLE_CARTE_MAX);
		if (hauteur < 1 || hauteur > TAILLE_CARTE_MAX) throw new IllegalArgumentException("Carte (" + nom + "): hauteur < 1 ou > " + TAILLE_CARTE_MAX);
		Objects.requireNonNull(chipset, "Carte (" + nom + "): chipset null passé en paramètre");
	}

	/** Constructeur **/
	public Carte(String nom, int largeur, int hauteur, Chipset chipset, Musique musique, int[][] c0, int[][] c1, int[][] c2, boolean[][] murs) {
		validerArguments(nom, largeur, hauteur, chipset);
		this.nom = nom;
		this.largeur = largeur;
		this.hauteur = hauteur;
		this.chipset = chipset;
		this.musique = musique;
		this.couches = new int[NB_COUCHES][][];
		int[][][] couchesSrc = { c0, c1, c2 };
		for (int c = 0; c < NB_COUCHES; c++) {
			this.couches[c] = (couchesSrc[c] != null) ? couchesSrc[c] : Util.creerMatriceINT(hauteur, largeur, TUILE_VIDE);
        }
		this.murs = (murs != null) ? murs : new boolean[hauteur][largeur];
	}

	public static Carte newCarteVide(String nom, int largeur, int hauteur, Chipset chipset, Musique musique) {
		return new Carte(nom, largeur, hauteur, chipset, musique, null, null, null, null);
	}

	private boolean estUnIndexMatriceValide(int i, int j) { return i >= 0 && i < hauteur && j >= 0 && j < largeur; }

	/** Getters **/
	public String getNom() { return nom; }
	public int getLargeur() { return largeur; }
	public int getHauteur() { return hauteur; }
	public Chipset getChipset() { return chipset; }
	public Musique getMusique() { return musique; }
	public int getNumTuile(int couche, int i, int j) {
		if (couche < 0 || couche >= NB_COUCHES) throw new IndexOutOfBoundsException("couche < 0 ou >= " + NB_COUCHES);
		if (!estUnIndexMatriceValide(i, j)) throw new IndexOutOfBoundsException("i < 0 ou i >= " + hauteur + " ou j < 0 ou j >= " + largeur);
		return couches[couche][i][j];
	}
	public boolean estMur(int i, int j) {
		if (!estUnIndexMatriceValide(i, j)) throw new IndexOutOfBoundsException("i < 0 ou i >= " + hauteur + " ou j < 0 ou j >= " + largeur);
		return murs[i][j];
	}

	/** Autres méthodes **/
	// --- Collisions ---
	public boolean detecterCollisionsMurs(Rectangle rect) {
		final int tailleCases = Config.TAILLE_CASES;
		final int x0 = Math.max(0, rect.x / tailleCases);
		final int x1 = Math.min(largeur, (rect.x + rect.width - 1) / tailleCases + 1);
		final int y0 = Math.max(0, rect.y / tailleCases);
		final int y1 = Math.min(hauteur, (rect.y + rect.height - 1) / tailleCases + 1);
		if (Config.DEBUG_MODE) System.out.printf("x0: %d, x1: %d, y0: %d, y1: %d%n", x0, x1, y0, y1);

		for (int i = y0; i < y1; i++) {
			for (int j = x0; j < x1; j++) {
				if (murs[i][j] && rect.intersects(MATRICE_RECT_GLOBALE[i][j])) return true;
			}
		}
		return false;
	}

	public EnsembleEvents detecterCollisionsEvents(Rectangle rect) {
		for (Map.Entry<Position, EnsembleEvents> entry : events.entrySet()) {
			Position pos = entry.getKey();
			Rectangle caseRect = MATRICE_RECT_GLOBALE[pos.y()][pos.x()];
			if (rect.intersects(caseRect)) return entry.getValue();
		}
		return null;
	}

	// --- Events ---
	public void ajouterEvent(int xCase, int yCase, Event ev) {
		if (!estUnIndexMatriceValide(yCase, xCase)) throw new IllegalArgumentException("Carte (" + nom + "): Coordonnées d'event hors limite [" + xCase + "," + yCase + "]");
		Objects.requireNonNull(ev, "Event null ajouté à la carte " + nom);
		events.computeIfAbsent(new Position(xCase, yCase), pos -> new EnsembleEvents()).add(0, ev);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Carte carte)) return false;

		return nom.equals(carte.nom)
			&& largeur == carte.largeur
			&& hauteur == carte.hauteur
			&& Objects.equals(chipset, carte.chipset)
			&& Objects.equals(musique, carte.musique)
			&& Arrays.deepEquals(couches, carte.couches)
			&& Arrays.deepEquals(murs, carte.murs)
			&& Objects.equals(events, carte.events);
	}

	@Override
	public int hashCode() {
		return Objects.hash(nom, largeur, hauteur, chipset, musique, Arrays.deepHashCode(couches), Arrays.deepHashCode(murs), events);
	}
}