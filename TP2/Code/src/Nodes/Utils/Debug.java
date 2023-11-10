package Nodes.Utils;

import Nodes.Classes.StreamingPacket;

import java.util.List;
import java.util.Map;

public class Debug {
    public static void printBytes(byte [] bytes) {
        for(byte b : bytes)
            System.out.print(b + " ");
        System.out.println();
    }
    public static void printDBRP(Map<String,byte[]> content) {
        for(String key : content.keySet())
        {
            StringBuilder r = new StringBuilder("[");
            for(byte b : content.get(key))
                r.append(b).append(" ");
            System.out.println("(" + key + "," + r + "])");
        }
    }
    public static void printContent(List<String> list) {
        System.out.println(list);
    }
    public static void printStramingPacket(StreamingPacket sp) {
        String aux = "";
        if(sp.getType() == Cods.codEndStream)
            aux = "End Stream";
        else if(sp.getType() == Cods.codVideo)
            aux = "Video";
        else if(sp.getType() == Cods.codAudio)
            aux = "Audio";
        else if (sp.getType() == Cods.codNoExist)
            aux = "No found";
        System.out.println(sp.getResource() + "," + aux + "," + sp.getContent().length);
    }
    public static void printLigacaoSucesso(String ip) {
        System.out.println("Ligação com o servidor " + ip + " estabelecida com sucesso");
    }
    public static void printError(String message) {
        System.out.println("Erro: " +message);
    }
    public static void printTask(String message) {
        System.out.println(message);
    }

}
