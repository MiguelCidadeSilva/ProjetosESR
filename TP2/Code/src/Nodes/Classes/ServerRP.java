package Nodes.Classes;

import Nodes.Utils.OrganizeIps;
import Protocols.ProtocolLoadContent;
import Nodes.Utils.Ports;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            Socket socket = new Socket(server.getAddress().getHostAddress(), Ports.portDB);
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            System.out.println(dis.available());
            ProtocolLoadContent.encapsulateRequest(resource,dos,true, server.getAddress().getHostAddress());
            System.out.println(dis.available());
            byte[] content = ProtocolLoadContent.decapsulateContent(dis);
            if(content.length > 0)
            {
                this.addResource(resource);
                Files.write(Paths.get(dir+resource), content);
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

    private boolean addResourceRP(String resource) {
        if(!this.hasResource(resource))
        {
            System.out.println("A organizar os servidores a contactar");
            List<InetSocketAddress> dbServers = OrganizeIps.organizeIps(this.getNeighbours());
            System.out.println("Ordem: " + dbServers.toString());
            boolean noFound = true;
            for(int i = 0; i < dbServers.size() && noFound; i++)
                noFound = contactServerDB(dbServers.get(i),resource);
            return !noFound;
        }
        return true;
    }
}
