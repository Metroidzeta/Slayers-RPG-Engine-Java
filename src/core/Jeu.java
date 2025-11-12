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

import events.*;
import ressources.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.BufferStrategy;
import java.util.Map;
import java.util.Objects;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Classe principale du moteur de jeu temps réel.
 * Charge les données, gère la boucle de rendu, les entrées, les événements et le rafraîchissement graphique.
 */
public final class Jeu {

	private static final long ATTACK_COOLDOWN_NANOS = 667_000_000L; // 2/3 secondes
	private static final int FIOLES_ANIMATION_FRAMES = 3;

	private boolean programmeActif;
	private final Fenetre fenetre;
	private final GestionnaireGraphiques dessiner;
	private final Camera camera = new Camera();
	private boolean mursVisibles, menuVisible;

	private final Heros heros;
	private final Map<String, BufferedImage> affichages = ChargerAffichages.get();
	private final Map<String, Skin> skins = ChargerSkins.get();
	private final Map<String, Font> polices = ChargerPolices.get();
	private final Map<String, Musique> musiques = ChargerMusiques.get();
	private final Map<String, Bruitage> bruitages = ChargerBruitages.get();
	private final Map<String, Chipset> chipsets = ChargerChipsets.get();
	private final Map<String, Carte> cartes = ChargerCartes.get(this);

	private final Controles controles = Controles.getInstance();
	private long frames = 0L;
	private long lastAttackCooldown = 0L;
	private double fpsResult = 0;
	private boolean refreshNextFrame;

	private int degatsAffiches = 0;
	private int fiolesTiming = 0;
	private int delaiMessage = 0;

	private final StringBuilder message = new StringBuilder(), sauvegardeMessage = new StringBuilder();
	private final Object messageLock = new Object();

	private EnsembleEvents eventsActuels = null;
	private int nbEventPass = 0;
	private Musique musiqueActuelle;

	/** Méthodes static **/
	private static String recupererNomHerosDepuisFichier(String chemin) {
		try {
			String contenu = Files.readString(Path.of(chemin)).trim();
			if (contenu.isEmpty()) throw new IllegalStateException("Fichier " + chemin + " vide.");
			return contenu;
		} catch (Exception e) {
			System.err.println("Erreur lecture du fichier " + chemin + " : " + e.getMessage() + " -> Nom par défaut : Test");
			return "Test";
		}
	}

	/** Constructeur **/
	public Jeu() {
		fenetre = new Fenetre(Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT, controles);
		ChargerEvents.inject(this);
		String nomHeros = recupererNomHerosDepuisFichier("PSEUDO.txt");
		heros = new Heros(nomHeros, getSkin("Evil.png"), HerosClasses.VOLEUR, 1, 1000, 12, 12, getCarte("Chateau_Roland_Cour_Interieure"), 10);
		controles.setCibles(heros, message, messageLock);
		camera.setCible(heros);
		camera.update();
		camera.sync();
		dessiner = new GestionnaireGraphiques(camera, heros, getPolice("FPS"), getPolice("Normal"), getAffichage("Fioles"), getAffichage("BarreXP"));

		musiqueActuelle = heros.getCarteActuelle().getMusique();
	}

	/** Getters **/
	public BufferedImage getAffichage(String nom) { return affichages.get(nom); } // Recherche en O(1)
	public Skin getSkin(String nom) { return skins.get(nom); }
	public Font getPolice(String nom) { return polices.get(nom); }
	public Musique getMusique(String nom) { return musiques.get(nom); }
	public Bruitage getBruitage(String nom) { return bruitages.get(nom); }
	public Chipset getChipset(String nom) { return chipsets.get(nom); }
	public Carte getCarte(String nom) { return cartes.get(nom); }

	/** Autres méthodes **/
	private void libererRessourcesAudio() {
		if (musiqueActuelle != null) musiqueActuelle.stop();
		bruitages.values().forEach(Bruitage::close);
		Bruitage.shutdown();
	}

	private void afficherFPS_Fenetre() {
		fenetre.setTitle(String.format("%s | FPS : %.2f", Config.TITRE_FENETRE, fpsResult));
	}

	private void viderMessage() {
		synchronized(messageLock) {
			message.setLength(0);
		}
	}

	private void sauvegarderMessage() {
		synchronized(messageLock) {
			sauvegardeMessage.setLength(0);
			sauvegardeMessage.append(message);
		}
	}

	private void remettreDernierMessage() {
		synchronized(messageLock) {
			message.append(sauvegardeMessage);
		}
	}

	private void executeEvent_MSG() {
		heros.setEstEnTrainDEcrire(false);
		viderMessage();
	}

	private void changerMusique(Musique musique) {
		Util.playAndStopMusique(musiqueActuelle, musique);
		musiqueActuelle = musique;
	}

	private void executeEvent_TP(Event_TP ev_tp) {
		degatsAffiches = 0;
		final Carte carteDst = ev_tp.getCarteDst();
		final Musique musique = carteDst.getMusique();
		if (!Objects.equals(musiqueActuelle, musique)) changerMusique(musique);
		heros.setCarteActuelle(carteDst);
		heros.modifierPosition(ev_tp.getXDst(), ev_tp.getYDst());
		System.out.println("Teleportation de " + heros.getNom() + " vers " + carteDst.getNom() + heros.getPosition());

		camera.setCible(heros);
		camera.update(); // recalculer immédiatement l'offset caméra
		camera.sync(); // synchroniser la caméra pour empêcher un glissement

		// forcer rendu complet immédiat (nouvelle carte)
		BufferStrategy bs = fenetre.getCanvas().getBufferStrategy();
		if (bs != null) {
			Graphics g = bs.getDrawGraphics();
			try {
				dessiner.fondNoir(g);
				camera.interpolate(1.0); // interpolation complète
				render(g); // afficher directement la nouvelle carte
				refreshNextFrame = true;
			} finally {
				g.dispose();
				bs.show();
				Toolkit.getDefaultToolkit().sync();
			}
		}
	}

	private void executeEvent_JM(Event_JM ev_jm) {
		final Musique musique = ev_jm.getMusique();
		if (!Objects.equals(musiqueActuelle, musique)) changerMusique(musique);
	}

	private void executeEvent_AM() {
		if (musiqueActuelle != null) {
			musiqueActuelle.stop();
			musiqueActuelle = null;
		}
	}

	private void executeEvent_MPV(Event_ModifPV e_mpv) { heros.modifierPV(e_mpv.getPV()); }
	private void executeEvent_MPM(Event_ModifPM e_mpm) { heros.modifierPM(e_mpm.getPM()); }
	private void executeEvent_LVLUP() { heros.levelUP(); }

	private void executeEvent(Event ev) {
		Objects.requireNonNull(ev, "L'event à exécuter est null");
		heros.setEstBloque(true);
		heros.setEstDansUnEvent(true);

		switch (ev) {
			case Event_MSG e     -> executeEvent_MSG();   // message du jeu
			case Event_TP e      -> executeEvent_TP(e);   // téléportation
			case Event_JM e      -> executeEvent_JM(e);   // lecture d'une musique
			case Event_AM e      -> executeEvent_AM();    // arrêt de la musique
			case Event_ModifPV e -> executeEvent_MPV(e);  // modifier PV du héros
			case Event_ModifPM e -> executeEvent_MPM(e);  // modifier PM du héros
			case Event_LVLUP e   -> executeEvent_LVLUP(); // héros passe au niveau supérieur
			default -> throw new IllegalArgumentException("Type d'événement inconnu : " + ev.getClass());
		}

		if (!(ev instanceof Event_MSG)) {
			nbEventPass++;
			heros.setEstDansUnEvent(false);
		}
	}

	private void updateCooldowns(long nowNanos) {
		if (!heros.getPeutAttaquer() && (nowNanos - lastAttackCooldown) > ATTACK_COOLDOWN_NANOS) heros.setPeutAttaquer(true);
	}

	private void updateUPS(long tempsDebutFrame) {
		boolean flechesAppuye = (controles.HAUT() || controles.BAS() || controles.GAUCHE() || controles.DROITE());
		if (flechesAppuye && !heros.estBloque()) {
			if (controles.HAUT() && !controles.BAS()) heros.deplacer(Directions.HAUT);
			if (controles.BAS() && !controles.HAUT()) heros.deplacer(Directions.BAS);
			if (controles.GAUCHE() && !controles.DROITE()) heros.deplacer(Directions.GAUCHE);
			if (controles.DROITE() && !controles.GAUCHE()) heros.deplacer(Directions.DROITE);
		} else {
			heros.setFrameDeplacement(7);
		}
		camera.update();
		if (flechesAppuye && !heros.estBloque() && eventsActuels == null) {
			eventsActuels = heros.getCarteActuelle().detecterCollisionsEvents(heros.getHitBox());
		}
		if (eventsActuels != null && !heros.estDansUnEvent()) {
			Event e = eventsActuels.getEventIfExists(0, nbEventPass);
			executeEvent(e);
		}

		if (!heros.estEnTrainDEcrire()) {
			if (controles.A()) {
				mursVisibles = !mursVisibles;
				System.out.println((mursVisibles ? "Activation" : "Désactivation") + " de l'affichage des murs!");
				controles.reset(Controles.Touche.A);
			}

			if (controles.Q()) {
				programmeActif = false;
				controles.reset(Controles.Touche.Q);
			}

			if (!heros.estBloque()) {
				if (controles.S()) {
					if (heros.getPeutAttaquer()) {
						getBruitage("Blow1.wav").play();
						heros.updateHitBoxEpee();
						/* if(heros.getHitBoxEpee().intersects(blob_hitbox)) {
							resultatAleatoire = (double) rand() / RAND_MAX;
							//System.out.println("resultatAleatoire = " + resultatAleatoire + " > tauxCrit = " + heros.getTauxCrit());
							if(resultatAleatoire > heros.getTauxCrit()) {
								System.out.println("Coup normal sur le monstre");
								bruitages.get(1).play();
							} else {
								System.out.println("Coup critique! sur le monstre");
								bruitages.get(2).play();
							}
							degatsAffiches = 1;
						} */
						heros.setAttaqueEpee(true);
						heros.setPeutAttaquer(false);
						lastAttackCooldown = tempsDebutFrame;
					}
					controles.reset(Controles.Touche.S);
				}
			}
		}

		if (controles.RETOUR_ARRIERE()) {
			synchronized(messageLock) {
				if (heros.estEnTrainDEcrire() && message.length() > 0) message.deleteCharAt(message.length() - 1);
			}
			controles.reset(Controles.Touche.RETOUR_ARRIERE);
		}

		if (controles.ENTREE()) {
			if (!heros.estBloque()) {
				boolean ecritureActive = heros.estEnTrainDEcrire();
				heros.setEstEnTrainDEcrire(!ecritureActive);	
				if (ecritureActive && message.length() > 0) {
					delaiMessage = 0;
					heros.setMessageTete(true);
					System.out.println(heros.getNom() + " : " + message);
					//ajouterMessageHistorique();
					sauvegarderMessage();
					viderMessage();
				}
			}
			controles.reset(Controles.Touche.ENTREE);
		}

		if (controles.ESPACE()) {
			if (heros.estDansUnEvent() && eventsActuels.getPageEvents().get(0).get(nbEventPass) instanceof Event_MSG) {
				nbEventPass++;
				heros.setEstDansUnEvent(false);
			}
			controles.reset(Controles.Touche.ESPACE);
		}

		if (controles.ECHAP()) {
			if (eventsActuels == null) {
				//afficherRecap = 0;
				menuVisible = !menuVisible;
				if (menuVisible) {
					heros.setEstEnTrainDEcrire(false);
					viderMessage();
					heros.setFrameDeplacement(7);	
				}
				heros.setEstBloque(!(heros.estBloque()));
				controles.reset(Controles.Touche.ECHAP);
			}
		}

		if (!heros.estBloque()) {
			if (controles.F1()) {
				controles.reset(Controles.Touche.F1);
			}
			if (controles.F3()) {
				heros.setEstEnTrainDEcrire(true);
				viderMessage();
				remettreDernierMessage();
				controles.reset(Controles.Touche.F3);
			}
		}

		if (controles.F5()) {
			dessiner.incrementIndexCouleurCadres();
			controles.reset(Controles.Touche.F5);
		}

		if (eventsActuels != null && nbEventPass == eventsActuels.getPageEvents().get(0).size()) {
			nbEventPass = 0;
			eventsActuels = null;
			heros.setEstBloque(false);
		}
	}

	private void render(Graphics g) {
		dessiner.fondNoir(g);
		dessiner.couche(g, 0); // couche 0 chipset
		dessiner.couche(g, 1); // couche 1 chipset
		if (mursVisibles) dessiner.murs(g); // affiche les murs
		dessiner.heros(g);
		dessiner.couche(g, 2); // couche 2 chipset
		if (heros.getAttaqueEpee()) { // Si le héros est en train d'attaquer
			dessiner.hitBoxEpeeHeros(g);
			heros.setAttaqueEpee(false);
		}

		dessiner.fiolePV(g, fiolesTiming);
		dessiner.fiolePM(g, fiolesTiming);
		dessiner.barreXP(g);

		if (heros.estEnTrainDEcrire()) dessiner.cadreEcriture(g, message.toString());
		if (heros.getMessageTete()) dessiner.messageTeteHeros(g, sauvegardeMessage.toString());
		if (heros.estDansUnEvent()) {
			Event ev = eventsActuels.getEventIfExists(0, nbEventPass);
			if (ev instanceof Event_MSG ev_msg) dessiner.messageEvent(g, ev_msg.getMessage());
		}

		if (menuVisible) {
			dessiner.menuNavigation(g); // affiche menu de navigation
			dessiner.menuStatistiques(g); // affiche sous-menu: statistiques
		}
		dessiner.alignement(g);
		dessiner.FPS(g, fpsResult);
		if(refreshNextFrame) {
			dessiner.computePalette(g);
			refreshNextFrame = false;
		}
	}

	private void updateFPS() {
		BufferStrategy bs = fenetre.getCanvas().getBufferStrategy();
		if (bs != null) {
			Graphics g = bs.getDrawGraphics();
			try {
				g.clearRect(0, 0, Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT); // efface l'écran
				render(g); // dessiner nouvelle frame
			} finally {
				g.dispose(); // vider les ressources
				Toolkit.getDefaultToolkit().sync();
				bs.show(); // afficher le rendu de la frame
			}
		}
		frames++;
	}

	/** Boucles de jeu **/
	public void jouer() {
		// --- Constantes temporelles ---
		final long NANOS_PER_RENDER     = 1_000_000_000L / Config.FPS;
		final long NANOS_PER_TICK       = 1_000_000_000L / Config.UPS;
		final long NANOS_588MS          = 588_000_000L;
		final long NANOS_1SEC           = 1_000_000_000L;  // 1 seconde
		final long NANOS_1MIN           = 60_000_000_000L; // 1 minute
		final long MAX_FRAME_SKIP_NANOS = 5_000_000_000L;  // 5 secondes
		final int NO_DELAYS_PER_YIELD = 16;

		// --- Variables de synchronisation ---
		long nowNanos   = System.nanoTime();
		long nextRender = nowNanos;
		long nextTick   = nowNanos;

		// --- Timers secondaires ---
		long lastFiolesTime = nowNanos, lastMinute = nowNanos, lastSecond = nowNanos;
		long overSleepNanos = 0L;
		int noDelays = 0;

		// --- Initialisation ---
		long lastFrameCount = 0L;
		if (musiqueActuelle != null) musiqueActuelle.play();
		programmeActif = true;

		// --- Boucle principale ---
		while (programmeActif) {
			nowNanos = System.nanoTime();

			// --- Détection d'une longue pause (veille, freeze, etc.) ---
			if (nowNanos - nextTick > MAX_FRAME_SKIP_NANOS) {
				nextTick = nowNanos + NANOS_PER_TICK;
				nextRender = nowNanos + NANOS_PER_RENDER;
				overSleepNanos = 0L;
				noDelays = 0;
			}

			// --- Logique globale ---
			updateCooldowns(nowNanos);

			// --- Animation fioles ---
			if (nowNanos - lastFiolesTime >= NANOS_588MS) {
				fiolesTiming = (fiolesTiming + 1) % FIOLES_ANIMATION_FRAMES;
				lastFiolesTime += NANOS_588MS;
			}

			// --- Chaque seconde ---
			if (nowNanos - lastSecond >= NANOS_1SEC) {
				if (heros.getMessageTete() && ++delaiMessage == 6) { // Si il y a déjà un message sur la tête du héros et que ça se termine
					delaiMessage = 0;
					heros.setMessageTete(false);
				}

				long framesEcoulees = frames - lastFrameCount;
				double elapsedSec = (nowNanos - lastSecond) / 1e9;
				fpsResult = framesEcoulees / elapsedSec;
				afficherFPS_Fenetre();
				if (Config.DEBUG_MODE) System.out.println("FPS = " + fpsResult);
				lastFrameCount = frames;
				lastSecond += NANOS_1SEC;
			}

			// --- Chaque minute ---
			if (nowNanos - lastMinute >= NANOS_1MIN) {
				heros.modifierAlignement(1);
				lastMinute += NANOS_1MIN;
			}

			// --- Updates logiques (UPS) ---
			while (nowNanos >= nextTick) {
				updateUPS(nowNanos);
				nextTick += NANOS_PER_TICK;
				nowNanos = System.nanoTime(); // anti dérive
			}

			// --- Rendu graphique (FPS) ---
			if (nowNanos >= nextRender) {
				// calcul du facteur d'interpolation entre deux updates logiques
				double interpolation = (double)(nowNanos - (nextTick - NANOS_PER_TICK)) / NANOS_PER_TICK;
				interpolation = Math.max(0.0, Math.min(1.0, interpolation)); // intervalle [0..1]

				camera.interpolate(interpolation);
				updateFPS();
				nextRender += NANOS_PER_RENDER;
			}

			// --- Gestion du CPU ---
			final long nextAction = Math.min(nextTick, nextRender);
			long sleepNanos = Math.max(0L, nextAction - System.nanoTime() - overSleepNanos);

			if (sleepNanos > 0) {
				long before = System.nanoTime();
				try { Thread.sleep(sleepNanos / 1_000_000L, (int) (sleepNanos % 1_000_000L)); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
				long after = System.nanoTime();
				overSleepNanos = (after - before) - sleepNanos;
				noDelays = 0;
			} else {
				overSleepNanos = 0L;
				if (++noDelays >= NO_DELAYS_PER_YIELD) {
					Thread.yield();
					noDelays = 0;
				}
			}
		}
		libererRessourcesAudio();
		System.exit(0);
	}

	public void jouer2() {
		// --- Constantes temporelles ---
		final double MILLIS_PER_RENDER = 1e3 / Config.FPS;
		final double MILLIS_PER_TICK = 1e3 / Config.UPS;
		final long MS_588MS = 588L;
		final long MS_1SEC  = 1_000L;  // 1 seconde
		final long MS_1MIN  = 60_000L; // 1 minute

		// --- Variables de synchronisation ---
		long now = System.currentTimeMillis();
		double nextRender = (double)now;
		double nextTick = (double)now;

		// --- Timers secondaires ---
		long lastFiolesTime = now, lastMinute = now, lastSeconde = now;
		long overSleepMillis = 0L;

		// --- Initialisation ---
		long lastFrameCount = 0L;
		if (musiqueActuelle != null) musiqueActuelle.play();
		programmeActif = true;

		// --- Boucle principale ---
		while (programmeActif) {
			now = System.currentTimeMillis();

			// --- Logique globale ---
			updateCooldowns(now * 1_000_000L);

			// --- Animations fioles ---
			if (now - lastFiolesTime >= MS_588MS) {
				fiolesTiming = (fiolesTiming + 1) % FIOLES_ANIMATION_FRAMES;
				lastFiolesTime += MS_588MS;
			}

			// --- Chaque Seconde ---
			if (now - lastSeconde >= MS_1SEC) {
				if (heros.getMessageTete() && ++delaiMessage == 6) { // Si il y a déjà un message sur la tête du héros et que ça se termine
					delaiMessage = 0;
					heros.setMessageTete(false);
				}
				long framesEcoulees = frames - lastFrameCount;
				double elapsedSec = (now - lastSeconde) / 1e3;
				fpsResult = framesEcoulees / elapsedSec;
				if (Config.DEBUG_MODE) System.out.println("FPS = " + fpsResult);
				afficherFPS_Fenetre();
				lastFrameCount = frames;
				lastSeconde += MS_1SEC;
			}

			// --- Chaque Minute ---
			if (now - lastMinute >= MS_1MIN) {
				heros.modifierAlignement(1);
				lastMinute += MS_1MIN;
			}

			// --- Updates logiques (UPS) ---
			while (now >= nextTick) {
				updateUPS(now * 1_000_000L);
				nextTick += MILLIS_PER_TICK;
			}

			// ---- Rendu Graphique (FPS) ----
			while (now >= nextRender) {
				double interpolation = (double)(now - (nextTick - MILLIS_PER_TICK)) / MILLIS_PER_TICK;
				interpolation = Math.max(0.0, Math.min(1.0, interpolation)); // intervalle [0..1]

				camera.interpolate(interpolation);
				updateFPS();
				nextRender += MILLIS_PER_RENDER;
			}

			// --- Gestion du CPU ---
			final long nextAction = (long)Math.min(nextTick, nextRender);
			long sleepTime =  Math.max(0L, nextAction - System.currentTimeMillis() - overSleepMillis);

			if (sleepTime > 0L) {
				long before = System.currentTimeMillis();
				try { Thread.sleep(sleepTime); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
				long after = System.currentTimeMillis();
				overSleepMillis = (after - before) - sleepTime;
			} else {
				overSleepMillis = 0L;
			}
		}
		libererRessourcesAudio();
		System.exit(0);
	}
}