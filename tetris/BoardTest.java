package edu.stanford.cs108.tetris;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class BoardTest {
	private Board board, board_2, board_3, board_4, board_5;
	private Piece pyr1, pyr2, pyr3, pyr4;
	private Piece l1_1, l1_2, l1_3, l1_4;
	private Piece s;
	private Piece v_stick;

	@Before
	public void setUp() throws Exception {
		board = new Board(3, 6);
		board_2 = new Board(3, 6);
		board_3 = new Board(4, 8);
		board_4 = new Board(4, 3);
		board_5 = new Board(3, 8);
		s = new Piece(Piece.S1_STR);
		v_stick = new Piece(Piece.STICK_STR);

		
		pyr1 = new Piece(Piece.PYRAMID_STR);
		pyr2 = pyr1.computeNextRotation();
		pyr3 = pyr2.computeNextRotation();
		pyr4 = pyr3.computeNextRotation();
		
		l1_1= new Piece(Piece.L1_STR);
		l1_2 = l1_1.computeNextRotation();
		l1_3 = l1_2.computeNextRotation();
		l1_4 = l1_3.computeNextRotation();
	}
	
	@Test
	public void test_basic() {
		
		//test board setup
		assertEquals(board.getHeight(), 6);
		assertEquals(board.getWidth(), 3);
		
		//testing simple performance when place one shape in the board.  
		int placement = board.place(pyr1, 0, 0);
		assertEquals(Board.PLACE_ROW_FILLED, placement);
		assertEquals(board.getMaxHeight(), 2);
		assertEquals(board.getRowWidth(0), 3);
		assertEquals(board.getRowWidth(1), 1);
		assertEquals(board.getColumnHeight(2), 1);
		assertEquals(board.getColumnHeight(1), 2);
        assertEquals(1, board.dropHeight(pyr2, 1));
        assertEquals(2, board.dropHeight(pyr2, 0));
        assertEquals(1, board.dropHeight(pyr4, 0));
        
		
	}
	
	@Test
	public void test_invalidPlacements() {
		
		//Testing the functionality of detecting invalid placements and undo function
		//attempting to place a shape out of board the bounds
		int out_bounds = board_3.place(pyr1, 3, 3);
		assertEquals(Board.PLACE_OUT_BOUNDS, out_bounds);
		board_3.undo();
		assertEquals(0, board_2.getMaxHeight());
		
		//attempting to place a shape over another
		board_3.place(pyr1, 0, 0);
		board_3.commit();
		int piece_conflict = board_3.place(pyr4, 0, 0);
		assertEquals(Board.PLACE_BAD, piece_conflict);
		board_3.undo();

		
		//attempting to place a shape in invalid coordinates
		int out_bounds_2 = board_3.place(pyr1, -1, -1);
		assertEquals(Board.PLACE_OUT_BOUNDS, out_bounds_2);
		board_3.undo();	
		
		//detecting that shape goes beyond upper border of board
		int out_bounds_3 = board_3.place(pyr1, 0, 7);
		assertEquals(Board.PLACE_OUT_BOUNDS, out_bounds_3);
		board_3.undo();	
		//detect that shape doesen't partly stay in grid
		assertEquals(false, board_3.getGrid(0, 7));
	  
	}
	
	/**  Test experiments with placing 3 shapes on the grid and tests
	 *  with clearing a single filled row and two consecutive filled rows 
	 */
	@Test
	public void test_placeClear_1() {
		
		//place a pyramid on the board and test if it clears its first row with clear rows
		board.place(pyr1, 0, 0);
		board.commit();
		int rows_cleared = board.clearRows();
		assertEquals(1, rows_cleared);
		board.commit();

		//CHECK clearing of two consecutive full rows is successful
		//place two inverted L's, making two full lines 
		int placement_2 = board.place(l1_4, 0, 0);
		board.commit();
		assertEquals(placement_2, Board.PLACE_ROW_FILLED);
		board.place(l1_2, 0, 2);
		board.commit();
		int rows_cleared_2 = board.clearRows();
		board.commit();
		assertEquals(2, rows_cleared_2);
		assertEquals(board.getColumnHeight(0), 1);
		assertEquals(board.getColumnHeight(1), 1);
		assertEquals(board.getRowWidth(0), 2);
		assertEquals(board.getRowWidth(1), 1);
		assertEquals(board.getRowWidth(2), 0);
		assertEquals(board.getColumnHeight(2), 2);	
		
		//CHECK clearing of two full rows that are not adjacent, upper row 
		board_2.place(pyr1, 0, 0);
		board_2.commit();
		//int s_drop = board_2.dropHeight(s, 0);
		board_2.place(s, 0, 2);
		board_2.commit();
		assertEquals(board_2.getColumnHeight(1),4);
		assertEquals(board_2.getColumnHeight(2),4);
		board_2.place(pyr4, 0, 3);
		board_2.commit();
		assertEquals(board_2.getMaxHeight(), 6);
		int rows_cleared_3 = board_2.clearRows();
		board_2.commit();
		assertEquals(2, rows_cleared_3);
		assertEquals(board_2.getColumnHeight(0),4);
		assertEquals(board_2.getColumnHeight(1),3);
		assertEquals(board_2.getColumnHeight(2),0);
		int drop_s = board_2.dropHeight(s, 0);
		assertEquals(drop_s, 4);
		board_2.undo();
		int drop_pyr = board_2.dropHeight(pyr3, 0);
		assertEquals(drop_pyr, 3);
		//place clear without commit, correct functionality to go back to state before place
		board_2.place(pyr3, 0, 3);
		board_2.clearRows();
		assertEquals(board_2.getRowWidth(4), 0);
		board_2.undo();
		assertEquals(board_2.getColumnHeight(1), 3);
		//place clear with commit after place - should keep piece but not clear its row when undo
		board_2.place(pyr3, 0, 3);
		board_2.commit();
		board_2.clearRows();
		assertEquals(board_2.getRowWidth(4), 0);
		board_2.undo();
		assertEquals(board_2.getColumnHeight(1), 5);
	}
	
	
	
	@Test
	public void test_place_clear_2() {
		
		//testing calling place then clear without comiting then calling undo
		board_3.place(pyr1, 0, 0);
		board_3.commit();
		board_3.place(v_stick, 3, 0);
		assertEquals(4, board_3.getColumnHeight(3));
		board_3.clearRows();
		board_3.undo();
		assertEquals(2, board_3.getMaxHeight());
		
		//testing completely filling up a board
		board_4.place(v_stick.computeNextRotation(), 0, 0);
		assertEquals(4, board_4.getRowWidth(0));
		board_4.commit();
		board_4.place(v_stick.computeNextRotation(), 0, 1);
		board_4.commit();
		board_4.place(v_stick.computeNextRotation(), 0, 2);
		assertEquals(3, board_4.getMaxHeight());
		int clear_all = board_4.clearRows();
		assertEquals(3, clear_all);
		assertEquals(false, board.getGrid(0, 0));
		assertEquals(0, board_4.getMaxHeight());
		board_4.commit();
		
	}
	
	@Test
	public void test_place_clear_3() {
		
		//Test performance clearing top bottom, and other middle rows
		board_5.place(pyr1, 0, 0);
		board_5.commit();
		board_5.place(pyr3, 0, 2);
		board_5.commit();
		assertEquals(4, board_5.dropHeight(pyr1, 0));
		board_5.place(pyr1, 0, 4);
		board_5.commit();
		board_5.place(pyr3, 0, 6);
		board_5.commit();
		assertEquals(8, board_5.getColumnHeight(0));
		int pyr_clear = board_5.clearRows();
		assertEquals(pyr_clear, 4);
		board_5.commit();
		//check performance
		assertEquals(4, board_5.getColumnHeight(1));
		assertEquals(0, board_5.getColumnHeight(2));
		assertEquals(1, board_5.getRowWidth(0));
		assertEquals(1, board_5.getRowWidth(1));
		assertEquals(1, board_5.getRowWidth(2));
		assertEquals(0, board_5.getRowWidth(4));
		assertEquals(0, board_5.getRowWidth(7));
		//check identification of a conflict when placing after clearing
		int piece_conflict = board_5.place(v_stick,1 , 3);
		assertEquals(Board.PLACE_BAD, piece_conflict);
		board_5.undo();
		//check that did not partially place piece
		assertEquals(0, board_5.getRowWidth(5));
		board_5.place(v_stick, 0, 0);
		//check that calling clear on a board with no rows to clear is succesful
		int no_clear = board_5.clearRows();
		assertEquals(0, no_clear);
		assertEquals(2, board_5.getRowWidth(2));
		board_5.commit();
        int place_fill = board_5.place(pyr1, 0, 4);
        assertEquals(place_fill, Board.PLACE_ROW_FILLED);
        int one_clear = board_5.clearRows();
		assertEquals(1, one_clear);
	}
	
	

}
