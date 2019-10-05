import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Scanner;

class Client extends ServerClient {
    public static void main(String[] args) {
        new Client().run();
    }

    @Override
    void setup() {
        Scanner stdin = new Scanner(System.in);

        boolean done = false;
        while (!done) {
            // get address
            System.out.print("Please enter a server address to connect to (localhost): ");
            String address = stdin.nextLine();

            // set default localhost
            if (address.equals("")) {
                address = "localhost";
            }


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


            // try connect to address
            try {
                socket = new Socket(address, port);
            } catch (IOException | IllegalArgumentException e) {
                System.out.println("Failed to connect to that address on that port");

                continue;
            }
            done = true;
        }

        // get input and output streams
        try {
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.out.println("Failed to get connection in and out");
            Runtime.getRuntime().exit(1);
        }
    }

    @Override
    void getPrimeAndGenerator() throws IOException {
        // get prime number
        KEY_SIZE = in.readInt() - 1;
        byte[] primeBytes = new byte[KEY_SIZE + 1];
        in.readFully(primeBytes);
        prime = new BigInteger(primeBytes);

        // get generator
        int generatorSize = in.readInt();
        byte[] generatorBytes = new byte[generatorSize];
        in.readFully(generatorBytes);
        generator = new BigInteger(generatorBytes);
    }
}
