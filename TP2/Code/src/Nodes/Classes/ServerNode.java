package Nodes.Classes;

import Protocols.ProtocolBuildTree;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ServerNode {
    private final List<String> neighbours;
    private final Map<String, List<InetSocketAddress>> clientsResourceMap;
    private final Map<String, ReadWriteLock> resourceLocks;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public ServerNode(String file) {
        try {
            this.neighbours = Files.readAllLines(Paths.get(file));
        } catch (IOException e) {
            System.out.println("Erro a carregar os vizinhos");
            throw new RuntimeException(e);
        }
        this.clientsResourceMap = new HashMap<>();
        this.resourceLocks = new HashMap<>();
    }

    public void addResource(String resource) {
        lock.writeLock().lock();
        this.clientsResourceMap.put(resource, new ArrayList<>());
        this.resourceLocks.put(resource,new ReentrantReadWriteLock());
        lock.writeLock().unlock();
    }
    public boolean hasResource(String resource) {
        lock.readLock().lock();
        boolean r = this.clientsResourceMap.containsKey(resource);
        lock.readLock().unlock();
        return r;
    }

    public void addClient(String resource, InetSocketAddress client){
        lock.readLock().lock();
        if(this.clientsResourceMap.containsKey(resource)){
            resourceLocks.get(resource).writeLock().lock();
            this.clientsResourceMap.get(resource).add(client);
            resourceLocks.get(resource).writeLock().unlock();
        }
        lock.readLock().unlock();
    }

    public boolean removeClient(String resource, InetSocketAddress client){
        boolean r = false;
        lock.writeLock().lock();
        if(this.clientsResourceMap.containsKey(resource)){
            resourceLocks.get(resource).writeLock().lock();
            this.clientsResourceMap.get(resource).remove(client);
            resourceLocks.get(resource).writeLock().unlock();
            if(this.clientsResourceMap.get(resource).isEmpty()){
                this.clientsResourceMap.remove(resource);
                this.resourceLocks.remove(resource);
                r=true;
            }
        }
        lock.writeLock().unlock();
        return r;
    }

    public String getNeighbour(int index) {
        return this.neighbours.get(index);
    }
    public int neighbourSize() {
        return this.neighbours.size();
    }

    public List<String> getNeighbours() {
        return neighbours;
    }

    // Método que recebo frames / partes de audio / partes do texto
    public void receiveResources() {}
    // Método que replica o conteudo recebido por todos os clientes
    public void sendResources() {}

    public boolean receiveRequest(String resource, InetSocketAddress origin) {
        // Ver se tem o recurso
        // Se tiver recurso, returnar true
        // Se não tiver recurso ver se há situação de loop
        // Se não houver situação de loop, guardar num map <recurso, lista de origens> a origem associada ao recurso
        // Contactar cada um dos vizinhos e guardar num map <vizinho, tempo de envio de request>
        // ao fim de contactar cada um dos vizinhos, calcular o minimo de tempo usado para cada vizinho
        // Se algum vizinho responder com loop, remover do map<vizinho,tempo de envio de request o vizinho>
        // Se o map<vizinho,tempo de envio de request o vizinho> estiver vazio retornar falso
        // pegar nesse vizinho com menor tempo
        // adicionar a um map<recurso,vizinho>
        // returnar false
        return false;
    }

    public DatagramPacket answerRequest(boolean found) {
        return found ? ProtocolBuildTree.encapsulateAnswerLoop() : ProtocolBuildTree.encapsulateAnswerFound();
    }
}
