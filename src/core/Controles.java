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

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.EnumSet;
import java.util.Map;

/**
 * Gère l’ensemble des contrôles clavier du jeu.
 * 
 * Cette classe centralise l’état des touches pressées via un modèle de type Singleton.
 * Elle traduit les codes clavier Java (KeyEvent) en touches logiques du jeu,
 * et permet d’associer un héros ainsi qu’un buffer de message pour la saisie de texte en jeu.
 * 
 * Fournit également des méthodes utilitaires simples (HAUT(), BAS(), A(), etc.)
 * pour interroger directement l’état des commandes dans la boucle principale.
 */
public final class Controles implements KeyListener {

	public enum Touche { // Touches reconnues par le jeu
		HAUT, BAS, GAUCHE, DROITE,
		A, B, Q, S,
		ESPACE, ENTREE, ECHAP, RETOUR_ARRIERE,
		F1, F3, F5
	}

	private static Controles instance = null;
	private Heros heros;
	private StringBuilder message;
	private Object messageLock;

	private Controles() {} // Empêche toute instanciation externe

	public static Controles getInstance() {
		if (instance == null) instance = new Controles();
		return instance;
	}

	/** Permet de lier un héros et son buffer de message à ce contrôleur **/
	public void setCibles(Heros heros, StringBuilder message, Object messageLock) {
		this.heros = heros;
		this.message = message;
		this.messageLock = messageLock;
	}

	// Toutes les touches actuellement pressées
	private final EnumSet<Touche> etats = EnumSet.noneOf(Touche.class);

	// Mapping KeyCode -> Touche
	private static final Map<Integer, Touche> keyMap = Map.ofEntries(
		Map.entry(KeyEvent.VK_UP, Touche.HAUT),
		Map.entry(KeyEvent.VK_DOWN, Touche.BAS),
		Map.entry(KeyEvent.VK_LEFT, Touche.GAUCHE),
		Map.entry(KeyEvent.VK_RIGHT, Touche.DROITE),
		Map.entry(KeyEvent.VK_A, Touche.A),
		Map.entry(KeyEvent.VK_B, Touche.B),
		Map.entry(KeyEvent.VK_Q, Touche.Q),
		Map.entry(KeyEvent.VK_S, Touche.S),
		Map.entry(KeyEvent.VK_SPACE, Touche.ESPACE),
		Map.entry(KeyEvent.VK_ENTER, Touche.ENTREE),
		Map.entry(KeyEvent.VK_ESCAPE, Touche.ECHAP),
		Map.entry(KeyEvent.VK_BACK_SPACE, Touche.RETOUR_ARRIERE),
		Map.entry(KeyEvent.VK_F1, Touche.F1),
		Map.entry(KeyEvent.VK_F3, Touche.F3),
		Map.entry(KeyEvent.VK_F5, Touche.F5)
	);

	@Override
	public void keyPressed(KeyEvent e) { // Quand une touche est pressée
		Touche t = keyMap.get(e.getKeyCode());
		if (t != null) etats.add(t);
	}

	@Override
	public void keyReleased(KeyEvent e) { // Quand une touche est relachée
		Touche t = keyMap.get(e.getKeyCode());
		if (t != null) etats.remove(t);
	}

	@Override
	public void keyTyped(KeyEvent e) {
		if (heros == null || message == null) return; // sécurité
		if (heros.estEnTrainDEcrire()) {
			char inputChar = e.getKeyChar();
			if (!Character.isISOControl(inputChar) && message.length() < Config.TAILLE_MAX_MSG) {
				synchronized (messageLock) {
					message.append(inputChar);
				}
			}
		}
	}

	public boolean estAppuye(Touche t) { return etats.contains(t); } // Vérifie si une touche est enfoncée
	public void reset(Touche t) { etats.remove(t); } // Consomme une touche en la retirant de l’ensemble des touches pressées

	public boolean HAUT()                { return estAppuye(Touche.HAUT); }
	public boolean BAS()                 { return estAppuye(Touche.BAS); }
	public boolean GAUCHE()              { return estAppuye(Touche.GAUCHE); }
	public boolean DROITE()              { return estAppuye(Touche.DROITE); }
	public boolean A()                   { return estAppuye(Touche.A); }
	public boolean B()                   { return estAppuye(Touche.B); }
	public boolean Q()                   { return estAppuye(Touche.Q); }
	public boolean S()                   { return estAppuye(Touche.S); }
	public boolean ESPACE()              { return estAppuye(Touche.ESPACE); }
	public boolean ENTREE()              { return estAppuye(Touche.ENTREE); }
	public boolean ECHAP()               { return estAppuye(Touche.ECHAP); }
	public boolean RETOUR_ARRIERE()      { return estAppuye(Touche.RETOUR_ARRIERE); }
	public boolean F1()                  { return estAppuye(Touche.F1); }
	public boolean F3()                  { return estAppuye(Touche.F3); }
	public boolean F5()                  { return estAppuye(Touche.F5); }
}