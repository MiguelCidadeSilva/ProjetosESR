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
            dos.write(bytes.length);
            dos.write(bytes);
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
            /*

            List<byte[]> listBuffer = new ArrayList<>();
            byte[] buffer = new byte[1024];
            int bytesRead, bytestotal = 0;
            while ((bytesRead = dis.read(buffer)) != -1)
            {
                bytestotal += bytesRead;
                listBuffer.add(buffer);
            }
            byte[] total = new byte[bytestotal];
            int i = 0;
            for(byte[] bytes: listBuffer)
                for(byte b : bytes)
                {
                    total[i] = b;
                    i++;
                }
            res = new HelperContentReader(total);
             */
            int number = dis.read();
            res = new HelperContentReader(dis.readNBytes(number));
        } catch (IOException e) {
            System.out.println("Erro a ler o pacote TCP");
            throw  new RuntimeException(e);
        }
        return res;
    }
}
