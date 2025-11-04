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

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.LineUnavailableException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Gère la lecture, la pause et l'arrêt d'une musique de fond au format audio ogg.
 * L'implémentation est thread-safe et optimise l'utilisation de la mémoire et du CPU
 * pour les musiques jouées. Une seule musique peut être active à la fois.
 */

public final class Musique implements AutoCloseable {

	private static final String DOSSIER = "musiques";
	private static final int BUFFER_SIZE = 16384;
	private static final int STOP_TIMEOUT_MS = 2000;

	private static Musique musiqueActive = null;
	private static boolean vorbisInitialise = false;
	private static final Object INIT_LOCK = new Object();

	private final String nom;
	private final File fichier;
	private final AudioFormat decodedFormat;

	private Thread threadLecture;
	private final AtomicBoolean enLecture = new AtomicBoolean(false);
	private volatile boolean loop;

	private final Lock pauseLock = new ReentrantLock();
	private final Condition pauseCondition = pauseLock.newCondition();
	private final AtomicBoolean paused = new AtomicBoolean(false);

	private SourceDataLine ligneAudio;

	private static void initVorbisSPI() {
		if (!vorbisInitialise) {
			synchronized (INIT_LOCK) {
				if (!vorbisInitialise) {
					vorbisInitialise = true;
					Runtime.getRuntime().addShutdownHook(new Thread(Musique::shutdown, "Musique-Shutdown")); // Fermeture automatique à l’arrêt du programme
				}
			}
		}
	}

	/** Constructeur **/
	public Musique(String nomFichier) {
		if (nomFichier == null || nomFichier.isBlank()) throw new IllegalArgumentException("Nom du fichier audio null ou vide");

		nom = nomFichier;
		fichier = new File(DOSSIER, nomFichier);
		if (!fichier.exists()) throw new IllegalStateException("Fichier introuvable : " + fichier.getAbsolutePath());

		initVorbisSPI();
		decodedFormat = initDecodedFormat();
	}

	/** Getters **/
	public String getNom() { return nom; }

	private AudioFormat initDecodedFormat() {
		try (AudioInputStream ais = AudioSystem.getAudioInputStream(fichier)) { // try-with-ressources
			AudioFormat baseFormat = ais.getFormat();
			return new AudioFormat(
					AudioFormat.Encoding.PCM_SIGNED,
					baseFormat.getSampleRate(),
					16,
					baseFormat.getChannels(),
					baseFormat.getChannels() * 2,
					baseFormat.getSampleRate(),
					false
			);
		}
		catch (UnsupportedAudioFileException e) { throw new RuntimeException("Format audio non supporté : " + nom, e); }
		catch (IOException e) { throw new RuntimeException("Erreur lecture fichier : " + nom, e); }
	}

	private synchronized void openAudioLine() {
		if (ligneAudio != null && ligneAudio.isOpen()) return;

		try {
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, decodedFormat);
			ligneAudio = (SourceDataLine) AudioSystem.getLine(info);
			ligneAudio.open(decodedFormat);
		} catch (LineUnavailableException e) {
			throw new RuntimeException("Ligne audio indisponible : " + nom, e);
		}
	}

	private synchronized void closeAudioLine() {
		if (ligneAudio != null && ligneAudio.isOpen()) {
			ligneAudio.drain();
			ligneAudio.close();
			ligneAudio = null;
		}
	}

	public void play() { play(true); }

	public synchronized void play(boolean loop) {
		if (enLecture.get()) return;
		this.loop = loop;

		stopMusiqueActive();
		musiqueActive = this;

		openAudioLine();

		enLecture.set(true);
		paused.set(false);

		if (ligneAudio != null) ligneAudio.start();

		threadLecture = new Thread(this::lecture, "Musique-" + nom);
		threadLecture.setDaemon(true);
		threadLecture.start();
	}

	private void stopMusiqueActive() {
		if (musiqueActive != null && musiqueActive != this) musiqueActive.stop();
	}

	private void lecture() {
		try {
			do { if (!lireUneFois()) break; }
			while (loop && enLecture.get());
		} finally {
			stopLigneAudio();
			enLecture.set(false);
		}
	}

	private boolean lireUneFois() {
		try (AudioInputStream ais = AudioSystem.getAudioInputStream(fichier);
			 AudioInputStream din = AudioSystem.getAudioInputStream(decodedFormat, ais)) { // try-with-ressources

			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead;

			while ((bytesRead = din.read(buffer)) != -1 && enLecture.get()) {
				if (!attendreFinPause()) return false;
				if (!enLecture.get()) return false;
				if (ligneAudio != null) ligneAudio.write(buffer, 0, bytesRead);
			}
			if (ligneAudio != null) ligneAudio.drain();
			return true;

		} catch (UnsupportedAudioFileException | IOException e) {
			System.err.println("Erreur durant lecture de " + nom + " : " + e.getMessage());
			return false;
		}
	}

	private boolean attendreFinPause() {
		pauseLock.lock();
		try {
			while (paused.get() && enLecture.get()) pauseCondition.await();
			return enLecture.get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return false;
		} finally {
			pauseLock.unlock();
		}
	}

	private void stopLigneAudio() {
		if (ligneAudio != null) {
			ligneAudio.stop();
			ligneAudio.flush();
		}
	}

	public synchronized void stop() {
		if (!enLecture.get()) return;

		enLecture.set(false);
		paused.set(false);

		pauseLock.lock();
		try { pauseCondition.signalAll(); }
		finally { pauseLock.unlock(); }

		stopLigneAudio();
		attendreFinThread();
		closeAudioLine();
	}

	private void attendreFinThread() {
		if (threadLecture != null && threadLecture.isAlive()) {
			threadLecture.interrupt();
			try { threadLecture.join(STOP_TIMEOUT_MS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
		}
	}

	public void pause() {
		pauseLock.lock();
		try { paused.set(true); }
		finally { pauseLock.unlock(); }
	}

	public void resume() {
		pauseLock.lock();
		try {
			if (paused.get()) {
				paused.set(false);
				pauseCondition.signalAll();
			}
		} finally { 
			pauseLock.unlock(); 
		}
	}

	public boolean isRunning() { return enLecture.get() && !paused.get(); }
	public boolean isPaused() { return paused.get(); }
	public boolean isPlaying() { return enLecture.get(); }

	@Override
	public synchronized void close() {
		stop();
		if (musiqueActive == this) musiqueActive = null;
		closeAudioLine();
	}

	public static synchronized void shutdown() {
		if (musiqueActive != null) {
			musiqueActive.close();
			musiqueActive = null;
		}
		synchronized (INIT_LOCK) {
			vorbisInitialise = false;
		}
	}
}