package Nodes.Classes;

import Nodes.Utils.Debug;
import Nodes.Utils.OrganizeIps;
import Protocols.ProtocolLoadContent;
import Nodes.Utils.Cods;
import Protocols.ProtocolStartStreaming;

import java.io.*;
import java.net.InetAddress;
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
    public boolean contactServerDB(InetAddress server, String resource) {
        boolean nosucess = true;
        try {
            Socket socket        = new Socket(server.getHostAddress(), Cods.portDB);
            DataInputStream  dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            ProtocolLoadContent.encapsulateRequest(resource,dos,true, server.getHostAddress());
            StreamingPacket packet = ProtocolLoadContent.decapsulateContent(dis);
            multicast(packet);
            if(packet.getType() == Cods.codNoExist)
            {
                Debug.printError("Recurso não existe");
            }
            else
            {
                while(packet.getType() != Cods.codEndStream) {
                    Debug.printStramingPacket(packet);
                    packet = ProtocolLoadContent.decapsulateContent(dis);
		            multicast(packet);
                }
                Debug.printTask("Fim de streaming do recurso " + resource);
                this.removeResource(resource);
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

    public boolean addResourceRP(String resource, InetAddress client, boolean teste) {
        if(!this.hasResource(resource))
        {
            this.addResource(resource);
            if(!teste)
                this.addClient(resource,client);
            Debug.printTask("A organizar os servidores a contactar");
            List<InetAddress> dbServers = this.getBestNeighbours(resource);
            if(dbServers == null)
                dbServers = OrganizeIps.organizeIps(this.getNeighbours());
            Debug.printTask("Ordem: " + dbServers.stream().map(InetAddress::getHostAddress).toList().toString());
            boolean noFound = true;
            for(int i = 0; i < dbServers.size() && noFound; i++)
                noFound = contactServerDB(dbServers.get(i),resource);
            if(noFound)
                Debug.printError("Recurso não existe na base de dados");
            return !noFound;
        }
        else
        {
	        Debug.printTask("RP contém o conteudo. Não vai fazer pedido ao servidor DB");
            this.addClient(resource,client);
        }
        return true;
    }

    @Override
    protected boolean startStreaming(Socket socket) {
        try {
            Debug.printLigacaoSucesso(socket.getInetAddress().getHostAddress());
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            String resource = ProtocolStartStreaming.decapsulate(dis);
            socket.close();
            return this.addResourceRP(resource,socket.getInetAddress(),false);
        } catch (IOException  e) {
            throw new RuntimeException(e);
        }
    }
}
