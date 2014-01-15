/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package klientlab2;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;

public class GetAllCommand implements ICommandInterface {

    @Override
    public void Execute(String parameters, DataOutputStream outStream, BufferedReader inStream) {
        try
        {
            // Wysyłamy komendę GETALL i pobieramy jedną linię danych.
            outStream.writeBytes("GETALL\r\n");
            System.out.println(inStream.readLine());
        }
        catch(IOException e)
        {
            System.out.println("IOException: " + e.getMessage());
        }
    }
    
}
