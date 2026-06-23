package core;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

public class Main extends Canvas implements Runnable,KeyListener {
	
	private static final long serialVersionUID = 1L;
	public String title = "flappy bird AI";
	public static int T = 16, W = 60*T,H = 60*T,Q = T*2;

	public static boolean isRunning = false;
	public double FPS = 60.0;
	
	public BufferedImage screen = new BufferedImage(W,H,BufferedImage.TYPE_4BYTE_ABGR_PRE);
	public JFrame frame;
	public Thread thread = new Thread(this);
	public int frameRate = 0;
	public int mx,my;
	
	
	public int popSize = 300;
	public Bird flock[];
	public double mutationChance = .5;
	public int curBest = 0;
	
	public int genTimer = 0, timerMax = 50,
			genCount = 0, genMax = 100, 
			popAlive = popSize;

	public GAElement[] lastGen;

	public String bests[];
	public int topScores[];

	public boolean isInDemonstration = false;
	public String demonstrationBirdDNA = 
	"1100111010010101110011000000101011101011001110100110011001100011001101110100101001010111001010100101100101010110011101000011100110101110"
	+ "11011010001100111100110110100011110100110001010111110010001010010110110101001101100100110111101101000010111111010111001000001011101111"
	+ "00011100010111011110010001010010001000000011111011001101001110001011100100101001101011111100001001100110111101110011000110001011001110"
	+ "00111000110110011001000101";
	
	public Bird demonstrationBird;
	public double demonstrationHighScore;

	public Font titleFont = new Font(Font.MONOSPACED, Font.PLAIN, 24);
	public Font textFont = new Font(Font.MONOSPACED, Font.PLAIN, 18);
	public Font neuronValueFont = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
	
	public static Pipe curPipe,lastPipe;
	
	public static int curLevel = 0;
	public static int maxLevel = 4;
	public int levelTimer = 20;
	public int levelTimerAdition = 4;
	public int levelScores[];
	public int timeInScore = 0;
	
	public static Color levelColors[] = {
		Color.blue,
		Color.cyan,
		Color.green,
		Color.yellow,
		Color.orange,
		Color.red
	};
	
	public int UIState = 1, UINumOfStates = 3;
	public int offset = 30;
	public Rectangle graphicRectangle = new Rectangle(0,offset,29*T,18*T);
	public Rectangle brainRectangle = new Rectangle(0,offset,38*T,20*T);
	public int brainXBorder = 90;
	public boolean end = false, pause = false;
	
	
	public Main() {
		this.setPreferredSize(new Dimension(W,H));
		this.addKeyListener(this);

		bests = new String[genMax];
		topScores = new int[genMax];
		
		flock = new Bird[popSize];
		for (int i = 0; i < popSize;i++) {
			flock[i] = new Bird();
		}

		graphicRectangle.x = W-graphicRectangle.width-offset;
		
		brainRectangle.x = W-brainRectangle.width-offset;
		
		curPipe = new Pipe();
		
		levelScores = new int[maxLevel];
		
		initFrame();
		thread.start();
	}
	
	public void setupDemonstration() {
		demonstrationBird = new Bird();
		
		demonstrationBird.value = demonstrationBirdDNA;
		demonstrationBird.reset();
		
		curPipe = new Pipe();
		lastPipe = null;
		
		isInDemonstration = true;
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
	public int populationAlive() {
		popAlive = 0;
		
		for (Bird b : flock) {
			if (b.alive == true) {
				popAlive++;
			}
		}
		
		return popAlive;
	}
	
	public double map(double val, double min1, double max1, double min2, double max2) {
		return min2 + (max2-min2)*(val-min1)/(max1-min1);
	}
	
	public void tick() {
		frame.setTitle(title+" - "+frameRate);
		if (pause) {
			return;
		}
		
		if (isInDemonstration == true) {
			tickDemonstration();
		} else {
			tickGeneration();
		}
	}
	
	public void pipeTick() {
		if (curPipe.rup.x + Pipe.w < Bird.startX) {// switch pipes
			lastPipe = curPipe;
			curPipe = new Pipe();
		}
		
		curPipe.tick();

		if (lastPipe != null) {
			lastPipe.tick();
		}
	}
	
	public void tickDemonstration() {
		if (curLevel < maxLevel && genTimer > levelTimer * (curLevel+1)) {
			curLevel++;
		}
		
		pipeTick();
		
		demonstrationBird.tick();
		demonstrationBird.evaluateElement();
		
		if (demonstrationBird.alive == false) {
			
			demonstrationHighScore = (demonstrationBird.eval > demonstrationHighScore) ? demonstrationBird.eval : demonstrationHighScore;
			demonstrationBird.reset();
			
			lastPipe = null;
			curPipe = new Pipe();

			curLevel = 0;
			genTimer = 0;
		}
		
		if (end) {
			System.out.println("the demonstrations Bird highScore is: " + demonstrationHighScore);
			stop();
		}
		
	}
	
	public void tickGeneration() {
		
		if (curLevel < maxLevel && genTimer > levelTimer * (curLevel+1)) {
			levelScores[curLevel] = timeInScore;
			curLevel++;
		}
				
		pipeTick();
		
		for(Bird b: flock) {
			b.tick();
		}
		
		timeInScore++;
		
		GA.evaluateAll((GAElement[])flock);
		curBest = GA.findBest((GAElement[])flock);
		
		if (end == true || populationAlive() <= 0) {
			System.out.println("generation: "+genCount+" | best Score: "+flock[curBest].eval+" | best Time: "+genTimer+" seconds");

			bests[genCount] = flock[curBest].value;
			topScores[genCount] = (int)flock[curBest].eval;
			
			
			if (genCount >= genMax-1 || end == true) {// FINALIZAR
				int allTimeBest = 0;
				
				for (int i = 0; i < genCount; i++) {
					if (topScores[i] >= topScores[allTimeBest]) {
						allTimeBest = i;
					}
				}

				System.out.println("best of all generations: " + bests[allTimeBest]);
				System.out.println("best score of all generations was: " + topScores[allTimeBest]);
				
				demonstrationBirdDNA = bests[allTimeBest];
				demonstrationHighScore = topScores[allTimeBest];
				
				setupDemonstration();
				
				end = false;
			} else {
				lastPipe = null;
				curPipe = new Pipe();
				
				GAElement[] newGen;
				if (genCount == 0) {
					newGen = GA.simpleGeneration(mutationChance, popSize, (GAElement[])flock);
					lastGen = (GAElement[])flock;
					
				} else {
					newGen = GA.simpleGeneration(mutationChance, popSize, GA.concatPop(lastGen, (GAElement[])flock));
					
					lastGen = (GAElement[])flock;
				}
				
				for (int i = 0; i < popSize;i++) {
					flock[i].transferChromosome(newGen[i]);// replace the generation's chromosomes
					flock[i].reset();
				}
			}
			
			genCount++;
			
			curLevel = 0;
			genTimer = 0;
			timeInScore = 0;
		}
		
	}
	
	public void renderUINeuralNetwork(int UIBoxBorder, Graphics g) {
		// BRAIN OF THE SMARTEST BIRD \\
		
		Bird best;
		if (isInDemonstration == false) {
			best = flock[curBest];
		} else {
			best = demonstrationBird;
		}
		
		g.setColor(Color.white);
		g.fillRect(brainRectangle.x-UIBoxBorder, brainRectangle.y-UIBoxBorder,
				brainRectangle.width+UIBoxBorder*2, brainRectangle.height+UIBoxBorder*2);
		
		if (isInDemonstration) {
			g.drawString("demonstration bird's brain:", brainRectangle.x, brainRectangle.y-offset/4);
		} else {
			g.drawString("best from generation:", brainRectangle.x, brainRectangle.y-offset/4);
		}
		
		g.setColor(best.color);
		g.fillRect(brainRectangle.x, brainRectangle.y,offset,offset);
		
		double xoffset = (brainRectangle.width-2*brainXBorder)/(Bird.neuronLayerSizes.length-1);
		
		int neuronRadious = 8;
		
		for (int layer = 0; layer < Bird.neuronLayerSizes.length; layer++) {
			double yoffset = brainRectangle.height / (Bird.neuronLayerSizes[layer] + 1);
			
			for (int neuron = 0; neuron < Bird.neuronLayerSizes[layer]; neuron++) {
				int nx = brainRectangle.x+(int)(layer*xoffset)+brainXBorder, ny = brainRectangle.y + (int)((neuron+1)*yoffset);
				
				if (layer > 0 && neuron == 0) {// drawBias
					double bias = best.brain.bias[layer - 1];
					
					double red = map(bias, NeuralNetwork.biasMax,NeuralNetwork.biasMin, 0, 255);
					double blue = map(bias, NeuralNetwork.biasMin, NeuralNetwork.biasMax, 0, 255);
					
					g.setColor(new Color((int)red, 0, (int)blue));

					g.fillRect((int)(nx-neuronRadious), (int)(ny-neuronRadious-neuronRadious*5), neuronRadious*2, neuronRadious*2);
				}
				
				if (layer >= Bird.neuronLayerSizes.length-1) {// is last layer
					if (neuron == best.curAction) {
						g.setColor(Color.RED);
					} else {
						g.setColor(Color.black);
					}
				} else { // is not last
					//draw connections
					
					double yoffset2 = brainRectangle.height / (Bird.neuronLayerSizes[layer+1] + 1);
					for (int i = 0; i < Bird.neuronLayerSizes[layer+1]; i++) {
						
						double weight = best.brain.weights[layer][i][neuron];
						
						// set connection color based on weights
						double red = map(weight, NeuralNetwork.weightMax,NeuralNetwork.weightMin, 0, 255);
						double blue = map(weight, NeuralNetwork.weightMin, NeuralNetwork.weightMax, 0, 255);
						g.setColor(new Color((int)red, 90, (int)blue));

						g.drawLine(nx, ny, (int)(nx+xoffset), brainRectangle.y + (int)((i+1)*yoffset2));
						g.drawLine(nx, ny+1, (int)(nx+xoffset), brainRectangle.y + (int)((i+1)*yoffset2)+1);
						g.drawLine(nx+1, ny, (int)(nx+xoffset)+1, brainRectangle.y + (int)((i+1)*yoffset2));
						g.drawLine(nx+1, ny+1, (int)(nx+xoffset)+1, brainRectangle.y + (int)((i+1)*yoffset2)+1);
					}
					g.setColor(Color.gray);
					
				}
				
				//drawNeuron
				
				g.fillOval(nx-neuronRadious, ny-neuronRadious, neuronRadious*2, neuronRadious*2);

				g.setFont(neuronValueFont);
				g.setColor(Color.black);
				String neuronValue = "" + (int)best.brain.neuronValue[layer][neuron];
				g.drawString(neuronValue,nx-g.getFontMetrics().stringWidth(neuronValue)+neuronRadious,ny-neuronRadious);
				
				if (layer == Bird.neuronLayerSizes.length-1) {
					g.drawString(Bird.outputLabels[neuron],(int)(nx+neuronRadious*1.5),ny+neuronRadious/2);
				} else if (layer == 0) {
					g.drawString(Bird.inputLabels[neuron],
						nx - neuronRadious - g.getFontMetrics().stringWidth(Bird.inputLabels[neuron]),
						ny+neuronRadious);
				}
				
			}
		}
	}
	
	public void renderUIScoreGraphic(int UIBoxBorder, Graphics g) {
		// GRAPHIC OF THE GENERATION'S HIGHSCORES \\
		
		if (isInDemonstration) {
			UIState = 0; // this menu cant me accessed during a demonstration
			return;
		}
		
		double genScoreCollumnHeight[] = new double[genCount+1];
		double bestEval = flock[curBest].eval;
		
		for (int i = 0; i < genCount; i++) {
			if (topScores[i] > bestEval) {
				bestEval = topScores[i];// find topScore
			}
		}
		
		// draw background
		
		g.setColor(Color.white);
		
		g.drawString("generation score - highscore: "+bestEval, graphicRectangle.x, graphicRectangle.y-offset/4);
		g.fillRect(graphicRectangle.x-UIBoxBorder, graphicRectangle.y-UIBoxBorder,
				graphicRectangle.width+UIBoxBorder*2, graphicRectangle.height+UIBoxBorder*2);
		
		if (genCount == 0) { // draw standby text when graphic cant be created
			g.setColor(Color.black);
			
			String standByText = "waiting for more data...";
			g.drawString(standByText,
					graphicRectangle.x + graphicRectangle.width/2 - g.getFontMetrics().stringWidth(standByText)/2,
					graphicRectangle.y + graphicRectangle.height/2 - g.getFontMetrics().getHeight()/2);
			
			return;
		}
		
		// draw the score that changes the levels
		for (int i = 0; i < maxLevel && levelScores[i] < bestEval && levelScores[i] > 0; i++) {
			
			int levelHeight = (int)(graphicRectangle.height * levelScores[i] / bestEval);
			levelHeight = graphicRectangle.y+graphicRectangle.height-levelHeight;
			
			g.setColor(levelColors[i+1]);
			
			g.drawString(""+(i+1), graphicRectangle.x+8, levelHeight-1);

			g.drawLine(graphicRectangle.x, levelHeight, graphicRectangle.x+graphicRectangle.width, levelHeight);
		}
		
		double xoffset = graphicRectangle.width;
		
		if (genCount != 0) {
			xoffset = graphicRectangle.width / genCount;
		}

		// draw graph lines //
		
		// set the graphic collumns
		for (int i = 0; i < genCount;i++) {
			genScoreCollumnHeight[i] = (graphicRectangle.height * topScores[i]) / bestEval;

			g.setColor(Color.gray);
			if (i != 0) {
				g.drawLine((int)(graphicRectangle.x + xoffset*i), graphicRectangle.y, 
						(int)(graphicRectangle.x + xoffset*i), graphicRectangle.y + graphicRectangle.height);
				
			}
			g.setColor(Color.blue);
			g.fillRect((int)(graphicRectangle.x + xoffset*i), graphicRectangle.y + graphicRectangle.height - (int)genScoreCollumnHeight[i],2,2);
		}
		
		// set the last collumn of the graphics as the current gen's best evaluation
		genScoreCollumnHeight[genCount] = (graphicRectangle.height * flock[curBest].eval) / bestEval;
		
		// link the columns with lines
		for (int i = 0; i < genScoreCollumnHeight.length-1;i++) {
			if (genScoreCollumnHeight[i+1] < genScoreCollumnHeight[i] ) {
				g.setColor(Color.red);
			} else {
				g.setColor(Color.blue);
			}
			g.drawLine((int)(graphicRectangle.x + xoffset*i), graphicRectangle.y + graphicRectangle.height - (int)genScoreCollumnHeight[i],
					(int)(graphicRectangle.x + xoffset*(i+1)), graphicRectangle.y + graphicRectangle.height - (int)genScoreCollumnHeight[i+1]);
		}
		
	}
	
	public void renderUI(Graphics g) {

		g.setFont(textFont);
		
		int UIBoxBorder = 2;
		
		switch(UIState) {
			case 1:
				renderUINeuralNetwork(UIBoxBorder, g);
			break;
			case 2:
				renderUIScoreGraphic(UIBoxBorder, g);
			break;
		}
		
		g.setFont(textFont);
		
		if (isInDemonstration) {
			g.setColor(Color.yellow);
			g.setFont(titleFont);
			int y = g.getFontMetrics().getHeight();
			g.drawString("DEMONSTRATION", 10, y);
		
			g.setFont(textFont);
			y += g.getFontMetrics().getHeight();
			g.drawString("timer: "+genTimer, 10, y);
			
		} else {
			g.setColor(Color.white);
			
			String[] menuData = {
					"curGen: "+genCount+" / "+(genMax-1),
					"level: "+curLevel,
					"remaining population: "+popAlive,
					"timer: "+genTimer
			};
			
			int y = g.getFontMetrics().getHeight();
			for (int i = 0; i < menuData.length; i ++) {
				g.drawString(menuData[i], 10, y);
				
				y+= g.getFontMetrics().getHeight();
			}
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

		if (isInDemonstration) {
			demonstrationBird.render(g);
		} else {
			for (int i = 0; i < flock.length;i++) {
				flock[i].render(g);
			}
		}
		
		// UI
		
		renderUI(g);
		
		// render //
		
		g = bs.getDrawGraphics();
		Rectangle scr = getBounds();
		g.setColor(Color.black);
		g.fillRect(scr.x,scr.y,scr.width,scr.height);
		g.drawImage(screen,scr.width/2-W/2,scr.height/2-H/2,W,H,null);
		
		bs.show();
	}
	
	public static void main(String[] args) {
		new Main();
	}
	
	public static void error(String message) {
		System.out.println("ERROR: " + message);
		
		stop();
	}
	
	public static void stop() {
		isRunning = false;
		System.exit(0);
	}

	public void run() {
		long lastTime = System.nanoTime();
		double amountOfTicks = FPS;
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
				if (pause == false) {
					genTimer++;
				}
				frames = 0;
				timer += 1000;
			}
		}
	}

	public void keyTyped(KeyEvent e) {}

	public void keyPressed(KeyEvent e) {}

	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_E) {
			end = true;
		}

		if (e.getKeyCode() == KeyEvent.VK_P) {
			if (pause) {
				pause = false;
			} else {
				pause = true;
			}
		}
		
		if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			UIState= (UIState+1) % UINumOfStates; 
		}
		
		if (e.getKeyCode() == KeyEvent.VK_D) {
			if (isInDemonstration == false) {
				setupDemonstration();
			}
		}
	}
}
