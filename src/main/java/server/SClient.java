package server;

import com.mycompany.chess.Message;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SClient class represents a single client connection to the chess server. Each
 * client runs in its own thread to handle communication independently.
 */
public class SClient extends Thread {

    // Unique identifier for this client
    int id;
    // Socket connection to the client
    Socket csocket;
    // Output stream for sending data to client
    OutputStream coutput;
    // Input stream for receiving data from client
    InputStream cinput;
    // Reference to the main server instance
    Server ownerServer;
    // Name of the player associated with this client
    String playerName;
    // Flag to track if client is still connected
    private boolean isConnected = true;

    /**
     * Constructor initializes client connection and assigns unique ID
     *
     * @param connectedSocket The socket connection from the client
     * @param server Reference to the main server instance
     * @throws IOException if there's an error setting up streams
     */
    public SClient(Socket connectedSocket, Server server) throws IOException {
        this.csocket = connectedSocket;
        // Set up output stream for sending messages to client
        this.coutput = this.csocket.getOutputStream();
        // Set up input stream for receiving messages from client
        this.cinput = this.csocket.getInputStream();
        this.ownerServer = server;
        // Assign unique ID and increment counter for next client
        this.id = server.clientId;
        server.clientId++;
    }

    /**
     * Parses incoming messages from the client and routes them to appropriate
     * handlers
     *
     * @param msg The message string received from client
     * @throws IOException if there's an error during message processing
     */
    public void MsgParser(String msg) throws IOException {
        try {
            // Split message by delimiter to extract message type and data
            String tokens[] = msg.split("#");
            // Convert first token to message type enum
            Message.Type mt = Message.Type.valueOf(tokens[0]);

            // Route message based on type
            switch (mt) {
                case JOIN_GAME:
                    // Extract player name from second token
                    this.playerName = tokens[1];
                    System.out.println("Player " + playerName + " wants to join a game");
                    // Add this player to a game room
                    ownerServer.addPlayerToGame(this);
                    break;

                case MOVE:
                    // Format: MOVE#from,to - handle chess move
                    ownerServer.handleMove(this, tokens[1]);
                    break;

                case DISCONNECT:
                    // Client requested graceful disconnect
                    System.out.println("Player " + playerName + " requested disconnect");
                    ownerServer.handleDisconnect(this);
                    disconnect();
                    break;

                case GAME_OVER:
                    // Handle game completion notification from client
                    ownerServer.handleGameEnd(this, tokens[1]);
                    break;

                default:
                    // Log unknown message types for debugging
                    System.out.println("Unknown message type: " + mt);
                    break;
            }
        } catch (Exception e) {
            // Log any errors during message parsing
            System.out.println("Error parsing message from client " + id + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Starts the client thread to begin listening for messages
     */
    public void StartListening() {
        this.start();
    }

    /**
     * Main thread loop - continuously listens for incoming messages from client
     */
    public void run() {
        try {
            // Keep listening while socket is open and client is connected
            while (!this.csocket.isClosed() && isConnected) {
                // Read the first byte which indicates message size
                int bsize = this.cinput.read();
                if (bsize == -1) {
                    break; // Connection closed by client
                }

                // Create buffer to hold the message data
                byte buffer[] = new byte[bsize];
                int bytesRead = 0;
                // Read the complete message
                while (bytesRead < bsize) {
                    int result = this.cinput.read(buffer, bytesRead, bsize - bytesRead);
                    if (result == -1) {
                        break; // Connection lost during read
                    }
                    bytesRead += result;
                }

                // Convert byte array to string message
                String rsMsg = new String(buffer, 0, bytesRead);
                System.out.println("Received message from client " + id + ": " + rsMsg);
                // Parse and handle the received message
                this.MsgParser(rsMsg);
            }
        } catch (IOException ex) {
            // Only log if we were still connected (not during graceful shutdown)
            if (isConnected) {
                System.out.println("Client " + id + " (" + (playerName != null ? playerName : "Unknown") + ") connection lost");
            }
        } finally {
            // Clean up when thread exits
            try {
                if (isConnected) {
                    // Notify server of unexpected disconnect
                    ownerServer.handleDisconnect(this);
                }
            } catch (IOException ex) {
                Logger.getLogger(SClient.class.getName()).log(Level.SEVERE, null, ex);
            }

            // Ensure connection is properly closed
            disconnect();
        }
    }

    /**
     * Sends a message to the client
     *
     * @param msg Byte array containing the message to send
     * @throws IOException if client is disconnected or send fails
     */
    public void SendMessage(byte[] msg) throws IOException {
        // Check if client is still connected
        if (!isConnected || csocket.isClosed()) {
            throw new IOException("Client is not connected");
        }

        try {
            // Send message length first, then the message data
            this.coutput.write(msg.length);
            this.coutput.write(msg);
            // Ensure data is sent immediately
            this.coutput.flush();
        } catch (IOException ex) {
            System.out.println("Failed to send message to client " + id + ": " + ex.getMessage());
            // Disconnect client if send fails
            disconnect();
            throw ex;
        }
    }

    /**
     * Closes the client connection and cleans up resources
     */
    public void disconnect() {
        try {
            // Mark as disconnected
            isConnected = false;
            // Close the socket if it's still open
            if (csocket != null && !csocket.isClosed()) {
                csocket.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(SClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Gets the player name, with fallback for unknown players
     *
     * @return Player name or "Unknown" if not set
     */
    public String getPlayerName() {
        return playerName != null ? playerName : "Unknown";
    }

    /**
     * Checks if the client is currently connected
     *
     * @return true if client is connected and socket is open
     */
    public boolean isConnected() {
        return isConnected && csocket != null && !csocket.isClosed();
    }
}
