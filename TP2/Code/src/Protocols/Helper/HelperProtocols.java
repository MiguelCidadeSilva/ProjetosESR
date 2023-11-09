package Protocols.Helper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

// This classes helps to uniformize the writing and reading packets.
// It can work with TCP and UDP
public class HelperProtocols {
    public static DatagramPacket writeContentUDP(HelperContentWriter content) {
        byte[] data = content.getBytesInfo();
        return new DatagramPacket(data,data.length);
    }
    public static void writeContentTCP(HelperContentWriter content, DataOutputStream dos) {
        try {
            byte [] bytes = content.getBytesInfo();
            dos.writeInt(bytes.length);
            dos.write(bytes);
        } catch (IOException e) {
            System.out.println("Erro a escrever o pacote TCP");
            throw new RuntimeException(e);
        }
    }
    public static HelperContentReader readContentUDP(DatagramPacket packet) {
        return new HelperContentReader(packet.getData());
    }

    public static HelperContentReader readContentTCP(DataInputStream dis) {
        HelperContentReader res = null;
        try {
            int number = dis.readInt();
            res = new HelperContentReader(dis.readNBytes(number));
        } catch (IOException e) {
            System.out.println("Erro a ler o pacote TCP");
            throw  new RuntimeException(e);
        }
        return res;
    }
}
