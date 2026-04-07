package core;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Random;

public class Pipe {
	
	public static int startingX = 50*Game.T;
	public static int vx = 6, opening = 6*Game.T, min = opening, max = Game.H-opening, w = 6*Game.T;
	public Rectangle rup, rdown;
	public int middleY,middleX;
	
	public Pipe() {
		Random r = new Random();
		
		middleY = min + r.nextInt(max-min);
		initRect(middleY);
	}
	
	public void initRect(int y) {
		rup = new Rectangle(startingX, 0, w, middleY-opening);
		rdown = new Rectangle(startingX, middleY+opening, w, Game.H);
	}
	
	public void tick() {
		rup.x -= vx+Game.curLevel;
		rdown.x -= vx+Game.curLevel;
		
		middleX = rup.x+w/2;
	}
	
	public void render(Graphics g) {
		g.setColor(Color.green);
		g.fillRect(rup.x, rup.y, rup.width, rup.height);
		g.fillRect(rdown.x, rdown.y, rdown.width, rdown.height);
	}
	
}
