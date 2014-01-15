/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package klientlab2;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;

public class GetCommand implements ICommandInterface {

    @Override
    public void Execute(String parameters, DataOutputStream outStream, BufferedReader inStream) {
        try
        {
            // Sprawdzamy ilość i jakość parametrów oraz jeśli są poprawne 
            // wysyłamy komendę na serwer.
            if (!parameters.contains(" ") && !parameters.equals("")) {
                outStream.writeBytes("GET " + parameters + "\r\n");   
                System.out.println(inStream.readLine());
            } else {
                System.out.println("Zła liczba parametrów!");
            }                          
        }
        catch(IOException e)
        {
            System.out.println("IOException: " + e.getMessage());
        }
    }
    
}
