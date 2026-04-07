package core;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Random;

public class Bird extends GAElement {
	
	public int timeAlive = 0;
	public boolean alive = true;
	
	public static Color colors[] = {
		Color.blue,
		Color.magenta,
		Color.yellow,
		Color.cyan,
		Color.orange,
		Color.pink,
		Color.gray
	};
	
	public Rectangle col;
	
	public double grav = .5, jumpStrength = 10;
	public double vy = 0;

	public static int startX = Game.T*3, maxDx = Pipe.startingX - startX, maxDy = Game.H-15*Game.T;
	
	public static int bitsPerValue= 8*2, numOfNeurons = 3, weightsPerNeuron = 2, biasPerNeuron = 1;
	public static int chromosomeLen = bitsPerValue*(numOfNeurons*weightsPerNeuron + numOfNeurons*biasPerNeuron);
	
	public double dx,dy;

	public double neuronMax = 10;
	
	public Neuron brain[];
	
	public Color color;
	
	public int drawOffset = 0,drawOffsetMax = 5;
	
	public Bird() {
		Random r = new Random();
		
		int variationMax = Game.T*2;
		int variation = r.nextInt(variationMax*2)-variationMax;
		
		col = new Rectangle(startX, Game.H/2+variation, Game.T*3, Game.T*3);
		
		color = colors[r.nextInt(colors.length)];
		
		brain = new Neuron[numOfNeurons];
		
		for (int i = 0; i < brain.length; i++) {
			brain[i] = new Neuron(weightsPerNeuron);
		}
		
		drawOffset = r.nextInt(drawOffsetMax*2)-drawOffsetMax;
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
		
		int variationMax = Game.T*2;
		int variation = r.nextInt(variationMax*2)-variationMax;
		col.y = Game.H/2 + variation;
		
		col.x = startX;
		
		setWeights();
		alive = true;
		timeAlive = 0;
	}
	
	public void decideAction() {
		

		
		double input[] = new double[2];
		input[0] = map(dx, 0, maxDx, 0.1, neuronMax);
		input[1] = map(dy, -maxDy, maxDy, 0, neuronMax);
		brain[0].activateGau(input);
		brain[1].activateGau(input);
		
		input[0] = brain[0].activation;
		input[1] = brain[1].activation;

		brain[2].activateStep(input, neuronMax/2);
		
		if (brain[2].activation == 1) {
			jump();
		}
	}
	
	public double map(double v0, double v0min, double v0max, double min, double max) {
		
		double value = min + v0*(max - min)/(v0max -v0min);
		
//		System.out.println("("+v0+",  "+v0min+",  "+v0max+",  "+min+",  "+max+") --> result: "+value);
		
		return value;
	}
	
	public void setWeights() {
		int neuronGeneLen = bitsPerValue * (weightsPerNeuron + biasPerNeuron);
		
		for (int i = 0; i < numOfNeurons; i++) {
			String gene = value.substring(i*neuronGeneLen, (i+1)*neuronGeneLen);
			
			brain[i].defineWeight(gene, weightsPerNeuron, bitsPerValue, -neuronMax, neuronMax);
		}	
	}
	
	public void tick() {
		if (col.intersects(Game.curPipe.rup) || col.intersects(Game.curPipe.rdown)) {
			alive = false;
		}
		
		if (alive == false) {
			if (col.x >= 0-col.width*2) {
				col.x -= Pipe.vx+Game.curLevel;
			}
			return;
		}

		vy += grav;
		col.y += vy;

		dx = Game.curPipe.middleX-col.x;
		dy = Game.curPipe.middleY-col.y;
				
		decideAction();
		
		if(col.y < 0) {
			col.y = 0;
			vy = 0;
		} else if (col.y > Game.H-col.height) {
			col.y = Game.H-col.height;
			vy = 0;
		}
		
		
		timeAlive++;
	}
	public void render(Graphics g) {
		if (alive == false) {
			if (col.x < 0 - col.width*2) {
				return;
			}
			g.setColor(Color.red);
		} else {
			g.setColor(color);
		}
//		g.drawLine(col.x+drawOffset,col.y,Game.curPipe.middleX,Game.curPipe.middleY);
		g.fillRect(col.x+drawOffset, col.y, col.width, col.height);
	}
	
	
}
