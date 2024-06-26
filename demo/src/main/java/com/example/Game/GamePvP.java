package com.example.Game;

import java.net.URLEncoder;
import org.asynchttpclient.*;
import org.json.JSONObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.swing.JOptionPane;

import com.example.Main;
import com.example.Piece.Bishop;
import com.example.Piece.King;
import com.example.Piece.Knight;
import com.example.Piece.Pawn;
import com.example.Piece.Piece;
import com.example.Piece.Queen;
import com.example.Piece.Rook;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.FontWeight;
import javafx.util.Pair;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

public class GamePvP extends Rule{
	public static String startFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";
    static Stopwatch w_stopwatch = new Stopwatch();
    static Stopwatch b_stopwatch = new Stopwatch();
    public GamePvP(GraphicsContext gc1 , Mouse mouse, Canvas canvas){
        gc = gc1;
        c = canvas;
        setPieces(startFen);
        game_mouse = mouse;
        copyPieces(pieces, simPieces);
    }
    private static List<String> getFenAndMoves() { // getFenAndMoves from RapidAPI using AssyhcHttpClient
		List<String> fenAndMoves = new ArrayList<>();
		try {
			// Define the FEN string and depth
			String fen = pieceToFen();
			int depth = 12;
	
			// Encode the FEN string to be URL safe
			String encodedFEN = URLEncoder.encode(fen, "UTF-8");
	
			// Form the URL
			String apiUrl = "https://chess-stockfish-16-api.p.rapidapi.com/chess/api";
	
			// Create AsyncHttpClient
			AsyncHttpClient client = new DefaultAsyncHttpClient();
	
			// Send POST request
			String responseBody = client.preparePost(apiUrl)
				.addHeader("content-type", "application/x-www-form-urlencoded")
				.addHeader("X-RapidAPI-Key", "6a1ed3e927msh2d8d4c93672832dp14f057jsn63c9b950ae4f")
				.addHeader("X-RapidAPI-Host", "chess-stockfish-16-api.p.rapidapi.com")
				.setBody("fen=" + encodedFEN)
				.execute()
				.toCompletableFuture()
				.thenApply(Response::getResponseBody)
				.join();
	
			JSONObject json = new JSONObject(responseBody);
			String bestMove = json.getString("bestmove");
	
			fenAndMoves.add(fen);
			fenAndMoves.add(bestMove);
	
			// Close the client
			client.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fenAndMoves;
	}
    public static String pieceToFen() { // get the FEN string from the pieces
		char[][] board = new char[8][8];
	
		// Initialize the board with empty squares
		for (char[] row : board) {
			Arrays.fill(row, '1');
		}
	
		// Place the pieces on the board
		for (Piece piece : pieces) {
			char pieceChar;
			if (piece instanceof Pawn) {
				pieceChar = piece.color == 0 ? 'P' : 'p';
			} else if (piece instanceof Rook) {
				pieceChar = piece.color == 0 ? 'R' : 'r';
			} else if (piece instanceof Knight) {
				pieceChar = piece.color == 0 ? 'N' : 'n';
			} else if (piece instanceof Bishop) {
				pieceChar = piece.color == 0 ? 'B' : 'b';
			} else if (piece instanceof Queen) {
				pieceChar = piece.color == 0 ? 'Q' : 'q';
			} else if (piece instanceof King) {
				pieceChar = piece.color == 0 ? 'K' : 'k';
			} else {
				continue;
			}
			board[piece.row][piece.col] = pieceChar;
		}
	
		// Convert the board to FEN format
		StringBuilder fen = new StringBuilder();
		for (int i = 0; i < 8; i++) {
			int emptyCount = 0;
			for (int j = 0; j < 8; j++) {
				if (board[i][j] == '1') {
					emptyCount++;
				} else {
					if (emptyCount != 0) {
						fen.append(emptyCount);
						emptyCount = 0;
					}
					fen.append(board[i][j]);
				}
			}
			if (emptyCount != 0) {
				fen.append(emptyCount);
			}
			if (i != 7) {
				fen.append('/');
			}
		}
        fen.append(' ');
    	fen.append(currentColor == 0 ? 'w' : 'b');
		// Append the castling information
		// fen.append(' ');
		// fen.append("KQkq");
	
		// // Append the en passant target square ("-"), halfmove clock ("0"), and fullmove number ("1")
		// fen.append(" - 0 1");
		return fen.toString();
	}
    
    public void gameloop(){
        new AnimationTimer() {
            double drawInterval = 1000000000 / FPS;
            double delta = 0;
            long LastTime = System.nanoTime();
            int count = 0;

            @Override
            public void handle(long now) {
                delta += (now - LastTime) / drawInterval;
                LastTime = now;
                if(delta >= 1){
                    update();
                    gc.clearRect(0, 0, c.getWidth(), c.getHeight());
                    render();
                    delta--;
                    count++;
                }
                if(count == FPS){
                    Timing();
                    count = 0;
                }
            }
        }.start();
    }
    static void Timing(){
        if(gameOver || stalemate) return;
        if(currentColor == WHITE){
            w_stopwatch.setSeconds(w_stopwatch.getSeconds() + 1);
            if(w_stopwatch.getSeconds() == 60){
                w_stopwatch.setMinutes(w_stopwatch.getMinutes() + 1);
                w_stopwatch.setSeconds(0);
            }
        }
        else{
            b_stopwatch.setSeconds(b_stopwatch.getSeconds() + 1);
            if(b_stopwatch.getSeconds() == 60){
                b_stopwatch.setMinutes(b_stopwatch.getMinutes() + 1);
                b_stopwatch.setSeconds(0);
            }
        }
    }
    static public void reset(){
        w_stopwatch.reset();
        b_stopwatch.reset();
        currentColor = WHITE;
        pieces.clear();
        simPieces.clear();
        promoPieces.clear();
        activeP = null;
        checkingP = null;
        castlingP = null;
        all_move.clear();
        isPromo = false;
        gameOver = false;
        stalemate = false;
        setPieces(startFen);
        copyPieces(pieces, simPieces);
    }
    private void update(){
    	
        if(isPromo){
            promoting();
        }else if(!gameOver && !stalemate){
            if(game_mouse.isPressed()){

                if(activeP == null){
                    for(Piece p: simPieces){
                        if( p.color == currentColor && p.row == game_mouse.y / Board.SQUARE_SIZE && p.col == game_mouse.x / Board.SQUARE_SIZE){
                            activeP = p;
                        }
                    }
                    if(activeP != null) getAllMove();
                }
                else{
                    // if player holding a piece
                    simulate();
                }
            }
            if(!game_mouse.isPressed()){
                //if player release the piece
                if(activeP != null){
                    if(activeP.isSamePosition(activeP.row, activeP.col)){
                        activeP.resetPosition();                
                    }
                    else{
                        // Move is confirmed
                        if(validSquare){
                            copyPieces(simPieces, pieces);
                            activeP.updatePosition();
                            // Check end game condition
                            if(isKingInCheck() && isCheckmate()){
                                    gameOver = true;
                            }
                            else if(isKingInCheck() &&isStalemate()){
                                stalemate = true;
                            }
                            else{
                                // normal move
                                if(castlingP != null){
                                    castlingP.updatePosition();
                                }
                                if(checkPromo()){
                                    isPromo = true;
                                }else{
                                    activeP = null;
                                    String fen = pieceToFen();
                                    System.out.println(fen);
                                    changeTurn();
                                    
                                }
                            }
                        }
                        else{
                            // Move is not completed
                            copyPieces(pieces, simPieces);
                            activeP.resetPosition();
                            activeP = null;
                        }
                    }
                }
            }
        }
        else if(gameOver || stalemate){
            // this mouse event can be modified to any event
            if(game_mouse.isPressed()){
                reset();
            }
        }
    }
    private void simulate(){
        canMove = false;
        validSquare = false;

        copyPieces(pieces, simPieces);

        // reset the position of the castling rook
        if(castlingP != null){
            castlingP.col = castlingP.preCol;
            castlingP.x = castlingP.getX(castlingP.col);
            castlingP = null;
        }

        activeP.x = game_mouse.x - Board.HALF_SQUARE_SIZE;
        activeP.y = game_mouse.y - Board.HALF_SQUARE_SIZE;
        
        if(game_mouse.x <= Board.HALF_SQUARE_SIZE ){
            activeP.x = 0;
            if(game_mouse.y <= Board.HALF_SQUARE_SIZE) activeP.y = 0;
            else if(game_mouse.y >= Board.SQUARE_SIZE * Board.MAX_ROW - Board.HALF_SQUARE_SIZE) activeP.y = Board.SQUARE_SIZE * Board.MAX_ROW - Board.SQUARE_SIZE;
        }else if(game_mouse.x >= Board.SQUARE_SIZE * Board.MAX_COL - Board.HALF_SQUARE_SIZE){
            activeP.x = Board.SQUARE_SIZE * Board.MAX_COL - Board.SQUARE_SIZE;
            if(game_mouse.y <= Board.HALF_SQUARE_SIZE) activeP.y = 0;
            else if(game_mouse.y >= Board.SQUARE_SIZE * Board.MAX_ROW - Board.HALF_SQUARE_SIZE) activeP.y = Board.SQUARE_SIZE * Board.MAX_ROW - Board.SQUARE_SIZE;
        }else if (game_mouse.y <= Board.HALF_SQUARE_SIZE ){
            activeP.y = 0;
            if(game_mouse.x <= Board.HALF_SQUARE_SIZE) activeP.x = 0;
            else if(game_mouse.x >= Board.SQUARE_SIZE * Board.MAX_COL - Board.HALF_SQUARE_SIZE) activeP.x = Board.SQUARE_SIZE * Board.MAX_COL - Board.SQUARE_SIZE;
        }else if(game_mouse.y >= Board.SQUARE_SIZE * Board.MAX_ROW - Board.HALF_SQUARE_SIZE ){
            activeP.y = Board.SQUARE_SIZE * Board.MAX_ROW - Board.SQUARE_SIZE;
            if(game_mouse.x <= Board.HALF_SQUARE_SIZE) activeP.x = 0;
            else if(game_mouse.x >= Board.SQUARE_SIZE * Board.MAX_COL - Board.HALF_SQUARE_SIZE) activeP.x = Board.SQUARE_SIZE * Board.MAX_COL - Board.SQUARE_SIZE;
        }

        activeP.row = activeP.getRow(activeP.y);
        activeP.col = activeP.getCol(activeP.x);

        // Check if the piece can move to the target square
        if(activeP.canMove(activeP.row, activeP.col)){
            canMove = true;
            
            // if player is holding a piece and moving through other pieces, they will be removed from the simPieces
            // That the reason why we need to reset the simPieces after any loop
            if(activeP.affectedP != null){
                simPieces.remove(activeP.affectedP.getIndex());
            }
            castling();
            // if player's king is not in check after the opponent's move, the move is valid
            if(!isKingIllegalMove(activeP) && opponentCanCaptureKing() == false){
                validSquare = true;
            }
        }
    }
    private void render(){
        //Draw Background
        gc.drawImage(background, 0, 0, Main.WIDTH, Main.HEIGHT);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(10);
        gc.strokeLine(800, 0, 800, Main.HEIGHT);

        //Draw Board
        Board.draw(gc);

        //Draw pieces
        for(Piece p : pieces){
            p.draw(gc);
        }
        
        //Active Piece
        if(activeP != null){
            gc.setGlobalAlpha(0.3); // Set opacity to 50%
            gc.setFill(Color.WHITE);
            gc.fillRect(activeP.preCol * Board.SQUARE_SIZE, activeP.preRow * Board.SQUARE_SIZE, Board.SQUARE_SIZE, Board.SQUARE_SIZE);
            gc.setGlobalAlpha(0.7);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(Board.SQUARE_SIZE / 20);
            int arcSize = 20;
            gc.strokeRoundRect(activeP.col * Board.SQUARE_SIZE, activeP.row * Board.SQUARE_SIZE, Board.SQUARE_SIZE, Board.SQUARE_SIZE, arcSize , arcSize);
            
            if(!all_move.isEmpty()){
                for(Pair<Integer, Integer> pos: all_move){
                    int row = (int) pos.getKey();
                    int col = (int) pos.getValue();
                    boolean hit = false;
                    gc.setFill(Color.BLACK);
                    gc.setGlobalAlpha(0.3);
                    for(Piece p: pieces){
                        if(p.row == row && p.col == col && p.color != activeP.color){
                            hit = true;
                        }
                    }
                    if(hit){
                        int temp = Board.HALF_SQUARE_SIZE / 15;
                        gc.setStroke(Color.BLACK);
                        gc.setLineWidth(Board.SQUARE_SIZE / 12);
                        gc.strokeOval(col * Board.SQUARE_SIZE + temp, row * Board.SQUARE_SIZE+temp, Board.SQUARE_SIZE- 2*temp, Board.SQUARE_SIZE-2*temp);
                    }else gc.fillOval(col * Board.SQUARE_SIZE + Board.HALF_SQUARE_SIZE/5 *3, row * Board.SQUARE_SIZE + Board.HALF_SQUARE_SIZE / 5 *3, Board.SQUARE_SIZE/10 *4, Board.SQUARE_SIZE/10 *4);;
                }
            }
            gc.setGlobalAlpha(1);
            activeP.draw(gc);
        }

        //MESSAGE
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 30));
        gc.setFontSmoothingType(FontSmoothingType .LCD);

        if(gameOver){
            gc.setLineWidth(200);
            gc.setStroke(Color.WHITE);
            gc.setGlobalAlpha(0.5);
            gc.strokeLine(0, Main.HEIGHT/2, Main.WIDTH, Main.HEIGHT/2);
            gc.setGlobalAlpha(1);
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 100));
            if(currentColor == BLACK){
                gc.fillText("BLACK WINS", 200, Main.HEIGHT/2 +35);
            }else{
                gc.fillText("WHITE WINS", 200, Main.HEIGHT/2 +35);
            }
        }
        else if(stalemate){
            gc.setLineWidth(200);
            gc.setStroke(Color.WHITE);
            gc.setGlobalAlpha(0.5);
            gc.strokeLine(0, Main.HEIGHT/2, Main.WIDTH, Main.HEIGHT/2);
            gc.setGlobalAlpha(1);
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 100));
            gc.fillText("STALEMATE", 200, Main.HEIGHT/2 +35);
        }
        else{
            if(isPromo){
                gc.fillText("Promote to: ", 850, 180);
                for(Piece p: promoPieces){
                    p.draw(gc);
                }
            }
            // NORMAL
            else{
                if(currentColor == WHITE){
                    gc.fillText("WHITE TURN", 850, 650);
                    if( checkingP != null && checkingP.color == BLACK){
                        gc.setFill(Color.RED);
                        gc.fillText("The King", 880, 380);
                        gc.fillText("is in Check", 870, 420);
                    }
                } else {
                    gc.fillText("BLACK TURN", 850, 150);
                    if( checkingP != null && checkingP.color == WHITE){
                        gc.setFill(Color.RED);
                        gc.fillText("The King", 880, 380);
                        gc.fillText("is in Check", 870, 420);
                    }
                }
            }
        }

        //Stopwatch
        gc.setFill(Color.WHITE);
        gc.setGlobalAlpha(0.5);
        gc.fillRoundRect(825, 10, 250, 80, 40, 40);
        gc.fillRoundRect(825, 710, 250, 80 , 40 , 40);
        gc.setGlobalAlpha(1);
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        gc.fillText(w_stopwatch.getTime(), 900, 763);
        gc.fillText(b_stopwatch.getTime(), 900, 63);
    }
}