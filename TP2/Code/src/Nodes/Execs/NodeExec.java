package Nodes.Execs;
import java.util.List;
import Nodes.Classes.ServerNode;
import Protocols.Helper.HelperConnection;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class NodeExec {

    public static void testAskHasResourceNode(ServerNode sn, String resource) throws InterruptedException, UnknownHostException {
        byte b1 = sn.receiveRequest(new HelperConnection(resource, InetAddress.getByName("10.0.11.10")));
        String b1str = Byte.toString(b1);
        System.out.println("b1 = " + b1str);
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
             }
        }
	for(Thread t : lista)
		t.join();
    }
}
