/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package klientlab2;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;

public class ListClientsCommand implements ICommandInterface {

    @Override
    public void Execute(String parameters, DataOutputStream outStream, BufferedReader inStream) {
        try
        {
            // Wysyłamy komendę na serwer i odbieramy jedną linię.
            outStream.writeBytes("LISTCLIENTS\r\n");
            System.out.println(inStream.readLine().replace(";", ", "));
        } 
        catch(IOException e) 
        {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    
}
