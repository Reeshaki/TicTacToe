package com.reeshaki.ticTacToe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class ticTacToe {

    final static String formatUnderline = "\033[4m";
    final static String formatReset = "\033[0m";
    static String[][] board = new String[3][3];
    static int[][] priority = new int[][] {{0, 0, 0}, {0, 1, 0}, {0, 0, 0}};
    final static int[] cellIndices = new int[] {0, 1, 2, 0, 1, 2};
    final static Point[] corners = new Point[] {new Point(0, 0), new Point(0, 2), new Point(2, 0), new Point(2, 2)};
    static boolean gameComplete = false;

    public static void main(String[] args) {
        Scanner scn = new Scanner(System.in);
        System.out.println("Starting Tic Tac Toe!");
        do {
            runHumansTurn(scn);
            runAisTurn();
        } while (!gameComplete);
    }

    private static void runAisTurn() {
        printBoard();
        System.out.println("Computer is 'O'. Populating board now!");
        int highestPriority = -100;
        List<Point> highestPriorityCells = new ArrayList<Point>();
        // Iterate through all of the cells and find the cells with the highest priority. The computer will select one
        // of these cells as its next move.
        for (int i = 0; i < priority.length; i++) {
            for (int j = 0; j < priority.length; j++) {
                // We only care about the cells that are null. If the cell is already set, then set the priority to 0 as
                // it can't be overwritten.
                if (board[i][j] == null) {
                    if (priority[i][j] > highestPriority) {
                        highestPriority = priority[i][j];
                        highestPriorityCells = new ArrayList<Point>();
                        highestPriorityCells.add(new Point(i, j));
                    } else if (priority[i][j] == highestPriority) {
                        highestPriorityCells.add(new Point(i, j));
                    }
                } else {
                    priority[i][j] = 0;
                }
            }
        }
        Random rand = new Random();
        Point cellToPopulate = highestPriorityCells.get(rand.nextInt(highestPriorityCells.size()));
        populateBoard(cellToPopulate, "O");
    }

    private static void runHumansTurn(Scanner scn) {
        Point point;
        do {
            printBoard();
            System.out.print("You are 'X', please input the index of the cell you'd like to play, ex. A1: ");
            point = readUserInputAndSetCellPriority(scn);
        } while (!populateBoard(point, "X"));
    }

    private static boolean populateBoard(Point point, String TicOrTac) {
        if (point.getX() >= board.length || point.getY() >= board.length) {
            System.out.println("Yea..." + Character.toString((char) ('A' + point.getX()))
                    + Character.toString((char) ('1' + point.getY())) + " is not a valid cell. Try again");
            return false;
        } else if (board[point.getX()][point.getY()] != null) {
            System.out.println("Oops! " + Character.toString((char) ('A' + point.getX()))
                    + Character.toString((char) ('1' + point.getY())) + " is already taken by: "
                    + board[point.getX()][point.getY()]);
            return false;
        } else {
            board[point.getX()][point.getY()] = TicOrTac;
            checkForWinnerAndSetPriority(point.getX(), point.getY());
            return true;
        }
    }

    private static void checkForWinnerAndSetPriority(int x, int y) {
        if (isBoardFull()) {
            printBoard();
            System.out.println("DRAW!");
            System.exit(0);
        }
        setPriority(x, y);
        increasePriorityForCloseWinsAndCheckForWinnerByRow(new Point(0, 0), new Point(0, 2));
        increasePriorityForCloseWinsAndCheckForWinnerByRow(new Point(1, 0), new Point(1, 2));
        increasePriorityForCloseWinsAndCheckForWinnerByRow(new Point(2, 0), new Point(2, 2));
        increasePriorityForCloseWinsAndCheckForWinnerByRow(new Point(0, 0), new Point(2, 0));
        increasePriorityForCloseWinsAndCheckForWinnerByRow(new Point(0, 1), new Point(2, 1));
        increasePriorityForCloseWinsAndCheckForWinnerByRow(new Point(0, 2), new Point(2, 2));
        increasePriorityForCloseWinsAndCheckForWinnerByRow(new Point(0, 0), new Point(2, 2));
        increasePriorityForCloseWinsAndCheckForWinnerByRow(new Point(2, 0), new Point(0, 2));
    }

    private static boolean isBoardFull() {
        for (int row = 0; row < board.length; row++) {
            for (int column = 0; column < board.length; column++) {
                if (board[row][column] == null || board[row][column].isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void increasePriorityForCloseWinsAndCheckForWinnerByRow(Point point, Point point2) {
        HashMap<String, Object> myMap = new HashMap<String, Object>();
        Point pointToPrioritize = null;
        int xFound = 0;
        int oFound = 0;
        int row = point.getX(), column = point.getY(), priorityIncrement = 0;
        myMap.put("xFound", xFound);
        myMap.put("oFound", oFound);
        myMap.put("priorityIncrement", priorityIncrement);
        myMap.put("pointToPrioritize", pointToPrioritize);
        for (int i = 0; i < priority.length; i++) {
            if (point.getX() == point2.getX()) {
                column = i;
            } else if (point.getY() == point2.getY()) {
                row = i;
            } else {
                column = i;
                // Diagonal going from (0,0) to (2,2)
                if (point.getX() == point.getY()) {
                    row = i;
                } else {
                    // Diagonal going from (2,0) to (0,2)
                    row = point.getX();
                    row -= i;
                }
            }
            myMap.put("row", row);
            myMap.put("column", column);
            checkForWinnerAndDeterminePriorityIncrement(myMap);
        }
        if (myMap.get("pointToPrioritize") != null) {
            // Both 'X' and 'O' were found in the row, decrease the priority
            if ((Integer) myMap.get("xFound") != 0 && (Integer) myMap.get("oFound") != 0) {
                myMap.put("priorityIncrement", -3);
            }
            priority[((Point) myMap.get("pointToPrioritize")).getX()][((Point) myMap.get("pointToPrioritize"))
                    .getY()] += (Integer) myMap.get("priorityIncrement");
        }
    }

    private static void checkForWinnerAndDeterminePriorityIncrement(HashMap<String, Object> myMap) {
        int xFound = (Integer) myMap.get("xFound"), oFound = (Integer) myMap.get("oFound"),
                row = (Integer) myMap.get("row"), column = (Integer) myMap.get("column");
        if (board[row][column] == null) {
            if ((Point) myMap.get("pointToPrioritize") == null) {
                myMap.put("pointToPrioritize", new Point(row, column));
            }
        } else if ("X".equals(board[row][column])) {
            // Increase the priority enough to possibly make it the highest priority cell
            if (xFound == 1) {
                myMap.put("priorityIncrement", 3);
            } else if (xFound == 2) {
                printBoard();
                System.out.println("You WIN!");
                System.exit(0);
            }
            myMap.put("xFound", ++xFound);
        } else if ("O".equals(board[row][column])) {
            // Increase the priority to make it the highest priority cell
            if (oFound == 1) {
                myMap.put("priorityIncrement", 4);
            } else if (oFound == 2) {
                printBoard();
                System.out.println("Computer WINS!");
                System.exit(0);
            }
            myMap.put("oFound", ++oFound);
        }

    }

    private static Point readUserInputAndSetCellPriority(Scanner scn) {
        String cellPointStr = scn.nextLine().toUpperCase();
        int row = cellPointStr.charAt(0) - 'A';
        int column = cellPointStr.charAt(1) - '1';
        Point point = new Point(row, column);
        return point;
    }

    private static void setPriority(int row, int column) {
        priority[row][column] = 0;
        for (int i = 1; i < priority.length; i++) {
            if (row == column) {
                // Takes care of the \ diagonal cells
                priority[cellIndices[row + i]][cellIndices[column + i]] += 1;
                if (row == 1) {
                    // Takes care of the bottom-left and top-right corner cells when center cell picked
                    priority[cellIndices[row + i]][cellIndices[column + 3 - i]] += 1;
                }
            }
            // Takes care of the / diagonal cells
            else if (Math.abs(row - column) == 2) {
                priority[cellIndices[row + i]][cellIndices[column + 3 - i]] += 1;
            }
            // If the user picks a corner, set a higher priority for the cells next to it and even higher for the center
            setPriorityForCornerSelections(row, column);

            // Takes care of the column and row cells
            priority[cellIndices[row + i]][cellIndices[column]] += 1;
            priority[cellIndices[row]][cellIndices[column + i]] += 1;
        }

    }

    private static void setPriorityForCornerSelections(int row, int column) {
        int neighborRowIncrement = 1;
        int neighborColumnIncrement = 1;
        for (Point corner : corners) {
            if (row == corner.getX() && column == corner.getY()) {
                if (row == 2) {
                    neighborRowIncrement = -1;
                }
                if (column == 2) {
                    neighborColumnIncrement = -1;
                }
                // If the neighboring cell's priority is set to 0, then this is the first turn. Set priority higher.
                if (priority[cellIndices[row + neighborRowIncrement]][cellIndices[column]] == 0) {
                    priority[cellIndices[row + neighborRowIncrement]][cellIndices[column]] += 2;
                    priority[cellIndices[row]][cellIndices[column + neighborColumnIncrement]] += 2;
                    // We want the center priority higher as that needs to be the next move for the AI.
                    priority[cellIndices[1]][cellIndices[1]] += 3;
                }
            }
        }

    }

    private static void printBoard() {
        StringBuilder sb = new StringBuilder();
        char rowLabel = 'A';
        char columnLabel = '1';
        for (int x = -1; x < board.length; x++) {
            if (x >= 0) {
                sb.append(Character.toString(rowLabel++)).append(" ");
            }
            for (int y = -1; y < board.length; y++) {
                if (x == -1) {
                    if (y == -1) {
                        // Top left corner is empty due to column and row labels
                        sb.append("  ");
                    } else {
                        sb.append(Character.toString(columnLabel++)).append(" ");
                    }
                }
                printRowContents(sb, x, y);
            }
            sb.append(System.lineSeparator());
        }
        System.out.println(sb.toString());
    }

    private static void printRowContents(StringBuilder sb, int x, int y) {
     // Print the board
        if (x >= 0 && y >= 0) {
            // Set the text format to underline
            if (x != 2) {
                sb.append(formatUnderline);
            }

            if (board[x][y] == null) {
                sb.append(" ");
            } else if (board[x][y] != null) {
                sb.append(board[x][y]);
            }

            // Reset the text format to default
            if (x != 2) {
                sb.append(formatReset);
            }

            if (y != 2) {
                sb.append("|");
            }
        }
    }

    static private class Point {
        private int x;
        private int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }
    }
}
