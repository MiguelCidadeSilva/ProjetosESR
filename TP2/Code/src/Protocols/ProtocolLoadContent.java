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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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

    private static HelperContentWriter writePacket(StreamingPacket streamingPacket, String destiny, int number) {
        int capacity = HelperContentWriter.calculateCapacity(1, List.of(streamingPacket.getResource()),List.of(streamingPacket.getContent()),null);

        LocalTime currentTime = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedTime = currentTime.format(formatter);
        Debug.printTask(formattedTime + " | Encapsular pacote " + streamingPacket + " para o destino: " + destiny + ". Restam " + number + " pacotes de " + streamingPacket.getTask());
        HelperContentWriter hcw = new HelperContentWriter(capacity);
        hcw.writeStr(streamingPacket.getResource());
        hcw.writeInt(streamingPacket.getType());
        hcw.writeBytes(streamingPacket.getContent());
        return hcw;
    }
    public static void encapsulateEndStream(VideoExtractor ve, DataOutputStream dos,boolean flush, String destiny) throws IOException {
        StreamingPacket streamingPacket = new StreamingPacket(ve.getVideo(), Cods.codEndStream,new byte[0]);
        HelperContentWriter hcw = writePacket(streamingPacket,destiny, ve.framesLeft());
        HelperProtocols.writeContentTCP(hcw,dos);
        makeFlush(dos,flush);
    }
    public static void encapsulateAudio(VideoExtractor ve, DataOutputStream dos, boolean flush, String destiny) throws IOException {
        byte[] audio = ve.nextAudio();
        StreamingPacket streamingPacket = new StreamingPacket(ve.getVideo(),Cods.codAudio,audio);
        HelperContentWriter hcw = writePacket(streamingPacket,destiny, ve.audioLeft());
        HelperProtocols.writeContentTCP(hcw,dos);
        makeFlush(dos,flush);
    }
    /*
    public static void encapsulateVideo(VideoExtractor ve,DataOutputStream dos,boolean flush, String destiny) throws IOException {
        List<byte[]> frames = ve.nextFrames();
        StreamingPacket streamingPacket = new StreamingPacket(ve.getVideo(),Cods.codVideo, FramesAux.encapsulateFrames(frames));
        HelperContentWriter hcw = writePacket(streamingPacket,"video",destiny,ve.framesLeft());
        HelperProtocols.writeContentTCP(hcw,dos);
        makeFlush(dos,flush);
    }
    */
    public static long encapsulateVideo(VideoExtractor ve,DataOutputStream dos,boolean flush, String destiny) throws IOException {
        long starttime = System.currentTimeMillis();
        List<StreamingPacket> frames = ve.nextFrames().stream().map(frame -> new StreamingPacket(ve.getVideo(),Cods.codVideo, frame)).toList();
        List<HelperContentWriter> hcws = frames.stream().map(frame -> writePacket(frame,destiny,ve.framesLeft())).toList();
        hcws.forEach(hcw -> HelperProtocols.writeContentTCP(hcw,dos));
        makeFlush(dos,flush);
        return System.currentTimeMillis() - starttime;
    }
    public static void encapsulateNoExist(String resource,DataOutputStream dos,boolean flush, String destiny) throws IOException {
        StreamingPacket streamingPacket = new StreamingPacket(resource,Cods.codNoExist,new byte[0]);
        HelperContentWriter hcw = writePacket(streamingPacket,destiny, 0);
        HelperProtocols.writeContentTCP(hcw,dos);
        makeFlush(dos,flush);
    }
    public static StreamingPacket decapsulateContent(DataInputStream dis) throws IOException {
        Debug.printTask("À espera de pacotes de streaming");
        HelperContentReader hcr = HelperProtocols.readContentTCP(dis);
        String resource = hcr.readStr();
        int codigo = hcr.readInt();
        byte[] array = hcr.readBytes();
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
