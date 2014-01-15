/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package klientlab2;

import javax.naming.directory.InvalidAttributesException;

/**
 * @author kkupidura
 */
public class CommandFactory {
    public static ICommandInterface Create(String commandName) throws InvalidAttributesException {
        // Łapiemy komendy wpisane małymi literami.
        commandName = commandName.toLowerCase();
        
        // Tworzymy określoną klasę komendy w zależności od przekazanej nazwy.
        // Każda komenda implementuje interfejs ICommandInterface dzięki czemu
        // możemy za pomocą jednego typu zwrócić różne komendy których specyficzne
        // zachowanie jest zaimplementowane w samej klasie.
        if (commandName.equals("exit")) 
        {
            return new ExitCommand();
        } 
        else if (commandName.equals("listclients")) 
        {
            return new ListClientsCommand();
        } 
        else if (commandName.equals("getall")) 
        {
            return new GetAllCommand();
        } 
        else if (commandName.equals("sendall")) 
        {
            return new SendAllCommand();
        } 
        else if (commandName.equals("send")) 
        {
            return new SendCommand();
        } 
        else if (commandName.equals("get")) 
        {
            return new GetCommand();
        } 
        else 
        {
            // Nieobsługiwana komenda.
            throw new InvalidAttributesException(commandName);
        }
    }
}
