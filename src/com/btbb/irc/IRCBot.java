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

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * IRC Bot
 * 
 * static String server = "irc.freenode.net"; static String channel =
 * "#enigma-dev"; static String nick = "dickkkk"; static String password =
 * "dick555"
 * 
 * @author Serge Humphrey
 * 
 */
public class IRCBot {

    /**
     * @param args
     */
    /**
     * static String server = "irc.esper.net"; static String channel =
     * "#azayzel"; static String nick = "ralph"; static String password =
     * "ralphkkk";
     */
    // ENIGMA-DEV Version
    static String            server            = "irc.freenode.net";
    static String            channel           = "#enigma-dev";
    final static String      NICK              = "dickkkk";
    static String            password          = "dick555";

    static String            nick              = NICK;

    // static String mcBot = "BeeblBot";

    static ArrayList<String> foods;
    static ArrayList<String> jokes;
    static ArrayList<String> insults;
    static ArrayList<String> twain;

    static String            logDir            = "/home/serge/Develop/Bots/irc.freenode.net/logs/";
    static String            banDir            = "/home/serge/Develop/Bots/irc.freenode.net/bans/";

    static String            markov            = "//home/serge/Develop/Bots/markov.lua";
    static String            rLine             = "/home/serge/Develop/Bots/randomline.sh";

    static File              logFolder;
    static File              banFolder;

    static int               port              = 6667;
    static IRC               JavaBot;

    static boolean           joined            = false;
    static boolean           invited           = false;
    static String            newchannel        = "";

    static boolean           isMSG             = false;
    static boolean           isPM              = false;
    static String            message           = null;
    static String            person            = null;

    long                     ping              = 0;

    private Socket           irc               = null;
    private BufferedWriter   bw                = null;
    private BufferedReader   br                = null;

    static char              IRC_COLOR_WHITE   = 300;
    static char              IRC_COLOR_BLACK   = 301;
    static char              IRC_COLOR_RED     = 304;
    static char              IRC_COLOR_ORANGE  = 307;
    static char              IRC_COLOR_YELLOW  = 308;
    static char              IRC_COLOR_GREEN   = 309;
    static char              IRC_COLOR_CYAN    = 311;
    static char              IRC_COLOR_BLUE    = 312;
    static char              IRC_COLOR_MAGENTA = 313;
    static char              IRC_COLOR_GREY    = 314;

    public static boolean validName(String name) {
        return name.length() > 1 && name.length() < 17 && !name.matches("(?i).*[^a-z0-9_].*");
    }

    public IRCBot()
        {

            System.out.println("Loading Foods");
            loadFile(foods = new ArrayList<String>(), "foods");
            System.out.println("Loading Jokes");
            loadFile(jokes = new ArrayList<String>(), "jokes");
            System.out.println("Loading Insults");
            loadFile(insults = new ArrayList<String>(), "insults");
            System.out.println("Loading Twain Quotes");
            loadFile(twain = new ArrayList<String>(), "twain");

            logFolder = new File(logDir);
            logFolder.mkdirs();
            banFolder = new File(banDir);
            banFolder.mkdirs();

            setupReconnect();

            while (true) {
                run();
                System.out.println("ERROR! Disconnected!");
                // Reset variables
                joined = false;
                invited = false;
                newchannel = "";
                isMSG = false;
                isPM = false;
                message = null;
                person = null;
                nick = NICK;
            }
        }

    private void run() {

        boolean justghosted = false;
        boolean moded = false;

        try {
            System.out.println("Starting IRC Bot");
            // our socket we're connected with
            irc = new Socket(server, port);
            // out output stream
            bw = new BufferedWriter(new OutputStreamWriter(irc.getOutputStream()));
            // our input stream
            br = new BufferedReader(new InputStreamReader(irc.getInputStream()));

            System.out.println("Joining Server");
            // create a new instance of the JavaBot
            JavaBot = new IRC(bw, channel);

            System.out.println("Logging in");

            // authenticate the JavaBot with the server
            JavaBot.login(nick);

            String currLine = null;
            while ((currLine = br.readLine()) != null) {
                ping = System.currentTimeMillis();
                String arg;
                String[] args;
                Request request = new Request(currLine);
                String sender = request.getSender();
                if (!moded && request.isMode()) {
                    moded = true;
                }

                isMSG = request.isMSG();
                isPM = isMSG && request.isPM();
                if (isPM) {
                    person = sender;
                } else {
                    person = channel;
                }
                message = request.getMessage();

                if (request.isPing()) {
                    JavaBot.pong(request.getPingMessage());
                    System.out.println("Pong");
                    continue;
                }
                System.out.println(currLine);

                if (request.isInvite()) {
                    // INVITE
                    newchannel = request.getInviteMessage();
                    invited = true;
                }

                // Check for nickname is already in use
                if (!joined) {
                    if (!justghosted && request.isError("433")) {
                        System.out.println("Ghosting nick...");
                        // Login again
                        int n = (int) (Math.random() * 20);
                        nick = nick + n;
                        JavaBot.nick(nick);
                        // Ghost nick
                        JavaBot.ghost(NICK, password);
                        justghosted = true;
                        continue;
                    }
                }

                if (justghosted && !nick.equals(NICK) && request.isGhostedMessage()) {
                    System.out.println("Changing nickname");
                    JavaBot.nick(NICK);
                    nick = NICK;
                    justghosted = false;
                    continue;
                }
                // JOIN CHANNEL
                if (invited || (!joined && moded)) {

                    if (!invited) {
                        // identify with nick serv
                        System.out.println("Identifying with NickServ");
                        JavaBot.identify(NICK, password);
                    } else {
                        // Leave old channel
                        if (joined) {
                            JavaBot.say("Going over to " + newchannel);
                            JavaBot.part();
                            channel = newchannel;
                        }
                    }
                    System.out.println("Entering Channel " + channel);
                    JavaBot.join(channel);
                    joined = true;
                    invited = false;
                    continue;
                }

                // LOG
                if (!request.isCommand()) {

                    // TODO : NOTE : Changed "." for command to "!"
                    if (isMSG && !isPM) {
                        if (message != null && message.length() > 0 && !request.getSender().equals(nick)
                                && !message.startsWith("!") && !message.contains("(") && !message.contains(")")
                                && !message.startsWith("@")) {
                            // Add phrase to the log files
                            File log = new File(logDir, sender);
                            BufferedWriter out = new BufferedWriter(new FileWriter(log, true));
                            out.write(message.replaceAll("\n", "") + "\n");
                            out.close();
                        }
                    } else if (isPM) {
                        if (request.hasOwnership()) {
                            IRCBot.isPM = false;
                            JavaBot.say(message);
                        } else {
                            JavaBot.MSG(message, sender);
                        }
                    }

                    continue;
                } /*else {
                    if (isBanned(sender)) {
                        // hahaha
                        continue;
                    }
                  }*/
                if ((arg = request.matchArg("echo")) != null) {
                    JavaBot.say(arg);
                    continue;
                } else if (request.match("echo")) {
                    JavaBot.say("Rats live on no evil star");
                    continue;
                }
                if (request.match("time")) {
                    Date d = new Date();
                    String msg = "The date is " + d;
                    JavaBot.say(msg);
                } else if (request.match("help")) {
                    // JavaBot.say("Commands: !help !time !define [arg] !greet (arg?) !taste [arg] "
                    // +
                    // "!mock [arg] !on !off !joke !cynic !quote [arg] !search [pl] [arg]");
                    JavaBot.say("Commands: $help $time $define [arg] $greet (arg?) $taste [arg] "
                            + "$mock [arg] $joke $cynic $twain $quote [arg] $search [pl] [arg] $google [arg] $g [arg]");

                    continue;
                }/* else if (request.match("on")) {
                    JavaBot.say("Fuck On");

                 } else if (request.match("off")) {
                    JavaBot.say("Fuck Off");

                 } */else if (request.match("joke")) {
                    JavaBot.say(getRandom(jokes));
                    continue;

                } else if (request.match("cynic")) {
                    JavaBot.say(getRandom(insults));
                    continue;

                } else if (request.match("twain")) {
                    JavaBot.say(getRandom(twain));
                    continue;

                } else if ((arg = request.matchArg("taste")) != null) {
                    if (Math.random() * 4 > 3) {
                        String food1, food2;
                        food1 = getRandom(foods);
                        food2 = getRandom(foods);
                        while (food1.equals(food2)) {
                            food1 = getRandom(foods);
                        }
                        JavaBot.say(arg + ", you taste like " + food1 + ", or " + food2);
                        continue;
                    } else {
                        JavaBot.say(arg + ", you taste like " + getRandom(foods));
                        continue;
                    }
                } else if ((arg = request.matchArg("mock")) != null) {

                    /*        if (arg.equalsIgnoreCase(nick)) {
                                JavaBot.say(request.getSender()
                                        + ", you have been suspended from using commands for five minutes");
                                new File(banFolder, sender).createNewFile();
                            } else*/if (!validName(arg) && !(new File(logFolder, arg).exists())) {
                        JavaBot.say("Invalid name");
                        continue;
                    } else {

                        File[] files = searchForPlayer(arg);

                        if (files.length < 1) {
                            JavaBot.say("I haven't heard of " + arg);
                            continue;
                        } else if (files.length > 1) {
                            JavaBot.say(getDidYouMean(files));
                            continue;
                        } else {
                            File f = files[0];

                            ProcessBuilder pb = new ProcessBuilder("bash", "-c", markov + " -d 1 -n 20 < \""
                                    + f.getAbsolutePath() + "\" | tr '\n' ' ' | tr -d '\r' > .out");
                            Process p = pb.start();
                            try {
                                p.waitFor();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            BufferedReader in = new BufferedReader(new FileReader(".out"));
                            String phrase = in.readLine();
                            in.close();
                            char c = 3;
                            if (phrase == null) {
                                JavaBot.say("huh?");
                            } else if (phrase.length() > 2) {
                                JavaBot.say(c + "2\"" + phrase.substring(0, phrase.length() - 2) + "\"" + c + "6 -- "
                                        + f.getName());
                                continue;
                            } else {
                                JavaBot.say("I don't know " + f.getName() + " well enough.");
                                continue;
                            }
                        }
                    }
                } else if (request.match("greet")) {
                    JavaBot.say(getGreeting(sender));
                    continue;
                } else if ((arg = request.matchArg("g")) != null) {
                    if (arg == null || arg.matches("\\s"))
                        JavaBot.say("http://tiny.cc/PAIN");
                    else
                        JavaBot.say("http://www.google.com/search?q=" + arg.replaceAll(" ", "+"));
                    continue;
                } else if (request.match("g")) {
                    JavaBot.say("http://tiny.cc/PAIN");
                    continue;
                }

                else if ((arg = request.matchArg("google")) != null) {
                    if (arg == null || arg.matches("\\s"))
                        JavaBot.say("http://tiny.cc/PAIN");
                    else
                        JavaBot.say("http://lmgtfy.com/?q=" + arg.replaceAll(" ", "%20"));
                    continue;
                } else if (request.match("google")) {
                    JavaBot.say("http://tiny.cc/PAIN");
                    continue;
                } else if ((arg = request.matchArg("greet")) != null) {

                    JavaBot.say(getGreeting(arg));
                    continue;
                } else if ((arg = request.matchArg("quote")) != null) {

                    File f = new File(logDir, arg);
                    if (!f.exists()) {
                        if (!validName(arg)) {
                            JavaBot.say("You scum!");
                            continue;
                        } else {
                            File[] files = searchForPlayer(arg);
                            if (files.length > 1) {
                                JavaBot.say(getDidYouMean(files));
                                continue;
                            } else if (files.length > 0) {
                                f = files[0];
                            }
                        }
                    }
                    if (f.exists()) {
                        String quote = "";
                        // Try to get a good quote
                        for (int i = 0; i < 10; i++) {
                            quote = randomLine(f.getAbsolutePath());
                            // This is the check to see if it's a good one
                            if (quote != null && quote.contains(" ")) {
                                break;
                            }
                        }
                        char c = 3;
                        JavaBot.say(c + "3\"" + quote + "\"" + c + "10 -- " + f.getName());
                        continue;
                    } else {
                        JavaBot.say("I haven't heard of " + arg);
                        continue;
                    }
                } else if ((arg = request.matchArg("define")) != null) {
                    JavaBot.say(define(arg));
                } else if ((args = request.matchArgName1("search")) != null) {
                    search(args);
                    continue;
                } else if ((arg = request.matchArg("help")) != null) {
                    if (arg.split(" ").length > 1) {
                        int i = arg.indexOf(" ");
                        String msg = arg.substring(i + 1);
                        JavaBot.say(getIdiotMessage(msg));
                        continue;
                    } else {
                        JavaBot.say("Busy");
                        continue;
                    }
                } else if (request.match("colors")) {

                    sayColors();
                    continue;
                }

                if (request.hasOwnership()) {
                    // check to see if the owner has given the !exit command
                    if (request.match("exit")) {
                        JavaBot.say("I'm tired, bye yall");
                        JavaBot.quit();
                    }

                    // parts one room and joins another, gives a nice little
                    // going away speech as well
                    if (request.match("join")) {
                        String newChannel = request.getDestinationChannel();
                        JavaBot.say("I'm going to go over to " + newChannel
                                + " and see what they're up to over there. Cya.");
                        JavaBot.part();
                        channel = newChannel;
                        JavaBot.join(channel);
                        JavaBot.say("Hey guys!");
                    }
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("No such host");
        } catch (IOException e) {
            System.err.println("There was an error connecting to the host");
            e.printStackTrace();
        }
    }

    /**
     * Automatic reconnect
     * 
     */
    public void setupReconnect() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // if bot hasn't contacted the server for 4 minutes
                if (irc != null && (ping + 60 * 4 * 1000) < System.currentTimeMillis()) {
                    try {
                        System.out.println("Closing socket!");
                        // Hopefully this will cause an exception or something
                        irc.close();
                    } catch (IOException e) {
                        System.out.println("Error killing bot connection!");
                        e.printStackTrace();
                    }
                }
            }
        }, 30 * 1000, 60 * 1000);
    }

    public static String getRandom(ArrayList<String> list) {
        int i = (int) (Math.random() * list.size());
        return list.get(i);
    }

    public static void loadFile(ArrayList<String> list, String name) {
        try {
            URL url = IRCBot.class.getClassLoader().getResource(name);
            BufferedReader in;
            if (url == null) {
                in = new BufferedReader(new FileReader(name));
            } else {
                URLConnection connection = url.openConnection();
                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            }
            String line;
            while ((line = in.readLine()) != null) {
                if (line.length() > 0)
                    list.add(line);
            }
            in.close();
        } catch (FileNotFoundException e) {
            System.out.println("Can't load " + name);
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Can not load " + name);
            e.printStackTrace();
        }
    }

    private static String getDidYouMean(File[] files) {
        String msg = "Do you mean [";
        for (int i = 0; i < files.length; i++) {
            msg = msg + files[i].getName() + ((i < files.length - 1) ? ", " : "");
        }
        return msg + "] ?";
    }

    private static String getIdiotMessage(String baseMsg) {
        int i = (int) (Math.random() * 8);
        switch (i) {
            case 0:
                return "Why Should I " + baseMsg + "?";
            case 1:
                return "Go " + baseMsg + " by yourself!";
            case 2:
                return "Time will " + baseMsg;
            case 3:
                return "It's too much work to " + baseMsg;
            case 4:
                return "What will happen when you " + baseMsg + "?";
            case 5:
                return "Didn't I just hear you say you wanted to " + baseMsg + "?";
            case 6:
                return "How come you didn't want to " + baseMsg + " yesterday?";
            case 7:
                return "I found a new idea, it's to go and " + baseMsg + "!";
        }
        return "You are a moron, because you want to " + baseMsg;
    }

    private static String getGreeting(String name) {
        int i = (int) (Math.random() * 12);
        switch (i) {
            case 0:
                return "Sup? " + name;
            case 1:
                return "Get a life, " + name;
            case 2:
                return "What's goin onnn?";
            case 3:
                return "hallo " + name;
            case 4:
                return "Merry Christmas " + name;
            case 5:
                return "Hi I'm ralph. What's a, " + name;
            case 6:
                return "hi, " + name;
            case 7:
                return "Beware of the commies, " + name;
            case 8:
                return "Who were you " + name + "?";
            case 9:
                return "Go outside " + name;
            case 10:
                return "You are a moron, " + name;
            case 11:
                ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
                scheduler.schedule(new Messanger("wait"), 1, TimeUnit.SECONDS);
                scheduler.schedule(new Messanger("nevermind"), 3, TimeUnit.SECONDS);
                return "Wait";

        }
        return "NUUU";
    }

    public static File[] searchForPlayer(String s) {
        File file = new File(logFolder, s);
        if (file.exists()) {
            return new File[]
                { file };
        }
        File[] files = logFolder.listFiles();
        ArrayList<File> fl = new ArrayList<File>();
        for (File f : files) {
            if (f.getName().equalsIgnoreCase(s))
                fl.add(f);
            else if (f.getName().toLowerCase().contains(s.toLowerCase())) {
                fl.add(f);
            }
        }
        if (fl.size() > 0)
            return fl.toArray(new File[fl.size()]);
        return new File[0];
    }

    /**
     * banned from using commands for Five minutes
     * 
     * @param s
     *            person's name
     * 
     * @return whether they are or not
     */
    public static boolean isBanned(final String s) {
        File[] files = banFolder.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return s.equalsIgnoreCase(pathname.getName());
            }
        });
        // Five minutes is 30000 Milliseconds
        long t = 300000;
        for (File f : files) {
            long m = f.lastModified();
            if (System.currentTimeMillis() < (m + t)) {
                return true;
            }
            f.delete();
        }
        // guess they aren't banned
        return false;
    }

    private static String randomLine(String file) {
        try {
            String l;
            ProcessBuilder pb = new ProcessBuilder("bash", "-c", rLine + " " + file + "  > .line");
            Process p;

            p = pb.start();
            try {
                p.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            BufferedReader in = new BufferedReader(new FileReader(".line"));
            l = in.readLine();
            in.close();
            return l;
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return null;
    }

    /**
     * Thanks Ism
     * 
     * @param phrase
     * @return
     */
    public static String define(String phrase) {
        String uphrase = phrase;
        try {
            uphrase = URLEncoder.encode(uphrase, "UTF-8");
        } catch (UnsupportedEncodingException e) {
        }
        // String url = "http://api.wordnik.com/api/word.xml/" + phrase +
        // "/definitions?api_key=" + key;
        String url = "http://www.merriam-webster.com/dictionary/" + uphrase;
        try {
            HttpURLConnection conn = (HttpURLConnection) (new URL(url).openConnection());

            // Set up a request.
            conn.setConnectTimeout(5000); // 10 sec
            conn.setReadTimeout(5000); // 10 sec
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("User-agent", "spider");

            // Send the request.
            conn.connect();
            InputStream is = conn.getInputStream();
            Scanner sc = new Scanner(is);
            sc.useDelimiter("<strong>:</strong>");
            sc.next();
            sc.useDelimiter("<(?:br/|/p|/div)>");
            String def = sc.next();
            sc.close();
            // IrcBot.toLog(def);
            def = def.replaceAll("<strong>:</strong>", ":").replaceAll("\n", " ");
            def = def.replaceAll("<.*?>", "--").replaceAll("\\&.*?;", "").trim();
            return phrase + def;
        } catch (NoSuchElementException e) {
            return "I don't have a definition for " + phrase;
        } catch (SocketTimeoutException e) {
            return "That definition is taking too long.";
        } catch (IOException e) {
            e.printStackTrace();
            return "I've encountered an internet error.";
        }
    }

    private static class Messanger implements Runnable {

        String msg;

        public Messanger(String msg)
            {
                this.msg = msg;
            }

        @Override
        public void run() {
            try {
                JavaBot.say(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private static void sayColors() {
        char c = 3;
        String s = "Colors ";
        for (int i = 0; i < 15; i++) {
            s = s + c + i + "  color " + i;
        }
        try {
            JavaBot.say(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void search(String[] args) {
        try {
            String key = args[1];
            /*      if (!key.matches("\\w+")) {
                      JavaBot.say("Bad keyword!");
                      return;
                  }*/
            File f = new File(logDir, args[0]);
            if (!f.exists()) {
                if (!validName(args[0])) {
                    JavaBot.say("Bad name!");
                    return;
                } else {
                    File[] files = searchForPlayer(args[0]);
                    if (files.length > 1) {
                        JavaBot.say(getDidYouMean(files));
                        return;
                    } else if (files.length > 0) {
                        f = files[0];
                    }
                }
            }
            if (f.exists()) {
                // Great, this will definately suck
                ArrayList<String> lines = new ArrayList<String>();
                BufferedReader r = new BufferedReader(new FileReader(f));
                String line;
                key = key.toLowerCase();
                while ((line = r.readLine()) != null)
                    if (line.toLowerCase().contains(key))
                        lines.add(line);
                r.close();
                /*     ProcessBuilder pb = new ProcessBuilder("bash", "-c", "grep -i " + key + " "
                             + f.getAbsolutePath() + " > .search");
                     Process p = pb.start();
                     try {
                         p.waitFor();
                     } catch (InterruptedException e) {
                         e.printStackTrace();
                     }
                      BufferedReader in = new BufferedReader(new FileReader(".search"));
                     String l;
                     while ((l = in.readLine()) != null) {
                         lines.add(l);
                     }
                     in.close();
                     */
                String s = "";
                char c = 3;
                int x = (int) (Math.random() * 3) + 3;

                if (lines.size() < 1) {
                    JavaBot.say(c + "4No results!");
                    return;
                }
                for (int i = 0; i < Math.min(x, lines.size()); i++) {
                    int n;
                    if (lines.size() <= x) {
                        n = i;
                    } else {
                        n = (int) (Math.random() * lines.size());
                    }
                    line = lines.get(n);
                    String space = "";
                    if (i > 0) {
                        space = " ";
                    }
                    // s = s + space + c + "9\"" + c + "14" + line + c +
                    // "9\"";
                    s = s + space + "\"" + line + "\"";
                }
                // JavaBot.say(c + "13" + f.getName() + c + "1 said " +
                // s);
                JavaBot.say("(" + lines.size() + ") " + c + "6" + f.getName() + c + "1 said " + c + "2" + s);
            } else {
                JavaBot.say("I haven't heard of " + args[0]);
            }
        } catch (Exception exc) {
            System.err.println("Search function failed");
            exc.printStackTrace();
        }
    }
}