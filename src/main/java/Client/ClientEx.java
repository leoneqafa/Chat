package Client; //faccio riferimento al pacchetto da cui è costituito

import java.io.PrintStream; //importo eventuali opzioni aggiunrive
import java.io.BufferedReader; 
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientEx implements Runnable //implemento runnable
{
    private static Socket clientSocket = null;
    private static PrintStream output = null;
    private static BufferedReader input = null;
    private static BufferedReader inputLine = null;
    private static boolean closed = false;
    public static void main(String[] args) 
    {
        int porta = 8888; //porta di riferimento per la connessione
        String host = "hots";
        if (args.length == 2) {
            host = args[0];
            porta = Integer.valueOf(args[1]);
        }
        System.out.println(String.format("Connessione all'host %s nella porta %d", host, porta)); //avviso della connssione
        try 
        {
            clientSocket = new Socket(host, porta); //creazione effettiva del client
            inputLine = new BufferedReader(new InputStreamReader(System.in)); //variabile di riferimento per salvare imput
            output = new PrintStream(clientSocket.getOutputStream()); //variabile di riferimento per un eventuale output
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); //variabile per eventuale input
        } catch (UnknownHostException e) {
            System.err.println(String.format("Connessione non stabilibile")); //gestione errori
        } catch (IOException e) {
            System.err.println(String.format("connessione non riuscita", host)); //gestione errori
        }
        if (clientSocket != null && output != null && input != null) //avvenuta la connessione
        {
            try {
                new Thread(new ClientEx()).start(); //lettura dal server  e invio 
                while (!closed) 
                {
                    output.println(inputLine.readLine().trim());
                }
                output.close();
                input.close();
                clientSocket.close();
            } catch (IOException e) {
                System.err.println(String.format("IOException:  %s", e.getMessage()));
            }
        }
    }
    public void run() //thread lettura dal server
    {
        String responseLine;
        try 
        {
            while ((responseLine = input.readLine()) != null) {
                System.out.println(responseLine);
                if (responseLine.contains("*** Ciao")) { //la lettura avviene fino alla  lettura di ciao
                    break;
                }
            }
            closed = true;
        } catch (IOException e) 
        {
            System.err.println(String.format("IOException:  %s", e.getMessage()));
        }
    }
}