package core;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Random;

public class Pipe {
	
	public static int startingX = 50*Main.T;
	public static int vx = 6, opening = (int)(5.5*Main.T), min = opening, max = Main.H-opening, w = 6*Main.T;
	public Rectangle rup, rdown;
	public int middleY,middleX;
	
	public Pipe() {
		Random r = new Random();
		
		middleY = min + r.nextInt(max-min);
		initRect(middleY);
	}
	
	public boolean place_meeting(Rectangle r) {
		return (r.intersects(rup) || r.intersects(rdown));
	}
	
	public void initRect(int y) {
		int penalty = 0;
		if (Main.curLevel == Main.maxLevel) {
			penalty = 5;
		}
		rup = new Rectangle(startingX, 0, w, middleY-(opening-penalty));
		rdown = new Rectangle(startingX, middleY+(opening-penalty), w, Main.H);
	}
	
	public void tick() {
		rup.x -= vx+Main.curLevel;
		rdown.x -= vx+Main.curLevel;
		
		middleX = rup.x+w/2;
	}
	
	public void render(Graphics g) {
		g.setColor(Main.levelColors[Main.curLevel]);
		g.fillRect(rup.x, rup.y, rup.width, rup.height);
		g.fillRect(rdown.x, rdown.y, rdown.width, rdown.height);
	}
	
}
