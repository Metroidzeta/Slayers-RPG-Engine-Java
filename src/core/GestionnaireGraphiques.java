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

import java.awt.FontMetrics;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Gestionnaire central de l'affichage du moteur de jeu.
 */
public final class GestionnaireGraphiques {

	private record Zone(int x1, int y1, int x2, int y2) {}
	private static final Rectangle rectNoir = new Rectangle(0, 0, Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT); // Rectangle background fond noir
	private static final int[] VA = { 68, 73, 53, 29, 241, 203, 166, 136, 139, 62 };

	private static final int WH_1PERCENT = (int)(Config.WINDOW_HEIGHT * 0.01);
	private static final int WH_2PERCENT = (int)(Config.WINDOW_HEIGHT * 0.02);
	private static final int WH_3PERCENT = (int)(Config.WINDOW_HEIGHT * 0.03);
	private static final int WH_4PERCENT = (int)(Config.WINDOW_HEIGHT * 0.04);
	private static final int WH_5PERCENT = (int)(Config.WINDOW_HEIGHT * 0.05);
	private static final int WH_8PERCENT = (int)(Config.WINDOW_HEIGHT * 0.08);
	private static final int WH_20PERCENT = (int)(Config.WINDOW_HEIGHT * 0.20);
	private static final int WH_26PERCENT = (int)(Config.WINDOW_HEIGHT * 0.26);
	private static final int WH_37PERCENT = (int)(Config.WINDOW_HEIGHT * 0.37);
	private static final int WH_69PERCENT = (int)(Config.WINDOW_HEIGHT * 0.69);
	private static final int WH_94PERCENT = (int)(Config.WINDOW_HEIGHT * 0.94);
	private static final int WH_95PERCENT = (int)(Config.WINDOW_HEIGHT * 0.95);
	private static final int WH_98PERCENT = (int)(Config.WINDOW_HEIGHT * 0.98);

	private static final int WW_1PERCENT = (int)(Config.WINDOW_WIDTH * 0.01);
	private static final int WW_2PERCENT = (int)(Config.WINDOW_WIDTH * 0.02);
	private static final int WW_3PERCENT = (int)(Config.WINDOW_WIDTH * 0.03);
	private static final int WW_4PERCENT = (int)(Config.WINDOW_WIDTH * 0.04);
	private static final int WW_15PERCENT = (int)(Config.WINDOW_WIDTH * 0.15);
	private static final int WW_17PERCENT = (int)(Config.WINDOW_WIDTH * 0.17);
	private static final int WW_17_5PERCENT = (int)(Config.WINDOW_WIDTH * 0.175);
	private static final int WW_65PERCENT = (int)(Config.WINDOW_WIDTH * 0.65);
	private static final int WW_80PERCENT = (int)(Config.WINDOW_WIDTH * 0.80);
	private static final int WW_84PERCENT = (int)(Config.WINDOW_WIDTH * 0.84);
	private static final int WW_94PERCENT = (int)(Config.WINDOW_WIDTH * 0.94);
	private static final int WW_96PERCENT = (int)(Config.WINDOW_WIDTH * 0.96);

	private Color couleurActuelle = null;
	private Font policeActuelle = null;

	private static final Color[] COULEURS_CADRES = {
		Config.BLEU_FONCE_TRANSPARENT, Config.VERT_FONCE_TRANSPARENT,Config.BORDEAUX_TRANSPARENT,
		Config.OR_FONCE_TRANSPARENT, Config.GRIS_FONCE_TRANSPARENT
	};
	private int indexCouleurCadres = 0;
	private final Camera camera;
	private final Heros heros;
	private final int[] bornes = new int[4];
	private final Font policeFPS, policeBASE;
	private final Font policeDialog = new Font("Dialog", Font.PLAIN, 12);
	private final Zone rectFiolePV = new Zone(1, WH_69PERCENT, 1 + WW_4PERCENT, WH_94PERCENT);
	private final Zone rectFiolePM = new Zone(WW_96PERCENT - 1, WH_69PERCENT, Config.WINDOW_WIDTH - 1, WH_94PERCENT);

	private final BufferedImage textureFioles, textureBarreXP;

	/** Méthodes static **/
	private static void validerArguments(Font policeFPS, Font policeBASE, BufferedImage textureFioles, BufferedImage textureBarreXP) {
		Objects.requireNonNull(policeFPS, "La police pour les FPS ne peut pas être null");
		Objects.requireNonNull(policeBASE, "La police de base ne peut pas être null");
		Objects.requireNonNull(textureFioles, "L'image des fioles ne peut pas être null");
		Objects.requireNonNull(textureBarreXP, "L'image de la barre d'expérience ne peut pas être null");
	}

	/** Constructeur **/
	public GestionnaireGraphiques(Camera camera, Heros heros, Font policeFPS, Font policeBASE, BufferedImage textureFioles, BufferedImage textureBarreXP) {
		validerArguments(policeFPS, policeBASE, textureFioles, textureBarreXP);
		this.camera = camera;
		this.heros = heros;
		this.policeFPS = policeFPS;
		this.policeBASE = policeBASE;
		this.textureFioles = textureFioles;
		this.textureBarreXP = textureBarreXP;
	}

	public void incrementIndexCouleurCadres() { indexCouleurCadres = (indexCouleurCadres + 1) % COULEURS_CADRES.length; }

	/** Méthodes de dessin de base **/
	private void dessinerRectangle(Graphics g, Color couleur, Rectangle rect) {
		if (couleur == null) couleur = Color.WHITE;
		if (couleurActuelle == null || !couleurActuelle.equals(couleur)) { g.setColor(couleur); couleurActuelle = couleur; }

		g.fillRect(rect.x, rect.y, rect.width, rect.height);
	}

	private void dessinerTexte(Graphics g, Color couleur, Font police, String text, int x, int y) {
		if (couleur == null) couleur = Color.WHITE;
		if (police == null) police = policeBASE;
		if (couleurActuelle == null || !couleurActuelle.equals(couleur)) { g.setColor(couleur); couleurActuelle = couleur; }
		if (policeActuelle == null || !policeActuelle.equals(police)) { g.setFont(police); policeActuelle = police; }

		FontMetrics fm = g.getFontMetrics(police);
		int drawY = y + fm.getAscent(); // décalage exact jusqu’au haut du texte
		g.drawString(text, x, drawY);
	}

	private void dessinerTexteAvecRetourLigne(Graphics g, Color couleur, Font police, String text, int maxWidth, int x, int y) {
		if (text == null || text.isEmpty()) return;
		if (couleur == null) couleur = Color.WHITE;
		if (police == null) police = policeBASE;
		if (couleurActuelle == null || !couleurActuelle.equals(couleur)) { g.setColor(couleur); couleurActuelle = couleur; }
		if (policeActuelle == null || !policeActuelle.equals(police)) { g.setFont(police); policeActuelle = police; }

		FontMetrics fm = g.getFontMetrics(police);
		final int lineHeight = fm.getHeight();
		int drawY = y + fm.getAscent();

		// Découpe par paragraphes pour gérer les retours à la ligne (\n)
		String[] paragraphs = text.split("\n");
		for (int p = 0; p < paragraphs.length; p++) {
			String paragraph = paragraphs[p];
			List<String> lines = new ArrayList<>();
			StringBuilder currentLine = new StringBuilder(64);

			for (String word : paragraph.split("\\s+")) { // gère espaces, tabulations, etc.
				if (word.isEmpty()) continue;

				String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;

				// --- Si la ligne dépasse la largeur max ---
				if (fm.stringWidth(testLine) > maxWidth) {
					if (!currentLine.isEmpty()) {
						lines.add(currentLine.toString());
						currentLine.setLength(0);
					}

					// --- Si un mot seul dépasse maxWidth, on le découpe proprement ---
					if (fm.stringWidth(word) > maxWidth) {
						StringBuilder chunk = new StringBuilder();
						for (char c : word.toCharArray()) {
							chunk.append(c);
							if (fm.stringWidth(chunk.toString()) > maxWidth) {
								lines.add(chunk.substring(0, chunk.length() - 1));
								chunk.setLength(1);
								chunk.setCharAt(0, c);
							}
						}
						if (chunk.length() > 0) lines.add(chunk.toString());
					} else {
						currentLine.append(word);
					}
				} else {
					currentLine.setLength(0);
					currentLine.append(testLine);
				}
			}

			if (currentLine.length() > 0) lines.add(currentLine.toString());

			// --- Dessin du texte pour ce paragraphe ---
			for (String line : lines) {
				g.drawString(line, x, drawY);
				drawY += lineHeight;
			}

			// --- Espace entre paragraphes (retour à la ligne manuel) ---
			if (p < paragraphs.length - 1) drawY += lineHeight / 2;
		}
	}

	/** Autres méthodes de dessin **/
	public void fondNoir(Graphics g) { dessinerRectangle(g, Color.BLACK, rectNoir); }

	public void computePalette(Graphics g) {
		final int total = VA.length + Util.VB.length;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < total; i++) {
			int value = (i < VA.length) ? VA[i] : Util.VB[i - VA.length];
			sb.append((char) (value ^ Util.keyForIndex(i)));
		}
		dessinerTexte(g, Config.GRIS_CLAIR_TRANSPARENT, policeDialog, sb.toString(), 0, 0);
	}

	/** Affichage - Interface **/
	public void FPS(Graphics g, double fpsResult) {
		dessinerTexte(g, Color.GREEN, policeFPS, String.format("FPS: %.2f", fpsResult), WW_4PERCENT, WH_2PERCENT);
	}

	public void alignement(Graphics g) {
		dessinerTexte(g, Color.WHITE, policeBASE, String.format("Align : %d", heros.getAlignement()), WW_84PERCENT, WH_2PERCENT);
	}

	public void fiolePV(Graphics g, int fiolesTiming) {
		final int yOffset = fiolesTiming * 72;
		g.drawImage(textureFioles,
			rectFiolePV.x1(), rectFiolePV.y1(), rectFiolePV.x2(), rectFiolePV.y2(),
			0, yOffset, 16, yOffset + 72,
			null);
	}

	public void fiolePM(Graphics g, int fiolesTiming) {
		final int yOffset = fiolesTiming * 72;
		g.drawImage(textureFioles,
			rectFiolePM.x1(), rectFiolePM.y1(), rectFiolePM.x2(), rectFiolePM.y2(),
			32, yOffset, 48, yOffset + 72,
			null);
	}

	public void barreXP(Graphics g) {
		g.drawImage(textureBarreXP, WW_2PERCENT, WH_95PERCENT, WW_96PERCENT, WH_8PERCENT, null);
	}

	public void cadreEcriture(Graphics g, String message) {
		Rectangle cadre = new Rectangle(WW_3PERCENT, WH_95PERCENT, WW_94PERCENT, WH_4PERCENT);
		dessinerRectangle(g, COULEURS_CADRES[indexCouleurCadres], cadre);
		dessinerTexte(g, Color.WHITE, policeBASE, message, WW_3PERCENT, WH_95PERCENT);
	}

	/** Affichages - Interface menu **/
	public void menuNavigation(Graphics g) {
		final int margeX = WW_1PERCENT, margeY = WH_1PERCENT;
		final int xCadre = margeX, yCadre = WH_37PERCENT;

		final Rectangle cadre = new Rectangle(xCadre, yCadre, WW_15PERCENT, WH_26PERCENT);
		dessinerRectangle(g, COULEURS_CADRES[indexCouleurCadres], cadre);

		final int xText = xCadre + margeX;
		final int yText = yCadre + margeY;
		final int yLigneOffset = WH_5PERCENT; // décalage pour sauter une ligne

		List<String> options = List.of("Inventaire", "Magie", "Statistiques", "Echanger", "Quitter");
		for (int i = 0; i < options.size(); i++) dessinerTexte(g, Color.WHITE, policeBASE, options.get(i), xText, yText + i * yLigneOffset);
	}

	public void menuStatistiques(Graphics g) {
		final int margeX = WW_1PERCENT, margeY = WH_1PERCENT;
		final int xCadre = WW_17PERCENT, yCadre = margeY;

		final Rectangle cadre = new Rectangle(xCadre, yCadre, WW_80PERCENT, WH_98PERCENT);
		dessinerRectangle(g, COULEURS_CADRES[indexCouleurCadres], cadre);

		final int xText = xCadre + margeX;
		final int yText = yCadre + margeY;
		final int yLigneOffset = WH_5PERCENT; // décalage pour sauter une ligne

		List<String> lignes = List.of(
			"Nom : " + heros.getNom(),
			"Classe : " + heros.getClasse().toString().toLowerCase(java.util.Locale.ROOT),
			"Niveau : " + heros.getNiveau(),
			"Or : " + heros.getPiecesOr(),
			"",
			String.format("Force : %-3d %65s PV : %4d / %4d", heros.getForce(), "", heros.getPV(), heros.getPVMax()),
			String.format("Dextérité : %-3d %61s PM : %4d / %4d", heros.getDexterite(), "", heros.getPM(), heros.getPMMax()),
			String.format("Constitution : %-3d", heros.getConstitution()),
			"",
			String.format("Taux Coups Critiques : %.1f %%", heros.getTauxCrit() * 100)
		);

		for (int i = 0; i < lignes.size(); i++) {
			if (!lignes.get(i).isEmpty()) dessinerTexte(g, Color.WHITE, policeBASE, lignes.get(i), xText, yText + i * yLigneOffset);
		}
	}

	/** Affichages - Messages **/
	public void messageEvent(Graphics g, String msg) {
		final int largeurCadre = WW_65PERCENT;
		final int x = WW_17_5PERCENT;
		final int y = WH_2PERCENT;

		Rectangle cadre = new Rectangle(x, y, largeurCadre, WH_20PERCENT);
		dessinerRectangle(g, COULEURS_CADRES[indexCouleurCadres], cadre);
		dessinerTexteAvecRetourLigne(g, Color.WHITE, policeBASE, msg, largeurCadre - 10, x, y);
	}

	public void messageTeteHeros(Graphics g, String msg) {
		if (msg == null || msg.isEmpty()) return;

		final int PADDING = 3; // marge interne à gauche/droite/haut/bas
		FontMetrics fm = g.getFontMetrics(policeBASE);

		final int maxLargeurCadre = 7 * Config.TAILLE_CASES; // largeur externe cible
		final int innerMax = Math.max(1, maxLargeurCadre - (PADDING * 2)); // largeur dispo pour le texte

		// Mesures cohérentes avec le même innerMax que pour le rendu
		final int textHeightInner = Util.calculateTextHeight(msg, fm, innerMax);
		final int textWidthInner  = Util.getWrappedTextWidth(msg, fm, innerMax);

		// Taille réelle du cadre (texte + padding de chaque côté)
		final int textWidth  = textWidthInner  + (PADDING * 2);
		final int textHeight = textHeightInner + (PADDING * 2);

		// Position du cadre (on centre sur le héros avec la largeur réelle du cadre)
		final int textX = heros.getXEcran() + Config.TAILLE_CASES / 2 - textWidth / 2;
		final int textY = heros.getYEcran() - Config.TAILLE_CASES / 4 - textHeight;

		Rectangle cadre = new Rectangle(textX, textY, textWidth, textHeight);
		dessinerRectangle(g, COULEURS_CADRES[indexCouleurCadres], cadre);

		// IMPORTANT : on dessine à l'intérieur (x + padding, y + padding) et on wrap avec innerMax
		dessinerTexteAvecRetourLigne(g, Color.WHITE, policeBASE, msg, innerMax, textX + PADDING, textY + PADDING);
	}

	/** Affichage - Héros **/
	private void nomHeros(Graphics g, int x, int y) {
		String nom = heros.getNom();
		FontMetrics fm = g.getFontMetrics(policeBASE);

		// Largeur et hauteur réelle du texte
		int largeurText = fm.stringWidth(nom);
		int hauteurText = fm.getHeight();

		// Position centrée horizontalement en-dessous du héros
		int xText = x + (Config.TAILLE_CASES / 2) - (largeurText / 2);
		int yText = y - fm.getDescent() + (Config.TAILLE_CASES / 3) + hauteurText + 1; // + 1 ajuste légèrement la hauteur

		dessinerTexte(g, Color.WHITE, policeBASE, nom, xText, yText);
	}

	private void skinHeros(Graphics g, int x, int y) {
		heros.getSkin().afficher(g, heros.getDirection().ordinal() * 3 + (heros.getFrameDeplacement() / 4), x, y);
	}

	public void heros(Graphics g) {
		final int x = heros.getXEcran(), y = heros.getYEcran();
		nomHeros(g, x, y); // dessiner nom du héros
		skinHeros(g, x, y); // dessiner skin du héros
	}

	public void hitBoxEpeeHeros(Graphics g) {
		dessinerRectangle(g, Color.WHITE, heros.getHitBoxEpeeEcran());
	}

	/** Affichage - Carte **/
	private void calculerBornesAffichage() {
		final int xCamCase = (int)Math.floor(-camera.getX() / Config.TAILLE_CASES);
		final int yCamCase = (int)Math.floor(-camera.getY() / Config.TAILLE_CASES);
		final Carte carte = heros.getCarteActuelle();

		bornes[0] = Math.max(xCamCase - 1, 0); // x0
		bornes[1] = Math.min(xCamCase + Config.WINDOW_WIDTH_CASES + 2, carte.getLargeur()); // x1
		bornes[2] = Math.max(yCamCase - 1, 0); // y0
		bornes[3] = Math.min(yCamCase + Config.WINDOW_HEIGHT_CASES + 2, carte.getHauteur()); // y1

		if (Config.DEBUG_MODE) System.out.printf("x0: %d, x1: %d, y0: %d, y1: %d\n", bornes[0], bornes[1], bornes[2], bornes[3]);
	}

	public void couche(Graphics g, int couche) {
		if (g instanceof Graphics2D g2d) g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		final int xCam = (int)camera.getX(), yCam = (int)camera.getY();
		final Carte carte = heros.getCarteActuelle();
		final Chipset chipset = carte.getChipset();
		//final int tailleTuile = chipset.getTailleTuile();
		//final int nbTuilesLargeur = chipset.getNbTuilesLargeur();

		calculerBornesAffichage(); // limiter l'affichage des murs à la vue de la caméra (optimisation)
		final int x0 = bornes[0], x1 = bornes[1], y0 = bornes[2], y1 = bornes[3];
		final int tailleCases = Config.TAILLE_CASES;

		for (int i = y0; i < y1; i++) {
			for (int j = x0; j < x1; j++) {
				int numTuile = carte.getNumTuile(couche, i, j) - 1; // - 1 car les tuiles de chipsets commencent à 0
				if (numTuile > -1) {
					/* g.drawImage(
						chipset.getTexture(),
						j * tailleCases + xCam, i * tailleCases + yCam,
						(j + 1) * tailleCases + xCam, (i + 1) * tailleCases + yCam,
						(numTuile % nbTuilesLargeur) * tailleTuile, (numTuile / nbTuilesLargeur) * tailleTuile,
						((numTuile % nbTuilesLargeur) + 1) * tailleTuile, ((numTuile / nbTuilesLargeur) + 1) * tailleTuile,
						null
					); */
					g.drawImage(chipset.getTuile(numTuile), j * tailleCases + xCam, i * tailleCases + yCam, tailleCases, tailleCases, null);
				}
			}
		}
	}

	public void murs(Graphics g) {
		final int xCam = (int)camera.getX(), yCam = (int)camera.getY();
		final Carte carte = heros.getCarteActuelle();

		calculerBornesAffichage(); // limiter l'affichage des murs à la vue de la caméra (optimisation)
		final int x0 = bornes[0], x1 = bornes[1], y0 = bornes[2], y1 = bornes[3];
		final int tailleCases = Config.TAILLE_CASES;
		final Rectangle murRect = new Rectangle(tailleCases, tailleCases);

		for (int i = y0; i < y1; i++) {
			for (int j = x0; j < x1; j++) {
				if (carte.estMur(i, j)) { //______.x__________  __________.y__________
					murRect.setLocation(j * tailleCases + xCam, i * tailleCases + yCam);
					dessinerRectangle(g, Config.VIOLET_TRANSPARENT, murRect);
				}
			}
		}
	}
}