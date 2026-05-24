package image;


import java.awt.*;

/**
 * create a padded image with size next power of 2 to each dimension
 */
public class PaddedImage {
	private final Image padded;

	/**
	 * pads the input image
	 * extra white pixels filled with white around the image
	 * @param image source image
	 */
	public PaddedImage(Image image) {

		//get the new dims
		int newWidth = nextPowOf2(image.getWidth());
		int newHeight = nextPowOf2(image.getHeight());

		int leftPad = (newWidth - image.getWidth())/2;
		int topPad = (newHeight - image.getHeight())/2;

		Color[][] paddedPixels = new Color[newHeight][newWidth];

		for ( int row = 0; row < newHeight; row++ ) {
			for ( int col = 0; col < newWidth; col++ ) {
				boolean inside =
						row >= topPad && row < topPad + image.getHeight()
						&& col >= leftPad && col < leftPad + image.getWidth();
				if (inside) {
					paddedPixels[row][col] = image.getPixel(row - topPad, col - leftPad);
				}
				else {
					paddedPixels[row][col] = Color.WHITE;
				}
			}
		}
		this.padded = new Image(paddedPixels, newWidth, newHeight);
	}

	public int getWidth() {
		return padded.getWidth();
	}
	public int getHeight() {
		return padded.getHeight();
	}

	/**
	 * return the next power of 2
	 * @param x the int which we want to get the closest power of 2 from it
	 * @return next power of 2 from x
	 */
	private static int nextPowOf2(int x) {
		int p = 1;
		while ( p < x) p*=2;
		return p;
	}

	/**
	 * splits the padded image to sub images
	 * @param resolution num of sub images per row
	 * @return 2D array of square Image tiles
	 */
	public  Image[][] subImages(int resolution) {
		int tileSize = padded.getWidth()/resolution;
		int gridCols = resolution;
		int gridRows = padded.getHeight()/tileSize;
		Image[][] tiles = new Image[gridRows][gridCols];
		for (int tileRow = 0; tileRow < gridRows; tileRow++) {
			for (int tileCol = 0; tileCol < gridCols; tileCol++) {
				int startCol = tileCol*tileSize;
				int startRow = tileRow*tileSize;
				Color[][] tilePixels = new Color[tileSize][tileSize];
				for (int r =  0; r < tileSize; r++) {
					for (int c = 0; c < tileSize; c++) {
						tilePixels[r][c] = padded.getPixel(startRow + r, startCol + c);
					}
				}
				tiles[tileRow][tileCol] = new Image(tilePixels, tileSize, tileSize);
			}
		}
		return tiles;
	}

}
