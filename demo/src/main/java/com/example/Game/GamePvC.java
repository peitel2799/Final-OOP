package com.example.Game;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.asynchttpclient.*;
import org.json.JSONObject;

public class GamePvC {
        public static void main(String[] args) {
            List<String> fenAndMoves = getFenAndMoves();
            System.out.println(fenAndMoves);
        }
    
        private static List<String> getFenAndMoves() {
            List<String> fenAndMoves = new ArrayList<>();
            try {
                // Define the FEN string and depth
                String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
                // int depth = 12;
        
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
}
