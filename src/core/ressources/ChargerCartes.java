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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import core.Carte;
import core.Chipset;
import core.Jeu;
import core.Musique;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

/**
 * Charge automatiquement toutes les cartes (données de base + couches + murs) depuis le dossier "cartes" et renvoie une map complète (nom -> carte)
 */
public final class ChargerCartes {

	private ChargerCartes() { throw new AssertionError("La classe ChargerCartes ne doit pas être instanciée."); } // Empêche toute instanciation

	/** Structure de données pour une carte **/
	private record CarteData(String nomCarte, int largeur, int hauteur, String nomChipset, String nomMusique, int[][] c0, int[][] c1, int[][] c2, boolean[][] murs) {}

	private static final String DOSSIER = "cartes";
	private static final Gson gson = new Gson();

	/** Méthodes static **/
	private static List<CarteData> getCartes() {
		final List<CarteData> cartes = new ArrayList<>();
		try {
			Path dossierCartes = Path.of(DOSSIER);
			if (!Files.exists(dossierCartes)) return List.of();

			try (DirectoryStream<Path> fichiers = Files.newDirectoryStream(dossierCartes, "*_BC.json")) {
				for (Path fichier : fichiers) lireFichierJSON(fichier, cartes);
			}
		} catch (IOException e) { throw new IllegalArgumentException("[ERREUR] Lecture des fichiers de cartes : " + e.getMessage(), e); }

		return cartes;
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

	private static boolean[][] convertirMurs(int[][] source, int hauteur, int largeur) {
		boolean[][] dst = new boolean[hauteur][largeur];
		if (source == null) return dst;

		int h = Math.min(hauteur, source.length);
		for (int i = 0; i < h; i++) {
			int w = Math.min(largeur, source[i].length);
			for (int j = 0; j < w; j++) {
				dst[i][j] = (source[i][j] == 1);
			}
		}
		return dst;
	}

	private static boolean[][] lireMurs(String nomCarte, int hauteur, int largeur) {
		Path fichierME = Path.of(DOSSIER, nomCarte + "_ME.json");
		if (!Files.exists(fichierME)) return new boolean[hauteur][largeur];

		try (FileReader reader = new FileReader(fichierME.toFile())) { // try-with-ressources
			JsonObject racine = JsonParser.parseReader(reader).getAsJsonObject();
			final int[][] mursInt = verifierMatrice(gson.fromJson(racine.get("murs"), int[][].class), hauteur, largeur, nomCarte, "murs");
			return convertirMurs(mursInt, hauteur, largeur);
		} catch (Exception e) {
			System.err.println("[ERREUR] Lecture murs de " + nomCarte + " : " + e.getMessage());
			return new boolean[hauteur][largeur];
		}
	}

	private static String extraireNomCarte(Path chemin, String suffixe) {
		String nomFichier = chemin.getFileName().toString();
		return nomFichier.endsWith(suffixe) ? nomFichier.substring(0, nomFichier.length() - suffixe.length()) : nomFichier;
	}

	private static int[][] verifierMatrice(int[][] matrice, int hauteur, int largeur, String nomCarte, String nomCouche) {
		Objects.requireNonNull(matrice, "[ERREUR] " + nomCarte + " : " + nomCouche + " manquante ou invalide");

		if (matrice.length != hauteur) { 
			throw new IllegalArgumentException("[ERREUR] " + nomCarte + " : hauteur de " + nomCouche + " incorrecte (" + matrice.length + " au lieu de " + hauteur + ")");
		}

		for (int i = 0; i < hauteur; i++) {
			if (matrice[i] == null || matrice[i].length != largeur) {
				throw new IllegalArgumentException("[ERREUR] " + nomCarte + " : largeur de " + nomCouche +
						" incorrecte à la ligne " + i + " (" + (matrice[i] == null ? 0 : matrice[i].length) +
						" au lieu de " + largeur + ")");
			}
		}
		return matrice;
	}

	private static void lireFichierJSON(Path chemin, List<CarteData> cartes) {
		String nomCarte = extraireNomCarte(chemin, "_BC.json");

		try (FileReader reader = new FileReader(chemin.toFile())) { // try-with-ressources
			JsonObject racine = JsonParser.parseReader(reader).getAsJsonObject();

			if (racine == null || !isIntKey(racine, "largeur") || !isIntKey(racine, "hauteur") || !isStringKey(racine, "chipset") || !isStringKey(racine, "musique")) {
				throw new IllegalArgumentException("[ERREUR] Fichier " + nomCarte + " incomplet ou invalide (champs manquants).");
			}

			final int largeur = racine.get("largeur").getAsInt();
			final int hauteur = racine.get("hauteur").getAsInt();
			final String nomChipset = racine.get("chipset").getAsString();
			final String nomMusique = racine.get("musique").getAsString();

			final int[][] c0 = verifierMatrice(gson.fromJson(racine.get("couche0"), int[][].class), hauteur, largeur, nomCarte, "couche0");
			final int[][] c1 = verifierMatrice(gson.fromJson(racine.get("couche1"), int[][].class), hauteur, largeur, nomCarte, "couche1");
			final int[][] c2 = verifierMatrice(gson.fromJson(racine.get("couche2"), int[][].class), hauteur, largeur, nomCarte, "couche2");
			final boolean[][] murs = lireMurs(nomCarte, hauteur, largeur);

			cartes.add(new CarteData(nomCarte, largeur, hauteur, nomChipset, nomMusique, c0, c1, c2, murs));

		} catch (Exception e) {
			System.err.println("[ERREUR] Fichier " + chemin.getFileName() + " : " + e.getMessage());
		}
	}

	private static void ajouterCarte(CarteData elem, Map<String, Carte> cartes, Jeu jeu) {
		final Chipset chipset = jeu.getChipset(elem.nomChipset);
		if (chipset == null) throw new IllegalArgumentException("[ERREUR] Chipset \"" + elem.nomChipset + "\" introuvable pour la carte \"" + elem.nomCarte + "\"");

		final Musique musique = jeu.getMusique(elem.nomMusique);
		if (musique == null) throw new IllegalArgumentException("[ERREUR] Musique \"" + elem.nomMusique + "\" introuvable pour la carte \"" + elem.nomCarte + "\"");

		Carte carte = new Carte(elem.nomCarte, elem.largeur, elem.hauteur, chipset, musique, elem.c0, elem.c1, elem.c2, elem.murs);
		cartes.put(elem.nomCarte, carte);
	}

	/** Getters **/
	public static Map<String, Carte> get(Jeu jeu) {
		Objects.requireNonNull(jeu, "Le jeu ne peut pas être null");
		final List<CarteData> dataCartes = getCartes();
		final Map<String, Carte> cartes = new HashMap<>(dataCartes.size());
		dataCartes.forEach(elem -> ajouterCarte(elem, cartes, jeu));
		return Map.copyOf(cartes);
	}
}