import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.awt.Color;
import java.util.ArrayList;

class Process {
    String name;
    int lastExecutionTime;
    int arrivalTime;
    int burstTime;
    int priority;
    int quantum;
    int remainingTime;
    double fcaiFactor;
    int waitingTime = 0;
    int turnaroundTime = 0;
    Color color;
    int usedQuantum;
    List<Integer> quantumHistory;

    public Process(String name, int arrivalTime, int burstTime, int priority, int initialQuantum, Color color) {
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
        this.quantum = initialQuantum;
        this.color = color;
        this.usedQuantum = 0;
        this.remainingTime = burstTime;
        this.lastExecutionTime = 0;
        this.quantumHistory = new ArrayList<>();
        this.quantumHistory.add(quantum);
    }

    public void calculateFcaiFactor(double v1, double v2) {
        this.fcaiFactor = Math.ceil(10 - this.priority) +Math.ceil (this.arrivalTime / v1) + Math.ceil(this.remainingTime / v2);
    }

    public int getPriority() {
        return priority;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public void updateQuantum() {
        if (this.remainingTime > 0 && this.usedQuantum >= this.quantum) {
            this.quantum += 2;
        } else if (this.usedQuantum > 0 && this.usedQuantum < this.quantum) {
            this.quantum += (this.quantum - this.usedQuantum);
        }
        quantumHistory.add(this.quantum);
        this.usedQuantum = 0;
    }
}

public class SchedulerSimulation {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        List<Process> processes = new ArrayList<>();

        System.out.print("Enter number of processes: ");
        int n = sc.nextInt();

        for (int i = 0; i < n; i++) {
            System.out.print("Enter Process Name: ");
            String name = sc.next();
            System.out.print("Enter Arrival Time: ");
            int at = sc.nextInt();
            System.out.print("Enter Burst Time: ");
            int bt = sc.nextInt();
            System.out.print("Enter Priority (lower number = higher priority): ");
            int priority = sc.nextInt();
            System.out.print("Enter Initial Quantum: ");
            int quantum = sc.nextInt();


            System.out.print("Enter color (e.g., red): ");
            String colorInput = sc.next();


            Color color = null;
            try {

                color = (Color) Color.class.getField(colorInput.toLowerCase()).get(null);

            } catch (Exception e) {
                System.out.println("Invalid color input, using default black.");
                color = Color.BLACK;
            }

            processes.add(new Process(name, at, bt, priority, quantum, color));
        }


        System.out.print("Enter context switching time: ");
        int contextSwitchingTime = sc.nextInt();


        System.out.println("Select Scheduling Algorithm:");
        System.out.println("1. Non-preemptive Priority Scheduling");
        System.out.println("2. Non-Preemptive Shortest Job First (SJF)");
        System.out.println("3. Shortest Remaining Time First (SRTF)");
        System.out.println("4. FCAI Scheduling");
        int choice = sc.nextInt();

        switch (choice) {
            case 1:
                nonPreemptivePriority(processes, contextSwitchingTime);
                break;
            case 2:
                nonPreemptiveSJF(processes);
                break;
            case 3:

                for (Process p : processes) {

                    p.quantum = -1;
                }
                shortestRemainingTimeFirst(processes ,contextSwitchingTime);
                break;
            case 4:
                fcaiScheduling(processes);
                break;
            default:
                System.out.println("Invalid choice");
        }

        sc.close();
    }

    static void nonPreemptivePriority(List<Process> processes, int contextSwitchingTime) {

        processes.sort(Comparator.comparingInt((Process p) -> p.priority)
                .thenComparingInt(p -> p.arrivalTime));

        List<Process> executionOrder = new ArrayList<>();
        int time = 0;

        while (!processes.isEmpty()) {

            List<Process> availableProcesses = new ArrayList<>();
            for (Process p : processes) {
                if (p.arrivalTime <= time) {
                    availableProcesses.add(p);
                }
            }

            if (availableProcesses.isEmpty()) {

                time++;
                continue;
            }

            int agingTimeThreshold = 100;
            int agingIncrease = 1;

            for (Process p : availableProcesses) {
                if (time - p.arrivalTime > agingTimeThreshold) {
                    p.priority = Math.max(1, p.priority - agingIncrease);
                }
            }

            Process current = availableProcesses.stream()
                    .min(Comparator.comparingInt(Process::getPriority)
                            .thenComparingInt(Process::getArrivalTime))
                    .orElseThrow();


            processes.remove(current);

            System.out.println("Time " + time + ": Process " + current.name + " starts execution");



            time += current.burstTime + contextSwitchingTime;



            if(time==0 || time <current.arrivalTime) {

                current.turnaroundTime =  current.arrivalTime - time ;
                if( current.turnaroundTime> current.burstTime){
                    current.waitingTime = current.turnaroundTime - current.burstTime;
                }
                else {
                    current.waitingTime =  current.burstTime - current.turnaroundTime ;
                }
            }
            else {
                current.turnaroundTime = time - current.arrivalTime;
                if( current.turnaroundTime> current.burstTime){
                    current.waitingTime = current.turnaroundTime - current.burstTime;
                }
                else {
                    current.waitingTime =  current.burstTime - current.turnaroundTime ;
                }
            }
            executionOrder.add(current);
        }

        printExecutionOrder(executionOrder);
        calculateAverageTimes(executionOrder);
        drawExecutionOrder(executionOrder);
    }

    static void nonPreemptiveSJF(List<Process> processes) {
        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));
        List<Process> executionOrder = new ArrayList<>();
        int time = 0;
        int agingTimeThreshold = 30;

        while (!processes.isEmpty()) {

            List<Process> availableProcesses = new ArrayList<>();
            for (Process p : processes) {
                if (p.arrivalTime <= time) {
                    availableProcesses.add(p);
                }
            }

            if (availableProcesses.isEmpty()) {

                time++;
                continue;
            }


            boolean priorityChanged = false;
            int i = 0;
            while (i < availableProcesses.size()) {
                Process p = availableProcesses.get(i);
                int waitingTime = time - p.arrivalTime;
                if (waitingTime >= agingTimeThreshold) {
                    p.priority = Math.max(0, p.priority - 1);
                    priorityChanged = true;
                    System.out.println("Process " + p.name + " aged at time " + time + ", new priority: " + p.priority);
                }
                i++;
            }
            Process current;
            if (priorityChanged) {

                current = availableProcesses.stream()
                        .min(Comparator.comparingInt((Process p) -> p.priority)
                                .thenComparingInt(p -> p.burstTime))
                        .orElseThrow();

            } else {

                current = availableProcesses.stream()
                        .min(Comparator.comparingInt(p -> p.burstTime))
                        .orElseThrow();

            }
            processes.remove(current);

            System.out.println("Time " + time + ": Process " + current.name + " starts execution");


            time += current.burstTime ;


            if(time==0 || time <current.arrivalTime) {

                current.turnaroundTime =  current.arrivalTime - time ;
                if( current.turnaroundTime> current.burstTime){
                    current.waitingTime = current.turnaroundTime - current.burstTime;
                }
                else {
                    current.waitingTime =  current.burstTime - current.turnaroundTime ;
                }
            }
            else {
                current.turnaroundTime = time - current.arrivalTime;
                if( current.turnaroundTime> current.burstTime){
                    current.waitingTime = current.turnaroundTime - current.burstTime;
                }
                else {
                    current.waitingTime =  current.burstTime - current.turnaroundTime ;
                }

            }

            executionOrder.add(current);
        }

        printExecutionOrder(executionOrder);
        calculateAverageTimes(executionOrder);
        drawExecutionOrder(executionOrder);
    }

    static void shortestRemainingTimeFirst(List<Process> processes, int contextSwitchTime) {
        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));
        PriorityQueue<Process> readyQueue = new PriorityQueue<>(Comparator.comparingInt(p -> p.remainingTime));
        List<Process> executionOrder = new ArrayList<>();
        Set<Process> completedProcesses = new HashSet<>();
        int time = 0;
        int contextSwitches = 0;
        Process previous = null;
        int agingTimeThreshold=60;
        boolean priorityChanged = false;
        Process lastCompletedProcess = null;

        for (Process p : processes) {
            p.remainingTime = p.burstTime;
            p.waitingTime = 0;
            p.lastExecutionTime = p.arrivalTime;
        }

        while (!processes.isEmpty() || !readyQueue.isEmpty()) {

            while (!processes.isEmpty() && processes.get(0).arrivalTime <= time) {
                Process process = processes.remove(0);
                readyQueue.add(process);
            }

            if (!readyQueue.isEmpty()) {
                for (Process p : readyQueue) {
                    int waitingTime = time - p.arrivalTime;
                    if (waitingTime >= agingTimeThreshold) {

                        p.priority = Math.max(0, p.priority - 1);
                        priorityChanged = true;
                        System.out.println("Process " + p.name + " aged at time " + time + " new priority: " + p.priority);
                    }
                }


                Process current;
                if (priorityChanged) {

                    current = readyQueue.stream()
                            .sorted(Comparator.comparingInt((Process p) -> p.priority)
                                    .thenComparingInt(p -> p.remainingTime))
                            .findFirst().orElseThrow();

                } else {

                    current = readyQueue.poll();

                }


                if (previous != null && current != previous) {

                    contextSwitches++;
                    time += contextSwitchTime;
                    executionOrder.add(new Process("CS", time, 0, 0, 0, Color.YELLOW));
                }


                current.remainingTime--;
                if (time > current.lastExecutionTime) {
                    current.waitingTime += time - current.lastExecutionTime;
                }

                current.lastExecutionTime = time + 1;
                executionOrder.add(current);

                if (current.remainingTime == 0 && !completedProcesses.contains(current)) {

                    current.turnaroundTime = time + 1 - current.arrivalTime;
                    current.waitingTime += contextSwitchTime;
                    current.turnaroundTime += contextSwitchTime;
                    completedProcesses.add(current);
                    lastCompletedProcess = current;
                } else {
                    readyQueue.add(current);
                }

                previous = current;
                time++;
            } else {

                if (previous != null && !readyQueue.isEmpty()) {

                    contextSwitches++;
                    time += contextSwitchTime;
                    executionOrder.add(new Process("CONTEXT SWITCH", time, 0, 0, 0, Color.YELLOW));
                }

                executionOrder.add(new Process("IDLE", time, 0, 0, 0, Color.GRAY));
                previous = null;
                time++;
            }
        }

        if (lastCompletedProcess != null) {
            contextSwitches++;
            time += contextSwitchTime;
            executionOrder.add(new Process("CS", time, 0, 0, 0, Color.YELLOW));
        }
        printExecutionOrder(executionOrder);
        calculateAverageTimes(new ArrayList<>(completedProcesses));
        drawExecutionOrder(executionOrder);
        System.out.println("Total Context Switches: " + contextSwitches);
    }

    public static void fcaiScheduling(List<Process> processList) {
        int currentTime = 0;
        double lastArrivalTime = processList.stream().mapToDouble(p -> p.arrivalTime).max().orElse(0);
        double maxBurstTime = processList.stream().mapToDouble(p -> p.burstTime).max().orElse(0);
        double v1 = lastArrivalTime / 10.0;
        double v2 = maxBurstTime / 10.0;

        Queue<Process> readyQueue = new LinkedList<>();
        List<Process> completedProcesses = new ArrayList<>();
        List<Process> executionOrder = new ArrayList<>();
        Process currentProcess = null;

        while (!processList.isEmpty() || !readyQueue.isEmpty() || currentProcess != null) {
            for (Iterator<Process> it = processList.iterator(); it.hasNext(); ) {
                Process process = it.next();
                if (process.arrivalTime <= currentTime) {
                    process.calculateFcaiFactor(v1, v2);
                    readyQueue.add(process);
                    System.out.printf("Time %d: Process %s added to Ready Queue with FCAI = %.2f\n", currentTime, process.name, process.fcaiFactor);
                    it.remove();
                }
            }

            if (currentProcess == null && !readyQueue.isEmpty()) {
                currentProcess = readyQueue.poll();
                currentProcess.usedQuantum = 0;
                System.out.printf("Time %d: Process %s selected from Ready Queue with FCAI = %.2f\n", currentTime, currentProcess.name, currentProcess.fcaiFactor);
                executionOrder.add(currentProcess);
            }

            if (currentProcess != null) {
                int executionTime = Math.min(currentProcess.remainingTime, currentProcess.quantum - currentProcess.usedQuantum);
                for (int t = 0; t < executionTime; t++) {
                    currentTime++;
                    currentProcess.remainingTime--;
                    currentProcess.usedQuantum++;

                    for (Iterator<Process> it = processList.iterator(); it.hasNext(); ) {
                        Process process = it.next();
                        if (process.arrivalTime <= currentTime) {
                            process.calculateFcaiFactor(v1, v2);
                            readyQueue.add(process);
                            System.out.printf("Time %d: Process %s added to Ready Queue with FCAI = %.2f\n", currentTime, process.name, process.fcaiFactor);
                            it.remove();
                        }
                    }

                    if (currentProcess.usedQuantum >= 0.4 * currentProcess.quantum) {
                        Process preemptingProcess = null;
                        for (Process process : readyQueue) {
                            if (process.arrivalTime <= currentTime && (preemptingProcess == null || process.fcaiFactor < preemptingProcess.fcaiFactor)) {
                                preemptingProcess = process;
                            }
                        }

                        if (preemptingProcess != null && preemptingProcess.fcaiFactor < currentProcess.fcaiFactor) {
                            System.out.printf("Time %d: Preempting Process %s with Process %s (Lower FCAI)\n", currentTime, currentProcess.name, preemptingProcess.name);
                            currentProcess.updateQuantum();
                            currentProcess.calculateFcaiFactor(v1, v2);
                            readyQueue.add(currentProcess);

                            currentProcess = preemptingProcess;
                            readyQueue.remove(preemptingProcess);
                            executionOrder.add(currentProcess);

                            break;
                        }
                    }
                }

                if (currentProcess.remainingTime == 0) {
                    System.out.printf("Time %d: Process %s completed\n", currentTime, currentProcess.name);
                    completedProcesses.add(currentProcess);
                    currentProcess.turnaroundTime = currentTime - currentProcess.arrivalTime;
                    currentProcess.waitingTime = currentProcess.turnaroundTime - currentProcess.burstTime;
                    currentProcess = null;
                }

                else if (currentProcess.usedQuantum >= currentProcess.quantum) {
                    System.out.printf("Time %d: Process %s quantum exhausted, re-queuing\n", currentTime, currentProcess.name);
                    currentProcess.updateQuantum();
                    currentProcess.calculateFcaiFactor(v1, v2);
                    readyQueue.add(currentProcess);

                    currentProcess = null;
                }
            }

            if (currentProcess == null && readyQueue.isEmpty() && !processList.isEmpty()) {
                currentTime = processList.stream().mapToInt(p -> p.arrivalTime).min().orElse(currentTime);
                executionOrder.add(new Process("IDLE",currentTime, 0, 0, 0, Color.GRAY));
            }
        }

        printExecutionOrder(executionOrder);
        calculateAverageTimes(new ArrayList<>(completedProcesses));
        drawFCAIExecutionOrder(executionOrder);
        System.out.println("\nFinal Quantum History for All Processes:");
        for (Process process : completedProcesses) {
            System.out.printf("Process %s: %s\n", process.name, process.quantumHistory);
        }

    }

    static JPanel createFCAITablePanel(List<Process> processes) {
        String[] columnNames = {"Process", "Color", "Name", "PID", "Priority", "Quantum History"};
        Object[][] data = new Object[processes.size()][6];

        for (int i = 0; i < processes.size(); i++) {
            Process p = processes.get(i);
            data[i][0] = i;
            data[i][1] = "";
            data[i][2] = p.name;
            data[i][3] = 3280 + i;
            data[i][4] = p.priority;
            data[i][5] = p.quantumHistory.toString();
        }

        JTable table = new JTable(data, columnNames) {
            @Override
            public Class<?> getColumnClass(int column) {
                return (column == 1) ? Color.class : Object.class;
            }

            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (column == 1) {
                    c.setBackground(processes.get(row).color);
                } else {
                    c.setBackground(Color.WHITE);
                }
                return c;
            }
        };

        table.setRowHeight(30);
        JScrollPane scrollPane = new JScrollPane(table);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    static void drawFCAIExecutionOrder(List<Process> executionOrder) {
        JFrame frame = new JFrame("FCAI Execution Order with Process Information");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(600);
        JPanel timelinePanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                int x = 10;
                for (Process p : executionOrder) {
                    int executionTimeWidth = p.usedQuantum * 10;
                    g.setColor(p.color);
                    g.fillRect(x, 50, executionTimeWidth, 50);
                    g.setColor(Color.BLACK);
                    g.drawRect(x, 50, executionTimeWidth, 50);
                    g.drawString(p.name, x + executionTimeWidth / 4, 75);
                    x += executionTimeWidth + 10;
                }
            }
        };

        timelinePanel.setPreferredSize(new Dimension(600, 200));
        splitPane.setLeftComponent(timelinePanel);

        JPanel tablePanel = createFCAITablePanel(executionOrder);
        splitPane.setRightComponent(tablePanel);

        frame.add(splitPane);
        frame.setVisible(true);
    }

    static void printExecutionOrder(List<Process> executionOrder) {
        System.out.println("Execution Order:");

        for (Process p : executionOrder) {
            System.out.println("Process " + p.name + " (Waiting Time: " + p.waitingTime + ", Turnaround Time: " + p.turnaroundTime + ")");
        }

    }

    static void calculateAverageTimes(List<Process> executionOrder) {
        double totalWaitingTime = 0, totalTurnaroundTime = 0;
        for (Process p : executionOrder) {
            totalWaitingTime += p.waitingTime;
            totalTurnaroundTime += p.turnaroundTime;
        }
        System.out.println("Average Waiting Time: " + (totalWaitingTime / executionOrder.size()));
        System.out.println("Average Turnaround Time: " + (totalTurnaroundTime / executionOrder.size()));
    }

    static JPanel createTablePanel(List<Process> processes) {
        String[] columnNames = {"PROCESS", "COLOR", "NAME", "PID", "PRIORITY"};
        Object[][] data = new Object[processes.size()][5];

        for (int i = 0; i < processes.size(); i++) {
            Process p = processes.get(i);
            data[i][0] = i;
            data[i][1] = "";
            data[i][2] = p.name;
            data[i][3] = 3280 + i;
            data[i][4] = p.priority;
        }

        JTable table = new JTable(data, columnNames) {
            @Override
            public Class<?> getColumnClass(int column) {
                return (column == 1) ? Color.class : Object.class;
            }

            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (column == 1) {
                    c.setBackground(processes.get(row).color);
                } else {
                    c.setBackground(Color.WHITE);
                }
                return c;
            }
        };

        table.setRowHeight(30);
        JScrollPane scrollPane = new JScrollPane(table);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    static void drawExecutionOrder(List<Process> executionOrder) {
        JFrame frame = new JFrame("Execution Order with Process Information");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);


        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(600);


        JPanel timelinePanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                int x = 10;
                int currentTime = 0;

                for (Process p : executionOrder) {
                    int processWidth = p.burstTime * 10;

                    g.setColor(p.color);
                    g.fillRect(x, 50, processWidth, 50);
                    g.setColor(Color.BLACK);
                    g.drawRect(x, 50, processWidth, 50);
                    g.drawString(p.name, x + processWidth / 4, 75);

                    x += processWidth + 10;
                    currentTime += p.burstTime;
                }
            }
        };
        timelinePanel.setPreferredSize(new Dimension(600, 200));
        splitPane.setLeftComponent(timelinePanel);

        JPanel tablePanel = createTablePanel(executionOrder);
        splitPane.setRightComponent(tablePanel);

        frame.add(splitPane);
        frame.setVisible(true);
    }
}