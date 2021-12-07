package messages;

/** Establishes all of the message types with its appropiate ids */
public enum MessageType
{
    CHOKE(0),
    UNCHOKE(1),
    INTERESTED(2),
    NOT_INTERESTED(3),
    HAVE(4),
    BITFIELD(5),
    REQUEST(6),
    PIECE(7);

    private static final MessageType[] typesById = values();

    private final int value;

    MessageType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static MessageType getMessageType(final int id) {
        return typesById[id];
    }
}
