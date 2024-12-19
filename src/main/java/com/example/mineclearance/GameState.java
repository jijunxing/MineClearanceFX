package com.example.mineclearance;

public class GameState {
    private int[][] mineField;
    private int[][] uncovered;
    private boolean gameOver = false;
    private boolean gameWon = false;
    private int round = 1;
    private int rows;
    private int cols;
    private int mines;
    private int minesLeft;

    //设置难度
    public GameState(String difficulty) {
        initializeGame(difficulty);
    }

    //根据难度初始化
    public void initializeGame(String difficulty) {
        switch (difficulty) {
            case "初级":
                rows = 9;
                cols = 9;
                mines = 10;
                break;
            case "中级":
                rows = 16;
                cols = 16;
                mines = 40;
                break;
            case "高级":
                rows = 16;
                cols = 30;
                mines = 99;
                break;
        }
        
        mineField = new int[rows + 2][cols + 2];
        uncovered = new int[rows + 2][cols + 2];
        minesLeft = mines;
        round = 1;
        gameOver = false;
        gameWon = false;
    }

    // Getters and setters
    public int[][] getMineField() { return mineField; }
    public int[][] getUncovered() { return uncovered; }
    public boolean isGameOver() { return gameOver; }
    public boolean isGameWon() { return gameWon; }
    public int getRound() { return round; }
    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public int getMines() { return mines; }
    public int getMinesLeft() { return minesLeft; }

    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }
    public void setGameWon(boolean gameWon) { this.gameWon = gameWon; }
    public void incrementRound() { this.round++; }
    public void decrementMinesLeft() { this.minesLeft--; }
    public void incrementMinesLeft() { this.minesLeft++; }
} 