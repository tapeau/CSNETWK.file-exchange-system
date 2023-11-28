# File Exchange System
A simple command-line file exchange system developed as Machine Project for DLSU CSNETWK Course (T1 2023-2024)

## Members
- John Lorenzo Tapia
- Daniel Cedric Argamosa

## Running the server
1. Ensure these two .java files are in one folder, then compile them
```
javac FileExchangeSystem_Server.java
javac FileExchangeSystem_Connection.java
```
2. In one instance of a command-line interface, run the server application
```
java FileExchangeSystem_Server
```

## Running the client
1. Ensure the file `FileExchangeSystem_Client.java` is in a separate directory from the server
2. Compile the client
```
javac FileExchangeSystem_Client.java
```
3. In another instance of your command-line interface (separate from the server's), run the client application
```
java FileExchangeSystem_Client
```
- To have multiple clients, run the client application in multiple separate instances of your command-line interface