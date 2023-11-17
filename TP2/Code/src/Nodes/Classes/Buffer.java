package Nodes.Classes;

import Nodes.Utils.Cods;
import Nodes.Utils.VideoExtractor;

import java.util.*;

public class Buffer {
    private final Map<Integer, Set<Integer>> receivedAudio;
    private final Map<Integer, Set<Integer>> receivedVideo;
    private final Map<Integer, Integer> expectedAudio;
    private final Map<Integer, Integer> expectedVideo;
    private final Map<Integer, List<byte[]>> bufferVideo;
    private final Map<Integer, List<byte[]>> bufferAudio;
    private final Player p;

    public Buffer(Player p) {
        this.p = p;
        bufferAudio = new HashMap<>();
        bufferVideo = new HashMap<>();
        receivedAudio = new HashMap<>();
        receivedVideo = new HashMap<>();
        expectedAudio = new HashMap<>();
        expectedVideo = new HashMap<>();
    }

    public void receive(StreamingPacket sp) {
        if (sp.getType() == Cods.codAudio) {
            handlePacket(sp, bufferAudio, receivedAudio, expectedAudio, false);
        } else if (sp.getType() == Cods.codVideo) {
            handlePacket(sp, bufferVideo, receivedVideo, expectedVideo, true);
        }
    }

    private void handlePacket(StreamingPacket sp, Map<Integer, List<byte[]>> buffer, Map<Integer, Set<Integer>> receivedPackets, Map<Integer, Integer> expected, boolean control) {
        int sequenceNumber = sp.getSequenceNumber();

        if (!buffer.containsKey(sequenceNumber)) {
            buffer.put(sequenceNumber, new ArrayList<>());
            receivedPackets.put(sequenceNumber, new HashSet<>());
            expected.put(sequenceNumber, sp.getNrPackets());
        }

        buffer.get(sequenceNumber).add(sp.getContent());
        receivedPackets.get(sequenceNumber).add(sp.getNrPackets());

        int lastExpected = expected.get(sequenceNumber);

        if (lastExpected == sp.getNrPackets()) {
            do {
          	lastExpected--;
	    } while (lastExpected > 0 && receivedPackets.get(sequenceNumber).contains(lastExpected));
            expected.put(sequenceNumber, lastExpected);

            if (lastExpected == 0) {
                List<byte[]> bytes = buffer.get(sequenceNumber);
                buffer.remove(sequenceNumber);
                byte[] content = VideoExtractor.concatenateChunks(bytes);

                if (control) {
                    p.updateFrame(content);
                } else {
                    p.updateAudio(content);
                }
            }
        }
    }
}
