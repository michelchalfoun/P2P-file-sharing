package messages.payload;

import java.io.*;

public class HavePayload implements Payload {

    private int pieceID;

    public HavePayload(final int pieceID) {
        this.pieceID = pieceID;
    }

    public HavePayload(final byte[] payloadInBytes) {
        try {
            this.pieceID = bytesToInt(payloadInBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] getBytes() {
        try {
            return intToBytes(pieceID);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // TODO
    // Build tests for this
    private byte[] intToBytes(int my_int) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);
        out.writeInt(my_int);
        out.close();
        byte[] int_bytes = bos.toByteArray();
        bos.close();
        return int_bytes;
    }

    public int bytesToInt(byte[] int_bytes) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(int_bytes);
        ObjectInputStream ois = new ObjectInputStream(bis);
        int my_int = ois.readInt();
        ois.close();
        return my_int;
    }

    public int getPieceID() {
        return pieceID;
    }
}
