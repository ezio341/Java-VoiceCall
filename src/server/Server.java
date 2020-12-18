/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.TargetDataLine;

/**
 *
 * @author WE
 */
public class Server {
    ByteArrayOutputStream byteOutputStream;
    AudioFormat adFormat;
    TargetDataLine targetDataLine;
    AudioInputStream InputStream;
    DatagramSocket serverSocket;

    private AudioFormat getAudioFormat() {
        //Audio Format Configuration
        float sampleRate = 16000.0F;
        int sampleInbits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate, sampleInbits, channels, signed, bigEndian);
    }

    public static void main(String args[]) {
        new Server().runVOIP();
    }

    public void runVOIP() {
        try {
            //set server datagam socket
            serverSocket = new DatagramSocket(900);
            //data container from any client
            byte[] receiveData = new byte[10000];
            while (true) {
                //packet receiver to set data into container (receivedata)
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                //set datagram socket receiver
                serverSocket.receive(receivePacket);
                System.out.println("RECEIVED: " + receivePacket.getAddress().getHostAddress() + " " + receivePacket.getPort());
                byte audioData[] = receivePacket.getData();
                InputStream byteInputStream = new ByteArrayInputStream(audioData);
                AudioFormat format = getAudioFormat();
                InputStream = new AudioInputStream(byteInputStream, format, audioData.length / format.getFrameSize());
                Thread playThread = new Thread(new PlayThread(receivePacket.getAddress(), receivePacket.getPort()));
                playThread.start();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    class PlayThread extends Thread {

        byte tempBuffer[] = new byte[10000];
        InetAddress ip;
        int port;

        public PlayThread() {
        }

        public PlayThread(InetAddress ip, int port) {
            this.ip = ip;
            this.port = port;
        }
        
        @Override
        public void run() {
            try {
                int cnt;
                while ((cnt = InputStream.read(tempBuffer, 0, tempBuffer.length)) != -1) {
                    if (cnt > 0) { 
//                        sourceLine.write(tempBuffer, 0, cnt);
                        DatagramPacket dp = new DatagramPacket(tempBuffer, cnt, ip, port);
                        serverSocket.send(dp);
                    }
                }
                //  sourceLine.drain();
                // sourceLine.close();
            } catch (IOException e) {
                System.out.println(e);
                System.exit(0);
            }
        }
        
    }
    
}
