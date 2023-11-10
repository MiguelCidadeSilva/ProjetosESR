package Protocols;

import Protocols.Helper.HelperContentReader;
import Protocols.Helper.HelperContentWriter;
import Protocols.Helper.HelperProtocols;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class ProtocolStartStreaming {
    public static String decapsulate(DataInputStream dis) {
        HelperContentReader hcr = HelperProtocols.readContentTCP(dis);
        return hcr.readStr();
    }
    public static void encapsulate(String resource,DataOutputStream dos) throws IOException {
        HelperContentWriter hcw = new HelperContentWriter(HelperContentWriter.calculateCapacity(0, List.of(resource),null,null));
        hcw.writeStr(resource);
        HelperProtocols.writeContentTCP(hcw,dos);
        dos.flush();
    }
}
