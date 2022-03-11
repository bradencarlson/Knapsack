import java.math.BigInteger;
import java.util.Random;

public class knapsack {
	public static void main(String[] args) {
		BigInteger[] array = privateSequence(5,100);
		printArray(array);
	}
	public static BigInteger[] privateSequence(int length, int start_length) {
		BigInteger[] sequence = new BigInteger[length];
		Random r = new Random();
		BigInteger start = new BigInteger(start_length,r);
		sequence[0] = start;
		byte[] array = start.toByteArray();
		BigInteger sum = start;
		for(int i = 1; i< length; i++) {
			BigInteger next = sum;
			BigInteger multiplier = new BigInteger(Integer.toString(r.nextInt(100000+i)));
			next = next.multiply(multiplier);
			sum = sum.add(next);
			sequence[i] = next;
		}
		return sequence;
	}

	public static void printArray(BigInteger[] array) {
		for(int i = 0; i< array.length; i++) {
			System.out.println(array[i]);
		}
	}
}
