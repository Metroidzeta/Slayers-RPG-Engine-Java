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

import java.util.Objects;

public final class Event_ModifPV extends Event {

	private final int PV;

	/** Constructeur **/
	public Event_ModifPV(int PV) {
		if (PV == 0) throw new IllegalArgumentException("Modifier les PV de 0 ne sert à rien");
		this.PV = PV;
	}

	/** Getters **/
	public int getPV() { return PV; }

	/** Autres méthodes **/
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Event_ModifPV ev_mpv)) return false;

		return PV == ev_mpv.PV;
	}

	@Override
	public int hashCode() { return Objects.hash(getClass(), PV); }
}