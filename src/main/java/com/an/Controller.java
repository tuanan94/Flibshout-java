package com.an;

import com.gmail.kunicins.olegs.libshout.Libshout;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;

import static java.lang.Thread.sleep;

public class Controller {
    private static String mp3TankFolder = null;
    private static String hostUrl = "fradio.site";
    private static int port = 8000;
    private static String password = null;
    private static String mountUrl = "/abc";
    private static Libshout icecast = null;
    static ArrayList<File> files = new ArrayList<File>();


    public static void main(String[] args) throws IOException, IOException {
        setInput();
        initIcecastConnection();
        CollectThread collectThread = new CollectThread();
        collectThread.start();
        while (true){
            if (files.isEmpty()){
                System.out.println("files list is empty. Wait...");
                try {
                    sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            File f = files.get(0);
            send_output(f, icecast);
        }
       // icecast.close();
    }
    private static class CollectThread extends Thread{
        @Override
        public void run() {
            while (true){
                boolean isCollected = false;
                File[] listFiles = new File(mp3TankFolder).listFiles();
                if (listFiles == null || listFiles.length == 0 ){
                    System.out.println("[CollectThread] mp3tank empty");
                    try {
                        sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                for (File f: listFiles) {
                    if (files.contains(f)){
                        continue;
                    }
                    files.add(f);
                    isCollected = true;
                    System.out.println("[CollectThread] Add file: " + f);
                }
                if (!isCollected){
                    System.out.println("[CollectThread] no new file. Current size = " + files.size());
                }
                try {
                    sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }


    private static void setInput(){
        Scanner sc = new Scanner(System.in);
        System.out.println("-----[SETUP]-----");
        System.out.print("Mp3 tank folder: ");
        mp3TankFolder = sc.nextLine();
        System.out.print("Mount password: ");
        sc.reset(); // Clear buffer
        password = sc.nextLine();
        String cInfo = String.format("Configuration information: %s - %s - %s - %s - %s"
                , mp3TankFolder, hostUrl, port, password, mountUrl);
        System.out.println(cInfo);
    }

    private static void initIcecastConnection(){
        try{
            icecast = new Libshout();
            icecast.setHost(hostUrl);
            icecast.setPort(port);
            icecast.setProtocol(Libshout.PROTOCOL_HTTP);
            icecast.setPassword(password);
            icecast.setMount(mountUrl);
            icecast.setFormat(Libshout.FORMAT_MP3);
            icecast.open();
        } catch (IOException e){
            System.out.println("[ERROR] - icecast int connection" + e);
        }
    }


    private static void send_output(File f, Libshout icecast) throws IOException {
        System.out.println("[Steam] [Start] f = " + f);
        byte[] buffer = new byte[1024];
        InputStream mp3 = new BufferedInputStream(new FileInputStream(f));
        int read = mp3.read(buffer);
        int count = 0;
        while (read > 0) {
            count ++;
            icecast.send(buffer, read);
            read = mp3.read(buffer);
            if (count % 50 == 0){
                System.out.println("[Stream] [Processing] f = " + f + " ." + count);
            }
        }
        mp3.close();
        System.out.println("[Steam] [End] f = " + f);
    }
}
