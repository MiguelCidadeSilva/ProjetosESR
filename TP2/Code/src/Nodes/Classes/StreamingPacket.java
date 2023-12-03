package Nodes.Classes;

import Nodes.Utils.Cods;
import Protocols.Helper.HelperContentReader;
import Protocols.Helper.HelperContentWriter;

import java.util.List;

public class StreamingPacket {
    private final String resource;
    private final int type;
    private final byte[] content;
    private final int sequenceNumber;
    private final int nrPackets;

    public StreamingPacket(String resource, int type, byte[] content, int sequenceNumber, int nrPackets) {
        this.resource = resource;
        this.type = type;
        this.content = content;
        this.sequenceNumber = sequenceNumber;
        this.nrPackets = nrPackets;
    }
    public StreamingPacket(HelperContentReader hcr) {
        this.resource = hcr.readStr();
        this.type = hcr.readInt();
        this.sequenceNumber = hcr.readInt();
        this.nrPackets = hcr.readInt();
        this.content = hcr.readBytes();
    }
    public HelperContentWriter writer() {
        HelperContentWriter hcw = new HelperContentWriter(HelperContentWriter.calculateCapacity(3, List.of(this.getResource()),List.of(this.getContent()),null));
        hcw.writeStr(this.getResource());
        hcw.writeInt(this.getType());
        hcw.writeInt(this.getSequenceNumber());
        hcw.writeInt(this.getNrPackets());
        hcw.writeBytes(this.getContent());
        return hcw;
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
    public String getTask() {
         return this.type == Cods.codAudio ? "Audio" : this.type == Cods.codVideo ? "Video" : "End stream";
    }
    @Override
    public String toString() {
        return "(Rec: " + this.resource +
                ", Task: " + this.getTask() +
                ", NSeq: " + this.getSequenceNumber() +
                ", NP: " + this.getNrPackets() +
                ", T: " + this.getContent().length + " bytes)";
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public int getNrPackets() {
        return nrPackets;
    }
}
