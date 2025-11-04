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

package ressources;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import core.Carte;
import core.Jeu;
import events.*;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Charge automatiquement tous les events depuis le dossier "cartes" et les injecte dans les cartes correspondantes
 */
public final class ChargerEvents {

	private ChargerEvents() { throw new AssertionError("La classe ChargerEvents ne doit pas être instanciée."); } // Empêche toute instanciation

	private record EventData(String nomCarte, int xCase, int yCase, Event event) {}

	private static final String DOSSIER = "cartes";

	private static List<EventData> getEvents(Jeu jeu) {
		final List<EventData> events = new ArrayList<>();
		try {
			Path dossierCartes = Path.of(DOSSIER);
			if (!Files.exists(dossierCartes)) return List.of();

			try (DirectoryStream<Path> fichiers = Files.newDirectoryStream(dossierCartes, "*_ME.json")) {
				for (Path fichier : fichiers) lireFichierJSON(fichier, jeu, events);
			}
		} catch (IOException e) { throw new IllegalArgumentException("[ERREUR] Lecture des fichiers d'événements : " + e.getMessage(), e); }
		return events;
	}

	private static String extraireNomCarte(Path chemin) {
		String nomFichier = chemin.getFileName().toString();
		int index = nomFichier.indexOf("_ME.json");
		return (index > 0) ? nomFichier.substring(0, index) : nomFichier;
	}

	private static void lireFichierJSON(Path chemin, Jeu jeu, List<EventData> events) {
		try (FileReader reader = new FileReader(chemin.toFile())) { // try-with-ressources
			JsonObject racine = JsonParser.parseReader(reader).getAsJsonObject();
			JsonArray ensemblesEvents = racine.getAsJsonArray("ensemblesEvents");
			if (ensemblesEvents == null) return;

			String nomCarte = extraireNomCarte(chemin);
			for (JsonElement blocElem : ensemblesEvents) {
				JsonObject bloc = blocElem.getAsJsonObject();
				if (!bloc.has("x") || !bloc.has("y") || !bloc.has("events")) {
					System.err.println("[ERREUR] Bloc d'événement incomplet ignoré dans " + chemin.getFileName());
					continue;
				}
				int xCase = bloc.get("x").getAsInt();
				int yCase = bloc.get("y").getAsInt();
				JsonArray arr = bloc.getAsJsonArray("events");
				if (arr == null) continue;

				for (JsonElement evElem : arr) {
					JsonObject ev = evElem.getAsJsonObject();

					if (ev.has("msg")) {
						String msg = ev.get("msg").getAsString();
						events.add(new EventData(nomCarte, xCase, yCase, new Event_MSG(msg)));
					}

					else if (ev.has("tp")) {
						JsonObject tp = ev.getAsJsonObject("tp");
						int xCaseDst = tp.get("xDst").getAsInt();
						int yCaseDst = tp.get("yDst").getAsInt();
						String nomCarteDst = tp.get("carteDst").getAsString();
						events.add(new EventData(nomCarte, xCase, yCase, new Event_TP(xCaseDst, yCaseDst, jeu.getCarte(nomCarteDst))));
					}

					else if (ev.has("musique")) {
						String nomMusique = ev.get("musique").getAsString();
						events.add(new EventData(nomCarte, xCase, yCase, new Event_JM(jeu.getMusique(nomMusique))));
					}

					else if (ev.has("arretMusique")) {
						events.add(new EventData(nomCarte, xCase, yCase, new Event_AM()));
					}

					else if (ev.has("PV")) {
						int val = ev.get("PV").getAsInt();
						events.add(new EventData(nomCarte, xCase, yCase, new Event_ModifPV(val)));
					}

					else if (ev.has("PM")) {
						int val = ev.get("PM").getAsInt();
						events.add(new EventData(nomCarte, xCase, yCase, new Event_ModifPM(val)));
					}

					else if (ev.has("LVLUP")) {
						events.add(new EventData(nomCarte, xCase, yCase, new Event_LVLUP()));
					}
				}
			}
		} catch (Exception e) {
			System.err.println("[ERREUR] Fichier " + chemin + " : " + e.getMessage());
		}
	}

	private static void ajouterEvent(EventData elem, Jeu jeu) {
		Carte carte = jeu.getCarte(elem.nomCarte);
		if (carte == null) throw new IllegalArgumentException("[ERREUR] Carte introuvable : \"" +  elem.nomCarte + "\" pour y insérer un event");
		carte.ajouterEvent(elem.xCase, elem.yCase, elem.event);
	}

	public static void inject(Jeu jeu) {
		Objects.requireNonNull(jeu, "Le jeu ne peut pas être null");
		getEvents(jeu).forEach(elem -> ajouterEvent(elem, jeu));
	}
}