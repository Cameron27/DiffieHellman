class Functions {
    /**
     * Create a hex string from an array of bytes.
     *
     * @param input  Bytes to use to create the hex string.
     * @param length Number of bytes from the input to use.
     * @return The input byte array as a hex string.
     */
    static String bytesToHex(byte[] input, int length) {
        StringBuilder keyHex = new StringBuilder();
        for (int i = 0; i < Math.min(input.length, length); i++) {
            keyHex.append(String.format("%02x ", input[i]));
        }
        return keyHex.toString();
    }

    /**
     * Create string from an array of bytes where non printable character are replaced with ..
     *
     * @param input  Bytes to use to create the string.
     * @param length Number of bytes from the input to use.
     * @return The input byte array as a string.
     */
    static String bytesToString(byte[] input, int length) {
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < Math.min(input.length, length); i++) {
            int b = (int) input[i];

            if (b >= 32 && b <= 126) {
                string.append((char) ((byte) b));
            } else {
                string.append('.');
            }
        }
        return string.toString();
    }
}
