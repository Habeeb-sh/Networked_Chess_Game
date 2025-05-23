package com.mycompany.chess;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import java.awt.Font;
import javax.swing.JOptionPane;

public class Game extends javax.swing.JFrame implements GameController.GameView {

    // 2D array to store button references for each square of the chess board
    private JButton[][] buttonGrid = new JButton[8][8];

    // Map to quickly access buttons by their chess position (e.g., "e4", "a1")
    private Map<String, JButton> buttonMap = new HashMap<>();

    // Stores the currently selected piece position, null if no piece is selected
    private String selectedPosition = null;

    // Controller that handles game logic
    private GameController controller;

    // Array representing chess board columns (a through h)
    private final char[] columns = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};

    /**
     * Constructor - initializes the game window and sets up the initial state
     */
    public Game() {
        // Create controller and set this view as the observer
        controller = new GameController();
        controller.setView(this);

        // Initialize Swing components
        initComponents();

        // Set initial turn label based on current player
        turnLabel.setText(controller.getCurrentTurn().equals("W") ? "White's Turn" : "Black's Turn");

        // Update the board display with initial piece positions
        updateBoard();
    }

    /**
     * Implementation of GameView interface method. Updates the visual
     * representation of all pieces on the board.
     */
    @Override
    public void updateBoard() {
        ChessBoard board = controller.getBoard();

        // Iterate through all button positions and update their display
        for (Map.Entry<String, JButton> entry : buttonMap.entrySet()) {
            String position = entry.getKey();
            JButton button = entry.getValue();

            // Get the piece at this position (null if empty square)
            Piece piece = board.getPieceAt(position);
            if (piece != null) {
                // Display piece symbol and set appropriate color
                button.setText(piece.getSymbol());
                if (piece.getColor().equals("W")) {
                    button.setForeground(Color.WHITE);
                } else {
                    button.setForeground(Color.BLACK);
                }
            } else {
                // Clear empty squares
                button.setText("");
            }
        }
    }

    /**
     * Implementation of GameView interface method. Updates the turn indicator
     * label.
     */
    @Override
    public void updateTurn(String turn) {
        turnLabel.setText(turn.equals("W") ? "White's Turn" : "Black's Turn");
    }

    /**
     * Implementation of GameView interface method. Displays dialog messages to
     * the user (checkmate, check, etc.).
     */
    @Override
    public void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    /**
     * Implementation of GameView interface method. Shows promotion dialog when
     * a pawn reaches the end of the board. Returns the piece type the user
     * chooses for promotion.
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

        // Create and return the chosen piece type
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
                // Default to Queen if dialog is closed without selection
                return new Queen(color.equals("W") ? "WQ" : "BQ", color);
        }
    }

    /**
     * Handles user clicks on chess board squares. Implements piece selection
     * and move execution logic.
     */
    private void handleClick(String pos) {
        if (selectedPosition == null) {
            // No piece currently selected - try to select a piece
            Piece piece = controller.getBoard().getPieceAt(pos);

            // Only allow selection of pieces belonging to current player
            if (piece != null && piece.getColor().equals(controller.getCurrentTurn())) {
                selectedPosition = pos;

                // Highlight selected piece in yellow
                buttonMap.get(pos).setBackground(Color.YELLOW);

                // Get and highlight all valid moves in green
                java.util.List<String> validMoves = controller.getValidMoves(pos);
                for (String move : validMoves) {
                    buttonMap.get(move).setBackground(Color.GREEN);
                }
            }
        } else {
            // A piece is already selected - try to make a move
            if (!selectedPosition.equals(pos)) {
                // Attempt to move to the clicked position
                controller.movePiece(selectedPosition, pos);
            }

            // Reset all square highlights and clear selection
            resetHighlights();
            selectedPosition = null;
        }
    }

    /**
     * Resets all square background colors to their default chess board pattern.
     * Light squares: beige color, Dark squares: brown color
     */
    private void resetHighlights() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                JButton btn = buttonGrid[row][col];

                // Alternate colors based on square position
                if ((row + col) % 2 == 0) {
                    btn.setBackground(new java.awt.Color(240, 217, 181)); // Light squares
                } else {
                    btn.setBackground(new java.awt.Color(181, 136, 99));  // Dark squares
                }
            }
        }
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
/*
    public static void main(String args[]) {

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Game().setVisible(true);
            }
        });
    }

*/
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel BoardPanel;
    protected javax.swing.JLabel turnLabel;
    // End of variables declaration//GEN-END:variables
}
