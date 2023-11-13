import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TaskExecutor extends Thread {
    private final BlockingQueue<Runnable> taskQueue;

    public TaskExecutor() {
        this.taskQueue = new LinkedBlockingQueue<>();
    }

    public void run() {
        while (true) {
            try {
                Runnable task = taskQueue.take(); // Wait for tasks to be added
                task.run(); // Execute the task
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Preserve interrupted status
                break; // Exit the thread on interruption
            }
        }
    }

    public void submitTask(Runnable task) {
        taskQueue.add(task);
    }

    public void shutdown() {
        this.interrupt(); // Signal the thread to exit
    }
}
