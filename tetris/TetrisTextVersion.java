package edu.stanford.cs108.tetris;

import java.util.Scanner;

/**
 * Created by pyoung on 9/6/2016.
 */
public class TetrisTextVersion implements TetrisUIInterface {

    // Game States
    private static int NOT_SETUP = 0;
    private static int GAME_IN_PROGRESS = 9;
    private static int GAME_OVER = 10;

    private TetrisLogic gameLogic;
    private int gameState = NOT_SETUP;


    private void manageGame() {
        gameLogic = new TetrisLogic(this);

        int inputInt = 0;
        do {
            System.out.println("Ready to Play\n");
            System.out.println("1) Begin Game.");
            System.out.println("2) Begin Game with Test Sequence.");
            System.out.println("3) Quit.");

            inputInt = scanner.nextInt();

            if(inputInt == 1) {
            	gameLogic.setTestMode(false);
                playAGame();
            } else if (inputInt == 2) {
            	gameLogic.setTestMode(true);
                playAGame();            	
            } else if (inputInt != 3) {
                System.out.println("I don't recognize " + inputInt);
            }

        } while(inputInt != 3);
    }

    Scanner scanner = new Scanner(System.in);

    String[] betweenGames = {
            "1) Start Game",
            "2) Quit",
    };

    String[] debugChoices = {
            "1) Keep Going",
            "2) Quit",
    };

    // no error checking, so don't call with an empty choices array
    // and make sure choices start with 1 and are sequential
    private int displayChoices(String prompt,String[] choices) {
        if (prompt != null) {
            System.out.println(prompt);
        }

        int inputInt = -1;
        do {
            for(int i=0; i<choices.length; i++) {
                System.out.println(choices[i]);
            }
            inputInt = scanner.nextInt();

            if (inputInt >= 1 && inputInt <= choices.length) {
                return inputInt;
            }
            System.out.println(inputInt + " is not one of the choices!");
        } while (true);
    }

    String[] pieceInPlay = {
            "1) Move Left",
            "2) Move Right",
            "3) Rotate",
            "4) Down",
            "5) Drop",
    };

    public void playAGame() {
        gameLogic.onStartGame();
        gameLogic.onTick();
        do {
            printBoard();
            int choice = displayChoices(null,pieceInPlay);

            switch(choice) {
                case 1:
                    gameLogic.onLeft();
                    break;
                case 2:
                    gameLogic.onRight();
                    break;
                case 3:
                    gameLogic.onRotate();
                    break;
                case 4:
                    /* This is actually a no-op (non-operation)
                       the user doesn't want to drop the piece all the way down,
                       but also doesn't want to move it left or right.
                       Just don't do anything and let the onTick after the switch
                       drop the piece down one square. */
                    break;
                case 5:
                    gameLogic.onDrop();
                    break;
            }
            gameLogic.onTick();
        } while (gameState != GAME_OVER);
    }

    // close to a copy of the board's own toString, however, that did
    // not differentiate between the top area and the rest of the board
    // which made it unsuitable for actual play.
    public void printBoard() {
        int totalHeight = gameLogic.getTotalHeight();
        int topHeight = gameLogic.getTopAreaHeight();
        int width = gameLogic.getWidth();

        StringBuilder buff = new StringBuilder();
        for (int y = totalHeight-1; y>=0; y--) {
            if (y > totalHeight - topHeight - 1) {
                buff.append('(');
            } else {
                buff.append('|');
            }
            for (int x=0; x<width; x++) {
                if (gameLogic.getGrid(x,y)) buff.append('+');
                else buff.append(' ');
            }
            if (y > totalHeight - topHeight - 1) {
                buff.append(')');
            } else {
                buff.append('|');
            }
            buff.append('\n');
        }
        for (int x=0; x<width+2; x++) buff.append('-');
        System.out.println(buff);
    }

    public static void main(String[] args) {
        TetrisTextVersion uiInterface = new TetrisTextVersion();
        uiInterface.manageGame();
    }

    @Override
    public void boardUpdated() {
//        System.out.println("Current Board State");
//        System.out.println(board.toString());
    }

    @Override
    public void dataUpdated() {

    }

    @Override
    public void rigGameOver() {
        System.out.println("\nGame Over!!!\n");
        gameState = GAME_OVER;
    }

    @Override
    public void rigGameInProgress() {
    	gameState = GAME_IN_PROGRESS;
    }
}
