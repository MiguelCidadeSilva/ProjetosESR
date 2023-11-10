package Protocols.Utils;

import Protocols.Helper.HelperContentReader;
import Protocols.Helper.HelperContentWriter;

import java.util.ArrayList;
import java.util.List;

public class FramesAux {

    public static byte[] encapsulateFrames(List<byte[]> frames) {
        HelperContentWriter hcw = new HelperContentWriter(HelperContentWriter.calculateCapacity(0,null,frames,null));
        for(byte [] bytes : frames)
            hcw.writeBytes(bytes);
        return hcw.getBytesInfo();
    }
    public static List<byte[]> decapsulateFrames(byte[] frames) {
        HelperContentReader hcr = new HelperContentReader(frames);
        List<byte[]> res = new ArrayList<>();
        while(hcr.hasContent())
            res.add(hcr.readBytes());
        return res;
    }
}
