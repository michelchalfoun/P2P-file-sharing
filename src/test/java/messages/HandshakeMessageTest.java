package messages;

import org.junit.Test;

import java.io.*;

public class HandshakeMessageTest {

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        HandshakeMessage msg = new HandshakeMessage(1);

        byte[] serializedData = getByteArray(msg);
        HandshakeMessage deserializedEmp = (HandshakeMessage) getObject(serializedData);

        System.out.println("Deserialized Employee : " + deserializedEmp);
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
