import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * SandwichManager
 */
public class SandwichManager {
    // Static variables
    static int numSandwiches,
            breadCapacity,
            eggCapacity,
            numBreadMakers,
            numEggMakers,
            numSandwichPackers,
            breadRate,
            eggRate,
            packingRate;
    static int requiredBread = 0, requiredEgg = 0, requiredSandwich = 0;
    static CircularBuffer<Bread> breadBuffer;
    static CircularBuffer<Egg> eggBuffer;

    public static void main(String[] args) {
        numSandwiches = Integer.parseInt(args[0]);
        breadCapacity = Integer.parseInt(args[1]);
        eggCapacity = Integer.parseInt(args[2]);
        numBreadMakers = Integer.parseInt(args[3]);
        numEggMakers = Integer.parseInt(args[4]);
        numSandwichPackers = Integer.parseInt(args[5]);
        breadRate = Integer.parseInt(args[6]);
        eggRate = Integer.parseInt(args[7]);
        packingRate = Integer.parseInt(args[8]);

        // Create the log header
        String systemLineSeparator = System.lineSeparator();
        String logHead = String.format(
                "sandwiches: %d%sbread capacity: %d%segg capacity: %d%sbread makers: %d%segg makers: %d%sandwich packers: %d%sbread rate: %d%segg rate: %d%spacking rate%d%s",
                numSandwiches, systemLineSeparator, breadCapacity, systemLineSeparator, eggCapacity,
                systemLineSeparator, numBreadMakers, systemLineSeparator, numEggMakers, systemLineSeparator,
                numSandwichPackers, systemLineSeparator, breadRate, systemLineSeparator, eggRate, systemLineSeparator,
                packingRate, systemLineSeparator);
        writeToLog(logHead, false);

        // We first want to set the total number of required ingredients
        requiredBread = numSandwiches * 2;
        requiredEgg = numSandwiches;
        requiredSandwich = numSandwiches;

        // Initialise our ingredient buffers
        breadBuffer = new CircularBuffer<Bread>(breadCapacity);
        eggBuffer = new CircularBuffer<Egg>(eggCapacity);

        // Initialise our machine threads
        BreadMachine[] breadMachines = new BreadMachine[numBreadMakers];
        EggMachine[] eggMachines = new EggMachine[numEggMakers];
        SandwichMachine[] sandwichMachines = new SandwichMachine[numSandwichPackers];

        // Start our threads
        for (int i = 0; i < numBreadMakers; i++) {
            breadMachines[i] = new BreadMachine(i, breadRate);
            breadMachines[i].start();
        }
        for (int i = 0; i < numEggMakers; i++) {
            eggMachines[i] = new EggMachine(i, eggRate);
            eggMachines[i].start();
        }
        for (int i = 0; i < numSandwichPackers; i++) {
            sandwichMachines[i] = new SandwichMachine(i, packingRate);
            sandwichMachines[i].start();
        }

        // Wait for threads to finish
        for (Thread breadThread : breadMachines) {
            try {
                breadThread.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (Thread eggThread : eggMachines) {
            try {
                eggThread.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (Thread sandwichThread : sandwichMachines) {
            try {
                sandwichThread.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Print out completion status with the number of sandwiches made and buffer
        // status
        // System.out.println("------------------------------\nJob status: " +
        // (requiredSandwich == 0 ? "SUCCESS" : "FAILURE") );
        // System.out.println("Sandwiches made: " + (numSandwiches - requiredSandwich));
        // System.out.println("Bread buffer: " + breadBufferItemCount);
        // System.out.println("Egg buffer: " + eggBufferItemCount);

        // Create the log summary
        StringBuilder summary = new StringBuilder();
        summary.append(systemLineSeparator + "summary:" + systemLineSeparator);

        for (BreadMachine breadMachine : breadMachines) {
            summary.append(breadMachine.getAmountMade() + systemLineSeparator);
        }
        for (EggMachine eggMachine : eggMachines) {
            summary.append(eggMachine.getAmountMade() + systemLineSeparator);
        }
        for (SandwichMachine sandwichMachine : sandwichMachines) {
            summary.append(sandwichMachine.getAmountMade() + systemLineSeparator);
        }

        System.out.println(summary.toString());
        writeToLog(summary.toString(), true);

    }

    static synchronized boolean canMakeBread() {
        if (requiredBread > 0) {
            requiredBread--;
            return true;
        }
        return false;
    }

    static synchronized boolean canMakeEgg() {
        if (requiredEgg > 0) {
            requiredEgg--;
            return true;
        }
        return false;
    }

    static synchronized boolean canMakeSandwich() {
        if (requiredSandwich > 0) {
            requiredSandwich--;
            return true;
        }
        return false;
    }

    // Synchronized method to write to the log
    static synchronized void writeToLog(String s, boolean append) {
        try {
            FileWriter fw = new FileWriter(new File("./log.txt"), append);
            fw.write(s);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void gowork(int n) {
        for (int i = 0; i < n; i++) {
            long m = 300000000;
            while (m > 0) {
                m--;
            }
        }
    }
}

class Bread {
    int id;
    String machineId;

    public Bread(int id, String name) {
        this.id = id;
        this.machineId = name;
    }

    @Override
    public String toString() {
        return "bread " + id + " from " + machineId;
    }
}

class BreadMachine extends Thread {
    private String machineId;
    private int rate;
    private int numMade = 0;

    public BreadMachine(int id, int rate) {
        this.machineId = "B" + id;
        this.rate = rate;
    }

    @Override
    public void run() {
        while (SandwichManager.canMakeBread()) {
            SandwichManager.gowork(rate); // Making bread...
            // Put made bread into the buffer
            String log = String.format("%s puts bread %d%s", machineId, numMade,
                    System.lineSeparator());
            SandwichManager.writeToLog(log, true);
            try {
                SandwichManager.breadBuffer.put(new Bread(numMade++, machineId));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public String getAmountMade() {
        return machineId + " makes " + numMade;
    }
}

class Egg {
    int id;
    String machineId;

    public Egg(int id, String name) {
        this.id = id;
        this.machineId = name;
    }

    @Override
    public String toString() {
        return "egg " + id + " from " + machineId;
    }
}

class EggMachine extends Thread {
    private String machineId;
    private int rate;
    private int numMade = 0;

    public EggMachine(int id, int rate) {
        this.machineId = "E" + id;
        this.rate = rate;
    }

    @Override
    public void run() {
        while (SandwichManager.canMakeEgg()) {
            SandwichManager.gowork(rate); // Making bread...
            // Put made bread into the buffer
            String log = String.format("%s puts egg %d%s", machineId, numMade,
                    System.lineSeparator());
            SandwichManager.writeToLog(log, true);
            try {
                SandwichManager.eggBuffer.put(new Egg(numMade++, machineId));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public String getAmountMade() {
        return machineId + " makes " + numMade;
    }
}

class SandwichMachine extends Thread {
    private String machineId;
    private int rate;
    private int numMade = 0;

    public SandwichMachine(int id, int rate) {
        this.machineId = "S" + id;
        this.rate = rate;
    }

    @Override
    public void run() {
        while (SandwichManager.canMakeSandwich()) {
            try {
                // Get ingredients from the buffer [DEADLOCK PREVENTION]
                Bread top, bottom;
                Egg egg;
                // NOTE: synchronized block is used to ensure that the three get operations are
                // atomic to prevent deadlock on ingredient allocation
                // synchronized (SandwichManager.allocatorMonitor) {
                // top = SandwichManager.getBread();
                // egg = SandwichManager.getEgg();
                // bottom = SandwichManager.getBread();
                // }
                top = SandwichManager.breadBuffer.get();
                bottom = SandwichManager.breadBuffer.get();
                egg = SandwichManager.eggBuffer.get();

                SandwichManager.gowork(rate); // Packing sandwich...
                // Write to log
                String log = String.format(
                        "%s packs sandwich %d with bread %d from %s and egg %d from %s and bread %d from %s%s",
                        machineId,
                        numMade++,
                        top.id,
                        top.machineId,
                        egg.id,
                        egg.machineId,
                        bottom.id,
                        bottom.machineId,
                        System.lineSeparator());
                SandwichManager.writeToLog(log, true);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public String getAmountMade() {
        return machineId + " makes " + numMade;
    }
}

class CircularBuffer<T> {
    private T[] buffer;
    private int size;
    private int head;
    private int tail;

    public CircularBuffer(int size) {
        this.buffer = (T[]) new Object[size];
        this.size = size;
        this.head = 0;
        this.tail = 0;
    }

    public synchronized void put(T item) throws InterruptedException {
        while (isFull()) {
            wait();
        }
        buffer[head] = item;
        head = (head + 1) % size;
        notifyAll();
    }

    public synchronized T get() throws InterruptedException {
        while (isEmpty()) {
            wait();
        }
        T item = buffer[tail];
        tail = (tail + 1) % size;
        notifyAll();
        return item;
    }

    public synchronized boolean isEmpty() {
        return head == tail;
    }

    public synchronized boolean isFull() {
        return (head + 1) % size == tail;
    }
}
