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

/**
 * Caméra 2D fluide capable de suivre dynamiquement une entité "CamLock".
 * Elle interpole la position de la cible pour obtenir un mouvement fluide,
 * même à des fréquences de rendu supérieures au taux de mise à jour logique.
 */
public final class Camera {

	public interface CamLock { int getXCam(); int getYCam(); }

	private CamLock cible; // cible actuellement suivie
	private double xPrec, yPrec;
	private double xInter, yInter;
	private int xActuel, yActuel;

	/** Getters **/
	public double getX() { return xInter; }
	public double getY() { return yInter; }

	/** Définit la cible suivie par la caméra **/
	public void setCible(CamLock cible) {
		this.cible = cible;
		if (cible != null) {
			xActuel = cible.getXCam();
			yActuel = cible.getYCam();
			sync();
		}
	}

	/** Met à jour les positions précédentes et actuelles (appelé à chaque update logique) */
	public void update() {
		if (cible == null) return;
		xPrec = xActuel;
		yPrec = yActuel;
		xActuel = cible.getXCam();
		yActuel = cible.getYCam();
	}

	/** Interpolation entre la position précédente et la position actuelle **/
	public void interpolate(double interpolation) {
		xInter = xPrec + (xActuel - xPrec) * interpolation;
		yInter = yPrec + (yActuel - yPrec) * interpolation;
	}

	/** Force une synchronisation complète entre les positions (utile après un TP ou changement de carte) **/
	public void sync() {
		xPrec = xActuel;
		yPrec = yActuel;
		xInter = xActuel;
		yInter = yActuel;
	}

	@Override
	public String toString() {
		return String.format("Camera [x: %.2f, y: %.2f, xActuel: %d, yActuel: %d]", xInter, yInter, xActuel, yActuel);
	}
}
