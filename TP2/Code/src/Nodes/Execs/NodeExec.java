package Nodes.Execs;

import Nodes.Classes.ServerNode;
import java.io.IOException;

public class NodeExec {

    public static void main(String[] args) throws InterruptedException, IOException {
        ServerNode serverNode = new ServerNode(args[0]);
        serverNode.initServer();
    }
}
