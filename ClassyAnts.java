package org.avk.ants;

import java.lang.Math;
import java.util.ListIterator;
import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.avk.ants.Ant;

public class ClassyAnts {

  /**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if (args.length < 1) {
			System.out.println("Usage: ClassyAnts numberOfAnts [stickLength] [antSpeed]");
			return;
		}
		int numberOfAnts = Integer.parseInt(args[0]);
		double stickLength = 100.0;	// Default: 100 cm.
		double antSpeed = 1.0;		// Default: 1.0 cm./sec.
		double runLength = stickLength / antSpeed ; // Standard duration of simulation, in seconds
		// For testing only:
		// double runLength = 70;
		Boolean dbgFlag = false;
		int iMin = 0;
		int iMax = numberOfAnts - 1;
		
		List<Ant> theAnts = new ArrayList<Ant>();
		
		// Initialize positions and velocities
		Random r = new Random(31103);
		for( int i=iMin; i<=iMax; i++) {
			// We force the Ants to be positioned on whole numbers
			double position = Math.rint(r.nextDouble() * stickLength);
			double velocity = antSpeed;
			if( r.nextBoolean() ) {
				velocity = -antSpeed;
			}
			theAnts.add(new Ant(i, position, velocity));
		}
		Collections.sort(theAnts);

		// Let's see what we've got
		System.out.format("Created %d ants with velocity +/- %.1f cm./sec. on a %3.0f cm. stick\n",
				numberOfAnts, antSpeed, stickLength);
		System.out.format("\n  #   position   velocity\n");
		for (Ant a : theAnts) {
			System.out.format(" %s\n", a);
		}

		// Start the simulation
		double timeStep = 0.5 ;
		System.out.format("\n    Time    Ant positions");
		for( double simTime = 0.0; simTime < runLength; simTime += timeStep ) {
			
			// Print the current positions of the Ants
			System.out.format("\n %6.1f:  ", simTime);
			for (ListIterator<Ant> antIter = theAnts.listIterator(); antIter.hasNext();) {
				int ia = antIter.nextIndex();	// The index of the next Ant,
				Ant a = antIter.next();			// and the next Ant itself.
				if( !a.isAlive() ) {
					// Ants that dropped off the stick
					System.out.format(" ----   ");
				} else {
					// Active Ants
					System.out.format("%4.1f", a.getX());
					if( antIter.hasNext() ) {
						// Peek at the following Ant
						Ant b = antIter.next();
						antIter.previous();
						if( a.isTouching(b) ) {
							// These two Ants collided
							System.out.print(" <> ");
						} else {
							System.out.print("    ");
						}
					}
				}
			}
			
			// Stop the simulation once all of the Ants are gone
			if( iMin > iMax ) {
				break;
			}
			
			// Update the Ant positions
			for (ListIterator<Ant> antIter = theAnts.listIterator(); antIter.hasNext();) {
				
				// Get the Ant we want to update, as well as its index
				int ia = antIter.nextIndex();	// The index,
				Ant a = antIter.next();			// and the Ant itself.
				
				// Don't bother with Ants that are gone
				if( ia < iMin || ia > iMax || !a.isAlive() ) {
					continue;
				}
				
				// Update this Ant's position
				a.simpleMove(timeStep);
				if( dbgFlag ) {
					System.out.format("\n dbg Updated Ant %d to %4.1f", ia, a.getX());
				}
				
				// Did this Ant just fall off the left or right end of the stick?
				if( a.getX() < 0.0 ) {
					a.disable();
					iMin++;
					if( dbgFlag ) {
						System.out.format(" dbg iMin is now %d\n", iMin);
					}
					continue;
				}
				if( a.getX() > stickLength ) {
					a.disable();
					iMax--;
					if( dbgFlag ) {
						System.out.format(" dbg iMax is now %d\n", iMax);
					}
					continue;
				}
						
				// If it exists, access the previous Ant
				// and see if our move has caused a collision
				if( antIter.hasPrevious() && antIter.previousIndex() > 0) {
					int iPrevious = antIter.previousIndex()-1;
					Ant previousAnt = theAnts.get(iPrevious);
					if( a.isTouching(previousAnt) ) {
						// A collision did occur.
						// Reverse the directions of both Ants.
						a.reflect();
						previousAnt.reflect();
						if( dbgFlag ) {
							System.out.format("\n dbg reflected %d and %d\n", ia, iPrevious);
						}
					}
				}
			}
		}
	}
}
