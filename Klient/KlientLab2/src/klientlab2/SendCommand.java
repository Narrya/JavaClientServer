/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package klientlab2;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;

public class SendCommand implements ICommandInterface {

    @Override
    public void Execute(String parameters, DataOutputStream outStream, BufferedReader inStream) {
        try
        {
            // Sprawdzamy ilość i jakość parametrów oraz jeśli są poprawne 
            // wysyłamy komendę na serwer.
            String[] parsedParameters = parameters.split(" ", 2);
            if (parsedParameters.length == 2 && !parsedParameters[1].equals("")) {
                outStream.writeBytes("SEND " + parsedParameters[0] + " " + parsedParameters[1] + "\r\n");   
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
