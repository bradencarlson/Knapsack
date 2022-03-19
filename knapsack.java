import java.math.BigInteger;
import java.util.Random;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class knapsack {
	public static void main(String[] args) {
		byte[][] message = split("message.txt",5);
		printDoubleArray(message);
		try {
			FileOutputStream out = new FileOutputStream("output.txt");
			for(int i = 0; i< message.length; i++) {
				out.write(message[i]);
			}
			out.close();
		} catch(IOException e) {
			System.err.println(e.getMessage());
		}
	}

	/** This method takes in the name of a file, as well as a length.
	*   This effectively splits up the message into blocks of bytes of
	*   the length provided.  This length should not be the length of the
	*   private sequence, but rather, (1/8)*(length of private sequence),
	*   since each byte will take up eight digits of the sequence.
	*   @param filename the name of the file where the message is
	*   being stored
	*   @param sequence_length the length of the blocks
	*   @return a double byte array containing the blocks of the message.
	*/
	public static byte[][] split(String filename, int sequence_length) {
		ArrayList<byte[]> list  = new ArrayList<>();
		try {
			FileInputStream fs = new FileInputStream(filename);
			byte[] block = new byte[sequence_length];
			int count = 0;
			do {
				count = fs.read(block);
				list.add(block);
				block = new byte[sequence_length];
			} while(count!=-1);


		} catch(IOException e) {
			System.err.println(e.getMessage());
		}


		byte[][] message = new byte[list.size()][sequence_length];
		for(int i = 0; i< list.size(); i++) {
			message[i] = list.get(i);
		}

		return message;
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
			multiplier = multiplier.add(new BigInteger(Integer.toString(r.nextInt(10000000))));
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

	public static BigInteger[] publicSequence(BigInteger[] privateSequence, BigInteger modulus, BigInteger multiplier) {
		BigInteger[] publicSequence = new BigInteger[privateSequence.length];
		for (int i = 0; i < publicSequence.length; i++) {
			publicSequence[i] = (privateSequence[i].multiply(multiplier)).mod(modulus);
		}

		return publicSequence;
	}


	//  for testing only 

	public static void printArray(BigInteger[] array) {
		for(int i = 0; i< array.length; i++) {
			System.out.println(array[i]);
		}
	}
	public static void printDoubleArray(byte[][] array) {
		for(int i = 0; i< array.length; i++) {
			for(int j = 0; j<array[i].length; j++) {
				System.out.println(array[i][j]+" ");
			}
			System.out.println();
		}
		System.out.println();
	}

}
