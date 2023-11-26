// Machine Project Group 10
// TAPIA, John Lorenzo N.
// ARGAMOSA, Daniel Cedric S.
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.Scanner;
import java.util.InputMismatchException;

public class FileExchangeSystem_Server {
    // Function to prompt user for serverport number
    public static int getPort() {
        System.out.print("Please enter a port number to be used by the server: ");
        int nInput = 0;
        
        // Scan for input and validate if it is an integer
        do {
            Scanner scInput = new Scanner(System.in);
            
            try {
                if (scInput.hasNextInt()) {
                    nInput = scInput.nextInt();
                } else {
                    throw new InputMismatchException("\nInvalid input. Please enter a positive non-zero integer: ");
                }
                
                // Additional check to ensure number is not negative or zero
                if (nInput <= 0) {
                    throw new InputMismatchException("\nInvalid input. Please enter a positive non-zero integer: ");
                }
            } catch (InputMismatchException e) {
                System.err.println("ERROR: " + e.getMessage());
                nInput = 0;
            }
        } while (nInput <= 0);
        
        // Pass value of input
        System.out.println(); // for formatting
        return nInput;
    }
    
    // Main function
    public static void main(String[] args) {
        // Initial interface
        System.out.println("------------------------------");
        System.out.println("FILE EXCHANGE SYSTEM - SERVER");
        System.out.println("------------------------------\n");
        System.out.println("Welcome!");
        
        // Declare variables
        ServerSocket ssServer;
        Socket socServer;
        int nServerPort = getPort();
        
        // TODO: Actual server functionalities
        
        
        
        // BELOW: Code from Lab Activity 4 - to be used as base for actual project code
        String[] serverInputs = new String[2];
        DataOutputStream[] serverStreams = new DataOutputStream[2];
        int serverIndex = 0;
        

        try {
            // Establish server socket
            ssServer = new ServerSocket(nServerPort);

            // Wait for Client 1
            socServer = ssServer.accept();

            System.out.println("Server: New client connected: " + socServer.getRemoteSocketAddress());

            // Get output stream of new client and store to array
            serverStreams[serverIndex] = new DataOutputStream(socServer.getOutputStream());

            // Get message from client and store to array
            DataInputStream disReader = new DataInputStream(socServer.getInputStream());
            serverInputs[serverIndex] = disReader.readUTF();
            serverIndex++;

            // Wait for Client 2, then repeat procedures same as above
            socServer = ssServer.accept();

            System.out.println("Server: New client connected: " + socServer.getRemoteSocketAddress());

            serverStreams[serverIndex] = new DataOutputStream(socServer.getOutputStream());

            disReader = new DataInputStream(socServer.getInputStream());
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
                socServer.close();
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