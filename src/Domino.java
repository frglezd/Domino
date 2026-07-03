import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.*;
import javax.swing.Timer;

public class Domino {
    static final int TILE_W = 70;
    static final int TILE_H = 130;
    static final int SIDE = -1, LEFT = 0, RIGHT = 1;

    JFrame frame = new JFrame("Domino");
    JLabel statusLabel = new JLabel();
    JLabel boneyardLabel = new JLabel();
    JPanel boardPanel = new JPanel();
    JPanel playerHandPanel = new JPanel();
    JPanel aiHandPanel = new JPanel();
    JButton drawButton = new JButton("Draw");
    JButton passButton = new JButton("Pass");
    JButton newGameButton = new JButton("New Game");

    List<Tile> boneyard = new ArrayList<>();
    List<Tile> playerHand = new ArrayList<>();
    List<Tile> aiHand = new ArrayList<>();
    List<int[]> boardTiles = new ArrayList<>();

    int leftEnd, rightEnd;
    boolean boardEmpty = true;
    boolean playerTurn = true;
    boolean gameOver = false;

    Domino() {
        frame.setSize(1000, 750);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(new Color(20, 90, 60));

        buildTopPanel();
        buildBoardPanel();
        buildHandPanels();
        buildControlPanel();

        frame.setVisible(true);
        newGame();
    }

    void buildTopPanel() {
        statusLabel.setBackground(Color.darkGray);
        statusLabel.setForeground(Color.white);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 26));
        statusLabel.setHorizontalAlignment(JLabel.CENTER);
        statusLabel.setOpaque(true);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        frame.add(statusLabel, BorderLayout.NORTH);
    }

    void buildBoardPanel() {
        boardPanel.setBackground(new Color(20, 90, 60));
        boardPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 6, 10));
        JScrollPane scroll = new JScrollPane(boardPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(new Color(20, 90, 60));
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        frame.add(scroll, BorderLayout.CENTER);
    }

    void buildHandPanels() {
        aiHandPanel.setBackground(new Color(15, 70, 47));
        aiHandPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 6, 6));
        aiHandPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        playerHandPanel.setBackground(new Color(15, 70, 47));
        playerHandPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 6, 6));
        playerHandPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    }

    void buildControlPanel() {
        JPanel south = new JPanel();
        south.setLayout(new BorderLayout());
        south.setBackground(new Color(15, 70, 47));

        JPanel controls = new JPanel();
        controls.setBackground(new Color(15, 70, 47));
        boneyardLabel.setForeground(Color.white);
        boneyardLabel.setFont(new Font("Arial", Font.BOLD, 16));

        styleButton(drawButton);
        styleButton(passButton);
        styleButton(newGameButton);

        drawButton.addActionListener(e -> onDraw());
        passButton.addActionListener(e -> onPass());
        newGameButton.addActionListener(e -> newGame());

        controls.add(boneyardLabel);
        controls.add(drawButton);
        controls.add(passButton);
        controls.add(newGameButton);

        south.add(aiHandPanel, BorderLayout.NORTH);
        south.add(controls, BorderLayout.CENTER);
        south.add(playerHandPanel, BorderLayout.SOUTH);
        frame.add(south, BorderLayout.SOUTH);
    }

    void styleButton(JButton button) {
        button.setBackground(Color.darkGray);
        button.setForeground(Color.white);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setFocusable(false);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
    }

    void newGame() {
        boneyard.clear();
        playerHand.clear();
        aiHand.clear();
        boardTiles.clear();
        boardEmpty = true;
        gameOver = false;

        for (int i = 0; i <= 6; i++) {
            for (int j = i; j <= 6; j++) {
                boneyard.add(new Tile(i, j));
            }
        }
        Collections.shuffle(boneyard);

        for (int i = 0; i < 7; i++) {
            playerHand.add(boneyard.remove(boneyard.size() - 1));
            aiHand.add(boneyard.remove(boneyard.size() - 1));
        }

        playerTurn = determineStarter();
        setStatus(playerTurn ? "You start! Click a tile to play." : "AI starts...");
        refresh();

        if (!playerTurn) {
            Timer timer = new Timer(700, e -> aiTurn());
            timer.setRepeats(false);
            timer.start();
        }
    }

    boolean determineStarter() {
        int playerBest = highestDouble(playerHand);
        int aiBest = highestDouble(aiHand);
        if (playerBest == -1 && aiBest == -1) return true;
        return playerBest >= aiBest;
    }

    int highestDouble(List<Tile> hand) {
        int best = -1;
        for (Tile t : hand) {
            if (t.isDouble() && t.left > best) best = t.left;
        }
        return best;
    }

    void onHandTileClicked(int index) {
        if (gameOver || !playerTurn) return;
        Tile tile = playerHand.get(index);

        if (boardEmpty) {
            placeTile(tile, SIDE);
            playerHand.remove(index);
            afterPlayerMove();
            return;
        }

        boolean fitsLeft = tile.matches(leftEnd);
        boolean fitsRight = tile.matches(rightEnd);

        if (!fitsLeft && !fitsRight) {
            setStatus("That tile doesn't fit either end. Try another or Draw.");
            return;
        }

        int side;
        if (fitsLeft && fitsRight && leftEnd != rightEnd) {
            int choice = JOptionPane.showOptionDialog(frame, "Play on which end?", "Choose a side",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                    new Object[]{"Left (" + leftEnd + ")", "Right (" + rightEnd + ")"}, "Left (" + leftEnd + ")");
            side = choice == 1 ? RIGHT : LEFT;
        } else {
            side = fitsLeft ? LEFT : RIGHT;
        }

        placeTile(tile, side);
        playerHand.remove(index);
        afterPlayerMove();
    }

    void afterPlayerMove() {
        if (playerHand.isEmpty()) {
            endGame("You win! Your hand is empty.");
            return;
        }
        playerTurn = false;
        refresh();
        setStatus("AI is thinking...");
        Timer timer = new Timer(700, e -> aiTurn());
        timer.setRepeats(false);
        timer.start();
    }

    void placeTile(Tile tile, int side) {
        if (boardEmpty) {
            boardTiles.add(new int[]{tile.left, tile.right});
            leftEnd = tile.left;
            rightEnd = tile.right;
            boardEmpty = false;
            return;
        }
        if (side == LEFT) {
            if (tile.right == leftEnd) {
                boardTiles.add(0, new int[]{tile.left, tile.right});
                leftEnd = tile.left;
            } else {
                boardTiles.add(0, new int[]{tile.right, tile.left});
                leftEnd = tile.right;
            }
        } else {
            if (tile.left == rightEnd) {
                boardTiles.add(new int[]{tile.left, tile.right});
                rightEnd = tile.right;
            } else {
                boardTiles.add(new int[]{tile.right, tile.left});
                rightEnd = tile.left;
            }
        }
    }

    void onDraw() {
        if (gameOver || !playerTurn || boneyard.isEmpty()) return;
        playerHand.add(boneyard.remove(boneyard.size() - 1));
        setStatus("You drew a tile.");
        refresh();
    }

    void onPass() {
        if (gameOver || !playerTurn) return;
        playerTurn = false;
        setStatus("You passed. AI is thinking...");
        refresh();
        Timer timer = new Timer(700, e -> aiTurn());
        timer.setRepeats(false);
        timer.start();
    }

    void aiTurn() {
        if (gameOver) return;

        Tile toPlay = null;
        int side = SIDE;
        for (Tile t : aiHand) {
            if (boardEmpty) {
                toPlay = t;
                break;
            }
            if (t.matches(leftEnd)) {
                toPlay = t;
                side = LEFT;
                break;
            }
            if (t.matches(rightEnd)) {
                toPlay = t;
                side = RIGHT;
                break;
            }
        }

        if (toPlay == null && !boneyard.isEmpty()) {
            aiHand.add(boneyard.remove(boneyard.size() - 1));
            refresh();
            Timer timer = new Timer(500, e -> aiTurn());
            timer.setRepeats(false);
            timer.start();
            return;
        }

        if (toPlay == null) {
            setStatus("AI can't move and passes.");
            playerTurn = true;
            refresh();
            checkBlocked();
            return;
        }

        placeTile(toPlay, side);
        aiHand.remove(toPlay);

        if (aiHand.isEmpty()) {
            endGame("AI wins! Its hand is empty.");
            return;
        }

        playerTurn = true;
        setStatus("Your turn — click a tile to play, or Draw.");
        refresh();
    }

    void checkBlocked() {
        boolean playerCanMove = boardEmpty || canPlay(playerHand);
        boolean aiCanMove = boardEmpty || canPlay(aiHand);
        if (boneyard.isEmpty() && !playerCanMove && !aiCanMove) {
            int playerPips = pipSum(playerHand);
            int aiPips = pipSum(aiHand);
            if (playerPips < aiPips) endGame("Game blocked! You win with fewer pips.");
            else if (aiPips < playerPips) endGame("Game blocked! AI wins with fewer pips.");
            else endGame("Game blocked! It's a tie.");
        }
    }

    boolean canPlay(List<Tile> hand) {
        for (Tile t : hand) {
            if (t.matches(leftEnd) || t.matches(rightEnd)) return true;
        }
        return false;
    }

    int pipSum(List<Tile> hand) {
        int sum = 0;
        for (Tile t : hand) sum += t.pipSum();
        return sum;
    }

    void endGame(String message) {
        gameOver = true;
        setStatus(message);
        refresh();
    }

    void setStatus(String text) {
        statusLabel.setText(text);
    }

    void refresh() {
        boardPanel.removeAll();
        for (int[] pair : boardTiles) {
            boardPanel.add(new DominoTileView(pair[0], pair[1], false, TILE_H, TILE_W));
        }

        playerHandPanel.removeAll();
        for (int i = 0; i < playerHand.size(); i++) {
            Tile t = playerHand.get(i);
            DominoTileView view = new DominoTileView(t.left, t.right, false, TILE_W, TILE_H);
            int index = i;
            view.setOnClick(() -> onHandTileClicked(index));
            playerHandPanel.add(view);
        }

        aiHandPanel.removeAll();
        for (int i = 0; i < aiHand.size(); i++) {
            aiHandPanel.add(new DominoTileView(0, 0, true, TILE_W, TILE_H));
        }

        boneyardLabel.setText("Boneyard: " + boneyard.size());
        drawButton.setEnabled(!gameOver && playerTurn && !boneyard.isEmpty());
        passButton.setEnabled(!gameOver && playerTurn && boneyard.isEmpty());

        boardPanel.revalidate();
        boardPanel.repaint();
        playerHandPanel.revalidate();
        playerHandPanel.repaint();
        aiHandPanel.revalidate();
        aiHandPanel.repaint();
    }
}
