package Nodes.Classes;

import Nodes.Utils.Debug;
import Nodes.Utils.OrganizeIps;
import Protocols.ProtocolLoadContent;
import Nodes.Utils.Cods;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ServerRP extends ServerNode{
    private final String dir = "Content/";
    // Constroi diretoria base com todo o conteudo
    private void buildBaseDirectory(){
        Path path = Paths.get(dir);
        try {
            if (Files.exists(path))
                Files.delete(path);
            Files.createDirectory(path);
        } catch (IOException ignored) {}
    }
    // Contacta um servidor que contem a base de dados
    public boolean contactServerDB(InetSocketAddress server, String resource) {
        boolean nosucess = true;
        try {
            Socket socket        = new Socket(server.getAddress().getHostAddress(), Cods.portDB);
            DataInputStream  dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            ProtocolLoadContent.encapsulateRequest(resource,dos,true, server.getAddress().getHostAddress());
            StreamingPacket packet = ProtocolLoadContent.decapsulateContent(dis);
            if(packet.getType() == Cods.codNoExist)
            {
                Debug.printError("Recurso não existe");
            }
            else
            {
                while(packet.getType() != Cods.codEndStream) {
                    Debug.printStramingPacket(packet);
                    packet = ProtocolLoadContent.decapsulateContent(dis);
                }
                Debug.printTask("Fim de streaming do recurso " + resource);
                nosucess = false;
            }
            socket.close();
        } catch (IOException e) {System.out.println(e.getMessage());}
        return nosucess;
    }
    public ServerRP(String fileIps) {
        super(fileIps);
        buildBaseDirectory();
    }

    public boolean addResourceRP(String resource) {
        if(!this.hasResource(resource))
        {
            System.out.println("A organizar os servidores a contactar");
            List<InetSocketAddress> dbServers = OrganizeIps.organizeIps(this.getNeighbours());
            System.out.println("Ordem: " + dbServers.stream().map(ip -> ip.getAddress().getHostAddress()).toList().toString());
            boolean noFound = true;
            for(int i = 0; i < dbServers.size() && noFound; i++)
                noFound = contactServerDB(dbServers.get(i),resource);
            if(noFound)
                Debug.printError("Recurso não existe na base de dados");
            return !noFound;
        }
        return true;
    }
}
