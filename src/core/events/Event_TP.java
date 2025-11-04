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

package events;

import core.Carte;
import core.Config;
import java.util.Objects;

public final class Event_TP extends Event {

	private final int xDst, yDst;
	private final Carte carteDst;

	/** Méthodes static **/
	private static void validerArguments(int xCaseDst, int yCaseDst, Carte carteDst) {
		Objects.requireNonNull(carteDst, "CarteDst event_tp null");
		if (xCaseDst < 0 || xCaseDst >= carteDst.getLargeur() || yCaseDst < 0 || yCaseDst >= carteDst.getHauteur()) {
			throw new IllegalArgumentException("Coordonnées d'event_tp hors limite carteDst " + carteDst.getNom() + " : " + xCaseDst + ", " + yCaseDst);
		}
	}

	/** Constructeur **/
	public Event_TP(int xCaseDst, int yCaseDst, Carte carteDst) {
		validerArguments(xCaseDst, yCaseDst, carteDst);
		xDst = xCaseDst * Config.TAILLE_CASES; // vraie valeur de x : il faut multiplier par TAILLE_CASES
		yDst = yCaseDst * Config.TAILLE_CASES; // vraie valeur de y : il faut multiplier par TAILLE_CASES
		this.carteDst = carteDst;
	}

	/** Getters **/
	public int getXDst() { return xDst; }
	public int getYDst() { return yDst; }
	public Carte getCarteDst() { return carteDst; }

	/** Autres méthodes **/
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Event_TP ev_tp)) return false;

		return xDst == ev_tp.xDst
			&& yDst == ev_tp.yDst
			&& Objects.equals(carteDst, ev_tp.carteDst);
	}

	@Override
	public int hashCode() { return Objects.hash(getClass(), xDst, yDst, carteDst); }
}