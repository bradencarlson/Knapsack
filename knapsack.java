import java.math.BigInteger;
import java.util.Random;

public class knapsack {
	public static void main(String[] args) {
		
	}

	/** This method returns a superincreasing sequence with a given length, where
	*		the first number has at most a given number of bits.
	*		@param length The number of terms to be in the sequence
	*		@param start_length The maximum number of bits for the first number in the
	*		sequence
	*		@return The superincreasing sequence
	*/
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
