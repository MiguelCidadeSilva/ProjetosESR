package Nodes.Utils;

import Protocols.ProtocolLoadContent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrganizeIps {

    public static List<InetAddress> organizeIps(List<InetAddress> ips)  {
        Map<String,Long[]> times = new HashMap<>();
        List<Thread> threads = new ArrayList<>();
        for (InetAddress ip : ips) {
            Long[] timesaux = new Long[2];
            times.put(ip.getHostAddress(), timesaux);
            Thread t = getThread(ip.getHostAddress(), times);
            t.start();
            threads.add(t);
        }
        for(Thread t : threads) {
            try { t.join();} catch (InterruptedException ignored) {System.out.println("seila");}
        }
        Map<String,Long> differenceTimes = new HashMap<>();
        times.forEach((ip,array) -> differenceTimes.put(ip,array[1]-array[0]));
        return ips.stream().sorted((s1,s2) -> differenceTimes.get(s2.getHostAddress()) - differenceTimes.get(s1.getHostAddress()) > 0 ? 1 : -1).toList();
    }

    private static Thread getThread(String ip, Map<String, Long[]> times) {
        return new Thread(() -> {
            times.get(ip)[0] = System.currentTimeMillis();
            try {
                Socket socket = new Socket(ip, Cods.portDB);
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                ProtocolLoadContent.encapsulateConnection(dos,true, ip);
                boolean b = ProtocolLoadContent.decapsulateConnection(dis);
                times.get(ip)[1] = System.currentTimeMillis();
                socket.close();
            } catch (IOException e) {
               System.out.println("Erro ao establecer ligação com o vizinho " + ip);
               times.get(ip)[1] = Long.MAX_VALUE;
            }
        });
    }

}
