package core;

import java.util.Random;

public abstract class GAElement {
	public double eval;
	public String value;
	
	public void initializeElement(int chromosomeBits) {
		Random r = new Random(System.nanoTime());
		value = "";
		for(int i = 0; i < chromosomeBits;i++) {
			if (r.nextDouble() < 0.5) {
				value += "0";
			} else {
				value += "1";
			}
		}
	}
	
	public String getValue() {
		return value;
	}
	
	public int binToInteger(String bin) {
		return Integer.parseInt(bin, 2);
	}
	
	public double getGeneValue(String gene, double min, double max) {
		return binToInteger(gene)*(max-min) / (Math.pow(2, gene.length()) - 1) + min;
	}
	
	public static int binToInt(String bin) {
		return Integer.parseInt(bin, 2);
	}
	
	@SuppressWarnings("deprecation")
	public GAElement crossover(GAElement other) {
		GAElement son = null;
		String nvalue = "";
		int cut = new Random(System.nanoTime()).nextInt(value.length());
		
		if (new Random(System.nanoTime()).nextDouble() < 0.5) {
			nvalue = value.substring(0, cut) + other.value.substring(cut,value.length());
		} else {
			nvalue = other.value.substring(0, cut) + value.substring(cut,value.length());
		}
		
		try {
			son = (GAElement)other.getClass().newInstance();
			son.value = nvalue;
		} catch (Exception e) {}
		
		return son;
	}
	
	public static double translateBinToRealNum(String bin, double min, double max, int k) { // k = number of bits used
		return min+((max-min)/Math.pow(2,k) * binToInt(bin));
	}
	
	public void transferChromosome(GAElement donor) {
		this.value = donor.value;
	}
	
	public void mutation(double chanceOfMutation) {
		Random r = new Random(System.nanoTime());
		char[] valuechar = value.toCharArray();
		
		for (int i = 0; i < value.length();i++) {
			if (r.nextDouble() <= chanceOfMutation/100) {
				if (valuechar[i] == '0') {
					valuechar[i] = '1';
				} else {
					valuechar[i] = '0';
				}
			}
		}
		
		value = new String(valuechar);
	}
	
	public abstract void evaluateElement();
}
