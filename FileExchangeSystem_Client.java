// Machine Project Group 10
// TAPIA, John Lorenzo N.
// ARGAMOSA, Daniel Cedric S.
import java.net.*;
import java.io.*;
import java.util.Scanner;

public class FileExchangeSystem_Client {
    static void displayHelp() {
        System.out.println(""
                + "/?\t\t\t\t\t\t\tDisplay information about all valid commands\n"
                + "/dir\t\t\t\t\t\tDisplay a directory of the files in the File Exchange server\n"
                + "/get [filename]\t\t\t\tDownload a file of name [filename] from the File Exchange server\n"
                + "/join [ip_address] [port]\tConnect to the File Exchange server of IP address [ip_address] and port number [port]\n"
                + "/leave\t\t\t\t\t\tDisconnect from the connected File Exchange server\n"
                + "/register [alias]\t\t\tRegister to the connected File Exchange server using the handle [alias]\n"
                + "/store [filename]\t\t\tUpload a file of name [filename] to the File Exchange server - [filename] must exist inside the client's directory\n");
    }
    
    public static void main(String[] args) {
        // Initial interface
        System.out.println("------------------------------");
        System.out.println("FILE EXCHANGE SYSTEM");
        System.out.println("------------------------------\n");
        System.out.println("Welcome!");
        System.out.println("Please connect to a server to start...\n");
        System.out.println("To get a list of all recognized commands, enter \'/?\'");
        
        // Scan for input
        Scanner input = new Scanner(System.in);
        input.useDelimiter(" ");
        
        // Evaluate input
        String command = input.next();
        switch (command) {
            case "/?" -> displayHelp();
            case "/join" -> {
                // TODO
            }
            default -> System.out.println("ERROR: \'" + command + "\' is not recognized as a valid command.");
        }
        
        
        
        // BELOW: Code from Lab Activity 4 - to be used as base for network code
        String sServerAddress = args[0];
        int nPort = Integer.parseInt(args[1]);
        String sName = args[2];
        String sMessage = args[3];

        try {
            Socket clientEndpoint = new Socket(sServerAddress, nPort);

            System.out.println(sName + ": Connecting to server at" + clientEndpoint.getRemoteSocketAddress());
            System.out.println(sName + ": Connected to server at" + clientEndpoint.getRemoteSocketAddress());

            // Send message to server
            DataOutputStream dosWriter = new DataOutputStream(clientEndpoint.getOutputStream());
            dosWriter.writeUTF("Message from " + sName + ": " + sMessage + "");

            // Receive message from server
            DataInputStream disReader = new DataInputStream(clientEndpoint.getInputStream());

            while (disReader.available() == 0) {}

            System.out.println(disReader.readUTF());

            clientEndpoint.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            System.out.println(sName + ": Connection is terminated.");
        }
    }
}