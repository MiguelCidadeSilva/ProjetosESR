package Nodes.Execs;

import Nodes.Classes.ServerNode;
import Protocols.Helper.HelperConnection;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class NodeExec {

    public static void testAskHasResourceNode(ServerNode sn, String resource) throws InterruptedException, UnknownHostException {
        byte b1 = sn.receiveRequest(new HelperConnection(resource, InetAddress.getLocalHost()));
        String b1str = Byte.toString(b1);
        System.out.println("b1 = " + b1str);
    }
    public static void main(String[] args) throws InterruptedException, IOException {
        ServerNode serverNode = new ServerNode(args[0]);
        if(args.length > 1)
        {
             switch (args[1]) {
                 case "0":
                     testAskHasResourceNode(serverNode,args[2]);
             }
        }
        else
            serverNode.initServer();
    }
}
