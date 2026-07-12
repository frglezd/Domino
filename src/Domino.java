import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.*;

public class Domino {
    static final int TILE_W = 70;
    static final int TILE_H = 130;
    static final int SIDE = -1, LEFT = 0, RIGHT = 1;
    static final int BOARD_MARGIN_X = 20;
    static final int BOARD_MARGIN_Y = 16;
    static final int TILE_GAP = 4;
    static final int DEFAULT_BOARD_WIDTH = 900;
    static final int MIN_FRAME_HEIGHT = 750;
    static final int DEFAULT_CHROME_HEIGHT = 400;
    static final int SCREEN_HEIGHT_MARGIN = 20;

    private record TilePlacement(int x, int y, int width, int height, int left, int right) {}

    enum Mode { VS_AI, TWO_PLAYER }

    JFrame frame = new JFrame("Domino");
    JLabel statusLabel = new JLabel();
    JLabel boneyardLabel = new JLabel();
    JPanel boardPanel = new JPanel();
    JScrollPane boardScroll;
    JPanel player1HandPanel = new JPanel();
    JPanel player2HandPanel = new JPanel();
    JButton drawButton = new JButton("Draw");
    JButton passButton = new JButton("Pass");
    JButton newGameButton = new JButton("New Game");
    JButton revealButton = new JButton("Ready");
    JRadioButton vsAiRadio = new JRadioButton("Vs AI", true);
    JRadioButton twoPlayerRadio = new JRadioButton("2 Players");

    List<Tile> boneyard = new ArrayList<>();
    List<Tile> player1Hand = new ArrayList<>();
    List<Tile> player2Hand = new ArrayList<>();
    List<int[]> boardTiles = new ArrayList<>();

    Mode mode = Mode.VS_AI;
    int leftEnd, rightEnd;
    boolean boardEmpty = true;
    boolean player1Turn = true;
    boolean awaitingReveal = false;
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
        boardPanel.setLayout(null);
        boardScroll = new JScrollPane(boardPanel);
        boardScroll.setBorder(BorderFactory.createEmptyBorder());
        boardScroll.getViewport().setBackground(new Color(20, 90, 60));
        boardScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        boardScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        boardScroll.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                relayoutBoard();
            }
        });
        frame.add(boardScroll, BorderLayout.CENTER);
    }

    void buildHandPanels() {
        player2HandPanel.setBackground(new Color(15, 70, 47));
        player2HandPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 6, 6));
        player2HandPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        player1HandPanel.setBackground(new Color(15, 70, 47));
        player1HandPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 6, 6));
        player1HandPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    }

    void buildControlPanel() {
        JPanel south = new JPanel();
        south.setLayout(new BorderLayout());
        south.setBackground(new Color(15, 70, 47));

        JPanel controls = new JPanel();
        controls.setBackground(new Color(15, 70, 47));
        boneyardLabel.setForeground(Color.white);
        boneyardLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JLabel modeLabel = new JLabel("Mode:");
        modeLabel.setForeground(Color.white);
        modeLabel.setFont(new Font("Arial", Font.BOLD, 16));

        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(vsAiRadio);
        modeGroup.add(twoPlayerRadio);
        for (JRadioButton radio : new JRadioButton[]{vsAiRadio, twoPlayerRadio}) {
            radio.setForeground(Color.white);
            radio.setFont(new Font("Arial", Font.BOLD, 16));
            radio.setOpaque(false);
            radio.setFocusable(false);
        }

        styleButton(drawButton);
        styleButton(passButton);
        styleButton(newGameButton);
        styleButton(revealButton);
        revealButton.setVisible(false);

        drawButton.addActionListener(e -> onDraw());
        passButton.addActionListener(e -> onPass());
        newGameButton.addActionListener(e -> newGame());
        revealButton.addActionListener(e -> onReveal());

        controls.add(modeLabel);
        controls.add(vsAiRadio);
        controls.add(twoPlayerRadio);
        controls.add(boneyardLabel);
        controls.add(drawButton);
        controls.add(passButton);
        controls.add(revealButton);
        controls.add(newGameButton);

        south.add(player2HandPanel, BorderLayout.NORTH);
        south.add(controls, BorderLayout.CENTER);
        south.add(player1HandPanel, BorderLayout.SOUTH);
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
        mode = twoPlayerRadio.isSelected() ? Mode.TWO_PLAYER : Mode.VS_AI;

        boneyard.clear();
        player1Hand.clear();
        player2Hand.clear();
        boardTiles.clear();
        boardEmpty = true;
        awaitingReveal = false;
        gameOver = false;

        for (int i = 0; i <= 6; i++) {
            for (int j = i; j <= 6; j++) {
                boneyard.add(new Tile(i, j));
            }
        }
        Collections.shuffle(boneyard);

        for (int i = 0; i < 7; i++) {
            player1Hand.add(boneyard.remove(boneyard.size() - 1));
            player2Hand.add(boneyard.remove(boneyard.size() - 1));
        }

        player1Turn = determineStarter();

        if (mode == Mode.VS_AI) {
            setStatus(player1Turn ? "You start! Click a tile to play." : "CPU starts...");
            refresh();
            if (!player1Turn) {
                Timer timer = new Timer(700, e -> aiTurn());
                timer.setRepeats(false);
                timer.start();
            }
        } else {
            awaitingReveal = true;
            setStatus("Pass the device to " + (player1Turn ? "Player 1" : "Player 2")
                    + " to start. Tap Ready when they're set.");
            refresh();
        }
    }

    boolean determineStarter() {
        int player1Best = highestDouble(player1Hand);
        int player2Best = highestDouble(player2Hand);
        if (player1Best == -1 && player2Best == -1) return true;
        return player1Best >= player2Best;
    }

    int highestDouble(List<Tile> hand) {
        int best = -1;
        for (Tile t : hand) {
            if (t.isDouble() && t.left > best) best = t.left;
        }
        return best;
    }

    List<Tile> activeHand() {
        return player1Turn ? player1Hand : player2Hand;
    }

    String subjectWins(boolean isPlayer1) {
        if (mode == Mode.VS_AI) return isPlayer1 ? "You win" : "CPU wins";
        return isPlayer1 ? "Player 1 wins" : "Player 2 wins";
    }

    void onHandTileClicked(int index) {
        if (gameOver || awaitingReveal) return;
        if (mode == Mode.VS_AI && !player1Turn) return;

        List<Tile> hand = activeHand();
        Tile tile = hand.get(index);

        if (boardEmpty) {
            placeTile(tile, SIDE);
            hand.remove(index);
            afterHumanMove();
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
        hand.remove(index);
        afterHumanMove();
    }

    void afterHumanMove() {
        if (activeHand().isEmpty()) {
            String tail = mode == Mode.VS_AI
                    ? (player1Turn ? "Your hand is empty." : "Its hand is empty.")
                    : "Their hand is empty.";
            endGame(subjectWins(player1Turn) + "! " + tail);
            return;
        }

        boolean nextIsPlayer1 = !player1Turn;
        setStatus(mode == Mode.VS_AI
                ? (nextIsPlayer1 ? "Your turn — click a tile to play, or Draw." : "CPU is thinking...")
                : "Pass the device to " + (nextIsPlayer1 ? "Player 1" : "Player 2") + ". Tap Ready when they're set.");
        advanceTurn();
    }

    void advanceTurn() {
        player1Turn = !player1Turn;
        if (mode == Mode.TWO_PLAYER) {
            awaitingReveal = true;
        }
        refresh();
        if (mode == Mode.VS_AI && !player1Turn) {
            Timer timer = new Timer(700, e -> aiTurn());
            timer.setRepeats(false);
            timer.start();
        }
    }

    void onReveal() {
        if (mode != Mode.TWO_PLAYER || gameOver || !awaitingReveal) return;
        awaitingReveal = false;
        setStatus((player1Turn ? "Player 1" : "Player 2") + "'s turn — click a tile to play, or Draw.");
        refresh();
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
        if (gameOver || awaitingReveal || boneyard.isEmpty()) return;
        if (mode == Mode.VS_AI && !player1Turn) return;

        activeHand().add(boneyard.remove(boneyard.size() - 1));
        setStatus(mode == Mode.VS_AI ? "You drew a tile."
                : (player1Turn ? "Player 1 drew a tile." : "Player 2 drew a tile."));
        refresh();
    }

    void onPass() {
        if (gameOver || awaitingReveal) return;
        if (mode == Mode.VS_AI && !player1Turn) return;

        checkBlocked();
        if (gameOver) return;

        boolean nextIsPlayer1 = !player1Turn;
        String who = mode == Mode.VS_AI ? "You" : (player1Turn ? "Player 1" : "Player 2");
        String next = mode == Mode.VS_AI
                ? (nextIsPlayer1 ? "Your turn — click a tile to play, or Draw." : "CPU is thinking...")
                : "Pass the device to " + (nextIsPlayer1 ? "Player 1" : "Player 2") + ".";
        setStatus(who + " passed. " + next);
        advanceTurn();
    }

    void aiTurn() {
        if (gameOver || mode != Mode.VS_AI) return;

        Tile toPlay = null;
        int side = SIDE;
        for (Tile t : player2Hand) {
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
            player2Hand.add(boneyard.remove(boneyard.size() - 1));
            refresh();
            Timer timer = new Timer(500, e -> aiTurn());
            timer.setRepeats(false);
            timer.start();
            return;
        }

        if (toPlay == null) {
            setStatus("CPU can't move and passes.");
            advanceTurn();
            checkBlocked();
            return;
        }

        placeTile(toPlay, side);
        player2Hand.remove(toPlay);

        if (player2Hand.isEmpty()) {
            endGame("CPU wins! Its hand is empty.");
            return;
        }

        setStatus("Your turn — click a tile to play, or Draw.");
        advanceTurn();
    }

    void checkBlocked() {
        boolean player1CanMove = boardEmpty || canPlay(player1Hand);
        boolean player2CanMove = boardEmpty || canPlay(player2Hand);
        if (boneyard.isEmpty() && !player1CanMove && !player2CanMove) {
            int player1Pips = pipSum(player1Hand);
            int player2Pips = pipSum(player2Hand);
            if (player1Pips < player2Pips) endGame("Game blocked! " + subjectWins(true) + " with fewer pips.");
            else if (player2Pips < player1Pips) endGame("Game blocked! " + subjectWins(false) + " with fewer pips.");
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
        awaitingReveal = false;
        setStatus(message);
        refresh();
    }

    void setStatus(String text) {
        statusLabel.setText(text);
    }

    void renderHandPanel(JPanel panel, List<Tile> hand, boolean visible) {
        panel.removeAll();
        for (int i = 0; i < hand.size(); i++) {
            if (visible) {
                Tile t = hand.get(i);
                DominoTileView view = new DominoTileView(t.left, t.right, false, TILE_W, TILE_H);
                int index = i;
                view.setOnClick(() -> onHandTileClicked(index));
                panel.add(view);
            } else {
                panel.add(new DominoTileView(0, 0, true, TILE_W, TILE_H));
            }
        }
        panel.revalidate();
        panel.repaint();
    }

    List<TilePlacement> computeBoardLayout() {
        List<TilePlacement> placements = new ArrayList<>();
        int n = boardTiles.size();
        if (n == 0) return placements;

        int viewportW = boardScroll.getViewport().getWidth();
        if (viewportW <= 0) viewportW = DEFAULT_BOARD_WIDTH;

        int usableW = Math.max(viewportW - 2 * BOARD_MARGIN_X, TILE_H);
        int slotsPerRow = Math.max(1, (usableW + TILE_GAP) / (TILE_H + TILE_GAP));

        int dir = 1;
        int penX = BOARD_MARGIN_X;
        int penY = BOARD_MARGIN_Y;
        int inRow = 0;
        int rowCapacity = 0;
        int i = 0;

        while (i < n) {
            if (inRow == 0) {
                int remaining = n - i;
                rowCapacity = remaining <= slotsPerRow ? remaining : slotsPerRow - 1;
            }

            int[] pair = boardTiles.get(i);
            int x = dir == 1 ? penX : penX - TILE_H;
            int shownLeft = dir == 1 ? pair[0] : pair[1];
            int shownRight = dir == 1 ? pair[1] : pair[0];
            placements.add(new TilePlacement(x, penY, TILE_H, TILE_W, shownLeft, shownRight));
            penX += dir * (TILE_H + TILE_GAP);
            i++;
            inRow++;

            if (inRow >= rowCapacity && i < n) {
                int[] elbow = boardTiles.get(i);
                int ex = dir == 1 ? penX : penX - TILE_W;
                placements.add(new TilePlacement(ex, penY, TILE_W, TILE_H, elbow[0], elbow[1]));
                i++;
                penX += dir * TILE_W;
                penY += TILE_H;
                dir = -dir;
                inRow = 0;
            }
        }

        return placements;
    }

    void relayoutBoard() {
        List<TilePlacement> placements = computeBoardLayout();
        int maxX = 0, maxY = 0;
        for (TilePlacement p : placements) {
            maxX = Math.max(maxX, p.x() + p.width());
            maxY = Math.max(maxY, p.y() + p.height());
        }
        adjustFrameHeight(maxY + BOARD_MARGIN_Y);

        boardPanel.removeAll();
        for (TilePlacement p : placements) {
            DominoTileView view = new DominoTileView(p.left(), p.right(), false, p.width(), p.height());
            view.setBounds(p.x(), p.y(), p.width(), p.height());
            boardPanel.add(view);
        }
        boardPanel.setPreferredSize(new Dimension(maxX + BOARD_MARGIN_X, maxY + BOARD_MARGIN_Y));
        boardPanel.revalidate();
        boardPanel.repaint();
    }

    void adjustFrameHeight(int requiredBoardHeight) {
        int chromeHeight = frame.getHeight() - boardScroll.getHeight();
        if (chromeHeight <= 0) chromeHeight = DEFAULT_CHROME_HEIGHT;

        int maxFrameHeight = Math.max(MIN_FRAME_HEIGHT, GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getMaximumWindowBounds().height - SCREEN_HEIGHT_MARGIN);

        int desired = Math.max(MIN_FRAME_HEIGHT, Math.min(maxFrameHeight, chromeHeight + requiredBoardHeight));

        if (Math.abs(desired - frame.getHeight()) > 1) {
            frame.setSize(frame.getWidth(), desired);
            frame.setLocationRelativeTo(null);
        }
    }

    void refresh() {
        relayoutBoard();

        boolean player1Visible = mode == Mode.VS_AI || (!awaitingReveal && player1Turn);
        boolean player2Visible = mode == Mode.TWO_PLAYER && !awaitingReveal && !player1Turn;

        renderHandPanel(player1HandPanel, player1Hand, player1Visible);
        renderHandPanel(player2HandPanel, player2Hand, player2Visible);

        boneyardLabel.setText("Boneyard: " + boneyard.size());

        boolean humanActionsEnabled = !gameOver && !awaitingReveal && (mode == Mode.TWO_PLAYER || player1Turn);
        drawButton.setEnabled(humanActionsEnabled && !boneyard.isEmpty());
        passButton.setEnabled(humanActionsEnabled && boneyard.isEmpty());
        revealButton.setVisible(mode == Mode.TWO_PLAYER && awaitingReveal);
    }
}
