package klient;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 *
 * @author jk
 */
public class KlientMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        // zajmie się wykonywaniem wątku serwera; pojedynczego
        try
        {
            Socket gniazdoPolaczenia = new Socket("127.0.0.1", 10101);
            DataOutputStream daneWysylane = null;
            BufferedReader daneOdbierane = null;        
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

            String daneWe = "";                     
            int ileLinii = -1;
            
            // Odbieram pierwszy komunikat.
            daneWe = daneOdbierane.readLine();
            System.out.println(daneWe);
            
            // Linia testowa ;)
            daneWysylane.writeBytes("TestLine...\r\n");
            daneWe = daneOdbierane.readLine();
            System.out.println(daneWe);
            
            // Wysyłamy ilość linii.            
            System.out.println("Podaj ilosc linii:");
            InputStreamReader wejscie = new InputStreamReader(System.in);
            BufferedReader bufWe = new BufferedReader(wejscie);            
            
            ileLinii = Integer.parseInt(bufWe.readLine());            
            daneWysylane.writeBytes(ileLinii + "\r\n");
            daneWe = daneOdbierane.readLine();
            System.out.println(daneWe);           
            
            // I dokładnie tyle linii ile trzeba...
            for(int i = 0; i < ileLinii; ++i) {                          
                System.out.println("Podaj linie " + (i + 1) + ":");
                daneWysylane.writeBytes(bufWe.readLine() + "\r\n");                               
                daneWe = daneOdbierane.readLine();
                System.out.println(daneWe);
            }            

            daneOdbierane.close();
            daneWysylane.close();
            gniazdoPolaczenia.close();                     
        }
        catch(NumberFormatException nfe) 
        {        
            System.out.println("Ilosc lini musi byc liczba! " + nfe.getMessage());
        }          
        catch(UnknownHostException uhe) 
        {        
            System.out.println("Unknown host exception: " + uhe.getMessage());
        }        
        catch (IOException e)
        {
            System.out.println("Błąd odczytu/zapisu: " + e.getMessage());
        }  
    }
}
