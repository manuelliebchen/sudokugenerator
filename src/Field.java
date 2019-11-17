/**
 * Field in a sudoku.
 * @author Manuel Liebchen (hallo@manuelliebchen.de)
 */
public class Field {

	int x;
	int y;

	public Field(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	int getBlock(int size) {
		return y / size + x / (size * size);
	}
}
