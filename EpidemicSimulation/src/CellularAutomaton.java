import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import static java.lang.Math.pow;

enum Neighborhood {VonNeumann, Moore}

public class CellularAutomaton {
    boolean contagious, removed, dead;
    int daysInfected;
    double resistance;

    CellularAutomaton(boolean infected, double removalChance){
        Random rand = new Random();

        resistance = removalChance;
        contagious = infected;
        removed = (rand.nextDouble() < resistance);
        dead = false;
    }

    CellularAutomaton(CellularAutomaton source){
        this.contagious = source.contagious;
        this.removed = source.removed;
        this.dead = source.dead;
        this.daysInfected = source.daysInfected;
        this.resistance = source.resistance;
    }

    // Update cell's state based on if the cells neighbors are contagious or not.
    // Generally, contagion decay and removal decay should be in the range of 0 and 1 exclusive.
    // If the decay values are set to 1, no decay occurs and the chance of infection or immunity stays the same.
    // If the decay values are set to less than or equal to 0 or greater than 1, standard exponential decay is used.
    // If the decay values are within the exclusive range of 0 and 1, the chance of infection or removal is multiplied
    // by the corresponding decay factor each calculation iteration.
    // Days to death is used to specify how many days a cell has to be removed before it dies.
    public CellularAutomaton getNextState(
            ArrayList<CellularAutomaton> neighbors,
            Neighborhood neighborhood,
            double baseInfectionChance,
            double contagionDecay,
            double removalDecay,
            int daysToDeath){

        // Make copy of current automaton.
        CellularAutomaton copy = new CellularAutomaton(this);

        // Variables used to determine when decay should occur depending on neighborhood model.
        int accumulator = 0;
        int radius = 1;
        int count = 0;

        // Radius multiplier represents either Von Neumann or Moore neighborhood model.
        int radiusMultiplier;
        // Von Neumann neighborhood population = sum(radius*4) over radius from 0 to n
        if(neighborhood == Neighborhood.VonNeumann){ radiusMultiplier = 4; }
        // Moore neighborhood population = sum(radius*8) over radius from 0 to n
        else{ radiusMultiplier = 8; }

        Random rand = new Random();
        // Only check if neighbors infect this cell if this cell is not already infected.
        if(!contagious) {
            Iterator it = neighbors.iterator();
            // Iterate over all neighbors.
            while (it.hasNext()) {
                CellularAutomaton neighbor = (CellularAutomaton) it.next();

                // Check if this cell gets infected by contagious neighbor cell.
                if (neighbor.contagious){ copy.contagious = (rand.nextDouble() < baseInfectionChance); }
                // If cell becomes infected, wait until next time step to update state again.
                if (copy.contagious){ return copy; }

                // Test if the next neighborhood radius is reached
                if (count == radius*radiusMultiplier + accumulator) {
                    // Neighbors farther away have less chance to infect this cell unless contagion decay factor is 1.
                    // If decay factor is outside the range 0 exclusive and 1 inclusive, use standard exponential decay.
                    if(contagionDecay > 0 && contagionDecay <= 1){
                        baseInfectionChance = baseInfectionChance * pow(contagionDecay, (double) radius);
                    } else { baseInfectionChance *= baseInfectionChance; }
                    radius++;
                    accumulator += count;
                }
                count++;
            }
        }
        // If the cell is already infected, check if immunity is gained.
        else if(!dead && !removed){
            copy.daysInfected++;
            // Chance of immunity decreases with days infected unless immunity decay factor is 1
            // If decay factor is not given a value between 0 exclusive and 1 inclusive use standard exponential decay
            if(removalDecay > 0 && removalDecay <= 1) {
                copy.removed = (rand.nextDouble() < resistance * pow(removalDecay, (double) copy.daysInfected));
            } else { copy.removed = (rand.nextDouble() < pow(resistance, (double) copy.daysInfected));}

            // Cell dies after X days of not gaining immunity.
            if(copy.daysInfected >= daysToDeath){ copy.dead = true; }
        }
        return copy;
    }
}