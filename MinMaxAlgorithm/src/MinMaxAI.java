import chesspresso.Chess;
import chesspresso.move.IllegalMoveException;
import chesspresso.position.Position;

/**
 * MinMaxAI class uses min-max algorithm to find best move for CPU
 */
public class MinMaxAI implements ChessAI {
    int iteratedDepthLimit;
    int nodesExplored;

    /**
     * Constructor that sets a final depth limit for the search problem
     * More depth means better decision but takes longer time
     */
    public MinMaxAI(int d) {
        iteratedDepthLimit = d;
    }

    /**
     * getMove method that uses iterative deepening and our depth-limited get move function
     * if time ever runs out, best move found so far is returned
     */
    public short getMove(Position position) {
        nodesExplored = 0;
        return getDepthLimitedMove(position, iteratedDepthLimit);
    }

    /**
     * Depth-limited get move function
     * Takes in a depth and uses Min-Max algorithm to return best move
     */
    public short getDepthLimitedMove(Position position, int depthLimit) {
        PositionDataNode positionNode = new PositionDataNode(position);

        short[] moves = positionNode.getPosition().getAllMoves();

        int startMaxValue = -2147483647;
        int tempMinValue;
        short bestMove = 0;

        for (short move : moves) {
            nodesExplored = nodesExplored + 1;
            try {
                positionNode.getPosition().doMove(move);
                positionNode.incrementDepth();
            } catch (IllegalMoveException exception) {
                System.out.println("IllegalMoveException");
            }

            tempMinValue = getMinValue(positionNode, depthLimit);

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
     * Recursively calls getMinValue method
     * One of two components for the min-max algorithm
     */
    public int getMaxValue(PositionDataNode positionNode, int depthLimit) {
        if (cutoffTest(positionNode, depthLimit)) {
            return utilityCalculator(positionNode.getPosition());
        }

        int max = -2147483647;
        short[] moves = positionNode.getPosition().getAllMoves();

        for (short move : moves) {
            nodesExplored = nodesExplored + 1;
            try {
                positionNode.getPosition().doMove(move);
                positionNode.incrementDepth();
            } catch (IllegalMoveException exception) {
                System.out.println("IllegalMoveException");
            }

            max = Math.max(max, getMinValue(positionNode, depthLimit));

            positionNode.getPosition().undoMove();
            positionNode.decrementDepth();
        }
        return max;
    }

    /**
     * Recursively calls getMaxValue method
     * One of two components for the min-max algorithm
     */
    public int getMinValue(PositionDataNode positionNode, int depthLimit) {
        if (cutoffTest(positionNode, depthLimit)) {
            return utilityCalculator(positionNode.getPosition());
        }

        int min = 2147483647;
        short[] moves = positionNode.getPosition().getAllMoves();

        for (short move : moves) {
            nodesExplored = nodesExplored + 1;
            try {
                positionNode.getPosition().doMove(move);
                positionNode.incrementDepth();
            } catch (IllegalMoveException exception) {
                System.out.println("IllegalMoveException");
            }

            min = Math.min(min, getMaxValue(positionNode, depthLimit));
            positionNode.getPosition().undoMove();
            positionNode.decrementDepth();
        }
        return min;
    }

    /**
     * Cut-off test to check if search/evaluation should be stopped at node
     * @return returns true if either (1) maximum depth is reached or (2) game is over
     */
    public boolean cutoffTest(PositionDataNode positionNode, int depthLimit) {
        return (positionNode.getDepth() >= depthLimit || positionNode.getPosition().isTerminal());
    }

    /**
     * If terminal state, returns high MAX, MIN or 0
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
     * Evaluates a utility for non-terminal nodes based on material value
     * also takes into consideration check positions
     */
    public int evaluatePosition(Position position) {
        return position.getMaterial();
    }
}
