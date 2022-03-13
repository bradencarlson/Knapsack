import java.math.BigInteger;
import java.util.Random;

public class knapsack {
	public static void main(String[] args) {
		BigInteger[] sequence = privateSequence(16,4);
		printArray(sequence);
		byte[] message = new byte[2];
		message[0] = 0x02;
		message[1] = 0x02;
		System.out.println(encrypt(message,sequence));
	}

	/** This method returns a superincreasing sequence with a given length, where
	*   the first number has at most a given number of bits.
	*   @param length The number of terms to be in the sequence
	*   @param start_length The maximum number of bits for the first number in the
	*   sequence
	*   @return The superincreasing sequence
	*/
	public static BigInteger[] privateSequence(int length, int start_length) {
		BigInteger[] sequence = new BigInteger[length];
		Random r = new Random();
		BigInteger start = new BigInteger(start_length,r);
		sequence[0] = start;
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

	/** Encrypt method that takes in an byte array that is the message, as well as
	*   the public sequence, then for each byte in the message, computes each
	*   bit in the byte, then adds the corresponding number in the public sequence
	*   to the encrypted message.  Note that this method requires that the length
	*   of the publicSequence must be an integer multiple of 8.  This will probably
	*   eventualy be fixed.
	*   @param message the message to be encrypted
	*   @param publicSequence The public sequence to be used for encryption
	*   @return The number that corresponds to the encrypted block
	*/
	public static BigInteger encrypt(byte[] message, BigInteger[] publicSequence) {
		int one = 0x01;	// first bit
		int two = 0x02; // second bit
		int four = 0x04; // ...
		int eight = 0x08;
		int sixteen = 0x10;
		int thirtytwo = 0x20;
		int sixtyfour = 0x40;
		int onetwentyeight = 0x80; // eighth bit.

		int count = 0;
		BigInteger encryptedMessage = new BigInteger("0");

		for(int i = 0; i< message.length; i++) {
			int x1 = message[i] & one;
			int x2 = (message[i] & two)/2;
			int x3 = (message[i] & four)/4;
			int x4 = (message[i] & eight)/8;
			int x5 = (message[i] & sixteen)/16;
			int x6 = (message[i] & thirtytwo)/32;
			int x7 = (message[i] & sixtyfour)/64;
			int x8 = (message[i] & onetwentyeight)/128;

			encryptedMessage = encryptedMessage.add(publicSequence[count++].multiply(
				new BigInteger(Integer.toString(x1))));
			encryptedMessage = encryptedMessage.add(publicSequence[count++].multiply(
				new BigInteger(Integer.toString(x2))));
			encryptedMessage = encryptedMessage.add(publicSequence[count++].multiply(
				new BigInteger(Integer.toString(x3))));
			encryptedMessage = encryptedMessage.add(publicSequence[count++].multiply(
				new BigInteger(Integer.toString(x4))));
			encryptedMessage = encryptedMessage.add(publicSequence[count++].multiply(
				new BigInteger(Integer.toString(x5))));
			encryptedMessage = encryptedMessage.add(publicSequence[count++].multiply(
				new BigInteger(Integer.toString(x6))));
			encryptedMessage = encryptedMessage.add(publicSequence[count++].multiply(
				new BigInteger(Integer.toString(x7))));
			encryptedMessage = encryptedMessage.add(publicSequence[count++].multiply(
				new BigInteger(Integer.toString(x8))));
		}
		return encryptedMessage;
	}

	public static void printArray(BigInteger[] array) {
		for(int i = 0; i< array.length; i++) {
			System.out.println(array[i]);
		}
	}
}
