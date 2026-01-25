package dev.spacetivity.tobi.database.api.connection.impl.sql;

import java.util.UUID;

/**
 * Utility class for converting UUIDs to/from binary format (BINARY(16)).
 * UUIDs are stored as 16 bytes in the database for efficiency.
 */
public final class UuidUtils {

    /**
     * Converts a UUID to a byte array (16 bytes).
     * The UUID is converted by extracting the most significant bits and least significant bits
     * and storing them as a 16-byte array in big-endian format.
     *
     * @param uuid the UUID to convert, can be null
     * @return a 16-byte array representing the UUID, or null if uuid is null
     */
    public static byte[] uuidToBytes(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        long mostSigBits = uuid.getMostSignificantBits();
        long leastSigBits = uuid.getLeastSignificantBits();
        byte[] bytes = new byte[16];
        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) (mostSigBits >>> (8 * (7 - i)));
        }
        for (int i = 8; i < 16; i++) {
            bytes[i] = (byte) (leastSigBits >>> (8 * (7 - (i - 8))));
        }
        return bytes;
    }

    /**
     * Converts a byte array (16 bytes) to a UUID.
     * The byte array must be exactly 16 bytes long and in big-endian format.
     *
     * @param bytes the byte array to convert, must be exactly 16 bytes, can be null
     * @return a UUID representing the bytes, or null if bytes is null or not 16 bytes
     */
    public static UUID bytesToUuid(byte[] bytes) {
        if (bytes == null || bytes.length != 16) {
            return null;
        }
        long mostSigBits = 0;
        long leastSigBits = 0;
        for (int i = 0; i < 8; i++) {
            mostSigBits = (mostSigBits << 8) | (bytes[i] & 0xff);
        }
        for (int i = 8; i < 16; i++) {
            leastSigBits = (leastSigBits << 8) | (bytes[i] & 0xff);
        }
        return new UUID(mostSigBits, leastSigBits);
    }
}
