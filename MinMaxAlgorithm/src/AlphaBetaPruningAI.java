import chesspresso.Chess;
import chesspresso.move.IllegalMoveException;
import chesspresso.position.Position;

import java.util.concurrent.ConcurrentHashMap;


/**
 * AlphaBetaPruningAI class expands on min-max algorithm to find best move for CPU
 * included is alpha-beta pruning which can reduce run-time around 1/2
 */
public class AlphaBetaPruningAI implements ChessAI {
    int iteratedDepthLimit;
    int nodesExplored;
    ConcurrentHashMap<Position, Integer> transpositionTable;

    /**
     * constructor that sets a final depth limit for the search problem
     * more depth means better decision but takes longer time
     */
    public AlphaBetaPruningAI(int d) {
        iteratedDepthLimit = d;
    }
    /**
     * getMove method that uses iterative deepening and our depth-limited get move function
     * if time ever runs out, best move found so far is returned
     */
    public short getMove(Position position) {
        nodesExplored = 0;
        transpositionTable = new ConcurrentHashMap<>();

        short bestMove = 0;
        boolean timeOut = false;

        for (int i = 1; i <= iteratedDepthLimit; i++) {
            bestMove = getDepthLimitedMove(position, i);

            if (timeOut) {
                return bestMove;
            }
        }

        return bestMove;
    }

    /**
     * depth-limited get move function
     * takes in a depth and uses Min-Max algorithm to return best move
     * modified for alpha-beta pruning
     */
    public short getDepthLimitedMove(Position position, int depthLimit) {
        PositionDataNode positionNode = new PositionDataNode(position);
        short[] moves = positionNode.getPosition().getAllMoves();
        int startMaxValue = -2147483647;
        int tempMinValue;
        short bestMove = 0;
        int alpha = -2147483647;
        int beta = 2147483647;
        for (short move : moves) {
            nodesExplored = nodesExplored + 1;
            try {
                positionNode.getPosition().doMove(move);
                positionNode.incrementDepth();
            } catch (IllegalMoveException exception) {
                System.out.println("IllegalMoveException");
            }
            tempMinValue = getMinValue(positionNode, depthLimit, alpha, beta);
            if (startMaxValue < tempMinValue) {
                startMaxValue = tempMinValue;
                bestMove = move;
            }
            positionNode.getPosition().undoMove();
            positionNode.decrementDepth();
        }
        System.out.println(nodesExplored);
        return bestMove;
    }
    /**
     * recursively calls getMinValue method
     * one of two components for the min-max algorithm
     * modified for alpha-beta pruning
     */
    public int getMaxValue(PositionDataNode positionNode, int depthLimit, int alpha, int beta) {
        if (cutoffTest(positionNode, depthLimit)) {
            return utilityCalculator(positionNode.getPosition());
        }
        int score;
        short[] moves = positionNode.getPosition().getAllMoves();
        for (short move : moves) {
            nodesExplored = nodesExplored + 1;
            try {
                positionNode.getPosition().doMove(move);
                positionNode.incrementDepth();
            } catch (IllegalMoveException exception) {
                System.out.println("IllegalMoveException");
            }

            if (! transpositionTable.contains(positionNode.getPosition())) {
                score = getMinValue(positionNode, depthLimit, alpha, beta);
                transpositionTable.put(positionNode.getPosition(), score);
            }
            else {
                score = transpositionTable.get(positionNode.getPosition());
            }

            if (score >= beta) {
                positionNode.getPosition().undoMove();
                positionNode.decrementDepth();
                return beta;
            }
            if (score > alpha) {
                alpha = score;
            }
            positionNode.getPosition().undoMove();
            positionNode.decrementDepth();
        }
        return alpha;
    }
    /**
     * recursively calls getMaxValue method; one of two components for the min-max algorithm
     * modified for alpha-beta pruning
     */
    public int getMinValue(PositionDataNode positionNode, int depthLimit, int alpha, int beta) {
        if (cutoffTest(positionNode, depthLimit)) {
            return utilityCalculator(positionNode.getPosition());
        }
        int score;
        short[] moves = positionNode.getPosition().getAllMoves();
        for (short move : moves) {
            nodesExplored = nodesExplored + 1;
            try {
                positionNode.getPosition().doMove(move);
                positionNode.incrementDepth();
            } catch (IllegalMoveException exception) {
                System.out.println("IllegalMoveException");
            }
            score = getMaxValue(positionNode, depthLimit, alpha, beta);
            if (score <= alpha) {
                positionNode.getPosition().undoMove();
                positionNode.decrementDepth();
                return alpha;
            }
            if (score < beta) {
                beta = score;
            }
            positionNode.getPosition().undoMove();
            positionNode.decrementDepth();
        }
        return beta;
    }
    /**
     * cut-off test to check if search/evaluation should be stopped at node
     * @return returns true if either (1) maximum depth is reached or (2) game is over
     */
    public boolean cutoffTest(PositionDataNode positionNode, int depthLimit) {
        return (positionNode.getDepth() >= depthLimit || positionNode.getPosition().isTerminal());
    }
    /**
     * if terminal state, returns high MAX, MIN or 0
     * if not terminal state, return an evaluated value
     * @return returns utility of state
     */
    public int utilityCalculator(Position position) {
        if (position.getToPlay() == Chess.WHITE) {
            if (position.isMate()) {
                return -2147483647;
            }
            if (position.isStaleMate()) {
                return 0;
            } else {
                return evaluatePosition(position);
            }
        }
        if (position.getToPlay() == Chess.BLACK) {
            if (position.isMate()) {
                return 2147483647;
            }
            if (position.isStaleMate()) {
                return 0;
            } else {
                return -evaluatePosition(position);
            }
        }
        return 0;
    }
    /**
     * evaluates a utility for non-terminal nodes based on material value
     * also takes into consideration check positions
     */
    public int evaluatePosition(Position position) {
        int materialValue = position.getMaterial();

        if (position.isCheck()) {
            materialValue = materialValue - 500;
        }

        return materialValue;
    }
}