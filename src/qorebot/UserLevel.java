package qorebot;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the level of a user.
 * 
 * @author Ralph Broenink
 * 
 */
public enum UserLevel {
	/**
	 * Erroneous user level that should not be used anywhere. It does not
	 * represent an user state, it only tells us that something went
	 * dramatically wrong while loading the user level.
	 */
	UNKNOWN,

	/**
	 * User level for unidentified users.
	 */
	NONE,

	/**
	 * User level representing users that are identified, but aren't known in
	 * the current channel.
	 */
	IDENTIFIED,

	/**
	 * User level representing users that have some kind of affiliation with the
	 * current channel.
	 */
	USER,

	/**
	 * User level representing users that have full usage rights in the current
	 * channel, but that can't administer anything.
	 */
	OPERATOR,

	/**
	 * User level representing users that may manage the current channel.
	 */
	ADMINISTRATOR,

	/**
	 * User level representing a bot owner. May perform cross-channel
	 * operations.
	 */
	OWNER;

	private static final Map<Integer, UserLevel> intMap;
	static {
		Map<Integer, UserLevel> tmp = new HashMap<Integer, UserLevel>();
		tmp.put(-1, UserLevel.UNKNOWN);
		tmp.put(0, UserLevel.NONE);
		tmp.put(1, UserLevel.IDENTIFIED);
		tmp.put(2, UserLevel.USER);
		tmp.put(3, UserLevel.OPERATOR);
		tmp.put(4, UserLevel.ADMINISTRATOR);
		tmp.put(5, UserLevel.OWNER);
		intMap = Collections.unmodifiableMap(tmp);
	}

	/**
	 * Retrieves the user level based on a given integer. Every integer is
	 * guaranteed to always return the same user level, although the ordering of
	 * integers may differ from the ordering of the enums.
	 * 
	 * @param userlevel
	 *            The number of a user level.
	 * @return A UserLevel belonging to the userLevel, or null if invalid.
	 */
	public static UserLevel fromInteger(int userlevel) {
		return UserLevel.intMap.get(userlevel);
	}

	/**
	 * Retrieves the integer belonging to the current UserLevel. Its value is
	 * guaranteed to return the same UserLevel when put in
	 * {@link UserLevel#fromInteger(int)}
	 * 
	 * @return A number representing the current UserLevel.
	 */
	public int toInteger() {
		for (Map.Entry<Integer, UserLevel> entry : UserLevel.intMap.entrySet()) {
			if (entry.getValue() == this) {
				return entry.getKey();
			}
		}
		return -1;
	}

	/**
	 * Returns the maximum of the two given UserLevels.
	 * 
	 * @param u1
	 *            The first UserLevel
	 * @param u2
	 *            The second UserLevel
	 * @return u1 if its {@link UserLevel#compareTo(UserLevel)} method returns a
	 *         value larger than 0, u2 otherwise
	 */
	public static UserLevel max(UserLevel u1, UserLevel u2) {
		if (u1.compareTo(u2) > 0) { // u1 > u2
			return u1;
		} else {
			return u2;
		}
	}
	
	@Override
	public String toString() {
		return super.toString().toLowerCase();
	}
}
