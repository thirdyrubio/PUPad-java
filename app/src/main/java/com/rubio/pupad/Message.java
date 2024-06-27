package com.rubio.pupad;

public class Message {
    // Constants for message sender identification
    public static String SENT_BY_ME = "me";
    public static String SENT_BY_BOT = "bot";

    // Instance variables
    private String message; // The content of the message
    private String sentBy; // Who sent the message ("me" or "bot")

    // Constructor to initialize Message objects
    public Message(String message, String sentBy) {
        this.message = message;
        this.sentBy = sentBy;
    }

    // Getter for retrieving the message content
    public String getMessage() {
        return message;
    }

    // Setter for setting the message content
    public void setMessage(String message) {
        this.message = message;
    }

    // Getter for retrieving the sender
    public String getSentBy() {
        return sentBy;
    }

    // Setter for setting the sender
    public void setSentBy(String sentBy) {
        this.sentBy = sentBy;
    }
}
