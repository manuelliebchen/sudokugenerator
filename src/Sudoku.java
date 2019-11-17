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
	 * Fields for row, column and block condition;
	 */
	boolean[][] rowCondition;
	boolean[][] columnCondition;
	boolean[][] blockCondition;

	
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
			rowCondition = new boolean[sizeSquare][sizeSquare];
			columnCondition = new boolean[sizeSquare][sizeSquare];
			blockCondition = new boolean[sizeSquare][sizeSquare];

			for (int x = 0; x < sudoku.length && !reset; x++) {
				for (int y = 0; y < sudoku[0].length && !reset; y++) {
					int zFeld = y / size + x / size * size;
					int counter = -1;
					do {
						counter++;
						sudoku[x][y] = rand.nextInt(sizeSquare);
						if (counter >= Math.pow(2, sizeSquare)) {
							reset = true;
							break;
						}
					} while (rowCondition[x][sudoku[x][y]] || columnCondition[y][sudoku[x][y]]
							|| blockCondition[zFeld][sudoku[x][y]]);
					rowCondition[x][sudoku[x][y]] = true;
					columnCondition[y][sudoku[x][y]] = true;
					blockCondition[zFeld][sudoku[x][y]] = true;
				}
			}
		} while (reset);

		locked = new boolean[this.sizeSquare][this.sizeSquare];
	}

	/**
	 * Copy constructor.
	 */
	Sudoku(Sudoku tobecopied) {
		this.sizeSquare = tobecopied.sizeSquare;
		this.size = tobecopied.size;

		this.sudoku = new int[sizeSquare][sizeSquare];

		locked = new boolean[sizeSquare][sizeSquare];

		rowCondition = new boolean[sizeSquare][sizeSquare];
		columnCondition = new boolean[sizeSquare][sizeSquare];
		blockCondition = new boolean[sizeSquare][sizeSquare];

		for (int x = 0; x < sudoku.length; x++) {
			for (int y = 0; y < sudoku[0].length; y++) {
				this.sudoku[x][y] = tobecopied.sudoku[x][y];
				this.locked[x][y] = tobecopied.locked[x][y];
				if (!locked[x][y]) {
					rowCondition[x][sudoku[x][y]] = true;
					columnCondition[y][sudoku[x][y]] = true;
					blockCondition[y / size + x / size * size][sudoku[x][y]] = true;
				}
			}
		}
	}

	/**
	 * Locks a field
	 * @param field to be locked
	 */
	void lockField(Field field) {
		locked[field.x][field.y] = true;

		rowCondition[field.x][sudoku[field.x][field.y]] = false;
		columnCondition[field.y][sudoku[field.x][field.y]] = false;
		blockCondition[field.y / size + field.x / size * size][sudoku[field.x][field.y]] = false;
	}

	/**
	 * Unlocks a field.
	 * @param field to be unlocked
	 */
	void unlockField(Field field) {
		locked[field.x][field.y] = false;

		rowCondition[field.x][sudoku[field.x][field.y]] = true;
		columnCondition[field.y][sudoku[field.x][field.y]] = true;
		blockCondition[field.y / size + field.x / size * size][sudoku[field.x][field.y]] = true;
	}
	
	/**
	 * Checks if given field is locked.
	 */
	boolean isLocked(Field field) {
		return locked[field.x][field.y];
	}

	/**
	 * Get possible numbers for given field.
	 * @return List of these numbers.
	 */
	List<Integer> getOptions(Field field) {
		List<Integer> posibles = new ArrayList<>(sizeSquare);
		int block = field.getBlock(size);
		for (int i = 0; i < sizeSquare; ++i) {
			if (!rowCondition[field.x][i] && !columnCondition[field.y][i] && !blockCondition[block][i]) {
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
				if (isLocked(move)) {
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
			copy.lockField(nextmove);
			if (!copy.uniqueSolvable()) {
				count++;
			} else {
				lockField(nextmove);
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
				copy.unlockField(move);
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
