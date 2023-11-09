package Nodes.Classes;

import Nodes.Utils.Cods;
import Protocols.Helper.HelperConnection;
import Protocols.ProtocolBuildTree;
import Protocols.ProtocolTransferContent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class ServerNode {
    private final List<InetSocketAddress> neighbours;
    private final Map<String, List<InetSocketAddress>> clientsResourceMap;
    private final Map<String, ReadWriteLock> resourceLocks;
    private final Map<String, List<InetSocketAddress>> clientsBuildTree;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final ReadWriteLock lockTree = new ReentrantReadWriteLock();

    public ServerNode(String file) {
        try {
            this.neighbours = Files.readAllLines(Paths.get(file)).stream().map(str -> new InetSocketAddress(str, Cods.portSOConnections)).collect(Collectors.toList());
        } catch (IOException e) {
            System.out.println("Erro a carregar os vizinhos");
            throw new RuntimeException(e);
        }
        this.clientsResourceMap = new HashMap<>();
        this.resourceLocks = new HashMap<>();
        this.clientsBuildTree = new HashMap<>();
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

    public InetSocketAddress getNeighbour(int index) {
        return this.neighbours.get(index);
    }
    public int neighbourSize() {
        return this.neighbours.size();
    }

    public List<InetSocketAddress> getNeighbours() {
        return neighbours;
    }

    // Método que recebo frames / partes de audio / partes do texto
    public StreamingPacket receiveResources(DatagramPacket datagramPacket) {
        return ProtocolTransferContent.decapsulate(datagramPacket);
    }
    // Método que replica o conteudo recebido por todos os clientes
    public List<InetSocketAddress> getClientList(StreamingPacket streamingPacket) {
        String resource = streamingPacket.getResource();
        lock.readLock().lock();
        resourceLocks.get(resource).readLock().lock();
        List<InetSocketAddress> clients = new ArrayList<>(clientsResourceMap.get(resource));
        resourceLocks.get(resource).readLock().unlock();
        lock.readLock().unlock();
        return clients;
    }
    public void sendRequest(InetSocketAddress neighbour, String resource, InetSocketAddress origin) {
        try {
            Socket socket = new Socket(neighbour.getAddress(), Cods.portSOConnections);
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            ProtocolBuildTree.encapsulateAsk(origin,resource,dos);
            boolean found = ProtocolBuildTree.decapsulateAnswer(dis);
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public boolean receiveRequest(HelperConnection hc) throws InterruptedException {
        // Ver se tem o recurso
        // Se tiver recurso, returnar true
        String resource = hc.name();
        InetSocketAddress origin = hc.address();
        /*
        if(this.hasResource(resource))
            return true;
        // Se não tiver recurso ver se há situação de loop
        this.lockTree.writeLock().lock();
        if(this.clientsBuildTree.get(resource).contains(origin)) {
            this.lockTree.writeLock().unlock();
            return false;
        }
        this.clientsBuildTree.get(resource).add(origin);
        this.lockTree.writeLock().unlock();
         */
        List<Thread> list = new ArrayList<>();
        for (InetSocketAddress neighbour : neighbours) {
            Thread t = new Thread(() -> sendRequest(neighbour, resource, origin));
            t.start();
            list.add(t);
        }
        for(Thread t : list)
            t.join();

        // Se não houver situação de loop, guardar num map <recurso, lista de origens> a origem associada ao recurso
        // Contactar cada um dos vizinhos e guardar num map <vizinho, tempo de envio de request>
        // ao fim de contactar cada um dos vizinhos, calcular o minimo de tempo usado para cada vizinho
        // Se algum vizinho responder com loop, remover do map<vizinho,tempo de envio de request o vizinho>
        // Se o map<vizinho,tempo de envio de request o vizinho> estiver vazio retornar falso
        // pegar nesse vizinho com menor tempo
        // adicionar a um map<recurso,vizinho>
        // returnar false
        /*
        this.lockTree.writeLock().lock();
        this.clientsBuildTree.get(resource).remove(origin);
        this.lockTree.writeLock().unlock();
        */
        return false;
    }

    public void answerRequest(boolean found, DataOutputStream dos) throws IOException {
        if(found)
            ProtocolBuildTree.encapsulateAnswerLoop(dos);
        else
            ProtocolBuildTree.encapsulateAnswerFound(dos);
    }
}
