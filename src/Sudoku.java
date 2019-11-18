import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Sudoku field.
 * @author Manuel Liebchen (hallo@manuelliebchen.de)
 */
public class Sudoku {
	int size;
	int sizeSquare;

	/**
	 * Numbers in the sudoku.
	 */
	int[][] sudoku;

	/**
	 * Locked fields in the sudoku.
	 */
	boolean[][] locked;

	
	/**
	 * Constructs a full sudoku field without empyt fields.
	 * @param size of a block
	 */
	Sudoku(int size, Random rand) {
		sizeSquare = size * size;
		this.size = size;
		boolean reset = false;
		
		do {
			reset = false;
			sudoku = new int[sizeSquare][sizeSquare];

			locked = new boolean[this.sizeSquare][this.sizeSquare];
			for (int x = 0; x < sudoku.length; x++) {
				for (int y = 0; y < sudoku[0].length; y++) {
					this.locked[x][y] = true;
				}
			}

			for (int x = 0; x < sudoku.length && !reset; x++) {
				for (int y = 0; y < sudoku[0].length && !reset; y++) {
					Field field = new Field(x,y);
					int counter = -1;
					do {
						counter++;
						sudoku[x][y] = rand.nextInt(sizeSquare);
						if (counter >= Math.pow(2, sizeSquare)) {
							reset = true;
							break;
						}
					} while (!checkConditions(field, sudoku[x][y]));
					locked[x][y] = false;
				}
			}
		} while (reset);
	}

	/**
	 * Copy constructor.
	 */
	Sudoku(Sudoku tobecopied) {
		this.sizeSquare = tobecopied.sizeSquare;
		this.size = tobecopied.size;

		this.sudoku = new int[sizeSquare][sizeSquare];

		locked = new boolean[sizeSquare][sizeSquare];

		for (int x = 0; x < sudoku.length; x++) {
			for (int y = 0; y < sudoku[0].length; y++) {
				this.sudoku[x][y] = tobecopied.sudoku[x][y];
				this.locked[x][y] = tobecopied.locked[x][y];
			}
		}
	}
	
	boolean checkConditions(Field field, int number) {
		int block = field.getBlock(size);
		for(int i = 0; i < sizeSquare; ++i) {
			if(!locked[field.x][i] && sudoku[field.x][i] == number) {
				return false;
			}
			if(!locked[i][field.y] && sudoku[i][field.y] == number) {
				return false;
			}
			Field blockfield = new Field(block, i, size);
			if(!locked[blockfield.x][blockfield.y] && sudoku[blockfield.x][blockfield.y] == number) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Get possible numbers for given field.
	 * @return List of these numbers.
	 */
	List<Integer> getOptions(Field field) {
		List<Integer> posibles = new ArrayList<>(sizeSquare);
		for (int i = 0; i < sizeSquare; ++i) {
			if (checkConditions(field, i)) {
				posibles.add(i);
			}
		}
		return posibles;
	}

	/**
	 * Get fields where a unique move is possible
	 */
	List<Field> getUniqueMoves() {
		List<Field> field = new ArrayList<>();
		for (int x = 0; x < sizeSquare; x++) {
			for (int y = 0; y < sizeSquare; y++) {
				Field move = new Field(x,y);
				if (locked[move.x][move.y]) {
					if(getOptions(move).size() == 1) {
						field.add(move);
					}
				}
			}
		}
		return field;
	}
	
	/**
	 * @return List of locked fields.
	 */
	List<Field> getLockedFields() {
		List<Field> fields = new ArrayList<>();
		for (int x = 0; x < sizeSquare; x++) {
			for (int y = 0; y < sizeSquare; y++) {
				if(locked[x][y]) {
					fields.add(new Field(x,y));
				}
			}
		}
		return fields;
	}
	
	/**
	 * Deletes fields of the sudoku to make it a puzzel.
	 * @param rand Random object to be used.
	 * @param tries of how often should it try to delete a field
	 * @return
	 */
	boolean[][] aussortieren(Random rand, int tries) {
		Field nextmove = null;
		int count = 0;
		do {
			int x, y;
			do {
				x = rand.nextInt(sizeSquare);
				y = rand.nextInt(sizeSquare);
			} while (locked[x][y]);
			nextmove = new Field(x, y);
			
			Sudoku copy = new Sudoku(this);
			copy.locked[nextmove.x][nextmove.y] = true;
			if (!copy.uniqueSolvable()) {
				count++;
			} else {
				locked[nextmove.x][nextmove.y] = true;
			}
		} while (count < tries);
		return locked;
	}

	/**
	 * Checks whether or not the sudoku is solvable by which every move is unique.
	 */
	boolean uniqueSolvable() {
		Sudoku copy = new Sudoku(this);
		if (copy.getLockedFields().isEmpty()) {
			return true;
		}
		
		List<Field> moves = new ArrayList<>();
		boolean fieldsLeft;
		do {
			fieldsLeft = false;
			moves.clear();
			moves = copy.getUniqueMoves();
			fieldsLeft = !copy.getLockedFields().isEmpty();
			if (moves.isEmpty() && fieldsLeft) {
				return false;
			}

			for (Field move : moves) {
				copy.locked[move.x][move.y] = false;
			}
		} while (fieldsLeft);
		return true;
	}

	/**
	 * Renders the sudoku with a given Graphics objekt
	 * @param g
	 * @param cellSize
	 */
	void render(Graphics g, float cellSize, Font font) {
		float sudokuSize = cellSize * sizeSquare;
		int sizeSquar = sizeSquare;
		int pixelSize = (int) Math.ceil(cellSize / 60);
		int offset = Math.round(14 * pixelSize);
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

		g.setColor(Color.BLACK);

		((Graphics2D) g).setStroke(new BasicStroke(Math.round(3 * pixelSize)));
		g.drawRect(0, 0, Math.round(sudokuSize), Math.round(sudokuSize));
		((Graphics2D) g).setStroke(new BasicStroke(Math.round(2 * pixelSize)));
		int size = (int) Math.sqrt(sizeSquar);
		for (int i = 0; i < sizeSquar - 1; ++i) {
			if (i % size == 2) {
				((Graphics2D) g).setStroke(new BasicStroke(Math.round(2 * pixelSize)));
			} else {
				((Graphics2D) g).setStroke(new BasicStroke(Math.round(1 * pixelSize)));
			}
			g.drawLine(0, Math.round((i + 1) * cellSize), Math.round(sudokuSize), Math.round((i + 1) * cellSize));
			g.drawLine(Math.round((i + 1) * cellSize), 0, Math.round((i + 1) * cellSize), Math.round(sudokuSize));
		}
		for (int i = 0; i < sizeSquar; ++i) {
		}

		g.setFont(font);
		for (int i = 0; i < sudoku.length; ++i) {
			for (int j = 0; j < sudoku.length; ++j) {
				if (!locked[j][i]) {
					g.drawString(String.valueOf(sudoku[j][i] + 1), Math.round(cellSize * i + offset),
							Math.round(cellSize * (j + 1) - offset));
				}
			}
		}
	}

	@Override
	public String toString() {
		String output = "";
		for(int k = 0; k < sizeSquare * 2 +1; ++k) {
			output += "-";
		}
		output += "\n";
		for (int i = 0; i < sudoku.length; ++i) {
			output += "|";
			for (int j = 0; j < sudoku[i].length; ++j) {
				String fill = j % size == 2 ? "|" : " ";
				if (!locked[i][j]) {
					output += sudoku[i][j] + 1 + fill;
				} else {
					output += " " + fill;
				}
			}
			output += "\n";
			if(i % size == 2) {
				for(int k = 0; k < sizeSquare * 2 +1; ++k) {
					output += "-";
				}
				output += "\n";
			}
		}
		return output;
	}
}
