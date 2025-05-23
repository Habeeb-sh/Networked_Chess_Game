package Client;

import com.mycompany.chess.*;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import java.awt.Font;
import java.io.IOException;
import javax.swing.JOptionPane;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * NetworkGame class represents the client-side chess game GUI for networked
 * multiplayer games. This class handles the graphical interface, user
 * interactions, and communication with the game server. It implements
 * GameController.GameView to receive game state updates from the controller.
 */
public class NetworkGame extends javax.swing.JFrame implements GameController.GameView {

    // 8x8 grid of buttons representing the chess board squares
    private JButton[][] buttonGrid = new JButton[8][8];

    // Map for quick access to buttons by their chess position (e.g., "e4", "a1")
    private Map<String, JButton> buttonMap = new HashMap<>();

    // Currently selected square position (null if no square is selected)
    private String selectedPosition = null;

    // Game controller that handles chess logic and rules
    private GameController controller;

    // Chess board column labels (a through h)
    private final char[] columns = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};

    // Network client for server communication
    private CClient client;

    // Player's assigned color: "W" for White, "B" for Black
    private String playerColor;

    // Name of the opponent player
    private String opponentName;

    // Flag indicating if it's currently this player's turn
    private boolean isMyTurn;

    /**
     * Constructor for NetworkGame
     *
     * @param client The network client for server communication
     * @param playerColor The color assigned to this player ("W" or "B")
     * @param opponentName The name of the opponent player
     */
    public NetworkGame(CClient client, String playerColor, String opponentName) {
        this.client = client;
        this.playerColor = playerColor;
        this.opponentName = opponentName;
        // White player starts first in chess
        this.isMyTurn = playerColor.equals("W");

        // Set this frame as the client's game frame for receiving updates
        client.setGameFrame(this);

        // Initialize game controller and set this as the view
        controller = new GameController();
        controller.setView(this);

        // Initialize GUI components and update display
        initComponents();
        updateTurnDisplay();
        updateBoard();
    }

    /**
     * Updates the turn display label to show whose turn it is Uses different
     * colors to indicate if it's the player's turn or opponent's turn
     */
    private void updateTurnDisplay() {
        String currentTurn = controller.getCurrentTurn();
        if (currentTurn.equals(playerColor)) {
            // Player's turn - show in green
            turnLabel.setText("Your Turn (" + (playerColor.equals("W") ? "White" : "Black") + ")");
            turnLabel.setForeground(Color.GREEN);
        } else {
            // Opponent's turn - show in red
            turnLabel.setText(opponentName + "'s Turn (" + (playerColor.equals("W") ? "Black" : "White") + ")");
            turnLabel.setForeground(Color.RED);
        }
    }

    /**
     * Updates the visual representation of the chess board Retrieves piece
     * information from the controller and displays symbols on buttons
     */
    @Override
    public void updateBoard() {
        ChessBoard board = controller.getBoard();
        // Iterate through all board positions
        for (Map.Entry<String, JButton> entry : buttonMap.entrySet()) {
            String displayPosition = entry.getKey();
            JButton button = entry.getValue();

            // Convert display position to actual board position (handles board flipping for black player)
            String actualPosition = convertDisplayToActual(displayPosition);

            // Get piece at this position and update button display
            Piece piece = board.getPieceAt(actualPosition);
            if (piece != null) {
                button.setText(piece.getSymbol());
                // Set piece color: white pieces in white text, black pieces in black text
                if (piece.getColor().equals("W")) {
                    button.setForeground(Color.WHITE);
                } else {
                    button.setForeground(Color.BLACK);
                }
            } else {
                // Empty square
                button.setText("");
            }
        }
    }

    /**
     * Called by the controller when the turn changes
     *
     * @param turn The color of the player whose turn it is ("W" or "B")
     */
    @Override
    public void updateTurn(String turn) {
        isMyTurn = turn.equals(playerColor);
        updateTurnDisplay();
    }

    /**
     * Displays a message dialog to the user
     *
     * @param message The message text to display
     * @param title The dialog title
     * @param messageType The type of message (info, warning, error, etc.)
     */
    @Override
    public void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    /**
     * Shows a dialog for pawn promotion piece selection
     *
     * @param color The color of the pawn being promoted
     * @return The selected piece for promotion
     */
    @Override
    public Piece showPromotionDialog(String color) {
        Object[] options = {"Queen", "Rook", "Bishop", "Knight"};
        int choice = JOptionPane.showOptionDialog(this,
                "Promote pawn to:",
                "Pawn Promotion",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        // Create and return the selected piece
        switch (choice) {
            case 0:
                return new Queen(color.equals("W") ? "WQ" : "BQ", color);
            case 1:
                return new Rook(color.equals("W") ? "WR" : "BR", color);
            case 2:
                return new Bishop(color.equals("W") ? "WB" : "BB", color);
            case 3:
                return new Knight(color.equals("W") ? "WN" : "BN", color);
            default:
                // Default to Queen if no selection made
                return new Queen(color.equals("W") ? "WQ" : "BQ", color);
        }
    }

    /**
     * Handles game over scenarios and displays appropriate message
     *
     * @param winner The winner of the game ("W", "B", or "draw")
     */
    public void gameOver(String winner) {
        String message;
        if (winner.equals("draw")) {
            message = "Game ended in a draw!";
        } else if (winner.equals(playerColor)) {
            message = "You won!";
        } else {
            message = "You lost!";
        }

        JOptionPane.showMessageDialog(this, message, "Game Over", JOptionPane.INFORMATION_MESSAGE);

        // Return to home screen after game ends
        returnToHome();
    }

    /**
     * Handles opponent disconnection scenario Player wins by default when
     * opponent disconnects
     */
    public void opponentDisconnected() {
        JOptionPane.showMessageDialog(this,
                "Your opponent has disconnected. You win by default!",
                "Opponent Disconnected",
                JOptionPane.INFORMATION_MESSAGE);

        gameOver(playerColor);
    }

    /**
     * Handles server disconnection scenario Shows error message and returns to
     * home screen
     */
    public void serverDisconnected() {
        JOptionPane.showMessageDialog(this,
                "Connection to server lost!",
                "Server Disconnected",
                JOptionPane.ERROR_MESSAGE);

        returnToHome();
    }

    /**
     * Closes the current game window and returns to the home screen
     */
    private void returnToHome() {
        this.setVisible(false);
        this.dispose();

        // Create new home window on the event dispatch thread
        java.awt.EventQueue.invokeLater(() -> {

            home h = home.getInstance();
            if (!h.isVisible()) {
                h.setVisible(true);
            }

        });

    }

    /**
     * Handles mouse clicks on chess board squares Manages piece selection, move
     * validation, and move execution
     *
     * @param displayPos The position clicked in display coordinates
     */
    private void handleClick(String displayPos) {
        // Prevent moves when it's not the player's turn
        if (!isMyTurn) {
            JOptionPane.showMessageDialog(this, "It's not your turn!", "Wait", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Convert display position to actual board position
        String actualPos = convertDisplayToActual(displayPos);

        if (selectedPosition == null) {
            // First click - select a piece
            Piece piece = controller.getBoard().getPieceAt(actualPos);
            // Only allow selection of player's own pieces
            if (piece != null && piece.getColor().equals(playerColor)) {
                selectedPosition = actualPos;
                // Highlight selected square in yellow
                buttonMap.get(displayPos).setBackground(Color.YELLOW);

                // Highlight valid moves in green
                java.util.List<String> validMoves = controller.getValidMoves(actualPos);
                for (String move : validMoves) {
                    String displayMove = convertDisplayToActual(move);
                    if (buttonMap.containsKey(displayMove)) {
                        buttonMap.get(displayMove).setBackground(Color.GREEN);
                    }
                }
            }
        } else {
            // Second click - attempt to move to the clicked square
            if (!selectedPosition.equals(actualPos)) {
                // Try to make the move
                if (controller.movePiece(selectedPosition, actualPos)) {
                    try {
                        // Send move to server if successful
                        client.sendMove(selectedPosition, actualPos);
                    } catch (IOException ex) {
                        Logger.getLogger(NetworkGame.class.getName()).log(Level.SEVERE, null, ex);
                        JOptionPane.showMessageDialog(this, "Failed to send move to server!", "Network Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            // Reset board highlighting and clear selection
            resetHighlights();
            selectedPosition = null;
        }
    }

    /**
     * Resets all square highlights back to the original chess board pattern
     * Light squares: cream color, Dark squares: brown color
     */
    private void resetHighlights() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                JButton btn = buttonGrid[row][col];
                // Alternate square colors in checkerboard pattern
                if ((row + col) % 2 == 0) {
                    btn.setBackground(new java.awt.Color(240, 217, 181)); // Light squares
                } else {
                    btn.setBackground(new java.awt.Color(181, 136, 99)); // Dark squares
                }
            }
        }
    }

    /**
     * Receives and processes moves from the opponent via the server
     *
     * @param from Starting position of the opponent's move
     * @param to Ending position of the opponent's move
     */
    public void receiveMove(String from, String to) {
        ChessBoard board = controller.getBoard();
        Piece piece = board.getPieceAt(from);

        if (piece != null) {
            // Store original turn state
            String originalTurn = controller.getCurrentTurn();

            // Switch turns if it was originally this player's turn
            if (originalTurn.equals(playerColor)) {
                controller.switchTurns();
            }

            // Apply the opponent's move
            boolean moveSuccessful = controller.movePiece(from, to);

            if (moveSuccessful) {
                // Update display if move was successful
                updateBoard();
                updateTurnDisplay();
            } else {
                System.out.println("Failed to apply opponent's move: " + from + " to " + to);
            }
        }
    }

    /**
     * Converts display position to actual board position Handles board flipping
     * for black player perspective
     *
     * @param displayPosition Position as shown on the display (e.g., "e4")
     * @return Actual board position
     */
    private String convertDisplayToActual(String displayPosition) {
        if (playerColor.equals("W")) {
            // White player sees board normally
            return displayPosition;
        }

        // Black player sees flipped board - convert coordinates
        char file = displayPosition.charAt(0);
        char rank = displayPosition.charAt(1);

        // Flip file (a->h, b->g, etc.) and rank (1->8, 2->7, etc.)
        char flippedFile = (char) ('a' + ('h' - file));
        char flippedRank = (char) ('1' + ('8' - rank));

        return "" + flippedFile + flippedRank;
    }

    //Swing GUI
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        BoardPanel = new javax.swing.JPanel();
        turnLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout BoardPanelLayout = new javax.swing.GroupLayout(BoardPanel);
        BoardPanel.setLayout(BoardPanelLayout);
        BoardPanelLayout.setHorizontalGroup(
            BoardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 369, Short.MAX_VALUE)
        );
        BoardPanelLayout.setVerticalGroup(
            BoardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 399, Short.MAX_VALUE)
        );

        turnLabel.setText("Who's Turn");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(93, 93, 93)
                .addComponent(BoardPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(121, 121, 121)
                .addComponent(turnLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(19, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(67, 67, 67)
                        .addComponent(BoardPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(126, 126, 126)
                        .addComponent(turnLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(101, Short.MAX_VALUE))
        );

        BoardPanel.setLayout(new java.awt.GridLayout(8, 8)); // 8 rows, 8 cols

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                JButton btn = new JButton();
                btn.setPreferredSize(new java.awt.Dimension(64, 64)); // optional
                btn.setFont(new Font("SansSerif", Font.PLAIN, 30));
                String position = "" + columns[col] + (8 - row);
                btn.setName(position); // useful for debugging or retrieval
                buttonGrid[row][col] = btn;

                // Optional: color pattern
                if ((row + col) % 2 == 0) {
                    btn.setBackground(new java.awt.Color(240, 217, 181)); // light
                } else {
                    btn.setBackground(new java.awt.Color(181, 136, 99)); // dark
                }
                buttonMap.put(position, btn);

                btn.addActionListener(e -> {
                    JButton clicked = (JButton) e.getSource();
                    String pos = clicked.getName(); // e.g., "e2"
                    handleClick(pos);
                });

                BoardPanel.add(btn);
            }
        }

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Override default close operation to handle server disconnection Ensures
     * client properly disconnects from server when window is closed
     */
    @Override
    public void setDefaultCloseOperation(int operation) {
        super.setDefaultCloseOperation(operation);
        // Disconnect from server when closing
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (client != null) {
                    client.disconnect();
                }
                System.exit(0);
            }
        });
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel BoardPanel;
    protected javax.swing.JLabel turnLabel;
    // End of variables declaration//GEN-END:variables
}
