package Servers.Classes;

import Protocolos.ProtocolLoadContent;
import Servers.Utils.Ports;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class ServerRP extends ServerNode{
    private final String dir = "Content/";
    // Constroi diretoria base com todo o conteudo
    private void buildBaseDirectory(){
        Path path = Paths.get(dir);
        try {
            Files.deleteIfExists(path);
            Files.createDirectory(path);
        } catch (IOException ignored) {}
    }
    // Carrega os ficheiros recebidos pelo servidor
    private void loadContent(Map<String,byte[]> content) {
        buildBaseDirectory();
        content.keySet().forEach(this::addResource);
        content.forEach((key, value) -> {
            try { Files.write(Paths.get(dir+key), value); }
            catch (IOException ignored) {}
        });
    }
    // Contacta um servidor que contem a base de dados
    public boolean contactServer(String server) {
        boolean nosucess = true;
        try {
            Socket clientSocket = new Socket(server, Ports.portDB);
            System.out.println("Connected to the server " + server + " .");
            DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
            Map<String,byte[]> content = ProtocolLoadContent.desencapsulate(dis);
            clientSocket.close();
            loadContent(content);
            nosucess = false;
        } catch (IOException e) {System.out.println(e.getMessage());}
        return nosucess;
    }
    public ServerRP(String fileIps) {
        try {
            List<String> ips = Files.readAllLines(Paths.get(fileIps));
            // Collection<Thread> threads = ips.stream().map(str -> new Thread(() -> this.loadContentServer(str))).toList();
            boolean noContent = true;
            for (int i = 0; i < ips.size() && noContent; i++)
                noContent = contactServer(ips.get(i));
        } catch (IOException e) {
            System.out.println("Erro a carregar conteudo para o RP");
            throw new RuntimeException(e);
        }
    }

    // No server RP não faz sentido este método.
    // Talvez criar uma classe nova que identifique os servers intermédios e tirar este médio da
    // classe ServerNode.
    // Nos clientes é o contrário, só faz sentido ter este método e não o sendResouces.
    @Override
    public void receiveResources() {}

    // Método que vai mandando os frames / pedaços de audio / texto para os clientes
    @Override
    public void sendResources() {
        // todo
    }
}
