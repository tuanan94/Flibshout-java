import java.io.*;
import java.util.Arrays;
import java.util.Comparator;

public class Controller {
    public static void main(String[] args) throws IOException, IOException {
        System.out.println("Hello");
        Libshout libshout = new Libshout();
        System.out.println(libshout.getVersion());
        Libshout icecast = new Libshout();
        icecast.setHost("YOUR HOST");
        icecast.setPort(8000);
        icecast.setProtocol(Libshout.PROTOCOL_HTTP);
        icecast.setPassword("YOUR PASSWORD");
        icecast.setMount("/MOUNT URL");
        icecast.setFormat(Libshout.FORMAT_MP3);
        icecast.open();
        File[] listFiles = new File("tmp/download").listFiles();
        Arrays.sort(listFiles, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.compare(f1.lastModified(), f2.lastModified());
            }
        });
        for (File f: listFiles) {
            String fileUrl = System.getProperty("user.dir") + "/" + f.getPath();
            System.out.println("\n Read file: " + fileUrl);
            send_output(fileUrl, icecast);
        }
        icecast.close();
    }


    public static void send_output(String fileUrl, Libshout icecast) throws IOException {
        byte[] buffer = new byte[1024];
        InputStream mp3 = new BufferedInputStream(new FileInputStream(new File(fileUrl)));
        int read = mp3.read(buffer);
        int i =0;
        while (read > 0) {
            i++;
            icecast.send(buffer, read);
            read = mp3.read(buffer);
            System.out.print(".");
        }
        mp3.close();
    }
}
