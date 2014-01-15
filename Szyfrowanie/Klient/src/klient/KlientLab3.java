package klient;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.spec.*;
import java.security.*;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;

public class KlientLab3 {

    // Obiekty odpowiedzialne za komunikacje.
    public static Socket clientSocket = null;
    static DataOutputStream sentData = null;
    static BufferedReader receivedData = null;
    // Parametry klucza publicznego.
    private final static BigInteger publicKeyModulus = new BigInteger("115258481741813675423209375660109193517328583790037036964788906320492445756911468825226294715705245733596520593212697179564927300183432267180794521502251501773339073346218376071738116548893377782100293337427813500208384189118994378824778265668337377583823474495977495982615130469441499313170420835128694839317");
    private final static BigInteger publicKeyExponent = new BigInteger("65537");
    // Klucze.
    private static Key decipheringKey = null;
    private static SecretKey aesKey = null;

    // Generujemy klucz publiczny i zapisujemy go w polu w klasie (RSA).
    private static void generatePublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(publicKeyModulus, publicKeyExponent);
        KeyFactory keysFactory = KeyFactory.getInstance("RSA");
        decipheringKey = (Key) keysFactory.generatePublic(publicKeySpec);
    }

    // Generujemy klucz dla AES.
    private static void generateAesKey() throws NoSuchAlgorithmException {
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        SecureRandom source = new SecureRandom();
        generator.init(source);
        aesKey = generator.generateKey();
    }

    // Wczytywanie linii z bufora wejściowego.
    public static String readLine() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        return reader.readLine();
    }

    public static void main(String[] args) throws Exception {
        try {
            clientSocket = new Socket("ib.polsl.pl", 10101);
        } catch (IOException ex) {
            throw new IOException(ex);
        }

        // Przygotowanie strumienia wysyłania i odbierania.
        sentData = new DataOutputStream(clientSocket.getOutputStream());
        receivedData = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        // Tworzenie klucza publicznego dla RSA.
        generatePublicKey();
        System.err.println("PUBLIC KEY: " + new BigInteger(decipheringKey.getEncoded()));

        // Tworzenie klucza AES do autoryzacji na serwerze.
        generateAesKey();
        System.err.format("AES KEY: %s\n", (new BigInteger(aesKey.getEncoded())).toString());

        // Opakowujemy klucz AES w szyfrowanie RSA aby zautoryzować się na serwerze i móc
        // wysyłać komendy.
        Cipher RSA = Cipher.getInstance("RSA");
        RSA.init(Cipher.WRAP_MODE, decipheringKey);
        byte[] wrappedAesKey = RSA.wrap(aesKey);
        System.err.println("OPAKOWANE DANE: " + new BigInteger(wrappedAesKey));

        String base64EncodedData = DatatypeConverter.printBase64Binary(wrappedAesKey) + "\n";
        System.err.println("DANE ZAKODOWANE BASE64: " + base64EncodedData);

        try {
            // Wysyłamy klucz AES do serwera i oczekujemy OK.
            sentData.writeBytes(base64EncodedData);
            String response = receivedData.readLine();
            
            if (!response.equals("OK")) {
                return;
            }
     
            // Ustawienia i tworzenie szyfratora/deszyfratora.
            Cipher coding = Cipher.getInstance("AES/ECB/PKCS5Padding");
            Cipher decoding = Cipher.getInstance("AES/ECB/PKCS5Padding");

            // od razu inicjujemy jeden do kodowania, drugi do odkodowywania
            coding.init(Cipher.ENCRYPT_MODE, aesKey);
            decoding.init(Cipher.DECRYPT_MODE, aesKey);

            String userInput, dataForSending, serverResponse;
            byte[] cipheredData, decipheredData;
            
            System.out.println("WPISYWANIE KOMEND...\n");

            do {
                userInput = readLine();
                
                if (userInput.equals("EXIT")) {
                    break;
                } else {
                    // Kodujemy dane wpisane przez użytkownika.
                    cipheredData = coding.doFinal(userInput.getBytes());
                    
                    // Po zakodowaniu musimy dodać koniec linii.
                    dataForSending = DatatypeConverter.printBase64Binary(cipheredData) + "\n";
                    sentData.write(dataForSending.getBytes());
                    serverResponse = receivedData.readLine();
                    cipheredData = DatatypeConverter.parseBase64Binary(serverResponse);
                                        
                    try {
                        // Deszyfrujemy i wypisujemy na ekran, z obsługą błędów.
                        decipheredData = decoding.doFinal(cipheredData);
                        String linijka = new String(decipheredData);
                        System.err.println(linijka);
                    } catch (Exception exception) {
                        System.err.format("BLAD DESZYFROWANIA: %s\n", exception.getMessage());
                    }

                    // Czyścimy dane dla następnego kroku.
                    userInput = "";
                    dataForSending = "";
                    serverResponse = "";
                    cipheredData = null;
                    decipheredData = null;
                }
            } while (true);
        } catch (IOException exception) {
            System.err.format("BLAD WE/WY: %s\n", exception.getMessage());
        } catch (Exception exception) {
            System.err.format("WYJATEK: %s\n", exception.getMessage());
        } finally {
            sentData.close();
            receivedData.close();
            clientSocket.close();
        }
    }
}