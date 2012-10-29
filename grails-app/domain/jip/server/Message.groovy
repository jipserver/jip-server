package jip.server;

/**
 * Job messages that carry a type and optional progress information
 */
class Message implements Comparable<Message>{
    /**
     * Available message types
     */
    enum Type {
        /**
         * A simple information message
         */
        INFO,
        /**
         * A warnings message
         */
        WARNING,
        /**
         * An error message
         */
        ERROR
    }
    /**
     * Message type
     */
    Type type = Type.INFO;

    /**
     * The message
     */
    String message;

    /**
     * The create date of the message
     */
    Date createDate = new Date();


    @Override
    public int compareTo(Message t) {
        return this.createDate.compareTo(t.createDate);
    }

    @Override
    public String toString() {
        return "${type}: ${message}"
    }
}
