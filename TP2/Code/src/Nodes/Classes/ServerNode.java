package Nodes.Classes;

import Nodes.Utils.Cods;
import Nodes.Utils.Debug;
import Protocols.Helper.HelperConnection;
import Protocols.ProtocolBuildTree;
import Protocols.ProtocolStartStreaming;
import Protocols.ProtocolTransferContent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
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
    private final Map<String, List<InetSocketAddress>> bestNeighbours;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReadWriteLock lockTree = new ReentrantReadWriteLock();
    private final ReadWriteLock lockBestN = new ReentrantReadWriteLock();

    public ServerNode(String file) {
        try {
            this.neighbours = Files.readAllLines(Paths.get(file)).stream().map(str -> new InetSocketAddress(str, Cods.portSOConnections)).collect(Collectors.toList());
        } catch (IOException e) {
            System.out.println("Erro a carregar os vizinhos");
            throw new RuntimeException(e);
        }
        this.clientsResourceMap = new HashMap<>();
        this.resourceLocks      = new HashMap<>();
        this.clientsBuildTree   = new HashMap<>();
        this.bestNeighbours     = new HashMap<>();
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

    public boolean removeResource(String resource) {
        boolean res = false;
        lock.writeLock().lock();
        if(this.clientsResourceMap.containsKey(resource)){
            this.clientsResourceMap.remove(resource);
            this.resourceLocks.remove(resource);
            res= true;
        }
        lock.writeLock().unlock();
        return res;
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
    protected List<InetSocketAddress> getBestNeighbours(String resource) {
        this.lockBestN.readLock().lock();
        List<InetSocketAddress> bestneighbours = new ArrayList<>(this.bestNeighbours.get(resource));
        this.lockBestN.readLock().unlock();
        return bestneighbours;
    }
    // Método que recebe frames / partes de audio / partes do texto

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

    public void multicast(StreamingPacket packet) throws IOException {
        List<InetSocketAddress> clients = getClientList(packet);
        Debug.printTask("A fazer multicast do recurso " + packet.getResource() + " para os clientes: " + clients.stream().map(i -> i.getAddress().getHostAddress()).toList());
        Debug.printTask("A encapsular o pacote");
        DatagramPacket dp = ProtocolTransferContent.encapsulate(packet);
        Debug.printTask("Pacote encapsulado, DatagramPacket resultante: "+dp);
        for (InetSocketAddress client : clients) {
            Debug.printTask("Criação de socket...");
            DatagramSocket ds = new DatagramSocket(client.getPort(),client.getAddress());
            Debug.printTask("Socket criado para a porta "+client.getPort() + " do cliente "+client.getAddress());
            Debug.printTask("Envio do datagrama");
            ds.send(dp);
            Debug.printTask("Datagrama enviado.");
            ds.close();
            Debug.printTask("Socket fechado.");
        }
    }

    public void sendRequest(InetSocketAddress neighbour, String resource, InetSocketAddress origin, int tid,Map<Integer,Long> responseTime) {
        try {
            String ip = neighbour.getAddress().getHostAddress();
            Debug.printTask("A perguntar " + ip + " se tem o recurso " + resource);
            Long startTime = System.currentTimeMillis();
            Socket socket = new Socket(neighbour.getAddress(), Cods.portSOConnections);
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            ProtocolBuildTree.encapsulateAsk(origin,resource,dos);
            byte codigo = ProtocolBuildTree.decapsulateAnswer(dis);
            socket.close();
            Long endTime = System.currentTimeMillis();
            Long delay = endTime - startTime;
            if (codigo == ProtocolBuildTree.found) {
                Debug.printTask("Vizinho " + ip + " contém o conteudo");
                responseTime.put(tid, delay);
            }
            else if (codigo == ProtocolBuildTree.loop) {
                Debug.printTask("Loop com o Vizinho " + ip );
            }
            else {
                Debug.printTask("Vizinho " + ip + " não encontrou o recurso.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public byte receiveRequest(HelperConnection hc) throws InterruptedException {
        // Ver se tem o recurso
        // Se tiver recurso, returnar true
        byte res = ProtocolBuildTree.nofound;
        String resource = hc.name();
        if(this.hasResource(resource)) {
            Debug.printTask("Tenho o recurso " + resource + ". Responder com código " + ProtocolBuildTree.found);
            res = ProtocolBuildTree.found;
        }
        else
        {
            int tid = 0;
            Map <Integer, Long> responseTime = new HashMap<>();
            Map <Integer, InetSocketAddress> neighbourMap = new HashMap<>();
            InetSocketAddress origin = hc.address();
            // Se não tiver recurso ver se há situação de loop
            this.lockTree.writeLock().lock();
            if(!this.clientsBuildTree.containsKey(resource))
                this.clientsBuildTree.put(resource,new ArrayList<>());
            if(this.clientsBuildTree.get(resource).contains(origin)) {
                this.lockTree.writeLock().unlock();
                Debug.printTask("Situação de loop . Responder com código " + ProtocolBuildTree.loop);
                res = ProtocolBuildTree.loop;
            }
            else
            {
                this.clientsBuildTree.get(resource).add(origin);
                this.lockTree.writeLock().unlock();
                Debug.printTask("Não foi encontrado o recurso em memória, a contactar vizinhos");
                List<Thread> list = new ArrayList<>();
                for (InetSocketAddress neighbour : neighbours) {
                    tid+=1;
                    neighbourMap.put(tid,neighbour);
                    int finalTid = tid;
                    Debug.printTask("A contactar vizinho " + neighbour.getAddress().getHostAddress() + " a perguntar se tem o recurso " + resource);
                    Thread t = new Thread(() -> sendRequest(neighbour, resource, origin, finalTid, responseTime));
                    t.start();
                    list.add(t);
                }
                for(Thread t : list)
                    t.join();
                List<InetSocketAddress> sortedNeighbours = responseTime.entrySet().stream().sorted(Map.Entry.comparingByValue()).map(Map.Entry::getKey).map(neighbourMap::get).toList();
                Debug.printTask("Vizinhos ordenados: " + sortedNeighbours.stream().map(i -> i.getAddress().getHostAddress()).toList());
                if(!sortedNeighbours.isEmpty()) {
                    System.out.println("Encontrou vizinhos com recursos. Responder com código " + ProtocolBuildTree.found);
                    res = ProtocolBuildTree.found;
                    this.lockBestN.writeLock().lock();
                    this.bestNeighbours.put(resource,sortedNeighbours);
                    this.lockBestN.writeLock().unlock();
                }
                else
                    System.out.println("Não encontrou vizinhos com o recurso. Responder com código " + ProtocolBuildTree.nofound);
                this.lockTree.writeLock().lock();
                this.clientsBuildTree.get(resource).remove(origin);
                this.lockTree.writeLock().unlock();
            }
        }
        return res;
    }

    private void encapsulateAnswer(byte found, DataOutputStream dos) throws IOException {
        if(found == ProtocolBuildTree.loop)
            ProtocolBuildTree.encapsulateAnswerLoop(dos);
        else if(found == ProtocolBuildTree.found)
            ProtocolBuildTree.encapsulateAnswerFound(dos);
        else
            ProtocolBuildTree.encapsulateAnswerNoFound(dos);
    }
    public void respondeRequest(Socket socket){
        try {
            String ips = socket.getInetAddress().getHostAddress();
            Debug.printLigacaoSucesso(ips);
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            HelperConnection hc = ProtocolBuildTree.decapsulateAsk(dis);
            byte response = receiveRequest(hc);
            Debug.printTask("Byte de resposta a pedido de conteudo " + response);
            encapsulateAnswer(response,dos);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    private boolean contactNeighbourStartStreaming(InetSocketAddress neighbour, String resource) {
        boolean sucess = false;
        try {
            String ip = neighbour.getAddress().getHostAddress();
            Debug.printTask("A contactar vizinho " + ip  + " para o recurso " + resource);
            Socket socket = new Socket(neighbour.getAddress(), Cods.portStartStreaming);
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            ProtocolStartStreaming.encapsulate(resource,dos);
            socket.close();
            Debug.printTask("Streaming do recurso " + resource + " irá ser feito a partir do " + ip);
            sucess = true;
        } catch (IOException ignored) {}
        return sucess;

    }
    protected boolean startStreaming(Socket socket) {
        try {
            String ips = socket.getInetAddress().getHostAddress();
            Debug.printLigacaoSucesso(ips);
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            String resource = ProtocolStartStreaming.decapsulate(dis);
            socket.close();
            Debug.printTask("A adicionar cliente " + ips + " para streaming do conteudo " + resource);
            this.addClient(resource,new InetSocketAddress(socket.getInetAddress(),Cods.portStreamingContent));
            List<InetSocketAddress> bestneighbours = this.getBestNeighbours(resource);
            Debug.printTask("Vizinhos que a contactar :" + bestneighbours.stream().map(ip -> ip.getAddress().getHostAddress()).toList());
            boolean sucess = false;
            for(int i = 0; i < bestneighbours.size() && !sucess; i++)
                sucess = contactNeighbourStartStreaming(bestneighbours.get(i),resource);
            if(sucess)
                Debug.printTask("Encontrado vizinho para streaming para o recurso " + resource);
            else
                Debug.printError("Não encontrado vizinho para streaming para o recurso " + resource);
            return sucess;
        } catch (IOException  e) {
            throw new RuntimeException(e);
        }
    }

    public void serverRequest(ServerSocket serverSocket) {
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread t1 = new Thread(() -> respondeRequest(clientSocket));
                t1.start();
            }
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void serverStartStreaming(ServerSocket serverSocket) {
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread t1 = new Thread(() -> startStreaming(clientSocket));
                t1.start();
            }
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void initServer() throws IOException {
        Thread taux =  new Thread(() -> {try { serverRequest(new ServerSocket(Cods.portSOConnections));} catch (IOException e) {throw new RuntimeException(e);}});
        taux.start();
        serverStartStreaming(new ServerSocket(Cods.portStartStreaming));
    }
}
