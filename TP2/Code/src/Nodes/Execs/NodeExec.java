package Nodes.Execs;
import java.util.List;
import Nodes.Classes.ServerNode;
import Protocols.Helper.HelperConnection;
import Protocols.ProtocolBuildTree;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class NodeExec {

    public static byte testAskHasResourceNode(ServerNode sn, String resource) throws InterruptedException, UnknownHostException {
        byte b1 = sn.receiveRequest(new HelperConnection(resource, InetAddress.getByName("10.0.11.10")));
        System.out.println("b1 = " + b1);
        return b1;
    }
    public static boolean testAskStartStreaming(ServerNode sn, String resource) throws UnknownHostException {
        boolean res = sn.startStreamingCN(new HelperConnection(resource, InetAddress.getByName("10.0.11.10"));
        System.out.println("Valor de startStreaming = " + res);
        return res;
    }
    public static void main(String[] args) throws InterruptedException, IOException {
        ServerNode serverNode = new ServerNode(args[0]);
        List<Thread> lista = serverNode.initServer();
	    if(args.length > 1)
        {
             switch (args[1]) {
                 case "0":
                     testAskHasResourceNode(serverNode,args[2]);
                     break;
                 case "1":
                     byte b = testAskHasResourceNode(serverNode,args[2]);
                     if(b == ProtocolBuildTree.found)
                     {
                         boolean bool = testAskStartStreaming(serverNode,args[2]);
                         if(bool)
                             System.out.println("Vai começar a fazer streaming");
                         else
                             System.out.println("Não vai fazer streaming");
                     }
                     break;
             }
        }
        for(Thread t : lista)
            t.join();
    }
}
