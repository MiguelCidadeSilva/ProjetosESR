package Nodes.Utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class VideoExtractor {
    private final String video;
    private Queue<byte[]> frames;
    private Queue<byte[]> audio;
    private int frameRate;
    private final boolean repeat;

    private final int segundosAudio = 1;
    private Queue<byte[]> readBytesDirectory(String dir) throws IOException {
        File directory = new File(dir);
        File[] files = directory.listFiles();
        Queue<byte []> list = new LinkedList<>();
        if(files != null)
        {
            Arrays.sort(files, Comparator.comparing(File::getName));
            for (File file : files) {
                FileInputStream fis = new FileInputStream(file);
                list.add(fis.readAllBytes());
                fis.close();
            }
        }
        return list;
    }
    private void runProcess(String command) throws InterruptedException, IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
        Process process = processBuilder.start();
        process.waitFor();
        process.destroy();
    }
    private void createDirs(String videoFolder, String audioFolder) throws IOException {
        Files.createDirectory(Path.of(videoFolder));
        Files.createDirectory(Path.of(audioFolder));
    }
    private void extractFrames(String videoFolder,String file) throws IOException, InterruptedException {
        System.out.println("A extrair vídeo");
        String command = "ffmpeg -i " + file + " -vsync vfr -q:v 2 " + videoFolder +"/frame-%04d.png";
        runProcess(command);
        frames = readBytesDirectory(videoFolder);
        System.out.println("Vídeo extraido");
    }
    private void extractAudio(String audioFolder,String file) throws IOException, InterruptedException {
        System.out.println("A extrair audio");
        String command = "ffmpeg -i " + file + " -vn -c:a pcm_s16le -f segment -segment_time " + segundosAudio +" " + audioFolder + "/audio%03d.wav";
        runProcess(command);
        audio = readBytesDirectory(audioFolder);
        System.out.println("Audio extraido");
    }
    private void extractFrameRate(String file) throws IOException, InterruptedException {
        System.out.println("A extrair frame rate");
        String command = "ffprobe -v 0 -of csv=p=0 -select_streams v:0 -show_entries stream=r_frame_rate " + file;
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String[] numbers = reader.readLine().split("/");
        process.waitFor();
        process.destroy();
        reader.close();
        this.frameRate = Integer.parseInt(numbers[0]) / Integer.parseInt(numbers[1]);
    }
    private void clean(String videoFolder, String audioFolder) throws IOException, InterruptedException {
        String command1 = "rm -r " + videoFolder;
        String command2 = "rm -r " + audioFolder;
        runProcess(command1);
        runProcess(command2);
    }
    public VideoExtractor(String resource, String file, boolean repeat) throws IOException, InterruptedException {
        this.video = resource;
        this.repeat = repeat;
        String videoFolder = "../Content/video" + resource;
        String audioFolder = "../Content/audio" + resource;
        createDirs(videoFolder,audioFolder);
        Thread t1 = new Thread(() -> { try {extractFrames(videoFolder,file);} catch (IOException | InterruptedException ignored) {}});
        Thread t2 = new Thread(() -> { try {extractAudio(audioFolder,file);} catch (IOException | InterruptedException ignored) {}});
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        clean(videoFolder,audioFolder);
        extractFrameRate(file);
        Debug.printTask("Video " + resource + " extraido com sucesso. Número de frames=" + this.frames.size() + ". Número de pedaços de audio com " + this.segundosAudio+ " segundos " + this.audio.size());
    }
    public int framesLeft() {return this.frames.size();}
    public int audioLeft() {return this.audio.size();}
    public byte[] nextAudio() {
        byte[] res = this.audio.poll();
        if(this.repeat)
            this.audio.add(res);
        return res;
    }

    public byte[] nextFrame() {
        byte[] res = this.frames.poll();
        if(this.repeat)
            this.frames.add(res);
        return res;
    }

    public boolean hasAudio() {
        return !this.audio.isEmpty();
    }

    public boolean hasFrames() {
        return !this.frames.isEmpty();
    }

    public String getVideo() {
        return video;
    }

    public int getFrameRate() {
        return frameRate;
    }

    public static List<byte[]> splitByteArray(byte[] data, int chunkSize) {
        List<byte[]> chunks = new ArrayList<>();
        for (int i = 0; i < data.length; i += chunkSize) {
            int remainingBytes = Math.min(chunkSize, data.length - i);
            byte[] chunk = new byte[remainingBytes];
            System.arraycopy(data, i, chunk, 0, remainingBytes);
            chunks.add(chunk);
        }
        return chunks;
    }
    public static byte[] concatenateChunks(List<byte[]> chunks) {
        int totalLength = chunks.stream().mapToInt(chunk -> chunk.length).sum();
        byte[] result = new byte[totalLength];

        int position = 0;
        for (byte[] chunk : chunks) {
            System.arraycopy(chunk, 0, result, position, chunk.length);
            position += chunk.length;
        }

        return result;
    }
}
