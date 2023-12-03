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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class ProtocolLoadContent{
    private static ReentrantLock lock = new ReentrantLock();
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
        LocalTime currentTime = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedTime = currentTime.format(formatter);
       // Debug.printTask(formattedTime + " | Encapsular pacote " + streamingPacket + " para o destino: " + destiny + ". Restam " + number + " pacotes de " + streamingPacket.getTask());
        return streamingPacket.writer();
    }
    public static void encapsulateEndStream(VideoExtractor ve, DataOutputStream dos,boolean flush, String destiny, int sequenceNumber) throws IOException {
        StreamingPacket streamingPacket = new StreamingPacket(ve.getVideo(), Cods.codEndStream,new byte[1],sequenceNumber,0);
        HelperContentWriter hcw = writePacket(streamingPacket,destiny, ve.framesLeft());
        HelperProtocols.writeContentTCP(hcw,dos);
        makeFlush(dos,flush);
    }
    private static void encapsulateByteArray(byte[] data, int sequenceNumber, VideoExtractor ve, DataOutputStream dos, String destiny, boolean flush, int codigo) throws IOException {
        List<byte[]> split = VideoExtractor.splitByteArray(data,Cods.packetSize);
        List<StreamingPacket> packets = new ArrayList<>(split.size());
        int packetsNumber = split.size();
        for(byte[] datasplit : split) {
            packets.add(new StreamingPacket(ve.getVideo(),codigo,datasplit,sequenceNumber,packetsNumber));
            packetsNumber--;
        }
        List<HelperContentWriter> hcws = packets.stream().map(p -> writePacket(p,destiny,codigo == Cods.codAudio ? ve.audioLeft() : ve.framesLeft())).toList();
        lock.lock();
        hcws.forEach(hcw -> {
            HelperProtocols.writeContentTCP(hcw,dos);
            try {makeFlush(dos,flush); } catch (IOException ignored) {}
        });
        lock.unlock();
    }
    public static void encapsulateAudio(VideoExtractor ve, DataOutputStream dos, boolean flush, String destiny, int sequenceNumber) throws IOException {
        byte[] audio = ve.nextAudio();
        encapsulateByteArray(audio,sequenceNumber,ve,dos,destiny,flush,Cods.codAudio);
    }
    public static void encapsulateVideo(VideoExtractor ve,DataOutputStream dos,boolean flush, String destiny, int sequenceNumber) throws IOException {
        byte[] video = ve.nextFrame();
        encapsulateByteArray(video,sequenceNumber,ve,dos,destiny,flush,Cods.codVideo);
    }
    public static void encapsulateNoExist(String resource,DataOutputStream dos,boolean flush, String destiny) throws IOException {
        StreamingPacket streamingPacket = new StreamingPacket(resource,Cods.codNoExist,new byte[1],1,0);
        HelperContentWriter hcw = writePacket(streamingPacket,destiny, 0);
        HelperProtocols.writeContentTCP(hcw,dos);
        makeFlush(dos,flush);
    }
    public static StreamingPacket decapsulateContent(DataInputStream dis) throws IOException {
        //Debug.printTask("À espera de pacotes de streaming");
        HelperContentReader hcr = HelperProtocols.readContentTCP(dis);
        return new StreamingPacket(hcr);
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
