package Nodes.Execs;

import Nodes.Classes.ServerDB;
import Nodes.Utils.Ports;
import Protocols.ProtocolLoadContent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerDBExec {
    // java nome_exec file
    // file contem lista de ficheiros com conteudo
    public static void main(String [] args) throws IOException {
        ServerDB sdb = new ServerDB(args[0]);
        ServerSocket serverSocket = new ServerSocket(Ports.portDB);
        System.out.println("ServerDB is listening at the port " + Ports.portDB);
        while (true) {
            Socket clientSocket = serverSocket.accept();
            Thread t1 = new Thread(() -> sdb.repondeRP(clientSocket));
            t1.start();
        }
    }
}
