package com.example;

import com.example.Game.GamePvC;
import com.example.Game.GamePvC1;
import com.example.Game.GamePvP1;
import com.example.Game.GamePvP;
import com.example.Game.Puzzle;
import com.example.Game.Mouse;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    private static Scene scene;

    Mouse mouse = new Mouse();
    public static final int WIDTH = 1100;
    public static final int HEIGHT = 800;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Chess");
        primaryStage.setResizable(false);
        
        // Game scene
        StackPane root= new StackPane();
        Canvas canvas = new Canvas(WIDTH, HEIGHT );
        root.getChildren().addAll(canvas);
        root.setStyle("-fx-background-color: black;");
        canvas.setOnMousePressed(e -> {
            mouse.setPressed(true);
        });
        canvas.setOnMouseMoved(e -> {
            mouse.x = (int) e.getX();
            mouse.y = (int) e.getY();
        });
        canvas.setOnMouseDragged(e -> {
            mouse.x = (int) e.getX();	
            mouse.y = (int) e.getY();
        });
        canvas.setOnMouseReleased(e -> {
            mouse.setPressed(false);
        });

        // Có thể thay đổi game từ đây
        GraphicsContext gc = canvas.getGraphicsContext2D();
        // Puzzle game  = new Puzzle(gc, mouse , canvas);
        GamePvC1 game  = new GamePvC1(gc, mouse , canvas);
        // GamePvP1 game = new GamePvP1(gc, mouse, canvas);
        // GamePvP game = new GamePvP(gc, mouse, canvas);
        game.gameloop();
        List<String> fenAndMoves = getFenAndMoves();
        System.out.println(fenAndMoves);
        game.board.set_BoardColor(1);            // Set color of the board
        //game.initialize_color(false);            // Set the color of the first player
        
        
        // Set up the scene
        scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
        
    }
}