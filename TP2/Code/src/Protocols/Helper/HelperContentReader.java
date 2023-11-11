package Protocols.Helper;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class HelperContentReader {
    ByteBuffer byteBuffer;
    public HelperContentReader(byte[] data){
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
    public byte readByte() {
        return byteBuffer.get();
    }
    public String readStr() {
        return new String(readBytes());
    }
    public byte[] getBytesInfo(){
        return byteBuffer.array();
    }

    public InetAddress readIp() {
        String ip = readStr();
        InetAddress res = null;
        try {res = InetAddress.getByName(ip);} catch (UnknownHostException ignored) {}
        return res;
    }

    public boolean hasContent() {
        return byteBuffer.hasRemaining();
    }
    public int numberBytes() {return byteBuffer.remaining();}
}