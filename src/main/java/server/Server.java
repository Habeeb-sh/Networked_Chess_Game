package server;

import com.mycompany.chess.Message;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main server class that handles client connections and manages chess games.
 * Runs in its own thread to continuously accept new client connections.
 */
public class Server extends Thread {

    // Counter for assigning unique client IDs
    int clientId;
    // Server socket for accepting client connections
    ServerSocket ssocket;
    // List of all connected clients
    ArrayList<SClient> clients;
    // List of active game rooms
    List<GameRoom> gameRooms;
    // Flag to control server running state
    private boolean isRunning = true;

    /**
     * Constructor initializes server on specified port
     *
     * @param port Port number for the server to listen on
     * @throws IOException if server socket creation fails
     */
    public Server(int port) throws IOException {
        this.clientId = 0;
        // Create server socket bound to the specified port
        this.ssocket = new ServerSocket(port);
        this.clients = new ArrayList<>();
        this.gameRooms = new ArrayList<>();
    }

    /**
     * Starts the server thread to begin accepting connections
     *
     * @throws IOException if there's an error starting the server
     */
    public void StartAcceptance() throws IOException {
        this.start();
    }

    /**
     * Adds a player to an available game room or creates a new one
     *
     * @param client The client requesting to join a game
     * @throws IOException if there's an error sending messages
     */
    public void addPlayerToGame(SClient client) throws IOException {
        // Look for an existing game room that needs a player
        GameRoom availableRoom = null;
        for (GameRoom room : gameRooms) {
            if (room.needsPlayer()) {
                availableRoom = room;
                break;
            }
        }

        // Create new game room if no available room found
        if (availableRoom == null) {
            availableRoom = new GameRoom();
            gameRooms.add(availableRoom);
        }

        // Add player to the room
        availableRoom.addPlayer(client);

        // Check if room is now full and can start the game
        if (availableRoom.isFull()) {
            // Start the game with both players
            availableRoom.startGame();
        } else {
            // Send waiting message to player until opponent joins
            String msg = Message.GenerateMsg(Message.Type.WAITING, "");
            client.SendMessage(msg.getBytes());
        }
    }

    /**
     * Handles a chess move from a client
     *
     * @param fromClient The client making the move
     * @param moveData String containing move information (e.g., "e2,e4")
     * @throws IOException if there's an error processing the move
     */
    public void handleMove(SClient fromClient, String moveData) throws IOException {
        // Find which game room this client belongs to
        for (GameRoom room : gameRooms) {
            if (room.hasPlayer(fromClient)) {
                // Forward the move to the appropriate game room
                room.handleMove(fromClient, moveData);
                break;
            }
        }
    }

    /**
     * Handles client disconnection, cleaning up game rooms and notifying
     * opponents
     *
     * @param client The client that disconnected
     * @throws IOException if there's an error during cleanup
     */
    public void handleDisconnect(SClient client) throws IOException {
        System.out.println("Handling disconnect for client: " + client.id);

        // Remove client from any game rooms and notify opponent if applicable
        for (GameRoom room : gameRooms) {
            if (room.hasPlayer(client)) {
                room.removePlayer(client);
                break;
            }
        }

        // Remove client from server's client list
        removeClient(client);
    }

    /**
     * Removes a client from the server and cleans up empty game rooms
     *
     * @param client The client to remove
     */
    public void removeClient(SClient client) {
        clients.remove(client);

        // Clean up any empty game rooms
        gameRooms.removeIf(room -> room.isEmpty());

        System.out.println("Client removed: " + client.id + ", remaining clients: " + clients.size());
    }

    /**
     * Handles game completion notification
     *
     * @param client The client reporting game end
     * @param winner String indicating the winner of the game
     * @throws IOException if there's an error handling game end
     */
    public void handleGameEnd(SClient client, String winner) throws IOException {
        // Find the game room for this client
        for (GameRoom room : gameRooms) {
            if (room.hasPlayer(client)) {
                // End the game and clean up
                room.endGame(winner);
                // Game room will be cleaned up when players are removed
                break;
            }
        }
    }

    /**
     * Gracefully shuts down the server, notifying all clients and closing
     * connections
     */
    public void shutdown() {
        try {
            isRunning = false;

            // Notify all clients that server is shutting down
            String shutdownMsg = Message.GenerateMsg(Message.Type.DISCONNECT, "Server shutting down");
            for (SClient client : new ArrayList<>(clients)) {
                try {
                    client.SendMessage(shutdownMsg.getBytes());
                    client.csocket.close();
                } catch (IOException e) {
                    // Ignore errors during shutdown
                }
            }

            // Close the server socket to stop accepting new connections
            if (ssocket != null && !ssocket.isClosed()) {
                ssocket.close();
            }

            System.out.println("Server shut down successfully");
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Main server thread loop - continuously accepts new client connections
     */
    @Override
    public void run() {
        try {
            System.out.println("Chess server started on port 6000");
            // Keep accepting connections while server is running
            while (!this.ssocket.isClosed() && isRunning) {
                try {
                    // Accept new client connection
                    Socket csocket = this.ssocket.accept();
                    // Create new client handler for this connection
                    SClient newClient = new SClient(csocket, this);
                    // Start listening for messages from this client
                    newClient.StartListening();
                    // Add to client list
                    this.clients.add(newClient);
                    System.out.println("New client connected: " + newClient.id);
                } catch (IOException ex) {
                    if (!isRunning) {
                        break; // Server is shutting down, exit gracefully
                    }
                    // Log unexpected errors
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Main method - entry point for the chess server application
     */
    public static void main(String[] args) {
        Server server = null;
        try {
            // Create and start server on port 6000
            server = new Server(6000);
            server.StartAcceptance();

            // Keep server running and wait for admin commands
            Scanner scanner = new Scanner(System.in);
            System.out.println("Server is running. Type 'quit' to stop.");
            while (true) {
                String input = scanner.nextLine();
                if ("quit".equalsIgnoreCase(input)) {
                    break; // Exit the server
                }
            }

            scanner.close();

        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            // Ensure server is properly shut down
            if (server != null) {
                server.shutdown();
            }
            System.exit(0);
        }
    }
}
