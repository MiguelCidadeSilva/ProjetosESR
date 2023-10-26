package Nodes.Utils;

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
}
