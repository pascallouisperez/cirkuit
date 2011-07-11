package cirkuit.remote;

import java.net.*;
import java.io.*;

/**
 * @author Sven Gowal (sven.gowal@lha.ch)
 * @author Pascal Perez (pascal.perez@lha.ch)
 * @version 1.0
 */
public class CommandSocket {
    private Socket socket = null;
    public InputStream in = null;
    public OutputStream out = null;

    public CommandSocket(Socket socket) throws IOException {
        this.socket = socket;
        this.in = socket.getInputStream();
        this.out = socket.getOutputStream();
    }
}
