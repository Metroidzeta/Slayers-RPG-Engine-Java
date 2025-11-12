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

	/** Structure de données pour un event **/
	private record EventData(String nomCarte, int xCase, int yCase, Event event) {}

	private static final String DOSSIER = "cartes";

	/** Méthodes static **/
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

	private static boolean isIntKey(JsonObject obj, String key) {
		return obj.has(key)
			&& obj.get(key).isJsonPrimitive()
			&& obj.getAsJsonPrimitive(key).isNumber();
	}

	private static boolean isStringKey(JsonObject obj, String key) {
		return obj.has(key)
			&& obj.get(key).isJsonPrimitive()
			&& obj.getAsJsonPrimitive(key).isString();
	}

	private static boolean isArrayKey(JsonObject obj, String key) {
		return obj.has(key)
			&& obj.get(key).isJsonArray();
	}

	private static void lireFichierJSON(Path chemin, Jeu jeu, List<EventData> events) {
		try (FileReader reader = new FileReader(chemin.toFile())) { // try-with-ressources
			JsonObject racine = JsonParser.parseReader(reader).getAsJsonObject();
			JsonArray ensemblesEvents = racine.getAsJsonArray("ensemblesEvents");
			if (ensemblesEvents == null || ensemblesEvents.isEmpty()) return;

			String nomCarte = extraireNomCarte(chemin);
			for (JsonElement blocElem : ensemblesEvents) {
				JsonObject bloc = blocElem.getAsJsonObject();
				if (!isIntKey(bloc, "x") || !isIntKey(bloc, "y") || !isArrayKey(bloc, "events")) {
					System.err.println("[ERREUR] Bloc d'événement incomplet ignoré dans " + chemin.getFileName());
					continue;
				}
				int xCase = bloc.get("x").getAsInt();
				int yCase = bloc.get("y").getAsInt();
				JsonArray arr = bloc.getAsJsonArray("events");
				if (arr == null || arr.isEmpty()) continue;

				for (JsonElement evElem : arr) {
					JsonObject ev = evElem.getAsJsonObject();
					if (!isStringKey(ev, "type")) {
						System.err.println("[ERREUR] Événement sans type ignoré (" + chemin.getFileName() + " [" + xCase + "," + yCase + "])");
						continue;
					}

					String type = ev.get("type").getAsString();
					switch (type) {
						case "MSG" -> {
							if (!isStringKey(ev, "texte")) {
								System.err.println("[AVERTISSEMENT] MSG sans texte ignoré dans " + chemin.getFileName());
								continue;
							}
							String texte = ev.get("texte").getAsString();
							events.add(new EventData(nomCarte, xCase, yCase, new Event_MSG(texte)));
						}
						case "TP" -> {
							if (!isIntKey(ev, "xDst") || !isIntKey(ev, "yDst") || !isStringKey(ev, "carteDst")) {
								System.err.println("[AVERTISSEMENT] TP incomplet ignoré dans " + chemin.getFileName());
								continue;
							}
							int xCaseDst = ev.get("xDst").getAsInt();
							int yCaseDst = ev.get("yDst").getAsInt();
							String nomCarteDst = ev.get("carteDst").getAsString();
							events.add(new EventData(nomCarte, xCase, yCase, new Event_TP(xCaseDst, yCaseDst, jeu.getCarte(nomCarteDst))));
						}
						case "JouerMusique" -> {
							if (!isStringKey(ev, "nom")) {
								System.err.println("[AVERTISSEMENT] JouerMusique sans nom de musique ignoré dans " + chemin.getFileName());
								continue;
							}
							String nomMusique = ev.get("nom").getAsString();
							events.add(new EventData(nomCarte, xCase, yCase, new Event_JM(jeu.getMusique(nomMusique))));
						}
						case "ArretMusique" -> events.add(new EventData(nomCarte, xCase, yCase, new Event_AM()));
						case "PV" -> {
							if (!isIntKey(ev, "valeur")) {
								System.err.println("[AVERTISSEMENT] modif PV sans valeur ignoré dans " + chemin.getFileName());
								continue;
							}
							int val = ev.get("valeur").getAsInt();
							events.add(new EventData(nomCarte, xCase, yCase, new Event_ModifPV(val)));
						}
						case "PM" -> {
							if (!isIntKey(ev, "valeur")) {
								System.err.println("[AVERTISSEMENT] modif PM sans valeur ignoré dans " + chemin.getFileName());
								continue;
							}
							int val = ev.get("valeur").getAsInt();
							events.add(new EventData(nomCarte, xCase, yCase, new Event_ModifPM(val)));
						}
						case "LVLUP" -> events.add(new EventData(nomCarte, xCase, yCase, new Event_LVLUP()));
						default -> System.err.println("[AVERTISSEMENT] Type inconnu \"" + type + "\" ignoré dans " + chemin.getFileName());
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