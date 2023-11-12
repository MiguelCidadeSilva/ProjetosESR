package Nodes.Classes;

import Nodes.Utils.Cods;

import java.util.Arrays;

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
    public String getTask() {
         return this.type == Cods.codAudio ? "Audio" : this.type == Cods.codVideo ? "Video" : "End stream";
    }
    @Override
    public String toString() {
        return "(" + this.resource + ","  + this.getTask() + "," + this.getContent().length + " bytes)";
    }
}
