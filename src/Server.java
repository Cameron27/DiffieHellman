import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.util.Scanner;

class Server extends ServerClient {
    public static void main(String[] args) {
        new Server().run();
    }

    @Override
    void setup() {
        ServerSocket serverSocket = null;
        Scanner stdin = new Scanner(System.in);

        // setup socket
        boolean done = false;
        while (!done) {
            // get port
            int port;
            System.out.print("Please enter a port to run server on (1000): ");
            String portString = stdin.nextLine();

            // set default port 1000
            if (portString.equals("")) {
                portString = "1000";
            }

            try {
                port = Integer.parseInt(portString);

                if (port < 0) {
                    throw new Exception();
                }
            } catch (Exception e) {
                System.out.println("Port must be a positive integer");
                continue;
            }


            // try open socket
            try {
                serverSocket = new ServerSocket(port);
            } catch (IOException | IllegalArgumentException e) {
                System.out.println("Failed to open socket on that port");
                continue;
            }
            done = true;
        }

        // listen for connection
        try {
            System.out.println("Waiting for connection");
            socket = serverSocket.accept();
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Accepting connection failed");
            Runtime.getRuntime().exit(1);
        }

        // get input and output streams
        try {
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.out.println("Failed to get connection input and output");
            Runtime.getRuntime().exit(1);
        }
    }

    @Override
    void getPrimeAndGenerator() throws IOException {
        // prime and generator are from RFC 3526 (https://www.rfc-editor.org/rfc/rfc3526.txt) 2048-bit MODP group
        KEY_SIZE = 2048 / 8;
        prime = new BigInteger("FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3DC2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F83655D23DCA3AD961C62F356208552BB9ED529077096966D670C354E4ABC9804F1746C08CA18217C32905E462E36CE3BE39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9DE2BCBF6955817183995497CEA956AE515D2261898FA051015728E5A8AACAA68FFFFFFFFFFFFFFFF", 16);
        generator = new BigInteger("2");

        // write prime and generator
        byte[] b = intToByteArray(prime.toByteArray().length);
        out.write(b);
        out.write(prime.toByteArray());
        out.write(intToByteArray(generator.toByteArray().length));
        out.write(generator.toByteArray());
        out.flush();
    }

    private byte[] intToByteArray(int i) {
        return new byte[]{(byte) (i >>> 24), (byte) (i >>> 16), (byte) (i >>> 8), (byte) (i)};
    }
}
