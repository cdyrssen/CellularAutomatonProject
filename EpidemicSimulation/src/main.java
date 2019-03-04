import java.io.FileWriter;
import java.util.HashMap;

public class main {
    public static void main(String[] args){
        // Hash map to easily extract statistics about the simulation.
        HashMap<String, Integer> statistics;

        // Create a new simulation with the desired parameters.
        Simulation mySimulation = new Simulation(
                50, 50, 3, 5, Neighborhood.Moore,
                0.05, 0.5, 0.95);

        // Populate the simulation space using the desired populate function and parameters.
        mySimulation.populateCells(0.01, 0.25);

        // Get and print initial state of simulation.
        statistics = mySimulation.getStatistics();
        int initialInfections, initialDeaths, initialImmunities;
        initialInfections = statistics.get("Infections");
        initialDeaths = statistics.get("Deaths");
        initialImmunities = statistics.get("Removed Cells");
        System.out.println("Infections: "+initialInfections);
        System.out.println("Deaths: "+initialDeaths);
        System.out.println("Immunities: "+initialImmunities);
        System.out.println("*****************************************************************************************");
        System.out.println();

        // Variables to see state changes and check for termination criteria.
        boolean stable = false;
        int oldInfectionCount, newInfectionCount, oldDeathCount, newDeathCount, oldRemovedCount, newRemovedCount;
        int day, daysStable;
        day = daysStable = 0;
        oldInfectionCount = oldDeathCount = oldRemovedCount = 0;

        // Initialize file writer and string builder and add csv headers to statistics string.
        FileWriter fw;
        StringBuilder builder = new StringBuilder();
        String columnHeaders = "Infections, Deaths, Removed Cells";
        builder.append(columnHeaders+"\n");

        // Continue simulating until a steady state is reached.
        while(!stable){
            int infections, deaths, removedCells;
            // Get the next time step state of the simulation.
            mySimulation.getNextTimeStep();

            // Get statistics for the next day and update simulation.
            statistics = mySimulation.getStatistics();
            newInfectionCount = statistics.get("Infections");
            newDeathCount = statistics.get("Deaths");
            newRemovedCount = statistics.get("Removed Cells");

            // Update the current simulation state to the new state simulated and increase the time step.
            mySimulation.updateTimeStep();
            day++;

            // Update statistics.
            infections = newInfectionCount-oldInfectionCount;
            deaths = newDeathCount-oldDeathCount;
            removedCells = newRemovedCount-oldRemovedCount;

            // Print statistics.
            System.out.println("Time step: Day #"+day);
            System.out.println("Infections: "+infections);
            System.out.println("Deaths: "+deaths);
            System.out.println("Removed Cells: "+removedCells);
            System.out.println();

            // Add new statistics to csv string.
            builder.append(infections+", "+deaths+", "+removedCells+"\n");

            // Update counts for next iteration.
            oldInfectionCount = newInfectionCount;
            oldDeathCount = newDeathCount;
            oldRemovedCount = newRemovedCount;

            // Check if system is stable.
            if(infections == 0 && deaths == 0 && removedCells == 0){ daysStable++; }
            else{ daysStable = 0; }
            stable = (daysStable == 3);
        }

        // Print final counts of the states of the cellular automatons from the simulation.
        statistics = mySimulation.getStatistics();
        System.out.println("*****************************************************************************************");
        System.out.println("Infections: "+statistics.get("Infections"));
        System.out.println("Deaths: "+statistics.get("Deaths"));
        System.out.println("Immunity: "+statistics.get("Removed Cells"));

        // Write statistics out to csv file.
        try{
            fw = new FileWriter("statistics.csv");
            fw.write(builder.toString());
            fw.close();
        }catch(java.io.IOException e){
            e.printStackTrace();;
        }
    }
}
