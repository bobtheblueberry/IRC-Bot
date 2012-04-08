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

    /**
     * Bot class constructor, set the defaults for the output stream and the
     * channel
     * 
     * @param writer
     * @param own
     */
    IRC(BufferedWriter writer)
        {
            bw = writer;
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
        say(message, IRCBot.bot.person);
    }

    public void say(String message, String person) throws IOException {
        bw.write("PRIVMSG " + person + " :" + message + "\n");
        bw.flush();
    }

    /**
     * Joins a specified IRC channel and sets this.channel accordingly
     * 
     * @param firstChannel
     * @throws java.io.IOException
     */
    public void join(String chan) throws IOException {
        bw.write("JOIN " + chan + "\n");
        bw.flush();
    }

    /**
     * Leave the current channel
     * 
     * @throws java.io.IOException
     */
    public void part(String channel) throws IOException {
        bw.write("PART " + channel + "\n");
        bw.flush();
    }

    /**
     * Quit the current IRC session
     * 
     * @param reason
     * @throws java.io.IOException
     */
    public void quit(String channel) throws IOException {
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

    public void ghost(String nick, String pass) throws IOException {
        bw.write(":PRIVMSG NickServ GHOST $nick $pass\n".replaceAll("\\$nick", nick).replaceAll("\\$pass", pass));
        bw.flush();
    }

    public void identify(String nick, String pass) throws IOException {
        bw.write(":PRIVMSG NickServ IDENTIFY $nick $pass\n".replaceAll("\\$nick", nick).replaceAll("\\$pass", pass));
        bw.flush();
    }
}