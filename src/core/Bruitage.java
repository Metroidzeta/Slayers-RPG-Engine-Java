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

import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.LineUnavailableException;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Gère la lecture de bruitages courts (.wav), préchargés en mémoire.
 * Lecture non bloquante via un pool de threads.
 * Idéal pour les sons d'attaque, d’impact, de menu, etc.
 */
public final class Bruitage implements AutoCloseable {

	private static final String DOSSIER = "bruitages";
	private static final ExecutorService EXECUTEUR = Executors.newCachedThreadPool(r -> {
		Thread t = new Thread(r, "Bruitage-Thread");
		t.setDaemon(true);
		return t;
	});

	static {
		Runtime.getRuntime().addShutdownHook(new Thread(Bruitage::shutdown, "Bruitage-Shutdown")); // Fermeture automatique à l’arrêt du programme
	}

	private final String nom;
	private final Clip clip;

	/** Constructeur **/
	public Bruitage(String nomFichier) {
		if (nomFichier == null || nomFichier.isBlank()) throw new IllegalArgumentException("Nom du bruitage null ou vide");

		this.nom = nomFichier;
		File fichier = new File(DOSSIER, nomFichier);
		if (!fichier.exists()) throw new IllegalStateException("Fichier introuvable : " + fichier.getAbsolutePath());

		try (AudioInputStream ais = AudioSystem.getAudioInputStream(fichier)) {
			clip = AudioSystem.getClip();
			clip.open(ais); // charge tout en mémoire
		}
		catch (UnsupportedAudioFileException e) { throw new RuntimeException("Format audio non supporté pour " + nom, e); }
		catch (LineUnavailableException e) { throw new RuntimeException("Ligne audio indisponible pour " + nom, e); }
		catch (IOException e) { throw new RuntimeException("Erreur lecture fichier : " + nom, e); }
	}

	public String getNom() { return nom; }
	public void play() { play(false); }

	public void play(boolean loop) {
		EXECUTEUR.execute(() -> {
			synchronized (clip) {
				if (!clip.isOpen()) return; // sécurité
				if (clip.isRunning()) clip.stop();
				clip.setFramePosition(0);
				clip.loop(loop ? Clip.LOOP_CONTINUOUSLY : 0);
			}
		});
	}

	public void stop() { synchronized (clip) { if (clip.isRunning()) clip.stop(); } }

	@Override public void close() { synchronized (clip) { if (clip.isOpen()) clip.close(); } }
	public static void shutdown() { EXECUTEUR.shutdownNow(); }
}