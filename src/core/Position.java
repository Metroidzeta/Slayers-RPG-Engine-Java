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

import java.util.Objects;
import java.awt.Rectangle;

public final record Position(int x, int y) {
	public Position deplacer(int dx, int dy) {
		return new Position(x + dx, y + dy);
	}

	public Rectangle getHitbox(int taille) {
		return new Rectangle(x, y, taille, taille);
	}

	public String toStringCases() {
		return "(" + (x / Config.TAILLE_CASES) + "," + (y / Config.TAILLE_CASES) + ")";
	}

	@Override
	public String toString() { return "(" + x + "," + y + ")"; }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Position position)) return false;

		return x == position.x && y == position.y;
	}

	@Override
	public int hashCode() { return Objects.hash(x, y); }
}