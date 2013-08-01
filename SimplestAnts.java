package org.avk.ants;
import java.lang.Math;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimplestAnts {

  /**
	 * SimplestAnts is a solution without much class
	 * 
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if (args.length < 1) {
			System.out.println("Usage: SimplestAnts numberOfAnts [stickLength] [antSpeed]");
			return;
		}
		int numberOfAnts = Integer.parseInt(args[0]);
		double stickLength = 100.0;	// Default: 100 cm.
		double antSpeed = 1.0;		// Default: 1.0 cm./sec.
		// double runLength = stickLength / antSpeed ; // Duration of simulation, in seconds
		// For testing only:
		double runLength = 50;
		int iMin = 0;
		int iMax = numberOfAnts - 1;
		double[] antPositions = new double[numberOfAnts];
		double[] antVelocities = new double[numberOfAnts];
		// Initialize positions and velocities
		for( int i=iMin; i<=iMax; i++) {
			antPositions[i] = Math.rint(Math.random() * stickLength);
			if( Math.random() < 0.5) {
				antVelocities[i] = -antSpeed;
			} else {
				antVelocities[i] = antSpeed;
			}
		}
		Arrays.sort(antPositions);
		
		// Let's see what we've got
		System.out.format("Created %d ants with speed +/- %.1f cm./sec. on a %3.0f cm. stick\n",
				numberOfAnts, antSpeed, stickLength);
		System.out.format("\n  #   position   speed\n");
		for( int i=iMin; i<=iMax; i++ ) {
			System.out.format(" %2d      %4.1f    %4.1f\n", i, antPositions[i], antVelocities[i]);
		}
		
		// Start the simulation
		double timeStep = 0.5 ;
		System.out.format("\n    Time    Ant positions");
		for( double simTime = 0.0; simTime < runLength; simTime += timeStep ) {
			System.out.format("\n %6.1f:  ", simTime);
			for( int i=0; i<numberOfAnts; i++ ) {
				if( i<iMin || i>iMax ) {
					// Ants that dropped off the stick
					System.out.format("  ----");
				} else if( i<iMax
						&& Math.abs(antPositions[i+1]-antPositions[i])<0.001 ) {
					// Ants that collided
					System.out.format("  %4.1f==%4.1f",
							antPositions[i], antPositions[i+1]);
					i++;
				} else {
					// Active Ants
					System.out.format("  %4.1f", antPositions[i]);
				}
				
			}
			for( int i=iMin; i<=iMax; i++ ) {
				double newPosition = antPositions[i] + timeStep*antVelocities[i];
				
				// Did the first Ant fall off the left end of the stick?
				if( i==iMin && newPosition <= 0.0 ) {
					iMin++;
					continue;
				} 
				
				// Update the Ant's position
				antPositions[i] = newPosition;
				if( i<iMax ) {
					// Examine the new positions of Ant[i] and Ant[i+1]
					// for possible collision
					double newPosition2 = antPositions[i+1] + timeStep*antVelocities[i+1];
					// Define epsX as the position difference that is
					// indistinguishable from zero.
					double epsX = 0.001;
					if( Math.abs(newPosition - newPosition2) < epsX ) {
						// A collision did occur.
						// Reverse the directions of the two Ants.
						antVelocities[i] *= -1.0 ;
						antVelocities[i+1] *= -1.0 ;
						// Update the position of the second Ant
						antPositions[i+1] = newPosition2;
						// Skip over both Ants
						i++;
						continue;
					}
				}
				
				// Did the last Ant fall off the right end of the stick?
				if( i==iMax && newPosition >= stickLength ) {
					iMax--;
					continue;
				}		
			}
		}
		System.out.format("\n");
	}
}
