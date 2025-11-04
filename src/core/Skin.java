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

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Objects;

public final class Skin {

	private static final String DOSSIER = "img/";
	private static final int REGION_WIDTH = 48;
	private static final int REGION_HEIGHT = 48;
	private static final int ROWS = 4;
	private static final int COLS = 3;
	private static final int TOTAL_REGIONS = ROWS * COLS;

	private final String nom;
	private final BufferedImage texture; // image source
	private final BufferedImage[] textureRegions; // régions de l'image source découpée

	/** Méthodes static **/
	private static BufferedImage[] extraireRegions(BufferedImage image) {
		final BufferedImage[] result = new BufferedImage[TOTAL_REGIONS];
		for (int i = 0; i < ROWS; i++) {
			final int y = i * REGION_HEIGHT;
			final int ligneIndex = i * COLS;
			for (int j = 0; j < COLS; j++) {
				result[ligneIndex + j] = image.getSubimage(j * REGION_WIDTH, y, REGION_WIDTH, REGION_HEIGHT);
			}
		}
		return result;
	}

	/** Constructeur **/
	public Skin(String nomFichier) {
		if (nomFichier == null || nomFichier.isBlank()) throw new IllegalArgumentException("Nom fichier null ou vide");
		this.nom = nomFichier;
		texture = Util.chargerImage(DOSSIER + nomFichier);
		textureRegions = extraireRegions(texture);
	}

	/** Getters **/
	public String getNom() { return nom; }

	/** Autres méthodes **/
	public void afficher(Graphics g, int numRegion, int x, int y) {
		Objects.requireNonNull(g, "Graphics null passe en paramètre");
		if (numRegion < 0 || numRegion >= TOTAL_REGIONS) throw new IllegalArgumentException("Indice de région invalide : " + numRegion);
		g.drawImage(textureRegions[numRegion], x, y, Config.TAILLE_CASES, Config.TAILLE_CASES, null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Skin skin)) return false;

		return Objects.equals(nom, skin.nom)
			&& Objects.equals(texture, skin.texture);
	}

	@Override
	public int hashCode() { return Objects.hash(nom, texture); }
}