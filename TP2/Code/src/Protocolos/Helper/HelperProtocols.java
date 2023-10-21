package Protocolos.Helper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;

// This classes helps to uniformize the writing and reading packets.
// It can work with TCP and UDP
public class HelperProtocols {
    public static DatagramPacket writeContentUDP(HelperContentWriter content) {
        byte[] data = content.getBytesInfo();
        return new DatagramPacket(data,data.length);
    }
    public static void writeContentTCP(HelperContentWriter content, DataOutputStream dos) {
        try {
            dos.write(content.getBytesInfo());
        } catch (IOException e) {
            System.out.println("Erro a escrever o pacote TCP");
        }
    }
    public static HelperContentReader readContentUDP(DatagramPacket packet) {
        return new HelperContentReader(packet.getData());
    }

    public static HelperContentReader readContentTCP(DataInputStream dis) {
        HelperContentReader res = null;
        try {
             res = new HelperContentReader(dis.readAllBytes());
        } catch (IOException e) {
            System.out.println("Erro a ler o pacote TCP");
        }
        return res;
    }
}
