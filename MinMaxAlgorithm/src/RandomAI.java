import chesspresso.position.Position;

import java.util.Random;

public class RandomAI implements ChessAI {
    public short getMove(Position position) {
        short[] moves = position.getAllMoves();
        return moves[new Random().nextInt(moves.length)];
    }
}
