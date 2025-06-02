// Author: Shanaldo Carty
// Completed Date: Pending, 2025

import java.io.Serializable;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.List;
import java.io.FileInputStream;       // For reading files
import java.io.FileOutputStream;      // For writing files
import java.io.ObjectInputStream;     // For deserializing objects
import java.io.ObjectOutputStream;    // For serializing objects
import java.io.FileNotFoundException; // To handle missing files
import java.io.IOException;           // To handle I/O errors
import java.io.PrintWriter;
import java.io.FileWriter;

public class Course implements Serializable {
    private static final long serialVersionUID = 1L;

    private String courseId;
    private String courseName;
    private int credits;
    private int capacity;

    private LinkedList<Student> enrolledStudents;
    private Queue<Student> waitlist;
    private PriorityQueue<Student> priorityQueue; // New attribute for the priority queue
    private LinkedList<String> prerequisites;

    //Holds the dynamic grading components for a course
    private List<GradingComponent> gradingComponents;

    // Inner class for custom Comparator to sort students by ID in the PriorityQueue
    private static class StudentComparator implements Comparator<Student>, Serializable {
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(Student s1, Student s2) {
            return s1.getId().compareTo(s2.getId());
        }
    }

    public Course(String courseId,
                  String courseName,
                  int credits,
                  int capacity,
                  LinkedList<String> prerequisites) {
        // Validate courseId format (3 uppercase letters followed by 4 digits)
        if (!courseId.matches("[A-Z]{3}\\d{4}")) {
            throw new IllegalArgumentException(
                    "Course ID must be exactly 3 uppercase letters followed by 4 digits (e.g., CMP1001)."
            );
        }
        this.courseId = courseId;

        // Set course name and validate that it starts with a capital letter
        setCourseName(courseName);

        // Validate credits are positive or zero
        if (credits < 0) {
            throw new IllegalArgumentException("Credits must be a non-negative number.");
        }
        this.credits = credits;

        // Validate capacity is positive
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be a positive number.");
        }
        this.capacity = capacity;

        // Initialize lists and queues for student management
        this.enrolledStudents = new LinkedList<>();
        this.waitlist = new LinkedList<>();
        this.priorityQueue = new PriorityQueue<>(new StudentComparator());
        this.prerequisites = (prerequisites != null) ? prerequisites : new LinkedList<>();

        // Initialize gradingComponents as an empty list; to be set later
        this.gradingComponents = new LinkedList<>();

        // Load persistent data for waitlist and priority queue
        loadWaitlist();
        loadPriorityQueue();
    }

    // -------------------------- Persistence for Waitlist & PriorityQueue --------------------------//

    @SuppressWarnings("unchecked")
    public void loadPriorityQueue() {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream("PriorityQueue_" + courseId + ".dat")
        )) {
            priorityQueue = (PriorityQueue<Student>) ois.readObject();
            System.out.println("Priority queue loaded for course " + courseId);
        } catch (FileNotFoundException e) {
            System.out.println("No priority queue found for course " + courseId + ". Starting fresh.");
            priorityQueue = new PriorityQueue<>(new StudentComparator());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Save the priority queue to a file for future retrieval.
    public void savePriorityQueue() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream("PriorityQueue_" + courseId + ".dat")
        )) {
            oos.writeObject(priorityQueue);
            System.out.println("Priority queue saved for course " + courseId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public void loadWaitlist() {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream("WaitList_" + courseId + ".dat")
        )) {
            waitlist = (Queue<Student>) ois.readObject();
            System.out.println("Waitlist loaded for course " + courseId);
        } catch (FileNotFoundException e) {
            System.out.println("No waitlist found for course " + courseId + ". Starting fresh.");
            waitlist = new LinkedList<>();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Save the waitlist to a file for future retrieval.
    public void saveWaitlist() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream("WaitList_" + courseId + ".dat")
        )) {
            oos.writeObject(waitlist);
            System.out.println("Waitlist saved for course " + courseId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // -------------------------- Getters and Setters with Validation --------------------------//

    public String getCourseId() {
        return courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        if (courseName == null || !courseName.matches("[A-Z][a-zA-Z\\s]*")) {
            throw new IllegalArgumentException("Course name must start with a capital letter.");
        }
        this.courseName = courseName;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        if (credits < 0) {
            throw new IllegalArgumentException("Credits must be a non-negative number.");
        }
        this.credits = credits;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be a positive number.");
        }
        this.capacity = capacity;
    }

    public LinkedList<String> getPrerequisites() {
        return prerequisites;
    }

    public void setPrerequisites(LinkedList<String> prerequisites) {
        this.prerequisites = (prerequisites != null) ? prerequisites : new LinkedList<>();
    }

    // -------------------------- Enrollment / Waitlist / Priority Methods --------------------------//

    public boolean enrollStudent(Student student, boolean isPriority) {
        // Check if already enrolled
        if (enrolledStudents.contains(student)) {
            System.out.println(student.getName() + " is already enrolled in " + courseName + ".");
            return false;
        }

        // If there is room, enroll directly
        if (enrolledStudents.size() < capacity) {
            enrolledStudents.add(student);
            System.out.println(student.getName() + " has been successfully enrolled in " + courseName + ".");
            return true;
        }

        // Otherwise, add to priority queue or to waitlist
        if (isPriority) {
            priorityQueue.add(student);
            System.out.println(student.getName() + " has been added to the priority queue for " + courseName + ".");
            savePriorityQueue();
        } else {
            addToWaitlist(student);
            saveWaitlist();
        }
        return false; // Not enrolled immediately
    }

    //Check if student is already on the waitlist (by ID).
    private boolean isStudentOnWaitlist(Student student) {
        return waitlist.stream().anyMatch(s -> s.getId().equals(student.getId()));
    }

    //Add a student to the waitlist if not already present.
    private void addToWaitlist(Student student) {
        if (!isStudentOnWaitlist(student)) {
            waitlist.add(student);
            System.out.println(student.getName() + " has been added to the waitlist for " + courseName + ".");
            saveWaitlist();
        }
    }

    public void removeStudent(Student student) {
        if (enrolledStudents.remove(student)) {
            // If priority queue has students, enroll the top priority
            if (!priorityQueue.isEmpty()) {
                Student nextInLine = priorityQueue.poll();
                enrolledStudents.add(nextInLine);
                nextInLine.addCourse(this.courseId);
                System.out.println(nextInLine.getName() + " has been enrolled from the priority queue.");
                savePriorityQueue();
            }
            // Else if waitlist has students, enroll the next
            else if (!waitlist.isEmpty()) {
                Student nextInLine = waitlist.poll();
                enrolledStudents.add(nextInLine);
                nextInLine.addCourse(this.courseId);
                System.out.println(nextInLine.getName() + " has been enrolled from the waitlist.");
                saveWaitlist();
            }
        }
    }

    public boolean removeFromWaitlist(Student student) {
        if (waitlist.remove(student)) {
            System.out.println(student.getName() + " has been removed from the waitlist for " + courseName + ".");
            saveWaitlist();
            return true;
        } else {
            System.out.println(student.getName() + " is not on the waitlist for " + courseName + ".");
            return false;
        }
    }

    public boolean removeFromPriorityQueue(Student student) {
        if (priorityQueue.remove(student)) {
            System.out.println(student.getName() + " has been removed from the priority queue for " + courseName + ".");
            savePriorityQueue();
            return true;
        } else {
            System.out.println(student.getName() + " is not in the priority queue for " + courseName + ".");
            return false;
        }
    }

    public boolean removeEnrolledStudent(Student student, boolean isAdmin) {
        if (enrolledStudents.contains(student)) {
            enrolledStudents.remove(student);

            if (!priorityQueue.isEmpty()) {
                Student nextInLine = priorityQueue.poll();
                enrolledStudents.add(nextInLine);
                nextInLine.addCourse(this.courseId);
                if (isAdmin) {
                    System.out.println(nextInLine.getName() + " has been enrolled from the priority queue.");
                }
                savePriorityQueue();
            } else if (!waitlist.isEmpty()) {
                Student nextInLine = waitlist.poll();
                enrolledStudents.add(nextInLine);
                nextInLine.addCourse(this.courseId);
                if (isAdmin) {
                    System.out.println(nextInLine.getName() + " has been enrolled from the waitlist.");
                }
                saveWaitlist();
            }
            return true;
        }
        return false;
    }

    public LinkedList<Student> getEnrolledStudents() {
        return enrolledStudents;
    }

    public Queue<Student> getWaitlist() {
        return waitlist;
    }

    public PriorityQueue<Student> getPriorityQueue() {
        return priorityQueue;
    }

    // -------------------------- Grading Components Support --------------------------//

    public static class GradingComponent implements Serializable {
        private static final long serialVersionUID = 1L;

        private String name;
        private int weight; // percentage 0–100

        public GradingComponent(String name, int weight) {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Component name cannot be empty.");
            }
            if (weight < 0 || weight > 100) {
                throw new IllegalArgumentException("Component weight must be between 0 and 100.");
            }
            this.name = name.trim();
            this.weight = weight;
        }

        public String getName() {
            return name;
        }

        public int getWeight() {
            return weight;
        }

        @Override
        public String toString() {
            return "GradingComponent{" +
                    "name='" + name + '\'' +
                    ", weight=" + weight +
                    '}';
        }
    }

    public List<GradingComponent> getGradingComponents() {
        return List.copyOf(gradingComponents);
    }

    public void setGradingComponents(List<GradingComponent> components) {
        if (components == null || components.isEmpty()) {
            throw new IllegalArgumentException("You must provide at least one grading component.");
        }
        int total = 0;
        for (GradingComponent gc : components) {
            total += gc.getWeight();
        }
        if (total != 100) {
            throw new IllegalArgumentException(
                    "The sum of all component weights must be exactly 100. Current sum = " + total
            );
        }
        // If validation passed, copy into our internal list
        this.gradingComponents = new LinkedList<>(components);
    }

    public void writeGradingComponentsToCSV() throws IOException {
        if (gradingComponents == null || gradingComponents.isEmpty()) {
            throw new IllegalStateException("No grading components defined for course " + courseId);
        }
        String filename = courseId + "_grading.csv";
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("ComponentName,Weight");
            for (GradingComponent gc : gradingComponents) {
                String compName = gc.getName();
                // If the component name contains commas or quotes, wrap in double quotes and escape quotes
                if (compName.contains(",") || compName.contains("\"")) {
                    compName = "\"" + compName.replace("\"", "\"\"") + "\"";
                }
                writer.println(compName + "," + gc.getWeight());
            }
        }
    }

    // -------------------------- toString Override --------------------------//

    @Override
    public String toString() {
        return "\nCourse ID: " + courseId +
                ", Course Name: " + courseName +
                ", Credits: " + credits +
                ", Capacity: " + capacity +
                ", Prerequisites: " + prerequisites +
                ", Enrolled Students: " + enrolledStudents.size() +
                ", Waitlist: " + waitlist.size() +
                ", Priority Queue: " + priorityQueue.size() +
                ", Grading Components: " + gradingComponents;
    }
}
