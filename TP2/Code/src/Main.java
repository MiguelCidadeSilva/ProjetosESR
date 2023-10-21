import Protocolos.Helper.HelperContentReader;
import Protocolos.Helper.HelperContentWriter;
import Protocolos.Helper.HelperProtocols;

import java.io.*;
import java.net.DatagramPacket;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Main {
    public static HelperContentWriter write() {
        HelperContentWriter hcw = new HelperContentWriter(HelperContentWriter.calculateCapacity(2,Arrays.asList("Braga","SCB","ESR"), Arrays.asList(new byte[]{0, 1, 2, 3, 4})));
        hcw.writeInt(1921);
        hcw.writeStr("Braga");
        hcw.writeStr("SCB");
        hcw.writeInt(0);
        hcw.writeBytes(new byte[]{0,1,2,3,4});
        hcw.writeStr("ESR");
        return hcw;
    }
    public static void print(HelperContentReader hcr) {
        System.out.println(hcr.readInt());
        System.out.println(hcr.readStr());
        System.out.println(hcr.readStr());
        System.out.println(hcr.readInt());
        byte[] bytes = hcr.readBytes();
        System.out.println(bytes.length);
        for(byte b : bytes)
            System.out.print(b + " ");
        System.out.println();
        System.out.println(hcr.readStr());
    }
    public static void testUDP(HelperContentWriter hcw){
        DatagramPacket dp = HelperProtocols.writeContentUDP(hcw);
        HelperContentReader hcr = HelperProtocols.readContentUDP(dp);
        System.out.println("UDP");
        print(hcr);
    }
    public static void testTCP(HelperContentWriter hcw) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream("example.dat");
        DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);
        HelperProtocols.writeContentTCP(hcw,dataOutputStream);
        dataOutputStream.close();
        fileOutputStream.close();
        FileInputStream fileInputStream = new FileInputStream("example.dat");
        DataInputStream dataInputStream = new DataInputStream(fileInputStream);
        HelperContentReader hcr = HelperProtocols.readContentTCP(dataInputStream);
        dataInputStream.close();
        fileInputStream.close();
        System.out.println("TCP");
        print(hcr);
    }
    public static void main(String[] args) throws IOException {
        HelperContentWriter hcw = write();
        testUDP(hcw);
        testTCP(hcw);
    }
}