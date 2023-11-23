package Nodes.Execs;

import Nodes.Classes.ServerRP;
import Protocols.Helper.HelperConnection;
import java.util.List;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ServerRPExec {
    // java nome_exec file
    // file contem os ips dos servidores que contem o conteudo
    public static void testStreamRPDB(ServerRP rp, String resource) throws UnknownHostException {
        rp.addResourceRP(resource,InetAddress.getByName("10.0.11.10"),true);
    }
    public static void testAskHasResourceDB(ServerRP rp, String resource) throws InterruptedException, UnknownHostException {
        byte b1 = rp.receiveRequest(new HelperConnection(resource, InetAddress.getByName("10.0.11.10")));
        String b1str = Byte.toString(b1);
        System.out.println("Código recebido = " + b1str);
    }
    public static void testEndStream(ServerRP rp, String resource) throws IOException, InterruptedException {
        Thread t = new Thread(() -> {
            try {
                rp.addResourceRP(resource,InetAddress.getByName("10.0.11.10"),true);
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        });
        t.start();
        Thread.sleep(3000);
        rp.endStream(resource,rp.getNeighbour(0));
    }

    public static void main(String [] args) throws InterruptedException, IOException {
        ServerRP rp = new ServerRP(args[0]);
        if(args.length > 1) {
            switch (args[1]) {
                case "0":
                    testAskHasResourceDB(rp, args[2]);
                    break;
                case "1":
                    testStreamRPDB(rp, args[2]);
                    break;
                case "2":
                    testEndStream(rp,args[2]);
                    break;
            }
        }
        else {
            List<Thread> lista = rp.initServer();
            for(Thread t : lista)
                t.join();
        }
    }
}
