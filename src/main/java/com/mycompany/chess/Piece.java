package com.mycompany.chess;

import java.util.Map;

/**
 * Abstract base class for all chess pieces. Defines common properties and
 * methods that all pieces must implement.
 */
public abstract class Piece {

    protected String name;     // Type of piece (e.g., "pawn", "rook", "king")
    protected String id;       // Unique identifier for this specific piece
    protected String color;    // Color of the piece ("W" for white, "B" for black)
    protected String symbol;   // Unicode symbol for display

    /**
     * Constructor for chess piece.
     *
     * @param name Type of the piece
     * @param id Unique identifier
     * @param color Color of the piece
     * @param symbol Unicode symbol for display
     */
    public Piece(String name, String id, String color, String symbol) {
        this.name = name;
        this.id = id;
        this.color = color;
        this.symbol = symbol;
    }

    // Getter methods for piece properties
    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getColor() {
        return color;
    }

    public String getSymbol() {
        return symbol;
    }

    /**
     * Abstract method that must be implemented by each piece type. Validates
     * whether a move from one position to another is legal for this piece.
     *
     * @param from Source position in algebraic notation
     * @param to Destination position in algebraic notation
     * @param boardState Current state of the board
     * @return true if move is valid for this piece type, false otherwise
     */
    public abstract boolean isValidMove(String from, String to, Map<String, Piece> boardState);
}
