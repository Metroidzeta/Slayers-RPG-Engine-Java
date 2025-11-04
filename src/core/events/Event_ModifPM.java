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

public final class Event_ModifPM extends Event {

	private final int PM;

	/** Constructeur **/
	public Event_ModifPM(int PM) {
		if (PM == 0) throw new IllegalArgumentException("Modifier les PM de 0 ne sert à rien");
		this.PM = PM;
	}

	/** Getters **/
	public int getPM() { return PM; }

	/** Autres méthodes **/
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Event_ModifPM ev_mpm)) return false;

		return PM == ev_mpm.PM;
	}

	@Override
	public int hashCode() { return Objects.hash(getClass(), PM); }
}