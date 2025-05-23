package com.mycompany.chess;

/**
 * Represents a chess move, including special moves like castling and en
 * passant. Stores all information needed to understand and potentially undo
 * moves.
 */
public class Move {

    // Source and destination positions
    private String from;
    private String to;

    // The piece that moved
    private Piece movedPiece;

    // The piece that was captured (null if no capture)
    private Piece capturedPiece;

    // Flags for special move types
    private boolean isEnPassant;
    private boolean isCastling;

    // Additional information for castling moves
    private String rookFrom;  // Original rook position
    private String rookTo;    // Final rook position

    /**
     * Constructor for regular moves (including captures and en passant)
     *
     * @param from Starting position
     * @param to Ending position
     * @param movedPiece The piece that moved
     * @param capturedPiece The piece that was captured (null if none)
     */
    public Move(String from, String to, Piece movedPiece, Piece capturedPiece) {
        this.from = from;
        this.to = to;
        this.movedPiece = movedPiece;
        this.capturedPiece = capturedPiece;
        this.isCastling = false;
        this.rookFrom = null;
        this.rookTo = null;

        // Detect en passant: pawn moves diagonally but captured piece isn't at destination
        this.isEnPassant = movedPiece.getName().equals("pawn")
                && capturedPiece != null
                && capturedPiece.getName().equals("pawn")
                && from.charAt(0) != to.charAt(0) // Diagonal move
                && !to.equals(capturedPiece.getId().substring(2)); // Captured piece not at destination
    }

    /**
     * Constructor for castling moves
     *
     * @param from King's starting position
     * @param to King's ending position
     * @param movedPiece The king that moved
     * @param rookFrom Rook's starting position
     * @param rookTo Rook's ending position
     */
    public Move(String from, String to, Piece movedPiece, String rookFrom, String rookTo) {
        this.from = from;
        this.to = to;
        this.movedPiece = movedPiece;
        this.capturedPiece = null;        // Castling never captures
        this.isEnPassant = false;         // Castling is not en passant
        this.isCastling = true;
        this.rookFrom = rookFrom;
        this.rookTo = rookTo;
    }

    /**
     * Returns a human-readable string representation of the move
     */
    @Override
    public String toString() {
        // Special notation for castling moves
        if (isCastling) {
            // Kingside castling (king moves right) vs Queenside castling (king moves left)
            return (to.charAt(0) > from.charAt(0)) ? "O-O" : "O-O-O";
        }

        // Standard move notation
        String moveNotation = movedPiece.getId() + " " + from + " to " + to;

        // Add capture information if applicable
        if (capturedPiece != null) {
            moveNotation += " (captured " + capturedPiece.getId() + ")";

        }
        return moveNotation;
    }

    // Getter methods for accessing move information
    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public Piece getMovedPiece() {
        return movedPiece;
    }

    public Piece getCapturedPiece() {
        return capturedPiece;
    }

    public boolean isEnPassant() {
        return isEnPassant;
    }

    public boolean isCastling() {
        return isCastling;
    }

    public String getRookFrom() {
        return rookFrom;
    }

    public String getRookTo() {
        return rookTo;
    }
}
