package com.mycompany.chess;

import java.util.Map;

/**
 * Knight piece class implementing L-shaped movement rules. Knights move in an
 * L-shape: 2 squares in one direction and 1 square perpendicular.
 */
public class Knight extends Piece {

    /**
     * Constructor for Knight piece.
     *
     * @param id Unique identifier for this piece
     * @param color Color of the piece ("W" for white, "B" for black)
     */
    public Knight(String id, String color) {
        // Call parent constructor with piece type, id, color, and Unicode symbol
        super("knight", id, color, color.equals("W") ? "♘" : "♞");
    }

    /**
     * Validates if a move is legal for a knight. Knights move in an L-shape and
     * can jump over other pieces.
     *
     * @param from Source position in algebraic notation
     * @param to Destination position in algebraic notation
     * @param boardState Current state of the board
     * @return true if move is valid, false otherwise
     */
    @Override
    public boolean isValidMove(String from, String to, Map<String, Piece> boardState) {
        // Convert positions to coordinates
        int fromCol = from.charAt(0) - 'a';
        int fromRow = 8 - Character.getNumericValue(from.charAt(1));
        int toCol = to.charAt(0) - 'a';
        int toRow = 8 - Character.getNumericValue(to.charAt(1));

        // Calculate the differences in position
        int colDiff = Math.abs(toCol - fromCol);
        int rowDiff = Math.abs(toRow - fromRow);

        // Valid knight moves: (2,1) or (1,2) in any direction
        if ((colDiff == 2 && rowDiff == 1) || (colDiff == 1 && rowDiff == 2)) {
            Piece target = boardState.get(to);
            // Can move to empty square or square with opponent piece
            return target == null || !target.color.equals(color);
        }

        return false;  // Not a valid knight move
    }

}
