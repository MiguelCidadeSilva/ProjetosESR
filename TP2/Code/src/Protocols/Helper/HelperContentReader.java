package Protocols.Helper;

import java.nio.ByteBuffer;

public class HelperContentReader {
    ByteBuffer byteBuffer;
    protected HelperContentReader(byte [] data){
        byteBuffer = ByteBuffer.wrap(data);
    }
    public int readInt() {
       return byteBuffer.getInt();
    }
    public byte[] readBytes() {
        int len = byteBuffer.getInt();
        byte[] array = new byte[len];
        byteBuffer.get(array);
        return array;
    }
    public String readStr() {
        return new String(readBytes());
    }
    public byte[] getBytesInfo(){
        return byteBuffer.array();
    }

    public boolean hasContent() {
        return byteBuffer.hasRemaining();
    }
}