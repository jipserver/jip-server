package jip.server;

/**
 * Possible Job finish states
 */
public enum FinishState {
    /**
     * Job successfully finished
     */
    Success,
    /**
     * Job finished with an error
     */
    Error,
    /**
     * Job was canceled
     */
    Cancel,
    /**
     * Finish state is unknown
     */
    Unknown

}
