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

import core.Musique;
import java.util.Objects;

public final class Event_JM extends Event {

	private final Musique musique;

	/** Constructeur **/
	public Event_JM(Musique musique) {
		this.musique = Objects.requireNonNull(musique, "Musique de l'Event_JM null");
	}

	/** Getters **/
	public Musique getMusique() { return musique; }

	/** Autres méthodes **/
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Event_JM ev_jm)) return false;

		return Objects.equals(musique, ev_jm.musique);
	}

	@Override
	public int hashCode() { return Objects.hash(getClass(), musique); }
}