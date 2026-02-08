package dev.spacetivity.tobi.hylib.database.api.scheduler;

/**
 * Represents a scheduled task that can be cancelled or queried for its status.
 * 
 * <p>A ScheduledTask is returned when scheduling a task via {@link TaskScheduler}.
 * It provides access to the task's ID and allows cancellation or status checks.</p>
 * 
 * @since 1.0
 */
public interface ScheduledTask {

    /**
     * Returns the unique identifier of this scheduled task.
     * 
     * @return the task ID
     */
    int getId();

    /**
     * Cancels this scheduled task.
     * 
     * <p>If the task has already been executed or cancelled, this method returns false.
     * Cancelled tasks are removed from the scheduler.</p>
     * 
     * @return true if the task was successfully cancelled, false if it was already executed or cancelled
     */
    boolean cancel();

    /**
     * Checks whether this task has been cancelled.
     * 
     * <p>Note: This method only indicates if the task was cancelled. It does not indicate
     * whether the task has been executed. For tasks scheduled via {@link TaskScheduler#runTaskLater(Runnable, Duration)},
     * the task may have been executed and removed, which is different from being cancelled.</p>
     * 
     * @return true if the task has been cancelled, false otherwise
     */
    boolean isCancelled();

}
