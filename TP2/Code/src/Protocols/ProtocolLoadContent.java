package Protocols;

import Protocols.Helper.HelperContentReader;
import Protocols.Helper.HelperContentWriter;
import Protocols.Helper.HelperProtocols;
import Protocols.Utils.Manager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ProtocolLoadContent{
    private static final byte request = 0;
    private static final byte connection = 1;
    private static void makeFlush(DataOutputStream dos, boolean flush) throws IOException {
        if(flush) {
            dos.flush();
        }
    }
    public static void encapsulateRequest(String resource, DataOutputStream dos, boolean flush, String destiny) throws IOException {
        int nrbytes = HelperContentWriter.calculateCapacity(0, List.of(resource),null,null);
        HelperContentWriter hcw = new HelperContentWriter(nrbytes+1);
        hcw.writeByte(request);
        hcw.writeStr(resource);
        HelperProtocols.writeContentTCP(hcw,dos);
        makeFlush(dos,flush);
        Manager.showSendPacket("Pedido de recurso", nrbytes,destiny);
    }

    public static void encapsulateContent(byte[] file, DataOutputStream dos, boolean flush, String destiny) throws IOException {
        int nrbytes = HelperContentWriter.calculateCapacity(0,null, Collections.singleton(file),null);
        HelperContentWriter hcw = new HelperContentWriter(nrbytes);
        hcw.writeBytes(file);
        HelperProtocols.writeContentTCP(hcw,dos);
        makeFlush(dos,flush);
        Manager.showSendPacket("Conteudo do ficheiro", nrbytes,destiny);
    }

    public static byte[] decapsulateContent(DataInputStream dis) {
        HelperContentReader hcr = HelperProtocols.readContentTCP(dis);
        Manager.sucessMessage("Ler conteudo de um recurso");
        return hcr.readBytes();
    }


    public static void encapsulateConnection(DataOutputStream dos, boolean flush, String destiny) throws IOException {
        HelperContentWriter hcw = new HelperContentWriter(1);
        hcw.writeByte(connection);
        HelperProtocols.writeContentTCP(hcw,dos);
        makeFlush(dos,flush);
        Manager.showSendPacket("Resposta conexão", 1,destiny);
    }

    public static boolean decapsulateConnection(DataInputStream dis) {
        HelperContentReader hcr = HelperProtocols.readContentTCP(dis);
        Manager.sucessMessage("Ler conteudo de uma resposta de conexão");
        return hcr.readByte() == connection;
    }

    public static boolean isRequest(DataInputStream dis, String [] aux) {
        HelperContentReader hcr = HelperProtocols.readContentTCP(dis);
        boolean res = hcr.readByte() == request;
        if(res)
            aux[0] = hcr.readStr();
        Manager.sucessMessage("Verificar se é um pedido de request ou conexão");
        return res;
    }
}
