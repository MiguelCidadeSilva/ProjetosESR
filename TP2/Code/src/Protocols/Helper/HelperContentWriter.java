package Protocols.Helper;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class HelperContentWriter {
    ByteBuffer byteBuffer;
    public HelperContentWriter(int capacity){
        byteBuffer = ByteBuffer.allocate(capacity);
    }
    public void writeInt(int number){
        // realocate(4);
        byteBuffer.putInt(number);
    }
    public void writeBytes(byte[] bytes){
        // realocate(4+bytes.length);
        byteBuffer.putInt(bytes.length);
        byteBuffer.put(bytes);
    }
    public void writeByte(byte b) {
        byteBuffer.put(b);
    }
    public void writeStr(String str){
        writeBytes(str.getBytes());
    }
    public byte[] getBytesInfo(){
        return byteBuffer.array();
    }
    public void writeIp(InetSocketAddress ip) {
        writeStr(ip.getHostName());
        writeInt(ip.getPort());
    }

    public static int calculateCapacity(int numbers_integer, Collection<String> strings, Collection<byte[]> bytes, InetSocketAddress ip){
        List<byte[]> aux = Stream.concat(strings.stream().map(String::getBytes), bytes.stream()).toList();
        int auxint = ip != null ? 4 + ip.getHostName().length() + 4 : 0;
        return 4*numbers_integer + 4*aux.size() + aux.stream().map(a -> a.length).reduce(0, Integer::sum) + auxint;
    }
}