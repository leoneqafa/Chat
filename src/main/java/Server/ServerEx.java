package Server; //package di riferimento
import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;
public class ServerEx 
{
    private static ServerSocket serverSocket = null;
    private static final int MaxUtenti = 2; //imposto il limite degli utenti che possono connettersi
    private static final ThClient[] threads = new ThClient[MaxUtenti];
    public static void main(String args[]) 
    {
        int Porta = 8888; //inizializzo la porta di riferimento
        if (args.length == 1) 
        {
            Porta = Integer.valueOf(args[0]);
        }
        System.out.println(String.format("Esecuzione del server avvenuta con successo")); //avviso della connessione avvenuta
        try {
            serverSocket = new ServerSocket(Porta);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        while (true) //creazione socket per ogni client
        {
            try 
            {
                Socket clientSocket = serverSocket.accept();
                int j;
                for (j = 0; j < MaxUtenti; j++) 
                {
                    if (threads[j] == null) 
                    {
                        (threads[j] = new ThClient(clientSocket, threads)).start();
                        break;
                    }
                }
                if (j == MaxUtenti) 
                {
                    PrintStream output = new PrintStream(clientSocket.getOutputStream());
                    output.println("Server pieno. Riprova più tardi."); //nel momento in cui più di due client sono connessi impedisco la connessione ad altri client
                    output.close();
                    clientSocket.close();
                }
            } catch (IOException e) 
            {
                System.out.println(e.getMessage());
            }
        }
    }
}
class ThClient extends Thread //classe che gestisce input e output 
{
    private PrintStream output = null;
    private Socket clientSocket;
    private final ThClient[] threads;
    private int MaxUtenti;
    ThClient(Socket clientSocket, ThClient[] threads)
    {
        this.clientSocket = clientSocket;
        this.threads = threads;
        MaxUtenti = threads.length;
    }
    public void run()
    {
        int maxClientsCount = this.MaxUtenti;
        ThClient[] threads = this.threads;
        try 
        {
            BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintStream(clientSocket.getOutputStream());
            output.println(String.format("Inserisci il tuo nome:")); //avviene il salvataggio del nome
            String name = input.readLine().trim();
            output.println(String.format("Benvenuto nella chat room", name)); //avviso dell'entrata in chat dell'utente x
            output.println(String.format("Per uscire scrivi /quit in una nuova linea%n"));
            synchronized(this) //gestisce un flusso ordinato di eventuali richiesta
            {
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i] != this) {
                        threads[i].output.println(String.format("nuovo utente ", name));
                    }
                }
            }
            while (true) 
            {
                String line = input.readLine().trim(); //pongo fine al flusso nel momentio in cui viene inviato al server /quit
                if (line.startsWith("/quit"))
                {
                    break;
                }
                synchronized(this) 
                {
                    for (int i = 0; i < maxClientsCount; i++) {
                        if (threads[i] != null) {
                            threads[i].output.println(String.format("<%s> %s", name, line));
                        }
                        else{
                            threads[i].output.println(String.format("*** Non c'è nessun altro nella chat room ***"));
                        }
                    }
                }
            }
            synchronized(this) 
            {
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i] != this) {
                        threads[i].output.println(String.format("utente uscito", name)); //avviso di un uscita dal server di un utente
                    }
                }
            }
            output.println(String.format("Ciao ", name)); 
            synchronized(this)
            {
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] == this) {
                        threads[i] = null;
                    }
                }
            }
            input.close();
            output.close();
            clientSocket.close(); //chiusura finale e gestione eventuali errori
        } catch (IOException e) { 
            System.out.println("Errore");
        }
    }
}
