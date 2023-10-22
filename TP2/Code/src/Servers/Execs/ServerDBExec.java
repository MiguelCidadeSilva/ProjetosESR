package Servers.Execs;

import Protocolos.ProtocolLoadContent;
import Servers.Classes.ServerDB;
import Servers.Utils.Ports;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ServerDBExec {
    public static void atendeCliente(Socket clientSocket, ServerDB sdb) {
        try {
            DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
            sdb.writeContent(dos);
            dos.flush();
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("Erro ao atender o cliente " + clientSocket.getInetAddress().getHostAddress());
        }
    }
    // java nome_exec file
    // file contem lista de ficheiros com conteudo
    public static void main(String [] args) throws IOException {
        ServerDB sdb = new ServerDB(args[0]);
        sdb.printDebug();
        ServerSocket serverSocket = new ServerSocket(Ports.portDB);
        System.out.println("ServerDB is listening at the port " + Ports.portDB);
        while (true) {
            Socket clientSocket = serverSocket.accept();
            Thread t1 = new Thread(() -> atendeCliente(clientSocket, sdb));
            t1.start();
        }
    }
}
