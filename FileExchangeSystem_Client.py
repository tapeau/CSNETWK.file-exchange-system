# Machine Project Group 10
# ARGAMOSA, Daniel Cedric S.
# TAPIA, John Lorenzo N.

import socket
import threading
import json
import ipaddress
import os

# Setup client UDP socket
client_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

# buffer for files and messages
buffer = 4096 # buffer for files and messages
server_directory = os.path.abspath("ServerFolder")

# Determins whether the client is connected to the server
isConnected = False 
isRegistered = False 

server_address = None

def validIP(serverIP):
    try:
        ipaddress.ip_address(serverIP)
        return True
    except socket.error:
        return False

def validPort(nServerPort):
    try:
        port_num = int(nServerPort)
        return 0 < port_num < 65536  # Because the port range is 1-65535
    except ValueError:
        return False

def download(filename):
    global server_directory

    # Read the file from the server folder
    with open(os.path.join(server_directory, filename), 'rb') as server_file:
        file_content = server_file.read()

    # Write the file content into the client's 'Downloads' folder
    downloads_path = os.path.join(os.path.expanduser('~'), 'Downloads', filename)
    with open(downloads_path, 'wb') as client_file:
        client_file.write(file_content)


def upload(filename):
    global server_directory
    
    # Read the file from the directory
    with open(os.path.join(os.getcwd(), filename), 'rb') as upload_file:
        file_content = upload_file.read()

    # Write the file into the server folder
    with open(os.path.join(server_directory, filename), 'wb') as server_file:
        server_file.write(file_content)

# Processes commands sent to the server
def toServer(userInput):
    global isConnected
    global isRegistered
    global server_address
    
    if not userInput.startswith('/'):
        print(f"ERROR: {userInput} is not a valid command. Type /? for the list of available commands.")
        return
    
    input_list = userInput.split()
    command = input_list[0]
    params = input_list[1:]
      
    if command == "/join":
        if not isConnected:
            if len(params) != 2:
                print("ERROR: Invalid command syntax. Command format: /join [ip_address] [port]")
            else:
                try:
                    server_address = (params[0], int(params[1]))
                
                    # Send the "join" command to the server
                    print(f"Attempting to connect to the File Exchange Server at {server_address}")
                    client_socket.sendto(json.dumps({"command": "join"}).encode(), server_address)

                    client_socket.settimeout(3)

                    try:
                        acknowledgement = client_socket.recvfrom(buffer)
                        data = json.loads(acknowledgement[0].decode())
 
                        if data['command'] == "success":
                            print("Connection to the File Exchange Server is successful! Welcome!")
                            isConnected = True
                            client_socket.settimeout(None)

                    except socket.timeout:
                        print("ERROR: Connection attempt timed out. Please check the server availability and try again.")
                        server_address = None
                        return

                    except ConnectionResetError:
                        print("ERROR: Received unexpected data from the server. Please check the input paramaeters")
                        server_address = None
                        return

                    except Exception as e:
                        print(f"ERROR: {str(e)}")
                        server_address = None
                        return

                except Exception as e:
                    print(f"ERROR: Check IP Address and Server Port format. {str(e)}")
                    server_address = None
                    return    
        else:
            print('ERROR: You are already connected to a File Exchange server.')
      
    elif command == "/leave":
        if isConnected:
            if len(params) > 0:
                print("ERROR: Invalid command syntax. Command format: /leave")
            else:
                client_socket.sendto(json.dumps({"command":"leave"}).encode(), server_address)
                print(f"Disconnected from server at {server_address}. Thank you!")
                isConnected = False
                isRegistered = False 
                server_address = None
        else:
            print('ERROR: Please see if you are connected to a server first')
   
    elif command == "/register":
        if isConnected:
            if not isRegistered:
                if len(params) != 1:
                    print("ERROR: Invalid command syntax. Command format: /register [alias]")
                else:
                    client_socket.sendto(json.dumps({"command":"register", "alias":params[0]}).encode(), server_address)
                    
                    acknowledgement = client_socket.recvfrom(buffer)
                    data = json.loads(acknowledgement[0].decode())
                    message = data['message']
 
                    print(f">\n{message}") 

                    if data['command'] == "success":
                        isRegistered = True
            else:
                print('ERROR: You are already registered')
        else:
            print('ERROR: Please see if you are connected to a server first')
   
    elif command == "/dir":
        if isConnected:
            if isRegistered:
                if len(params) > 0:
                    print("ERROR: Invalid command syntax. Command format: /dir")
                else:
                    client_socket.sendto(json.dumps({"command":"dir"}).encode(), server_address)
            else:
                print('ERROR: Please see if you are a registered user first')
        else:
            print('ERROR: Please see if you are connected to a server first')
    
    elif command == "/get":
        if isConnected:
            if isRegistered:
                if len(params) != 1:
                    print("ERROR: Invalid command syntax. Command format: /get [filename.filetype]")
                else:

                    try:
                        client_socket.sendto(json.dumps({"command":"get", "filename":params[0]}).encode(), server_address)
                        # The code that uses the file functions in fromServer
                    except Exception as e:
                        return
            else:
                print('ERROR: Please see if you are a registered user first')
        else:
            print('ERROR: Please see if you are connected to a server first')

    elif command == "/store":
        if isConnected:
            if isRegistered:
                if len(params) != 1:
                    print("ERROR: Invalid command syntax. Command format: /store [filename.filetype]")
                else:
                    try:
                        client_socket.sendto(json.dumps({"command":"store", "filename":params[0]}).encode(), server_address)
                        # The code that uses the file functions in fromServer
                    except Exception as e:
                        return
            else:
                print('ERROR: Please see if you are a registered user first')
        else:
            print('ERROR: Please see if you are connected to a server first')
    
    elif command == "/all":
        if isConnected:
            if isRegistered:
                if len(params) == 0:
                    print("ERROR: Invalid command syntax. Command format: /all [message]")
                else:
                    message = ' '.join(params)
                    client_socket.sendto(json.dumps({"command":"all", "message":message}).encode(), server_address)
            else:
                print('ERROR: Please see if you are a registered user first')
        else:
            print('ERROR: Please see if you are connected to a server first')
   
    elif command == "/msg":
        if isConnected:
            if isRegistered:
                if len(params) < 2:
                    print("ERROR: Invalid command syntax. Command format: /msg [alias] [message]")
                else:
                    alias = params[0]
                    message = ' '.join(params[1:])
                    client_socket.sendto(json.dumps({"command":"msg", "alias":alias, "message":message}).encode(), server_address)
            else:
                print('ERROR: Please see if you are a registered user first')
        else:
           print('ERROR: Please see if you are connected to a server first')
    
    elif command == "/?":
        print("/?                           Display information about all valid commands")
        print("/all [message]               Send a message to every registered alias")
        print("/dir                         Display a directory of the files in the File Exchange Server")
        print("/get [filename.filetype]     Download a file of name [filename] from the File Exchange Server")
        print("/join [ip_address] [port]    Connects to the File Exchange Server")
        print("/leave                       Disconnects from the connected File Exchange Server")
        print("/msg [alias] [message]       Send a direct message to a single registered alias")
        print("/register [alias]            Register to the connected File Exchange server using a unique [alias]")
        print("/store [filename.filetype]   Upload a file of name [filename] to the File Exchange server - [filename] must exist inside the client's directory")
    
    else:
        print(f"ERROR: {userInput} is not a valid command. Type /? for the list of available commands.")

# Processes commands received from the server
def fromServer(data):
    command = data['command'] 
    
    # Acknowledge ping from server
    if command == 'ping':
        ping_ack = {'command': 'ping'}
        client_socket.sendto(json.dumps(ping_ack).encode(), server_address)
        return
    
    # Receive a global or error message 
    elif command == "all" or command == "error":
        message = data['message']
        
    # Receive a direct message
    elif command == "msg":
        alias = data['alias']
        message = f"[From {alias}]: {data['message']}"
    
    # Return success message
    elif command == "success":
        message = data['message']

        if 'filename' in data and 'type' in data:
            # Handles files
            if data['type'] == "store":
                upload(data['filename'])
            elif data['type'] == "get":
                download(data['filename'])
        
    print(f">\n{message}\n> ", end="")      

# Function thats being run in a thread
def receive_messages():
    global isConnected
    message = None
    
    while True:
        if isConnected:
            if isRegistered: # Majority of the features are locked behind a registered client
                try:
                    message = client_socket.recvfrom(buffer)
                    data = json.loads(message[0].decode())
                    fromServer(data)
                except ConnectionResetError:
                    print("ERROR: Connection to the File Exchange Server terminated. Disconnecting.")
                    isConnected = False
                except Exception as e:
                    print(f"ERROR Test: {str(e)}")

# Start a thread to continuously receive and print messages from the server
file_thread = threading.Thread(target = receive_messages)
file_thread.start()

print("------------------------------")
print("FILE EXCHANGE SYSTEM - CLIENT")
print("------------------------------")
print("Welcome!")
print("Please connect to a server to start...")
print("Enter a command. Type /? for get a list of all recognized commands")

while True:
    # Get a command from the user
    userInput = input("> ")
    toServer(userInput)

