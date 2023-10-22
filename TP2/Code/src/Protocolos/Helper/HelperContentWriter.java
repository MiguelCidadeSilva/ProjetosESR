package Protocolos.Helper;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
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

    public void writeStr(String str){
        writeBytes(str.getBytes());
    }
    public byte[] getBytesInfo(){
        return byteBuffer.array();
    }

    public static int calculateCapacity(int numbers_integer, Collection<String> strings, Collection<byte[]> bytes){
        List<byte[]> aux = Stream.concat(strings.stream().map(String::getBytes), bytes.stream()).toList();
        return 4*numbers_integer + 4*aux.size() + aux.stream().map(a -> a.length).reduce(0, Integer::sum);
    }
}