package com.mycompany.chess;

import java.util.Map;

/**
 * King piece class implementing king movement and castling rules. Kings can
 * move one square in any direction and can castle under specific conditions.
 */
public class King extends Piece {

    // Track whether this king has moved (affects castling eligibility)
    private boolean hasMoved = false;

    /**
     * Constructor for King piece.
     *
     * @param id Unique identifier for this piece
     * @param color Color of the piece ("W" for white, "B" for black)
     */
    public King(String id, String color) {
        // Call parent constructor with piece type, id, color, and Unicode symbol
        super("king", id, color, color.equals("W") ? "♔" : "♚");
    }

    /**
     * Validates if a move is legal for a king. Kings can move one square in any
     * direction or castle.
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
        int colDiff = Math.abs(toCol - fromCol);
        int rowDiff = Math.abs(toRow - fromRow);

        // Normal king move: one square in any direction
        if (colDiff <= 1 && rowDiff <= 1) {
            Piece target = boardState.get(to);
            // Can move to empty square or square with opponent piece
            return target == null || !target.color.equals(color);
        }

        // Castling move: king moves two squares horizontally
        if (rowDiff == 0 && colDiff == 2 && !hasMoved) {
            // Determine which rank the king should be on for castling
            int kingRow = color.equals("W") ? 1 : 8;
            // Verify king is on its starting rank
            if (fromRow != 8 - kingRow) {
                return false;
            }

            // Determine castling direction (1 for kingside, -1 for queenside)
            int direction = toCol > fromCol ? 1 : -1;
            // Find the rook position for this castling direction
            String rookPos = (direction == 1) ? "h" + kingRow : "a" + kingRow;
            Piece rook = boardState.get(rookPos);

            // Verify rook exists, is a rook, and hasn't moved
            if (rook == null || !rook.getName().equals("rook") || ((Rook) rook).hasMoved()) {
                return false;
            }

            // Check that all squares between king and rook are empty
            for (int col = fromCol + direction; col != (direction == 1 ? 7 : 0); col += direction) {
                String pos = "" + (char) ('a' + col) + kingRow;
                if (boardState.containsKey(pos)) {
                    return false;  // Path is blocked
                }
            }

            // Verify king is not currently in check
            String kingPos = from;
            String middleSquare = "" + (char) ('a' + (fromCol + direction)) + kingRow;
            Piece king = boardState.get(kingPos);

            // Temporarily move king to middle square and check for check
            boardState.remove(kingPos);
            boardState.put(middleSquare, king);
            boolean inCheck = isKingInCheckOnBoard(color, boardState);
            // Restore king to original position
            boardState.remove(middleSquare);
            boardState.put(kingPos, king);

            if (inCheck) {
                return false;  // Cannot castle through check
            }

            // Temporarily move king to final position and check for check
            boardState.remove(kingPos);
            boardState.put(to, king);
            inCheck = isKingInCheckOnBoard(color, boardState);
            // Restore king to original position
            boardState.remove(to);
            boardState.put(kingPos, king);

            return !inCheck;  // Cannot castle into check
        }

        return false;  // Invalid move
    }

    /**
     * Sets whether this king has moved (affects castling eligibility).
     *
     * @param moved true if king has moved, false otherwise
     */
    public void setHasMoved(boolean moved) {
        this.hasMoved = moved;
    }

    /**
     * Gets whether this king has moved.
     *
     * @return true if king has moved, false otherwise
     */
    public boolean hasMoved() {
        return hasMoved;
    }

    /**
     * Checks if a king is in check on a given board state. This is a helper
     * method used during castling validation.
     *
     * @param kingColor Color of the king to check
     * @param boardState Board state to check
     * @return true if king is in check, false otherwise
     */
    private boolean isKingInCheckOnBoard(String kingColor, Map<String, Piece> boardState) {
        // Find the king's position
        String kingPos = null;
        for (Map.Entry<String, Piece> entry : boardState.entrySet()) {
            Piece piece = entry.getValue();
            if (piece.getName().equals("king") && piece.getColor().equals(kingColor)) {
                kingPos = entry.getKey();
                break;
            }
        }

        if (kingPos == null) {
            return false;  // King not found
        }

        String opponentColor = kingColor.equals("W") ? "B" : "W";

        // Check if any opponent piece can attack the king
        for (Map.Entry<String, Piece> entry : boardState.entrySet()) {
            Piece piece = entry.getValue();
            if (piece.getColor().equals(opponentColor)) {
                // Special handling for pawns (they attack diagonally, not forward)
                if (piece.getName().equals("pawn")) {
                    String fromPos = entry.getKey();
                    int fromCol = fromPos.charAt(0) - 'a';
                    int fromRow = 8 - Character.getNumericValue(fromPos.charAt(1));
                    int kingCol = kingPos.charAt(0) - 'a';
                    int kingRow = 8 - Character.getNumericValue(kingPos.charAt(1));

                    // Pawn attack direction depends on color
                    int direction = piece.getColor().equals("W") ? -1 : 1;
                    // Check if pawn can attack king diagonally
                    if (fromRow + direction == kingRow && Math.abs(fromCol - kingCol) == 1) {
                        return true;
                    }
                } // For all other pieces, use their normal move validation
                else if (piece.isValidMove(entry.getKey(), kingPos, boardState)) {
                    return true;
                }
            }
        }

        return false;  // King is not in check
    }
}
