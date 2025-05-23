package com.mycompany.chess;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * Controls the chess game logic, including move validation, turn management,
 * and game state checking (check, checkmate, stalemate).
 */
public class GameController {

    // The chess board that maintains piece positions
    private ChessBoard board;

    // Current player's turn: "W" for white, "B" for black
    private String currentTurn = "W";

    // History of all moves made in the game
    private static List<Move> moveHistory = new ArrayList<>();

    // Reference to the view (GUI) for updates and user interaction
    private GameView view;

    /**
     * Constructor - initializes a new game with standard starting position
     */
    public GameController() {
        board = new ChessBoard();
        board.initializeBoard(); // Set up pieces in starting positions
    }

    /**
     * Sets the view component that will receive updates from this controller
     */
    public void setView(GameView view) {
        this.view = view;
    }

    /**
     * Returns the current player's turn ("W" or "B")
     */
    public String getCurrentTurn() {
        return currentTurn;
    }

    /**
     * Attempts to move a piece from one position to another. Handles all move
     * validation, special moves, and game state updates.
     *
     * @param from Starting position (e.g., "e2")
     * @param to Target position (e.g., "e4")
     * @return true if move was successful, false otherwise
     */
    public boolean movePiece(String from, String to) {
        // Get the piece at the starting position
        Piece piece = board.getPieceAt(from);

        // Validate basic move conditions
        if (piece == null || !piece.getColor().equals(currentTurn) || !piece.isValidMove(from, to, board.getBoardState())) {
            return false;
        }

        // Check if move would put own king in check (illegal move)
        if (board.wouldMoveCauseCheck(from, to, currentTurn)) {
            if (view != null) {
                view.showMessage("You cannot move into check!", "Illegal Move", JOptionPane.WARNING_MESSAGE);
            }
            return false;
        }

        // Variables to track castling moves
        boolean isCastling = false;
        String rookFrom = null;
        String rookTo = null;

        // Check if this is a castling move (king moves 2 squares horizontally)
        if (piece.getName().equals("king") && Math.abs(from.charAt(0) - to.charAt(0)) == 2) {
            int kingRow = piece.getColor().equals("W") ? 1 : 8; // King's starting row
            isCastling = true;

            // Determine if kingside (right) or queenside (left) castling
            boolean isKingside = to.charAt(0) > from.charAt(0);
            rookFrom = (isKingside ? "h" : "a") + kingRow;     // Rook's current position
            rookTo = "" + (char) ('a' + (isKingside ? 5 : 3)) + kingRow; // Rook's destination

            // Move the rook as part of castling
            Piece rook = board.getPieceAt(rookFrom);
            board.placePiece(rookTo, rook);
            board.removePiece(rookFrom);

            // Mark rook as having moved
            if (rook instanceof Rook) {
                ((Rook) rook).setHasMoved(true);
            }
        }

        // Handle piece capture or en passant
        Piece capturedPiece = board.getPieceAt(to);

        // Check for en passant capture (pawn captures diagonally to empty square)
        if (piece.getName().equals("pawn") && Math.abs(from.charAt(0) - to.charAt(0)) == 1 && capturedPiece == null) {
            // Calculate position of pawn to be captured en passant
            String enPassantPos = to.substring(0, 1) + from.charAt(1);
            Piece enPassantPiece = board.getPieceAt(enPassantPos);

            // Verify it's a valid en passant capture
            if (enPassantPiece != null
                    && enPassantPiece.getName().equals("pawn")
                    && !enPassantPiece.getColor().equals(piece.getColor())) {
                capturedPiece = enPassantPiece;
                board.removePiece(enPassantPos); // Remove the captured pawn
            }
        }

        // Check for pawn promotion
        piece = checkForPromotion(piece, to);

        // Record the move in history
        if (isCastling) {
            recordCastlingMove(from, to, piece, rookFrom, rookTo);
        } else {
            recordMove(from, to, piece, capturedPiece);
        }

        // Execute the move on the board
        board.placePiece(to, piece);
        board.removePiece(from);

        // Update piece state for pieces that track movement
        if (piece instanceof King) {
            ((King) piece).setHasMoved(true);
        } else if (piece instanceof Rook) {
            ((Rook) piece).setHasMoved(true);
        }

        // Update the visual board
        if (view != null) {
            view.updateBoard();
        }

        // Check for game ending conditions and notify about check
        checkGameStateAfterMove();

        // Switch to the other player's turn
        switchTurns();

        return true;
    }

    /**
     * Checks if a pawn has reached the promotion rank and handles promotion.
     *
     * @param piece The piece that moved
     * @param to The destination position
     * @return The original piece or promoted piece
     */
    private Piece checkForPromotion(Piece piece, String to) {
        if (piece.getName().equals("pawn")) {
            int targetRow = Character.getNumericValue(to.charAt(1));

            // Check if pawn reached the opposite end of the board
            if ((piece.getColor().equals("W") && targetRow == 8)
                    || (piece.getColor().equals("B") && targetRow == 1)) {
                if (view != null) {
                    // Let user choose promotion piece
                    return view.showPromotionDialog(piece.getColor());
                } else {
                    // Default to Queen if no view available
                    return new Queen(piece.getColor().equals("W") ? "WQ" : "BQ", piece.getColor());
                }
            }
        }
        return piece; // No promotion needed
    }

    /**
     * Records a regular move in the move history
     */
    private void recordMove(String from, String to, Piece piece, Piece capturedPiece) {
        Move move = new Move(from, to, piece, capturedPiece);
        moveHistory.add(move);
        printMoveHistory(); // Debug output
    }

    /**
     * Records a castling move in the move history
     */
    private void recordCastlingMove(String from, String to, Piece piece, String rookFrom, String rookTo) {
        Move move = new Move(from, to, piece, rookFrom, rookTo);
        moveHistory.add(move);
        printMoveHistory(); // Debug output
    }

    /**
     * Returns the most recent move made, or null if no moves have been made
     */
    public static Move getLastMove() {
        if (moveHistory.isEmpty()) {
            return null;
        }
        return moveHistory.get(moveHistory.size() - 1);
    }

    /**
     * Checks the game state after a move and handles game ending conditions
     */
    private void checkGameStateAfterMove() {
        String opponentColor = currentTurn.equals("W") ? "B" : "W";

        // Check for checkmate
        if (isCheckmate(opponentColor)) {
            if (view != null) {
                view.showMessage("Checkmate! "
                        + (currentTurn.equals("W") ? "White" : "Black") + " wins!",
                        "Game Over", JOptionPane.INFORMATION_MESSAGE);
            }
            resetGame();
            return;
        }

        // Check for stalemate
        if (isStalemate(opponentColor)) {
            if (view != null) {
                view.showMessage("Stalemate, " + "draw!",
                        "Game Over", JOptionPane.INFORMATION_MESSAGE);
            }
            resetGame();
            return;
        }

        // Check if opponent king is in check (but not checkmate)
        if (board.isKingInCheck(opponentColor)) {
            if (view != null) {
                view.showMessage((opponentColor.equals("W") ? "White" : "Black") + " is in check!",
                        "Check", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    /**
     * Determines if the specified color is in checkmate (king is in check and
     * has no legal moves)
     */
    private boolean isCheckmate(String color) {
        // Must be in check to be checkmate
        if (!board.isKingInCheck(color)) {
            return false;
        }
        // No legal moves available
        return !board.hasLegalMoves(color);
    }

    /**
     * Determines if the specified color is in stalemate (king is not in check
     * but has no legal moves)
     */
    private boolean isStalemate(String color) {
        // Must not be in check to be stalemate
        if (board.isKingInCheck(color)) {
            return false;
        }
        // No legal moves available
        return !board.hasLegalMoves(color);
    }

    /**
     * Switches the current turn to the other player and updates the view
     */
    public void switchTurns() {
        currentTurn = currentTurn.equals("W") ? "B" : "W";
        if (view != null) {
            view.updateTurn(currentTurn);
        }
    }

    /**
     * Resets the game to initial state with pieces in starting positions
     */
    public void resetGame() {
        board = new ChessBoard();
        board.initializeBoard();
        moveHistory.clear();
        currentTurn = "W";
        if (view != null) {
            view.updateBoard();
            view.updateTurn(currentTurn);
        }
    }

    /**
     * Prints the complete move history to console for debugging
     */
    private void printMoveHistory() {
        System.out.println("\nMove History:");
        for (int i = 0; i < moveHistory.size(); i++) {
            System.out.println((i + 1) + ". " + moveHistory.get(i));
        }
        System.out.println();
    }

    /**
     * Returns the chess board instance
     */
    public ChessBoard getBoard() {
        return board;
    }

    /**
     * Gets all valid moves for a piece at the specified position
     */
    public List<String> getValidMoves(String position) {
        return board.getValidMovesForPiece(position, true);
    }

    /**
     * Interface that defines methods the view must implement to receive updates
     * from the game controller
     */
    public interface GameView {

        void updateBoard();                                           // Update piece positions

        void updateTurn(String turn);                                // Update turn indicator

        void showMessage(String message, String title, int messageType); // Show dialog messages

        Piece showPromotionDialog(String color);                     // Handle pawn promotion
    }
}
