package Nodes.Execs;

import Nodes.Classes.Client;

import java.io.IOException;
import java.net.InetAddress;

public class ClientExec {
    public static void main(String[] args) throws IOException {
        Client c = new Client(InetAddress.getByName(args[0]),InetAddress.getByName(args[1]),args[2]);
        System.out.println("Client with ip = "+args[0]+" whose neighbour is "+ args[1]+ " created.");
        System.out.println("Client starting...");
        c.start();
    }
}
