// Machine Project Group 10
// TAPIA, John Lorenzo N.
// ARGAMOSA, Daniel Cedric S.

package FileExchangeSystem_Server;

import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
                nInput = -1;
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
        int nServerPort = getPort();
        String sServerAdd;
        ServerSocket ssServer;
        ArrayList<String> sAliasList = new ArrayList<>();
        DateTimeFormatter dtfTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // Date/Time formatter for logging purposes
        
        // Establish server socket
        try {
            ssServer = new ServerSocket(nServerPort);
            sServerAdd = ssServer.getInetAddress().getHostAddress();
            System.out.println("[" + LocalDateTime.now().format(dtfTimeFormat) + "] Server " + sServerAdd + ":" + nServerPort + " - Listening to port " + nServerPort + ".");
            
            // Listen for client connections
            while (true) {
                // Accept incoming client connection
                Socket socEndpoint = ssServer.accept();
                System.out.println("[" + LocalDateTime.now().format(dtfTimeFormat) + "] Server " + sServerAdd + ":" + nServerPort + " - Client at " + socEndpoint.getRemoteSocketAddress() + " has connected.");
                
                // Place client connection into a separate File Exchange System Connection thread
                FileExchangeSystem_Connection fescConnection = new FileExchangeSystem_Connection(socEndpoint, sServerAdd, nServerPort, sAliasList);
                
                // Start the File Exchange System Connection thread to start client-server interactions
                fescConnection.start();
            }
        } catch (IOException e) {
            System.err.println("ERROR: " + e.getMessage());
        }
        
        // Termination message
        System.out.println("Program terminated.");
    }
}