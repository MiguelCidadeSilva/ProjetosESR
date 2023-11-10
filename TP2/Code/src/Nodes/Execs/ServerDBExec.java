package Nodes.Execs;

import Nodes.Classes.ServerDB;
import Nodes.Utils.Cods;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerDBExec {
    // java nome_exec file
    // file contem lista de ficheiros com conteudo
    public static void deliverContent(ServerSocket serverSocket, ServerDB sdb) {
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread t1 = new Thread(() -> sdb.repondeRP(clientSocket));
                t1.start();
            }
        }catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    public static void deliverTree(ServerSocket serverSocket, ServerDB sdb) {
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread t1 = new Thread(() -> sdb.hasResource(clientSocket));
                t1.start();
            }
        }catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    public static void main(String [] args) throws IOException {
        ServerDB sdb = new ServerDB(args[0]);
        ServerSocket serverSocketContent = new ServerSocket(Cods.portDB);
        ServerSocket serverSocketTree = new ServerSocket(Cods.portSOConnections);
        System.out.println("ServerDB is listening at the ports " + Cods.portDB + " and " + Cods.portSOConnections);
        Thread tcontent =  new Thread(() -> deliverContent(serverSocketContent,sdb));
        tcontent.start();
        deliverTree(serverSocketTree,sdb);
    }
}
