package Servers.Execs;

import Servers.Classes.ServerRP;

public class ServerRPExec {
    // java nome_exec file
    // file contem os ips dos servidores que contem o conteudo
    public static void main(String [] args) {
        ServerRP rp = new ServerRP(args[0]);
        // Conteudo está carregado a partir daqui
        // esperar por conexões e mandar dados para as conexões
    }
}
