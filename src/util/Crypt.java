package util;

import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * Utility for several cryptographic purposes.
 * 
 * @author Ralph Broenink
 */
public class Crypt {

	/**
	 * Generates a MD5-hash for any given input.
	 * 
	 * @param input
	 *            The string to convert to MD5
	 * @return The MD5 hash of the input
	 */
	public static String generateMD5(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.reset();
			md.update(input.getBytes());
			byte[] digest = md.digest();
			BigInteger bigInt = new BigInteger(1, digest);
			String result = bigInt.toString(16);
			while (result.length() < 32) {
				result = "0" + result;
			}
			return result;
		} catch (Exception e) {
			return null;
		}
	}
}
