import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
            System.out.println("Failed to get connection in and out");
            Runtime.getRuntime().exit(1);
        }
    }

    @Override
    void exchangeKey() {
        // read key from client
        try {
            in.read(key);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // print key
        System.out.println("The chosen key is:");
        System.out.println(Functions.bytesToHex(key, key.length));
    }
}
