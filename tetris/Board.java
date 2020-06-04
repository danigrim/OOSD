//Board.java
package edu.stanford.cs108.tetris;

import java.util.Arrays;

/**
 CS108 Tetris Board.
 Represents a Tetris board -- essentially a 2-d grid
 of booleans. Supports tetris pieces and row clearing.
 Has an "undo" feature that allows clients to add and remove pieces efficiently.
 Does not do any drawing or have any idea of pixels. Instead,
 just represents the abstract 2-d board.
 */
public class Board	{
	// Some ivars are stubbed out for you:
	private int width;
	private int height;
	private boolean[][] grid;
	private boolean DEBUG = true;
	boolean committed;

	private int[] widths; //keep track of width of rows
	private int[] heights; //keep track of heights of columns
	private int maxHeight; //maximum height in board

	//backups ivars
	private int[] xWidths; //backup for widths array
	private int[] xHeights; //backup for heights array
	private boolean [][] xGrid; //backup for grid 
	private int xMaxHeight; //backup for max height

	/**
	 Creates an empty board of the given width and height
	 measured in blocks.
	 */
	public Board(int width, int height) {
		this.width = width;
		this.height = height;
		grid = new boolean[width][height];	
		committed = true;

		widths = new int[height];
		heights = new int[width];
		maxHeight =0;

		xWidths = new int [height];
		xHeights = new int [width];
		xGrid = new boolean [width][height];
		xMaxHeight =0;

	}

	/**
	 Returns the width of the board in blocks.
	 */
	public int getWidth() {
		return width;
	}

	/**
	 Returns the height of the board in blocks.
	 */
	public int getHeight() {
		return height;
	}

	/**
	 Returns the max column height present in the board.
	 For an empty board this is 0.
	 */
	public int getMaxHeight() {	
		return maxHeight; 
	}

	/**
	 Checks the board for internal consistency -- used
	 for debugging.
	 */
	public void sanityCheck() {
		if (DEBUG) {
			int maxHeightCheck =0;
			int local_max_height;
			int [] heightCheck = new int[width];
			int [] widthCheck = new int[height];
			for(int w=0; w<width; w++) {
				local_max_height=-1;
				for(int h=0; h<height; h++) {
					if(grid[w][h]) {
						if(h>local_max_height) {
							local_max_height=h;
						}
						widthCheck[h]++;
						if(local_max_height+1>maxHeightCheck) {
							maxHeightCheck = local_max_height+1;
						}
					}
				}
				if(local_max_height==-1) {
					heightCheck[w]=0;
				}else {
				heightCheck[w]=(local_max_height+1);
			}
			}
			//check heights
			for(int w=0; w<width; w++) {
				
				if(heights[w]!=heightCheck[w]) {
					throw new RuntimeException("The heights array is wrong");
				}
			}
			//check widths
			for(int h=0; h<height; h++) {
				if(widths[h]!=widthCheck[h]) {
					throw new RuntimeException("The width array is wrong");
				}
			}
			//check max height
			if(maxHeightCheck!=maxHeight) {
				throw new RuntimeException("The max height is wrong");
			}
		}
	}

	/**
	 Given a piece and an x, returns the y
	 value where the piece would come to rest
	 if it were dropped straight down at that x.

	 <p>
	 Implementation: use the skirt and the col heights
	 to compute this fast -- O(skirt length).
	 */
	public int dropHeight(Piece piece, int x) {
		int lowest_y =0;
		for(int i=0; i<piece.getWidth(); i++) {
			if(heights[x+i]-piece.getSkirt()[i]>lowest_y) {
				lowest_y = heights[x+i]-piece.getSkirt()[i];
			}
		}
		return lowest_y; 
	}


	/**
	 Returns the height of the given column --
	 i.e. the y value of the highest block + 1.
	 The height is 0 if the column contains no blocks.
	 */
	public int getColumnHeight(int x) {
		return heights[x];
	}


	/**
	 Returns the number of filled blocks in
	 the given row.
	 */
	public int getRowWidth(int y) {
		return widths[y]; 
	}


	/**
	 Returns true if the given block is filled in the board.
	 Blocks outside of the valid width/height area
	 always return true.
	 */
	public boolean getGrid(int x, int y) {
		if(y>height-1 || x>width-1 || x<0 || y<0) {
			return true;
		}
		else return this.grid[x][y];
	}

	public static final int PLACE_OK = 0;
	public static final int PLACE_ROW_FILLED = 1;
	public static final int PLACE_OUT_BOUNDS = 2;
	public static final int PLACE_BAD = 3;

	/**
	 Attempts to add the body of a piece to the board.
	 Copies the piece blocks into the board grid.
	 Returns PLACE_OK for a regular placement, or PLACE_ROW_FILLED
	 for a regular placement that causes at least one row to be filled.

	 <p>Error cases:
	 A placement may fail in two ways. First, if part of the piece may falls out
	 of bounds of the board, PLACE_OUT_BOUNDS is returned.
	 Or the placement may collide with existing blocks in the grid
	 in which case PLACE_BAD is returned.
	 In both error cases, the board may be left in an invalid
	 state. The client can use undo(), to recover the valid, pre-place state.
	 */
	public int place(Piece piece, int x, int y) {
		// flag !committed problem
		if (!committed) throw new RuntimeException("place commit problem");			
		int result = PLACE_OK;		

		if(((x+piece.getWidth())>width || (y+piece.getHeight())>height) || x<0 || y<0){
			return PLACE_OUT_BOUNDS;
		}	
		backup();
		//Check each coordinate of the point to see if it can fit in the location
		for(TPoint coordinate: piece.getBody()) {
			//Case that coordinate is filled
			if(grid[x+coordinate.x][y+coordinate.y]) {
				return PLACE_BAD;
			}
			//Update board state and widths array
			grid[x+coordinate.x][y+coordinate.y] = true;
			widths[y+coordinate.y]++;
			//case that entire row is filled by piece placement
			if(widths[y+coordinate.y]==width) {
				result = PLACE_ROW_FILLED;
			}
		}
		updateHeights();
		sanityCheck();
		return result;
	}

	/**
	 Deletes rows that are filled all the way across, moving
	 things above down. Returns the number of rows cleared.
	 *if board is in commited state, backup board state
	 */
	public int clearRows() {
		int rowsCleared = 0;

		if(committed) {
			backup();	
		}

		for(int h=0; h<maxHeight;) {	
			if(widths[h]==width) {
				rowsCleared++;
				//shift all the filled places
				for(int r=h; r<height-1; r++) {
					widths[r]=widths[r+1];
					for(int w=0; w<width; w++) {
						grid[w][r] = grid[w][r+1];	
					}
				}   
				addEmptyTop();	    
				
			}else {
				//only increment iterator if row wasn't cleared, else must check again
				h++;			
			}
		}
		updateHeights();
		sanityCheck();
		return rowsCleared;
	}
	
/**
 * Function to add an empty top to the grid when a row is cleared
 */
	private void addEmptyTop() {
		for(int w=0; w<width; w++) {
			grid[w][height-1]=false;
		}
		widths[height-1]=0;
	}
	
	/**
	 * Function to update heights array and the max height
	 */
	private void updateHeights() {
		int local_max_height;
		int new_max_height=0;
		for(int w=0; w<width; w++) {
			local_max_height=-1;
			for(int h=0; h<height; h++) {
				if(grid[w][h]) {
					if((h)>local_max_height) {
						local_max_height=h;
					}
					if((local_max_height+1)>new_max_height) {
						new_max_height = local_max_height+1;
					}
				}
			}
			//case when column is clear
			if(local_max_height==-1) {
				heights[w]=0;
			}else {
			heights[w]=(local_max_height+1);	
		}
		maxHeight=new_max_height;
	}
	}


	/**
	 Reverts the board to its state before up to one place
	 and one clearRows();
	 If the conditions for undo() are not met, such as
	 calling undo() twice in a row, then the second undo() does nothing.
	 See the overview docs.
	 */
	public void undo() {
		if(!committed) {
			boolean[][] temp_grid;
			int temp_maxHeight;
			int [] temp_heights;
			int [] temp_widths;

			temp_grid = grid;
			grid = xGrid;
			xGrid = temp_grid;

			temp_maxHeight = maxHeight;
			maxHeight = xMaxHeight;
			xMaxHeight = temp_maxHeight;

			temp_heights = heights;
			heights = xHeights;
			xHeights = temp_heights;

			temp_widths = widths;
			widths = xWidths;
			xWidths= temp_widths;

			committed = true;	
			sanityCheck();
		}	
	}

	/**
	 Puts the board in the committed state.
	 */
	public void commit() {
		committed = true;
	}

	private void backup() {
		committed = false;
		for(int i=0; i<width; i++) {
			System.arraycopy(grid[i], 0, xGrid[i], 0, height);
		}
		System.arraycopy(heights, 0, xHeights, 0, heights.length);
		System.arraycopy(widths, 0, xWidths, 0, widths.length);
		xMaxHeight = maxHeight;
	}

	/*
	 Renders the board state as a big String, suitable for printing.
	 This is the sort of print-obj-state utility that can help see complex
	 state change over time.
	 (provided debugging utility) 
	 */
	public String toString() {
		StringBuilder buff = new StringBuilder();
		for (int y = height-1; y>=0; y--) {
			buff.append('|');
			for (int x=0; x<width; x++) {
				if (getGrid(x,y)) buff.append('+');
				else buff.append(' ');
			}
			buff.append("|\n");
		}
		for (int x=0; x<width+2; x++) buff.append('-');
		return(buff.toString());
	}
}


