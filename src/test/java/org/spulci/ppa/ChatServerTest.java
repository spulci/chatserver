package org.spulci.ppa;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class ChatServerTest 
{

    static Process server;
    static ChatClient client;

    @BeforeAll
    static void init() throws InterruptedException, IOException{
        server = ChatServer.start();
        server.waitFor();
        client = new ChatClient();
    }

    @AfterAll
    static void stopServer() throws IOException{
        client.stop();
        server.destroy();
    }

    @Test
    @DisplayName("Processo server startato")
    void whenServerStarted(){
        assertNotNull(server);
    }

    @Test
    @DisplayName("Test connessione client")
    void whenOneNewClientConnected(){
        //Send an empty message to skip the server welcome message
        client.sendMessage("");
        String respA = client.sendMessage("Questo è un msg di prova");
        assertEquals("Questo è un msg di prova", respA);
    }

}
