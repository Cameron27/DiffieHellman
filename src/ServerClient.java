import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Random;
import java.util.Scanner;

abstract class ServerClient {
    Socket socket;
    DataOutputStream out;
    DataInputStream in;
    int KEY_SIZE;
    BigInteger prime;
    BigInteger generator;
    private int writeKeyPointer = 0;
    private int readKeyPointer = 0;
    byte[] key;

    // run app
    void run() {
        setup();

        System.out.println("Exchanging key");
        try {
            exchangeKey();
        } catch (IOException e) {
            System.out.println("Key exchange failed");
            Runtime.getRuntime().exit(1);
        }


        System.out.println("Ready to chat");

        startChat();

        try {
            socket.close();
        } catch (IOException ignored) {
        }

    }

    /**
     * Setup the socket, out and in.
     */
    abstract void setup();

    /**
     * Exchange keys with the other server / client using Diffie Hellman.
     */
    private void exchangeKey() throws IOException {
        getPrimeAndGenerator();

        Random rnd = new SecureRandom();

        // pick a secret from
        BigInteger secretExponent;
        do {
            secretExponent = new BigInteger(KEY_SIZE * 8, rnd);
        } while (prime.subtract(secretExponent).signum() != 1 || secretExponent.equals(BigInteger.ZERO));

        // calculate exponential
        BigInteger numberToSend = generator.modPow(secretExponent, prime);

        // get bit to send and pad them so it is a predictable length
        byte[] bytesToSend = numberToSend.toByteArray();
        byte[] bytesToSendPadded = new byte[KEY_SIZE + 1];
        System.arraycopy(bytesToSend, 0, bytesToSendPadded, bytesToSendPadded.length - bytesToSend.length, bytesToSend.length);

        // send data and receive data
        byte[] bytesReceived = new byte[KEY_SIZE + 1];

        out.write(bytesToSendPadded);
        out.flush();

        in.readFully(bytesReceived);


        BigInteger numberReceived = new BigInteger(bytesReceived);
        BigInteger keyInt = numberReceived.modPow(secretExponent, prime);

        byte[] keyBytes = keyInt.toByteArray();

        // calculate start point as the first byte may need to be skipped as BigInteger.toByteArray used two's two's-complement
        int startPoint = keyBytes.length <= KEY_SIZE ? 0 : 1;
        key = new byte[KEY_SIZE];
        System.arraycopy(keyBytes, startPoint, key, key.length - (keyBytes.length - startPoint), keyBytes.length - startPoint);

//        System.out.println(Functions.bytesToHex(key, KEY_SIZE));
    }

    abstract void getPrimeAndGenerator() throws IOException;

    /**
     * Starts the server / client listening for data on the socket to display and for input on stdin to send.
     */
    private void startChat() {
        // create thread to write stdin to socket
        Thread writeThread = new Thread(() -> {
            Scanner stdin = new Scanner(System.in);
            String line;
            // read lines continuously
            while ((line = stdin.nextLine()) != null) {

                try {
                    // end when stop is sent
                    if (line.equals("stop")) {
                        socket.close();
                        break;
                    }

                    // encrypt and write
                    out.write(encrypt(line + "\n"));
                    out.flush();
                } catch (IOException ignored) {
                }
            }
        });
        writeThread.start();

        byte[] buff = new byte[16384];
        int length;
        // read from socket continuously
        while (true) {
            try {
                if ((length = in.read(buff)) == -1)
                    break;

                // decrypt data
                String line = decrypt(buff, length);

                // end when stop is sent
                if (line.equals("stop")) {
                    break;
                }

                // print encrypted message
//                System.out.println(Functions.bytesToHex(buff, length));
//                System.out.println(Functions.bytesToString(buff, length));

                // print decrypted message
//                System.out.println(Functions.bytesToHex(line.getBytes(), length));
                System.out.print(line);
            } catch (IOException ignored) {
                break;
            }
        }

        System.out.println("Session ended");
    }

    /**
     * Decrypt an array of bytes.
     *
     * @param input  Array of bytes to encrypt.
     * @param length Number of bytes from the array to encrypt.
     * @return The decrypted string.
     */
    private String decrypt(byte[] input, int length) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < Math.min(input.length, length); i++) {
            byte j = (byte) (input[i] ^ key[readKeyPointer]);
            output.append((char) j);

            // increment pointer
            readKeyPointer++;
            if (readKeyPointer >= KEY_SIZE) {
                readKeyPointer = 0;
            }
        }

        return output.toString();
    }

    /**
     * Encrypt a string.
     *
     * @param input The string to encrypt.
     * @return An array of encrypted bytes.
     */
    private byte[] encrypt(String input) {
        byte[] output = input.getBytes();
        for (int i = 0; i < output.length; i++) {
            output[i] = (byte) (output[i] ^ key[writeKeyPointer]);

            // increment pointer
            writeKeyPointer++;
            if (writeKeyPointer >= KEY_SIZE) {
                writeKeyPointer = 0;
            }
        }

        return output;
    }
}

