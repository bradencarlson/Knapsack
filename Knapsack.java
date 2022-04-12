import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.lang.IllegalArgumentException;

class DecryptThread implements Callable<int []> {
	private BigInteger encrypted;
	private BigInteger [] privateS;
	private BigInteger inverse;
	private BigInteger modulus;
	
	public DecryptThread(BigInteger encrypted, BigInteger [] privateS, BigInteger inverse, BigInteger modulus) {
		this.encrypted = encrypted;
		this.privateS = privateS;
		this.inverse = inverse;
		this.modulus = modulus;
	}
	
	@Override
	public int[] call() throws Exception {
		// TODO Auto-generated method stub
		return Knapsack.decrypt(encrypted, privateS, inverse, modulus);
	}
	
}

class EncryptThread implements Callable<BigInteger> {
	private BigInteger [] publicS;
	private byte [] messagePart;
	
	public EncryptThread(byte [] messagePart, BigInteger [] publicS) {
		this.messagePart = messagePart;
		this.publicS = publicS;
	}
	
	@Override
	public BigInteger call() throws Exception {
		// TODO Auto-generated method stub
		return Knapsack.encrypt(messagePart, publicS);
	}
	
}

public class Knapsack {

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

		// split the message up into blocks of appropriate size
		byte[][] message = split("message.txt",1);

		// create the public sequence based on the parameters above
		BigInteger[] publicS = new BigInteger[privateS.length];
		publicS = publicSequence(privateS, modulus, multiplier);

		// create the encrypted array
		BigInteger[] encrypted = new BigInteger[message.length];

		// go through the message, and encrypt each block
		// this can be parallelized eventually, possibly completed
//		for(int i = 0; i< message.length; i++) {
//			encrypted[i] = encrypt(message[i],publicS);
//		}
		//start of encryption threading
		ExecutorService service1 = Executors.newFixedThreadPool(message.length);
		List<Future> allFutures1 = new ArrayList<>();
		for(int i = 0; i<message.length; i++) {
			Future<BigInteger> future = service1.submit(new EncryptThread(message[i], publicS));
			allFutures1.add(future);
		}
		
		boolean alive = true;
		System.out.println("Encryption Started");
		
		//Makes sure all the threads get done before continuing, could possibly remove because of the .get() method that futures support
		//which might make the program wait until it has something to actually get. But alive is a good safety net. 
		while(alive){
			alive = false;
			for(int k = 0; k < allFutures1.size() && alive == false; k++) {
				if(!allFutures1.get(k).isDone()) {
					alive = true;
				}
			}
		}
		System.out.println("Encryption Finished");
		for(int i = 0; i<encrypted.length; i++) {
			try {
				encrypted[i] = (BigInteger) (allFutures1.get(i)).get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		service1.shutdown();

		// create the decrypted array, and go through the
		// encrypted sequence and decrypt each block of the
		// message, this can be parallelized eventually, possibly completed
//		int[][] decrypted = new int[message.length][];
//		for(int i = 0; i< encrypted.length; i++) {
//			decrypted[i] = decrypt(encrypted[i],privateS, inverse,modulus);
//		}
		//Start of decryption threading
		int [][] decrypted = new int[message.length][];
		ExecutorService service2 = Executors.newFixedThreadPool(encrypted.length);
		List<Future> allFutures2 = new ArrayList<>();
		for(int i = 0; i<encrypted.length; i++) {
			Future<int []> future = service2.submit(new DecryptThread(encrypted[i],privateS, inverse,modulus));
			allFutures2.add(future);
		}

		alive = true;
		System.out.println("Decryption Started");
		while(alive){
			alive = false;
			for(int k = 0; k < allFutures2.size() && alive == false; k++) {
				if(!allFutures2.get(k).isDone()) {
					alive = true;
				}
			}
		}
		System.out.println("Decryption Finished");
		for(int i = 0; i<encrypted.length; i++) {
			try {
				decrypted[i] = (int[]) (allFutures2.get(i)).get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		service2.shutdown();

		// try to write the decrypted message to a file
		try {
			FileOutputStream fw = new FileOutputStream("output.txt");
			for(int i = 0; i< decrypted.length; i++) {
				for(int j = 0; j<decrypted[i].length; j++) {
					fw.write(decrypted[i][j]);
				}
			}
			fw.close();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

	}

	/** Make Modulus and Multiplier method.  Takes a private sequence,
	*   which should be super increasing, and creates a modulus that is
	*   as least twice as big as the last term of the sequence, and a
	*   multiplier that is relatively prime to the modulus, and returns
	*   these numbers as an array.
	*   @param b The private sequence
	*   @return A BigInteger array that contains the modulus and the
	*   multiplier.
	*/
	public static BigInteger[] makeMnM(BigInteger[] b) {
		BigInteger[] arr = new BigInteger[2];
		Random rand = new Random();
		BigInteger lastDoubled = (b[b.length-1]).multiply(BigInteger.valueOf(2));
		BigInteger mult = new BigInteger(lastDoubled.bitLength()+99, rand);
		arr[0] = mult;
		boolean bool = false;
		BigInteger big = b[b.length-1].subtract(b[0]);
		int len = b[b.length-1].bitLength();
		do {
			BigInteger modulus = new BigInteger(len, rand);
			if(modulus.compareTo(b[0])<0) {
				modulus = modulus.add(b[0]);
			}
			if(modulus.compareTo(b[b.length-1]) >= 0 ) {
				modulus = modulus.mod(big);
			}
			if(mult.gcd(modulus).equals(BigInteger.ONE)) {
				arr[1] = modulus;
				bool = true;
			}
		} while(!bool);

		return arr;

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

		int count = publicSequence.length-1;
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


			encryptedMessage = encryptedMessage.add(publicSequence[count--].multiply(
				new BigInteger(Integer.toString(x1))));
			encryptedMessage = encryptedMessage.add(publicSequence[count--].multiply(
				new BigInteger(Integer.toString(x2))));
			encryptedMessage = encryptedMessage.add(publicSequence[count--].multiply(
				new BigInteger(Integer.toString(x3))));
			encryptedMessage = encryptedMessage.add(publicSequence[count--].multiply(
				new BigInteger(Integer.toString(x4))));
			encryptedMessage = encryptedMessage.add(publicSequence[count--].multiply(
				new BigInteger(Integer.toString(x5))));
			encryptedMessage = encryptedMessage.add(publicSequence[count--].multiply(
				new BigInteger(Integer.toString(x6))));
			encryptedMessage = encryptedMessage.add(publicSequence[count--].multiply(
				new BigInteger(Integer.toString(x7))));
			encryptedMessage = encryptedMessage.add(publicSequence[count--].multiply(
				new BigInteger(Integer.toString(x8))));
		}
		return encryptedMessage;
	}

	/** Takes a sigle block of an encrypted message, and decrypts it,
	*   by using the private sequence that is provided, to solve the
	*   Knapsack problem of finding the original bits of the message.
	*   This method retuns a int array that will contain the original
	*   message. This method does not return a byte[] becuase of sign
	*   issues.  The fact that this method returns an int array means
	*   that to write the decrypted message to a file, we cannot use
	*   the write(byte[]) method of the FileOutputStream, but we have
	*   to use the write(int b) method of the FileOutputStream.
	*   @param message the BigInteger corresponding to the encrypted block
	*   @param privateSequence The private sequence
	*   @return a byte array containing the original message
	*/
	public static int[] decrypt(BigInteger message, BigInteger[] privateSequence,
		BigInteger multiplier, BigInteger modulus) {
			message = (message.multiply(multiplier)).mod(modulus);

			int position = privateSequence.length-1;
			boolean[] bits = new boolean[8];

			int[] decrypted = new int[privateSequence.length/8];
			int count = 0;

			while (message.compareTo(BigInteger.ZERO)>0) {
				for(int i = 0; i< 8; i++) {
					//System.out.println(position);
					if (message.compareTo(privateSequence[position])>=0) {
						bits[i] = true;
						message = message.subtract(privateSequence[position]);
						position--;
					} else {
						bits[i] = false;
						position--;
					}
				}
				int myByte = toByte(bits);
				decrypted[count++] = myByte;
			}

			return decrypted;
	}

	/** helper method that takes a boolean array and turns in into an
	*   integer.  This is used by the decrypt method to turn binary
	*   representations of messages into integers that can be written
	*   into a file.
	*   @param array the array to convert into an integer
	*   @return the integer represented by the array
	*   @throws IllegalArgumentException if the length of the boolean array
	*   is not 8.
	*/
	public static int toByte(boolean[] array) throws IllegalArgumentException {
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
					newByte = newByte | bits[i];

				}
			}

			return newByte;
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
			System.out.print(array[i]+" ");
		}
		System.out.println();
	}

	public static void printAsTable(byte[] array1, int[] array2) {
		for(int i = 0; i< array1.length; i++) {
			System.out.println(array1[i]+"\t"+array2[i]);
		}
		System.out.println();
	}

	public static void printArray(byte[] array) {
		for(int i = 0; i< array.length; i++) {
			System.out.print(array[i]+" ");
		}
		System.out.println();
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

	public static void printDoubleArray(int[][] array) {
		for(int i = 0; i< array.length; i++) {
			for(int j = 0; j<array[i].length; j++) {
				System.out.print(array[i][j]+" ");
			}
			System.out.println();
		}
		System.out.println();
	}

}
