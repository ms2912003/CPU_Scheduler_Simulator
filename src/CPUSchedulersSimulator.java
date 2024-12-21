import java.util.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.*;

class Process {
    String name;
    int arrivalTime, burstTime, priority, waitingTime, turnaroundTime, remainingTime, quantum;
    double fcaiFactor;
    Color color;

    public Process(String name, int burstTime, int arrivalTime, int priority, int quantum, Color color) {
        this.name = name;
        this.burstTime = burstTime;
        this.arrivalTime = arrivalTime;
        this.priority = priority;
        this.remainingTime = burstTime;
        this.fcaiFactor = 0;
        this.quantum = quantum;
        this.color = color;
    }
}

public class CPUSchedulersSimulator {

    static JPanel timelinePanel;

    public static void main(String[] args) {
        JFrame frame = new JFrame("CPU Schedulers Simulator");
        frame.setSize(700, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);
        frame.setResizable(false);

        JLabel titleLabel = new JLabel("CPU Scheduling Simulator");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBounds(200, 10, 300, 30);
        frame.add(titleLabel);

        JLabel processCountLabel = new JLabel("Number of Processes:");
        processCountLabel.setBounds(50, 60, 200, 30);
        frame.add(processCountLabel);

        JTextField processCountField = new JTextField();
        processCountField.setBounds(200, 60, 50, 30);
        frame.add(processCountField);

        JTextArea processInputArea = new JTextArea("Format: Name,Burst,Arrival,Priority,Quantum,Color\nOne per line");
        processInputArea.setBounds(50, 100, 400, 150);
        processInputArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        frame.add(processInputArea);

        JLabel algorithmLabel = new JLabel("Select Algorithm:");
        algorithmLabel.setBounds(50, 270, 200, 30);
        frame.add(algorithmLabel);

        String[] algorithms = {"Non-Preemptive SJF", "SRTF", "Priority Scheduling", "FCAI"};
        JComboBox<String> algorithmDropdown = new JComboBox<>(algorithms);
        algorithmDropdown.setBounds(200, 270, 200, 30);
        frame.add(algorithmDropdown);

        JButton runButton = new JButton("Run Scheduler");
        runButton.setBounds(150, 320, 150, 40);
        frame.add(runButton);

        JTextArea outputArea = new JTextArea();
        outputArea.setBounds(50, 380, 400, 150);
        outputArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        outputArea.setEditable(false);
        frame.add(outputArea);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBounds(50, 380, 400, 150);
        frame.add(scrollPane);

        timelinePanel = new JPanel();
        timelinePanel.setBounds(50, 550, 600, 100);
        timelinePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
        timelinePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        frame.add(timelinePanel);

        runButton.addActionListener(e -> {
            try {
                int numProcesses = Integer.parseInt(processCountField.getText().trim());
                List<Process> processes = new ArrayList<>();
                String[] processDetails = processInputArea.getText().split("\n");

                for (String processLine : processDetails) {
                    String[] details = processLine.split(",");
                    if (details.length != 6) {
                        outputArea.setText("Invalid input format. Use Name,Burst,Arrival,Priority,Quantum,Color");
                        return;
                    }
                    String name = details[0].trim();
                    int burstTime = Integer.parseInt(details[1].trim());
                    int arrivalTime = Integer.parseInt(details[2].trim());
                    int priority = Integer.parseInt(details[3].trim());
                    int quantum = Integer.parseInt(details[4].trim());
                    Color color = getColorByName(details[5].trim().toLowerCase());

                    if (color == null) {
                        outputArea.setText("Invalid color name: " + details[5].trim());
                        return;
                    }

                    processes.add(new Process(name, burstTime, arrivalTime, priority, quantum, color));
                }

                String selectedAlgorithm = (String) algorithmDropdown.getSelectedItem();
                StringBuilder result = new StringBuilder();
                timelinePanel.removeAll();
                List<Process> processCopy = deepCopy(processes);

                switch (selectedAlgorithm) {
                    case "Non-Preemptive SJF":
                        result.append("Non-Preemptive Shortest Job First Scheduling:\n");
                        sjfScheduling(processCopy, numProcesses, result);
                        break;
                    case "SRTF":
                        result.append("Shortest Remaining Time First Scheduling:\n");
                        srtfScheduling(processCopy, numProcesses, result);
                        break;
                    case "Priority Scheduling":
                        result.append("Priority Scheduling:\n");
                        priorityScheduling(processCopy, numProcesses, result);
                        break;
                    case "FCAI":
                        result.append("FCAI Scheduling:\n");
                        fcaiScheduling(processCopy, numProcesses, result);
                        break;
                }

                outputArea.setText(result.toString());
                timelinePanel.revalidate();
                timelinePanel.repaint();
            } catch (NumberFormatException ex) {
                outputArea.setText("Invalid number format. Please check your inputs.");
            } catch (Exception ex) {
                outputArea.setText("An error occurred: " + ex.getMessage());
            }
        });

        frame.setVisible(true);
    }

    static Color getColorByName(String colorName) {
        switch (colorName) {
            case "red": return Color.RED;
            case "blue": return Color.BLUE;
            case "green": return Color.GREEN;
            case "yellow": return Color.YELLOW;
            case "orange": return Color.ORANGE;
            case "pink": return Color.PINK;
            case "black": return Color.BLACK;
            case "white": return Color.WHITE;
            case "cyan": return Color.CYAN;
            case "magenta": return Color.MAGENTA;
            default: return null; // Invalid color name
        }
    }

    static void addProcessToTimeline(Process process, int duration) {
        for (int i = 0; i < duration; i++) {
            JPanel bar = new JPanel();
            bar.setBackground(process.color);
            bar.setPreferredSize(new Dimension(10, 50));
            bar.setToolTipText(process.name);
            timelinePanel.add(bar);
        }
    }

    static void sjfScheduling(List<Process> processes, int numProcesses, StringBuilder result) {
        List<Process> readyQueue = new ArrayList<>();
        int currentTime = 0;
        double totalWaitingTime = 0, totalTurnaroundTime = 0;

        while (!processes.isEmpty() || !readyQueue.isEmpty()) {
            for (Iterator<Process> it = processes.iterator(); it.hasNext(); ) {
                Process p = it.next();
                if (p.arrivalTime <= currentTime) {
                    readyQueue.add(p);
                    it.remove();
                }
            }

            for (Process p : readyQueue) {
                p.waitingTime++;
            }

            if (!readyQueue.isEmpty()) {
                readyQueue.sort(Comparator.comparingInt(p -> p.burstTime - p.waitingTime / 10));

                Process current = readyQueue.remove(0);
                int burstTime = current.burstTime;
                addProcessToTimeline(current, burstTime);
                current.waitingTime = currentTime - current.arrivalTime;
                current.turnaroundTime = current.waitingTime + current.burstTime;
                currentTime += burstTime;

                totalWaitingTime += current.waitingTime;
                totalTurnaroundTime += current.turnaroundTime;

                result.append(current.name)
                        .append(" executed. Waiting Time: ")
                        .append(current.waitingTime)
                        .append(", Turnaround Time: ")
                        .append(current.turnaroundTime)
                        .append("\n");
            } else {
                currentTime++;
            }
        }

        result.append("Average Waiting Time: ")
                .append(totalWaitingTime / numProcesses)
                .append("\nAverage Turnaround Time: ")
                .append(totalTurnaroundTime / numProcesses)
                .append("\n");
    }
    static void srtfScheduling(List<Process> processes, int numProcesses, StringBuilder result) {
        int currentTime = 0;
        double totalWaitingTime = 0, totalTurnaroundTime = 0;
        PriorityQueue<Process> readyQueue = new PriorityQueue<>(
                Comparator.comparingInt(p -> p.remainingTime - p.waitingTime / 10)
        );

        while (!processes.isEmpty() || !readyQueue.isEmpty()) {
            for (Iterator<Process> it = processes.iterator(); it.hasNext(); ) {
                Process p = it.next();
                if (p.arrivalTime <= currentTime) {
                    readyQueue.add(p);
                    it.remove();
                }
            }

            for (Process p : readyQueue) {
                p.waitingTime++;
            }

            if (!readyQueue.isEmpty()) {
                Process current = readyQueue.poll();
                current.remainingTime--;
                addProcessToTimeline(current, 1);
                currentTime++;

                if (current.remainingTime == 0) {
                    current.turnaroundTime = currentTime - current.arrivalTime;
                    current.waitingTime = current.turnaroundTime - current.burstTime;

                    totalWaitingTime += current.waitingTime;
                    totalTurnaroundTime += current.turnaroundTime;

                    result.append(current.name)
                            .append(" completed. Waiting Time: ")
                            .append(current.waitingTime)
                            .append(", Turnaround Time: ")
                            .append(current.turnaroundTime)
                            .append("\n");
                } else {
                    readyQueue.add(current);
                }
            } else {
                currentTime++;
            }
        }

        result.append("Average Waiting Time: ")
                .append(totalWaitingTime / numProcesses)
                .append("\nAverage Turnaround Time: ")
                .append(totalTurnaroundTime / numProcesses)
                .append("\n");
    }


    // FCAI Scheduling
    static void fcaiScheduling(List<Process> processes, int numProcesses, StringBuilder result) {
        int currentTime = 0;
        double totalWaitingTime = 0, totalTurnaroundTime = 0;

        double v1 = processes.stream().mapToInt(p -> p.arrivalTime).max().orElse(1) / 10.0;
        double v2 = processes.stream().mapToInt(p -> p.burstTime).max().orElse(1) / 10.0;

        PriorityQueue<Process> readyQueue = new PriorityQueue<>(Comparator.comparingDouble(p -> p.fcaiFactor));

        // Create a list to store table rows
        List<Object[]> tableData = new ArrayList<>();

        while (!processes.isEmpty() || !readyQueue.isEmpty()) {
            for (Iterator<Process> it = processes.iterator(); it.hasNext(); ) {
                Process p = it.next();
                if (p.arrivalTime <= currentTime) {
                    updateFcaiFactor(p, v1, v2);
                    readyQueue.add(p);
                    it.remove();
                }
            }

            if (!readyQueue.isEmpty()) {
                Process current = readyQueue.poll();

                int allowedNonPreemptiveTime = (int) Math.ceil(current.quantum * 0.4);
                int executedTime = Math.min(current.remainingTime, allowedNonPreemptiveTime);
                String actionDetails;
                int oldQuantum = current.quantum;

                // Check if the process has executed 40% of its quantum
                if (executedTime >= allowedNonPreemptiveTime) {
                    // Preempt the process and re-add it to the queue
                    readyQueue.add(current);
                    actionDetails = String.format("Preempted. Quantum: %d → %d", oldQuantum, current.quantum);
                } else {
                    // Continue executing the next process
                    actionDetails = String.format("Continued. Quantum: %d → %d", oldQuantum, current.quantum);
                }
                int startTime = currentTime;
                currentTime += executedTime;

                addProcessToTimeline(current, executedTime);
                current.remainingTime -= executedTime;

                if (current.remainingTime == 0) {
                    current.turnaroundTime = currentTime - current.arrivalTime;
                    current.waitingTime = current.turnaroundTime - current.burstTime;
                    totalWaitingTime += current.waitingTime;
                    totalTurnaroundTime += current.turnaroundTime;
                    actionDetails = "Completed";
                } else {
                    updateQuantum(current);
                    updateFcaiFactor(current, v1, v2);
                    readyQueue.add(current);
                    actionDetails = String.format("Preempted. Quantum: %d → %d", oldQuantum, current.quantum);
                }

                // Add row data to the table
                tableData.add(new Object[]{
                        startTime + "-" + currentTime,
                        current.name,
                        executedTime,
                        current.remainingTime,
                        current.quantum,
                        current.priority,
                        current.fcaiFactor,
                        actionDetails
                });
            } else {
                currentTime++;
            }
        }

        // Display the table with scheduling results
        displayFcaiTable(tableData);

        result.append("\nAverage Waiting Time: ").append(totalWaitingTime / numProcesses)
                .append("\nAverage Turnaround Time: ").append(totalTurnaroundTime / numProcesses)
                .append("\n");
    }

    // Method to display the FCAI results in a JTable
    static void displayFcaiTable(List<Object[]> tableData) {
        String[] columnNames = {
                "Time", "Process", "Exec Time", "Remaining", "Updated Quantum", "Priority", "FCAI Factor", "Action Details"
        };

        Object[][] data = tableData.toArray(new Object[0][]);
        JTable table = new JTable(data, columnNames);

        table.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(table);

        JFrame tableFrame = new JFrame("FCAI Scheduling Results");
        tableFrame.setSize(800, 400);
        tableFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        tableFrame.add(scrollPane);
        tableFrame.setVisible(true);
    }

    // Update the FCAI Factor for a process
    static void updateFcaiFactor(Process process, double v1, double v2) {
        process.fcaiFactor = (10 - process.priority) +
                (process.arrivalTime / v1) +
                (process.remainingTime / v2);
    }

    // Update the quantum for a process
    static void updateQuantum(Process process) {
        if (process.remainingTime > 0) {
            process.quantum += 2;
        } else {
            process.quantum += process.remainingTime; // Unused quantum case
        }
    }

    // priority Scheduling
    static void priorityScheduling(List<Process> processes, int numProcesses, StringBuilder result) {
        List<Process> readyQueue = new ArrayList<>();
        int currentTime = 0;
        double totalWaitingTime = 0, totalTurnaroundTime = 0;

        while (!processes.isEmpty() || !readyQueue.isEmpty()) {
            for (Iterator<Process> it = processes.iterator(); it.hasNext(); ) {
                Process p = it.next();
                if (p.arrivalTime <= currentTime) {
                    readyQueue.add(p);
                    it.remove();
                }
            }

            if (!readyQueue.isEmpty()) {
                readyQueue.sort(Comparator.comparingInt(p -> p.priority));
                Process current = readyQueue.remove(0);

                int burstTime = current.burstTime;
                addProcessToTimeline(current, burstTime);
                current.waitingTime = currentTime - current.arrivalTime;
                current.turnaroundTime = current.waitingTime + current.burstTime;
                currentTime += burstTime;

                totalWaitingTime += current.waitingTime;
                totalTurnaroundTime += current.turnaroundTime;

                result.append(current.name)
                        .append(" executed. Waiting Time: ")
                        .append(current.waitingTime)
                        .append(", Turnaround Time: ")
                        .append(current.turnaroundTime)
                        .append("\n");
            } else {
                currentTime++;
            }
        }

        result.append("Average Waiting Time: ")
                .append(totalWaitingTime / numProcesses)
                .append("\nAverage Turnaround Time: ")
                .append(totalTurnaroundTime / numProcesses)
                .append("\n");
    }


    // Deep copy of the process list to prevent modification during scheduling
    static List<Process> deepCopy(List<Process> original) {
        List<Process> copy = new ArrayList<>();
        for (Process p : original) {
            copy.add(new Process(p.name, p.burstTime, p.arrivalTime, p.priority, p.quantum, p.color));
        }
        return copy;
    }
}