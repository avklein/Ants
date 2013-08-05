package org.avk.ants;
import java.lang.Math;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

public class ProceduralAnts {

	/**
	 * ProceduralAnts uses procedures to modularize the code,
	 * unlike SimplestAnts, which is like a big ball of mud.
	 * 
	 * DONE: ensure that each ant has a unique position.
	 * TODO: simplify ant management, reduce use of iMin and iMax.
	 * TODO: Introduce timeToCollision and variable-sized time steps
	 * 
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
		int numberOfAnts = 6;		// Arbitrary default value
		double stickLength = 100.0;	// Default: 100 cm.
		double antSpeed = 1.0;		// Default: 1.0 cm./sec.

		try {
			if( args.length >= 1 ) {
				numberOfAnts = Integer.parseInt(args[0]);
			}
			if( args.length >= 2 ) {
				stickLength = Double.parseDouble(args[1]);
			}
			if( args.length >= 3 ) {
				antSpeed = Double.parseDouble(args[2]);
			}
		} catch( Exception e ) {
			System.out.format("Could not parse command line: %s\n", e);
			System.out.println("Usage: ProceduralAnts [numberOfAnts] [stickLength] [antSpeed]");
		}

		// For production:
		double runLength = stickLength / antSpeed ; // Duration of simulation, in seconds
		// For testing only, use a shorter runLength
		// double runLength = 20;

		Boolean printAllAntSteps = false;
		int iMin = 0;
		int iMax = numberOfAnts - 1;

		// Create the ants
		double[] antPositions = initializePositions(stickLength, numberOfAnts);
		double[] antVelocities = initializeVelocities(antSpeed, numberOfAnts);
		double[] originalVelocities = antVelocities;

		// Let's see what we've got
		System.out.format("There are %d ants with speed +/- %.1f cm./sec. on a %3.0f cm. stick\n",
				numberOfAnts, antSpeed, stickLength);
		
		if( printAllAntSteps ) {
			printAnts(antPositions, antVelocities);
		}

		// Start the simulation
		double timeStep = 0.5 * antSpeed ;
		if( printAllAntSteps ) {
			System.out.format("\n    Time    Ant positions    iMin..iMax\n");
		}

		for( double simTime = 0.0; simTime < runLength; simTime += timeStep ) {

			if( printAllAntSteps ) {
				printAntStep(numberOfAnts, iMin, iMax, antPositions, simTime);
			}
			for( int i=iMin; i<=iMax; i++ ) {
				// Update this Ant's position
				double newPosition = antPositions[i] + timeStep*antVelocities[i];
				antPositions[i] = newPosition;

				// Cases to consider
				if( newPosition < 0.0 ) {
					// This ant just fell off the left end of the stick.
					iMin++;
					// continue;
				} else if( newPosition > stickLength ) {
					// This ant just fell off the right end of the stick.
					iMax--;
					// continue;
				} else if( i > iMin ) {
					// There is an Ant to the left of this one;
					// check if the two of them have just collided
					// and reverse their directions if so.
					justCollided( i-1, i, antPositions, antVelocities );
				}
			}
			// Are there any active ants left? If not, we're done.
			if( iMin > iMax ) {
				break;
			}
		}
		if( printAllAntSteps) {
			System.out.format("\n");
		}

		// Report final positions and velocities
		System.out.format("\nSimulation completed\n");
		printAnts(antPositions, antVelocities);
	}

	/**
	 * @param numberOfAnts
	 * @param iMin
	 * @param iMax
	 * @param antPositions
	 * @param simTime
	 */
	public static void printAntStep(int numberOfAnts, int iMin, int iMax,
			double[] antPositions, double simTime) {
		System.out.format(" %6.1f:  ", simTime);
		for( int i=0; i<numberOfAnts; i++ ) {
			if( i<iMin || i>iMax ) {
				// Ants that dropped off the stick
				System.out.format("   ----");
			} else if( i<iMax
					&& Math.abs(antPositions[i+1]-antPositions[i])<0.001 ) {
				// Ants that collided
				System.out.format("  %5.1f==%5.1f",
						antPositions[i], antPositions[i+1]);
				i++;
			} else {
				// Active Ants
				System.out.format("  %5.1f", antPositions[i]);
			}
		}
		System.out.format("  %2d..%2d\n", iMin, iMax);
	}

	/**
	 * @param stickLength
	 * @param antSpeed
	 * @param antPositions
	 * @param antVelocities
	 */
	public static void printAnts(double[] antPositions, double[] antVelocities) {
		System.out.format("\n  #   position   speed\n");
		for( int i=0; i<antPositions.length; i++ ) {
			System.out.format(" %2d      %4.1f    %4.1f\n", i, antPositions[i], antVelocities[i]);
		}
	}

	/**
	 * Create an array of <code>numberOfAnts</code> ant positions,
	 * where the positions are randomly distributed in the range 
	 * <code>[0, stickLength)</code>,
	 * the positions are restricted to whole numbers,
	 * and each position must be unique.
	 * 
	 * @param stickLength in cm.
	 * @param numberOfAnts
	 * @return an array of ant positions
	 */
	public static double[] initializePositions(double stickLength, int numberOfAnts) {
		// Initialize positions
		Random r = new Random(211);
		Set<Double> uniquePositions = new TreeSet<Double>();
		while( uniquePositions.size() < numberOfAnts ) {
			uniquePositions.add( Math.rint(r.nextDouble()*stickLength) );
		}
		double[] retval = new double[numberOfAnts];
		int i=0;
		for( double z: uniquePositions ) {
			retval[i++] = z;
		}
		return retval;
	}
	/**
	 * Initialize the ant velocities
	 * 
	 * @param antSpeed in cm./sec.
	 * @param antVelocities
	 */
	public static double[] initializeVelocities(double antSpeed, int numberOfAnts) {
		// Initialize velocities
		double[] retval = new double[numberOfAnts];
		Random r = new Random(2113);
		for( int i=0; i<numberOfAnts; i++) {
			if( r.nextBoolean() ) {
				retval[i] = -antSpeed;
			} else {
				retval[i] = antSpeed;
			}
		}
		return retval;
	}

	public static boolean justCollided(int iLeft, int i, double[] antPositions, double[] antVelocities) {
		boolean retval = false;
		// Define epsX as the position difference that is
		// indistinguishable from zero.
		double epsX = 0.001;
		// Decide: after updating the positions of both ants, did they collide?
		if( Math.abs(antPositions[i] - antPositions[iLeft]) < epsX 
				&& (antVelocities[i] != antVelocities[iLeft])) {
			// A collision did occur.
			// Reverse the directions of the two Ants.
			antVelocities[i] *= -1.0 ;
			antVelocities[iLeft] *= -1.0 ;
			retval = true;
		}
		return retval;
	}
}
