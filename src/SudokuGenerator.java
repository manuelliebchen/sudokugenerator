import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Random;

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Generator for sudokus.
 * @author Manuel Liebchen (hallo@manuelliebchen.de)
 */
public class SudokuGenerator {

	public static void main(String[] args) {
		Random rand = new Random();
		int size = 3;
		
		BufferedImage sudokuSheet = generateSheet(4, 6, size, 60, (int) Math.pow(size, 5), rand,
				new Font("Comic Neue", 1, 48));

		try {
			ImageIO.write(sudokuSheet, "png", new File("sudoku.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Renders a full sheet of sudokus.
	 * 
	 * @param numberX  of sudokus in every row
	 * @param numberY  of sudokus in every column
	 * @param size     of the sudoku
	 * @param cellSize rendered size of every field
	 * @param tries    of how often should it try to delete a field
	 * @param rand     Random object for consistency if seed is provide
	 * @return BufferedImage of the sudoku sheet.
	 */

	public static BufferedImage generateSheet(int numberX, int numberY, int size, float cellSize, int tries,
			Random rand, Font font) {
		int sizeSquare = size * size;
		BufferedImage bu = new BufferedImage(
				Math.round(cellSize * sizeSquare * numberX + cellSize * (numberX - 1) + 2 * cellSize),
				Math.round(cellSize * sizeSquare * numberY + cellSize * (numberY - 1) + 2 * cellSize),
				BufferedImage.TYPE_INT_ARGB);
		Graphics g = bu.getGraphics();
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

		for (int i = 0; i < numberX; ++i) {
			for (int j = 0; j < numberY; ++j) {
				AffineTransform af = new AffineTransform();
				af.translate(Math.round(cellSize * (sizeSquare + 1) * i + cellSize),
						Math.round(cellSize * (sizeSquare + 1) * j + cellSize));
				((Graphics2D) g).setTransform(af);

				Sudoku sudoku = new Sudoku(size, rand);
				sudoku.aussortieren(rand, tries);
				sudoku.render(g, cellSize, font);
			}
		}
		return bu;
	}
}