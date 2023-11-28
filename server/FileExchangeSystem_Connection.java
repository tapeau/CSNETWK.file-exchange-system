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
                
                // Evaluate client input
                switch (cCommand) {
                    case 'D' -> {
                        // TODO: Send file directory to client
                    }
                    
                    case 'F' -> {
                        // TODO: Send requested file to client
                    }
                    
                    case 'L' -> {
                        // TODO: Close connection to client
                    }
                    
                    case 'R' -> {
                        try {
                            // Get given alias
                            String sAliasGiven = sMessage.substring(1);

                            // Find local file "alias.txt" which contains all aliases already in use
                            File fAliasList = new File("alias.txt");
                            Scanner scFile = new Scanner(fAliasList);
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
                            // GUIDE:
                            // S = Success
                            // F = Fail
                            // T = Taken (Alias is taken)
                            if (bAliasFree == false) {
                                dosOutput.writeChar('T');
                            } else {
                                dosOutput.writeChar('S');
                            }
                        } catch (FileNotFoundException e) {
                            System.err.println("ERROR: File \"alias.txt\" not found.");
                            dosOutput.writeChar('F');
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