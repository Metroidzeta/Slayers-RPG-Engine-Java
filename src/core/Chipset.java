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

import java.util.Objects;
import java.awt.image.BufferedImage;

/**
 * Représente un chipset, c’est-à-dire un ensemble de tuiles graphiques
 * découpées à partir d’une image source unique servant à composer les cartes du jeu.
 */
public final class Chipset {

	private static final String DOSSIER = "img/";

	private final String nom;
	private final int tailleTuile; // Taille d'une tuile en pixels (n*n)
	private final int nbTuilesHauteur, nbTuilesLargeur; // nombre de tuiles en cases
	private final BufferedImage[] tuiles;

	/** Méthodes static **/
	private static void validerArguments(String nomFichier, int tailleTuile) {
		if (nomFichier == null || nomFichier.isBlank()) throw new IllegalArgumentException("Nom fichier du chipset null ou vide");
		if (tailleTuile < 1) throw new IllegalArgumentException("TailleTuile du chipset " + nomFichier + " < 1");
	}

	private static BufferedImage[] extraireTuiles(BufferedImage image, int tailleTuile, int nbTuilesHauteur, int nbTuilesLargeur) {
		final int nbTuiles = nbTuilesHauteur * nbTuilesLargeur;
		BufferedImage[] result = new BufferedImage[nbTuiles];
		for (int i = 0; i < nbTuilesHauteur; i++) {
			final int y = i * tailleTuile;
			final int ligneIndex = i * nbTuilesLargeur;
			for (int j = 0; j < nbTuilesLargeur; j++) {
				result[ligneIndex + j] = image.getSubimage(j * tailleTuile, y, tailleTuile, tailleTuile);
			}
		}
		return result;
	}

	/** Constructeur **/
	public Chipset(String nomFichier, int tailleTuile) {
		validerArguments(nomFichier, tailleTuile);
		nom = nomFichier;
		this.tailleTuile = tailleTuile;

		final BufferedImage texture = Util.chargerImage(DOSSIER + nomFichier); // Charger l'image source
		final int largeurTexture = texture.getWidth(), hauteurTexture = texture.getHeight();
		if (largeurTexture % tailleTuile != 0 || hauteurTexture % tailleTuile != 0) {
			throw new IllegalArgumentException("Dimensions de l'image incompatibles avec tailleTuile (pas mod 0)");
		}
		nbTuilesHauteur = hauteurTexture / tailleTuile;
		nbTuilesLargeur = largeurTexture / tailleTuile;
		tuiles = extraireTuiles(texture, tailleTuile, nbTuilesHauteur, nbTuilesLargeur);
	}

	/** Getters **/
	public String getNom() { return nom; }
	public int getTailleTuile() { return tailleTuile; }
	public int getNbTuilesHauteur() { return nbTuilesHauteur; }
	public int getNbTuilesLargeur() { return nbTuilesLargeur; }
	public BufferedImage getTuile(int index) {
		if (index < 0 || index >= tuiles.length) throw new IndexOutOfBoundsException("Index tuile invalide: " + index);
		return tuiles[index];
	}

	/** Autres méthodes **/
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Chipset chipset)) return false;

		return nom.equals(chipset.nom)
			&& tailleTuile == chipset.tailleTuile
			&& nbTuilesHauteur == chipset.nbTuilesHauteur
			&& nbTuilesLargeur == chipset.nbTuilesLargeur;
	}

	@Override
	public int hashCode() {
		return Objects.hash(nom, tailleTuile, nbTuilesHauteur, nbTuilesLargeur);
	}
}