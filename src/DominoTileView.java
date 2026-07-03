import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;

public class DominoTileView extends JComponent {
    private static final int[][][] PIP_LAYOUT = {
        {},
        {{1, 1}},
        {{0, 0}, {2, 2}},
        {{0, 0}, {1, 1}, {2, 2}},
        {{0, 0}, {2, 0}, {0, 2}, {2, 2}},
        {{0, 0}, {2, 0}, {1, 1}, {0, 2}, {2, 2}},
        {{0, 0}, {2, 0}, {0, 1}, {2, 1}, {0, 2}, {2, 2}},
    };

    private final int left;
    private final int right;
    private final boolean faceDown;
    private boolean selected = false;

    public DominoTileView(int left, int right, boolean faceDown, int width, int height) {
        this.left = left;
        this.right = right;
        this.faceDown = faceDown;
        setPreferredSize(new Dimension(width, height));
        setOpaque(false);
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        repaint();
    }

    public void setOnClick(Runnable action) {
        if (action == null) return;
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                action.run();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        boolean horizontal = w >= h;

        RoundRectangle2D body = new RoundRectangle2D.Double(1, 1, w - 2, h - 2, 12, 12);

        if (faceDown) {
            g2.setColor(new Color(40, 60, 90));
            g2.fill(body);
            g2.setColor(new Color(80, 110, 150));
            g2.setStroke(new BasicStroke(2));
            g2.draw(body);
            g2.setColor(new Color(70, 95, 130));
            for (int i = -h; i < w; i += 8) {
                g2.drawLine(i, h, i + h, 0);
            }
        } else {
            g2.setColor(new Color(250, 246, 235));
            g2.fill(body);
            g2.setColor(selected ? new Color(255, 200, 60) : new Color(30, 30, 30));
            g2.setStroke(new BasicStroke(selected ? 4 : 2));
            g2.draw(body);

            g2.setColor(new Color(30, 30, 30));
            if (horizontal) {
                g2.drawLine(w / 2, 6, w / 2, h - 6);
                drawPips(g2, left, 0, 0, w / 2, h);
                drawPips(g2, right, w / 2, 0, w / 2, h);
            } else {
                g2.drawLine(6, h / 2, w - 6, h / 2);
                drawPips(g2, left, 0, 0, w, h / 2);
                drawPips(g2, right, 0, h / 2, w, h / 2);
            }
        }

        g2.dispose();
    }

    private void drawPips(Graphics2D g2, int number, int x, int y, int w, int h) {
        int pad = Math.min(w, h) / 5;
        int cellW = (w - 2 * pad) / 2;
        int cellH = (h - 2 * pad) / 2;
        int dot = Math.min(w, h) / 6;

        for (int[] pos : PIP_LAYOUT[number]) {
            int cx = x + pad + pos[0] * cellW;
            int cy = y + pad + pos[1] * cellH;
            g2.fillOval(cx - dot / 2, cy - dot / 2, dot, dot);
        }
    }
}
