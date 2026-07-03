## Domino

A simple Java Swing implementation of block dominoes (double-six set), played against a basic AI opponent.

### Rules implemented

- 28-tile double-six set, 7 tiles dealt to each player, the rest form the boneyard.
- Whoever holds the highest double starts.
- Click a tile in your hand to play it against a matching open end of the board; if it fits both ends you'll be asked which side to play.
- Click **Draw** to pick up a tile from the boneyard when you can't play.
- Click **Pass** if the boneyard is empty and you have no valid move.
- First player to empty their hand wins. If the game blocks (boneyard empty, neither player can move), the player with the lower pip count in hand wins.

### Folder structure

- `src`: sources (`App`, `Domino`, `Tile`, `DominoTileView`)
- `lib`: dependencies (none currently)
- `bin`: compiled output (VS Code default)

### Run

```
javac -d bin src/*.java
java -cp bin App
```
