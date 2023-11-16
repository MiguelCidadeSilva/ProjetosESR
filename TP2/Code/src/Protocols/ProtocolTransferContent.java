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
        return new StreamingPacket(hcr);
    }

    public static DatagramPacket encapsulate(StreamingPacket packet){
        return HelperProtocols.writeContentUDP(packet.writer());
    }
}
