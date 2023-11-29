// Machine Project Group 10
// TAPIA, John Lorenzo N.
// ARGAMOSA, Daniel Cedric S.

package FileExchangeSystem_Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FileExchangeSystem_Connection extends Thread {
    // Client socket of the connection
    private final Socket socEndpoint;
    
    // Client alias of the connection
    private String sClientAlias = "";
    
    // Address of the server who owns the connection (for logging purposes)
    private final String sServerAdd;
    
    // ArrayList of strings containing aliases that are already in use (inherited from server)
    private final ArrayList<String> sAliasList;
    
    // Date/Time formatter for logging purposes
    private final DateTimeFormatter dtfTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Constructor function
    public FileExchangeSystem_Connection(Socket soc, String add, int port, ArrayList<String> arr) {
        this.socEndpoint = soc;
        this.sServerAdd = add + ":" + port;
        this.sAliasList = arr;
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
                            
                            System.out.println("[" + LocalDateTime.now().format(dtfTimeFormat) + "] Server " + sServerAdd + " - \"" + sClientAlias + "\" has requested for server file directory.");
                        } catch (IOException e) {
                            System.err.println("[" + LocalDateTime.now().format(dtfTimeFormat) + "] I/O ERROR: " + e.getMessage());
                            dosOutput.writeUTF("*");
                        }
                    }
                    
                    case 'F' -> {
                        try {
                            // Get requested file name
                            String sFileName = "./files/" + sMessage.substring(1);
                            
                            // Find requested file in directory
                            File filRequested = new File(sFileName);
                            
                            // If file does not exist, send -1 to the client, otherwise send the size of the file (in bytes)
                            if (filRequested.exists() == false) {
                                dosOutput.writeInt(-1);
                            } else {
                                dosOutput.writeInt((int)filRequested.length());
                                
                                // After sending the size of the file, then send the actual contents of the file
                                // First convert the file into a FileInputStream object
                                try (FileInputStream fisFile = new FileInputStream(sFileName)) {
                                    // Set transfer buffer to 4 MB at a time
                                    byte[] byFileBuffer = new byte[4096];
                                    int bytesRead;

                                    // Read and send the contents of the file in a buffer
                                    while ((bytesRead = fisFile.read(byFileBuffer)) != -1) {
                                        dosOutput.write(byFileBuffer, 0, bytesRead);
                                    }
                                } catch (IOException e) {
                                    System.err.println("[" + LocalDateTime.now().format(dtfTimeFormat) + "] I/O ERROR: " + e.getMessage());
                                }
                                
                                System.out.println("[" + LocalDateTime.now().format(dtfTimeFormat) + "] Server " + sServerAdd + " - \"" + sClientAlias + "\" has downloaded file \"" + sMessage.substring(1) + "\".");
                            }
                        } catch (IOException e) {
                            System.err.println("[" + LocalDateTime.now().format(dtfTimeFormat) + "] I/O ERROR: " + e.getMessage());
                        }
                    }
                    
                    case 'L' -> {
                        try {
                            // Unregister client's alias from server
                            sAliasList.remove(sMessage.substring(1));
                            
                            // Close connection with client
                            if (sClientAlias.isEmpty()) {
                                System.out.println("[" + LocalDateTime.now().format(dtfTimeFormat) + "] Server " + sServerAdd + " - Client at " + socEndpoint.getRemoteSocketAddress() + " has disconnected.");
                            } else {
                                System.out.println("[" + LocalDateTime.now().format(dtfTimeFormat) + "] Server " + sServerAdd + " - \"" + sClientAlias + "\" has disconnected.");
                            }
                            
                            socEndpoint.close();
                        } catch (IOException e) {
                            System.err.println("[" + LocalDateTime.now().format(dtfTimeFormat) + "] I/O ERROR: " + e.getMessage());
                        }
                        // Halt continuation of server thread
                        bContinue = false;
                    }
                    
                    case 'R' -> {
                        try {
                            // Get given alias
                            String sAliasGiven = sMessage.substring(1);
                            
                            // If given alias is already in use, send fail message to client, otherwise send success message
                            // ':' = Taken (Alias is taken)
                            if (sAliasList.contains(sAliasGiven)) {
                                dosOutput.writeChar(':');
                            } else {
                                // Add new alias to the server's alias list
                                sAliasList.add(sAliasGiven);
                                
                                // Set alias to the connection's client alias
                                sClientAlias = sAliasGiven;
                                
                                // Send success message to client
                                dosOutput.writeChar('/');
                                System.out.println("[" + LocalDateTime.now().format(dtfTimeFormat) + "] Server " + sServerAdd + " - Client at " + socEndpoint.getRemoteSocketAddress() + " has registered with alias \"" + sAliasGiven + "\".");
                            }
                        } catch (IOException e) {
                            System.err.println("[" + LocalDateTime.now().format(dtfTimeFormat) + "] I/O ERROR: " + e.getMessage());
                            dosOutput.writeChar('*');
                        }
                    }
                    
                    case 'S' -> {
                        try {
                            // Get name of incoming file
                            String sFileName = "./files/" + sMessage.substring(1);
                            
                            // Get size of incoming file
                            int nFileSize = disInput.readInt();
                            
                            // Create local file
                            FileOutputStream fosFile = new FileOutputStream(sFileName);

                            // Receive file contents from client in a buffer and write it to local file
                            byte[] byFileBuffer = new byte[4096];
                            int bytesRead;
                            while (nFileSize > 0 && (bytesRead = disInput.read(byFileBuffer, 0, (int) Math.min(byFileBuffer.length, nFileSize))) != -1) {
                                fosFile.write(byFileBuffer, 0, bytesRead);
                                nFileSize -= bytesRead;
                            }

                            System.out.println("[" + LocalDateTime.now().format(dtfTimeFormat) + "] Server " + sServerAdd + " - \"" + sClientAlias + "\" has uploaded file \"" + sMessage.substring(1) + "\".");
                            
                        } catch (IOException e) {
                            System.err.println("[" + LocalDateTime.now().format(dtfTimeFormat) + "] I/O ERROR: " + e.getMessage());
                        }
                    }
                }
                
            } catch (IOException e) {
                System.err.println("[" + LocalDateTime.now().format(dtfTimeFormat) + "] I/O ERROR: " + e.getMessage());
            }
        } while (bContinue);
    }
}