// This class to to store the location of a cellular automaton.
public class Cell {
    int x, y;

    Cell(int i, int j){
        setValues(i, j);
    }

    // Cell comparator.
    public boolean equals(Cell comparison){ return (comparison.x == x && comparison.y == y); }

    // Get the x and y coordinates as a key to hash.
    public String getAsKey(){ return ""+x+""+y; }

    // Set the x and y coordinates to the desired values.
    public void setValues(int i, int j){
        x = i;
        y = j;
    }
}
