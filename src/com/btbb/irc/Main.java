/*
 Copyright (c) 2012 Serge Humphrey<bobtheblueberry@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 and associated documentation files (the "Software"), to deal in the Software without restriction,
 including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
 subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or
 substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.btbb.irc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

public class Main {

    static IRCBot                  bot;
    static File                    settingsFile;
    static HashMap<String, String> settings;

    /**
     * @param args
     */
    public static void main(String[] args) {
        settingsFile = new File("settings");
        if (!settingsFile.exists())
            writeSettings();
        loadSettings();
        bot = new IRCBot(settings);

    }

    private static void loadSettings() {
        settings = new HashMap<String, String>();
        try {
            BufferedReader r = new BufferedReader(new FileReader(settingsFile));
            String line;
            while ((line = r.readLine()) != null) {
                String[] d = line.split("=", 2);
                if (d.length < 2)
                    continue;
                settings.put(d[0], d[1]);
            }
            r.close();
        } catch (IOException e) {
            System.out.println("Error loading settings");
            e.printStackTrace();
        }
    }

    private static void writeSettings() {
        try {
            PrintWriter w = new PrintWriter(settingsFile);
            w.println("server=irc.freenode.net");
            w.println("channels can be multiple: #chan1,#chan2,#chan3 etc.");
            w.println("channel=#lobby");
            w.println("nick=AlfredENewman");
            w.println("password=muffins");
            w.println("port=6667");
            w.println("markov=markov.lua");
            w.println("logs=logs/");
            w.println("owner=JonPHolder");
            w.println("modewait=1");
            
            w.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error writing settings");
            e.printStackTrace();
        }
    }

}
