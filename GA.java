package core;

import java.util.Random;

public class GA {
	
	public static void initializePop(int chromosomeBits, GAElement pop[]) {
		for (int i = 0; i < pop.length;i++) {
			pop[i].initializeElement(chromosomeBits);
		}
	}
	
	public static double evaluateAll(GAElement pop[]) {
		double evalSum = 0;
		for (int i = 0; i < pop.length;i++) {
			pop[i].evaluateElement();
			evalSum+=pop[i].eval;
		}
		return evalSum;
	}
	
	public static void mutateAll(double chanceOfMutation, GAElement pop[]) {
		for (int i = 0; i < pop.length;i++) {
			pop[i].mutation(chanceOfMutation);
		}
	}

	public static double evalSum(GAElement pop[]) {
		double evalSum = 0;
		for (int i = 0; i < pop.length;i++) {
			evalSum+=pop[i].eval;
		}
		return evalSum;
	}
	
	public static int roulette(GAElement pop[]) {
		return roulette(evalSum(pop), pop);
	}
	
	public static int roulette(double evalSum, GAElement pop[]) {
		Random r = new Random(System.nanoTime());
		double s = r.nextDouble()*evalSum;
		
		int i = 0;
		double aux = pop[0].eval;
		while (aux < s) {
			i++;
			aux += pop[i].eval;
		}
		
		return i;
	}
	
	public static GAElement[] generation(double mutationChance, int newSize, GAElement pop[]) {
		// codigo basico
		Random r = new Random(System.nanoTime());
		
		GAElement newPop[] = new GAElement[pop.length];
		GAElement dad,mom,child;
		
		double evalSum = evaluateAll(pop);
		
		for (int i = 0; i < newSize; i++) {
			dad = (GAElement) pop[roulette(evalSum, pop)];
			mom = (GAElement) pop[roulette(evalSum, pop)];
			
			child = dad.crossover(mom);
			
			child.mutation(mutationChance);
			
			newPop[i] = child;
		}
		
		return newPop;
	}
	
	public static void mutateAll(GAElement[] pop,double chanceOfMutation) {
		for (int i = 0; i < pop.length;i++) {
			pop[i].mutation(chanceOfMutation);
		}
	}
	
	public static int findBest(GAElement pop[]) {
		int best = 0;
		double bestEval = 0;
		for (int i = 0; i < pop.length; i++) {
			if (pop[i].eval > bestEval) {
				bestEval = pop[i].eval;
				best = i;
			}
		}
		return best;
	}
}
