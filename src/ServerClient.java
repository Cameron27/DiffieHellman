import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

abstract class ServerClient {
    Socket socket;
    DataOutputStream out;
    DataInputStream in;
    final int KEY_SIZE = 128;
    private int writeKeyPointer = 0;
    private int readKeyPointer = 0;
    byte[] key = new byte[KEY_SIZE];

    // run app
    void run() {
        setup();

        exchangeKey();

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
     * Exchange keys with the other server / client.
     */
    abstract void exchangeKey();

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

        byte[] buff = new byte[256];
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
                System.out.println(Functions.bytesToHex(buff, length));
                System.out.println(Functions.bytesToString(buff, length));

                // print decrypted message
                System.out.println(Functions.bytesToHex(line.getBytes(), length));
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

