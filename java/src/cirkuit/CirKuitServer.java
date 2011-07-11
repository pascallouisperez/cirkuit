package cirkuit;

import cirkuit.remote.server.Server;
import cirkuit.remote.server.ServerConfigurator;

/**
 * This is the main class of the server program of CirKuit 2D.
 * @author Sven Gowal (sven.gowal@lha.ch)
 * @author Pascal Perez (pascal.perez@lha.ch)
 */
public class CirKuitServer {
    private static Server server = null;
    private static ServerConfigurator serverConfigurator = null;
    
    public static void main(String[] args) {
        try {
            if (args.length == 1) {
                server = new Server(Integer.parseInt(args[0]));
            } else {
                server = new Server();
            }
            serverConfigurator = new ServerConfigurator(server, System.in, System.out, false);
            server.start();
            serverConfigurator.start();
        } catch(Exception e) {
            System.out.println("Usage: java -cp classes cirkuit.CirKuitServer [port]");
        }
    }
}