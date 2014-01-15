/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package klientlab2;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;

public class SendAllCommand implements ICommandInterface {

    @Override
    public void Execute(String parameters, DataOutputStream outStream, BufferedReader inStream) {
        try
        {
            // Wysyłamy komendę na serwer.
            outStream.writeBytes("SENDALL " + parameters + "\r\n");
        }
        catch(IOException e)
        {
            System.out.println("IOException: " + e.getMessage());
        }
    }
    
}
