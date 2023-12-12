/* Implement this class. */

import java.util.List;

public class MyDispatcher extends Dispatcher {

    private int lastHostIndex = 0;

    public MyDispatcher(SchedulingAlgorithm algorithm, List<Host> hosts) {
        super(algorithm, hosts);
    }

    @Override
    public void addTask(Task task) {
        switch (algorithm) {
            case ROUND_ROBIN -> roundRobin(task);
            case SHORTEST_QUEUE -> shortestQueue(task);
            case SIZE_INTERVAL_TASK_ASSIGNMENT -> sita(task);
            case LEAST_WORK_LEFT -> lwl(task);
        }
    }

    // handling round robin algorithm
    private void roundRobin(Task task) {
        hosts.get(lastHostIndex).addTask(task);
        // changing value of the next index of host to add task (i + 1) % n
        lastHostIndex = (lastHostIndex + 1) % hosts.size();
    }

    // handling shortest queue algorithm

    private void shortestQueue(Task task) {
        int indexToAssign = 0;
        int minQueueSize = Integer.MAX_VALUE;
        for (int i = 0; i < hosts.size(); i++) {
            MyHost currHost = (MyHost) hosts.get(i);
            int queueSize = hosts.get(i).getQueueSize() + (currHost.getTaskIsRunning());
            if (queueSize < minQueueSize) {
                indexToAssign = i;
                minQueueSize = queueSize;
            }
        }
        hosts.get(indexToAssign).addTask(task);
    }


    // handling size interval task assignment
    private void sita(Task task) {
        if (task.getType().equals(TaskType.SHORT)) {
            hosts.get(0).addTask(task);
        } else if (task.getType().equals(TaskType.MEDIUM)) {
            hosts.get(1).addTask(task);
        } else {
            hosts.get(2).addTask(task);
        }
    }

    // handling least work left algorithm
    private void lwl(Task task) {
        int indexToAssign = 0;
        long minWork = Long.MAX_VALUE;
        for (int i = 0; i < hosts.size(); i++) {
            MyHost hostToCheck = (MyHost) hosts.get(i);
            // getting work left on host
            long workLeft = hostToCheck.getWorkLeft();
            if (workLeft < minWork) {
                // changing index
                indexToAssign = i;
                minWork = workLeft;
            }
        }
        hosts.get(indexToAssign).addTask(task);
    }
}
