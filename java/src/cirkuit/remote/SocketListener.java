package cirkuit.remote;

/**
 * This class defines a Socket Listener.
 * @author Sven Gowal (sven.gowal@lha.ch)
 * @author Pascal Perez (pascal.perez@lha.ch)
 */
public interface SocketListener {
    public void dataArrived(String command);
}
