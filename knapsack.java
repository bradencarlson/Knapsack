import java.math.BigInteger;
import java.util.Random;

public class knapsack {
	public static void main(String[] args) {
		BigInteger[] array = privateSequence(10);
	}
	public static BigInteger[] privateSequence(BigInteger length) {
		BigInteger[] sequence = new BigInteger[length];
		Random r = new Random();
		BigInteger start = BigInteger(length,r);
		sequence[0] = start;
		for(BigInteger i = BigInteger.ONE; i.compareTo(length); i = i.add(BigInteger.ONE)) {
			BigInteger b = new BigInteger(length*2,r);
			while (b< sequence[i-1]) {
				BigInteger b = new BigInteger(length*2,r);
			}
			sequence[i] = b;
		}
	}
	
	public static void printArray(BigInteger[] array) {
		for(int i = 0; i< array.length; i++) {
			System.out.println(array[i]);
		}
	}
}