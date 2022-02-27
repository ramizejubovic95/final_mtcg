import Requests.ReqHandler;
import Requests.Request;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import sockethandler.SocketHandler;

public class Main
{

    public static void main(String args[]) throws IOException
    {
        final ExecutorService executorService = Executors.newFixedThreadPool(20);

        try (ServerSocket AnotherServerSocket = new ServerSocket(10001, 10))
        {
            while (true)
            {
                final Socket clientConnection = AnotherServerSocket.accept();

                final SocketHandler socketHandler = new SocketHandler(clientConnection);
                executorService.submit(socketHandler);
            }
        }

    }
}

