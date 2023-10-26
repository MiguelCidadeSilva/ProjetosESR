package Nodes.Classes;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ServerNode {
    private final Map<String, List<InetSocketAddress>> clientsResourceMap;
    private final Map<String, ReadWriteLock> resourceLocks;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private void lockhelper(String resource){
        lock.readLock().lock();
        resourceLocks.get(resource).readLock().lock();
    }

    private void unlockhelper(String resource){
        lock.readLock().unlock();
        resourceLocks.get(resource).readLock().unlock();
    }

    public ServerNode() {
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


    public void clearResources(){
        lock.writeLock().lock();
        for (String resource : this.clientsResourceMap.keySet()) {
            this.resourceLocks.get(resource).writeLock().lock();
            boolean aux =this.clientsResourceMap.get(resource).isEmpty();
            if(aux) this.clientsResourceMap.remove(resource);
            this.resourceLocks.get(resource).writeLock().unlock();
            if(aux) this.resourceLocks.remove(resource);
        }
        lock.writeLock().unlock();
    }


    // Método que recebo frames / partes de audio / partes do texto
    public void receiveResources() {}
    // Método que replica o conteudo recebido por todos os clientes
    public void sendResources() {}
}
