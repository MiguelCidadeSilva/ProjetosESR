package Protocols.Utils;

public class Manager {
    public static void showSendPacket(String packet, int nrbytes, String destiny) {
        System.out.printf("A enviar %s para %s. Pacote com %d bytes%n",packet,destiny,nrbytes);
    }
    public static void sucessMessage(String task) {
        System.out.println("Tarefa " + task + " foi realizada com sucesso");
    }
}
