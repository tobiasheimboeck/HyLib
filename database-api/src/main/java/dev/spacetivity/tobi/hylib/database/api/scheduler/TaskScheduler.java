package dev.spacetivity.tobi.hylib.database.api.scheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

/**
 * Scheduler for executing tasks with delays or at fixed rates.
 * 
 * <p>All tasks are executed asynchronously. The scheduling itself happens on dedicated
 * scheduler threads, while the actual task execution is delegated to a worker executor
 * service (typically using virtual threads).</p>
 * 
 * <p>Tasks scheduled via {@link #runTaskLater(Runnable, Duration)} are automatically
 * removed from the scheduler after execution. Tasks scheduled via {@link #schedule(Runnable, Duration)}
 * or {@link #scheduleAtFixedRate(Runnable, Duration, Duration)} remain in the scheduler
 * until explicitly cancelled.</p>
 * 
 * @since 1.0
 */
public interface TaskScheduler {

    /**
     * Schedules a task to be executed once after the specified delay.
     * 
     * <p>The task will be executed asynchronously after the delay has elapsed.
     * The task remains in the scheduler until explicitly cancelled via {@link ScheduledTask#cancel()}.</p>
     * 
     * @param task the task to execute, must not be null
     * @param delay the delay before execution, must not be null
     * @return a ScheduledTask that can be used to cancel the task
     * @throws NullPointerException if task or delay is null
     */
    ScheduledTask schedule(Runnable task, Duration delay);

    /**
     * Schedules a task to be executed at a specific point in time.
     * 
     * <p>The task will be executed asynchronously at the specified instant. The task remains
     * in the scheduler until explicitly cancelled via {@link ScheduledTask#cancel()}.</p>
     * 
     * <p>If the specified instant is in the past, the task will be executed immediately
     * (with zero delay).</p>
     * 
     * @param task the task to execute, must not be null
     * @param time the instant at which to execute the task, must not be null
     * @return a ScheduledTask that can be used to cancel the task
     * @throws NullPointerException if task or time is null
     */
    ScheduledTask scheduleAtTime(Runnable task, Instant time);

    /**
     * Schedules a task to be executed repeatedly at a fixed rate.
     * 
     * <p>The task will be executed asynchronously after the initial delay, and then
     * repeatedly at the specified period. The task remains in the scheduler until
     * explicitly cancelled via {@link ScheduledTask#cancel()}.</p>
     * 
     * <p>Note: If a task execution takes longer than the period, subsequent executions
     * may be delayed. The scheduler does not guarantee concurrent execution of the same task.</p>
     * 
     * @param task the task to execute, must not be null
     * @param initialDelay the delay before the first execution, must not be null
     * @param period the period between executions, must not be null
     * @return a ScheduledTask that can be used to cancel the task
     * @throws NullPointerException if task, initialDelay, or period is null
     */
    ScheduledTask scheduleAtFixedRate(Runnable task, Duration initialDelay, Duration period);

    /**
     * Schedules a task to be executed repeatedly with a fixed delay between the end of one
     * execution and the start of the next.
     * 
     * <p>The task will be executed asynchronously after the initial delay, and then
     * repeatedly with the specified delay between the end of one execution and the start
     * of the next. The task remains in the scheduler until explicitly cancelled via
     * {@link ScheduledTask#cancel()}.</p>
     * 
     * <p>Unlike {@link #scheduleAtFixedRate(Runnable, Duration, Duration)}, this method
     * ensures a fixed delay between executions, regardless of how long the task takes to execute.
     * If a task execution takes longer than the delay, the next execution will start immediately
     * after the previous one completes.</p>
     * 
     * @param task the task to execute, must not be null
     * @param initialDelay the delay before the first execution, must not be null
     * @param delay the delay between the end of one execution and the start of the next, must not be null
     * @return a ScheduledTask that can be used to cancel the task
     * @throws NullPointerException if task, initialDelay, or delay is null
     */
    ScheduledTask scheduleWithFixedDelay(Runnable task, Duration initialDelay, Duration delay);

    /**
     * Schedules a task to be executed once after the specified delay.
     * 
     * <p>This method is similar to {@link #schedule(Runnable, Duration)}, but the task
     * is automatically removed from the scheduler after execution. This is useful for
     * one-time delayed tasks that don't need to be manually cancelled.</p>
     * 
     * <p>The task will be executed asynchronously after the delay has elapsed.
     * After execution, the task is automatically removed and cannot be retrieved via
     * {@link #getTask(int)}.</p>
     * 
     * @param task the task to execute, must not be null
     * @param delay the delay before execution, must not be null
     * @return a ScheduledTask that can be used to cancel the task before execution
     * @throws NullPointerException if task or delay is null
     */
    ScheduledTask runTaskLater(Runnable task, Duration delay);

    /**
     * Cancels a scheduled task by its ID.
     * 
     * <p>If the task has already been executed or cancelled, this method returns false.
     * Cancelled tasks are removed from the scheduler.</p>
     * 
     * @param id the task ID
     * @return true if the task was successfully cancelled, false if it was not found or already cancelled
     */
    boolean cancelTask(int id);

    /**
     * Retrieves a scheduled task by its ID.
     * 
     * <p>Returns null if the task does not exist (e.g., it was already executed and removed,
     * or it was cancelled). Tasks scheduled via {@link #runTaskLater(Runnable, Duration)}
     * are automatically removed after execution and will return null.</p>
     * 
     * @param id the task ID
     * @return the ScheduledTask, or null if not found
     */
    ScheduledTask getTask(int id);

    /**
     * Returns all currently scheduled tasks.
     * 
     * <p>This method returns a snapshot of all tasks that are currently scheduled in
     * this scheduler. The set includes tasks scheduled via {@link #schedule(Runnable, Duration)},
     * {@link #scheduleAtFixedRate(Runnable, Duration, Duration)}, and
     * {@link #scheduleWithFixedDelay(Runnable, Duration, Duration)}, but excludes tasks
     * scheduled via {@link #runTaskLater(Runnable, Duration)} that have already been executed.</p>
     * 
     * <p>The returned set is a snapshot and will not reflect changes made after
     * this method returns. Modifying the returned set will not affect the scheduler.</p>
     * 
     * @return a set of all currently scheduled tasks, never null
     */
    Set<ScheduledTask> getAllTasks();

    /**
     * Returns the number of currently scheduled tasks.
     * 
     * <p>This is a more efficient alternative to {@link #getAllTasks()}.size() as it
     * does not create a collection of all tasks.</p>
     * 
     * @return the number of currently scheduled tasks
     */
    int getTaskCount();

    /**
     * Returns the remaining delay until the next execution of a scheduled task.
     * 
     * <p>For one-time tasks (scheduled via {@link #schedule(Runnable, Duration)} or
     * {@link #runTaskLater(Runnable, Duration)}), this returns the delay until execution.
     * For recurring tasks, this returns the delay until the next scheduled execution.</p>
     * 
     * @param id the task ID
     * @return the remaining delay, or null if the task does not exist or has already been executed
     */
    Duration getRemainingDelay(int id);

    /**
     * Cancels all scheduled tasks.
     * 
     * <p>This method cancels all tasks currently scheduled in this scheduler and clears
     * the internal task registry. After calling this method, all tasks are removed
     * and cannot be retrieved via {@link #getTask(int)}.</p>
     */
    void cancelAll();

}
