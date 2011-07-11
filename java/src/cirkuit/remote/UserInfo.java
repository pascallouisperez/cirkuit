package cirkuit.remote;

import java.awt.Color;

/**
 * @author Sven Gowal (sven.gowal@lha.ch)
 * @author Pascal Perez (pascal.perez@lha.ch)
 * @version 1.0
 */
public class UserInfo {
    private Color color = null;
    private SocketAnalyser socketAnalyser = null;
    private String gamename = null;
    private String joinedgame = null;
    private boolean ready = false;
    
    public UserInfo(SocketAnalyser sa, Color color) {
        this.color = color;
        this.socketAnalyser = sa;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }
    
    public Color getColor() {
        return this.color;
    }
    
    public void setSocketAnalyser(SocketAnalyser sa) {
        this.socketAnalyser = sa;
    }
    
    public SocketAnalyser getSocketAnalyser() {
        return this.socketAnalyser;
    }
    
    public void setGameName(String gamename) {
        this.gamename = gamename;
    }
    
    public String getGameName() {
        return this.gamename;
    }
    
    public void setJoinedGame(String gn) {
        this.joinedgame = gn;
    }
    
    public String getJoinedGame() {
        return this.joinedgame;
    }
    
    public void setReady(boolean r) {
        this.ready = r;
    }
    
    public boolean isReady() {
        return this.ready;
    }
}
