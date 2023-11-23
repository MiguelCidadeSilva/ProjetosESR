package Nodes.Classes;

import Nodes.Utils.Cods;
import Nodes.Utils.Debug;
import Protocols.Helper.HelperConnection;
import Protocols.ProtocolBuildTree;
import Protocols.ProtocolEndStreaming;
import Protocols.ProtocolStartStreaming;
import Protocols.ProtocolTransferContent;

import java.io.*;
import java.net.*;

public class Client {
    private final InetAddress ipclient;
    private final InetAddress ipneighbour;
    private final String resource;

    public Client(InetAddress ipclient, InetAddress ipneighbour, String resource) {
        this.ipclient = ipclient;
        this.ipneighbour = ipneighbour;
        this.resource = resource;
    }

    public void start() throws IOException {
        Debug.printTask("Inicio de criação da árvore.");
        byte tree = requestTree(resource);
        switch (tree) {
            case 0: //loop
                Debug.printTask("Erro, a rede entrou em loop.");
                break;
            case 1: //found
                Debug.printTask("Recurso encontrado");
                //startStreaming
                Debug.printTask("Iniciação do Streaming");
                startStreaming(resource);
                Debug.printTask("Streaming");
                getContent();
                Debug.printTask("Fim de Streaming");
                break;
            case 2: //not found
                Debug.printTask("Recurso não encontrado");
                break;
        }
    }

    private byte requestTree(String resource){
        Debug.printTask("Criação do socket TCP");
        try (
                Socket socket = new Socket(ipneighbour, Cods.portSOConnections);
                OutputStream outputStream = socket.getOutputStream();
                DataOutputStream dos = new DataOutputStream(outputStream);
                InputStream inputStream = socket.getInputStream();
                DataInputStream dis = new DataInputStream(inputStream)
        ) {
            Debug.printTask("Encapsulamento dos dados a enviar.");
            ProtocolBuildTree.encapsulateAsk(ipclient, resource, dos);
            Debug.printTask("Desencapsulamento da resposta.");
            // Receive the response from the server
            byte res = ProtocolBuildTree.decapsulateAnswer(dis);
            Debug.printTask("Resposta desencapsulada");
            return res;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    protected void endStreaming(){
        Debug.printTask("Criação do socket TCP para terminar streaming");
        try (
                Socket socket = new Socket(ipneighbour, Cods.portEndStreaming);
                OutputStream outputStream = socket.getOutputStream();
                DataOutputStream dos = new DataOutputStream(outputStream);
        ) {
            Debug.printTask("Encapsulamento dos dados.");
            ProtocolEndStreaming.encapsulate(new HelperConnection(resource,ipclient),dos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void startStreaming(String resource){
        Debug.printTask("Criação do socket TCP");
        try (
                Socket socket = new Socket(ipneighbour, Cods.portStartStreaming);
                OutputStream outputStream = socket.getOutputStream();
                DataOutputStream dos = new DataOutputStream(outputStream);
        ) {
            Debug.printTask("Encapsulamento dos dados.");
            ProtocolStartStreaming.encapsulate(resource,dos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void getContent(){
        Player p = new Player(this);
        Buffer b = new Buffer(p);
        Debug.printTask("Criação do socket UDP");
        try (DatagramSocket socket = new DatagramSocket(Cods.portStreamingContent))
        {
            byte[] buffer = new byte[Cods.packetSize+10000];
            Debug.printTask("Receção de pacotes UDP.");
	        boolean streaming = true;
            while (streaming) {
                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(receivePacket);
                StreamingPacket packet = ProtocolTransferContent.decapsulate(receivePacket);
                Debug.printTask("Pacote de streaming recebido" + packet);
                b.receive(packet);
                streaming = packet.getType() != Cods.codEndStream;
	        }
            Debug.printTask("Fim de streaming");
            System.exit(0);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
