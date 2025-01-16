package dk.spilstuff.Server;

import java.io.IOException;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.SpaceRepository;

public class GameServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        // Create a repository and lobby space
        SpaceRepository repository = new SpaceRepository();
        SequentialSpace lobby = new SequentialSpace();
        repository.add("lobby", lobby);
        int[] lobbyCounts = new int[2];

        // Open a gate to expose the lobby
        repository.addGate("tcp://localhost:9001/?keep");
        System.out.println("Gate opened. Listening...");

        // Start a thread to check for joining players
        Thread joinThread = new Thread(() -> {
            while (true) {
                try {
                    int gameMode = (int)lobby.get(new ActualField(0), new ActualField("join"), new FormalField(Integer.class))[2];

                    lobbyCounts[gameMode]++;
                } catch (InterruptedException e) {
                    System.out.println("Thread interrupted, exiting loop.");
                    break; // Exit the loop if interrupted
                }
            }
        });

        // Start the thread
        joinThread.start();

        while(true){
            // Check if a lobby is full
            for(int i = 0; i < lobbyCounts.length; i++) {
                if(lobbyCounts[i] == 2) {
                    lobbyCounts[i] = 0;
                    
                    System.out.println("Gamemode " + i + " start message sent to both players.");

                    lobby.put(i, "joinMessage", 0);
                    lobby.put(i, "joinMessage", 1);
                }
            }
        }
    }
}
