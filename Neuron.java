package core;

public class Neuron {
	
	public double weights[];
	public double bias;
	
	public double weightedSum = 0;
	
	double activation = 0;
	
	public Neuron(int weightNum) {
		weights = new double[weightNum];
	}
	
	public void defineWeight(String gene, int weightNum, int valueBits, double Wmin, double Wmax) {
		double[] w = new double[weightNum];
		
		for (int i = 0; i < w.length+1; i++) {
			String locus = gene.substring(i*valueBits, (i+1)*valueBits);
			if (i == w.length) {
				// last part is reserved to the bias
				bias = GAElement.translateBinToRealNum(locus, Wmin, Wmax, valueBits);
			} else {
				w[i] = GAElement.translateBinToRealNum(locus, Wmin, Wmax, valueBits);
			}
		}
		
		defineWeight(w);
	}

	public void activateGau(double[] input) {
		getWeightedSum(input);
		gaussianFuncion(weightedSum);
	}
	public void activateStep(double[] input, double threshold) {
		getWeightedSum(input);
		
		stepFunction(weightedSum, threshold);
	}

	public double gaussianFuncion(double u) {
		double middle = 5, radious = 5.8;
		
		return gaussianFuncion(u,middle,radious);
	}
	
	public double gaussianFuncion(double u, double middle, double radious) {
		double value = Math.E * -1*Math.pow((u - middle), 2) / Math.pow(radious,2);
		
		activation = value;
		return value;
	}
	
	public int stepFunction(double u, double threshold) {
		if (u >= threshold) {
			activation = 1;
			return 1;
		}
		activation = 0;
		return 0;
	}
	
	public double getWeightedSum(double input[]) {
		double sum = 0;
		
		for (int i = 0; i < input.length; i++) {
			sum += input[i]*weights[i];
		}
		sum += bias;
		
		this.weightedSum = sum;		
		return sum;
	}
	

	public void defineWeight(double weights[]) {
		for (int i = 0; i < weights.length; i++) {
			this.weights[i] = weights[i];
		}
	}
	
}
