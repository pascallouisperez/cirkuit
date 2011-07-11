package cirkuit.player;

import java.util.Vector;
import java.util.Stack;
import java.awt.Point;
import java.util.Random;
import java.awt.Color;

import cirkuit.game.Game;

/**
 * This class defines a computer player.
 * @author Sven Gowal (sven.gowal@lha.ch)
 * @author Pascal Perez (pascal.perez@lha.ch)
 */
public class ComputerPlayer extends Player {
    private Point bestPoint = null;
    private Stack lastPosition = new Stack();
    
    private Random random = new Random();
    
    public ComputerPlayer() {
        super();
    }
    
    public ComputerPlayer(String name) {
        super(name);
    }
    
    public ComputerPlayer(Color color) {
        super(color);
    }
    
    public ComputerPlayer(String name, Color color) {
        super(name, color);
    }

	public Point play(Game g) {
        // gets the best move
        try {
            setBestPoint((Game)g.clone(), 2);
        } catch (CloneNotSupportedException e) { }
        
        // if the best move is wrong
        if (bestPoint == null || !g.getCircuit().isInside(bestPoint) || (getPosition()!=null && g.getCircuit().crossedBorder(getPosition(), bestPoint))) {
            System.out.println(!g.getCircuit().isInside(bestPoint)+"!"+g.getCircuit().crossedBorder(getPosition(), bestPoint));
            Vector allMoves = g.getValidMoves(this);
            Vector validMoves = cleanValidMoves(g, (Vector)allMoves.clone());
            Point tmpPoint;
            
            if (getPosition()!=null) {
                int index = -1;
                double maxLength = 0, tmpLength = 0;
                if (validMoves.size() > 0) { // get the fastest one
                    for (int i=0; i<validMoves.size(); i++) {
                        tmpPoint = (Point)validMoves.get(i);
                        tmpLength = (tmpPoint.x-getPosition().x)*(tmpPoint.x-getPosition().x)+(tmpPoint.y-getPosition().y)*(tmpPoint.y-getPosition().y);
                        if (index == -1 || tmpLength > maxLength) {
                            index = i;
                            maxLength = tmpLength;
                        }
                    }
                    return (Point)validMoves.get(index);
                } else { // if no valid move was found
                    for (int i=0; i<allMoves.size(); i++) {
                        tmpPoint = (Point)allMoves.get(i);
                        tmpLength = (tmpPoint.x-getPosition().x)*(tmpPoint.x-getPosition().x)+(tmpPoint.y-getPosition().y)*(tmpPoint.y-getPosition().y);
                        if (index == -1 || tmpLength > maxLength) {
                            index = i;
                            maxLength = tmpLength;
                        }
                    }
                    return (Point)allMoves.get(index);
                }
            } else {
                return (Point)allMoves.get(Math.abs(random.nextInt()%allMoves.size()));
            }
        }
        return bestPoint;
    }
    
    private void setBestPoint(Game g, int depth) {
        explore(g, 0, Math.max(depth,1), -10000, 10000);
    }
    
    /** The heuristic */
    private int evaluateGame(Game g, Player player) {
        Point p = getLastPosition();
        Point pl;
        // if outside or crossed
        if (getPosition() != null && (!g.getCircuit().isInside(getPosition()) || (p != null && g.getCircuit().crossedBorder(p, getPosition())))) {
            return -1000;
        }
        
        // crossed
        int n = lastPosition.size()-1;
        for (int i=0; i< n; i++) {
            p = (Point)lastPosition.get(i);
            pl = (Point)lastPosition.get(i+1);
            if (p != null && pl != null && g.getCircuit().crossedBorder(p, pl))
                return -1000;
        }
        
        // has still valid moves
        if (cleanValidMoves(g, g.getValidMoves(this)).size() == 0) {
            return -1000;
        }
        /*if (goesOut(g, this, 4)) {
            return -1000;
        }*/
        
        // angle
        double retAngle = 0;
        double bestAngle = (getBestAngle(g.getCircuit().getInnerBorder(), getLastPosition())+getBestAngle(g.getCircuit().getOuterBorder(), getLastPosition()))/2;
        double diff = Math.abs(bestAngle-getAngle());
        retAngle = 1/(diff+1)*10;
       
        // go fast
        double retSelf = 0;
        p = getLastPosition();
        if (p != null && getPosition() != null) {
            retSelf += (getPosition().x-p.x)*(getPosition().x-p.x)+(getPosition().y-p.y)*(getPosition().y-p.y);
        }
        
        double totalLength = 0;
        p = null;
        Player tmpP;
        for (int i=0; i<g.getPlayers().size(); i++) {
            tmpP = (Player)g.getPlayers().get(i);
            if (tmpP.getName().equals(getName()))
                p = tmpP.getPosition();
                break;
        }
        pl = getPosition();
        if (p != null && pl != null) { 
            totalLength += getLength(g.getCircuit().getOuterBorder(), p, pl);
            totalLength += getLength(g.getCircuit().getInnerBorder(), p, pl);
        }
        
        return (int)(retSelf + 3*totalLength + 8*retAngle);
    }
    
    /** Checks if the player goes out after n turn */
    private boolean goesOut(Game g, Player player, int d) {
        if (getPosition() != null && !g.getCircuit().isInside(getPosition())) {
            return true;
        } else {
            if (d != 0) {
                Stack stk = new Stack();
                stk.addAll(g.getValidMoves(player));
                Point tmpMove;
                boolean b = true;
                while(stk.size()>0) {
                    tmpMove = (Point)stk.pop();
                    
                    // save actual moves
                    Point tmpPointStart = player.getPosition();
                    double startAngle = player.getAngle();
                    double startSpeed = player.getSpeed();
                    
                    if (tmpPointStart != null && g.getCircuit().isInside(tmpPointStart)) {
                        if (player == this) {
                            putLastPosition();
                        }
                        player.setFakePosition(tmpMove);
                        player.setAngle(g.getAngle(tmpMove.x-tmpPointStart.x, tmpMove.y-tmpPointStart.y));
                        player.setSpeed(Math.sqrt((tmpMove.y-tmpPointStart.y)*(tmpMove.y-tmpPointStart.y)+(tmpMove.x-tmpPointStart.x)*(tmpMove.x-tmpPointStart.x))/((double)g.getProperties().getGrid()));
                    }
                   
                    b = b && goesOut(g, player, d-1);
                    
                    //undo move
                    player.setFakePosition(tmpPointStart);
                    if (player == this) {
                        resetLastPosition();
                    }
                    player.setAngle(startAngle);
                    player.setSpeed(startSpeed);
                }
                return b;
            } else {
                return false;
            }
        }
    }
    
    /** Gets the smallest length of the border from pi to pf */
    private int getLength(Point[] pt, Point pi, Point pf) {
        int dmini=-1, dminf=-1;
        int indexi=-1, indexf=-1;
        int di, df;
        
        for (int i=0; i<pt.length; i++) {
            di = (pt[i].x-pi.x)*(pt[i].x-pi.x)+(pt[i].y-pi.y)*(pt[i].y-pi.y);
            df = (pt[i].x-pf.x)*(pt[i].x-pf.x)+(pt[i].y-pf.y)*(pt[i].y-pf.y);
            if (dmini==-1 || dmini>di) {
                indexi = i;
                dmini = di;
            }
            if (dminf==-1 || dminf>df) {
                indexf = i;
                dminf = df;
            }
        }
        
        return getLength(pt, indexi, indexf);
    }

    /** Gets the smallest length of the border from pt[iinit] and pt[ifinal] */
    private int getLength(Point[] pt, int iinit, int ifinal) {
        int l1=0, l2=0, itemp;
        
        if (iinit > ifinal) {
            itemp = iinit;
            iinit = ifinal;
            ifinal = itemp;
        }
            
        for (int i=iinit+1; i<ifinal; i++) {
            l1 += (pt[i].x-pt[i-1].x)*(pt[i].x-pt[i-1].x)+(pt[i].y-pt[i-1].y)*(pt[i].y-pt[i-1].y);
        }
        
        for (int i=1; i<iinit; i++) {
            l2 += (pt[i].x-pt[i-1].x)*(pt[i].x-pt[i-1].x)+(pt[i].y-pt[i-1].y)*(pt[i].y-pt[i-1].y);
        }
        for (int i=ifinal+1; i<pt.length; i++) {
            l2 += (pt[i].x-pt[i-1].x)*(pt[i].x-pt[i-1].x)+(pt[i].y-pt[i-1].y)*(pt[i].y-pt[i-1].y);
        }
        
        return (l1<l2)?l1:l2;
    }
    
    /** Minimax */
    private boolean minimax(int current_score, int bestScore, Player player) {
        if (player != this) {
            if (current_score < bestScore)
                return true;
            else if (current_score > bestScore)
                return false;
        } else {
            if (current_score > bestScore)
                return true;
            else if (current_score < bestScore)
                return false;
        }
        return (random.nextInt()%2==1)?true:false;
    }

    /** Exploring with alpha beta cutoff algorithm */
    private Result explore(Game g, int current_depth, int max_depth, int _alpha, int _beta) {
        int bestScore;
        Result ret, toSend;
        int alpha = _alpha;
        int beta = _beta;
        
        // init of bestScore
        Player player;
        player = this;
        bestScore = -10000;
        
        // end of tree
        if (current_depth == max_depth || g.getPlayers().size()==0) {
            toSend = new Result();
            toSend.score = evaluateGame(g, this);
            toSend.alphaBeta = 0;
            return toSend;
        }
        
        // save actual moves
        Point tmpPointStart = player.getPosition();
        double startAngle = player.getAngle();
        double startSpeed = player.getSpeed();
        
        Stack stk = new Stack();
        stk.addAll(g.getValidMoves(player));
        Point tmpMove;
        while(stk.size()>0) {
            tmpMove = (Point)stk.pop();
            
            // if inside, make move
            if (tmpPointStart != null && g.getCircuit().isInside(tmpPointStart)) {
                if (player == this) {
                    putLastPosition();
                }
                player.setFakePosition(tmpMove);
                player.setAngle(g.getAngle(tmpMove.x-tmpPointStart.x, tmpMove.y-tmpPointStart.y));
                player.setSpeed(Math.sqrt((tmpMove.y-tmpPointStart.y)*(tmpMove.y-tmpPointStart.y)+(tmpMove.x-tmpPointStart.x)*(tmpMove.x-tmpPointStart.x))/((double)g.getProperties().getGrid()));
            }
            ret = explore(g, current_depth+1, max_depth, alpha, beta);
            
            // alpha beta
            if (current_depth != 0 && current_depth != max_depth-1) {
                if (player == this) {
                    alpha = ret.alphaBeta;
                } else {
                    beta = ret.alphaBeta;
                }
            }
            
            // minimax
            if (minimax(ret.score, bestScore, player)) {
                bestScore = ret.score;
                if (current_depth == 0) {
                    bestPoint = tmpMove;
                }
            }
            
            //undo move
            player.setFakePosition(tmpPointStart);
            if (player == this) {
                resetLastPosition();
            }
            player.setAngle(startAngle);
            player.setSpeed(startSpeed);
            
            // alpha beta
            if (player!=this && bestScore<=alpha && current_depth!=0)
                break;
            if (player==this && bestScore>=beta && current_depth!=0)
                break;
        }
        
        // send result
        toSend = new Result();
        toSend.score = bestScore;
        if (player != this) {
            if (alpha < bestScore)
                toSend.alphaBeta = bestScore;
            else
                toSend.alphaBeta = alpha;
        } else {
            if (beta > bestScore)
                toSend.alphaBeta = bestScore;
            else
                toSend.alphaBeta = beta;
        }
        
        return toSend;
    }
    
    public void putLastPosition() {
       lastPosition.push(getPosition());
    }
    
    public Point getLastPosition() {
        if (lastPosition.size()>0)
            return (Point)lastPosition.peek();
        return null;
    }
    
    public Point resetLastPosition() {
        if (lastPosition.size()>0)
            return (Point)lastPosition.pop();
        return null;
    }
    
    /** Gets really valid moves */
    private Vector cleanValidMoves(Game g, Vector moves) {
        Point start = getPosition();
        if (start != null) {
            Point moveTmp;
            int movesSize = moves.size();
            for (int i = 0; i < movesSize; i++) {
                moveTmp = (Point)moves.get(i);
                if (!g.getCircuit().isInside(moveTmp) || g.getCircuit().crossedBorder(start, moveTmp)) {
                    moves.remove(i);
                    movesSize--;
                    i--;
                }
            }
        }
        return moves;
    }
    
    private double getBestAngle(Point[] e, Point position) {
        if (position != null) {
            // finding the closest point
            double d = -1;
            double dTmp = -1;
            double angle,angleTmp;
            int dx, dy;
            int segmentStart = 0;
            int[] segmentEnd = new int[2];
            for (int i = 0; i < e.length; i++) {
                // distance to node i
                dx = e[i].x-position.x;
                dy = e[i].y-position.y;
                dTmp = dx*dx+dy+dy;
                
                if (d == -1 || d < dTmp) {
                    segmentStart = i;
                }
            }
            if (e.length > 1) {
                if (segmentStart == 0) {
                    segmentEnd[0] = 1;
                    segmentEnd[1] = e.length;
                } else if (segmentStart == (e.length-1)) {
                    segmentEnd[0] = 0;
                    segmentEnd[1] = segmentStart-1;
                } else {
                    segmentEnd[0] = segmentStart+1;
                    segmentEnd[1] = segmentStart-1;
                }
                angle = Game.getAngle(e[segmentEnd[0]].x-e[segmentStart].x, e[segmentEnd[0]].y-e[segmentStart].y);
                angleTmp = Game.getAngle(e[segmentEnd[1]].x-e[segmentStart].x, e[segmentEnd[1]].y-e[segmentStart].y);
                if (angle-getAngle() > angleTmp-getAngle()) {
                    angle = angleTmp;
                }
                return angle;
            }
            return getAngle();
        }
        return 0;
    }
    
    /** Class to save the result of the explore method */
    private class Result {
        public int score;
        public int alphaBeta;
    }
}
