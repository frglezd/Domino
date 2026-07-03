public class Tile {
    public final int left;
    public final int right;

    public Tile(int left, int right) {
        this.left = left;
        this.right = right;
    }

    public boolean isDouble() {
        return left == right;
    }

    public boolean matches(int end) {
        return left == end || right == end;
    }

    public int other(int end) {
        return left == end ? right : left;
    }

    public int pipSum() {
        return left + right;
    }

    @Override
    public String toString() {
        return left + "|" + right;
    }
}
