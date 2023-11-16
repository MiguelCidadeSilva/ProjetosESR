package Nodes.Classes;

import Nodes.Utils.Debug;
import Nodes.Utils.TaskExecutor;
import Nodes.Utils.VideoExtractor;
import Protocols.Helper.HelperConnection;
import Protocols.ProtocolBuildTree;
import Protocols.ProtocolLoadContent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

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
            Debug.printTask("- Recurso | Ficheiro");
            content.forEach((key, value) -> Debug.printTask("- " + key + " | " + value));
        } catch (IOException e) {
            System.out.println("Erro a carregar o servidor com o conteudo");
            throw new RuntimeException(e);
        }
    }
    private void sendVideo(VideoExtractor ve, DataOutputStream dos, String destiny) throws InterruptedException {
        if(ve.hasFrames())
        {
            TaskExecutor t = new TaskExecutor();
            t.start();
            int ss = 1;
            long sleep = 1000/ve.getFrameRate();
            while (ve.framesLeft() > 1)
            {
                int finalSs = ss;
                t.submitTask(() -> {try {ProtocolLoadContent.encapsulateVideo(ve,dos,true,destiny, finalSs);} catch (IOException ignored) {}});
                ss++;
                Thread.sleep(sleep);
            }
            t.shutdown();
            try {ProtocolLoadContent.encapsulateVideo(ve,dos,true,destiny,ss);} catch (IOException ignored) {};
        }
    }
    private void sendAudio(VideoExtractor ve, DataOutputStream dos, String destiny) throws InterruptedException {
        if (ve.hasAudio()) {
            TaskExecutor t = new TaskExecutor();
            t.start();
            int ss = 1;
            while (ve.audioLeft() > 1) {
                int finalSs = ss;
                t.submitTask(() -> {try {ProtocolLoadContent.encapsulateAudio(ve, dos, true, destiny, finalSs);} catch (IOException ignored) {}});
                ss++;
                Thread.sleep(1000);
            }
            t.shutdown();
            try {ProtocolLoadContent.encapsulateAudio(ve, dos, true, destiny, ss);} catch (IOException ignored) {};
        }
    }
    private void sendResource(String resource, DataOutputStream dos, String destiny) throws IOException, InterruptedException {
        Debug.printTask("Recurso " + resource + ": A começar streaming, ficheiro: " + content.get(resource));
        VideoExtractor ve = new VideoExtractor(resource,content.get(resource));
        Thread taudio = new Thread(() -> {try {sendAudio(ve,dos,destiny);} catch (InterruptedException ignored) {}});
        Thread tvideo = new Thread(() -> {try {sendVideo(ve,dos,destiny);} catch (InterruptedException ignored) {}});
        taudio.start();
        tvideo.start();
        taudio.join();
        tvideo.join();
        ProtocolLoadContent.encapsulateEndStream(ve,dos,true,destiny,1);
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
                    sendResource(aux[0],dos,socket.getInetAddress().getHostAddress());
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

    public void hasResource(Socket socket) {
        try {
            Debug.printLigacaoSucesso(socket.getInetAddress().getHostAddress());
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            HelperConnection hc = ProtocolBuildTree.decapsulateAsk(dis);
            if(this.content.containsKey(hc.name()))
                ProtocolBuildTree.encapsulateAnswerFound(dos);
            else
                ProtocolBuildTree.encapsulateAnswerNoFound(dos);
            socket.close();
        } catch (IOException e) {
            System.out.println("Erro ao atender o cliente " + socket.getInetAddress().getHostAddress() + ".");
            throw new RuntimeException(e);
        }
    }
}
