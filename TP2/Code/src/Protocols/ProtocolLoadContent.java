package Protocols;

import Nodes.Classes.StreamingPacket;
import Nodes.Utils.Cods;
import Nodes.Utils.Debug;
import Nodes.Utils.VideoExtractor;
import Protocols.Helper.HelperContentReader;
import Protocols.Helper.HelperContentWriter;
import Protocols.Helper.HelperProtocols;
import Protocols.Utils.Manager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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

    private static HelperContentWriter writePacket(StreamingPacket streamingPacket, String tarefa, String destiny) {
        int capacity = HelperContentWriter.calculateCapacity(1, List.of(streamingPacket.getResource()),List.of(streamingPacket.getContent()),null);
        Debug.printTask("A encapsular pacote de "+tarefa+" com " + capacity + " bytes (" + streamingPacket.getContent().length + " bytes)" + " para o destino: " + destiny);
        HelperContentWriter hcw = new HelperContentWriter(capacity);
        hcw.writeStr(streamingPacket.getResource());
        hcw.writeInt(streamingPacket.getType());
        hcw.writeBytes(streamingPacket.getContent());
        return hcw;
    }
    public static void encapsulateEndStream(VideoExtractor ve, DataOutputStream dos,boolean flush, String destiny) throws IOException {
        StreamingPacket streamingPacket = new StreamingPacket(ve.getVideo(), Cods.codEndStream,new byte[0]);
        HelperContentWriter hcw = writePacket(streamingPacket, "fim de stream",destiny);
        HelperProtocols.writeContentTCP(hcw,dos);
        makeFlush(dos,flush);
    }
    public static void encapsulateAudio(VideoExtractor ve, DataOutputStream dos, boolean flush, String destiny) throws IOException {
        byte[] audio = ve.nextAudio();
        StreamingPacket streamingPacket = new StreamingPacket(ve.getVideo(),Cods.codAudio,audio);
        HelperContentWriter hcw = writePacket(streamingPacket,"audio",destiny);
        HelperProtocols.writeContentTCP(hcw,dos);
        makeFlush(dos,flush);
    }
    public static void encapsulateVideo(VideoExtractor ve,DataOutputStream dos,boolean flush, String destiny) throws IOException {
        List<byte[]> frames = ve.nextFrames();
        int totalLength = frames.stream().mapToInt(array -> array.length).sum();
        StreamingPacket streamingPacket = new StreamingPacket(ve.getVideo(),Cods.codVideo,new byte[0]);
        int capacity = HelperContentWriter.calculateCapacity(2,List.of(streamingPacket.getResource()),frames,null);
        Debug.printTask("A encapsular pacote de frames com " + capacity + " bytes ("+totalLength+" bytes)" + " para o destino: " + destiny);
        HelperContentWriter hcw = new HelperContentWriter(capacity);
        hcw.writeStr(streamingPacket.getResource());
        hcw.writeInt(streamingPacket.getType());
        for(byte[] array : frames)
            hcw.writeBytes(array);
        HelperProtocols.writeContentTCP(hcw,dos);
        makeFlush(dos,flush);
    }
    public static void encapsulateNoExist(String resource,DataOutputStream dos,boolean flush, String destiny) throws IOException {
        StreamingPacket streamingPacket = new StreamingPacket(resource,Cods.codNoExist,new byte[0]);
        HelperContentWriter hcw = writePacket(streamingPacket,"não existe",destiny);
        HelperProtocols.writeContentTCP(hcw,dos);
        makeFlush(dos,flush);
    }
    private static List<byte[]> decapsulateFrames(HelperContentReader hcr)
    {
        List<byte[]> res = new ArrayList<>();
        while(hcr.hasContent())
            res.add(hcr.readBytes());
        return res;
    }
    private static byte[] concatenateByteArrays(List<byte[]> byteArrayList) {
        int totalLength = byteArrayList.stream().mapToInt(array -> array.length).sum();
        byte[] result = new byte[totalLength];
        int currentIndex = 0;
        for (byte[] byteArray : byteArrayList) {
            System.arraycopy(byteArray, 0, result, currentIndex, byteArray.length);
            currentIndex += byteArray.length;
        }
        return result;
    }
    public static StreamingPacket decapsulateContent(DataInputStream dis) throws IOException {
        Debug.printTask("À espera de pacotes de streaming");
        HelperContentReader hcr = HelperProtocols.readContentTCP(dis);
        String resource = hcr.readStr();
        int codigo = hcr.readInt();
        byte[] array = switch (codigo) {
            case Cods.codVideo -> {
                List<byte[]> frames = decapsulateFrames(hcr);
                yield concatenateByteArrays(frames);
            }
            case Cods.codAudio, Cods.codEndStream, Cods.codNoExist -> hcr.readBytes();
            default -> new byte[0];
        };
        return new StreamingPacket(resource,codigo,array);
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
        {
            aux[0] = hcr.readStr();
            Manager.printReceived("Pedido de request do recurso " + aux[0]);
        }
        else
            Manager.printReceived("Pedido de conexão");
        return res;
    }
}
