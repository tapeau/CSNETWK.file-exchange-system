// Machine Project Group 10
// TAPIA, John Lorenzo N.
// ARGAMOSA, Daniel Cedric S.

package FileExchangeSystem_Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileExchangeSystem_Client {
    // Function to display Help page - accessed with '/?' command
    public static void displayHelp() {
        System.out.println(""
                + "/?\t\t\t\t\t\t\tDisplay information about all valid commands\n"
                + "/dir\t\t\t\t\t\tDisplay a directory of the files in the File Exchange server\n"
                + "/exit\t\t\t\t\t\tExit the program - disconnects from any connected File Exchange server\n"
                + "/get [filename]\t\t\t\tDownload a file of name [filename] from the File Exchange server\n"
                + "/join [ip_address] [port]\tConnect to the File Exchange server of IP address [ip_address] and port number [port]\n"
                + "/leave\t\t\t\t\t\tDisconnect from the connected File Exchange server\n"
                + "/register [alias]\t\t\tRegister to the connected File Exchange server using the handle [alias] (must be unique)\n"
                + "/rejoin\t\t\t\t\t\tReconnect to the previously-connected File Exchange server\n"
                + "/store [filename]\t\t\tUpload a file of name [filename] to the File Exchange server - [filename] must exist inside the client's directory\n");
    }
    
    // Function to check whether a string is a valid IPv4 address
    public static boolean isValidIPAddress(String ipAddress) {
        // Define the regex pattern for a valid IPv4 address
        String ipPattern = "^(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\\."
                        + "(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\\."
                        + "(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\\."
                        + "(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)$";

        // Compile the pattern
        Pattern pattern = Pattern.compile(ipPattern);

        // Match the given IP address with the pattern
        Matcher matcher = pattern.matcher(ipAddress);

        // Return true if it matches or if the string is "localhost", false otherwise
        return ipAddress.equals("localhost") || matcher.matches();
    }
    
    // Function to connect a socket to a server given IPv4 address and port number
    public static void connectSocket(Socket soc, DataInputStream in, DataOutputStream out, String ipAddress, int port) {
        try {
            System.out.println("Connecting to server at " + soc.getRemoteSocketAddress());
            
            // Connect socket
            soc.connect(new InetSocketAddress(ipAddress, port), 6000); // Timeout after 6000 ms (6 seconds)
            
            // Connect socket streams
            in = new DataInputStream(soc.getInputStream());
            out = new DataOutputStream(soc.getOutputStream());
            
            System.out.println("Connected to server at " + soc.getRemoteSocketAddress());
        } catch (SocketTimeoutException e) {
            System.err.println("ERROR: Connection timed out. Check server availability and try again.");
        } catch (IOException e) {
            if (e instanceof java.net.ConnectException) {
                System.err.println("ERROR: Could not connect to the server. Make sure the server is running and try again.");
            } else {
                System.err.println("I/O ERROR: " + e.getMessage());
            }
        }
    }
    
    // Main function
    public static void main(String[] args) {
        // Initial interface
        System.out.println("------------------------------");
        System.out.println("FILE EXCHANGE SYSTEM");
        System.out.println("------------------------------\n");
        System.out.println("Welcome!");
        System.out.println("Please connect to a server to start...\n");
        System.out.println("To get a list of all recognized commands, enter \"/?\"");
        
        // Declare variables
        String sServerAdd = "";
        String sClientAlias = "";
        int nServerPort = -1;
        boolean bContinue = true;
        Socket socEndpoint = new Socket();
        DataInputStream disInput = null;
        DataOutputStream dosOutput = null;
        
        /*
        -----------------------------------------
        CLIENT-SERVER MESSAGE GUIDE
        
        MESSAGE             MEANING
        D                   Get directory
        F                   Get file
        L                   Close connection
        R                   Register
        S                   Store file
        -----------------------------------------
        */

        // Main program loop
        do {
            System.out.println();
            
            // Scan for input
            Scanner scInput = new Scanner(System.in);
            scInput.useDelimiter(" ");
            System.out.println();

            // Evaluate input
            String sCommand = scInput.next();
            switch (sCommand) {
                // '/?' command
                case "/?" -> displayHelp();
                
                // '/dir' command
                case "/dir" -> {
                    // Check if (1) the client is connected to a server, (2) if the client is registered to the server
                    if (socEndpoint.isConnected() == false) {
                        System.out.println("ERROR: You are not connected to any File Exchange server.");
                    } else if (sClientAlias.isEmpty()) {
                        System.out.println("ERROR: You are not yet registered to this File Exchange server.");
                    } else {
                        try {
                            // Send request to server for its file directory
                            dosOutput.writeUTF("D");
                            
                            // Get the first response of server, which for this case should be an integer
                            // indicating the amount of files in the server
                            int nFileAmount = disInput.readInt();
                            
                            // Evaluate first response
                            if (nFileAmount == 0) {
                                System.out.println("Server directory is currently empty.");
                            } else {
                                System.out.println("List of current files in the server:");
                                // Print each file name received from the server
                                // up until nFirstResponse, which should contain the number of files in the server
                                for (int i = 0; i < nFileAmount; i++) {
                                    String sResponse = disInput.readUTF();
                                    
                                    // Check if the server sent the fail code, otherwise print the file name
                                    if (sResponse.equals("*")) {
                                        System.out.println("ERROR: An error occurred in the server.");
                                        i = nFileAmount; // Terminate loop
                                    } else {
                                        System.out.println(sResponse);
                                    }
                                }
                            }
                        } catch (IOException e) {
                            System.err.println("I/O ERROR: " + e.getMessage());
                        }
                    }
                }
                
                // '/exit' command
                case "/exit" -> {
                    // Halt continuation of program
                    bContinue = false;
                     
                    // Check if the client is connected to any server
                    if (socEndpoint.isConnected()) {
                        // Execute client disconnection protocol
                        try {
                            // Notify server that client will be disconnecting
                            dosOutput.writeUTF("L");
                            
                            // Close the connection
                            socEndpoint.close();
                            disInput = null;
                            dosOutput = null;
                            
                            // Reset client alias
                            sClientAlias = "";
                            
                            System.out.println("Disconnected from server at " + sServerAdd + ":" + nServerPort);
                        } catch (IOException e) {
                            System.err.println("I/O ERROR: " + e.getMessage());
                        }
                    }
                }
                
                // '/get' command
                case "/get" -> {
                    // Check if (1) the client is connected to a server, (2) the client is registered to the server
                    if (socEndpoint.isConnected() == false) {
                        System.out.println("ERROR: You are not connected to any File Exchange server.");
                    } else if (sClientAlias.isEmpty()) {
                        System.out.println("ERROR: You are not yet registered to this File Exchange server.");
                    } else {
                        // TODO: Get file from server
                    }
                }
                
                // '/join' command
                case "/join" -> {
                    // Check if (1) the client is already connected to a server, (2) the user entered an IP address, and (3) the user entered a port number
                    if (socEndpoint.isConnected()) {
                        System.out.println("ERROR: You are already connected to a File Exchange server.");
                    } else if (scInput.hasNext() == false) {
                        System.out.println("ERROR: You have not entered any IP address\n"
                                + "Command format:\t/join [ip_address] [port]");
                    } else if (scInput.hasNext() == false) {
                        System.out.println("ERROR: You have not entered any port number\n"
                                + "Command format:\t/join [ip_address] [port]");
                    } else {
                        // Set server IP address
                        sServerAdd = scInput.next();

                        // Additional check if address is valid
                        if (isValidIPAddress(sServerAdd) == false) {
                            System.out.println("ERROR: You entered an invalid IPv4 address.");
                            sServerAdd = "";
                        }

                        // Set server port
                        // Try-catch system for port number validation
                        try {
                            // Attempt to parse the string as an integer
                            nServerPort = Integer.parseInt(scInput.next());
                        } catch (NumberFormatException e) {
                            System.out.println("ERROR: You entered an invalid server port number.");
                            nServerPort = -1;
                        }

                        // Connect to the server if all inputs are valid
                        if (sServerAdd.isEmpty() == false && nServerPort != -1) {
                            connectSocket(socEndpoint, disInput, dosOutput, sServerAdd, nServerPort);
                        }
                    }
                }
                
                // '/leave' command
                case "/leave" -> {
                    // Check if the client is not connected to any server
                    if (socEndpoint.isConnected() == false) {
                        System.out.println("ERROR: You are not connected to any File Exchange server.");
                    } else {
                        try {
                            // Notify server that client will be disconnecting
                            dosOutput.writeUTF("L");
                            
                            // Close the connection
                            socEndpoint.close();
                            disInput = null;
                            dosOutput = null;
                            
                            // Reset client alias
                            sClientAlias = "";
                            
                            System.out.println("Disconnected from server at " + sServerAdd + ":" + nServerPort);
                        } catch (IOException e) {
                            System.err.println("I/O ERROR: " + e.getMessage());
                        }
                    }
                }
                
                // '/register' command
                case "/register" -> {
                    // Check if (1) the client is connected to a server, (2) the client is already registered to the server, and (3) the user entered an alias
                    if (socEndpoint.isConnected() == false) {
                        System.out.println("ERROR: You are not connected to any File Exchange server.");
                    } else if (sClientAlias.isEmpty() == false) {
                        System.out.println("ERROR: You are already registered to this File Exchange server.");
                    } else if (scInput.hasNext() == false) {
                        System.out.println("ERROR: You have not entered any alias\n"
                                + "Command format:\t/register [alias]");
                    } else {
                        // Send alias to server and set client to be registered
                        sClientAlias = scInput.next();
                        
                        try {
                            dosOutput.writeUTF("R" + sClientAlias);
                            
                            // Get server response
                            char cResponse = disInput.readChar();
                            
                            // Evaluate server response
                            switch (cResponse) {
                                case '/' -> System.out.println("You have successfully registered to the server with the alias " + sClientAlias);
                                
                                case ':' -> {
                                    System.out.println("ERROR: The alias " + sClientAlias + " is already taken in this server.");
                                    sClientAlias = ""; // Reset client alias if it is taken
                                }
                                
                                case '*' -> {
                                    System.out.println("ERROR: An error occurred in the server.");
                                    sClientAlias = ""; // Reset client alias in the event of an error
                                }
                            }
                            
                        } catch (IOException e) {
                            System.err.println("ERROR: " + e.getMessage());
                            sClientAlias = ""; // Reset client alias in the event of an error
                        }
                    }
                }
                
                // '/rejoin' command
                case "/rejoin" -> {
                    // Check if (1) the client is already connected to a server, and (2) the client previously connected to a server
                    if (socEndpoint.isConnected()) {
                        System.out.println("ERROR: You are already connected to a File Exchange server.");
                    } else if (sServerAdd.isEmpty() || nServerPort == -1) {
                        System.out.println("ERROR: You have not connected to any File Exchange server during this session.");
                    } else {
                        // Connect to the previously-connected server
                        connectSocket(socEndpoint, disInput, dosOutput, sServerAdd, nServerPort);
                    }
                }
                
                // '/store' command
                case "/store" -> {
                    // Check if (1) the client is connected to a server, (2) the client is registered to the server
                    if (socEndpoint.isConnected() == false) {
                        System.out.println("ERROR: You are not connected to any File Exchange server.");
                    } else if (sClientAlias.isEmpty()) {
                        System.out.println("ERROR: You are not yet registered to this File Exchange server.");
                    } else {
                        // TODO: Send file to server
                    }
                }
                
                // For invalid commands
                default -> System.out.println("ERROR: \'" + sCommand + "\' is not recognized as a valid command.");
            }
        } while (bContinue);
        
        // Termination message
        System.out.println("Program terminated.");
    }
}