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

import events.Event;
import java.util.Objects;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Représente un ensemble d'événements organisés en plusieurs pages.
 * 
 * Chaque page contient une liste ordonnée d'events, permettant de structurer
 * des séquences d'actions successives dans le jeu (dialogues, téléportations, etc.).
 */
public final class EnsembleEvents {

	private final List<List<Event>> pageEvents = new ArrayList<>();

	/** Constructeur **/
	public EnsembleEvents() {}

	/** Getters **/
	public List<List<Event>> getPageEvents() { return Collections.unmodifiableList(pageEvents); }

	/** Autres méthodes **/
	public void add(int page, Event ev) {
		Objects.requireNonNull(ev, "Event ajoute null");
		if (page > pageEvents.size()) {
			throw new IllegalArgumentException("Impossible d’ajouter un event sur une page inexistante : seule la page suivante est acceptée pour agrandir la liste");
		}
		if (page == pageEvents.size()) pageEvents.add(new ArrayList<>());
		pageEvents.get(page).add(ev);
	}

	public Event getEventIfExists(int page, int index) {
		if (page < 0 || page >= pageEvents.size()) return null;
		List<Event> events = pageEvents.get(page);
		if (index < 0 || index >= events.size()) return null;
		return events.get(index);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof EnsembleEvents ee)) return false;

		return Objects.equals(pageEvents, ee.pageEvents);
	}

	@Override
	public int hashCode() { return Objects.hash(pageEvents); }
}