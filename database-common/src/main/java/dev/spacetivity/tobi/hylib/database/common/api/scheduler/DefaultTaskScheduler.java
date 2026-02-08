package dev.spacetivity.tobi.hylib.database.common.api.scheduler;

import dev.spacetivity.tobi.hylib.database.api.scheduler.ScheduledTask;
import dev.spacetivity.tobi.hylib.database.api.scheduler.TaskScheduler;

import java.time.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Default implementation of {@link TaskScheduler}.
 * 
 * <p>Uses a {@link ScheduledThreadPoolExecutor} for scheduling tasks and delegates
 * actual task execution to a provided worker {@link ExecutorService}. This allows
 * the scheduler threads to remain available for scheduling while tasks execute
 * asynchronously on worker threads (typically virtual threads).</p>
 * 
 * @since 1.0
 */
public class DefaultTaskScheduler implements TaskScheduler {

    private final AtomicInteger taskIdSeq = new AtomicInteger();

    private final ConcurrentMap<Integer, ScheduledFuture<?>> tasks = new  ConcurrentHashMap<>();

    private final ScheduledThreadPoolExecutor scheduler;
    private final ExecutorService worker;

    public DefaultTaskScheduler(int schedulerThreads, ExecutorService worker) {
        this.scheduler = new ScheduledThreadPoolExecutor(
                schedulerThreads,
                Thread.ofPlatform().name("scheduler-", 0).factory()
        );
        this.scheduler.setRemoveOnCancelPolicy(true);
        this.worker = worker;
    }

    @Override
    public ScheduledTask schedule(Runnable task, Duration delay) {
        int id = taskIdSeq.incrementAndGet();

        ScheduledFuture<?> future = this.scheduler.schedule(
          () -> worker.submit(task),
          delay.toMillis(),
          TimeUnit.MILLISECONDS
        );

        this.tasks.put(id, future);
        return wrap(id, future);
    }

    @Override
    public ScheduledTask scheduleAtTime(Runnable task, Instant time) {
        Instant now = Instant.now();
        Duration delay = Duration.between(now, time);
        
        // If the time is in the past, execute immediately (zero delay)
        if (delay.isNegative() || delay.isZero()) {
            return schedule(task, Duration.ZERO);
        }
        
        return schedule(task, delay);
    }

    @Override
    public ScheduledTask scheduleAtFixedRate(Runnable task, Duration initialDelay, Duration period) {
        int id = this.taskIdSeq.incrementAndGet();

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
                () -> this.worker.submit(task),
                initialDelay.toMillis(),
                period.toMillis(),
                TimeUnit.MILLISECONDS
        );

        this.tasks.put(id, future);
        return wrap(id, future);
    }

    @Override
    public ScheduledTask scheduleWithFixedDelay(Runnable task, Duration initialDelay, Duration delay) {
        int id = this.taskIdSeq.incrementAndGet();

        ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(
                () -> this.worker.submit(task),
                initialDelay.toMillis(),
                delay.toMillis(),
                TimeUnit.MILLISECONDS
        );

        this.tasks.put(id, future);
        return wrap(id, future);
    }

    @Override
    public ScheduledTask runTaskLater(Runnable task, Duration delay) {
        int id = taskIdSeq.incrementAndGet();

        ScheduledFuture<?> future = this.scheduler.schedule(
                () -> {
                    worker.submit(task);
                    this.tasks.remove(id);
                },
                delay.toMillis(),
                TimeUnit.MILLISECONDS
        );

        this.tasks.put(id, future);
        return wrap(id, future);
    }

    @Override
    public boolean cancelTask(int id) {
        ScheduledFuture<?> future = this.tasks.remove(id);
        return future != null && future.cancel(false);
    }

    @Override
    public ScheduledTask getTask(int id) {
        ScheduledFuture<?> future = this.tasks.get(id);
        return future != null ? wrap(id, future) : null;
    }

    @Override
    public Set<ScheduledTask> getAllTasks() {
        Set<ScheduledTask> result = new HashSet<>(this.tasks.size());
        this.tasks.forEach((id, future) -> result.add(wrap(id, future)));
        return result;
    }

    @Override
    public int getTaskCount() {
        return this.tasks.size();
    }

    @Override
    public Duration getRemainingDelay(int id) {
        ScheduledFuture<?> future = this.tasks.get(id);
        if (future == null) {
            return null;
        }
        long delayMillis = future.getDelay(TimeUnit.MILLISECONDS);
        return delayMillis >= 0 ? Duration.ofMillis(delayMillis) : null;
    }

    @Override
    public void cancelAll() {
        this.tasks.values().forEach(future -> future.cancel(false));
        this.tasks.clear();
    }

    @Override
    public ScheduledTask scheduleAtTimeOfDay(LocalTime time, Period period, Runnable task) {
        int id = taskIdSeq.incrementAndGet();
        
        // Calculate next execution time
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime todayAtTime = now.toLocalDate().atTime(time).atZone(now.getZone());
        
        // If the time has already passed today, schedule for next occurrence
        final ZonedDateTime nextExecution = (todayAtTime.isBefore(now) || todayAtTime.isEqual(now))
            ? todayAtTime.plus(period)
            : todayAtTime;
        
        Duration initialDelay = Duration.between(now, nextExecution);
        
        // Create a self-rescheduling task
        Runnable reschedulingTask = new Runnable() {
            private ZonedDateTime nextRun = nextExecution;
            
            @Override
            public void run() {
                // Execute the actual task
                worker.submit(task);
                
                // Schedule next execution
                nextRun = nextRun.plus(period);
                ZonedDateTime currentTime = ZonedDateTime.now();
                
                // Ensure we don't schedule in the past (shouldn't happen, but safety check)
                if (nextRun.isBefore(currentTime)) {
                    nextRun = currentTime.toLocalDate().atTime(time).atZone(currentTime.getZone());
                    if (nextRun.isBefore(currentTime) || nextRun.isEqual(currentTime)) {
                        nextRun = nextRun.plus(period);
                    }
                }
                
                Duration delayUntilNext = Duration.between(currentTime, nextRun);
                
                ScheduledFuture<?> future = scheduler.schedule(
                    this,
                    delayUntilNext.toMillis(),
                    TimeUnit.MILLISECONDS
                );
                
                tasks.put(id, future);
            }
        };
        
        ScheduledFuture<?> future = scheduler.schedule(
            reschedulingTask,
            initialDelay.toMillis(),
            TimeUnit.MILLISECONDS
        );
        
        this.tasks.put(id, future);
        return wrap(id, future);
    }

    private ScheduledTask wrap(int id, ScheduledFuture<?> future) {
        return new ScheduledTask() {
            @Override
            public int getId() {
                return id;
            }

            @Override
            public boolean cancel() {
                return cancelTask(id);
            }

            @Override
            public boolean isCancelled() {
                return future.isCancelled();
            }
        };
    }
}
