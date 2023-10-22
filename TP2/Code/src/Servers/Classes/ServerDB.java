package Servers.Classes;

import Protocolos.ProtocolLoadContent;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ServerDB {
    private final Map<String,byte[]> content;

    // Carrega o conteudo dos ficheiros
    public ServerDB(String file) {
        try {
            this.content = new HashMap<>();
            // Ficheiros que contem conteudo
            Collection<String> files = Files.readAllLines(Paths.get(file));
            // Percorrer cada um dos ficheiros e encher a variavel content
            for(String str : files) {
                byte[] contentFile = Files.readAllBytes(Paths.get(str));
                content.put(str,contentFile);
            }
        } catch (IOException e) {
            System.out.println("Erro a carregar o servidor com o conteudo");
            throw new RuntimeException(e);
        }
    }
    // Escreve o conteudo no socket
    public void writeContent(DataOutputStream dos) {
        ProtocolLoadContent.encapsulate(content,dos);
    }

    public void printDebug() {
        for(String key : content.keySet())
        {
            StringBuilder r = new StringBuilder("[");
            for(byte b : content.get(key))
                r.append(b).append(" ");
            System.out.println("(" + key + "," + r + "])");
        }
    }
}
