// Machine Project Group 10
// TAPIA, John Lorenzo N.
// ARGAMOSA, Daniel Cedric S.
import java.net.*;
import java.io.*;

public class FileExchangeSystem_Server {
    public static void main(String[] args) {
        int nPort = Integer.parseInt(args[0]);
        System.out.println("Server: Listening on port " + args[0] + "...");
        ServerSocket serverSocket;
        Socket serverEndpoint;
        String[] serverInputs = new String[2];
        DataOutputStream[] serverStreams = new DataOutputStream[2];
        int serverIndex = 0;

        try {
            // Establish server socket
            serverSocket = new ServerSocket(nPort);

            // Wait for Client 1
            serverEndpoint = serverSocket.accept();

            System.out.println("Server: New client connected: " + serverEndpoint.getRemoteSocketAddress());

            // Get output stream of new client and store to array
            serverStreams[serverIndex] = new DataOutputStream(serverEndpoint.getOutputStream());

            // Get message from client and store to array
            DataInputStream disReader = new DataInputStream(serverEndpoint.getInputStream());
            serverInputs[serverIndex] = disReader.readUTF();
            serverIndex++;

            // Wait for Client 2, then repeat procedures same as above
            serverEndpoint = serverSocket.accept();

            System.out.println("Server: New client connected: " + serverEndpoint.getRemoteSocketAddress());

            serverStreams[serverIndex] = new DataOutputStream(serverEndpoint.getOutputStream());

            disReader = new DataInputStream(serverEndpoint.getInputStream());
            serverInputs[serverIndex] = disReader.readUTF();
            serverIndex++;

            // Send messages when both clients have sent their messages
            if (serverInputs[0] != null && serverInputs[1] != null) {
                // Send message of Client 1 to Client 2
                serverStreams[1].writeUTF(serverInputs[0]);
                serverStreams[1].flush();

                // Send message of Client 2 to Client 1
                serverStreams[0].writeUTF(serverInputs[1]);
                serverStreams[0].flush();

                // Close the server
                serverEndpoint.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            System.out.println("Server: Connection is terminated.");
        }
    }
}