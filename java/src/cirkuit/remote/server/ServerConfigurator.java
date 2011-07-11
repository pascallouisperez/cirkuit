package cirkuit.remote.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Hashtable;

import cirkuit.remote.StreamTokenizer;

/**
 * This object is linked to a server and communicates via the 'in' and 'out' streams to configure the
 * server.<br>
 * <br>
 * <table width="300" border="0">
 *   <tr>
 *     <td colspan="2"><b>Command list:</b></td>
 *   </tr>
 *   <tr>
 *     <td width="100" valign="top">echo <i>text</i></td>
 *     <td width="200"><i>Output the specified text.</i></td>
 *   </tr>
 *   <tr>
 *     <td width="100" valign="top">exit</td>
 *     <td width="200"><i>Close the configurator.</i></td>
 *   </tr>
 *   <tr>
 *     <td width="100" valign="top">ls [-lp]</td>
 *     <td width="200"><i>List the active games.<br>
 *                        -l : detailed listing<br>
 *                        -p : list players on the server</i></td>
 *   </tr>
 *   <tr>
 *     <td width="100" valign="top">quit</td>
 *     <td width="200"><i>Same as exit.</i></td>
 *   </tr>
 *   <tr>
 *     <td width="100" valign="top">set <i>key</i> <i>value</i></td>
 *     <td width="200"><i>Store the value in the key variable.</i></td>
 *   </tr>
 *   <tr>
 *     <td width="100" valign="top">unset <i>key</i></td>
 *     <td width="200"><i>Delete the key variable.</i></td>
 *   </tr>
 * </table>
 * @author Sven Gowal (sven.gowal@lha.ch)
 * @author Pascal Perez (pascal.perez@lha.ch)
 * @version 1.0
 */
public class ServerConfigurator extends Thread {
    private boolean exitable = true;
    private Server server = null;
    private StreamTokenizer streamTokenizer = null;
    private PrintStream out = null;
    private volatile boolean run = true;
    
    private Hashtable environment = new Hashtable();
    
    /**
     * @param server the server to which this configurator should be linked
     * @param in the input stream on which the configurator should receive commands
     * @param out the output stream on which the configurator should send the results
     */
    public ServerConfigurator(Server server, InputStream in, OutputStream out) throws NullPointerException {
        this(server, in, out, true);
    }
    
    /**
     * @param server the server to which this configurator should be linked
     * @param in the input stream on which the configurator should receive commands
     * @param out the output stream on which the configurator should send the results
     * @param exitable tells wether this server configurator can be exited
     */
    public ServerConfigurator(Server server, InputStream in, OutputStream out, boolean exitable) throws NullPointerException {
        this.exitable = exitable;
        setDaemon(true);
        this.streamTokenizer = new StreamTokenizer(in);
        if (server != null && out != null) {
            this.server = server;
            this.out = new PrintStream(out);
        } else {
            throw new NullPointerException();
        }
        environment.put("bash", "[root@CirKuit 2D]# ");
    }
    
    public void run() {
        String command = null;
        while (run) {
            try {
                out.print(environment.get("bash"));
                streamTokenizer.loadLine(environment);
                command = streamTokenizer.getNextToken();
                if (command != null)
                    this.getClass().getMethod("command_"+command, null).invoke(this, null);
            } catch (NoSuchMethodException e) {
                out.println(command+": command not found");
            } catch (IOException e) {
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.yield();
        }
    }
    
    public int command_echo() {
        out.println(streamTokenizer.getAll());
        return 0;
    }
    
    public int command_exit() {
        if (exitable)
            run = false;
        else
            System.out.println("cannot exit the configurator in this mode");
        return 0;
    }
    
    public int command_ls() {
        char c;
        int paramLength;
        // options
        boolean detailed   = false;
        boolean players    = false;
        int     linelength = 80;
        // parsing parameters
        String param;
        while ((param = streamTokenizer.getNextToken()) != null) {
            paramLength = param.length();
            if (paramLength > 1) {
                c = param.charAt(0);
                if (c == '-') {
                    for (int i = 1; i < paramLength; i++) {
                        c = param.charAt(i);
                        switch ((int)c) {
                            case (int)'l':
                                detailed = true;
                                break;
                                
                            case (int)'p':
                                players = true;
                                break;
                        }
                    }
                }
            }
        }
        // showing the list
        //String[] col0 = {"lha", "people", "do not call!", "please", "22lha", "22people", "22do not call!", "22please"};
        String[] col0 = null;
        if (players)
            col0 = server.getUserList();
        else
            col0 = server.getGameList();
        String[] col1 = null;
        
        int col0length = Math.max(maxlength(col0)+1, 10);
        int col1length = Math.max(maxlength(col0)+1, 3);
        if (detailed) {
            col1 = new String[col0.length];
            
            out.print("Total ");
            if (players) {
                out.print("players ");
                for (int i = 0; i < col0.length; i++) {
                    col1[i] = server.getUserGame(col0[i]);
                    if (col1[i] == null) {
                        col1[i] = "";
                    }
                }
            } else {
                out.print("games ");
                for (int i = 0; i < col0.length; i++) {
                    col1[i] = server.getGamePlayersNumber(col0[i])+"";
                }
            }
            out.println(col0.length);
            for (int i = 0; i < col0.length; i++) {
                // col 0
                out.print(col0[i]);
                for (int k = col0[i].length(); k < col0length; k++)
                    out.print(" ");
                // col 1
                out.print(col1[i]);
                for (int k = col1[i].length(); k < col1length; k++)
                    out.print(" ");
                out.println();
            }
        } else {
            int colnum  = linelength/col0length;
            int linenum = col0.length/colnum+1;
            int index   = 0;
            for (int i = 0; i < linenum; i++) {
                for (int j = 0; j < colnum; j++) {
                    if (index < col0.length) {
                        out.print(col0[index]);
                        for (int k = col0[index].length(); k < col0length; k++)
                            out.print(" ");
                        index++;
                    }
                }
                out.println();
            }
        }
        return 0;
    }
    
    public int command_poweroff() {
        server.poweroff();
        return command_exit();
        //return 0;
    }
    
    public int command_quit() {
        return command_exit();
    }
    
    public int command_set() {
        String key = streamTokenizer.getNextToken();
        String value = streamTokenizer.getNextToken();
        if (key != null && value != null) {
            environment.put(key, value);
            return 0;
        } else {
            return 1;
        }
    }
    
    public int command_unset() {
        String key = streamTokenizer.getNextToken();
        if (key != null && environment.containsKey(key)) {
            environment.remove(key);
            return 0;
        }
        return 1;
    }
    
    private static int maxlength(String[] array) {
        int l = -1;
        for (int i = 0; i < array.length; i++) {
            if (array[i] != null && array[i].length() > l) {
                l = array[i].length();
            }
        }
        return l;
    }
}