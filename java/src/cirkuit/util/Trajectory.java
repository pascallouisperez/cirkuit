package cirkuit.util;

import java.util.Vector;

/**
 * @author Sven Gowal (sven.gowal@lha.ch)
 * @author Pascal Perez (pascal.perez@lha.ch)
 * @version 1.0
 */
public class Trajectory {
    Vector trajectory = null;
    double angle = 0;
    double speed = 0;
    
    /**
     * @param trajectory the trajectory
     * @param angle the angle
     */
    public Trajectory(Vector trajectory, double angle, double speed) {
        this.trajectory = trajectory;
        this.angle = angle;
        this.speed = speed;
    }
    
    /**
     * Gets the trajectory.
     * @return the trajectory
     */
    public Vector getTrajectory() {
        return trajectory;
    }
    
    /**
     * Gets the angle.
     * @return the angle
     */
    public double getAngle() {
        return angle;
    }
    
    /**
     * Gets the angle.
     * @return the angle
     */
    public double getSpeed() {
        return speed;
    }
}
