package Nodes.Classes;

import Protocols.Helper.HelperContentReader;
import Protocols.Helper.HelperContentWriter;
import Protocols.Helper.HelperProtocols;

import java.net.DatagramPacket;
import java.util.List;

public class StreamingPacket {
    private String resource;
    private int type;
    private byte[] content;

    public StreamingPacket(String resource, int type, byte[] content) {
        this.resource = resource;
        this.type = type;
        this.content = content;
    }

    public String getResource() {
        return resource;
    }

    public int getType() {
        return type;
    }

    public byte[] getContent() {
        return content;
    }
}
