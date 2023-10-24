package Servers.Classes;

import java.util.HashSet;
import java.util.Set;

public class ServerNode {
    private final Set<String> resources;

    public ServerNode() {
        this.resources = new HashSet<>();
    }
    public void addResource(String resource) {
        this.resources.add(resource);
    }
    public boolean hasResource(String elem) {
        return this.resources.contains(elem);
    }
    // Método que recebo frames / partes de audio / partes do texto
    public void receiveResources() {}
    // Método que replica o conteudo recebido por todos os clientes
    public void sendResources() {}
}
