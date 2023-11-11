package Nodes.Execs;

import Nodes.Classes.ServerDB;
import Nodes.Utils.Cods;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerDBExec {
    // java nome_exec file
    // file contem lista de ficheiros com conteudo
    public static void deliverContent(ServerDB sdb) {
        try(ServerSocket serverSocket = new ServerSocket(Cods.portDB))
        {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread t1 = new Thread(() -> sdb.repondeRP(clientSocket));
                t1.start();
            }
        }catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    public static void deliverTree(ServerDB sdb) {
        try(ServerSocket serverSocket = new ServerSocket(Cods.portSOConnections))
        {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread t1 = new Thread(() -> sdb.hasResource(clientSocket));
                t1.start();
            }
        }catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    public static void main(String [] args) {
        ServerDB sdb = new ServerDB(args[0]);
        System.out.println("ServerDB is listening at the ports " + Cods.portDB + " and " + Cods.portSOConnections);
        Thread tcontent =  new Thread(() -> deliverContent(sdb));
        tcontent.start();
        deliverTree(sdb);
    }
}
