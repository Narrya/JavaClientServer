package serwer;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 *
 * @author jk
 */

public class Serwer implements Runnable {

    ServerSocket gniazdoNasluchujace = null;

    private final static int DEFAULT_PORT = 10101;
    //private final static int DEFAULT_TIMEOUT = 100; // ms

    // konstruktory klasy
    public Serwer() throws IOException
    {
        this(DEFAULT_PORT);
    }

    public Serwer(int port)  throws IOException
    {
        // utworzenie serwera == utworzeniem gniazda, ale 
        // bez wejścia w procedurę akceptacji -> to jest
        // dopiero w metodzie run() wywoływanej w trakcie
        // uruchomienia wątku
        gniazdoNasluchujace = new ServerSocket(port);
        setSocketParameters();
    }

    public Serwer(InetAddress ia, int port) throws IOException
    {
        // można też podać konkretny adres
        gniazdoNasluchujace = new ServerSocket(port, 0, ia);
        setSocketParameters();
    }

    private void setSocketParameters() throws IOException
    {
        // sposób na szybkie, powtórne użycie portu/adresu po poprzednim programie
        //gniazdoNasluchujace.setSoTimeout(DEFAULT_TIMEOUT);
        gniazdoNasluchujace.setReuseAddress(true);
    }

    
    public void run() {
        // serwer może tworzyć wiele wątków (tyle ile połączeń), więc
        // wykorzystana zostanie klasa Executors, która efektywnie obsługuje
        // taki model
        Executor watki = Executors.newCachedThreadPool();

        // tutaj tak samo, jak w serwerze jednowątkowym
        while (true)
        {
            try
            {
                Socket gniazdoPolaczenia = gniazdoNasluchujace.accept();
                // asystent to klasa prywatna; też ma metodę run()
                // dostaje na dzień dobry działające gniazdo połączenia, 
                // a serwer wraca do przyjmowania połączeń
                watki.execute(new Asystent(gniazdoPolaczenia));
            }
            catch (IOException ioe)
            {
                System.out.println("Błąd połączenia: " + ioe.getLocalizedMessage());
                continue;
            }
        }
    }


    private class Asystent implements Runnable
    {
        Socket gniazdoPolaczenia;

        DataOutputStream daneWysylane;
        BufferedReader daneOdbierane;

        Asystent(Socket s)
        {
            gniazdoPolaczenia = s;
        }
        

        // do wykonania w nowym wątku
        public void run()
        {

            try
            {
                daneOdbierane = new BufferedReader(new InputStreamReader(
                        gniazdoPolaczenia.getInputStream()
                        ));

                daneWysylane = new DataOutputStream(
                        gniazdoPolaczenia.getOutputStream()
                        );
            }
            catch (IOException e)
            {
                System.out.println("Nie udało się utworzyć strumieni" + e.getMessage());
                try
                {
                    if (daneOdbierane != null)
                        daneOdbierane.close();
                    if (daneWysylane != null)
                        daneWysylane.close();
                    gniazdoPolaczenia.close();
                }
                catch (Exception ee)
                {

                }
            }


            // strumienie są
            //String poprzedniaLinia = "\n";
            String daneWe = "";
            String daneWy = "";
            
            int liczbaLinii = -1;

            try
            {
                daneWe = daneOdbierane.readLine();
                liczbaLinii = Integer.parseInt(daneWe);
                
                int i = 0;
                while (liczbaLinii-- > 0 && (daneWe = daneOdbierane.readLine()) != null)
                {
                    i++;
                    System.out.println("Odebralem od klienta: "
                            + gniazdoPolaczenia.getInetAddress().getCanonicalHostName()
                            + " następujące dane: " + daneWe);
                    //if (daneWe.equals("") && poprzedniaLinia.equals(daneWe))
                    //     break;
                    
                    daneWy = String.format("odebrano linie %d (zostalo: %d): %s:", i, liczbaLinii, daneWe);
                    daneWysylane.writeBytes(daneWy);
                    //poprzedniaLinia = daneWe;
                }

                System.out.println(daneWy);
            }
            catch (NumberFormatException e) // tylko w przypadku 
            {
                String msg = "Pierwsza linijka powinna zawierac liczbe linii w calej wiadomosci (a zawierala: " + daneWe + ")";
                try
                {
                    daneWysylane.write(msg.getBytes());
                }
                catch (IOException e1)
                {
                    System.out.println("błąd odczytu/zapisu: " + e.getMessage());
                }
                        
            }
            catch (IOException e)
            {
                System.out.println("błąd odczytu/zapisu: " + e.getMessage());
            }


            try
            {
                // tym razem poprawnie zamknięte zostaną strumienie
                daneOdbierane.close();
                daneWysylane.close();
                gniazdoPolaczenia.close();
            }
            catch (IOException e)
            {
                    System.out.println("zamknięcia gniazda: " + e.getMessage());
            }
            


        }

    }

}
