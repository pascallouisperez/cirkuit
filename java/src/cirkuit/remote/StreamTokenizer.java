package cirkuit.remote;

import java.io.InputStream;
import java.io.IOException;
import java.awt.Color;
import java.util.Hashtable;

/**
 * @author Sven Gowal (sven.gowal@lha.ch)
 * @author Pascal Perez (pascal.perez@lha.ch)
 * @version 1.0
 */
public class StreamTokenizer {
    private InputStream in = null;
    private StringBuffer inBuffer = new StringBuffer(256);
    
    /**
     * Create a stream tokenizer.
     * @param in the input stream on which the tokenizer receives commands
     */
    public StreamTokenizer(InputStream in) throws NullPointerException {
        if (in != null) {
            this.in = in;
        } else {
            throw new NullPointerException();
        }
    }
    
    /**
     * Reads the input and places the next command line in the input buffer.<br>
     * The line feed '\n' is not appended to the command line but the carriage return is.
     */
    public void loadLine(Hashtable environment) throws IOException {
        StringBuffer varBuffer = null;
        char c;
        boolean var = false;
        // flushing the input buffer
        inBuffer.delete(0, inBuffer.length());
        
        // getting the command
        while((c = (char)in.read()) != '\n') {
            if (var) {
                if (Character.isWhitespace(c)) {
                    // variable substitution
                    if (environment.containsKey(varBuffer.toString())) {
                        inBuffer.append(environment.get(varBuffer.toString()));
                    } else {
                        inBuffer.append('$'+varBuffer.toString());
                    }
                    // adding the whitespace
                    inBuffer.append(c);
                    // changing the parsing mode
                    var = false;
                } else if (c == '$') {
                    // variable substitution
                    if (environment.containsKey(varBuffer.toString())) {
                        inBuffer.append(environment.get(varBuffer.toString()));
                    } else {
                        inBuffer.append('$'+varBuffer.toString());
                    }
                    // initialising the variable buffer
                    varBuffer = new StringBuffer(20);
                } else {
                    // saving the variable name
                    varBuffer.append(c);
                }
            } else {
                if (c == '$') {
                    // changing the parsing mode
                    var = true;
                    // initialising the variable buffer
                    varBuffer = new StringBuffer(20);
                } else {
                    // adding the character
                    inBuffer.append(c);
                }
            }
        }
        // variable substitution
        if (var) {
            if (environment.containsKey(varBuffer.toString())) {
                inBuffer.append(environment.get(varBuffer.toString()));
            } else {
                inBuffer.append('$'+varBuffer.toString());
            }
        }
    }
    
    /**
     * Fetches the rest of the buffer at once.
     * @return a string containing the buffer's content
     */
    public String getAll() {
        String bufferContent;
        char c;
        int start = -1;
        int i = 0;
        int l = inBuffer.length();
        
        // searching start
        while (start < 0 && i < l) {
            c = inBuffer.charAt(i);
            if (c == ' ') {
                i++;
            } else {
                start = i;
                i++;
            }
        }
        
        bufferContent = inBuffer.substring(start, l);
        inBuffer.delete(0, l);
        return bufferContent;
    }
    
    
    /**
     * Fetches the next token.
     * @return the next token, null if there are no more
     */
    public String getNextToken() {
        if (hasMoreToken()) {
            int l = inBuffer.length();
            StringBuffer result = new StringBuffer(l);
            int start = -1, end = -1;
            int i = 0;
            char c = (char)13;
            boolean isString = false;
            
            // searching start
            while (start < 0 && i < l) {
                c = inBuffer.charAt(i);
                if (Character.isWhitespace(c)) {
                    i++;
                } else {
                    start = i;
                    i++;
                }
            }
            if (c == '"') {
                isString = true;
            } else {
                result.append(c);
            }
            // searching end
            while (end < 0 && i < l) {
                c = inBuffer.charAt(i);
                if (c == '\\') {
                    i++;
                } else if (c == (int)13 || (!isString && Character.isWhitespace(c))) {
                    end = i;
                } else if (isString && c == '"') {
                    end = i + 1;
                } else {
                    result.append(c);
                    i++;
                }
            }
            if (end < 0) {
                end = l;
            }
            // sending back the result
            inBuffer.delete(0, end);
            return result.toString();
        }
        return null;
    }
    
    /**
     * @return true if there are more tokens, false otherwise
     */
    public boolean hasMoreToken() {
        char c;
        int i = 0;
        int l = inBuffer.length();
        while (i < l) {
            c = inBuffer.charAt(i);
            if (!Character.isWhitespace(c) || c != (int)13) {
                return true;
            }
        }
        return false;
    }
}
