package Nodes.Execs;

import Nodes.Classes.Client;

import java.io.IOException;
import java.net.InetAddress;

public class ClientExec {
    public static void main(String[] args) throws IOException {
        Client c = new Client(InetAddress.getByName(args[0]),InetAddress.getByName(args[1]),args[2]);
        System.out.println("Cliente com ip = "+args[0]+" cujo vizinho é "+ args[1]+ " criado.");
        System.out.println("Cliente a inicializar...");
        c.start();
    }
}
