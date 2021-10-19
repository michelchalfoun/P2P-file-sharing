package messages;

import org.junit.Test;

import java.io.*;

import static junit.framework.TestCase.assertEquals;

public class HandshakeMessageTest
{
    @Test
    public void testDeserialization() throws IOException, ClassNotFoundException {
        HandshakeMessage expectedHandshakeMessage = new HandshakeMessage(1);

        byte[] serializedData = getByteArray(expectedHandshakeMessage);
        final HandshakeMessage deserializedMessage = (HandshakeMessage) getObject(serializedData);

        assertEquals(expectedHandshakeMessage, deserializedMessage);
    }

    private static byte[] getByteArray(Object obj) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream os = new ObjectOutputStream(bos)) {
            os.writeObject(obj);
        }
        return bos.toByteArray();
    }

    private static Object getObject(byte[] byteArr) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(byteArr);
        ObjectInput in = new ObjectInputStream(bis);
        return in.readObject();
    }
}
