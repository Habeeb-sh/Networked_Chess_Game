package com.mycompany.chess;

import java.util.Map;

/**
 * Rook chess piece class that extends the base Piece class. The Rook moves
 * horizontally and vertically across the board.
 */
public class Rook extends Piece {

    // Track whether this rook has moved (important for castling rules)
    private boolean hasMoved = false;

    /**
     * Constructor for Rook piece
     *
     * @param id The unique identifier for this rook piece
     * @param color The color of the piece ("W" for white, "B" for black)
     */
    public Rook(String id, String color) {
        // Initialize the rook with its type, id, color, and Unicode symbol
        super("rook", id, color, color.equals("W") ? "♖" : "♜");
    }

    /**
     * Validates if a move from one position to another is legal for a Rook
     *
     * @param from Starting position (e.g., "a1")
     * @param to Destination position (e.g., "a8")
     * @param boardState Current state of the chess board with all pieces
     * @return true if the move is valid, false otherwise
     */
    @Override
    public boolean isValidMove(String from, String to, Map<String, Piece> boardState) {
        // Convert chess notation to array coordinates
        // Column: 'a'=0, 'b'=1, etc.
        int fromCol = from.charAt(0) - 'a';
        int fromRow = 8 - Character.getNumericValue(from.charAt(1)); // Row: '8'=0, '7'=1, etc.
        int toCol = to.charAt(0) - 'a';
        int toRow = 8 - Character.getNumericValue(to.charAt(1));

        // Rook can only move horizontally or vertically, not diagonally
        if (fromCol != toCol && fromRow != toRow) {
            return false;
        }

        // Check for vertical movement (same column)
        if (fromCol == toCol) {
            // Determine direction of movement (up or down)
            int step = fromRow < toRow ? 1 : -1;
            // Check each square between start and end positions
            for (int row = fromRow + step; row != toRow; row += step) {
                // Convert back to chess notation to check board state
                String pos = "" + from.charAt(0) + (8 - row);
                // If there's a piece blocking the path, move is invalid
                if (boardState.containsKey(pos)) {
                    return false;
                }
            }
        } else { // Horizontal movement (same row)
            // Determine direction of movement (left or right)
            int step = fromCol < toCol ? 1 : -1;
            // Check each square between start and end positions
            for (int col = fromCol + step; col != toCol; col += step) {
                // Convert back to chess notation to check board state
                String pos = "" + (char) ('a' + col) + from.charAt(1);
                // If there's a piece blocking the path, move is invalid
                if (boardState.containsKey(pos)) {
                    return false;
                }
            }
        }

        // Check if destination square is empty or contains an opponent's piece
        Piece target = boardState.get(to);
        return target == null || !target.color.equals(color);
    }

    /**
     * Sets whether this rook has moved (used for castling logic)
     *
     * @param moved true if the rook has moved, false otherwise
     */
    public void setHasMoved(boolean moved) {
        this.hasMoved = moved;
    }

    /**
     * Checks if this rook has moved from its starting position
     *
     * @return true if the rook has moved, false otherwise
     */
    public boolean hasMoved() {
        return hasMoved;
    }
}
