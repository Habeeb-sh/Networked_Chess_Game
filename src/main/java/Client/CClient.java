package Client;

import com.mycompany.chess.Message;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client-side network handler for the chess game. Manages connection to the
 * server and handles all network communication.
 */
public class CClient extends Thread {

    // Network communication components
    Socket csocket;           // Socket connection to the server
    OutputStream coutput;     // Stream for sending data to server
    InputStream cinput;       // Stream for receiving data from server
    String playerName;        // This player's name

    // UI components that this client communicates with
    home homeFrame;           // Main menu/lobby frame
    NetworkGame gameFrame;    // Game interface frame

    // Connection status tracking
    private boolean isConnected = false;

    /**
     * Constructor that establishes connection to the chess server
     *
     * @param ip Server IP address
     * @param port Server port number
     * @param playerName This player's chosen name
     * @throws IOException if connection fails
     */
    public CClient(String ip, int port, String playerName) throws IOException {
        // Establish socket connection to server
        this.csocket = new Socket(ip, port);
        this.coutput = csocket.getOutputStream();
        this.cinput = csocket.getInputStream();
        this.playerName = playerName;
        this.isConnected = true;
    }

    /**
     * Sets reference to the home/lobby frame for UI updates
     *
     * @param homeFrame The main menu frame
     */
    public void setHomeFrame(home homeFrame) {
        this.homeFrame = homeFrame;
    }

    /**
     * Sets reference to the game frame for UI updates
     *
     * @param gameFrame The chess game frame
     */
    public void setGameFrame(NetworkGame gameFrame) {
        this.gameFrame = gameFrame;
    }

    /**
     * Parses incoming messages from the server and handles them appropriately
     *
     * @param msg Raw message string from server
     */
    public void MsgParser(String msg) {
        try {
            // Split message into components (format: TYPE#data)
            String tokens[] = msg.split("#");
            Message.Type mt = Message.Type.valueOf(tokens[0]);

            // Handle different message types from server
            switch (mt) {
                case GAME_START:
                    // Server indicates game is starting with player assignments
                    // Format: GAME_START#playerColor,opponentName
                    String[] gameData = tokens[1].split(",");
                    String playerColor = gameData[0];  // "W" or "B"
                    String opponentName = gameData[1]; // Opponent's name

                    // Notify home frame to transition to game
                    if (homeFrame != null) {
                        homeFrame.startGame(playerColor, opponentName);
                    }
                    break;

                case MOVE:
                    // Opponent made a move that needs to be applied to our board
                    // Format: MOVE#from,to
                    if (gameFrame != null) {
                        String[] moveData = tokens[1].split(",");
                        String from = moveData[0]; // Starting position
                        String to = moveData[1];   // Destination position
                        gameFrame.receiveMove(from, to);
                    }
                    break;

                case GAME_OVER:
                    // Game has ended with a winner/draw
                    // Format: GAME_OVER#winner
                    if (gameFrame != null) {
                        gameFrame.gameOver(tokens[1]);
                    }
                    break;

                case WAITING:
                    // Player is in queue waiting for an opponent
                    if (homeFrame != null) {
                        javax.swing.JOptionPane.showMessageDialog(homeFrame,
                                "Waiting for another player to join...",
                                "Waiting",
                                javax.swing.JOptionPane.INFORMATION_MESSAGE);
                    }
                    break;

                case DISCONNECT:
                    // Opponent has disconnected from the game
                    if (gameFrame != null) {
                        gameFrame.opponentDisconnected();
                    }
                    break;

                case ERROR:
                    // Server sent an error message
                    javax.swing.JOptionPane.showMessageDialog(
                            homeFrame != null ? homeFrame : gameFrame,
                            tokens[1], // Error message content
                            "Error",
                            javax.swing.JOptionPane.ERROR_MESSAGE);
                    break;

                default:
                    // Unknown message type received
                    System.out.println("Unknown message type: " + mt);
                    break;
            }
        } catch (Exception e) {
            // Error parsing the message
            System.out.println("Error parsing message: " + e.getMessage());
        }
    }

    /**
     * Sends a message to the server
     *
     * @param msg Message string to send
     * @throws IOException if send fails or not connected
     */
    public void SendMessage(String msg) throws IOException {
        // Check connection status before sending
        if (!isConnected || csocket.isClosed()) {
            throw new IOException("Not connected to server");
        }

        // Convert message to bytes and send with length prefix
        byte[] msgBytes = msg.getBytes();
        // Send message length first, then message content
        this.coutput.write(msgBytes.length);
        this.coutput.write(msgBytes);
        this.coutput.flush(); // Ensure data is sent immediately
    }

    /**
     * Sends a chess move to the server
     *
     * @param from Starting position of the move
     * @param to Destination position of the move
     * @throws IOException if send fails
     */
    public void sendMove(String from, String to) throws IOException {
        // Format: MOVE#from,to
        String msg = Message.GenerateMsg(Message.Type.MOVE, from + "," + to);
        SendMessage(msg);
    }

    /**
     * Sends a request to join a game
     *
     * @throws IOException if send fails
     */
    public void joinGame() throws IOException {
        // Request to join game with player name
        String msg = Message.GenerateMsg(Message.Type.JOIN_GAME, playerName);
        SendMessage(msg);
    }

    /**
     * Starts listening for server messages and sends initial join request
     *
     * @throws IOException if connection setup fails
     */
    public void Listen() throws IOException {
        // Start the message listening thread
        this.start();
        // Send join game message after connection
        joinGame();
    }

    /**
     * Main thread loop that continuously listens for server messages Runs in
     * separate thread to avoid blocking UI
     */
    public void run() {
        try {
            // Keep listening while connected
            while (!this.csocket.isClosed() && isConnected) {
                // Read message length first
                int bsize = this.cinput.read();
                if (bsize == -1) {
                    break; // Connection closed by server
                }

                // Read the actual message content
                byte buffer[] = new byte[bsize];
                int bytesRead = 0;
                // Ensure we read the complete message
                while (bytesRead < bsize) {
                    int result = this.cinput.read(buffer, bytesRead, bsize - bytesRead);
                    if (result == -1) {
                        break; // Connection lost
                    }
                    bytesRead += result;
                }

                // Convert bytes to string and process the message
                String rsMsg = new String(buffer, 0, bytesRead);
                this.MsgParser(rsMsg);
            }
        } catch (IOException ex) {
            // Log connection errors
//            Logger.getLogger(CClient.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("disconnected");
        } finally {
            // Clean up when connection is lost
            handleDisconnection();
        }
    }

    /**
     * Handles cleanup and UI notification when connection is lost
     */
    private void handleDisconnection() {
        isConnected = false;

        // Notify the game frame if it exists that server disconnected
        if (gameFrame != null) {
            java.awt.EventQueue.invokeLater(() -> {
                gameFrame.serverDisconnected();
            });
        }
    }

    /**
     * Cleanly disconnects from the server
     */
    public void disconnect() {
        try {
            isConnected = false;
            if (csocket != null && !csocket.isClosed()) {
                // Send disconnect notification to server
                try {
                    String msg = Message.GenerateMsg(Message.Type.DISCONNECT, playerName);
                    SendMessage(msg);
                } catch (IOException e) {
                    // Ignore errors when disconnecting (server might be down)
                }
                csocket.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(CClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Checks if client is currently connected to server
     *
     * @return true if connected and socket is open
     */
    public boolean isConnected() {
        return isConnected && csocket != null && !csocket.isClosed();
    }
}
