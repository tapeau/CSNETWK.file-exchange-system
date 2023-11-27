// Machine Project Group 10
// TAPIA, John Lorenzo N.
// ARGAMOSA, Daniel Cedric S.

package FileExchangeSystem;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;

public class FileExchangeSystem_Connection extends Thread {
    // Client socket of the connection
    private Socket socEndpoint;
    
    // Constructor function
    public FileExchangeSystem_Connection(Socket soc) {
        this.socEndpoint = soc;
    }
    
    // Override run() function to implement intended file transfer functionalities
    @Override
    public void run() {
        // TODO
    }
}