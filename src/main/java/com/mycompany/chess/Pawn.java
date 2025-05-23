package com.mycompany.chess;

import java.util.Map;

/**
 * Pawn piece class implementing pawn movement rules. Pawns move forward one
 * square, can move two squares from starting position, attack diagonally, and
 * can capture en passant.
 */
public class Pawn extends Piece {

    /**
     * Constructor for Pawn piece.
     *
     * @param id Unique identifier for this piece
     * @param color Color of the piece ("W" for white, "B" for black)
     */
    public Pawn(String id, String color) {
        // Call parent constructor with piece type, id, color, and Unicode symbol
        super("pawn", id, color, color.equals("W") ? "♙" : "♟");
    }

    /**
     * Validates if a move is legal for a pawn. Handles forward movement,
     * initial two-square move, diagonal captures, and en passant.
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

        // Determine movement direction based on color (white moves up, black moves down)
        int direction = color.equals("W") ? -1 : 1;

        // Forward movement (same column)
        if (fromCol == toCol) {
            // Single square forward move
            if (toRow == fromRow + direction && !boardState.containsKey(to)) {
                return true;  // Valid forward move to empty square
            }
            // Double square forward move from starting position
            if ((fromRow == (color.equals("W") ? 6 : 1)) // Check if on starting rank
                    && toRow == fromRow + 2 * direction
                    && !boardState.containsKey(to) // Destination must be empty
                    && !boardState.containsKey(from.substring(0, 1) + (8 - (fromRow + direction)))) {  // Path must be clear
                return true;  // Valid initial two-square move
            }
        } // Diagonal movement (capture)
        else if (Math.abs(toCol - fromCol) == 1 && toRow == fromRow + direction) {
            Piece target = boardState.get(to);
            // Normal diagonal capture
            if (target != null && !target.color.equals(color)) {
                return true;  // Valid capture of opponent piece
            }
            // En passant capture
            if (target == null) {
                // Position of the pawn that might be captured en passant
                String enPassantPos = to.substring(0, 1) + from.charAt(1);
                Piece enPassantPiece = boardState.get(enPassantPos);

                // Check conditions for en passant capture
                if (enPassantPiece != null
                        && enPassantPiece.getName().equals("pawn") // Must be a pawn
                        && !enPassantPiece.getColor().equals(color) // Must be opponent's pawn
                        && GameController.getLastMove() != null) {  // Must have access to last move

                    Move lastMove = GameController.getLastMove();
                    // Verify last move was a two-square pawn move to the en passant position
                    if (lastMove.getMovedPiece().getName().equals("pawn")
                            && lastMove.getTo().equals(enPassantPos)
                            && Math.abs(Character.getNumericValue(lastMove.getFrom().charAt(1))
                                    - Character.getNumericValue(lastMove.getTo().charAt(1))) == 2) {
                        return true;  // Valid en passant capture
                    }
                }
            }
        }

        return false;  // Invalid pawn move
    }
}
