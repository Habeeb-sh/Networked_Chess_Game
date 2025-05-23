package com.mycompany.chess;

/**
 * Message class for handling different types of game communication.
 * Defines message types and provides utility for message generation.
 */
public class Message {

    /**
     * Enumeration of all possible message types in the chess game.
     */
    public enum Type {
        NONE, // No message
        JOIN_GAME, // Player joining a game
        GAME_START, // Game starting
        MOVE, // Player making a move
        GAME_OVER, // Game has ended
        WAITING, // Waiting for opponent
        ERROR, // Error occurred
        DISCONNECT, // Player disconnected
        SERVER_SHUTDOWN, // Server shutting down
        OPPONENT_DISCONNECTED   // Opponent has disconnected
    }

    /**
     * Generates a formatted message string.
     *
     * @param type Type of message
     * @param data Additional data for the message
     * @return Formatted message string with type and data separated by '#'
     */
    public static String GenerateMsg(Message.Type type, String data) {
        return type + "#" + data;
    }
}
