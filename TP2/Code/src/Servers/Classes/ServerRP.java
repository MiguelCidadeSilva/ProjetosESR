package Servers.Classes;

import Protocolos.ProtocolLoadContent;
import Servers.Utils.Ports;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ServerRP {
    Map<String,byte[]> content;
    public boolean loadContentServer(String server) {
        boolean nosucess = false;
        try {
            Socket clientSocket = new Socket(server, Ports.portDB);
            System.out.println("Connected to the server.");
            DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
            this.content = ProtocolLoadContent.desencapsulate(dis);
        } catch (IOException e) {
            nosucess = true;
        }
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
}
