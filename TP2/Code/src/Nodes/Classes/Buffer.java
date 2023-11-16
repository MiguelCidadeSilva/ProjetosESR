package Nodes.Classes;

import Nodes.Utils.Cods;
import Nodes.Utils.VideoExtractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Buffer {
    private final Map<String, List<byte[]>> bufferVideo;
    private final Map<String, List<byte[]>> bufferAudio;
    private final Player p;
    public Buffer(Player p) {
        this.p = p;
        bufferAudio = new HashMap<>();
        bufferVideo = new HashMap<>();
    }

    public void receive(StreamingPacket sp) {
        if(sp.getType() == Cods.codAudio) {
            if(!bufferAudio.containsKey(sp.getResource()))
            {
                bufferAudio.put(sp.getResource(),new ArrayList<>());
            }
            bufferAudio.get(sp.getResource()).add(sp.getContent());
            if(sp.getNrPackets() == 1)
            {
                List<byte[]> bytes = bufferAudio.get(sp.getResource());
                bufferAudio.remove(sp.getResource());
                byte[] content = VideoExtractor.concatenateChunks(bytes);
                p.updateAudio(content);
            }
        }
        else if(sp.getType() == Cods.codVideo) {
            if(!bufferVideo.containsKey(sp.getResource()))
            {
                bufferVideo.put(sp.getResource(),new ArrayList<>());
            }
            bufferVideo.get(sp.getResource()).add(sp.getContent());
            if(sp.getNrPackets() == 1)
            {
                List<byte[]> bytes = bufferVideo.get(sp.getResource());
                bufferVideo.remove(sp.getResource());
                byte[] content = VideoExtractor.concatenateChunks(bytes);
                p.updateFrame(content);
            }
        }
    }
}
