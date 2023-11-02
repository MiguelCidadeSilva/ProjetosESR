package Protocols;

import Nodes.Classes.StreamingPacket;
import Protocols.Helper.HelperContentReader;
import Protocols.Helper.HelperContentWriter;
import Protocols.Helper.HelperProtocols;

import java.net.DatagramPacket;
import java.util.List;

public class ProtocolTransferContent {

    public static StreamingPacket decapsulate(DatagramPacket packet) {
        HelperContentReader hcr = HelperProtocols.readContentUDP(packet);
        String resource = hcr.readStr();
        int type = hcr.readInt();
        byte[] content = hcr.readBytes();
        return new StreamingPacket(resource,type,content);
    }

    public static DatagramPacket encapsulate(StreamingPacket packet){
        HelperContentWriter hcw = new HelperContentWriter(HelperContentWriter.calculateCapacity(1, List.of(packet.getResource()),List.of(packet.getContent()),null));
        hcw.writeStr(packet.getResource());
        hcw.writeInt(packet.getType());
        hcw.writeBytes(packet.getContent());
        return HelperProtocols.writeContentUDP(hcw);
    }
}
