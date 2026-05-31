import scheduling.TiredExecutor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class TiredExecutorTest {

    private TiredExecutor executor;
    private final int NUM_THREADS = 3;

    @BeforeEach
    void setUp() {
        executor = new TiredExecutor(NUM_THREADS);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        if (executor != null) {
            executor.shutdown();
        }
    }

    // --- Basic Functionality ---

    @Test
    void testSubmitAllEmptyList() {
        assertDoesNotThrow(() -> executor.submitAll(Collections.emptyList()));
    }

    @Test
    void testSingleTask() {
        AtomicBoolean ran = new AtomicBoolean(false);
        executor.submit(() -> ran.set(true));
        
        List<Runnable> tasks = new ArrayList<>();
        tasks.add(() -> ran.set(true));
        executor.submitAll(tasks);
        
        assertTrue(ran.get());
    }

    // --- Concurrency & Load ---

    @Test
    void testHighLoadManyTasks() {
        int numTasks = 1000;
        AtomicInteger counter = new AtomicInteger(0);
        List<Runnable> tasks = new ArrayList<>();
        
        for (int i = 0; i < numTasks; i++) {
            tasks.add(counter::incrementAndGet);
        }

        executor.submitAll(tasks);
        
        assertEquals(numTasks, counter.get(), "All 1000 tasks should be executed");
    }

    @Test
    void testWaitMechanism() throws InterruptedException {
        AtomicBoolean finishedTooEarly = new AtomicBoolean(false);
        int sleepTime = 200;
        
        List<Runnable> tasks = new ArrayList<>();
        tasks.add(() -> {
            try { Thread.sleep(sleepTime); } catch (InterruptedException e) {}
        });

        Thread mainTester = new Thread(() -> {
            executor.submitAll(tasks);
            finishedTooEarly.set(true);
        });
        
        mainTester.start();
        
        Thread.sleep(50); 
        assertFalse(finishedTooEarly.get(), "submitAll returned before the task was finished!");
        
        mainTester.join();
        assertTrue(finishedTooEarly.get());
    }
    
    // --- Interruptions ---

    @Test
    void testInterruptedWhileWaiting() {

        Thread mainThread = Thread.currentThread();
        
        List<Runnable> tasks = new ArrayList<>();
        tasks.add(() -> {
            try { 
                Thread.sleep(2000);
            } catch (InterruptedException e) {} 
            
            mainThread.interrupt(); 
        });
        
        executor.submitAll(tasks);
        
        assertTrue(Thread.interrupted(), "Main thread should have the interrupt flag set after submitAll");
    }
}