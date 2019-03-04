import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Simulation {
    private HashMap<String, CellularAutomaton> cells, nextTimeStep;
    private int height, width, contagiousRadius, daysToDeath;
    private Neighborhood model;
    double infectionChance, contagionDecayRate, removalDecayRate;

    // Parameters:
    // w = width of simulation space
    // h = height of simulation space
    // r = radius of effect of infection
    // deathTime = max # of days a person can go without gaining immunity before a death
    // neighborhood = neighborhood model selection
    // infectionProbability = the probability of any one cellular automaton to cause an infection on any other automaton
    // contagionDecay = the decay rate of how effective the infection is on the radii of the neighborhood model
    // removalDecay = the decay rate of a cellular automaton's chance of becoming removed over each time step
    Simulation(int w, int h, int r, int deathTime, Neighborhood neighborhood,
               double infectionProbability, double contagionDecay, double removalDecay){
        height = h;
        width = w;
        contagiousRadius = r;
        cells = new HashMap<>();
        nextTimeStep = new HashMap<>();
        model = neighborhood;
        daysToDeath = deathTime;
        infectionChance = infectionProbability;
        contagionDecayRate = contagionDecay;
        removalDecayRate = removalDecay;
    }

    // Constant resistance chance for entire population
    public void populateCells(double initialInfectionPercentage, double populationResistance){
        Random rand = new Random();
        boolean infected;
        Cell cell = new Cell(0,0);
        for(int y=0; y<height; y++){
            for(int x=0; x<width; x++){
                infected = (rand.nextDouble() < initialInfectionPercentage);
                cell.setValues(x, y);
                cells.put(cell.getAsKey(), new CellularAutomaton(infected, populationResistance));
            }
        }
    }

    // Gaussian distribution with standard deviation skewed towards target resistance chance for the entire population
    // Generally target resistance should be between 0 and 1 exclusive
    // Depending on the standard deviation and target resistance picked, there may be many completely resistant or
    // unresistant cellular automatons. The boolean allows for a re-roll of such resistance values, but this will cause
    // a higher chance of automatons having a resistance close to the target even if the standard deviation is wide.
    public void populateCells(
            double initialInfectionPercent,
            double targetResistance,
            double standardDeviation,
            boolean reroll){

        Random rand = new Random();
        boolean infected;
        double resistance;
        Cell cell = new Cell(0,0);
        if(targetResistance > 0 || targetResistance < 1) {
            for (int y=0; y<height; y++) {
                for (int x=0; x<width; x++) {
                    infected = (rand.nextDouble() < initialInfectionPercent);
                    resistance = skewedGaussianDistributionRoll(targetResistance, standardDeviation);
                    while(resistance == 0 || resistance == 1 && reroll){
                        resistance = skewedGaussianDistributionRoll(targetResistance, standardDeviation);
                    }
                    cell.setValues(x, y);
                    cells.put(cell.getAsKey(), new CellularAutomaton(infected, resistance));
                }
            }
        }
    }

    // Returns a random number from a Gaussian distribution skewed at the target with the specified standard deviation.
    private double skewedGaussianDistributionRoll(double target, double standardDeviation){
        Random rand = new Random();
        return Math.max(0.0, Math.min(1.0, target + rand.nextGaussian()*standardDeviation));
    }

    // Uniform distribution resistance probability across the entire population.
    public void populateCells(double initialInfectionPercentage){
        Random rand = new Random();
        boolean infected;
        double resistance;
        Cell cell = new Cell(0,0);
        for(int y=0; y<height; y++){
            for(int x=0; x<width; x++){
                infected = (rand.nextDouble() < initialInfectionPercentage);
                resistance = rand.nextDouble();
                cell.setValues(x, y);
                cells.put(cell.getAsKey(), new CellularAutomaton(infected, resistance));
            }
        }
    }

    // Returns a list of neighbors to the selected cell ordered from inner radius neighbors to outer radius neighbors.
    private ArrayList<CellularAutomaton> getNeighbors(Cell cell, int radius){
        ArrayList<CellularAutomaton> neighbors = new ArrayList<>();

        // Limit the radius to smaller dimension of the simulation space.
        if(radius > width/2){ radius = width/2; }
        if(radius > height/2){ radius = height/2; }

        // Get the neighbors within each radius of the desired neighborhood model in order from inner to outer radius.
        for(int r=1; r<=radius; r++){
            if(model == Neighborhood.VonNeumann){ neighbors.addAll(getVonNeumannRadius(cell, r)); }
            else{ neighbors.addAll(getMooreRadius(cell, r)); }
        }

        return neighbors;
    }

    // Get all the neighbors along the specified Von Neumann radius.
    private ArrayList<CellularAutomaton> getVonNeumannRadius(Cell cell, int radius){
        ArrayList<CellularAutomaton> radiusCells = new ArrayList<>();

        // Wrap around the simulation space when necessary.
        int xStart = (cell.x-radius < 0) ? width+cell.x-radius:cell.x-radius;
        int yStart = cell.y;
        Cell startCell = new Cell(xStart, yStart);

        int xTarget = (cell.x+radius > width-1) ? cell.x+radius-width:cell.x+radius;
        int yUpperTarget = (cell.y+radius > height-1) ? cell.y+radius-height:cell.y+radius;
        int yLowerTarget = (cell.y-radius < 0) ? height+cell.y-radius:cell.y-radius;

        // Traverse Von Neumann radius
        Cell currentCell = new Cell(xStart, yStart);
        // Increment x and y and wrap around simulation space when necessary.
        while(currentCell.y != yUpperTarget){
            radiusCells.add(cells.get(currentCell.getAsKey()));
            currentCell.x = (currentCell.x+1 > width-1) ? 0:currentCell.x+1;
            currentCell.y = (currentCell.y+1 > height-1) ? 0:currentCell.y+1;
        }
        // Increment x and decrement y and wrap around simulation space when necessary.
        while(currentCell.x != xTarget){
            radiusCells.add(cells.get(currentCell.getAsKey()));
            currentCell.x = (currentCell.x+1 > width-1) ? 0:currentCell.x+1;
            currentCell.y = (currentCell.y-1 < 0) ? height-1:currentCell.y-1;

        }
        // Decrement x and y and wrap around simulation space when necessary.
        while(currentCell.y != yLowerTarget){
            radiusCells.add(cells.get(currentCell.getAsKey()));
            currentCell.x = (currentCell.x-1 < 0) ? width-1:currentCell.x-1;
            currentCell.y = (currentCell.y-1 < 0) ? height-1:currentCell.y-1;
        }
        // Decrement x and increment y and wrap around simulation space when necessary.
        while(!currentCell.equals(startCell)){
            radiusCells.add(cells.get(currentCell.getAsKey()));
            currentCell.x = (currentCell.x-1 < 0) ? width-1:currentCell.x-1;
            currentCell.y = (currentCell.y+1 > height-1) ? 0:currentCell.y+1;
        }

        return radiusCells;
    }

    // Get all neighbors along the specified Moore radius.
    private ArrayList<CellularAutomaton> getMooreRadius(Cell cell, int radius){
        ArrayList<CellularAutomaton> radiusCells = new ArrayList<>();

        // Wrap around the simulation space when necessary.
        int xStart = (cell.x-radius < 0) ? width+cell.x-radius:cell.x-radius;
        int yStart = (cell.y-radius < 0) ? height+cell.y-radius:cell.y-radius;
        int xTarget = (cell.x+radius > width-1) ? cell.x+radius-width:cell.x+radius;
        int yTarget = (cell.y+radius > height-1) ? cell.y+radius-height:cell.y+radius;

        // Traverse Moore radius
        Cell currentCell = new Cell(xStart, yStart);
        // Increment x and wrap around simulation space when necessary.
        while(currentCell.x != xTarget){
            radiusCells.add(cells.get(currentCell.getAsKey()));
            currentCell.x = (currentCell.x+1 > width-1) ? 0:currentCell.x+1;
        }
        // Increment y and wrap around simulation space when necessary.
        while(currentCell.y != yTarget){
            radiusCells.add(cells.get(currentCell.getAsKey()));
            currentCell.y = (currentCell.y+1 > height-1) ? 0:currentCell.y+1;
        }
        // Decrement x and wrap around simulation space when necessary.
        while(currentCell.x != xStart){
            radiusCells.add(cells.get(currentCell.getAsKey()));
            currentCell.x = (currentCell.x-1 < 0) ? width-1:currentCell.x-1;
        }
        // Decrement y and wrap around simulation space when necessary.
        while(currentCell.y != yStart){
            radiusCells.add(cells.get(currentCell.getAsKey()));
            currentCell.y = (currentCell.y-1 < 0) ? height-1:currentCell.y-1;
        }

        return radiusCells;
    }

    // Returns a hash map of the state of all the cellular automatons for the next time step.
    public void getNextTimeStep(){
        Cell currentCell = new Cell(0,0);
        CellularAutomaton automaton;
        for(int y=0; y<height; y++){
            for(int x=0; x<width; x++){
                currentCell.setValues(x, y);
                automaton = cells.get(currentCell.getAsKey()).getNextState(
                        getNeighbors(currentCell, contagiousRadius),
                        model,
                        infectionChance,
                        contagionDecayRate,
                        removalDecayRate,
                        daysToDeath);

                nextTimeStep.put(currentCell.getAsKey(), automaton);
            }
        }
    }

    // Updates the hash map to the specified hashmap.
    // Generally this should be the hash map returned by the above getNextTimeStep() method.
    public void updateTimeStep(){ cells = nextTimeStep; }

    // Get final results of simulation.
    public HashMap<String, Integer> getStatistics(){
        Cell currentCell = new Cell(0,0);
        HashMap<String,Integer> finalCounts = new HashMap<>();

        int infectionCount, deathCount, removalCount;
        infectionCount = deathCount = removalCount = 0;
        for(int y=0; y<height; y++){
            for(int x=0; x<width; x++){
                currentCell.setValues(x, y);
                if(cells.get(currentCell.getAsKey()).contagious){ infectionCount++; }
                if(cells.get(currentCell.getAsKey()).dead){ deathCount++; }
                if(cells.get(currentCell.getAsKey()).removed){ removalCount++; }
            }
        }

        finalCounts.put("Infections", infectionCount);
        finalCounts.put("Deaths", deathCount);
        finalCounts.put("Removed Cells", removalCount);

        return finalCounts;
    }
}