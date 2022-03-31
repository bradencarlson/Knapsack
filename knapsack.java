import java.math.BigInteger;
import java.util.Random;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import java.lang.IllegalArgumentException;

public class knapsack {

	/** main method to test and utilize all methods defined in this
	*   document.
	*   @param args arguments.
	*/
	public static void main(String[] args) {
		BigInteger[] privateS = {new BigInteger("2"),new BigInteger("4"),
		new BigInteger("8"),new BigInteger("16"),new BigInteger("32"),
		new BigInteger("64"), new BigInteger("128"), new BigInteger("256")};

		BigInteger modulus = new BigInteger("1000");
		BigInteger multiplier = new BigInteger("333");
		BigInteger inverse = new BigInteger("997");

		byte[][] message = split("message.txt",1);
		printDoubleArray(message);

		BigInteger[] publicS = new BigInteger[privateS.length];
		publicS = publicSequence(privateS, modulus,multiplier);

		BigInteger[] encrypted = new BigInteger[message.length];

		for(int i = 0; i< message.length; i++) {
			encrypted[i] = encrypt(message[i],publicS);
		}

		byte[][] decrypted = new byte[message.length][];
		for(int i = 0; i< encrypted.length; i++) {
			decrypted[i] = decrypt(encrypted[i],privateS, inverse,modulus);
		}
		System.out.println();
		printDoubleArray(decrypted);

		try {
			FileOutputStream fw = new FileOutputStream("output.txt");
			for(int i = 0; i< decrypted.length; i++) {
				fw.write(decrypted[i]);
			}
			fw.close();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}


		System.out.println((0b01 << 3) | 0b01 );

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
			while(fs.read(block)!=-1) {
				list.add(block);
				block = new byte[sequence_length];
			}


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
	*   the first number has at most a given number of bits.  This method first
	*   checks to ensure that the length of the sequence is a multiple of 8, so
  *   that it works with the encrypt method.
	*   @param length The number of terms to be in the sequence
	*   @param start_length The maximum number of bits for the first number in the
	*   sequence
	*   @return The superincreasing sequence
	*/
	public static BigInteger[] privateSequence(int length, int start_length) {
		if (length%8 != 0) {
			length += (8-length%8);
		}

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
	*   of the publicSequence must be an integer multiple of 8.  Which is
	*   guaranteed by the privateSequence method.
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

	/** Takes a sigle block of an encrypted message, and decrypts it,
	*   by using the private sequence that is provided, to solve the
	*   Knapsack problem of finding the original bits of the message.
	*   This method retuns a byte array that will contain the original
	*   message.
	*   @param message the BigInteger corresponding to the encrypted block
	*   @param privateSequence The private sequence
	*   @return a byte array containing the original message
	*/
	public static byte[] decrypt(BigInteger message, BigInteger[] privateSequence,
		BigInteger multiplier, BigInteger modulus) {
			message = message.multiply(multiplier).mod(modulus);

			int position = privateSequence.length-1;
			boolean[] bits = new boolean[8];

			byte[] decrypted = new byte[privateSequence.length/8];
			int count = 0;

			while (message.compareTo(BigInteger.ZERO)>0) {
				for(int i = 0; i< 8; i++) {
					//System.out.println(position);
					//System.out.println("Comparing: "+message + " to "+privateSequence[position]);
					if (message.compareTo(privateSequence[position])>=0) {
						bits[i] = true;
						message = message.subtract(privateSequence[position]);
						position--;
					} else {
						bits[0] = false;
						position--;
					}
				}
				byte myByte = toByte(bits);
				//System.out.println(myByte);
				decrypted[count++] = myByte;
			}

			return decrypted;
	}

	public static byte toByte(boolean[] array) throws IllegalArgumentException {
		if (array.length==8) {
			int[] bits = new int[8];

			bits[0] = 0b00000001;
			bits[1] = 0b00000010;
			bits[2] = 0b00000100;
			bits[3] = 0b00001000;
			bits[4] = 0b00010000;
			bits[5] = 0b00100000;
			bits[6] = 0b01000000;
			bits[7] = 0b10000000;

			int newByte = 0x00;
			byte test = 0b00000000;

			for(int i = 0; i< 8; i++) {
				if(array[i]) {
					//newByte = newByte | bits[i];
					test = (test | 0b01) >> 1;
				}
			}

			return (byte) newByte;
		}
		else {
			throw new IllegalArgumentException("To create a new byte, an array length\nof 8 is required.");
		}
	}

	/** Takes a private sequence and uses the given multiplier and modulus
	*   to convert it into a public sequence, if the multiplier is chosen well,
	*   then this will hide the details of the private sequence, so the
	*   encrypted message created with this public sequence is secure.
	*   @param privateSequence The private sequence to be kept secret
	*   @param modulus The modulus
	*   @param multiplier The multiplier to use.
	*   @return The public sequence to be published.
	*/
	public static BigInteger[] publicSequence(BigInteger[] privateSequence, BigInteger modulus, BigInteger multiplier) {
		BigInteger[] publicSequence = new BigInteger[privateSequence.length];
		for (int i = 0; i < publicSequence.length; i++) {
			publicSequence[i] = (privateSequence[i].multiply(multiplier)).mod(modulus);
		}

		return publicSequence;
	}



	/** Prints out a BigInteger array.
	*   @param array The array to be printed.
	*/
	public static void printArray(BigInteger[] array) {
		for(int i = 0; i< array.length; i++) {
			System.out.println(array[i]);
		}
	}

	/** prints out a double byte array.
	*   @param array The array to be printed.
	*/
	public static void printDoubleArray(byte[][] array) {
		for(int i = 0; i< array.length; i++) {
			for(int j = 0; j<array[i].length; j++) {
				System.out.print(array[i][j]+" ");
			}
			System.out.println();
		}
		System.out.println();
	}

}
