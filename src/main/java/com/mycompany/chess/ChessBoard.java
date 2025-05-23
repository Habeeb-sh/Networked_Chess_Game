package com.mycompany.chess;

import java.util.HashMap;
import java.util.Map;

/**
 * ChessBoard class manages the chess board state and piece movements. Handles
 * board initialization, piece movement validation, and game state checks.
 */
public class ChessBoard {

    // HashMap to store piece positions using algebraic notation (e.g., "a1", "h8")
    private Map<String, Piece> boardState = new HashMap<>();
    // Array of column letters for board traversal
    private final char[] columns = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};

    /**
     * Initializes the chess board with all pieces in their starting positions.
     * Sets up both black pieces (rank 8 and 7) and white pieces (rank 1 and 2).
     */
    public void initializeBoard() {
        // Clear any existing pieces from the board
        boardState.clear();

        // Initialize black pieces on rank 8 (back row)
        boardState.put("a8", new Rook("BR1", "B"));      // Black rook queenside
        boardState.put("b8", new Knight("BN1", "B"));    // Black knight queenside
        boardState.put("c8", new Bishop("BB1", "B"));    // Black bishop queenside
        boardState.put("d8", new Queen("BQ", "B"));      // Black queen
        boardState.put("e8", new King("BK", "B"));       // Black king
        boardState.put("f8", new Bishop("BB2", "B"));    // Black bishop kingside
        boardState.put("g8", new Knight("BN2", "B"));    // Black knight kingside
        boardState.put("h8", new Rook("BR2", "B"));      // Black rook kingside

        // Initialize black pawns on rank 7
        for (char col : columns) {
            boardState.put(col + "7", new Pawn("BP" + col, "B"));
        }

        // Initialize white pieces on rank 1 (back row)
        boardState.put("a1", new Rook("WR1", "W"));      // White rook queenside
        boardState.put("b1", new Knight("WN1", "W"));    // White knight queenside
        boardState.put("c1", new Bishop("WB1", "W"));    // White bishop queenside
        boardState.put("d1", new Queen("WQ", "W"));      // White queen
        boardState.put("e1", new King("WK", "W"));       // White king
        boardState.put("f1", new Bishop("WB2", "W"));    // White bishop kingside
        boardState.put("g1", new Knight("WN2", "W"));    // White knight kingside
        boardState.put("h1", new Rook("WR2", "W"));      // White rook kingside

        // Initialize white pawns on rank 2
        for (char col : columns) {
            boardState.put(col + "2", new Pawn("WP" + col, "W"));
        }
    }

    /**
     * Gets the piece at the specified position.
     *
     * @param position Algebraic notation position (e.g., "e4")
     * @return Piece at the position, or null if empty
     */
    public Piece getPieceAt(String position) {
        return boardState.get(position);
    }

    /**
     * Moves a piece from one position to another. Does not validate the move -
     * assumes it's already been validated.
     *
     * @param from Source position in algebraic notation
     * @param to Destination position in algebraic notation
     */
    public void movePiece(String from, String to) {
        Piece piece = boardState.get(from);  // Get the piece to move
        boardState.put(to, piece);           // Place piece at destination
        boardState.remove(from);             // Remove piece from source
    }

    /**
     * Places a piece at the specified position. Used for piece promotion or
     * special moves.
     *
     * @param position Position in algebraic notation
     * @param piece Piece to place
     */
    public void placePiece(String position, Piece piece) {
        boardState.put(position, piece);
    }

    /**
     * Removes and returns the piece at the specified position.
     *
     * @param position Position in algebraic notation
     * @return The removed piece, or null if position was empty
     */
    public Piece removePiece(String position) {
        return boardState.remove(position);
    }

    /**
     * Checks if the king of the specified color is in check.
     *
     * @param color Color of the king to check ("W" for white, "B" for black)
     * @return true if king is in check, false otherwise
     */
    public boolean isKingInCheck(String color) {
        // Find the position of the king for the specified color
        String kingPos = findKingPosition(color);
        if (kingPos == null) {
            return false;  // No king found (shouldn't happen in normal game)
        }

        // Determine opponent's color
        String opponentColor = color.equals("W") ? "B" : "W";

        // Create a copy of board state to avoid modification during iteration
        Map<String, Piece> boardStateCopy = new HashMap<>(boardState);

        // Check if any opponent piece can attack the king
        for (Map.Entry<String, Piece> entry : boardStateCopy.entrySet()) {
            if (entry.getValue().getColor().equals(opponentColor)) {
                // If opponent piece can move to king's position, king is in check
                if (entry.getValue().isValidMove(entry.getKey(), kingPos, boardState)) {
                    return true;
                }
            }
        }

        return false;  // King is not in check
    }

    /**
     * Finds the position of the king for the specified color.
     *
     * @param color Color of the king to find ("W" or "B")
     * @return Position of the king in algebraic notation, or null if not found
     */
    public String findKingPosition(String color) {
        // Search through all pieces on the board
        for (Map.Entry<String, Piece> entry : boardState.entrySet()) {
            if (entry.getValue().getName().equals("king")
                    && entry.getValue().getColor().equals(color)) {
                return entry.getKey();  // Return position of matching king
            }
        }
        return null;  // King not found (shouldn't happen in normal game)
    }

    /**
     * Checks if moving a piece would put the player's own king in check. Used
     * to validate moves and prevent illegal moves that expose the king.
     *
     * @param from Source position of the move
     * @param to Destination position of the move
     * @param color Color of the player making the move
     * @return true if the move would cause check, false if safe
     */
    public boolean wouldMoveCauseCheck(String from, String to, String color) {
        // Get the piece being moved
        Piece piece = boardState.get(from);
        if (piece == null) {
            return false;  // No piece to move
        }

        // Create a temporary board state to simulate the move
        Map<String, Piece> tempBoardState = new HashMap<>(boardState);

        // Store the piece that might be captured (for restoration)
        Piece targetPiece = tempBoardState.get(to);
        // Simulate the move
        tempBoardState.remove(from);
        tempBoardState.put(to, piece);

        // Find the king's position after the simulated move
        String kingPos = null;
        for (Map.Entry<String, Piece> entry : tempBoardState.entrySet()) {
            if (entry.getValue().getName().equals("king")
                    && entry.getValue().getColor().equals(color)) {
                kingPos = entry.getKey();
                break;
            }
        }

        if (kingPos == null) {
            return false;  // No king found (shouldn't happen)
        }

        // Check if any opponent piece can attack the king in this position
        String opponentColor = color.equals("W") ? "B" : "W";
        boolean isCheck = false;

        for (Map.Entry<String, Piece> entry : tempBoardState.entrySet()) {
            if (entry.getValue().getColor().equals(opponentColor)) {
                if (entry.getValue().isValidMove(entry.getKey(), kingPos, tempBoardState)) {
                    isCheck = true;
                    break;
                }
            }
        }

        return isCheck;  // Return whether the move would cause check
    }

    /**
     * Gets the current board state map.
     *
     * @return Map of positions to pieces representing current board state
     */
    public Map<String, Piece> getBoardState() {
        return boardState;
    }

    /**
     * Gets all valid moves for a piece at the specified position.
     *
     * @param position Position of the piece in algebraic notation
     * @param checkForCheck Whether to filter out moves that would cause check
     * @return List of valid destination positions
     */
    public java.util.List<String> getValidMovesForPiece(String position, boolean checkForCheck) {
        Piece piece = boardState.get(position);
        java.util.List<String> validMoves = new java.util.ArrayList<>();

        if (piece == null) {
            return validMoves;  // No piece at position
        }

        // Check every possible destination square on the board
        for (char col : columns) {
            for (int row = 1; row <= 8; row++) {
                String targetPos = "" + col + row;
                // Skip if trying to move to same position
                if (!position.equals(targetPos) && piece.isValidMove(position, targetPos, boardState)) {
                    // If checking for check, ensure move doesn't expose king
                    if (!checkForCheck || !wouldMoveCauseCheck(position, targetPos, piece.getColor())) {
                        validMoves.add(targetPos);
                    }
                }
            }
        }

        return validMoves;
    }

    /**
     * Checks if the specified color has any legal moves available. Used to
     * detect checkmate and stalemate conditions.
     *
     * @param color Color to check for legal moves ("W" or "B")
     * @return true if player has at least one legal move, false otherwise
     */
    public boolean hasLegalMoves(String color) {
        // Create copy to avoid concurrent modification
        Map<String, Piece> boardStateCopy = new HashMap<>(boardState);

        // Check each piece of the specified color
        for (Map.Entry<String, Piece> entry : boardStateCopy.entrySet()) {
            if (entry.getValue().getColor().equals(color)) {
                String from = entry.getKey();
                // If piece has any valid moves, player is not stuck
                if (!getValidMovesForPiece(from, true).isEmpty()) {
                    return true;
                }
            }
        }
        return false;  // No legal moves available
    }

    /**
     * Gets the array of column letters used for board traversal.
     *
     * @return Array of column characters from 'a' to 'h'
     */
    public char[] getColumns() {
        return columns;
    }
}
