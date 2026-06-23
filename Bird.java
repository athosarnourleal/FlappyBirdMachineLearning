package core;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Random;

public class Bird extends GAElement {

	public int timeAlive = 0;
	public boolean alive = true;
	
	public Color color;
	public static Color colors[] = {
		Color.blue,
		Color.magenta,
		Color.yellow,
		Color.cyan,
		Color.orange,
		Color.pink,
		Color.gray
	};
	
	public int drawOffset = 0,drawOffsetMax = 5;
	
	public Rectangle colision;
	
	public double dx,dy;
	
	public double vy = 0;
	public double grav = .5, jumpStrength = 10;
	
	public static int startX = Main.T*3, maxDx = Pipe.startingX - startX, maxDy = Main.H-15*Main.T;
	
	public NeuralNetwork brain;
	public final static int[] neuronLayerSizes = {4, 4, 4, 2};
	public final static String[] inputLabels = {"dx","dy","level","Yvel"};
	public final static String[] outputLabels = {"no action","jump"};
	
	public int curAction = 0;
	
	public Bird() {
		Random r = new Random();

		color = colors[r.nextInt(colors.length)];
		drawOffset = r.nextInt(drawOffsetMax*2)-drawOffsetMax;

		
		int startYvariationMax = Main.T*2;
		int startYvariation = r.nextInt(startYvariationMax*2)-startYvariationMax;
		colision = new Rectangle(startX, Main.H/2+startYvariation, Main.T*3, Main.T*3);

		brain = new NeuralNetwork(neuronLayerSizes);
		
		initializeElement(brain.getDNALength());
		
		brain.defineWeightsFromDNA(this);
		
	}

	public void evaluateElement() {
		this.eval = timeAlive;
	}


	public void jump() {
		vy = -1*jumpStrength;
	}

	
	public void reset() {
		vy = 0;
		Random r = new Random();
		
		int startYvariationMax = Main.T*2;
		int startYvariation = r.nextInt(startYvariationMax*2)-startYvariationMax;
		colision.y = Main.H/2 + startYvariation;
		
		colision.x = startX;
		
		brain.defineWeightsFromDNA(this);
		alive = true;
		timeAlive = 0;
		eval = 0;
	}
	
	public void decideAction() {
		double inputs[] = {dx, dy, Main.curLevel, vy};

		brain.runNeuralNetwork(inputs);
		
		curAction = brain.getBiggestOutput();
		
		if (curAction == 1) {
			jump();
		}
	}
	
	public void tick() {
		if (Main.curPipe.place_meeting(colision)) {
			alive = false;
		}
		
		if (alive == false) {
			if (colision.x >= 0-colision.width*2) {
				colision.x -= Pipe.vx+Main.curLevel;
			}
			return;
		}

		vy += grav;
		colision.y += vy;

		dx = Main.curPipe.middleX-colision.x;
		dy = Main.curPipe.middleY-colision.y;
		
		if(colision.y < 0) {
			colision.y = 0;
			vy = 0;
		} else if (colision.y > Main.H-colision.height) {
			colision.y = Main.H-colision.height;
			vy = 0;
		}

		decideAction();
		
		timeAlive++;
	}
	
	public void render(Graphics g) {
		if (alive == false) {
			if (colision.x < 0 - colision.width*2) {
				return;
			}
			g.setColor(Color.red);
		} else {
			g.setColor(color);
		}
		
		g.fillRect(colision.x+drawOffset, colision.y, colision.width, colision.height);
	}
	
	
}
