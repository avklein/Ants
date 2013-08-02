package org.avk.ants;

/**
 * Implementation of a basic Ant.
 * Basic ants are point-like objects that live in a 1-dimensional space.
 * An Ant has an identifier (<code>name</code>), a location (<code>x</code>)
 * and a velocity (<code>v</code>). 
 * In the Ant simulation we will see that all Ants move at the same speed
 * (1 cm./sec.), either forward or backward.
 * 
 * Notes
 * This class represents velocity as a <code>double</code>, even though
 * the original problem only has velocities of +1 or -1.
 * 
 * @author Andrew Klein
 */

public class Ant implements Comparable<Ant> {
  private String name;
	private double x;
	private double v;
	private Boolean alive;
	// epsX and epsV are the position and
	// velocity differences that - for our purposes -
	// are indistinguishable from zero.
	static public final double epsX = 0.001;
	static public final double epsV = 0.001;
	
	/**
	 * Helper function for the Ant constructors
	 *
	 * @param x is the position
	 * @param v is the velocity
	 */
	private void initialize(double x, double v) {
		this.name = null;
		this.x = x;
		this.v = v;
		this.alive = true;
	}
	
	public Ant(double x, double v) {
		this.initialize(x,v);
	}
	public Ant(int id, double x, double v) {
		this.initialize(x,v);
		this.name = String.valueOf(id);
	}
	public Ant(String name, double x, double v) {
		this.initialize(x,v);
		this.name = name;
	}
	
	public void disable() {
		alive = false;
	}
	public void enable() {
		alive = true;
	}
	public Boolean isAlive() {
		return alive;
	}

	@Override
	public String toString() {
		String aliveLabel = " ";
		if( !isAlive() ) {
			aliveLabel = "dead";
		}
		try {
			int id = Integer.parseInt(name);
			return String.format("%2d      %4.1f    %4.1f %s", id, x, v, aliveLabel);
		} catch (NumberFormatException e) {
			return String.format("%s      %4.1f    %4.1f %s", name, x, v, aliveLabel);
		}
	}
	
	/**
	 * Enables us to sort Ants by location
	 */
	public int compareTo(Ant otherAnt) { 
		return (int)Math.signum(getX() - otherAnt.getX()); 
	}
	
	/**
	 * Updates the position of this ant, ignoring
	 * the possibility of a collision.
	 * 
	 * @param t is the time (in seconds) to allow the ant to move
	 */
	public void simpleMove(double t) {
		x = x + t*v;
	}
	
	public void reflect() {
		v = -v;
	}
	
	/**
	 * Is this Ant touching the other Ant?
	 * This is a part of determining if the two Ants have collided,
	 * but that question must also consider the velocities.
	 */
	public Boolean isTouching(Ant otherAnt) {
		return Math.abs(otherAnt.getX() - getX()) < epsX;
	}
	
	/**
	 * How long until these two Ants collide?
	 * 
	 * @param other another Ant object
	 * @return number of seconds until the two Ants collide.
	 * If ants are moving in opposite directions then
	 * the collision occurred in the past, so it is valid
	 * to return a negative number.
	 * If the ants are moving in the same direction then
	 * there won't be a collision. Indicate this by
	 * returning a large negative number.
	 */
	public double timeToCollision(Ant otherAnt) {
		double epsV = 0.001;
		double deltaV = otherAnt.getV() - getV() ;
		double deltaX = otherAnt.getX() - getX();
		if( Math.abs(deltaV) < epsV ) {
			// the ants are moving in the same direction
			return -100.0/epsV;
		} else {
			return -deltaX/deltaV;
		}
	}
	
	/**
	 * How long until this Ant reaches the given position?
	 * 
	 * @param position is a fixed location
	 * @return number of seconds until this Ant gets to
	 * the given position. Will be negative if Ant is moving
	 * away from the position.
	 */
	public double timeToPosition(double position) {
		return (position-getX())/getV() ;
	}
	
	public Boolean willNeverCollide(Ant otherAnt) {
		return Math.signum(getV()) == Math.signum(otherAnt.getV());
	}
	
	public Boolean willCollide(Ant otherAnt) {
		return timeToCollision(otherAnt) > 0.0;
	}
	
	/**
	 * Public setter for the name
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * Public getter and setter for the position
	 */
	public double getX() {
		return x;
	}
	public void setX(double newPosition) {
		this.x = newPosition;
	}
	/**
	 * Public getter for the velocity
	 */
	public double getV() {
		return v;
	}

}
