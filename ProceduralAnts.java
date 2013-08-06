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
	 * DONE: simplify ant management, reduce use of iMin and iMax.
	 * DONE: create structures for (a) doing multiple runs, (b) accumulating counts, (c) presenting results. 
	 * TODO: Introduce timeToCollision and variable-sized time steps
	 * 
	 * @param args command-line arguments
	 */

	public static void main(String[] args) {
		int numberOfAnts = 7;		// Number of ants to simulate
		double stickLength = 100.0;	// Default: 100 cm.
		double antSpeed = 1.0;		// Default: 1.0 cm./sec.
		int seed = 2177;			// Seed value for Random number generator
		int nRuns = 3000;			// Number of simulation runs to accumulate

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
			if( args.length >= 4 ) {
				seed = Integer.parseInt(args[3]);
			}
			if( args.length >= 5 ) {
				nRuns = Integer.parseInt(args[4]);
			}
		} catch( Exception e ) {
			System.out.format("Could not parse command line: %s\n", e);
			System.out.println("Usage: ProceduralAnts [numberOfAnts] [stickLength] [antSpeed] [seed] [# runs]");
		}

		int debugLevel = 0;
		Random r = new Random(seed);

		// -------------------------------------------------------------------------
		// Creating and capturing statistics
		//
		// For a single run:
		// we create a set of ants with random velocities and place them
		// at random positions on the meter stick, then let them run until they
		// have all fallen off. For each ant we compare its initial to its final
		// velocity.
		//
		// After each run:
		// we add this run's velocity comparison results to an accumulator.
		//
		// At the end of N runs:
		// we report (velocity accumulator / N ), which is the observed probability
		// that an ant will fall off the same end of the stick that it started towards,
		// averaged over N runs.

		// int nGroups = (int)Math.round(Math.sqrt(nRuns));
		// int nSimulationsPerGroup = nRuns;
		// or
		int nGroups = 1;
		int nSimulationsPerGroup = nRuns;
		// or
		// int nGroups = 10;
		// int nSimulationsPerGroup = (int)Math.round(nRuns/(1.0*nGroups));
		
		System.out.format("\n*** Starting %d sets of %d simulation runs ***\n", nGroups, nSimulationsPerGroup);

												// special
		double[] probabilityAccumulator = new double[numberOfAnts]; // special
		for( int i=0; i<numberOfAnts; i++ ) { 						// special
			probabilityAccumulator[i]=0;							// special
		}															// special
		for( int j=0; j<nGroups; j++) {							// special

			double[] velocityAccumulator = new double[numberOfAnts];
			for( int i=0; i<numberOfAnts; i++ ) { 
				velocityAccumulator[i]=0;
			}

			for( int runIndex=0; runIndex<nSimulationsPerGroup; runIndex++ ) {
				int[] vp = doRun(numberOfAnts, stickLength, antSpeed, 
						debugLevel, r);
				for( int i=0; i<numberOfAnts; i++ ) {
					velocityAccumulator[i] += (double)vp[i];
				}
			}
			System.out.format("\nFinished %d runs with %d ants\n", nSimulationsPerGroup, numberOfAnts);
			System.out.format("Printing the probability that an ant falls off the same end of the stick\nthat it faced initially\n");
			System.out.format("ant  probability\n");
			int i=0;
			for( double v: velocityAccumulator ) {
				double probabilityAntSameSide = v/(double)nSimulationsPerGroup;
				System.out.format(" %2d  %5.3f\n", i++, probabilityAntSameSide);
			}

			for( int k=0; k<numberOfAnts; k++ ) {						// special
				probabilityAccumulator[k] += velocityAccumulator[k]/nSimulationsPerGroup;	// special
			}															// special

		}															// special

		System.out.format("\n >> After %d sets of %d runs/group (a total of %d runs), the averages of the probabilities are:\n\n",
				nGroups, nSimulationsPerGroup, nGroups*nSimulationsPerGroup);
		for( int k=0; k<numberOfAnts; k++ ) {						// special
			System.out.format(" %2d.   %6.4f\n", 					// special
					k, probabilityAccumulator[k]/nGroups);			// special
		}
	}

	/**
	 * @param numberOfAnts
	 * @param stickLength
	 * @param antSpeed
	 * @param debugLevel
	 * @param r
	 */
	public static int[] doRun(int numberOfAnts, double stickLength,
			double antSpeed, int debugLevel, Random r) {

		// Create the ants
		double[] antPositions = initializePositions(stickLength, numberOfAnts, r);
		double[] antVelocities = initializeVelocities(antSpeed, numberOfAnts, r);
		double[] originalVelocities = antVelocities.clone();

		if( debugLevel >= 2 ) {
			// Let's see what we've got
			System.out.format("\nSimulation: %d ants with speed +/- %.1f cm./sec. on a %3.0f cm. stick\n",
					numberOfAnts, antSpeed, stickLength);
			printAnts(antPositions, antVelocities);
		}

		// Start the simulation
		double timeStep = 0.5 * antSpeed ;

		doSimulation(timeStep, numberOfAnts, stickLength, antSpeed,
				debugLevel, antPositions, antVelocities);

		// Report final positions and velocities
		if( debugLevel >= 2 ) {
			System.out.format("\nSimulation completed:\n");
			printAnts(antPositions, antVelocities);
		}

		// Report original vs. final velocities
		int[] velocitiesEqual = new int[numberOfAnts];
		if( debugLevel >= 1 ) {
			System.out.format("\n  #  orig   new   equal?\n");
		}
		for(int iv=0; iv<numberOfAnts; iv++) {
			double origV = originalVelocities[iv];
			double endV = antVelocities[iv];
			int velocityDiff = (origV==endV)?1:0;
			velocitiesEqual[iv] = velocityDiff;
			if( debugLevel >= 1 ) {
				System.out.format(" %2d  %4.1f  %4.1f  %2d\n",
						iv, originalVelocities[iv], antVelocities[iv], velocityDiff);
			}
		}
		return velocitiesEqual;
	}

	/**
	 * @param numberOfAnts
	 * @param stickLength
	 * @param antSpeed
	 * @param debugLevel
	 * @param antPositions
	 * @param antVelocities
	 * @param iMin
	 * @param iMax
	 * @param timeStep
	 */
	public static void doSimulation(double timeStep, int numberOfAnts, double stickLength,
			double antSpeed, int debugLevel,
			double[] antPositions, double[] antVelocities) {

		int iMin = 0;
		int iMax = numberOfAnts - 1;
		double maxAntLifetime = stickLength / antSpeed ; // Maximum possible duration of simulation, in seconds

		if( debugLevel >= 3 ) {
			System.out.format("\n    Time    Ant positions    iMin..iMax\n");
		}

		for( double simTime = 0.0; simTime < maxAntLifetime; simTime += timeStep ) {

			if( debugLevel >= 3 ) {
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
			System.out.format(" %2d      %5.1f   %5.1f\n", i, antPositions[i], antVelocities[i]);
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
	public static double[] initializePositions(double stickLength, int numberOfAnts, Random r) {
		// Initialize positions
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
	public static double[] initializeVelocities(double antSpeed, int numberOfAnts, Random r) {
		// Initialize velocities
		double[] retval = new double[numberOfAnts];
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
