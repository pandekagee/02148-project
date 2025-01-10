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
        String[] playerNames = new String[2];

        // Open a gate to expose the lobby
        repository.addGate("tcp://localhost:9001/?keep");

        System.out.println("Server started. Waiting for players to join...");

        while(true){
            // Wait for players to join
            lobby.get(new ActualField(0), new ActualField("join"), new ActualField(0));
            lobby.get(new ActualField(0), new ActualField("join"), new ActualField(0));

            System.out.println("Game start message sent to both players.");

            lobby.put(0, "joinMessage", 0);
            lobby.put(0, "joinMessage", 1);
        }
    }
}
