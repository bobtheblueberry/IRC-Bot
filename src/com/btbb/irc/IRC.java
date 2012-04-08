package com.btbb.irc;

import java.io.BufferedWriter;
import java.io.IOException;



/**
 * The skeleton of the IRC Bot.
 * 
 * @author dydx
 */
public class IRC {
    BufferedWriter bw;
    String channel;

    /**
     * Bot class constructor, set the defaults for the output stream and the
     * channel
     * 
     * @param writer
     * @param own
     */
    IRC(BufferedWriter writer, String chan) {
        bw = writer;
        channel = chan;
    }

    /**
     * This is the main part of the bot, it replies to PING's
     * 
     * @throws java.io.IOException
     */
    public void pong(String s) throws IOException {
        bw.write("PONG :" + s + "\n");
        bw.flush();
    }

    /**
     * General purpose method for inputting into the channel at hand
     * 
     * @param message
     * @throws java.io.IOException
     */
    public void say(String message) throws IOException {
        if (IRCBot.isPM) {
            this.MSG(message, IRCBot.person);
            return;
        }
        bw.write("PRIVMSG " + channel + " :" + message + "\n");
        bw.flush();
    }
    
    public void MSG(String message, String person) throws IOException {
        bw.write("PRIVMSG " + person + " :" + message + "\n");
        bw.flush();
    }

    /**
     * Joins a specified IRC channel and sets this.channel accordingly
     * 
     * @param channel
     * @throws java.io.IOException
     */
    public void join(String chan) throws IOException {
        bw.write("JOIN " + chan + "\n");
        bw.flush();

        // we're doing this so that there's no confusion
        // as to which channel the bot is in
        channel = chan;
    }

    /**
     * Leave the current channel
     * 
     * @throws java.io.IOException
     */
    public void part() throws IOException {
        bw.write("PART " + channel + "\n");
        bw.flush();
    }

    /**
     * Quit the current IRC session
     * 
     * @param reason
     * @throws java.io.IOException
     */
    public void quit() throws IOException {
        bw.write("QUIT " + channel + "\n");
        bw.flush();
    }

    /**
     * Class method used to authenticate the Bot with the IRC server
     * 
     * @param nick
     * @throws java.io.IOException
     */
    public void login(String nick) throws IOException {
        bw.write("USER Q Q Q :Q\n".replaceAll("Q", nick));
        bw.write("NICK " + nick + "\n");
        bw.flush();
    }
    
    public void nick(String nick) throws IOException {
        bw.write("NICK " + nick + "\n");
        bw.flush();
    }
    
    public void ghost(String nick, String pass)  throws IOException {
        bw.write(":PRIVMSG NickServ GHOST $nick $pass\n".replaceAll("\\$nick", nick)
                .replaceAll("\\$pass", pass));
        bw.flush();
    }
    
    public void identify(String nick, String pass)  throws IOException {
        bw.write(":PRIVMSG NickServ IDENTIFY $nick $pass\n".replaceAll("\\$nick", nick)
                .replaceAll("\\$pass", pass));
        bw.flush();
    }
}