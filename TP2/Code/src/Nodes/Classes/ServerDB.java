package Nodes.Classes;

import Nodes.Utils.Debug;
import Nodes.Utils.TaskExecutor;
import Nodes.Utils.VideoExtractor;
import Protocols.Helper.HelperConnection;
import Protocols.ProtocolBuildTree;
import Protocols.ProtocolEndStreaming;
import Protocols.ProtocolLoadContent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ServerDB {
    // private final Map<String,byte[]> content;
    // Carrega o conteudo dos ficheiros
    private final Map<String,String> content;
    private final Map<String,Boolean> stop = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final boolean repeat;
    public ServerDB(String file, boolean repeat) {
        try {
            this.repeat = repeat;
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
    private boolean available(String resource) {
        lock.readLock().lock();
        boolean res = this.stop.get(resource);
        lock.readLock().unlock();
        return res;
    }
    private void addStream(String resource) {
        lock.writeLock().lock();
        this.stop.put(resource,true);
        lock.writeLock().unlock();
    }
    private void removeStream(String resource) {
        lock.writeLock().lock();
        this.stop.remove(resource);
        lock.writeLock().unlock();
    }

    private void endStreaming(HelperConnection hc) {
        lock.writeLock().lock();
        this.stop.put(hc.name(),false);
        lock.writeLock().unlock();
    }
    private void sendVideo(VideoExtractor ve, DataOutputStream dos, String destiny) throws InterruptedException {
        if(ve.hasFrames())
        {
            TaskExecutor t = new TaskExecutor();
            t.start();
            int ss = 1;
            long sleep = 1000/ve.getFrameRate();
            while (ve.framesLeft() > 1 && available(ve.getVideo()))
            {
                int finalSs = ss;
                t.submitTask(() -> {try {ProtocolLoadContent.encapsulateVideo(ve,dos,true,destiny, finalSs);}  catch (IOException ignored) {}});
                ss++;
                Thread.sleep(sleep);
            }
            t.shutdown();
            if(available(ve.getVideo()))
                try {ProtocolLoadContent.encapsulateVideo(ve,dos,true,destiny,ss);}  catch (IOException ignored) {};
        }
    }

    private void sendAudio(VideoExtractor ve, DataOutputStream dos, String destiny) throws InterruptedException {
        if (ve.hasAudio()) {
            TaskExecutor t = new TaskExecutor();
            t.start();
            int ss = 1;
            while (ve.audioLeft() > 1 && available(ve.getVideo())) {
                int finalSs = ss;
                t.submitTask(() -> {try {ProtocolLoadContent.encapsulateAudio(ve, dos, true, destiny, finalSs);}  catch (IOException ignored) {}});
                ss++;
                Thread.sleep(1000);
            }
            t.shutdown();
            if(available(ve.getVideo()))
                try {ProtocolLoadContent.encapsulateAudio(ve, dos, true, destiny, ss);} catch (IOException ignored) {};
        }
    }
    private void sendResource(String resource, DataOutputStream dos, String destiny) throws IOException, InterruptedException {
        Debug.printTask(" A iniciar streaming do recurso "+ resource+", do ficheiro: " + content.get(resource));
        this.addStream(resource);
        VideoExtractor ve = new VideoExtractor(resource,content.get(resource),repeat);
        Thread taudio = new Thread(() -> {try {sendAudio(ve,dos,destiny);} catch (InterruptedException ignored) {}});
        Thread tvideo = new Thread(() -> {try {sendVideo(ve,dos,destiny);} catch (InterruptedException ignored) {}});
        taudio.start();
        tvideo.start();
        taudio.join();
        tvideo.join();
        ProtocolLoadContent.encapsulateEndStream(ve,dos,true,destiny,1);
        this.removeStream(resource);
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
    public void endStream(Socket socket) {
        try {
            Debug.printLigacaoSucesso(socket.getInetAddress().getHostAddress());
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            HelperConnection hc = ProtocolEndStreaming.decapsulate(dis);
            socket.close();
            endStreaming(hc);

        } catch (IOException e) {
            System.out.println("Erro ao atender o cliente " + socket.getInetAddress().getHostAddress() + ".");
            throw new RuntimeException(e);
        }
    }
}
