package dk.spilstuff.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;

public class GameClient {
    public static void main(String[] args) throws IOException, InterruptedException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        // Connect to the lobby
        RemoteSpace lobby = new RemoteSpace("tcp://localhost:9001/lobby?keep");

        System.out.print("Enter your name: ");
        String playerName = reader.readLine();

        // Join the game
        lobby.put("join", playerName);
        System.out.println(playerName + " has joined the game. Waiting for the game to start...");

        new Thread(() -> {
            try {
                while (true) {
                    Object[] movement = lobby.get(new ActualField("update"), new ActualField(playerName), new FormalField(Integer.class));
                    int y = (int) movement[2];
                    System.out.println("Incomming Movement from The other player: y=" + y);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        // Handle keyboard input for movement
        handleKeyboardInput(lobby, playerName);

        // Wait for the "Game start" message
        Object[] message = lobby.get(new ActualField("message"), new ActualField(playerName), new FormalField(String.class));
        System.out.println("Server: " + message[2]);
    }

    public static void handleKeyboardInput(RemoteSpace lobby, String playerName) throws IOException, InterruptedException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        int y = 0;
    
        System.out.println("Press 'W' to increase y, 'S' to decrease y, or 'Q' to quit.");
    
        while (true) {
            String input = reader.readLine().toUpperCase();
    
            switch (input) {
                case "W":
                    y++;
                    System.out.println("y increased: " + y);
                    // Broadcast movement to other clients
                    lobby.put("movement", playerName, y);
                    break;
                case "S":
                    y--;
                    System.out.println("y decreased: " + y);
                    // Broadcast movement to other clients
                    lobby.put("movement", playerName, y);
                    break;
                case "Q":
                    System.out.println("Exiting program. Final value of y: " + y);
                    return; // Exit the loop and terminate the program
                default:
                    System.out.println("Invalid input. Use 'W', 'S', or 'Q'.");
            }
        }
    }
    
}