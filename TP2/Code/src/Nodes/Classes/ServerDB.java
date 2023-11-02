package Nodes.Classes;

import Nodes.Utils.Debug;
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
            System.out.println(content);
        } catch (IOException e) {
            System.out.println("Erro a carregar o servidor com o conteudo");
            throw new RuntimeException(e);
        }
    }

    public void repondeRP(Socket socket) {
        try {
            System.out.println("Cliente recebido");
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            String[] aux = new String[1];
            boolean isRequest = ProtocolLoadContent.isRequest(dis,aux);
            System.out.println(isRequest);
            if(isRequest)
            {
                byte[] contentFile = content.containsKey(aux[0]) ? Files.readAllBytes(Paths.get(content.get(aux[0]))) : new byte[0];
                ProtocolLoadContent.encapsulateContent(contentFile,dos,true,socket.getInetAddress().getHostAddress());
            }
            else
                ProtocolLoadContent.encapsulateConnection(dos,true,socket.getInetAddress().getHostAddress());
            socket.close();
        } catch (IOException e) {
            System.out.println("Erro ao atender o cliente " + socket.getInetAddress().getHostAddress());
        }
    }
}
