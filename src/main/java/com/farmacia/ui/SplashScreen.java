package com.farmacia.ui;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class SplashScreen {

    private Stage splashStage;
    private Runnable onFinishCallback;

    public SplashScreen() {
        this.splashStage = new Stage();
        this.splashStage.initStyle(StageStyle.UNDECORATED); // Sin bordes
    }

    public void show(Runnable onFinish) {
        this.onFinishCallback = onFinish;

        // Contenedor principal
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #ffffff, #f5f5f5);" +
            "-fx-padding: 40;" +
            "-fx-border-color: #cccccc;" +
            "-fx-border-width: 2;"
        );

        try {
            // Cargar logo
            Image logoImage = new Image(getClass().getResourceAsStream("/images/logo.png"));
            ImageView logoView = new ImageView(logoImage);
            logoView.setFitHeight(250);
            logoView.setFitWidth(250);
            logoView.setPreserveRatio(true);

            // Contenedor del logo con borde
            StackPane logoContainer = new StackPane(logoView);
            logoContainer.setStyle(
                "-fx-border-color: #cccccc;" +
                "-fx-border-width: 3;" +
                "-fx-border-radius: 10;" +
                "-fx-background-color: white;" +
                "-fx-background-radius: 10;" +
                "-fx-padding: 10;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 15, 0, 0, 3);"
            );

            root.getChildren().add(logoContainer);

        } catch (Exception e) {
            // Si no se puede cargar el logo, mostrar texto
            Label titleLabel = new Label("FarmaCiando");
            titleLabel.setStyle(
                "-fx-font-size: 48px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #2c3e50;"
            );
            root.getChildren().add(titleLabel);
        }

        // Subtítulo
        Label subtitleLabel = new Label("Sistema de Gestión de Farmacia");
        subtitleLabel.setStyle(
            "-fx-font-size: 18px;" +
            "-fx-text-fill: #7f8c8d;"
        );

        // Barra de progreso
        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(300);
        progressBar.setStyle(
            "-fx-accent: #3498db;"
        );

        // Label de carga
        Label loadingLabel = new Label("Cargando...");
        loadingLabel.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-text-fill: #95a5a6;"
        );

        // Versión
        Label versionLabel = new Label("v1.0.0 | Java 21 | Spring Boot 3.2.11");
        versionLabel.setStyle(
            "-fx-font-size: 12px;" +
            "-fx-text-fill: #bdc3c7;"
        );

        root.getChildren().addAll(subtitleLabel, progressBar, loadingLabel, versionLabel);

        // Crear escena con fondo transparente
        Scene scene = new Scene(root, 500, 550);
        scene.setFill(Color.TRANSPARENT);

        splashStage.setScene(scene);
        splashStage.centerOnScreen();

        // Animación de entrada (fade in)
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        // Mostrar el splash
        splashStage.show();

        // Animar la barra de progreso
        animateProgressBar(progressBar);

        // Cerrar después de 4 segundos
        PauseTransition pause = new PauseTransition(Duration.seconds(4));
        pause.setOnFinished(e -> closeSplash(root));
        pause.play();
    }

    private void animateProgressBar(ProgressBar progressBar) {
        // Simular carga progresiva
        javafx.animation.Timeline timeline = new javafx.animation.Timeline();
        
        for (int i = 0; i <= 100; i++) {
            final double progress = i / 100.0;
            javafx.animation.KeyFrame keyFrame = new javafx.animation.KeyFrame(
                Duration.millis(i * 35), // 3.5 segundos total
                e -> progressBar.setProgress(progress)
            );
            timeline.getKeyFrames().add(keyFrame);
        }
        
        timeline.play();
    }

    private void closeSplash(VBox root) {
        // Animación de salida (fade out)
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), root);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            splashStage.close();
            if (onFinishCallback != null) {
                onFinishCallback.run();
            }
        });
        fadeOut.play();
    }
}
