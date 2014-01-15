/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package klientlab2;

import java.io.BufferedReader;
import java.io.DataOutputStream;

/**
 * Interfejs komend - ma tylko jedną metodę która wykonuje w określonych klasach
 * właściwe dla komendy operacje.
 * 
 * @author kkupidura
 */
public interface ICommandInterface 
{
    public void Execute(String parameters, DataOutputStream outStream, BufferedReader inStream);
}