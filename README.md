# Brick Break-in
The project of Introduction to Coordination in Distributed Applications (02148) made by group 16, consisting of Martin Handest [s224755] ([@tacecapSx](https://github.com/tacecapSx)) and Oskar Holland [s224768] ([@pandekagee](https://github.com/pandekagee)).

Brick Break-in is a real-time online multiplayer PvP game based on the retro hit Breakout. Two players face each other in head-to-head breakout combat to come out on top.

## Game mechanics
Following is a short explanation of the basic game mechanics.

### Bricks and powerups
Some bricks, when broken, drop powerups for the player who broke it. An extra ball can drop, or a super-extending powerup.

### Balls
The balls of breakout are assigned a colour based on the player who touched it first. Red balls damage the blue paddle when hit, and blue balls damage the red paddle when hit. Seek to deplenish your opponents health and win!

### Gamemodes
Brick Break-in has two gamemodes: BallBlockade and ColourSwap.

In BallBlockade, losing a ball damages you, but gives you the ball back to use further. Also, balls belonging to you will bounce off of the opponent's back wall. Aim to trap your balls on the opponent's side for massive damage!

In ColourSwap, balls can change colour by being hit by a paddle, and missing a ball makes it wrap around the screen, giving it to your opponent. Take control of your opponent's balls to pile on them!

## Setup
1. The [jSpace](https://github.com/pSpaces/jSpace) library is necessary to play the game, so make sure to install it with [Maven](https://maven.apache.org/download.cgi).
2. Make sure you are using JDK 23 or above, making sure the Java version in your path matches the version Maven uses. The below commands should output the same JDK 23 version:
```
java --version
mvn -v
```

## Hosting
To host the game server, launch `GameServer.java`, located in the Server folder. The server defaults to being hosted on `localhost:9001`, but insert any TCP address here.

## Playing
To play the game, make sure that the server is launched and ready. Make sure that the TCP address at the top of the `Game.java` class matches the one you defined when launching the server. Now, simply run `Game.java`, and the game will be compiled and launched. Now you just need a friend to do the same! Have fun!