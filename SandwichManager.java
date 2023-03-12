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
                "sandwiches: %d%sbread capacity: %d%segg capacity: %d%sbread makers: %d%segg makers: %d%ssandwich packers: %d%sbread rate: %d%segg rate: %d%spacking rate: %d%s%s",
                numSandwiches, systemLineSeparator, breadCapacity, systemLineSeparator, eggCapacity,
                systemLineSeparator, numBreadMakers, systemLineSeparator, numEggMakers, systemLineSeparator,
                numSandwichPackers, systemLineSeparator, breadRate, systemLineSeparator, eggRate, systemLineSeparator,
                packingRate, systemLineSeparator, systemLineSeparator);
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

class GenericMachine extends Thread {
    private String machineId;
    private int rate;
    private int numMade = 0;

    public GenericMachine(String prefix, int id, int rate) {
        this.machineId = prefix + id;
        this.rate = rate;
    }

    public String getMachineId() {
        return machineId;
    }

    public void setMachineId(String machineId) {
        this.machineId = machineId;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public synchronized int getNumMade() {
        return numMade;
    }

    public synchronized void setNumMade(int numMade) {
        this.numMade = numMade;
    }

    public String getAmountMade() {
        return machineId + " makes " + numMade;
    }
}

class GenericIngredient {
    private String machineId;
    private String name;
    private int id;

    public GenericIngredient(int id, String machineId, String name) {
        this.id = id;
        this.machineId = machineId;
        this.name = name;
    }

    public String getMachineId() {
        return machineId;
    }

    public void setMachineId(String machineId) {
        this.machineId = machineId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return name + " " + id + " from " + machineId;
    }
}

class Bread extends GenericIngredient {
    public Bread(int id, String machineId) {
        super(id, machineId, "bread");
    }
}

class BreadMachine extends GenericMachine {

    public BreadMachine(int id, int rate) {
        super("B", id, rate);
    }

    @Override
    public void run() {
        while (SandwichManager.canMakeBread()) {
            // Make bread
            SandwichManager.gowork(getRate());
            try {
                // Put made bread into the buffer
                int numMade = getNumMade();
                SandwichManager.breadBuffer.put(new Bread(numMade++, getMachineId()));
                setNumMade(numMade);
                String log = String.format("%s puts bread %d%s", getMachineId(), numMade,
                        System.lineSeparator());
                SandwichManager.writeToLog(log, true);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class Egg extends GenericIngredient {
    public Egg(int id, String machineId) {
        super(id, machineId, "egg");
    }
}

class EggMachine extends GenericMachine {

    public EggMachine(int id, int rate) {
        super("E", id, rate);
    }

    @Override
    public void run() {
        while (SandwichManager.canMakeEgg()) {
            // Making egg
            SandwichManager.gowork(getRate());
            try {
                // Put made egg into the buffer
                int numMade = getNumMade();
                SandwichManager.eggBuffer.put(new Egg(numMade++, getMachineId()));
                setNumMade(numMade);
                String log = String.format("%s puts egg %d%s", getMachineId(), numMade,
                        System.lineSeparator());
                SandwichManager.writeToLog(log, true);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class SandwichMachine extends GenericMachine {

    public SandwichMachine(int id, int rate) {
        super("S", id, rate);
    }

    @Override
    public void run() {
        while (SandwichManager.canMakeSandwich()) {
            try {
                // Get ingredients from the bread and egg buffers
                Bread top, bottom;
                Egg egg;
                top = SandwichManager.breadBuffer.get();
                bottom = SandwichManager.breadBuffer.get();
                egg = SandwichManager.eggBuffer.get();

                // Pack sandwich
                SandwichManager.gowork(getRate());
                int numMade = getNumMade();
                String log = String.format(
                        "%s packs sandwich %d with bread %d from %s and egg %d from %s and bread %d from %s%s",
                        getMachineId(),
                        numMade++,
                        top.getId(),
                        top.getMachineId(),
                        egg.getId(),
                        egg.getMachineId(),
                        bottom.getId(),
                        bottom.getMachineId(),
                        System.lineSeparator());
                SandwichManager.writeToLog(log, true);
                setNumMade(numMade);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

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
