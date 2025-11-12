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

import javax.swing.JFrame;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;

/**
 * Fenêtre principale du jeu (contient le canvas de rendu et gère la configuration graphique)
 */
public class Fenetre extends JFrame {

	private int REAL_WIDTH;
	private int REAL_HEIGHT;
	private Canvas canvas;

	public Fenetre(int largeur, int hauteur, Controles controles) {
		configureFenetre(largeur, hauteur);

		EventQueue.invokeLater(() -> canvas.createBufferStrategy(3)); // important : créer le BufferStrategy *après* que la fenêtre soit visible
		addKeyListener(controles); // gestion des entrées clavier
	}

	private void configureFenetre(int largeur, int hauteur) {
		setTitle(Config.TITRE_FENETRE); // titre
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // ferme le programme lorsqu'on clique sur la croix rouge
		setResizable(false); // ne peut pas être agrandi ou réduit
		setIgnoreRepaint(true); // pas de reffraichissement automatique de la fenêtre

		canvas = new Canvas();
		canvas.setPreferredSize(new Dimension(largeur, hauteur));
		canvas.setBackground(Color.BLACK);
		canvas.setIgnoreRepaint(true);
		canvas.setFocusable(false);

		add(canvas);
		pack(); // ajuste la fenêtre selon la taille du canvas

		this.REAL_WIDTH = getWidth();
		this.REAL_HEIGHT = getHeight();

		setLocationRelativeTo(null); // centre la fenêtre
		setVisible(true); // visible (après le pack()!)
	}

	/** Getters **/
	public Canvas getCanvas() { return canvas; }
	public int getREAL_WIDTH() { return REAL_WIDTH; }
	public int getREAL_HEIGHT() { return REAL_HEIGHT; }
}
