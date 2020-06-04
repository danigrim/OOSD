package assign4;

import java.util.*;

/*
 * Encapsulates a Sudoku grid to be solved.
 * CS108 Stanford.
 */
public class Sudoku {
	// Provided grid data for main/testing
	// The instance variable strategy is up to you.

	// Provided easy 1 6 grid
	// (can paste this text into the GUI too)
	public static final int[][] easyGrid = Sudoku.stringsToGrid(
			"1 6 4 0 0 0 0 0 2",
			"2 0 0 4 0 3 9 1 0",
			"0 0 5 0 8 0 4 0 7",
			"0 9 0 0 0 6 5 0 0",
			"5 0 0 1 0 2 0 0 8",
			"0 0 8 9 0 0 0 3 0",
			"8 0 9 0 4 0 2 0 0",
			"0 7 3 5 0 9 0 0 1",
			"4 0 0 0 0 0 6 7 9");

	// Provided medium 5 3 grid
	public static final int[][] mediumGrid = Sudoku.stringsToGrid(
			"530070000",
			"600195000",
			"098000060",
			"800060003",
			"400803001",
			"700020006",
			"060000280",
			"000419005",
			"000080079");

	// Provided hard 3 7 grid
	// 1 solution this way, 6 solutions if the 7 is changed to 0
	public static final int[][] hardGrid = Sudoku.stringsToGrid(
			"3 7 0 0 0 0 0 8 0",
			"0 0 1 0 9 3 0 0 0",
			"0 4 0 7 8 0 0 0 3",
			"0 9 3 8 0 0 0 1 2",
			"0 0 0 0 4 0 0 0 0",
			"5 2 0 0 0 6 7 9 0",
			"6 0 0 0 2 1 0 4 0",
			"0 0 0 5 3 0 9 0 0",
			"0 3 0 0 0 0 0 5 1");

	public static final int SIZE = 9;  // size of the whole 9x9 puzzle
	public static final int PART = 3;  // size of each 3x3 part
	public static final int MAX_SOLUTIONS = 100;

	//my variables:
	private Spot[][] grid; 
	private int [][] first_solution;
	private ArrayList<Spot> spot_order;
	private int solutions;
	private long time_elapsed;

	// Provided various static utility methods to
	// convert data formats to int[][] grid.

	/**
	 * Returns a 2-d grid parsed from strings, one string per row.
	 * The "..." is a Java 5 feature that essentially
	 * makes "rows" a String[] array.
	 * (provided utility)
	 * @param rows array of row strings
	 * @return grid
	 */
	public static int[][] stringsToGrid(String... rows) {
		int[][] result = new int[rows.length][];
		for (int row = 0; row<rows.length; row++) {
			result[row] = stringToInts(rows[row]);
		}
		return result;
	}


	/**
	 * Given a single string containing 81 numbers, returns a 9x9 grid.
	 * Skips all the non-numbers in the text.
	 * (provided utility)
	 * @param text string of 81 numbers
	 * @return grid
	 */
	public static int[][] textToGrid(String text) {
		int[] nums = stringToInts(text);
		if (nums.length != SIZE*SIZE) {
			throw new RuntimeException("Needed 81 numbers, but got:" + nums.length);
		}

		int[][] result = new int[SIZE][SIZE];
		int count = 0;
		for (int row = 0; row<SIZE; row++) {
			for (int col=0; col<SIZE; col++) {
				result[row][col] = nums[count];
				count++;
			}
		}
		return result;
	}


	/**
	 * Given a string containing digits, like "1 23 4",
	 * returns an int[] of those digits {1 2 3 4}.
	 * (provided utility)
	 * @param string string containing ints
	 * @return array of ints
	 */
	public static int[] stringToInts(String string) {
		int[] a = new int[string.length()];
		int found = 0;
		for (int i=0; i<string.length(); i++) {
			if (Character.isDigit(string.charAt(i))) {
				a[found] = Integer.parseInt(string.substring(i, i+1));
				found++;
			}
		}
		int[] result = new int[found];
		System.arraycopy(a, 0, result, 0, found);
		return result;
	}


	// Provided -- the deliverable main().
	// You can edit to do easier cases, but turn in
	// solving hardGrid.
	public static void main(String[] args) {
		Sudoku sudoku;
		sudoku = new Sudoku(hardGrid);
		System.out.println(sudoku); // print the raw problem
		int count = sudoku.solve();
		System.out.println("solutions:" + count);
		System.out.println("elapsed:" + sudoku.getElapsed() + "ms");
		System.out.println(sudoku.getSolutionText());
	}

	/**
	 * Sets up sudoku grid to a grid of spots with their values
	 * equivalent to the ints in the grid it takes in.
	 */
	public Sudoku(int[][] ints) {
		this.grid = new Spot[SIZE][SIZE];
		this.first_solution = new int[SIZE][SIZE];//first grid solution
		this.spot_order = new ArrayList<Spot>(); //arraylist to keep track of order of spots to be solved
		this.solutions=0;//number of solutions
		this.time_elapsed=0; //time taken for sudoku to be solved
		//initialize grid of spots and spot ordering, determined by spot class
		for(int r=0; r<SIZE; r++) {
			for(int c=0; c<SIZE; c++) {
				int value = ints[r][c];
				Spot new_spot = new Spot(r, c, value);
				grid[r][c] = new_spot;
				if(value==0) {
					spot_order.add(new_spot);
				}
			}
		}
		Collections.sort(spot_order);
	}



	/**
	 * Solves the puzzle, invoking the underlying recursive search.
	 */
	public int solve() {
		long begin_time = System.currentTimeMillis();
		Boolean unsolvable = grid_unsolvable();
		if(unsolvable) {
			return 0;
		}
		recursive_helper(0);
		long end_time = System.currentTimeMillis();
		this.time_elapsed = end_time - begin_time;
		return solutions; 
	}

	//Method returns true if grid is invalid and therefore unsolvable, 
	//in the case that it already contains duplicate digits in same row, column or part
	public boolean grid_unsolvable() {
		for(int r=0; r<SIZE; r++) {
			for(int c=0; c<SIZE; c++) {
				Spot test = grid[r][c];
				int value = test.value;
				if(value!=0) {
					if(!test.find_candidates().contains(test.value)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	//helper function for recursive backtracking to find solutions to the puzzle
	public void recursive_helper(int index) {				
		if(solutions >= MAX_SOLUTIONS) {
			return;
		}	
		if(index==spot_order.size()) {
			solutions++;
			if(solutions == 1) { //copy the current grid to save it 
				for(int r=0; r<SIZE; r++) {
					for(int c=0; c<SIZE; c++) {
						first_solution[r][c]=grid[r][c].value;	
					}
				}
			}
			return;
		}
		Spot sp = spot_order.get(index);
		if(sp.num_candidates()==0) {
			return;
		}
		ArrayList<Integer> candidates = sp.find_candidates();
		for(int c : candidates) {
			sp.set(c, false); //choose
			recursive_helper(index+1); //try
			sp.undo(); //un-choose
		}
	}
	
	//returns the string formatted version of the first found solution
	public String getSolutionText() {
		if(solutions ==0) {
			return "";
		}
		StringBuilder grid_str = new StringBuilder("");
		for(int r=0; r<SIZE; r++) {					
			for(int c=0; c<SIZE; c++) {
				grid_str.append(" ");
				grid_str.append(first_solution[r][c]);
			}
			grid_str.append('\n');
		}
		return grid_str.toString();
	}

	//returns the string version of sudoku grid 
	@Override 
	public String toString() {
		StringBuilder grid_str = new StringBuilder("");
		for(int r=0; r<SIZE; r++) {					
			for(int c=0; c<SIZE; c++) {
				grid_str.append(" ");
				grid_str.append(grid[r][c].value);
			}
			grid_str.append('\n');
		}
		return grid_str.toString();
	}

	public long getElapsed() {
		return this.time_elapsed; 
	}

	public class Spot implements Comparable<Spot>{
		//row and column where spot is located in sudoku grid
		public int col;
		public int row;
		//number value of spot
		public int value;
		//backup value for recursion
		public int backup_value;
		//true if the value is valid
		public boolean final_state;
		public Spot(int y, int x, int val) {
			this.col = x;
			this.row = y;
			this.value = val;

			if(val==0) {
				final_state = false;
			}else {
				final_state = true;
			}
			backup_value =0;
		}

		public int get_row() {
			return this.row;
		}

		public int get_col() {
			return this.col;
		}

		public int get_value() {
			return this.value;
		}

		public boolean is_final() {
			return this.final_state;
		}

		public void set(int new_value, boolean uncertain) {
			if(uncertain) {
				backup_value = this.value;
			}
			this.value = new_value;
			final_state = true;
		}

		public void undo() {
			this.value = this.backup_value;
			final_state = false;
		}

		//Function that finds all legal values the spot can take
		//Returns arrayList with the values 
		public ArrayList<Integer> find_candidates(){
			//helper array to find already used values in scope of that spot
			boolean[] used = new boolean[SIZE+1];
			ArrayList<Integer> candidates = new ArrayList<Integer>();
			//find all numbers in the same row
			for(int r=0; r<SIZE; r++) {
				int val = (grid [r][this.col]).value;
				if(r!=this.row) {
					used[val] = true;
				}
			}
			//find all numbers is the same column
			for(int c=0; c<SIZE; c++) {	
				int val = (grid [this.row][c]).value;
				if(c!=this.col) {
					used[val] = true;
				}
			}

			int square_Xindex = this.col % 3;
			int square_Yindex = this.row %3;
			int square_row = this.row-square_Yindex;
			int square_col = this.col-square_Xindex;
			//find all numbers in the PART of the square			
			for(int c= 0; c<PART; c++) {
				for(int r=0; r<PART; r++) {	
					int val = (grid [square_row + r][square_col+c]).value;
					if(c!=square_Xindex||r!=square_Yindex) {
						used[val] = true;
					}
				}
			}
			//make a list with all numbers not included in the row, column or part. 
			for(int i=1; i<SIZE+1; i++) {
				if(!used[i]) {
					candidates.add((Integer)(i));
				}
			}

			return candidates;
		}
		//returns number of possible values the spot can take
		public int num_candidates() {
			return this.find_candidates().size();
		}

		@Override
		public int compareTo(Spot sp) {
			return this.num_candidates()-sp.num_candidates();
		}

	}

}
