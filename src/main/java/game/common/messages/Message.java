package game.common.messages;

import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;

public abstract class Message implements Serializable {
    public final String senderId;

    public Message(String senderId) {
        this.senderId = senderId;
    }

    public byte[] toBytes() {
        return SerializationUtils.serialize(this);
    }

    public static Message fromBytes(byte[] bytes) {
        return SerializationUtils.deserialize(bytes);
    }
}
