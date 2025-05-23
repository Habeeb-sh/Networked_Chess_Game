package com.mycompany.chess;

import java.util.Map;

/**
 * Bishop piece class implementing diagonal movement rules. Bishops move
 * diagonally any number of squares but cannot jump over pieces.
 */
public class Bishop extends Piece {

    /**
     * Constructor for Bishop piece.
     *
     * @param id Unique identifier for this piece
     * @param color Color of the piece ("W" for white, "B" for black)
     */
    public Bishop(String id, String color) {
        // Call parent constructor with piece type, id, color, and Unicode symbol
        super("bishop", id, color, color.equals("W") ? "♗" : "♝");
    }

    /**
     * Validates if a move is legal for a bishop. Bishops can only move
     * diagonally and cannot jump over other pieces.
     *
     * @param from Source position in algebraic notation
     * @param to Destination position in algebraic notation
     * @param boardState Current state of the board
     * @return true if move is valid, false otherwise
     */
    @Override
    public boolean isValidMove(String from, String to, Map<String, Piece> boardState) {
        // Convert algebraic notation to array indices
        int fromCol = from.charAt(0) - 'a';  // Convert 'a'-'h' to 0-7
        int fromRow = 8 - Character.getNumericValue(from.charAt(1));  // Convert rank to 0-7
        int toCol = to.charAt(0) - 'a';
        int toRow = 8 - Character.getNumericValue(to.charAt(1));

        // Check if move is diagonal (absolute difference in rows equals absolute difference in columns)
        if (Math.abs(toCol - fromCol) != Math.abs(toRow - fromRow)) {
            return false;  // Not a diagonal move
        }

        // Determine direction of movement (1 for increasing, -1 for decreasing)
        int colStep = toCol > fromCol ? 1 : -1;
        int rowStep = toRow > fromRow ? 1 : -1;

        // Start checking from the square after the starting position
        int col = fromCol + colStep;
        int row = fromRow + rowStep;

        // Check each square along the diagonal path (excluding destination)
        while (col != toCol && row != toRow) {
            // Convert back to algebraic notation
            String pos = "" + (char) ('a' + col) + (8 - row);
            // If any square along the path is occupied, move is blocked
            if (boardState.containsKey(pos)) {
                return false;
            }
            col += colStep;
            row += rowStep;
        }

        // Check destination square - can move there if empty or contains opponent piece
        Piece target = boardState.get(to);
        return target == null || !target.color.equals(color);
    }

}
