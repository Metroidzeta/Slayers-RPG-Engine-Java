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

import java.awt.Rectangle;
import java.util.Objects;

/**
 * Représente le héros contrôlé par le joueur : position, statistiques et état de jeu.
 */
public final class Heros implements Camera.CamLock {

	private static final class Stats {  // Initialisation des Stats de base
		static final int FORCE = 12;
		static final int DEXTERITE = 9;
		static final int CONSTITUTION = 10;
		static final int ALIGNEMENT = 50;
		static final int PV = 600;
		static final int PM = 250;
	}

	private static final int OFFSET_EPEE_PX = Config.TAILLE_CASES / 4;

	private final String nom;
	private Skin skin;
	private final HerosClasses classe;
	private int niveau;
	private int piecesOr;
	private Position position; // position réelle
	private final Position positionEcran; // position SUR L'ECRAN
	private int xOffset, yOffset; // décalage entre position réelle (x,y) et la position (x,y) SUR L'ECRAN
	private int force = Stats.FORCE;
	private int dexterite = Stats.DEXTERITE;
	private int constitution = Stats.CONSTITUTION;
	private final Jauge PV = new Jauge(Stats.PV, Stats.PV); // PV / PVMax
	private final Jauge PM = new Jauge(Stats.PM, Stats.PM); // PM / PMMax
	private Directions direction = Directions.BAS; // regarde vers le bas par défaut
	private final Jauge alignement = new Jauge(Stats.ALIGNEMENT, 100);
	private boolean peutAttaquer, attaqueEpee, estBloque, messageTete, estEnTrainDEcrire, estDansUnEvent;
	private Carte carteActuelle;
	private float tauxCrit;

	private final Rectangle tempHitBox;
	private final Rectangle[] hitBoxEpee, hitBoxEpeeEcran;
	private int frameDeplacement = 7;

	/** Méthodes static **/
	private static void validerArguments(String nom, Skin skin, HerosClasses hc, int niveau, int piecesOr, int xCase, int yCase, Carte carte, float tauxCrit) {
		if (nom == null || nom.isBlank()) throw new IllegalArgumentException("Nom du heros null ou vide");
		Objects.requireNonNull(skin, "Skin du heros " + nom + " null");
		Objects.requireNonNull(hc, "Classe du heros " + nom + " null");
		if (niveau < 1 || niveau > Config.NIVEAU_MAX) throw new IllegalArgumentException("Niveau du heros " + nom + " < 1 ou > " + Config.NIVEAU_MAX);
		if (piecesOr < 0) throw new IllegalArgumentException("PiecesOr du heros " + nom + " < 0");
		Objects.requireNonNull(carte, "Carte du heros " + nom + " null");
		if (xCase < 0 || xCase >= carte.getLargeur()) throw new IllegalArgumentException("xCase heros " + nom + " < 0 ou >= largeur carte");
		if (yCase < 0 || yCase >= carte.getHauteur()) throw new IllegalArgumentException("yCase heros " + nom + " < 0 ou >= hauteur carte");
		if (tauxCrit < 0 || tauxCrit > 100) throw new IllegalArgumentException("TauxCrit du heros " + nom + " < 0 ou > 100");
	}

	private static int alignToTile(int value) { return value - (value % Config.TAILLE_CASES); }

	private static Rectangle[] creerHitBoxsEpee(int x, int y) { // HIT BOX EPEE (REELLE / A L'ECRAN)
		final int taille = Config.TAILLE_CASES;
		return new Rectangle[] {
			new Rectangle(x, y + (taille / 2) + OFFSET_EPEE_PX, taille, taille / 2), // BAS
			new Rectangle(x - OFFSET_EPEE_PX, y, taille / 2, taille),                // GAUCHE
			new Rectangle(x + (taille / 2) + OFFSET_EPEE_PX, y, taille / 2, taille), // DROITE
			new Rectangle(x, y - (taille / 2) + OFFSET_EPEE_PX, taille, taille / 2)  // HAUT
		};
	}

	/** Constructeur **/
	public Heros(String nom, Skin skin, HerosClasses hc, int niveau, int piecesOr, int xCase, int yCase, Carte carte, float tauxCrit) {
		validerArguments(nom, skin, hc, niveau, piecesOr, xCase, yCase, carte, tauxCrit);
		this.nom = nom;
		this.skin = skin;
		classe = hc;
		this.niveau = niveau;
		this.piecesOr = piecesOr;
		position = new Position(xCase * Config.TAILLE_CASES, yCase * Config.TAILLE_CASES);
		positionEcran = new Position(alignToTile(Config.WINDOW_WIDTH / 2), alignToTile(Config.WINDOW_HEIGHT / 2));
		carteActuelle = carte;
		this.tauxCrit = tauxCrit / 100f; // divise par 100 (pour obtenir un ratio)

		tempHitBox = new Rectangle(getX(), getY(), Config.TAILLE_CASES, Config.TAILLE_CASES);
		hitBoxEpee = creerHitBoxsEpee(getX(), getY());
		hitBoxEpeeEcran = creerHitBoxsEpee(getXEcran(), getYEcran());
		updateOffSet();
	}

	/** Getters **/
	public String getNom() { return nom; }
	public Skin getSkin() { return skin; }
	public HerosClasses getClasse() { return classe; }
	public int getNiveau() { return niveau; }
	public int getPiecesOr() { return piecesOr; }
	public Position getPosition() { return position; }
	public int getXOffset() { return xOffset; }
	public int getYOffset() { return yOffset; }
	public int getX() { return position.x(); }
	public int getY() { return position.y(); }
	public int getXCase() { return position.x() / Config.TAILLE_CASES; }
	public int getYCase() { return position.y() / Config.TAILLE_CASES; }
	public Position getPositionEcran() { return positionEcran; }
	public int getXEcran() { return positionEcran.x(); }
	public int getYEcran() { return positionEcran.y(); }
	@Override public int getXCam() { return getXOffset(); }
	@Override public int getYCam() { return getYOffset(); }
	public int getForce() { return force; }
	public int getDexterite() { return dexterite; }
	public int getConstitution() { return constitution; }
	public int getPV() { return PV.getValeur(); }
	public int getPVMax() { return PV.getMax(); }
	public int getPM() { return PM.getValeur(); }
	public int getPMMax() { return PM.getMax(); }
	double getPVRatio() { return PV.getRatio(); }
	double getPMRatio() { return PM.getRatio(); }
	public Directions getDirection() { return direction; }
	public int getAlignement() { return alignement.getValeur(); }
	public boolean getPeutAttaquer() { return peutAttaquer; }
	public boolean getAttaqueEpee() { return attaqueEpee; }
	public boolean estBloque() { return estBloque; }
	public boolean getMessageTete() { return messageTete; } 
	public boolean estEnTrainDEcrire() { return estEnTrainDEcrire; }
	public boolean estDansUnEvent() { return estDansUnEvent; }
	public Carte getCarteActuelle() { return carteActuelle; }
	public float getTauxCrit() { return tauxCrit; }
	public Rectangle getHitBox() { return position.getHitbox(Config.TAILLE_CASES); }
	public Rectangle getHitBoxEpee() { return hitBoxEpee[direction.ordinal()]; }
	public Rectangle getHitBoxEpeeEcran() { return hitBoxEpeeEcran[direction.ordinal()]; }
	public int getFrameDeplacement() { return frameDeplacement; }

	/** Setters **/
	public void setPeutAttaquer(boolean b) { peutAttaquer = b; }
	public void setAttaqueEpee(boolean b) { attaqueEpee = b; }
	public void setEstBloque(boolean b) { estBloque = b; }
	public void setMessageTete(boolean b) { messageTete = b; }
	public void setEstEnTrainDEcrire(boolean b) { estEnTrainDEcrire = b; }
	public void setEstDansUnEvent(boolean b) { estDansUnEvent = b; }
	public void setCarteActuelle(Carte carte) { carteActuelle = carte; }
	public void setFrameDeplacement(int fd) { frameDeplacement = fd; }

	/** Autres méthodes **/
	private void incrementFrameDeplacement() { frameDeplacement = (frameDeplacement + 1) % 12; }

	public void updateOffSet() {
		xOffset = positionEcran.x() - position.x();
		yOffset = positionEcran.y() - position.y();
	}

	public void modifierPosition(int x, int y) { position = new Position(x, y); updateOffSet(); }
	public void modifierAlignement(int val) { alignement.modifier(val); }
	public void modifierPV(int val) { PV.modifier(val); }
	public void modifierPM(int val) { PM.modifier(val); }
	public void levelUP() { if (niveau < Config.NIVEAU_MAX) niveau++; }

	public void updateHitBoxEpee() {
		final int taille = Config.TAILLE_CASES;
		final Rectangle rect = hitBoxEpee[direction.ordinal()];
		final int x = position.x(), y = position.y();
		switch (direction) {
			case BAS     -> rect.setLocation(x, y + (taille / 2) + OFFSET_EPEE_PX);
			case GAUCHE  -> rect.setLocation(x - OFFSET_EPEE_PX, y);
			case DROITE  -> rect.setLocation(x + (taille / 2) + OFFSET_EPEE_PX, y);
			case HAUT    -> rect.setLocation(x, y - (taille / 2) + OFFSET_EPEE_PX);
		}
	}

	public boolean deplacer(Directions d) {
		tempHitBox.setLocation(position.x(), position.y());
		switch (d) {
			case HAUT -> tempHitBox.y -= (int)Config.DEPLACEMENT_JOUEUR;
			case BAS -> tempHitBox.y += (int)Config.DEPLACEMENT_JOUEUR;
			case GAUCHE -> tempHitBox.x -= (int)Config.DEPLACEMENT_JOUEUR;
			case DROITE -> tempHitBox.x += (int)Config.DEPLACEMENT_JOUEUR;
		}

		boolean changerDirection = false;
		if (d != direction) {
			direction = d;
			changerDirection = true;
		}

		if (tempHitBox.x >= 0 && tempHitBox.y >= 0
			&& tempHitBox.x <= (carteActuelle.getLargeur() - 1) * Config.TAILLE_CASES
			&& tempHitBox.y <= (carteActuelle.getHauteur() - 1) * Config.TAILLE_CASES
			&& !carteActuelle.detecterCollisionsMurs(tempHitBox)) {

			modifierPosition(tempHitBox.x, tempHitBox.y);
			incrementFrameDeplacement();
			//System.out.printf("%s s'est deplacé vers %-6s : (%d,%d)\n", nom, direction, getXCase(), getYCase());
			return true;
		}
		return changerDirection;
	}

	@Override
	public String toString() {
		return String.format(
			"Heros[nom: %s, classe: %s, niveau: %d, PV: %s, PM: %s, position: %s]",
			nom, classe, niveau, PV, PM, position
		);
	}
}