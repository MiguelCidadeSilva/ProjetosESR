package Protocolos;

import Protocolos.Helper.HelperContentReader;
import Protocolos.Helper.HelperContentWriter;
import Protocolos.Helper.HelperProtocols;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.Map;

public class ProtocolLoadContent{

    public static void encapsulate(Map<String,byte[]> files, DataOutputStream dos) {
        int nrbytes = HelperContentWriter.calculateCapacity(0,files.keySet(),files.values());
        HelperContentWriter hcw = new HelperContentWriter(nrbytes);
        files.forEach((key, value) -> {
            hcw.writeStr(key);
            hcw.writeBytes(value);
        });
        HelperProtocols.writeContentTCP(hcw,dos);
    }

    public static Map<String,byte[]> desencapsulate(DataInputStream dis) {
        HelperContentReader hcr = HelperProtocols.readContentTCP(dis);
        Map<String,byte[]> res = new HashMap<>();
        while(hcr.hasContent()) {
            String str = hcr.readStr();
            byte[] content = hcr.readBytes();
            res.put(str,content);
        }
        return res;
    }
}
