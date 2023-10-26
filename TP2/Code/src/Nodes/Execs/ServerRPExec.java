package Nodes.Execs;

import Nodes.Classes.ServerRP;

public class ServerRPExec {
    // java nome_exec file
    // file contem os ips dos servidores que contem o conteudo
    public static void main(String [] args) {
        ServerRP rp = new ServerRP(args[0]);
        rp.receiveRequest("db1",null);
        rp.receiveRequest("db2",null);
        // rp.receiveRequest("db3",null);
        // Conteudo está carregado a partir daqui
        // esperar por conexões e mandar dados para as conexões
    }
}
