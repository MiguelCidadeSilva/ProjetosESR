import Nodes.Utils.Compresser;
import Nodes.Utils.VideoExtractor;
import Protocols.Helper.HelperContentReader;
import Protocols.Helper.HelperContentWriter;
import Protocols.Helper.HelperProtocols;

import java.io.*;
import java.net.DatagramPacket;
import java.util.Arrays;

public class Main {
    public static HelperContentWriter write() {
        HelperContentWriter hcw = new HelperContentWriter(HelperContentWriter.calculateCapacity(2,Arrays.asList("Braga","SCB","ESR"), Arrays.asList(new byte[]{0, 1, 2, 3, 4}),null));
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
    public static void main(String[] args) throws Exception {
        // HelperContentWriter hcw = write();
        // testUDP(hcw);
        // testTCP(hcw);

        VideoExtractor ve = new VideoExtractor("video.mp4","Testfiles/DBFiles/videoA.mp4");
        System.out.println(ve.hasFrames());
        System.out.println(ve.hasAudio());
        System.out.println(ve.getFrameRate());
        System.out.println(ve.getVideo());
        byte[] frames = ve.nextFrames().get(0);
        byte[] compressed = Compresser.compress(frames);
        byte[] decompressed = Compresser.decompress(compressed);
        System.out.println(frames.length);
        System.out.println(compressed.length);
        System.out.println(decompressed.length);
    }
}