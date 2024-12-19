package com.example.mineclearance;

import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class ResourceManager {
    private static final String IMAGE_PATH = "/images/";
    private static final String SOUND_PATH = "/sound/";

    private Image[] numberImages = new Image[9];
    private Image bombImage;
    private Image flagImage;
    private Image questionImage;
    private Image unopenedImage;

    public ResourceManager() {
        try {
            loadImages();
        } catch (Exception e) {
            System.out.println("资源加载失败: " + e.getMessage());
        }
    }

    private void loadImages() throws Exception {
        for (int i = 0; i <= 8; i++) {
            numberImages[i] = loadImage(IMAGE_PATH + i + ".gif");
        }
        bombImage = loadImage(IMAGE_PATH + "bomb.gif");
        flagImage = loadImage(IMAGE_PATH + "flag.gif");
        questionImage = loadImage(IMAGE_PATH + "question.gif");
        unopenedImage = loadImage(IMAGE_PATH + "unopened.gif");
    }

    //加载图片
    private Image loadImage(String path) throws Exception {
        var resourceStream = getClass().getResourceAsStream(path);
        if (resourceStream == null) throw new Exception("找不到图片资源: " + path);
        return new Image(resourceStream);
    }

    //播放音乐
    public void playSound(String soundFileName) {
        try {
            var resource = getClass().getResource(SOUND_PATH + soundFileName);
            if (resource == null) {
                System.out.println("找不到音频文件: " + soundFileName);
                return;
            }

            var media = new Media(resource.toURI().toString());
            var mediaPlayer = new MediaPlayer(media);

            mediaPlayer.setOnReady(mediaPlayer::play);
            mediaPlayer.setOnEndOfMedia(mediaPlayer::dispose);

        } catch (Exception e) {
            System.out.println("播放音频时出错: " + e.getMessage());
        }
    }

    // Getters for images
    public Image[] getNumberImages() { return numberImages; }
    public Image getBombImage() { return bombImage; }
    public Image getFlagImage() { return flagImage; }
    public Image getQuestionImage() { return questionImage; }
    public Image getUnOpenedImage() { return unopenedImage; }
}
