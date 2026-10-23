package battleship;

/**
 * The type Barge represents a ship with a size of 1 unit.
 * It is positioned on the game board based on its bearing and initial position.
 *
 * Author: britoeabreu
 * Date: 2023-10-10
 * Time: 15:30
 */
public class Barge extends Ship {

	/**
	 * Instantiates a new Barge.
	 *
	 * @param bearing The bearing of the barge (NORTH, SOUTH, EAST, or WEST).
	 * @param pos     The upper-left position of the barge on the game board.
	 */
	public Barge(Compass bearing, IPosition pos) {
		super("Barca", bearing, pos, 1);

		// Add the single position of the barge to the list of positions
		getPositions().add(new Position(pos.getRow(), pos.getColumn()));
	}
    public void myUselessLongMethodForQodana() {
        System.out.println("Line 1");
        System.out.println("Line 2");
        System.out.println("Line 3");
        System.out.println("Line 4");
        System.out.println("Line 5");
        System.out.println("Line 6");
        System.out.println("Line 7");
        System.out.println("Line 8");
        System.out.println("Line 9");
        System.out.println("Line 10");
        System.out.println("Line 11");
        System.out.println("Line 12");
        System.out.println("Line 13");
        System.out.println("Line 14");
        System.out.println("Line 15");
        System.out.println("Line 16");
        System.out.println("Line 17");
        System.out.println("Line 18");
        System.out.println("Line 19");
        System.out.println("Line 20");
        System.out.println("Line 21");
        System.out.println("Line 22");
        System.out.println("Line 23");
        System.out.println("Line 24");
        System.out.println("Line 25");
        System.out.println("Line 26");
        System.out.println("Line 27");
        System.out.println("Line 28");
        System.out.println("Line 29");
        System.out.println("Line 30");
        System.out.println("Line 31");
        System.out.println("Line 32");
    }
}