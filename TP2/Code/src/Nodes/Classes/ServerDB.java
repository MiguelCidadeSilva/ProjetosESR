package Nodes.Classes;

import Nodes.Utils.Debug;
import Nodes.Utils.VideoExtractor;
import Protocols.Helper.HelperContentWriter;
import Protocols.ProtocolLoadContent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ServerDB {
    // private final Map<String,byte[]> content;
    // Carrega o conteudo dos ficheiros
    private final Map<String,String> content;
    public ServerDB(String file) {
        try {
            content = new HashMap<>();
            List<String> resources = Files.readAllLines(Paths.get(file));
            for(String resource : resources) {
                String[] aux = resource.split("/");
                String name = aux[aux.length-1];
                content.put(name,resource);
            }
            System.out.println(content);
        } catch (IOException e) {
            System.out.println("Erro a carregar o servidor com o conteudo");
            throw new RuntimeException(e);
        }
    }
    private void sendFrames(String resource,DataInputStream dis, DataOutputStream dos, String destiny) throws IOException, InterruptedException {
        Debug.printTask("Recurso " + resource + ": A começar streaming, ficheiro: " + content.get(resource));
        VideoExtractor ve = new VideoExtractor(resource,content.get(resource));
        while(ve.hasFrames())
        {
            ProtocolLoadContent.encapsulateAudio(ve,dos,true,destiny);
            ProtocolLoadContent.encapsulateVideo(ve,dos,true,destiny);
            System.out.println("Adormeceu");
            Thread.sleep(3000);
            System.out.println("Acordou " + ve.framesLeft() + " , " + ve.audioLeft());
        }
        ProtocolLoadContent.encapsulateEndStream(ve,dos,true,destiny);
        Debug.printTask("Recurso " + resource + ": terminou streaming");
    }
    public void repondeRP(Socket socket) {
        try {
            Debug.printLigacaoSucesso(socket.getInetAddress().getHostAddress());
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            String[] aux = new String[1];
            boolean isRequest = ProtocolLoadContent.isRequest(dis,aux);
            if(isRequest)
            {
                if(content.containsKey(aux[0]))
                    sendFrames(aux[0],dis,dos,socket.getInetAddress().getHostAddress());
                else
                    ProtocolLoadContent.encapsulateNoExist(aux[0],dos,true,socket.getInetAddress().getHostAddress());
            }
            else
                ProtocolLoadContent.encapsulateConnection(dos,true,socket.getInetAddress().getHostAddress());
            socket.close();
        } catch (IOException e) {
            System.out.println("Erro ao atender o cliente " + socket.getInetAddress().getHostAddress() + ".");
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            System.out.println("Erro ao mandar os frames");
        }
    }
}
