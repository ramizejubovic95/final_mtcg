package sockethandler;

import Requests.ReqHandler;
import Requests.Request;
import response.ResponseHandler;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;

public class SocketHandler extends Thread
{
    private final Socket clientConnection;
    private final ResponseHandler responseHandler;
    private ReqHandler reqHandler = null;
    // ServerSocket serverSocket = new ServerSocket(10001);
    Request request;
    int counter;
    private BufferedReader in = null;
    private BufferedWriter out = null;


    public SocketHandler(Socket clientConnection) throws IOException {
        this.clientConnection = clientConnection;
        this.in = new BufferedReader(new InputStreamReader(this.clientConnection.getInputStream()));
        this.out = new BufferedWriter(new OutputStreamWriter(this.clientConnection.getOutputStream()));
        this.responseHandler = new ResponseHandler(this.out);
        this.reqHandler = new ReqHandler(this.responseHandler);
        this.request = new Request();
    }

    @Override
    public void run() {

        // while(true)
        // {
        /* Socket clientSocket = null;
        try {
            clientSocket = serverSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
        } */
        try
        {
            System.out.println("Connection created to Client\n");

            String input = "";
            while (this.in.ready())
            {
                input = this.in.readLine();

                if (counter == 0) {
                    String[] splittedInput = input.split(" ");
                    this.request.setMethod(splittedInput[0]);
                    this.request.setRoute(splittedInput[1]);
                }
                if (input.contains("Authorization")) {
                    String[] splittedInput = input.split(" ");
                    this.request.setAuthorization(splittedInput[2]);
                }
                if (input.contains("Content-Length")) {
                    String[] splittedInput = input.split(" ");
                    try
                    {
                        this.request.setContentLength(Integer.parseInt(splittedInput[1]));
                    }
                    catch (NumberFormatException e)
                    {
                        System.out.println("Number REQUEST DID NOT WORK");
                        continue;
                    }
                    int cLength = this.request.getContentLength();
                    if (cLength > 0) {
                        char[] cBuff = new char[cLength + 2];
                        try {
                            this.in.read(cBuff, 0, cLength + 2);
                        } catch (IOException e) {
                            e.printStackTrace();
                            break;
                        }
                        this.request.setContent(new String(cBuff));
                        break;
                    }
                }
                this.counter++;
            }

            this.counter = 0;

            try
            {
                // this.request.print();
                this.reqHandler.handle(this.request);
                return;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                System.out.println("REQUEST DID NOT WORK");
            }

            // }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                this.in.close();
                this.out.close();
                // System.out.println("CONNECTION CLOSED???");
                this.clientConnection.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
