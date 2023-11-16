package Nodes.Utils;

// Ficheiro que contem as portas
public class Cods {
    // Porta que o servidor com a BD responde
    public static final int portDB = 8001;
    public static final int portSOConnections = 8002;
    public static final int portStartStreaming = 8003;
    public static final int portStreamingContent = 8004;

    public static final int codAudio = 0;
    public static final int codVideo = 1;
    public static final int codEndStream = 2;
    public static final int codNoExist = 3;

    public static final int packetSize = 20000;
}
