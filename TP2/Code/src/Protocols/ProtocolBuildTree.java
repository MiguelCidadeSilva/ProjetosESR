package Protocols;

import Protocols.Helper.HelperConnection;
import Protocols.Helper.HelperContentReader;
import Protocols.Helper.HelperContentWriter;
import Protocols.Helper.HelperProtocols;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;



public class ProtocolBuildTree {
    public static final byte loop = 0;
    public static final byte found = 1;
    public static final byte nofound = 2;
    public static void encapsulateAsk(InetSocketAddress ipO, String name, DataOutputStream dos) throws IOException {
        int capacity = HelperContentWriter.calculateCapacity(0, List.of(name), Collections.singleton(new byte[0]),ipO);
        HelperContentWriter hcp = new HelperContentWriter(capacity);
        hcp.writeIp(ipO);
        hcp.writeStr(name);
        HelperProtocols.writeContentTCP(hcp,dos);
        dos.flush();
    }
    public static HelperConnection decapsulateAsk(DataInputStream dos) {
        HelperContentReader hcr = HelperProtocols.readContentTCP(dos);
        InetSocketAddress ipO = hcr.readIp();
        String name = hcr.readStr();
        return new HelperConnection(name,ipO);
    }
    private static void encapsulateAnswer(byte codigo, DataOutputStream dos) throws IOException {
        HelperContentWriter hcp = new HelperContentWriter(1);
        hcp.writeByte(codigo);
        HelperProtocols.writeContentTCP(hcp,dos);
        dos.flush();

    }
    public static void encapsulateAnswerFound(DataOutputStream dos) throws IOException {
        encapsulateAnswer(found,dos);
    }

    public static void encapsulateAnswerLoop(DataOutputStream dos) throws IOException {
        encapsulateAnswer(loop, dos);
    }
    public static void encapsulateAnswerNoFound(DataOutputStream dos) throws IOException {
        encapsulateAnswer(nofound, dos);
    }


    public static byte decapsulateAnswer(DataInputStream dos) {
        HelperContentReader hcr = HelperProtocols.readContentTCP(dos);
        return hcr.readByte();
    }
}
