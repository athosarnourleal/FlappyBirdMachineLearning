package core;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

public class Game extends Canvas implements Runnable,KeyListener {
	
	private static final long serialVersionUID = 1L;
	public String title = "base";
	public static int T = 16, W = 60*T,H = 60*T,Q = T*2;

	public boolean isRunning = false;
	
	public BufferedImage screen = new BufferedImage(W,H,BufferedImage.TYPE_4BYTE_ABGR_PRE);
	public JFrame frame;
	public Thread thread = new Thread(this);
	public int frameRate = 0;
	public int mx,my;
	
	public static Pipe curPipe,lastPipe;
	
	public int popSize = 100;
	public Bird flock[];
	public double mutationChance = 2;
	public int curBest = 0;
	
	public int genTimer = 0, timerMax = 30, genCount = 0, genMax = 30, popAlive = popSize;
	
	public static int curLevel = 0;
	public int maxLevel = 5;
	
	public boolean end = false;
	
	public Game() {
		this.setPreferredSize(new Dimension(W,H));
		this.addKeyListener(this);
		
		flock = new Bird[popSize];
		for (int i = 0; i < popSize;i++) {
			flock[i] = new Bird();
			flock[i].initializeElement(Bird.chromosomeLen);
			flock[i].setWeights();
		}
		
		curPipe = new Pipe();
		
		initFrame();
		thread.start();
	}
	
	public void initFrame() {
		frame = new JFrame(title);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(this);
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(true);
		frame.setLocationRelativeTo(null);
		requestFocus();
	}
	public int flockAlive() {
		popAlive = 0;
		for (Bird b : flock) {
			if (b.alive == true) {
				popAlive++;
			}
		}
		
		return popAlive;
	}
	
	public void tick() {
		frame.setTitle(title+" - "+frameRate);

		if (curPipe.rup.x+Pipe.w < Bird.startX) {
			lastPipe = curPipe;
			curPipe = new Pipe();
		}
		
		if (curPipe != null) {
			curPipe.tick();
		}
		if (lastPipe != null) {
			lastPipe.tick();
		}
		
		for(Bird b: flock) {
			b.tick();
		}

		GA.evaluateAll((GAElement[])flock);
		curBest = GA.findBest((GAElement[])flock);
		
		if (genTimer >= timerMax*(curLevel+1) || flockAlive() <= 0) {
			System.out.println("generation: "+genCount+" | best Score: "+flock[curBest].eval+" | best Time: "+genTimer+" seconds");
			
			if (genCount > genMax || end == true) {
				System.out.println("best of Generation: "+flock[curBest].value);
				System.exit(0);
			}

			lastPipe = null;
			curPipe = new Pipe();
			
			GAElement[] newGen = GA.generation(mutationChance, popSize, (GAElement[])flock);
			for (int i = 0; i < popSize;i++) {
				flock[i].transferChromosome(newGen[i]);
				flock[i].reset();
			}
			
			if (genTimer >= timerMax*(curLevel+1) && curLevel < maxLevel) {
				System.out.println("WIN!");
				
				curLevel++;
				if (curLevel == maxLevel) {
					System.out.println("level "+maxLevel+": no more time limit!");
				}
			}
			
			genTimer = 0;
			genCount++;
		}
	}
	
	public void render() {
		BufferStrategy bs = this.getBufferStrategy();
		if (bs == null) {
			this.createBufferStrategy(3);
			return;
		}
		
		Graphics g = screen.getGraphics();
		
		g.setColor(Color.black);
		g.fillRect(0,0,W,H);
		// render //
		
		if (lastPipe != null) {
			lastPipe.render(g);
		}
		if (curPipe != null) {
			curPipe.render(g);
		}

		for (int i = 0; i < flock.length;i++) {
			flock[i].render(g);
		}
		
		// UI
		
		g.setColor(Color.white);
		if (end) {
			g.setColor(Color.red);
		}
		g.drawString("curGen: "+genCount, 10, 10);
		g.drawString("level: "+curLevel, 10, 25);
		g.drawString("remaining population: "+popAlive, 10, 40);
		if (curLevel < maxLevel) {
			g.drawString("timer: "+genTimer+"  /  "+(timerMax * (curLevel+1)), 10, 55);
		} else {
			g.drawString("timer: "+genTimer, 10, 55);
		}
		// render //
		g = bs.getDrawGraphics();
		Rectangle scr = getBounds();
		g.setColor(Color.black);
		g.fillRect(scr.x,scr.y,scr.width,scr.height);
		g.drawImage(screen,scr.width/2-W/2,scr.height/2-H/2,W,H,null);
		
		bs.show();
	}
	
	public static void main(String[] args) {
		new Game();
	}

	public void run() {
		long lastTime = System.nanoTime();
		double amountOfTicks = 120.0;
		double ns = 1000000000 / amountOfTicks;
		double delta = 0;
		int frames = 0;
		double timer = System.currentTimeMillis();
		isRunning = true;
		
		while (isRunning) {
			long now = System.nanoTime();
			delta += (now-lastTime) / ns;
			lastTime = now;
			if (delta >= 1) {
				tick();
				render();
				frames++;
				delta--;
			}
			
			if (System.currentTimeMillis() - timer >= 1000) {
				frameRate = frames;
				genTimer++;
				frames = 0;
				timer += 1000;
			}
		}
	}

	public void keyTyped(KeyEvent e) {}

	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_E) {
			end = true;
		}
	}

	public void keyReleased(KeyEvent e) {}
}
