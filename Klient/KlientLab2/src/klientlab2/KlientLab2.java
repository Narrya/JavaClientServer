package klientlab2;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import javax.naming.directory.InvalidAttributesException;

/**
 * @author kkupidura
 */
public class KlientLab2 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        // zajmie się wykonywaniem wątku serwera; pojedynczego
        try
        {
            Socket gniazdoPolaczenia = new Socket("ib.polsl.pl", 10105);
            
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
            catch (IOException ioException)
            {
                System.out.println("Nie udało się utworzyć strumieni" + ioException.getMessage());
                
                try
                {
                    if (daneOdbierane != null)
                        daneOdbierane.close();

                    if (daneWysylane != null)
                        daneWysylane.close();

                    gniazdoPolaczenia.close();
                }
                catch (Exception genericException)
                {
                    System.out.println("Generic exception " + genericException.getMessage());
                }
            }
                                          
            // Odbieram pierwszy komunikat.
            daneWysylane.writeBytes("LISTCLIENTS\r\n");            
            
            List<String> loginsList = Arrays.asList(daneOdbierane.readLine().split(";"));            
            System.out.println("Zalgowani uzytkownicy:");
            
            for(String login : loginsList) {
                System.out.println(login);     
            }               
            
            // Tworzenie buforowanych linii komend.
            InputStreamReader converter = new InputStreamReader(System.in);
            BufferedReader in = new BufferedReader(converter);
  
            // Pytamy użytkownika o login.
            System.out.println("Podaj login:");             
            String userLogin = in.readLine();
            
            if (loginsList.contains(userLogin)) {
                System.out.println("Nazwa uzytkownika jest juz zajeta. KONIEC.");
                return;
            }
            
            // Logowanie i ustawienia dot. wysyłania.
            daneWysylane.writeBytes("LOGIN " + userLogin + "\r\n");            
            daneWysylane.writeBytes("AUTORECEIVE TRUE\r\n");
            
            String commandLine = "", parameters;
            String[] parsedCommand = new String[2];
            
            while (!(commandLine.toLowerCase().equals("exit"))) {
                // Wczytujemy komendę i zabezpieczamy się przed komendami
                // bezparametrowymi aby nie wkradł się nam null.
                commandLine = in.readLine();
                parsedCommand = commandLine.split(" ", 2);                
                if (parsedCommand.length < 2) {
                    parameters = "";                    
                } else {
                    parameters = parsedCommand[1];
                }
                
                try 
                {
                    // Tworzymy za pomocą fabryki specyficzną klase komendy (zależną od nazwy).
                    ICommandInterface command = CommandFactory.Create(parsedCommand[0]);       
                    
                    // Wykonujemy komendę która została zwrócona z fabryki.
                    command.Execute(parameters, daneWysylane, daneOdbierane);
                } 
                catch(InvalidAttributesException unexpectedCommand)
                {
                    // Jeśli komenda jest nieobsługiwana wyświetlamy informację i tresc błędu.
                    System.out.println("Komenda jest nieobslugiwana (" + unexpectedCommand.getMessage() + ")!");
                }
            }           

            daneOdbierane.close();
            daneWysylane.close();
            gniazdoPolaczenia.close();                     
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