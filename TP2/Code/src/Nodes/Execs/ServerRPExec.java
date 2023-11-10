package Nodes.Execs;

import Nodes.Classes.ServerDB;
import Nodes.Classes.ServerRP;
import Nodes.Utils.Cods;
import Nodes.Utils.Debug;
import Protocols.Helper.HelperConnection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerRPExec {
    // java nome_exec file
    // file contem os ips dos servidores que contem o conteudo
    public static void testStreamRPDB(ServerRP rp) {
        rp.addResourceRP("db1");
        rp.addResourceRP("video.mp4");
        rp.addResourceRP("videoA.mp4");
    }
    public static void testAskHasResourceDB(ServerRP rp) throws InterruptedException {
        byte b1 = rp.receiveRequest(new HelperConnection("db1",new InetSocketAddress("127.0.0.1",8002)));
        String b1str = Byte.toString(b1);
        System.out.println("b1 = " + b1str);
        byte b2 = rp.receiveRequest(new HelperConnection("video.mp4",new InetSocketAddress("127.0.0.1",8002)));
        String b2str = Byte.toString(b2);
        System.out.println("b2 = " + b2str);
        byte b3 = rp.receiveRequest(new HelperConnection("videoA.mp4",new InetSocketAddress("127.0.0.1",8002)));
        String b3str = Byte.toString(b3);
        System.out.println("b3 = " + b3str);
    }

    public static void testAskHasResourceNode(ServerRP rp) {
        rp.addResource("teste");
    }

    public static void deliverTree(ServerSocket serverSocket, ServerRP sr) {
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread t1 = new Thread(() -> sr.respondeRequest(clientSocket));
                t1.start();
            }
        }catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    public static void main(String [] args) throws InterruptedException, IOException {
        ServerRP rp = new ServerRP(args[0]);
        if(args.length > 1)
	{
		switch (args[1]) {
		 	case "0":
				testStreamRPDB(rp);
				break;
			case "1":
				testAskHasResourceDB(rp);
				break;
		}
	}
	else
	{
        	ServerSocket serverSocketTree = new ServerSocket(Cods.portSOConnections);
        	System.out.println("ServerRP is listening at the ports " + Cods.portSOConnections);
        	deliverTree(serverSocketTree,rp);
	}
    }
}
