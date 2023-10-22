package Servers.Classes;

import Protocolos.ProtocolLoadContent;
import Servers.Utils.Ports;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class ServerRP {
    Map<String,byte[]> content;
    public boolean loadContentServer(String server) {
        boolean nosucess = true;
        try {
            Socket clientSocket = new Socket(server, Ports.portDB);
            System.out.println("Connected to the server.");
            DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
            this.content = ProtocolLoadContent.desencapsulate(dis);
            nosucess = false;
        } catch (IOException ignored) {}
        return nosucess;
    }
    public ServerRP(String fileIps)
    {
        try {
            List<String> ips = Files.readAllLines(Paths.get(fileIps));
            // Collection<Thread> threads = ips.stream().map(str -> new Thread(() -> this.loadContentServer(str))).toList();
            boolean noContent = true;
            for (int i = 0; i < ips.size() && noContent; i++)
                noContent = loadContentServer(ips.get(i));
        } catch (IOException e) {
            System.out.println("Erro a carregar conteudo para o RP");
            throw new RuntimeException(e);
        }
    }

    public void printDebug() {
        for(String key : content.keySet())
        {
            StringBuilder r = new StringBuilder("[");
            for(byte b : content.get(key))
                r.append(b).append(" ");
            System.out.println("(" + key + "," + r + "])");
        }
    }
}
