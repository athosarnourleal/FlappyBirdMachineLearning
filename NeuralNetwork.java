package core;

public class NeuralNetwork {

	public double neuronValue[][]; // neuronValue[layer][neuron] 
	public double weights[][][]; // weights[layer][recipientID][donorID]
	public double bias[];

	public final int bitsPerGene = 10;
	
	public static final double weightMax = 4, weightMin = -4;
	public static final double biasMax = 4, biasMin = -4;
	
	public int layerNum;
	
	public NeuralNetwork(int layerSizes[]) {
		int layerNum = layerSizes.length;
		
		neuronValue = new double[layerNum][];
		for (int i = 0 ;i < layerNum; i++) {
			neuronValue[i] = new double[layerSizes[i]];
		}
		
		weights = new double[layerNum-1][][];
		for (int i = 1;i < layerNum; i++) {
			weights[i-1] = new double[layerSizes[i]][layerSizes[i-1]];
		}
		
		bias = new double[layerNum-1];
		
		this.layerNum = layerNum;
	}
	
// ------------------------------------------------------------------------------------------------------ EXECUTING //
	
	public void runNeuralNetwork(double inputs[]) {
		setInputs(inputs);
		updateNetwork();
	}
	
	public void setInputs(double inputs[]) {
		if (neuronValue[0].length != inputs.length) {
			Main.error("INPUT LENGTH MISMATCH(defineInputs)");
		}
		
		for (int i = 0; i < inputs.length;i++) {
			neuronValue[0][i] = inputs[i];
		}
	}
	
	public double[] getOutputLayer() {
		return neuronValue[layerNum-1];
	}
	
	public double getSingularOutput(int ID) {
		return neuronValue[layerNum-1][ID];
	}
	
	public int getBiggestOutput() {
		int ID = 0;
		
		for (int i = 1; i < neuronValue[layerNum-1].length;i++) {
			if (neuronValue[layerNum-1][i] > neuronValue[layerNum-1][ID]) {
				ID = i;
			}
		}
		
		return ID;
	}

	public void updateNetwork() {
		for (int layer = 1; layer < layerNum;layer++) {
			for (int neuron = 0; neuron < neuronValue[layer].length; neuron++) {
				neuronValue[layer][neuron] = RELU(getPonderedSum(layer, neuron));
			}
		}
	}

	// ------------------------------------------------------------------------------------------------------ CALCULATIONS //
		
	public double getPonderedSum(int curLayer, int curNeuron) {
		double sum = 0;
		
		if (curLayer == 0) {
			Main.error("CANT CALCULATE THE PONDERED SUM IN THE INPUT LAYER(getPonderedSum)");
		}
		
		int lastLayer = curLayer-1;
		
		for (int i = 0; i < weights[lastLayer][curNeuron].length; i++) {
			sum += neuronValue[lastLayer][i] * weights[lastLayer][curNeuron][i];
		}
		
		sum += bias[curLayer-1];
		
		return sum;
	}
	
	public double gaussianFunction(double u) {
		double middle = 5, radious = 5.8;// default presets
		
		return gaussianFunction(u,middle,radious);
	}
	
	public double gaussianFunction(double u, double middle, double radious) {
		double value = Math.E * -1*Math.pow((u - middle), 2) / Math.pow(radious,2);
		
		return value;
	}
	
	public double RELU(double u) {
		if (u < 0) {
			return 0;
		} else {
			return u;
		}
	}
	
// ------------------------------------------------------------------------------------------------------ GENETIC MANIPULATION //

	public void defineWeightsFromDNA(GAElement element) {
		if (element.value.length() != getDNALength()) {
			Main.error("SIZE OF DNA IS NOT COMPATIBLE WITH NEURAL NETWORK(defineWeightsFromDNA)");
		}
		
		int curGene = 0;
		
		for (int i = 0; i < weights.length;i++) {
			for (int j = 0; j < weights[i].length;j++) {
				for (int k = 0; k < weights[i][j].length;k++) {
					weights[i][j][k] = element.getSubsectionNumeralValue(curGene, bitsPerGene, weightMin, weightMax);
					curGene++;
				}
			}
		}
		
		for (int i = 0; i < bias.length; i++) {
			bias[i] = element.getSubsectionNumeralValue(curGene, bitsPerGene, biasMin, biasMax);
			curGene++;
		}
	}
	
	public int getDNALength() {
		int geneNumber = 0;
		for (int i = 0; i < weights.length;i++) { // weights
			geneNumber+= weights[i].length * weights[i][0].length;
		}
		
		geneNumber+= bias.length; // weights
		
		
		return geneNumber * bitsPerGene;
	}
	
	
}
