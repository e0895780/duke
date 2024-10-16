package denny.storage;

import denny.task.Deadline;
import denny.task.Event;
import denny.task.Task;
import denny.task.ToDo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Storage {
    private final String filePath;

    public Storage(String filePath) {
        this.filePath = filePath;
        createDirectoryIfNotExists();
    }

    private void createDirectoryIfNotExists() {
        File directory = new File(filePath).getParentFile();
        if (directory != null && !directory.exists()) {
            directory.mkdirs();
        }
    }

    public List<Task> loadTasks() throws IOException {
        List<Task> tasks = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            return tasks;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    tasks.add(parseTask(line));
                } catch (IllegalArgumentException e) {
                    System.err.println("Error parsing line: " + line + ". " + e.getMessage());
                }
            }
        }
        return tasks;
    }

    public void saveTasks(List<Task> tasks) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Task task : tasks) {
                writer.write(formatTask(task));
                writer.newLine();
            }
        }
    }

    private Task parseTask(String line) {
        String[] parts = line.split("\\|");
        if (parts.length < 3) {
            throw new IllegalArgumentException("Invalid task format");
        }
        String type = parts[0].trim();
        boolean isDone = parts[1].trim().equals("1");
        String description = parts[2].trim();

        Task task;
        switch (type) {
            case "T":
                task = new ToDo(description);
                break;
            case "D":
                String by = parts[3].trim();
                task = new Deadline(description, by);
                break;
            case "E":
                String from = parts[3].trim();
                String to = parts[4].trim();
                task = new Event(description, from, to);
                break;
            default:
                throw new IllegalArgumentException("Unknown task type: " + type);
        }

        if (isDone) {
            task.markAsDone();
        }
        return task;
    }

    private String formatTask(Task task) {
        String type = task instanceof ToDo ? "T" : task instanceof Deadline ? "D" : "E";
        String isDone = task.isDone() ? "1" : "0";
        String formatted = String.format("%s | %s | %s", type, isDone, task.getDescription());
        if (task instanceof Deadline) {
            formatted += " | " + ((Deadline) task).getBy();
        } else if (task instanceof Event) {
            formatted += " | " + ((Event) task).getFrom() + " | " + ((Event) task).getTo();
        }
        return formatted;
    }
}
