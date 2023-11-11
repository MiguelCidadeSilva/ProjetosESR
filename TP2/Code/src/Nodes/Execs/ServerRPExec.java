package Nodes.Execs;

import Nodes.Classes.ServerRP;
import Protocols.Helper.HelperConnection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ServerRPExec {
    // java nome_exec file
    // file contem os ips dos servidores que contem o conteudo
    public static void testStreamRPDB(ServerRP rp, String resource) {
        rp.addResourceRP(resource);
    }
    public static void testAskHasResourceDB(ServerRP rp, String resource) throws InterruptedException, UnknownHostException {
        byte b1 = rp.receiveRequest(new HelperConnection(resource, InetAddress.getLocalHost()));
        String b1str = Byte.toString(b1);
        System.out.println("Código recebido = " + b1str);
    }

    public static void main(String [] args) throws InterruptedException, IOException {
        ServerRP rp = new ServerRP(args[0]);
        if(args.length > 1) {
            switch (args[1]) {
                case "0":
                    testStreamRPDB(rp, args[2]);
                    break;
                case "1":
                    testAskHasResourceDB(rp, args[2]);
                    break;
            }
        }
        else
            rp.initServer();
    }
}
