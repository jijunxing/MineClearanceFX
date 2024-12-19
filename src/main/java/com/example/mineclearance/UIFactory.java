package com.example.mineclearance;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class UIFactory {
    public static ComboBox<String> createDifficultyComboBox() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setItems(FXCollections.observableArrayList("初级", "中级", "高级"));
        comboBox.setValue("初级");
        comboBox.getStyleClass().add("difficulty-combo");
        return comboBox;
    }

    //创建界面控件
    public static HBox createInfoPanel(ComboBox<String> difficultyComboBox, 
                                     Label minesLeftLabel, 
                                     Button resetButton, 
                                     Label timerLabel) {
        HBox infoPanel = new HBox();
        infoPanel.getStyleClass().add("info-panel");
        
        HBox difficultyBox = createDifficultyBox(difficultyComboBox);
        
        Region spacer1 = new Region();
        Region spacer2 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        
        infoPanel.getChildren().addAll(
            difficultyBox,
            spacer1,
            minesLeftLabel,
            resetButton,
            spacer2,
            timerLabel
        );
        
        return infoPanel;
    }

    //创建难度下拉选择框
    private static HBox createDifficultyBox(ComboBox<String> difficultyComboBox) {
        HBox difficultyBox = new HBox();
        difficultyBox.getStyleClass().add("difficulty-box");
        
        Label diffLabel = new Label("难度:");
        diffLabel.getStyleClass().add("difficulty-label");
        
        difficultyBox.getChildren().addAll(diffLabel, difficultyComboBox);
        return difficultyBox;
    }
} 