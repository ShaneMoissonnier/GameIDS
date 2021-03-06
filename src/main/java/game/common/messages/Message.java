package game.common.messages;

import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;

/**
 * This class represents a message sent by entities connected to the RabbitMQ Server.
 */
public abstract class Message implements Serializable {
    public byte[] toBytes() {
        return SerializationUtils.serialize(this);
    }

    public static Message fromBytes(byte[] bytes) {
        return SerializationUtils.deserialize(bytes);
    }
}
