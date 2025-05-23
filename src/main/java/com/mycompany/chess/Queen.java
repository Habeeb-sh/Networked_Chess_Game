package com.mycompany.chess;

import java.util.Map;

/**
 * Queen chess piece class that extends the base Piece class. The Queen can move
 * like both a Rook (horizontally/vertically) and Bishop (diagonally).
 */
public class Queen extends Piece {

    /**
     * Constructor for Queen piece
     *
     * @param id The unique identifier for this queen piece
     * @param color The color of the piece ("W" for white, "B" for black)
     */
    public Queen(String id, String color) {
        // Initialize the queen with its type, id, color, and Unicode symbol
        super("queen", id, color, color.equals("W") ? "♕" : "♛");
    }

    /**
     * Validates if a move from one position to another is legal for a Queen
     *
     * @param from Starting position (e.g., "e2")
     * @param to Destination position (e.g., "e4")
     * @param boardState Current state of the chess board with all pieces
     * @return true if the move is valid, false otherwise
     */
    @Override
    public boolean isValidMove(String from, String to, Map<String, Piece> boardState) {
        // Create temporary Rook and Bishop instances to leverage their movement logic
        Rook rook = new Rook(id, color);
        Bishop bishop = new Bishop(id, color);

        // Queen can move if the move is valid for either a rook OR a bishop
        return rook.isValidMove(from, to, boardState)
                || bishop.isValidMove(from, to, boardState);
    }
}
