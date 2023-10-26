package Protocols;

import Protocols.Helper.HelperConnection;
import Protocols.Helper.HelperContentReader;
import Protocols.Helper.HelperContentWriter;
import Protocols.Helper.HelperProtocols;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;



public class ProtocolBuildTree {
    public static final byte loop = 0;
    public static final byte found = 1;
    public static DatagramPacket encapsulateAsk(InetSocketAddress ipO, String name) {
        int capacity = HelperContentWriter.calculateCapacity(0, List.of(name), Collections.singleton(new byte[0]),ipO);
        HelperContentWriter hcp = new HelperContentWriter(capacity);
        hcp.writeIp(ipO);
        hcp.writeStr(name);
        return HelperProtocols.writeContentUDP(hcp);

    }
    public static HelperConnection decapsulateAsk(DatagramPacket dp) {
        HelperContentReader hcr = HelperProtocols.readContentUDP(dp);
        InetSocketAddress ipO = hcr.readIp();
        String name = hcr.readStr();

        return new HelperConnection(name,ipO);
    }
    private static DatagramPacket encapsulateAnswer(byte codigo) {
        HelperContentWriter hcp = new HelperContentWriter(1);
        hcp.writeByte(codigo);
        return HelperProtocols.writeContentUDP(hcp);

    }
    public static DatagramPacket encapsulateAnswerFound() {
        return encapsulateAnswer(found);
    }

    public static DatagramPacket encapsulateAnswerLoop() {
        return encapsulateAnswer(loop);
    }


    public static boolean decapsulateAnswer(DatagramPacket dp) {
        HelperContentReader hcr = HelperProtocols.readContentUDP(dp);
        return hcr.readByte() == found;
    }
}
