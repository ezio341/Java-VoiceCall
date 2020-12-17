/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package JFrame;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

/**
 *
 * @author ASUS
 */
public class ClientThread extends Thread{
    DatagramSocket ds;
    AudioInputStream InputStream;
    SourceDataLine sourceLine;
    Front view;
    private boolean isStopped = false;

    public ClientThread(DatagramSocket ds, AudioInputStream InputStream, SourceDataLine sourceLine, Front view) {
        this.ds = ds;
        this.InputStream = InputStream;
        this.sourceLine = sourceLine;
        this.view = view;
    }
    
    @Override
    public void run(){
        try {
            receivePacket();
        } catch (IOException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void receivePacket() throws IOException{
        byte[] receiveData = new byte[10000];
        while (true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            ds.receive(receivePacket);
            view.getjTextArea1().append("RECEIVED: " + receivePacket.getAddress().getHostAddress() + " " + receivePacket.getPort()+"\n");
//            System.out.println("RECEIVED: " + receivePacket.getAddress().getHostAddress() + " " + receivePacket.getPort());
            try {
                byte audioData[] = receivePacket.getData();
                InputStream byteInputStream = new ByteArrayInputStream(audioData);
                AudioFormat adFormat = getAudioFormat();
                InputStream = new AudioInputStream(byteInputStream, adFormat, audioData.length / adFormat.getFrameSize());
                DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, adFormat);
                sourceLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
                sourceLine.open(adFormat);
                sourceLine.start();
                ExecutorService ex = Executors.newFixedThreadPool(10);
                ex.submit(new ShareThread());
            } catch (Exception e) {
                System.out.println(e);
                System.exit(0);
            }
        }
    }
    class ShareThread implements Runnable{

        byte tempBuffer[] = new byte[10000];

        public void run() {
            try {
                int cnt;
                while ((cnt = InputStream.read(tempBuffer, 0, tempBuffer.length)) != -1 && !isStopped) {
                    if (cnt > 0) {
                       sourceLine.write(tempBuffer, 0, cnt);
                    }
                }
                //                sourceLine.drain();
                //             sourceLine.close();
            } catch (Exception e) {
                System.out.println(e);
                System.exit(0);
            }
        }
    }
    private AudioFormat getAudioFormat() {
        float sampleRate = 16000.0F;
        int sampleInbits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate, sampleInbits, channels, signed, bigEndian);
    }
    public void setIsStopped(boolean isStopped) {
        this.isStopped = isStopped;
    }
}
