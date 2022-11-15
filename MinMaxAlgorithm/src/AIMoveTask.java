import chesspresso.position.Position;
import javafx.concurrent.Task;

public class AIMoveTask extends Task<Short> {
    private Position position = null;
    private final ChessAI ai;

    public AIMoveTask(ChessAI ai, Position p) {
        super();
        position = p;
        this.ai = ai;
    }

    @Override
    protected Short call() throws Exception {
        return ai.getMove(position);
    }
}
