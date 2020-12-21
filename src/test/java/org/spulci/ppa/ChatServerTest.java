package org.spulci.ppa;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.spulci.ppa.client.ChatClient;

public class ChatServerTest 
{
    static Process server;
    static ChatClient client;

    @BeforeAll
    static void init() throws InterruptedException, IOException{
        server = ChatServer.start();
        Thread.sleep(2000);
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
        String respA = client.sendMessage("Questo è un msg di prova");
        assertEquals("Questo è un msg di prova", respA);
    }

}
