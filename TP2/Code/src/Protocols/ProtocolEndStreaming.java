package Protocols;

import Protocols.Helper.HelperConnection;
import Protocols.Helper.HelperContentReader;
import Protocols.Helper.HelperContentWriter;
import Protocols.Helper.HelperProtocols;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class ProtocolEndStreaming {
    public static HelperConnection decapsulate(DataInputStream dis) {
        HelperContentReader hcr = HelperProtocols.readContentTCP(dis);
        return new HelperConnection(hcr.readStr(),hcr.readIp());
    }
    public static void encapsulate(HelperConnection hc, DataOutputStream dos) throws IOException {
        HelperContentWriter hcw = new HelperContentWriter(HelperContentWriter.calculateCapacity(0, List.of(hc.name()),null,hc.address()));
        hcw.writeStr(hc.name());
        hcw.writeIp(hc.address());
        HelperProtocols.writeContentTCP(hcw,dos);
        dos.flush();
    }
}
