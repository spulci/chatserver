package org.spulci.ppa.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;

import lombok.extern.java.Log;

@Log
public class ChatClient {
    private AsynchronousSocketChannel client;
    private Future<Void> future;

    public ChatClient() {
        try {
            client = AsynchronousSocketChannel.open();
            InetSocketAddress hostAddress = new InetSocketAddress("localhost", 10000);
            future = client.connect(hostAddress);
            start();

        } catch (IOException e) {
            log.log(Level.SEVERE, "AsynchSocket issue on opening", e);
        }
    }

    private void start() {
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            log.log(Level.SEVERE, "Exception while connecting to remote host", e);
        }
    }

    public String sendMessage(String message) {
        byte[] byteMsg = message.getBytes();
        ByteBuffer buffer = ByteBuffer.wrap(byteMsg);
        Future<Integer> writeResult = client.write(buffer);
        
        try {
            writeResult.get();
        } catch (Exception e) {
            log.log(Level.SEVERE, "Issue writing msg", e);
        }
        buffer.flip();
        Future<Integer> readResult = client.read(buffer);
        try {
            readResult.get();
        } catch (Exception e) {
            log.log(Level.SEVERE, "Issue reading msg", e);
        }
        String echo = new String(buffer.array()).trim();
        buffer.clear();
        return echo;
    }
    public void stop() {
        try {
            client.close();
        } catch (IOException e) {
            log.log(Level.SEVERE, "Issue closing channel", e);
        }
    }

    public static void main(String[] args) throws Exception {
        ChatClient client = new ChatClient();
        client.start();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line;
        log.info("Message to server:");
        while ((line = br.readLine()) != null) {
            String response = client.sendMessage(line);
            log.finest("response from server: " + response);
            log.finest("Message to server:");
        }
    }
}
