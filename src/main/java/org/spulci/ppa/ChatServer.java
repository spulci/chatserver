package org.spulci.ppa;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;

@Log
public class ChatServer{
    
    private AsynchronousServerSocketChannel listener;
    private final ArrayList<AsynchronousSocketChannel> asynchSocketChannelList = new ArrayList<>();
    public ChatServer(){
        try{   
            listener = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress("localhost",10000));
            log.info("Server started on port 10000");
            listener.accept( null, new CompletionHandler<AsynchronousSocketChannel,Void>() {

                @Override
                public void completed(AsynchronousSocketChannel ch, Void att){

                    synchronized(asynchSocketChannelList){
                        asynchSocketChannelList.add(ch);
                    }

                    listener.accept( null, this );

                    ByteBuffer byteBuffer = ByteBuffer.allocate(4096);
                    try
                    {

                        int bytesRead = ch.read( byteBuffer ).get(30, TimeUnit.SECONDS);

                        boolean isRunning = true;
                        while( bytesRead != -1 && isRunning ){   

                            if( byteBuffer.position() > 2 ){

                                byteBuffer.flip();

                                byte[] lineBytes = new byte[ bytesRead ];
                                byteBuffer.get( lineBytes, 0, bytesRead );
                                String line = new String( lineBytes );

                                asynchSocketChannelList.stream().forEach(currentChannel ->
                                {
                                    currentChannel.write(ByteBuffer.wrap( line.getBytes() ));
                                });

                                log.info("Inviato in broadcast: " + line);

                                byteBuffer.clear();

                                bytesRead = ch.read(byteBuffer).get(30, TimeUnit.SECONDS);
                            }
                            else{
                                //Empty line
                                isRunning = false;
                            }
                        }
                    }
                    catch (InterruptedException e){
                        log.log(Level.WARNING,"Interruption received!",e);
                    }
                    catch (ExecutionException e){   
                        log.log(Level.SEVERE, "Execution exception raised", e);
                    }
                    catch (TimeoutException e){
                        log.info("Closing current connection due to reached timeout");
                        ch.write(ByteBuffer.wrap( "Good Bye\n".getBytes()));
                    }

                    log.info("Ending conversation with a client");
                    try{
                        if(ch.isOpen()){   
                            synchronized(asynchSocketChannelList){
                                asynchSocketChannelList.remove(ch);
                            }
                            ch.close();
                        }
                    }
                    catch (IOException e1){
                        log.log(Level.SEVERE, "IOException while closing a connection!!", e1);
                    }
                }

                @Override
                public void failed(Throwable exc, Void att){
                    log.log(Level.SEVERE, "Operation failed! Client connection not accepted!", exc);
                }
            });
        }
        catch (IOException e){
            log.log(Level.SEVERE, "Having some IO issues...exiting", e);
            System.exit(0);
        }
    }

    public static Process start() throws IOException{
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String className = ChatServer.class.getCanonicalName();

        ProcessBuilder builder = new ProcessBuilder(javaBin, "-cp", classpath, className);
        return builder.start();
    }

    public static void main(String args[]){
        try{
            ChatServer server = new ChatServer();
            Thread.currentThread().join();
        }
        catch(Exception e){
            log.log(Level.SEVERE , "Server can't start!", e);
        }
        
    }

}
