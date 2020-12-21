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
public class ChatServer {
    
    private AsynchronousServerSocketChannel listener;
    private final ArrayList<AsynchronousSocketChannel> asynchSocketChannelList = new ArrayList<>();
    public ChatServer()
    {
        try
        {   
            //AsynchronousChannelGroup group = AsynchronousChannelGroup.withThreadPool(Executors.newCachedThreadPool());
           
            listener = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress("localhost",10000));
            log.info("Server started on port 10000");
            
            // Listen for a new request
            listener.accept( null, new CompletionHandler<AsynchronousSocketChannel,Void>() {

                @Override
                public void completed(AsynchronousSocketChannel ch, Void att){

                    synchronized(asynchSocketChannelList){
                        asynchSocketChannelList.add(ch);
                    }

                    // Accept the next connection
                    listener.accept( null, this );

                    // Allocate a byte buffer (4K) to read from the client
                    ByteBuffer byteBuffer = ByteBuffer.allocate( 4096 );
                    try
                    {
                        // Read the first line
                        int bytesRead = ch.read( byteBuffer ).get(30, TimeUnit.SECONDS);

                        boolean isRunning = true;
                        while( bytesRead != -1 && isRunning ){   

                            // Make sure that we have data to read
                            if( byteBuffer.position() > 2 ){
                                // Make the buffer ready to read
                                byteBuffer.flip();

                                // Convert the buffer into a line
                                byte[] lineBytes = new byte[ bytesRead ];
                                byteBuffer.get( lineBytes, 0, bytesRead );
                                String line = new String( lineBytes );

                                // Broadcast to all clients
                                asynchSocketChannelList.stream().forEach(currentChannel ->
                                {
                                    currentChannel.write(ByteBuffer.wrap( line.getBytes() ));
                                });

                                log.info("Inviato in broadcast: " + line);

                                // Make the buffer ready to write
                                byteBuffer.clear();

                                // Read the next line
                                bytesRead = ch.read( byteBuffer ).get(30, TimeUnit.SECONDS);
                            }
                            else
                            {
                                // An empty line signifies the end of the conversation in our protocol
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
                        // The user exceeded the 20 second timeout, so close the connection
                        log.info("Closing current connection due to reached timeout");
                        ch.write(ByteBuffer.wrap( "Good Bye\n".getBytes()));
                    }

                    log.info("Ending conversation with a client");
                    try{
                        // Close the connection if we need to
                        if( ch.isOpen() ){   
                            synchronized(asynchSocketChannelList){
                                asynchSocketChannelList.remove(ch);
                            }

                            ch.close();
                        }
                    }
                    catch (IOException e1){
                        log.severe("IOException");
                    }
                }

                @Override
                public void failed(Throwable exc, Void att){
                    ///...
                }
            });
        }
        catch (IOException e){
            log.log(Level.SEVERE, "Having some IO issues...exiting" , e);
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
            log.log(Level.SEVERE , "Server can't start", e);
        }
        
    }

}
