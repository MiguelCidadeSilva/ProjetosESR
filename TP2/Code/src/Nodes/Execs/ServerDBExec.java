package Nodes.Execs;

import Nodes.Classes.ServerDB;
import Nodes.Utils.Ports;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

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
        ServerSocket serverSocket = new ServerSocket(Ports.portDB);
        System.out.println("ServerDB is listening at the port " + Ports.portDB);
        while (true) {
            Socket clientSocket = serverSocket.accept();
            Thread t1 = new Thread(() -> atendeCliente(clientSocket, sdb));
            t1.start();
        }
    }
}
