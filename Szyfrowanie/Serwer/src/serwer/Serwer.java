package serwer;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.spec.*;
import java.util.concurrent.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.xml.bind.*;

/**
 *
 * @author jk
 * @version 0.2
 */

public class Serwer implements Runnable {

    /* klucz publiczny serwera; wykorzystaj klucz wygenerowany wcześniej */
    final static BigInteger kluczPublicznyDzielnik = new BigInteger("115258481741813675423209375660109193517328583790037036964788906320492445756911468825226294715705245733596520593212697179564927300183432267180794521502251501773339073346218376071738116548893377782100293337427813500208384189118994378824778265668337377583823474495977495982615130469441499313170420835128694839317");
    final static BigInteger kluczPublicznyWykladnik = new BigInteger("65537");
    final static BigInteger kluczPrywatnyDzielnik = new BigInteger("115258481741813675423209375660109193517328583790037036964788906320492445756911468825226294715705245733596520593212697179564927300183432267180794521502251501773339073346218376071738116548893377782100293337427813500208384189118994378824778265668337377583823474495977495982615130469441499313170420835128694839317");
    final static BigInteger kluczPrywatnyWykladnik = new BigInteger("6026989592584272482343386642464473597874255102437660034458879441541840664188711776005165204246790013717980317575414090275249185311024648360904265150803604398602221411737649178424386841193099949780926792304950347104107643022732973584722692679755969098129998365980940937388156676140744399217197989544170787333");
    
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

        // klucze prywatny i publiczny; zamiast tutaj, to powinno znaleźć
        // się raczej w głównym wątku serwera (bo w tym przypadku wszystkie
        // kopie są takie same), ale 
        Key kPrywatny = null;
        Key kPubliczny = null;
        
        // dane klienta
        InetAddress klient;
        String nazwaKlienta;
        
        Asystent(Socket s)
        {
            gniazdoPolaczenia = s;
        }
        

        
        /**
         * Spróbuj zamknąć strumienie czytania i pisania
         * 
         * @return false - jesli nie udalo sie zamknac, true - udalo sie zamknac
         */
        private boolean sprobujZamknacStrumienie()
        {
                try
                {
                    if (daneOdbierane != null)
                        daneOdbierane.close();
                    if (daneWysylane != null)
                        daneWysylane.close();
                }
                catch (Exception ee)
                {
                    return false;
                }
                return true;
        
        }
        
        /**
         * sprobuj posprzatac przed zamknieciem watku: zamknij strumienie
         * i gniazdo
         * 
         * @return false - cos sie nie udalo, ale sprobowano wszystkiego (czyli
         * cos moze nie byc zamkniete, ale nic na to nie mozna poradzic)
         * true - udalo sie
         * 
         */
        private boolean sprobujPosprzatacIZamknij()
        {
            boolean ret = sprobujZamknacStrumienie();
                
            try {
                gniazdoPolaczenia.close();                
            } catch (Exception e)
            {
                System.err.println("nie udało się zamknąć połączenia!");
                ret = false;
            }
            return ret;

        }
        
        /**
         * Odwtorz klucze serwera: kPrywatny i kPubliczny na podstawie specyfikacji podanej w klasie
         * nadrzednej (wspolnej dla wszystkich watkow).
         */
        private void OdtworzKluczeSerwera()
        {
            RSAPrivateKeySpec kPrywatnyS = new RSAPrivateKeySpec(kluczPrywatnyDzielnik, kluczPrywatnyWykladnik);
            RSAPublicKeySpec kPublicznyS = new RSAPublicKeySpec(kluczPublicznyDzielnik, kluczPublicznyWykladnik);  
            
            // 0b. odtwórz klucze
            try 
            {
                KeyFactory keyFact = KeyFactory.getInstance("RSA");
                kPubliczny = (Key) keyFact.generatePublic(kPublicznyS);
                kPrywatny = (Key) keyFact.generatePrivate(kPrywatnyS);    
            } 
            catch (Exception ex) 
            {
                throw new RuntimeException("Nie udalo sie odtworzyc kluczy: blad serwera!"+ ex.getMessage());
            }
            
            // prymitywny test kluczy:
            try {
                // klcz do zakodowania
                byte [] kluczB = "BardzoTajnyKlucz".getBytes(); 
                SecretKey klucz = new SecretKeySpec(kluczB, "AES");

                Cipher s = Cipher.getInstance("RSA");
                s.init(Cipher.WRAP_MODE, kPubliczny);
                byte [] opakowanyKluczB = s.wrap(klucz);
                
                s.init(Cipher.UNWRAP_MODE, kPrywatny);
                SecretKey klucz2 = (SecretKey)s.unwrap(opakowanyKluczB, "AES", Cipher.SECRET_KEY);
                
            } catch (Exception e) {
                System.err.println(e.getMessage());
                throw new RuntimeException(e);
            }
            
            
        }
        
        
        /**
         * obsluga klienta
         */
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
                System.err.println("Nie udało się utworzyć strumieni" + e.getMessage());
                sprobujPosprzatacIZamknij(); // te które się udało
                return;
            }

            // dane klienta do logowania
            klient = gniazdoPolaczenia.getInetAddress();
            nazwaKlienta = klient.getCanonicalHostName();

            // 0. zregeneruj klucz prywatny i publiczny (patrz przyklad) 
            // NORMALNIE TO RACZEJ POWINNO BYĆ ROBIONE RAZ DLA CAŁEGO 
            // SERWERA (a poza tym czytane z certyfikatów)
            

            // 0a. odtwórz specyfikację
            try 
            {
                OdtworzKluczeSerwera();
                System.err.println("klucz publiczny serwera: " + new BigInteger(kPubliczny.getEncoded()));
            } 
            catch (Exception e) 
            {
                System.err.println("brak możliwości odtworzenia klucza RSA" + e.getMessage());
                sprobujPosprzatacIZamknij();
                return;
            }
            
            // 1. oczekuj, że pierwsza wiadomość będzie zawierała opakowany, zakodowany
            // klucz AES. odkoduj go (sposób kodowania musi być zgodny z klientem)
            // odpakuj używając swojego klucza prywatnego i zregeneruj klucz
            
            // tutaj kodowanie to BASE64 + \n, więc czytamy readlinem + 
            // dekodujemy i odpakowujemy (zgodnie z przykładami z Szyfrowanie.java)

            // 1a. sprawdź, czy odebrałeś/aś dobry klucz (np. wydrukuj go jako liczbę
            // w kliencie i tutaj)

            // 2. po odebraniu i regenracji wyślij OK (niezaszyfrowane)
            
            
            SecretKey kluczAES = null;
            try 
            {
                // odbierz pierwszą linijkę
                String zakodowanyOpakowanyKlucz = daneOdbierane.readLine();
                // odkoduj BASE64
                System.err.println("zakodowany opakowany klucz BASE64: " + zakodowanyOpakowanyKlucz);
                
                byte [] opakowanyKluczAES = DatatypeConverter.parseBase64Binary(zakodowanyOpakowanyKlucz);
                System.err.println("opakowany klucz jako bigint: " + new BigInteger(opakowanyKluczAES));
                
                // odpkauj klucz AES korzystając z RSA i klucza PRYWATNEGO
                Cipher szyfrRSA = Cipher.getInstance("RSA");
                szyfrRSA.init(Cipher.UNWRAP_MODE, kPrywatny);
                kluczAES = (SecretKey)szyfrRSA.unwrap(opakowanyKluczAES, "AES", Cipher.SECRET_KEY);
                System.err.format("Od klienta %s odebralem klucz AES %s\n", 
                        nazwaKlienta, 
                        (new BigInteger(kluczAES.getEncoded())).toString());
                // potwierdz OK
                daneWysylane.write("OK\n".getBytes());
            } 
            catch (Exception e)
            {
                sprobujPosprzatacIZamknij();
                System.err.println("Nie udało się odpakować klucza! " + e.getMessage());
                return;
            }
            

            // 3. każdą linijkę odebraną jakoś zmodyfikuj i odeślij
            // zaszyfrowaną

            // od razu dwa, żeby nie przestawiać trybu
            Cipher szyfrator;
            Cipher deszyfrator;
            try {
                // dla wygody szyfrator ECB, żeby nie przesyłać dodatkowo IV
                szyfrator = Cipher.getInstance("AES/ECB/PKCS5Padding");
                deszyfrator = Cipher.getInstance("AES/ECB/PKCS5Padding");
                // od razu inicjujemy jeden do kodowania, drugi do odkodowywania
                szyfrator.init(Cipher.ENCRYPT_MODE, kluczAES);
                deszyfrator.init(Cipher.DECRYPT_MODE, kluczAES);
            } catch (Exception e) {
                System.err.println("nie udalo sie uruchomic szyfratora" + e.getMessage());
                return;
            }

            // wejdź w pętlę przetwarzania
            try 
            {
                String doPrzetworzenia;
                while ((doPrzetworzenia = daneOdbierane.readLine()) != null)
                {
                    // poczekaj na odpowiedź
                    System.err.format("Od %s dostalem zaszyfrowana i zakodowana linijke: %s\n",
                            nazwaKlienta, doPrzetworzenia);
                    
                    // mamy string BASE64
                    byte [] zaszyfrowane = DatatypeConverter.parseBase64Binary(doPrzetworzenia);
                    
                    // przestawiamy sie na odszyfrowanie + wykonujemy
                    // je w jednym kroku (bez update'ów i innych)
                    byte [] daneOdszyfrowane = deszyfrator.doFinal(zaszyfrowane);
                    
                    // mamy linijkę
                    String linijka;
                    try {
                        linijka = new String(daneOdszyfrowane);
                    } catch (Exception e1) {
                        linijka = "BLAD DESZYFROWANIA";
                    }
                    
                    // dla testowania serwera
                    System.err.println("odebralem od " + nazwaKlienta + " " + linijka);
                    
                    // przetwarzamy ją; dodajemy :) i enter, żeby po odkodowaniu było widać dane odebrane
                    linijka = ":) " + linijka + "\n";
                    
                    // szyfrujemy; tutaj znowu w jednym kroku
                    zaszyfrowane = szyfrator.doFinal(linijka.getBytes());
                    
                    // kodujemy i na końcu dodajemy '\n', żeby dało się przeczytać readlinem!
                    String daneWy = DatatypeConverter.printBase64Binary(zaszyfrowane) + "\n";
                    System.err.format("Do %s wysylam zakodowany: %s\n", nazwaKlienta, daneWy);
                    daneWysylane.write(daneWy.getBytes());
                }

            } catch (Exception ex) {
                System.err.format("Nie udalo sie przetworzyc danych klienta %s: %s\n",  
                        nazwaKlienta, ex.getMessage());
            }
            
            
            // koniec
            sprobujPosprzatacIZamknij();

        } // run

    }

}
