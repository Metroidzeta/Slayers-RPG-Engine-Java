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

/**
 * Représente une jauge de valeur (PV, PM, etc.)
 */
public final class Jauge {
	private int valeur, max; // valeur actuelle / valeur maximale

	public Jauge(int valeurInitiale, int max) {
		if (max < 1) throw new IllegalArgumentException("Le max < 1");
		if (valeurInitiale < 0 || valeurInitiale > max) throw new IllegalArgumentException("La valeur initiale < 0 ou > " + max);
		this.valeur = valeurInitiale;
		this.max = max;
	}

	/** Getters **/
	public int getValeur() { return valeur; }
	public int getMax() { return max; }
	public double getRatio() { return (double)valeur / max; }

	/** Setters **/
	public void setMax(int nouvMax) {
		if (nouvMax < 1) throw new IllegalArgumentException("Le nouveau max est < 1");
		max = nouvMax;
		if (valeur > max) valeur = max; // ajuste la valeur si max inférieur
	}
	public void setValeur(int nouvValeur) { valeur = Math.max(0, Math.min(nouvValeur, max)); } // entre 0 et max

	/** Autres méthodes **/
	public boolean estPleine() { return valeur == max; }
	public boolean estVide() { return valeur == 0; }
	public void modifier(int delta) { valeur = Math.max(0, Math.min(valeur + delta, max)); } // entre 0 et max

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Jauge jauge)) return false;

		return valeur == jauge.valeur
			&& max == jauge.max;
	}

	@Override
	public int hashCode() { return Objects.hash(valeur, max); }

	@Override
	public String toString() { return "[" + valeur + "/" + max + "]"; }
}