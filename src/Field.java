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
	
	public Field(int block, int index, int size) {
		y = size * (block / size) + (index / size);
		x = size * (block % size) + (index % size);
	}
	
	int getBlock(int size) {
		return x / size + size * (y / size);
	}
	
	@Override
	public String toString() {
		return "Field[ " + String.valueOf(x) + ", " +  String.valueOf(y) + "]";
	}
}
