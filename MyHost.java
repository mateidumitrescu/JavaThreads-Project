
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class MyHost extends Host {

    private final BlockingQueue<Task> taskBlockingQueue;
    private Task currentRunningTask;

    // while (true) in run
    private boolean runningHost;

    private final Object lockTaskRunning = new Object();

    private boolean taskIsRunning;

    private long startTimeCurrentTask;



    public MyHost() {
        runningHost = true;
        taskIsRunning = false;
        // storing tasks based on priority of tasks
        this.taskBlockingQueue = new PriorityBlockingQueue<>(1, (o1, o2) -> {
            if (o1.getPriority() != o2.getPriority()) {
                return Integer.compare(o2.getPriority(), o1.getPriority());
            } else {
                return Integer.compare(o1.getStart(), o2.getStart());
            }
        });
    }
    @Override
    public void run() {
        while (runningHost) {
            try {
                // trying to get head of queue until queue is not empty
                currentRunningTask = taskBlockingQueue.take();
            } catch (InterruptedException e) {
                break;
            }

            synchronized (lockTaskRunning) {
                // running task on processor starts here
                taskIsRunning = true;
                // storing starting time of task for LWL algo
                startTimeCurrentTask = System.currentTimeMillis();
                long startTime = (long) Timer.getTimeDouble();
                try {
                    lockTaskRunning.wait(currentRunningTask.getLeft()); // waiting
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                // calulated elapsed time since task has started
                long endTime = (long) Timer.getTimeDouble();
                long elapsedTime = (endTime - startTime) * 1000;


                // it means that it was not preempted, and it finished its work
                if (elapsedTime >= currentRunningTask.getLeft()) {
                    currentRunningTask.finish();
                } else { // it got preempted
                    currentRunningTask.setLeft(currentRunningTask.getLeft() - elapsedTime);
                    taskBlockingQueue.add(currentRunningTask);
                }
                taskIsRunning = false;
            }

        }
    }


    // method to check and notify lockTaskRunning if a new task with higher prio came
    private synchronized void checkPreempt(Task task) {

        taskBlockingQueue.add(task);
        if (taskIsRunning) {
            if (currentRunningTask.isPreemptible() &&
                    task.getPriority() > currentRunningTask.getPriority()) {
                synchronized (lockTaskRunning) {
                    // letting running thread to put running task back in queue
                    // because it must be preempted
                    lockTaskRunning.notify();
                }
            }
        }


    }

    @Override
    public void addTask(Task task) {
        checkPreempt(task);
    }

    @Override
    public int getQueueSize() {
        return taskBlockingQueue.size();
    }

    public int getTaskIsRunning() {
        if (taskIsRunning) {
            return 1;
        }
        return 0;
    }

    @Override
    public long getWorkLeft() {
        long workLeft = 0;
        for (Task task : taskBlockingQueue) {
            // adding all work left of threads
            workLeft += task.getLeft();
        }
        if (taskIsRunning) { // if a task is running, adding its work left since it started
            workLeft += Math.max(currentRunningTask.getLeft() - System.currentTimeMillis() + startTimeCurrentTask, 0);
        }
        return workLeft / 100;
    }

    @Override
    public void shutdown() {
        // stopping while in run method and clearing queue
        runningHost = false;

        this.interrupt(); // take() is probably still waiting for a new task in queue
        taskBlockingQueue.clear();

        // notifying all locks to stop waiting
        synchronized (lockTaskRunning) {
            lockTaskRunning.notifyAll();
        }

        currentRunningTask = null;


    }
}