package scheduling;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.RuntimeErrorException;

public class TiredExecutor {

    private final TiredThread[] workers;
    private final PriorityBlockingQueue<TiredThread> idleMinHeap = new PriorityBlockingQueue<>();
    private final AtomicInteger inFlight = new AtomicInteger(0);

    public TiredExecutor(int numThreads) {
        // TODO
        workers = new TiredThread[numThreads];
        for (int i = 0; i< workers.length; i++) {
            workers[i] = new TiredThread(i, Math.random() + 0.5);
            workers[i].start();
            idleMinHeap.add(workers[i]);
        }  
    }

    public void submit(Runnable task) {
        try {
            TiredThread thread = idleMinHeap.take();

            Runnable wrappedRunnable = ()-> {
                long startTime = System.nanoTime();

                try {
                    task.run();
                }

                finally {
                    synchronized(TiredExecutor.this){
                        thread.updateTimeStats(startTime);
                        inFlight.decrementAndGet();
                        idleMinHeap.put(thread);
                        this.notifyAll();
                    }
                }
            };

            inFlight.incrementAndGet();
            thread.newTask(wrappedRunnable);
        }
        catch(InterruptedException e){
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while submitting task", e);
        }   
    }

    public void submitAll(Iterable<Runnable> tasks) {
        
        for (Runnable task : tasks) {
            submit(task);
        }
        synchronized(this) {
            while (inFlight.get() > 0) {
                try {
                    this.wait();
                }

                catch(InterruptedException e){
                    System.err.println("error at executor with exception: " + e);
                    Thread.currentThread().interrupt();
                }
            } 
        }
    }

    public void shutdown() throws InterruptedException {
        for (TiredThread worker : workers)
            worker.shutdown();
        
        for(TiredThread worker : workers)
            worker.join();
    }

    public synchronized String getWorkerReport() {
        // TODO: return readable statistics for each worker
        StringBuilder sb  = new StringBuilder();
        sb.append("========== WORKER PERFORMANCE REPORT ==========\n");

        double avgFatigue = 0;

        for (TiredThread worker : workers){
            sb.append(worker.getReport());
            sb.append("-----------------------------------------------\n");
            avgFatigue += worker.getFatigue();
        }

        avgFatigue = avgFatigue / workers.length;

        double sumOfSquare = 0;
        for (TiredThread worker : workers){
            double difference = worker.getFatigue() - avgFatigue;
            sumOfSquare += difference*difference;
        }

        sb.append("Fairness: " + sumOfSquare + "\n");
        return sb.toString();
    } 
}
