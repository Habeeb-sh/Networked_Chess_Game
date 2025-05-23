package server;

import com.mycompany.chess.Message;
import java.io.IOException;

/**
 * GameRoom class manages a single chess game session between two players. It
 * handles player assignment, turn management, move validation, and game state.
 * Each GameRoom instance represents one active chess game on the server.
 */
public class GameRoom {

    // Player 1 is always assigned the White pieces
    private SClient player1; // White player

    // Player 2 is always assigned the Black pieces
    private SClient player2; // Black player

    // Current turn indicator: "W" for White's turn, "B" for Black's turn
    private String currentTurn = "W"; // "W" for white, "B" for black

    // Flag indicating whether the game has been started
    private boolean gameStarted = false;

    // Flag indicating whether the game has ended
    private boolean gameEnded = false;

    /**
     * Checks if this game room needs another player to start
     *
     * @return true if the room has space for one more player and game hasn't
     * ended
     */
    public boolean needsPlayer() {
        return !gameEnded && (player1 == null || player2 == null);
    }

    /**
     * Checks if the game room has both players assigned
     *
     * @return true if both player slots are filled
     */
    public boolean isFull() {
        return player1 != null && player2 != null;
    }

    /**
     * Checks if the game room is completely empty
     *
     * @return true if no players are assigned to this room
     */
    public boolean isEmpty() {
        return player1 == null && player2 == null;
    }

    /**
     * Adds a player to the game room First player is assigned White (player1),
     * second player is assigned Black (player2)
     *
     * @param client The client to add to the game room
     * @throws IOException if there's an error during player assignment
     */
    public void addPlayer(SClient client) throws IOException {
        // Don't add players to ended games
        if (gameEnded) {
            return;
        }

        if (player1 == null) {
            // Assign first player as White
            player1 = client;
            System.out.println("Player 1 (White) added: " + client.getPlayerName());
        } else if (player2 == null) {
            // Assign second player as Black
            player2 = client;
            System.out.println("Player 2 (Black) added: " + client.getPlayerName());
        }
    }

    /**
     * Starts the game when both players are present Sends game start messages
     * to both players with their assigned colors and opponent names
     *
     * @throws IOException if there's an error sending start messages
     */
    public void startGame() throws IOException {
        // Can only start if room is full and game hasn't ended
        if (!isFull() || gameEnded) {
            return;
        }

        gameStarted = true;
        System.out.println("Starting game between " + player1.getPlayerName() + " (White) and " + player2.getPlayerName() + " (Black)");

        try {
            // Prepare game start messages for both players
            // Format: "PlayerColor,OpponentName"
            String gameStartMsg1 = Message.GenerateMsg(Message.Type.GAME_START, "W," + player2.getPlayerName());
            String gameStartMsg2 = Message.GenerateMsg(Message.Type.GAME_START, "B," + player1.getPlayerName());

            // Send start message to player 1 (White)
            if (player1.isConnected()) {
                player1.SendMessage(gameStartMsg1.getBytes());
            }
            // Send start message to player 2 (Black)
            if (player2.isConnected()) {
                player2.SendMessage(gameStartMsg2.getBytes());
            }
        } catch (IOException ex) {
            System.out.println("Error starting game: " + ex.getMessage());
            throw ex;
        }
    }

    /**
     * Handles a move made by one of the players Validates turn order and
     * forwards the move to the opponent
     *
     * @param fromClient The client who made the move
     * @param moveData The move data in format "from,to" (e.g., "e2,e4")
     * @throws IOException if there's an error sending the move to the opponent
     */
    public void handleMove(SClient fromClient, String moveData) throws IOException {
        // Only process moves for active games
        if (!gameStarted || gameEnded) {
            return;
        }

        // Determine which player made the move and who the opponent is
        String playerColor;
        SClient otherPlayer;

        if (fromClient == player1) {
            playerColor = "W"; // Player 1 is White
            otherPlayer = player2;
        } else if (fromClient == player2) {
            playerColor = "B"; // Player 2 is Black
            otherPlayer = player1;
        } else {
            return; // Invalid player - not in this game room
        }

        // Validate that it's the correct player's turn
        if (!currentTurn.equals(playerColor)) {
            String errorMsg = Message.GenerateMsg(Message.Type.ERROR, "Not your turn!");
            try {
                fromClient.SendMessage(errorMsg.getBytes());
            } catch (IOException ex) {
                System.out.println("Failed to send error message to client: " + ex.getMessage());
            }
            return;
        }

        // Forward the move to the opponent if they're connected
        if (otherPlayer != null && otherPlayer.isConnected()) {
            try {
                String moveMsg = Message.GenerateMsg(Message.Type.MOVE, moveData);
                otherPlayer.SendMessage(moveMsg.getBytes());

                // Switch turns: 
                currentTurn = currentTurn.equals("W") ? "B" : "W";

                System.out.println("Move from " + fromClient.getPlayerName() + " to " + otherPlayer.getPlayerName() + ": " + moveData);
            } catch (IOException ex) {
                System.out.println("Failed to forward move to opponent: " + ex.getMessage());
                // If we can't reach the opponent, they've likely disconnected
                removePlayer(otherPlayer);
            }
        }
    }

    /**
     * Checks if a specific client is a player in this game room
     *
     * @param client The client to check
     * @return true if the client is either player1 or player2
     */
    public boolean hasPlayer(SClient client) {
        return client == player1 || client == player2;
    }

    /**
     * Removes a player from the game room and handles disconnection logic
     * Notifies the remaining player of the disconnection and awards them the
     * win
     *
     * @param client The client to remove
     * @throws IOException if there's an error notifying the remaining player
     */
    public void removePlayer(SClient client) throws IOException {
        // Don't process removals for already ended games
        if (gameEnded) {
            return;
        }

        String disconnectedPlayerName = client.getPlayerName();
        SClient remainingPlayer = null;
        String winnerColor = null;

        // Determine which player disconnected and who remains
        if (client == player1) {
            remainingPlayer = player2;
            winnerColor = "B"; // Black wins by default when White disconnects
            player1 = null;
        } else if (client == player2) {
            remainingPlayer = player1;
            winnerColor = "W"; // White wins by default when Black disconnects
            player2 = null;
        }

        // If game was started and there's a remaining connected player, notify them
        if (gameStarted && remainingPlayer != null && remainingPlayer.isConnected()) {
            try {
                // First send disconnect notification
                String disconnectMsg = Message.GenerateMsg(Message.Type.DISCONNECT, disconnectedPlayerName + " has disconnected");
                remainingPlayer.SendMessage(disconnectMsg.getBytes());

                // Then send game over message with winner
                String gameOverMsg = Message.GenerateMsg(Message.Type.GAME_OVER, winnerColor);
                remainingPlayer.SendMessage(gameOverMsg.getBytes());

                System.out.println("Notified " + remainingPlayer.getPlayerName() + " that " + disconnectedPlayerName + " disconnected");
            } catch (IOException ex) {
                System.out.println("Failed to notify remaining player of disconnection: " + ex.getMessage());
            }
        }

        // Mark game as ended and stopped
        gameEnded = true;
        gameStarted = false;
        System.out.println("Player removed from game room: " + disconnectedPlayerName);
    }

    /**
     * Ends the game with a specified winner Sends game over messages to both
     * players
     *
     * @param winner The winner of the game ("W" for White, "B" for Black, or
     * "draw")
     * @throws IOException if there's an error sending game over messages
     */
    public void endGame(String winner) throws IOException {
        // Only end active games
        if (!gameStarted || gameEnded) {
            return;
        }

        gameEnded = true;
        gameStarted = false;

        String gameOverMsg = Message.GenerateMsg(Message.Type.GAME_OVER, winner);

        // Send game over message to player 1 (White) if connected
        if (player1 != null && player1.isConnected()) {
            try {
                player1.SendMessage(gameOverMsg.getBytes());
            } catch (IOException ex) {
                System.out.println("Failed to send game over message to player1: " + ex.getMessage());
            }
        }

        // Send game over message to player 2 (Black) if connected
        if (player2 != null && player2.isConnected()) {
            try {
                player2.SendMessage(gameOverMsg.getBytes());
            } catch (IOException ex) {
                System.out.println("Failed to send game over message to player2: " + ex.getMessage());
            }
        }

        System.out.println("Game ended. Winner: " + winner);

        // Clean up player references
        player1 = null;
        player2 = null;
    }

    /**
     * Checks if the game is currently active (started but not ended)
     *
     * @return true if the game is in progress
     */
    public boolean isGameActive() {
        return gameStarted && !gameEnded;
    }
}
