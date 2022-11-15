import chesspresso.position.Position;
import javafx.concurrent.Worker;

/**
 * A MoveMaker wraps the process that decides on a move given a position,
 * whether the move is gotten from the UI, the server, or a local AI
 */
interface MoveMaker {
    void start(Position position);

    void reset();   // set state to READY

    Worker.State getState();

    short getMove();
}
