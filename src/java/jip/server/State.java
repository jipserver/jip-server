package jip.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Possible job states
 */
public enum State {
    /**
     * Initial Job state of a job that is submitted to
     * the JIP server
     */
    Queued(),

    /**
     * Job is on hold and will not be submitted
     * to the remote server
     */
    Hold(),

    /**
     * Job is submitted to the remote server
     */
    Submitted(),

    /**
     * Job is running
     */
    Running(),

    /**
     * Job is completed successfully
     */
    Done();

    /**
     * Get list of allowed next states
     *
     * @return nextStates possible next states
     */
    List<State> getNextStates() {
        List<State> nextStates = null;
        switch (this){
            case Done: nextStates = new ArrayList<State>(); break;
            case Running: nextStates = Arrays.asList(Hold, Done); break;
            case Submitted: nextStates = Arrays.asList(Running, Done); break;
            case Hold: nextStates = Arrays.asList(Queued, Submitted, Done); break;
            case Queued: nextStates = Arrays.asList(Submitted, Hold, Done); break;
        }
        return nextStates;
    }
}
