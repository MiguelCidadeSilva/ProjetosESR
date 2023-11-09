package Nodes.Execs;

import Nodes.Classes.ServerRP;
import Protocols.Helper.HelperConnection;

public class ServerRPExec {
    // java nome_exec file
    // file contem os ips dos servidores que contem o conteudo
    public static void main(String [] args) throws InterruptedException {
        ServerRP rp = new ServerRP(args[0]);
        rp.addResourceRP("db1");
        rp.addResourceRP("video.mp4");
        // rp.receiveRequest("db3",null);
    }
}
