package Nodes.Classes;

import Nodes.Utils.Cods;
import Nodes.Utils.VideoExtractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Buffer {
    private final Map<String, Map<Integer, List<byte[]>>> bufferVideo;
    private final Map<String, Map<Integer, List<byte[]>>> bufferAudio;
    private final Player p;
    public Buffer(Player p) {
        this.p = p;
        bufferAudio = new HashMap<>();
        bufferVideo = new HashMap<>();
    }

    public void receive(StreamingPacket sp) {
        if(sp.getType() == Cods.codAudio) {
            if(!bufferAudio.containsKey(sp.getResource()))
                bufferAudio.put(sp.getResource(),new HashMap<>());
	    if(!bufferAudio.get(sp.getResource()).containsKey(sp.getSequenceNumber()))
	  	bufferAudio.get(sp.getResource()).put(sp.getSequenceNumber(), new ArrayList<>());
            bufferAudio.get(sp.getResource()).get(sp.getSequenceNumber()).add(sp.getContent());
            if(sp.getNrPackets() == 1)
            {
                List<byte[]> bytes = bufferAudio.get(sp.getResource()).get(sp.getSequenceNumber());
                bufferAudio.get(sp.getResource()).remove(sp.getSequenceNumber());
                byte[] content = VideoExtractor.concatenateChunks(bytes);
                p.updateAudio(content);
            }
        }
        else if(sp.getType() == Cods.codVideo) {
            if(!bufferVideo.containsKey(sp.getResource()))
                bufferVideo.put(sp.getResource(),new HashMap<>());
	    if(!bufferVideo.get(sp.getResource()).containsKey(sp.getSequenceNumber()))
                bufferVideo.get(sp.getResource()).put(sp.getSequenceNumber(), new ArrayList<>());
            bufferVideo.get(sp.getResource()).get(sp.getSequenceNumber()).add(sp.getContent());
            if(sp.getNrPackets() == 1)
            {
                List<byte[]> bytes = bufferVideo.get(sp.getResource()).get(sp.getSequenceNumber());
                bufferVideo.get(sp.getResource()).remove(sp.getSequenceNumber());
                byte[] content = VideoExtractor.concatenateChunks(bytes);
                p.updateFrame(content);
            }
        }
    }
}
