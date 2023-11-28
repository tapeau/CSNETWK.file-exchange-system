// Machine Project Group 10
// TAPIA, John Lorenzo N.
// ARGAMOSA, Daniel Cedric S.

package FileExchangeSystem_Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.Scanner;

public class FileExchangeSystem_Connection extends Thread {
    // Client socket of the connection
    private final Socket socEndpoint;
    
    // Constructor function
    public FileExchangeSystem_Connection(Socket soc) {
        this.socEndpoint = soc;
    }
    
    // Override run() function to implement intended file transfer functionalities
    @Override
    public void run() {
        // Declare variables
        String sMessage;
        char cCommand;
        boolean bContinue = true;
        DataInputStream disInput;
        DataOutputStream dosOutput;
        
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
        
        // Main server thread loop
        do {
            try {
                // Get respective input and output streams
                disInput = new DataInputStream(socEndpoint.getInputStream());
                dosOutput = new DataOutputStream(socEndpoint.getOutputStream());
                
                // Store message sent by client
                sMessage = disInput.readUTF();
                
                // Read command from message
                cCommand = sMessage.charAt(0);
                
                /*
                -----------------------------------------
                SERVER RESPONSE GUIDE

                RESPONSE            MEANING
                /                   Success
                *                   Fail or Error
                -----------------------------------------
                */
                
                // Evaluate client input
                switch (cCommand) {
                    case 'D' -> {
                        // Create a File object for the directory "./files" where the files of the server (that are available for exchange) are stored
                        File filDirectoryPath = new File("./files");
                        
                        // Get a list of all files inside directory "./files" and store in an array
                        File[] filDirectory = filDirectoryPath.listFiles();
                        
                        try {
                            // First send the amount of file names to be expected by the client
                            dosOutput.writeInt(filDirectory.length);

                            // If-statement to send file names only if the directory is not empty
                            if (filDirectory.length != 0) {
                                // Iterate through the list of files and folders
                                for (File file : filDirectory) {
                                    // Send each file name to the client
                                    dosOutput.writeUTF(file.getName());
                                }
                            }
                        } catch (IOException e) {
                            System.err.println("I/O ERROR: " + e.getMessage());
                            dosOutput.writeUTF("*");
                        }
                    }
                    
                    case 'F' -> {
                        // TODO: Send file to client
                    }
                    
                    case 'L' -> {
                        // Close connection with client
                        socEndpoint.close();
                        
                        // Halt continuation of server thread
                        bContinue = false;
                    }
                    
                    case 'R' -> {
                        try {
                            // Get given alias
                            String sAliasGiven = sMessage.substring(1);

                            // Find local file "alias.txt" which contains all aliases already in use
                            File filAliases = new File("alias.txt");
                            Scanner scFile = new Scanner(filAliases);
                            boolean bAliasFree = true;

                            // Scan "alias.txt"
                            while (bAliasFree && scFile.hasNextLine()) {
                                String sAliasTaken = scFile.nextLine();
                                
                                // Check if the alias given by the client is already in use or not
                                if (sAliasGiven.equals(sAliasTaken)) {
                                    bAliasFree = false;
                                }
                            }
                            
                            // If given alias is already in use, send fail message to client, otherwise send success message
                            // ':' = Taken (Alias is taken)
                            if (bAliasFree == false) {
                                dosOutput.writeChar(':');
                            } else {
                                dosOutput.writeChar('/');
                            }
                        } catch (FileNotFoundException e) {
                            System.err.println("ERROR: File \"alias.txt\" not found.");
                            dosOutput.writeChar('*');
                        }
                    }
                    
                    case 'S' -> {
                        // TODO: Read file sent by client and store into local directory
                    }
                }
                
            } catch (IOException e) {
                System.err.println("I/O ERROR: " + e.getMessage());
            }
        } while (bContinue);
    }
}