package Protocols;

import Protocols.Helper.HelperContentReader;
import Protocols.Helper.HelperContentWriter;
import Protocols.Helper.HelperProtocols;

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
    public static void encapsulateRequest(String resource, DataOutputStream dos, boolean flush) throws IOException {
        int nrbytes = HelperContentWriter.calculateCapacity(0, List.of(resource),null,null);
        System.out.println("A encapsular pacote de pedido de recurso " + resource + ". Pacote com " + nrbytes + " bytes");
        HelperContentWriter hcw = new HelperContentWriter(nrbytes+1);
        hcw.writeByte(request);
        hcw.writeStr(resource);
        HelperProtocols.writeContentTCP(hcw,dos);
        makeFlush(dos,flush);
        System.out.println("Pacote encapsulado");
    }

    public static void encapsulateContent(byte[] file, DataOutputStream dos, boolean flush) throws IOException {
        int nrbytes = HelperContentWriter.calculateCapacity(0,null, Collections.singleton(file),null);
        System.out.println("A encapsular conteudo de um ficheiro. Pacote com " + nrbytes + " bytes.");
        HelperContentWriter hcw = new HelperContentWriter(nrbytes);
        hcw.writeBytes(file);
        HelperProtocols.writeContentTCP(hcw,dos);
        makeFlush(dos,flush);
        System.out.println("Conteudo escrito");
    }

    public static byte[] decapsulateContent(DataInputStream dis) {
        System.out.println("A desencapsular conteudo");
        HelperContentReader hcr = HelperProtocols.readContentTCP(dis);
        System.out.println("Conteudo lido");
        return hcr.readBytes();
    }


    public static void encapsulateConnection(DataOutputStream dos, boolean flush) throws IOException {
        System.out.println("A encapsular resposta de conecção");
        HelperContentWriter hcw = new HelperContentWriter(1);
        hcw.writeByte(connection);
        HelperProtocols.writeContentTCP(hcw,dos);
        makeFlush(dos,flush);
        System.out.println("Pacote escrito");
    }

    public static boolean decapsulateConnection(DataInputStream dis) {
        System.out.println("A desencapsultar pacote conecção");
        HelperContentReader hcr = HelperProtocols.readContentTCP(dis);
        System.out.println("Pacote desencapsulado");
        return hcr.readByte() == connection;
    }

    public static boolean isRequest(DataInputStream dis, String [] aux) {
        System.out.println("Há espera de confirmar se é request");
        HelperContentReader hcr = HelperProtocols.readContentTCP(dis);
        System.out.println("Mensagem recebida");
        boolean res = hcr.readByte() == request;
        if(res)
            aux[0] = hcr.readStr();
        return res;
    }
}
