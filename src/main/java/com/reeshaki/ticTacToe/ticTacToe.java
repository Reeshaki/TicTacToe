package com.reeshaki.ticTacToe;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ticTacToe {

    final static Logger log = LogManager.getLogger(ticTacToe.class);
    static String[][] board = new String[3][3];
    static int[][] priority = new int[][] {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}};
    final static int[] cellIndices = new int[] {0, 1, 2, 0, 1, 2};
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
        System.out.println("Computer is 'O'. Populating board now!");
        int highestPriority = 0;
        List<Point> highestPriorityCell = new ArrayList<Point>();
        for (int i = 0; i < priority.length; i++) {
            for (int j = 0; j < priority.length; j++) {
                if (priority[i][j] > highestPriority) {
                    highestPriority = priority[i][j];
                    highestPriorityCell = new ArrayList<Point>();
                    highestPriorityCell.add(new Point(i, j));
                } else if (priority[i][j] == highestPriority) {
                    highestPriorityCell.add(new Point(i, j));
                }
            }
        }
        Random rand = new Random();
        Point cellToPopulate;
        do {
            cellToPopulate = highestPriorityCell.get(rand.nextInt(highestPriorityCell.size()));
        } while (!populateBoard(cellToPopulate, "O"));
    }

    private static void runHumansTurn(Scanner scn) {
        Point point;
        do {
            printBoard();
            System.out.print("You are 'X', please input the index of the cell you'd like to play, ex. A1: ");
            point = readUserInputAndSetCellPriority(scn);
        } while (!populateBoard(point, "X"));
        printBoard();
    }

    private static boolean populateBoard(Point point, String TicOrTac) {
        if (point.getX() >= board.length || point.getY() >= board.length) {
            System.out.println("Yea..." + Character.toString((char) ('A' + point.getX()))
                    + Character.toString((char) ('1' + point.getY())) + " is not a valid cell. Try again");
            return false;
        } else if (board[point.getX()][point.getY()] != null) {
            System.out.println("Oops! This position is already taken by: " + board[point.getX()][point.getY()]);
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
        increasePriorityForCloseWinsAndCheckForWinner(new Point(0, 0), new Point(0, 2));
        increasePriorityForCloseWinsAndCheckForWinner(new Point(1, 0), new Point(1, 2));
        increasePriorityForCloseWinsAndCheckForWinner(new Point(2, 0), new Point(2, 2));
        increasePriorityForCloseWinsAndCheckForWinner(new Point(0, 0), new Point(2, 0));
        increasePriorityForCloseWinsAndCheckForWinner(new Point(0, 1), new Point(2, 1));
        increasePriorityForCloseWinsAndCheckForWinner(new Point(0, 2), new Point(2, 2));
        increasePriorityForCloseWinsAndCheckForWinner(new Point(0, 0), new Point(2, 2));
        increasePriorityForCloseWinsAndCheckForWinner(new Point(2, 0), new Point(0, 2));
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

    private static void increasePriorityForCloseWinsAndCheckForWinner(Point point, Point point2) {
        Point pointToPrioritize = null;
        int xFound = 0;
        int oFound = 0;
        int row = 0, column = 0, priorityIncrement = 0;
        for (int i = 0; i < priority.length; i++) {
            if (point.getX() == point2.getX()) {
                column = i;
                row = point.getX();
            } else if (point.getY() == point2.getY()) {
                column = point.getY();
                row = i;
            } else {
                column = i;
                row = i;
            }
            if (board[row][column] == null) {
                if (pointToPrioritize == null) {
                    pointToPrioritize = new Point(row, column);
                }
            }
            if ("X".equals(board[row][column])) {
                if (xFound == 1) {
                    priorityIncrement = 3;
                } else if (xFound == 2) {
                    printBoard();
                    System.out.println("You WIN!");
                    System.exit(0);
                    ;
                }
                xFound++;
            } else if ("O".equals(board[row][column])) {
                if (oFound == 1) {
                    priorityIncrement = 4;
                } else if (oFound == 2) {
                    printBoard();
                    System.out.println("Computer WINS!");
                    System.exit(0);
                }
                oFound++;
            }
        }
        if (xFound != 0 && oFound != 0) {
            priorityIncrement = -3;
        }
        if (pointToPrioritize != null && priority[pointToPrioritize.getX()][pointToPrioritize.getY()] > 0) {
            priority[pointToPrioritize.getX()][pointToPrioritize.getY()] += priorityIncrement;
        }
    }

    private static Point readUserInputAndSetCellPriority(Scanner scn) {
        String cellPointStr = scn.nextLine();
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
            // Takes care of the column and row cells
            priority[cellIndices[row + i]][cellIndices[column]] += 1;
            priority[cellIndices[row]][cellIndices[column + i]] += 1;
        }

    }

    private static void printBoard() {
        StringBuilder sb = new StringBuilder();
        for (int x = 0; x < board.length; x++) {
            for (int y = 0; y < board.length; y++) {
                if (x != 2 && board[x][y] == null) {
                    sb.append("\033[4m").append(" ").append("\033[0m");
                } else if (board[x][y] != null) {
                    sb.append("\033[4m").append(board[x][y]).append("\033[0m");
                } else {
                    sb.append(" ");
                }
                if (y != 2) {
                    sb.append("|");
                }
            }
            sb.append(System.lineSeparator());
        }
        System.out.println(sb.toString());
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
