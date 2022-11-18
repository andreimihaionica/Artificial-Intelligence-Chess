import chesspresso.position.Position;

public class PositionDataNode {
    Position position;
    int depth;

    public PositionDataNode(Position p) {
        position = p;
        depth = 0;
    }

    public void changePosition(Position p) {
        position = p;
    }

    public void incrementDepth() {
        depth = depth + 1;
    }

    public void decrementDepth() {
        depth = depth - 1;
    }

    public Position getPosition() {
        return position;
    }

    public int getDepth() {
        return depth;
    }
}
