package com.example.mineclearance;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.util.Random;

public class MineClearanceFX extends Application {
    private GameState gameState;
    private ResourceManager resourceManager;
    private ObservableList<Button> buttonList = FXCollections.observableArrayList();
    private Label timerLabel = new Label("时间: 00:00:00");
    private Timeline timeline;
    private int secondsElapsed = 0;
    private Button resetButton;
    private ComboBox<String> difficultyComboBox;
    private Label minesLeftLabel = new Label();

    @Override
    public void start(Stage primaryStage) {
        resourceManager = new ResourceManager();
        gameState = new GameState("初级");

        primaryStage.setTitle("扫雷游戏");

        Image icon = resourceManager.getBombImage();
        primaryStage.getIcons().add(icon);

        difficultyComboBox = UIFactory.createDifficultyComboBox();
        difficultyComboBox.setOnAction(event -> {
            String selectedDifficulty = difficultyComboBox.getValue();
            initGame(selectedDifficulty);
            resetGame();
        });

        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");

        // 创建重置按钮
        resetButton = new Button("重新开始");
        resetButton.getStyleClass().add("reset-button");
        resetButton.setOnAction(e -> resetGame());

        // 设置标签样式
        minesLeftLabel.getStyleClass().add("mines-label");
        timerLabel.getStyleClass().add("timer-label");

        root.setTop(UIFactory.createInfoPanel(difficultyComboBox, minesLeftLabel, resetButton, timerLabel));
        root.setCenter(createGridPane());

        initTimer();

        Scene scene = new Scene(root, 600, 650);
        try {
            String css = getClass().getResource("/styles.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception e) {
            System.err.println("无法��载样式文件: " + e.getMessage());
            e.printStackTrace();
        }
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(650);
        primaryStage.show();

        initGame("初级");
    }

    //创建雷区
    private GridPane createGridPane() {
        GridPane gridPane = new GridPane();
        gridPane.getStyleClass().add("game-grid");
        gridPane.setHgap(1);
        gridPane.setVgap(1);
        gridPane.setPadding(new Insets(10));
        gridPane.setAlignment(Pos.CENTER);
        return gridPane;
    }

    //根据难度初始化游戏
    private void initGame(String difficulty) {
        gameState = new GameState(difficulty);
        updateWindowSize();
        createButtons();
        updateMinesLeftLabel();
    }

    //设置窗口大小
    private void updateWindowSize() {
        int buttonSize = 35;
        int padding = 40;
        int headerHeight = 120;

        int contentWidth = gameState.getCols() * buttonSize + padding * 2;
        int contentHeight = gameState.getRows() * buttonSize + headerHeight + padding * 2;

        Stage stage = (Stage) difficultyComboBox.getScene().getWindow();
        stage.setWidth(Math.max(contentWidth, 500));
        stage.setHeight(Math.max(contentHeight, 550));
    }

    //���建可点击的格子
    private void createButtons() {
        GridPane gridPane = (GridPane) ((BorderPane) difficultyComboBox.getScene().getRoot()).getCenter();
        gridPane.getChildren().clear();
        buttonList.clear();

        for (int row = 1; row <= gameState.getRows(); row++) {
            for (int col = 1; col <= gameState.getCols(); col++) {
                createButton(gridPane, row, col);
            }
        }
    }

    //为格子添加图片和点击事件
    private void createButton(GridPane gridPane, int row, int col) {
        Button button = new Button();
        button.getStyleClass().add("game-button");

        ImageView unopenedView = new ImageView(resourceManager.getUnOpenedImage());
        unopenedView.setFitWidth(35);
        unopenedView.setFitHeight(35);
        unopenedView.setPreserveRatio(true);
        button.setGraphic(unopenedView);

        button.setOnAction(event -> handleButtonClick(button));
        button.setOnContextMenuRequested(event -> handleRightClick(button));
        gridPane.add(button, col, row);
        buttonList.add(button);
    }

    //左键点击格子逻辑
    private void handleButtonClick(Button button) {
        if (gameState.isGameOver() || gameState.isGameWon()) {
            return;
        }

        int row = GridPane.getRowIndex(button);
        int col = GridPane.getColumnIndex(button);

        // 如果已经插上旗子或者是问号，不能点击
        if (button.getGraphic() instanceof ImageView imageView
            && (imageView.getImage() == resourceManager.getFlagImage()
            || imageView.getImage() == resourceManager.getQuestionImage())) {
            return;
        }

        if (gameState.getRound() == 1) {
            initMineField(row, col);
            timeline.play();
        }

        if (gameState.getMineField()[row][col] == -1) {
            resourceManager.playSound("lose.mp3");
            button.setText("X");
            gameState.setGameOver(true);
            showAllMines();
            timeline.stop();
            return;
        }

        uncover(row, col);
        updateAllButtons();  // 更新所有已经揭开的按钮

        if (checkWin()) {
            gameState.setGameWon(true);
            resourceManager.playSound("win.mp3");
            showWinMessage();
            timeline.stop();
        }

        resourceManager.playSound("secure.mp3");
        gameState.incrementRound();
    }

    //右键点击格子逻辑
    private void handleRightClick(Button button) {
        if (gameState.isGameOver() || gameState.isGameWon()) return;

        resourceManager.playSound("rightClick.mp3");
        int row = GridPane.getRowIndex(button);
        int col = GridPane.getColumnIndex(button);

        if (gameState.getUncovered()[row][col] == 0) {
            if (button.getGraphic() instanceof ImageView imageView
                && imageView.getImage() == resourceManager.getUnOpenedImage()) {
                // 从未打开状态变为红旗
                ImageView flagView = new ImageView(resourceManager.getFlagImage());
                flagView.setFitWidth(35);
                flagView.setFitHeight(35);
                flagView.setPreserveRatio(true);
                button.setGraphic(flagView);
                gameState.decrementMinesLeft();
            } else if (button.getGraphic() instanceof ImageView imageView
                      && imageView.getImage() == resourceManager.getFlagImage()) {
                // 从红旗变为问号
                ImageView questionView = new ImageView(resourceManager.getQuestionImage());
                questionView.setFitWidth(35);
                questionView.setFitHeight(35);
                questionView.setPreserveRatio(true);
                button.setGraphic(questionView);
                gameState.incrementMinesLeft();
            } else {
                // 从问号变回未打开状态
                ImageView unopenedView = new ImageView(resourceManager.getUnOpenedImage());
                unopenedView.setFitWidth(35);
                unopenedView.setFitHeight(35);
                unopenedView.setPreserveRatio(true);
                button.setGraphic(unopenedView);
            }
            updateMinesLeftLabel();
        }
    }

    //放置地雷
    private void initMineField(int firstRow, int firstCol) {
        Random random = new Random();
        int minesPlaced = 0;

        // 放置地雷
        while (minesPlaced < gameState.getMines()) {
            int row = random.nextInt(gameState.getRows()) + 1;
            int col = random.nextInt(gameState.getCols()) + 1;

            // 确保第一次点击的位置及其周围没有地雷
            if (Math.abs(row - firstRow) <= 1 && Math.abs(col - firstCol) <= 1) {
                continue;
            }

            if (gameState.getMineField()[row][col] != -1) {
                gameState.getMineField()[row][col] = -1;
                minesPlaced++;
            }
        }

        // 计算每个格子周围的地雷数
        for (int i = 1; i <= gameState.getRows(); i++) {
            for (int j = 1; j <= gameState.getCols(); j++) {
                if (gameState.getMineField()[i][j] != -1) {
                    int count = 0;
                    for (int r = i - 1; r <= i + 1; r++) {
                        for (int c = j - 1; c <= j + 1; c++) {
                            if (gameState.getMineField()[r][c] == -1) {
                                count++;
                            }
                        }
                    }
                    gameState.getMineField()[i][j] = count;
                }
            }
        }
    }

    //揭开格子逻辑
    private void uncover(int x, int y) {
        // 检查边界和是否已经揭开
        if (x < 1 || x > gameState.getRows() || y < 1 || y > gameState.getCols()
            || gameState.getUncovered()[x][y] == 1) {
            return;
        }

        // 获取当前格子的按钮
        int index = (x - 1) * gameState.getCols() + (y - 1);
        Button button = buttonList.get(index);

        // 检查是否有红旗标记
        if (button.getGraphic() instanceof ImageView imageView 
            && imageView.getImage() == resourceManager.getFlagImage()) {
            gameState.incrementMinesLeft(); // 如果有红旗，增加剩余地雷数
            updateMinesLeftLabel();
        }

        // 标记为已揭开
        gameState.getUncovered()[x][y] = 1;

        // 如果是空白格（周围没有地雷），则递归揭开周围的格子
        if (gameState.getMineField()[x][y] == 0) {
            // 遍历周围8个格子
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i == 0 && j == 0) continue;  // 跳过当前格子
                    uncover(x + i, y + j);
                }
            }
        }
    }

    //更新格子图片处理
    private void updateAllButtons() {
        for (int i = 1; i <= gameState.getRows(); i++) {
            for (int j = 1; j <= gameState.getCols(); j++) {
                if (gameState.getUncovered()[i][j] == 1) {
                    int index = (i - 1) * gameState.getCols() + (j - 1);
                    Button button = buttonList.get(index);
                    updateButtonText(button);
                }
            }
        }
    }

    //更新格子图片
    private void updateButtonText(Button button) {
        int row = GridPane.getRowIndex(button);
        int col = GridPane.getColumnIndex(button);

        // 只更新已经揭开的格子
        if (gameState.getUncovered()[row][col] == 1) {
            if (gameState.getMineField()[row][col] == -1) {
                ImageView bombView = new ImageView(resourceManager.getBombImage());
                bombView.setFitWidth(35);
                bombView.setFitHeight(35);
                bombView.setPreserveRatio(true);
                button.setGraphic(bombView);
            } else {
                int number = gameState.getMineField()[row][col];
                ImageView numberView = new ImageView(resourceManager.getNumberImages()[number]);
                numberView.setFitWidth(35);
                numberView.setFitHeight(35);
                numberView.setPreserveRatio(true);
                button.setGraphic(numberView);
            }
            button.setText("");
        }
    }

    //检查是否胜利
    private boolean checkWin() {
        for (int i = 1; i <= gameState.getRows(); i++) {
            for (int j = 1; j <= gameState.getCols(); j++) {
                if (gameState.getUncovered()[i][j] != 1 && gameState.getMineField()[i][j] != -1) {
                    return false;
                }
            }
        }
        return true;
    }

    //重新开始游戏
    private void resetGame() {
        gameState.initializeGame(difficultyComboBox.getValue());
        secondsElapsed = 0;
        timerLabel.setText("时间: 00:00:00");
        timeline.stop();

        for (Button button : buttonList) {
            ImageView unopenedView = new ImageView(resourceManager.getUnOpenedImage());
            unopenedView.setFitWidth(35);
            unopenedView.setFitHeight(35);
            unopenedView.setPreserveRatio(true);
            button.setGraphic(unopenedView);
            button.setText("");
        }

        updateMinesLeftLabel();
    }

    //显示所有地雷
    private void showAllMines() {
        for (int i = 1; i <= gameState.getRows(); i++) {
            for (int j = 1; j <= gameState.getCols(); j++) {
                if (gameState.getMineField()[i][j] == -1) {
                    int index = (i - 1) * gameState.getCols() + j - 1;
                    Button button = buttonList.get(index);
                    ImageView bombView = new ImageView(resourceManager.getBombImage());
                    bombView.setFitWidth(35);
                    bombView.setFitHeight(35);
                    bombView.setPreserveRatio(true);
                    button.setGraphic(bombView);
                    button.setText("");
                }
            }
        }
    }

    //弹出胜利框
    private void showWinMessage() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("游戏胜利");
        alert.setHeaderText(null);
        alert.setContentText("恭喜你赢得了游戏！");
        alert.showAndWait();
    }

    //初始化计时器
    private void initTimer() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            secondsElapsed++;
            int hours = secondsElapsed / 3600;
            int minutes = (secondsElapsed % 3600) / 60;
            int seconds = secondsElapsed % 60;
            timerLabel.setText(String.format("时间: %02d:%02d:%02d", hours, minutes, seconds));
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    //更新剩余地雷数量
    private void updateMinesLeftLabel() {
        minesLeftLabel.setText("剩余地雷: " + gameState.getMinesLeft());
    }

    public static void main(String[] args) {launch();}
}

