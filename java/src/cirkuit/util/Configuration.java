package cirkuit.util;

import java.io.*;
import java.util.*;

/**
 * This Configuration class manages configuration files of the form :
<pre>
key1 = value1
key2 = value2
...
keyN = valueN
</pre>
 * All the values are stored as strings.
 * @author Sven Gowal (sven.gowal@lha.ch)
 * @author Pascal Perez (pascal.perez@lha.ch)
 * @version 1.0
 */
public class Configuration {
    private String url;
    private Hashtable hash = new Hashtable();
    
    /**
     * Creates a new instance of a configuration file manger.
     * @param url the url of the configuration file
     */
    public Configuration(String url) {
        this.url = url;
    }
    
    /**
     * Gets the value corresponding to key. You must read the configuration file before calling this method
     * otherwise the returned value will always be null.
     * @param key the value's key
     * @return the value who's key is passed in argument, null if the key does not exist
     * @see cirkuit.util.Configuration#read
     */
    public String get(String key) {
        if (hash.containsKey(key)) {
            return (String)hash.get(key);
        } else {
            return null;    
        }
    }
    
    /**
     * Sets the value corresponding to key.
     * @param key the value's key
     * @param value the value to store
     */
    public void set(String key, String value) {
        hash.put(key, value);
    }
    
    /**
     * Writes the configuration.
     * @return true if the file was successfully written
     */
    public boolean write() {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(url));
            for (Enumeration e = hash.keys() ; e.hasMoreElements() ; ) {
                Object ele = e.nextElement();
                out.write((String)ele + " = " + (String)hash.get(ele));
                out.newLine();
            }
            if (out != null) out.close();
            return true;
        }
        catch(Exception e) {
            return false;
        }
    }
    
    /**
     * Reads the configuration file.
     * @return true if the file was successfully read
     */
    public boolean read() {
        try {
            BufferedReader in = new BufferedReader(new FileReader(url));
            String buffer = buffer = in.readLine();
            String conf;
            String value;
            while (buffer != null) {
                if (buffer.indexOf(" = ") > 0) {
                    conf = buffer.substring(0, buffer.indexOf(" = "));
                    value = buffer.substring(buffer.indexOf(" = ")+3);
                    set(conf,value);
                }
                buffer = in.readLine();
            }
            if (in != null) in.close();   
            return true;
        }
        catch(Exception e) {
            return false;
        }
    }
}
