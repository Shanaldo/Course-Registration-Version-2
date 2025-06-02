// Author: Shanaldo Carty
// Completed Date: Pending, 2025

import java.io.*;
import java.util.*;

public class CourseRegistrationSystem {
    private LinkedList<Student> students; // Linked list to store students
    private LinkedList<Course> courses;   // Linked list to store courses
    private Stack<String> registrationHistory; // Stack to track last actions for undo
    private LinkedList<User> users;       // List of User objects

    // File paths for data storage
    private final String studentFilePath = "students.dat";
    private final String courseFilePath = "courses.dat";
    private final String userFilePath = "users.dat";
    private final String registrationHistoryFilePath = "registrationHistory.dat";
    private final String gradesFilePath = "grades.csv"; // File path for grades

    public CourseRegistrationSystem() {
        this.students = new LinkedList<>();
        this.courses = new LinkedList<>();
        this.users = new LinkedList<>();
        this.registrationHistory = new Stack<>();

        loadUsersFromFile();       // Load users on startup
        loadStudentsFromFile();    // Load students on startup
        loadCoursesFromFile();     // Load courses on startup
        loadRegistrationHistory(); // Load registration history on startup

        if (users.isEmpty()) {
            createDefaultUsers();
            saveUsersToFile();     // Save them so they persist
        }
    }

    public void saveRegistrationHistory() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(registrationHistoryFilePath))) {
            oos.writeObject(registrationHistory);
            System.out.println("Registration history saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Add a new student
    public void addStudent(Student student) {
        students.add(student);
        addUser(student.getName(), student.getId(), "student"); // Name as username, ID as password
        saveStudentsToFile();
        System.out.println("Student added successfully with name as username and ID as password.");
    }

    // Remove a student by ID
    public void removeStudent(String studentId) {
        Student student = getStudentById(studentId);
        if (student == null) {
            System.out.println("No student found with ID: " + studentId);
            return;
        }

        // Remove student from all courses they're involved in
        for (Course course : courses) {
            if (course.getEnrolledStudents().contains(student)) {
                course.removeStudent(student);
                System.out.println("Removed student " + student.getName() + " from enrolled list in course " + course.getCourseName());
            }
            if (course.getWaitlist().contains(student)) {
                course.removeFromWaitlist(student);
                System.out.println("Removed student " + student.getName() + " from waitlist in course " + course.getCourseName());
            }
            if (course.getPriorityQueue().contains(student)) {
                course.removeFromPriorityQueue(student);
                System.out.println("Removed student " + student.getName() + " from priority queue in course " + course.getCourseName());
            }
        }

        students.remove(student);
        saveStudentsToFile();
        saveCoursesToFile(); // Persist course changes
        System.out.println("Student with ID " + studentId + " has been removed successfully.");
    }

    // Modify a student's information by ID
    public void modifyStudent(String studentId, String newName) {
        Student student = getStudentById(studentId);
        if (student == null) {
            System.out.println("No student found with ID: " + studentId);
            return;
        }
        student.setName(newName);
        saveStudentsToFile();
        System.out.println("Student with ID " + studentId + " has been updated successfully.");
    }

    // Helper to check if a prerequisite course exists
    private boolean prerequisiteExists(String prerequisiteId) {
        for (Course course : courses) {
            if (course.getCourseId().equals(prerequisiteId)) {
                return true;
            }
        }
        return false;
    }

    // Add a new course
    public void addCourse(Course course) {
        LinkedList<String> prerequisites = course.getPrerequisites();
        LinkedList<String> nonExistentPrereqs = new LinkedList<>();

        if (prerequisites != null && !prerequisites.isEmpty()) {
            for (String prerequisiteId : prerequisites) {
                if (!prerequisiteExists(prerequisiteId)) {
                    nonExistentPrereqs.add(prerequisiteId);
                }
            }
            if (!nonExistentPrereqs.isEmpty()) {
                System.out.println("\nWARNING: The following prerequisites have not been created yet:");
                for (String prereq : nonExistentPrereqs) {
                    System.out.println("- " + prereq);
                }
                System.out.print("\nWould you like to continue adding this course anyway? (yes/no): ");
                Scanner scanner = new Scanner(System.in);
                String response = scanner.nextLine().trim().toLowerCase();
                if (!response.equals("yes")) {
                    System.out.println("Course addition cancelled. Please create the prerequisites first.");
                    return;
                }
                System.out.println("Course added with non-existent prerequisites. Please ensure to create them later.");
            }
        }

        courses.add(course);
        saveCoursesToFile();
        System.out.println("Course " + course.getCourseId() + " added successfully.");
    }

    // Remove a course by ID
    public void removeCourse(String courseId) {
        Course course = findCourseById(courseId);
        if (course == null) {
            System.out.println("Course with ID " + courseId + " does not exist. Cannot remove.");
            return;
        }
        
        public boolean hasAnyStudent() {
            return !students.isEmpty();
        }
        
        public boolean hasAnyCourse() {
            return !courses.isEmpty();
        }

        if (!course.getEnrolledStudents().isEmpty()
                || !course.getWaitlist().isEmpty()
                || !course.getPriorityQueue().isEmpty()) {
            System.out.println("Cannot remove course " + courseId + " as it has students enrolled or waiting for enrollment.");
            System.out.println("Please wait until the course is completely empty before removing.");
            return;
        }

        List<Course> dependentCourses = new ArrayList<>();
        for (Course otherCourse : courses) {
            if (otherCourse.getPrerequisites() != null
                    && otherCourse.getPrerequisites().contains(courseId)) {
                dependentCourses.add(otherCourse);
            }
        }
        if (!dependentCourses.isEmpty()) {
            System.out.println("\nWARNING: Course " + courseId + " is a prerequisite for the following courses:");
            for (Course dependentCourse : dependentCourses) {
                System.out.println("- " + dependentCourse.getCourseId() + ": " + dependentCourse.getCourseName());
            }
            System.out.println("\nAre you sure you want to remove this course? (yes/no)");
            Scanner scanner = new Scanner(System.in);
            String response = scanner.nextLine().trim().toLowerCase();
            if (!response.equals("yes")) {
                System.out.println("Course removal cancelled.");
                return;
            }
        }

        courses.remove(course);
        saveCoursesToFile();
        System.out.println("Course " + courseId + " removed successfully.");
    }

    // Modify a course by ID
    public void modifyCourse(String courseId, String newName, int newCredits, int newCapacity, LinkedList<String> newPrerequisites) {
        Course course = findCourseById(courseId);
        if (course == null) {
            System.out.println("Course with ID " + courseId + " does not exist. Cannot modify.");
            return;
        }

        course.setCourseName(newName);
        course.setCredits(newCredits);
        course.setCapacity(newCapacity);
        course.setPrerequisites(newPrerequisites);
        saveCoursesToFile();
        System.out.println("Course details updated successfully.");
    }

    // View details of a specific course by ID
    public void viewCourseDetails(String courseId) {
        Course course = findCourseById(courseId);
        if (course != null) {
            System.out.println("\nCourse ID: " + course.getCourseId());
            System.out.println("Course Name: " + course.getCourseName());
            System.out.println("Credits: " + course.getCredits());
            System.out.println("Capacity: " + course.getCapacity());
            System.out.println("Prerequisites: " + course.getPrerequisites());

            System.out.println("Enrolled Students:");
            if (course.getEnrolledStudents().isEmpty()) {
                System.out.println("  None");
            } else {
                for (Student student : course.getEnrolledStudents()) {
                    System.out.println("  - ID: " + student.getId() + ", Name: " + student.getName()
                            + ", Enrolled Courses: " + student.getEnrolledCourses());
                }
            }

            System.out.println("Waitlist:");
            if (course.getWaitlist().isEmpty()) {
                System.out.println("  None");
            } else {
                for (Student student : course.getWaitlist()) {
                    System.out.println("  - ID: " + student.getId() + ", Name: " + student.getName()
                            + ", Enrolled Courses: " + student.getEnrolledCourses());
                }
            }
        } else {
            System.out.println("Course with ID " + courseId + " not found.");
        }
    }

    // Student self-enrollment in a course
    public void enrollSelfInCourse(String studentId, String courseId) {
        Student student = getStudentById(studentId);
        if (student == null) {
            System.out.println("Student with ID " + studentId + " not found.");
            return;
        }

        Course course = findCourseById(courseId);
        if (course == null) {
            System.out.println("Course with ID " + courseId + " not found.");
            return;
        }

        if (student.getCompletedCourses().contains(courseId)) {
            System.out.print("You have already completed this course with a passing grade. Are you sure you want to retake it? (yes/no): ");
            Scanner scanner = new Scanner(System.in);
            String response = scanner.nextLine().trim().toLowerCase();
            if (!response.equals("yes")) {
                System.out.println("Enrollment canceled. You have already passed this course.");
                return;
            }
        }

        if (course.getEnrolledStudents().contains(student)) {
            System.out.println("You are already enrolled in " + course.getCourseName() + ".");
            return;
        }
        if (course.getWaitlist().contains(student)) {
            System.out.println("You are already on the waitlist for " + course.getCourseName() + ".");
            return;
        }
        if (course.getPriorityQueue().contains(student)) {
            System.out.println("You are already in the priority queue for " + course.getCourseName() + ".");
            return;
        }

        if (!student.hasCompletedCourses(new LinkedList<>(course.getPrerequisites()))) {
            System.out.println("You do not meet the prerequisites for " + course.getCourseName() + ".");
            return;
        }

        boolean isPriority = needsCourseForPrerequisite(student, courseId);
        student.setPriority(isPriority);

        boolean enrolled = course.enrollStudent(student, isPriority);
        if (enrolled) {
            student.addCourse(courseId);
            registrationHistory.push(studentId + "-" + courseId);
            saveRegistrationHistory();
            System.out.println(student.getName() + " has been enrolled in " + course.getCourseName());
        } else {
            System.out.println(student.getName() + " has been added to the " +
                    (isPriority ? "priority queue" : "waitlist") + " for " + course.getCourseName());
        }
        student.addCourseToHistory(courseId);
        saveCoursesToFile();
        saveStudentsToFile();
    }

    // Retrieve student ID by username
    public String getStudentIdByUsername(String username) {
        for (Student s : students) {
            if (s.getName().equals(username)) {
                return s.getId();
            }
        }
        System.out.println("No student ID found for the provided username.");
        return null;
    }

    // Update a user’s password (e.g., when the student sets a new one on first login)
    public void updateUserPassword(String username, String newPassword) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                user.setPassword(newPassword);
                saveUsersToFile();
                System.out.println("Password for user \"" + username + "\" has been updated.");
                return;
            }
        }
        System.out.println("User \"" + username + "\" not found. Cannot update password.");
    }

    // View enrolled courses for a student
    public void viewEnrolledCourses(String studentId) {
        Student student = getStudentById(studentId);
        if (student != null) {
            System.out.println("Enrolled Courses for " + student.getName() + ":");
            if (student.getEnrolledCourses().isEmpty()) {
                System.out.println("No enrolled courses.");
            } else {
                for (String cid : student.getEnrolledCourses()) {
                    Course course = findCourseById(cid);
                    if (course != null) {
                        System.out.println(" - " + course.getCourseName() + " (" + course.getCourseId() + ")");
                    } else {
                        System.out.println(" - Course ID " + cid + " (details not available)");
                    }
                }
            }
        } else {
            System.out.println("Student with ID " + studentId + " not found.");
        }
    }

    // View completed courses for a student
    public void viewCompletedCourses(String studentId) {
        Student student = getStudentById(studentId);
        if (student != null) {
            System.out.println("Completed Courses for " + student.getName() + ":");
            if (student.getCompletedCourses().isEmpty()) {
                System.out.println("No completed courses.");
            } else {
                for (String cid : student.getCompletedCourses()) {
                    Course course = findCourseById(cid);
                    if (course != null) {
                        System.out.println(" - " + course.getCourseName() + " (" + course.getCourseId() + ")");
                    } else {
                        System.out.println(" - Course ID " + cid + " (details not available)");
                    }
                }
            }
        } else {
            System.out.println("Student with ID " + studentId + " not found.");
        }
    }

    // View personal details for a student
    public void viewPersonalDetails(String studentId) {
        Student student = getStudentById(studentId);
        if (student != null) {
            System.out.println("Personal Details for " + student.getName() + ":");
            System.out.println(" - ID: " + student.getId());
            System.out.println(" - Name: " + student.getName());
            System.out.println(" - Enrolled Courses: " + student.getEnrolledCourses().size());
            System.out.println(" - Completed Courses: " + student.getCompletedCourses().size());
            System.out.println(" - Failed Courses: " + student.getFailedCourses().size());
        } else {
            System.out.println("Student with ID " + studentId + " not found.");
        }
    }

    // View waitlist positions for a student
    public void viewStudentWaitlistPositions(String studentId) {
        Student student = getStudentById(studentId);
        if (student == null) {
            System.out.println("Student not found.");
            return;
        }

        boolean foundAny = false;
        System.out.println("\nWaitlist Positions:");

        for (Course course : courses) {
            Queue<Student> waitlist = course.getWaitlist();
            int position = 1;

            for (Student w : waitlist) {
                if (w.getId().equals(studentId)) {
                    System.out.println("Course: " + course.getCourseName() + " (" + course.getCourseId() + ")");
                    System.out.println("Position on waitlist: " + position);
                    System.out.println("Total students on waitlist: " + waitlist.size());
                    System.out.println("-----------------------------");
                    foundAny = true;
                    break;
                }
                position++;
            }
        }

        if (!foundAny) {
            System.out.println("You are not on any waitlists.");
        }
    }

    // View priority-queue positions for a student
    public void viewStudentPriorityQueuePositions(String studentId) {
        Student student = getStudentById(studentId);
        if (student == null) {
            System.out.println("Student not found.");
            return;
        }

        boolean foundAny = false;
        System.out.println("\nPriority Queue Positions:");

        for (Course course : courses) {
            PriorityQueue<Student> pq = course.getPriorityQueue();
            Student[] queueArray = pq.toArray(new Student[0]);

            for (int i = 0; i < queueArray.length; i++) {
                if (queueArray[i].getId().equals(studentId)) {
                    System.out.println("Course: " + course.getCourseName() + " (" + course.getCourseId() + ")");
                    System.out.println("Position in priority queue: " + (i + 1));
                    System.out.println("Total students in priority queue: " + pq.size());
                    System.out.println("-----------------------------");
                    foundAny = true;
                }
            }
        }

        if (!foundAny) {
            System.out.println("You are not in any priority queues.");
        }
    }

    // Remove a student from a course (admin or student)
    public void removeCourseFromStudent(String studentId, String courseId, boolean isAdmin) {
        Student student = getStudentById(studentId);
        Course course = findCourseById(courseId);

        if (student == null) {
            System.out.println("Student with ID " + studentId + " not found.");
            return;
        }
        if (course == null) {
            System.out.println("Course with ID " + courseId + " not found.");
            return;
        }
        if (!course.getEnrolledStudents().contains(student)) {
            System.out.println("Student is not enrolled in this course.");
            return;
        }

        if (course.removeEnrolledStudent(student, isAdmin)) {
            student.removeCourse(courseId);
            if (isAdmin) {
                System.out.println("Successfully removed " + student.getName() + " from " + course.getCourseName());
            } else {
                System.out.println("Successfully removed from " + course.getCourseName());
            }
            if (!course.getEnrolledStudents().isEmpty()) {
                Student nextStudent = course.getEnrolledStudents().getLast();
                if (!nextStudent.getEnrolledCourses().contains(courseId)) {
                    nextStudent.addCourse(courseId);
                    System.out.println(nextStudent.getName() + " has been enrolled in " + course.getCourseName());
                }
            }
            saveCoursesToFile();
            saveStudentsToFile();
        } else {
            System.out.println("Failed to remove student from course.");
        }
    }

    // Enroll a student in a course (admin action)
    public void enrollStudentInCourse(String studentId, String courseId) {
        Student student = getStudentById(studentId);
        Course course = findCourseById(courseId);
        Scanner scanner = new Scanner(System.in);

        if (student == null) {
            System.out.println("Student with ID " + studentId + " not found.");
            return;
        }
        if (course == null) {
            System.out.println("Course with ID " + courseId + " not found.");
            return;
        }

        if (student.getCompletedCourses().contains(courseId)) {
            System.out.print("The student has already completed this course with a passing grade. Are you sure you want to re-enroll the student? (yes/no): ");
            String response = scanner.nextLine().trim().toLowerCase();
            if (!response.equals("yes")) {
                System.out.println("Enrollment canceled. The student has already passed this course.");
                return;
            }
        }

        if (course.getEnrolledStudents().contains(student)) {
            System.out.println("The student is already enrolled in " + course.getCourseName() + ".");
            return;
        }
        if (course.getWaitlist().contains(student)) {
            System.out.println("The student is already on the waitlist for " + course.getCourseName() + ".");
            return;
        }
        if (course.getPriorityQueue().contains(student)) {
            System.out.println("The student is already in the priority queue for " + course.getCourseName() + ".");
            return;
        }

        if (!student.hasCompletedCourses(new LinkedList<>(course.getPrerequisites()))) {
            System.out.println(student.getName() + " does not meet the prerequisites for " + course.getCourseName() + ".");
            return;
        }

        boolean isPriority = needsCourseForPrerequisite(student, courseId);
        student.setPriority(isPriority);

        boolean enrolled = course.enrollStudent(student, isPriority);
        if (enrolled) {
            student.addCourse(courseId);
            registrationHistory.push(studentId + "-" + courseId);
            saveRegistrationHistory();
            System.out.println(student.getName() + " has been enrolled in " + course.getCourseName());
        } else {
            System.out.println(student.getName() + " has been added to the " +
                    (isPriority ? "priority queue" : "waitlist") + " for " + course.getCourseName());
        }

        saveCoursesToFile();
        saveStudentsToFile();
    }

    // Undo the last registration action for a specific student
    public void undoLastRegistrationForStudent(String studentId) {
        Student student = getStudentById(studentId);
        if (student == null) {
            System.out.println("Student not found.");
            return;
        }

        String courseId = student.undoLastCourseRegistration();
        if (courseId == null) {
            System.out.println("No recent registration to undo for the student with ID " + studentId + ".");
            return;
        }

        Course course = findCourseById(courseId);
        if (course != null) {
            if (course.getEnrolledStudents().contains(student)) {
                course.removeStudent(student);
                student.removeCourse(courseId);
            }
            if (course.getWaitlist().contains(student)) {
                course.removeFromWaitlist(student);
            }
            if (course.getPriorityQueue().contains(student)) {
                course.removeFromPriorityQueue(student);
            }

            System.out.println("Undid the registration for " + student.getName() + " in course " + course.getCourseName());

            if (!course.getPriorityQueue().isEmpty()) {
                Student nextInPriority = course.getPriorityQueue().poll();
                course.enrollStudent(nextInPriority, true);
            } else if (!course.getWaitlist().isEmpty()) {
                Student nextInWaitlist = course.getWaitlist().poll();
                course.enrollStudent(nextInWaitlist, false);
            }

            saveCoursesToFile();
            saveStudentsToFile();
        } else {
            System.out.println("Course not found.");
        }
    }

    // Undo the last registration action (admin-level)
    public void undoLastRegistration() {
        if (registrationHistory.isEmpty()) {
            System.out.println("No registration actions to undo.");
            return;
        }

        String lastAction = registrationHistory.pop();
        String[] parts = lastAction.split("-");
        String studentId = parts[0];
        String courseId = parts[1];

        Student student = getStudentById(studentId);
        Course course = findCourseById(courseId);

        if (student != null && course != null) {
            course.removeStudent(student);
            student.removeCourse(courseId);
            System.out.println("Undid the registration of " + student.getName() + " from " + course.getCourseName());
            saveRegistrationHistory();
            saveCoursesToFile();
            saveStudentsToFile();
        }
    }

    // Handle course completion after grade entry
    public void handleCourseCompletion(String studentId, String courseId, String grade) {
        Student student = getStudentById(studentId);
        if (student != null) {
            if (isPassingLetter(grade)) {
                System.out.println("Course " + courseId + " marked as completed for student " + studentId);
            }
            notifyCourseCompletion(courseId);
        }
    }

    public void notifyCourseCompletion(String courseId) {
        checkAndEnrollNextStudentInCourse(courseId);
    }

    // Check and enroll next student when a spot opens
    public void checkAndEnrollNextStudentInCourse(String courseId) {
        Course course = findCourseById(courseId);
        if (course == null) {
            System.out.println("Course with ID " + courseId + " not found.");
            return;
        }

        if (!course.getPriorityQueue().isEmpty()) {
            Student nextInPriority = course.getPriorityQueue().poll();
            course.enrollStudent(nextInPriority, true);
            nextInPriority.addCourse(courseId);
            System.out.println("Enrolled " + nextInPriority.getName() + " from the priority queue for course " + course.getCourseName());
        } else if (!course.getWaitlist().isEmpty()) {
            Student nextInWaitlist = course.getWaitlist().poll();
            course.enrollStudent(nextInWaitlist, false);
            nextInWaitlist.addCourse(courseId);
            System.out.println("Enrolled " + nextInWaitlist.getName() + " from the waitlist for course " + course.getCourseName());
        } else {
            System.out.println("No students in the priority queue or waitlist for course " + course.getCourseName());
        }
        saveCoursesToFile();
        saveStudentsToFile();
    }

    public void loadRegistrationHistory() {
        File file = new File(registrationHistoryFilePath);
        if (!file.exists()) {
            System.out.println("No previous registration history found. Starting fresh.");
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(registrationHistoryFilePath))) {
            registrationHistory = (Stack<String>) ois.readObject();
        } catch (FileNotFoundException e) {
            System.out.println("No registration history found. Starting fresh.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Load users from file
    private void loadUsersFromFile() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(userFilePath))) {
            users = (LinkedList<User>) ois.readObject();
            System.out.println("User data loaded successfully.");
        } catch (FileNotFoundException e) {
            System.out.println("User data file not found, starting with an empty user list.");
            createDefaultUsers();
            saveUsersToFile();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void enterGradesForCourse(String courseId, Scanner scanner) {
        Course course = findCourseById(courseId);
        if (course == null) {
            System.out.println("Course with ID " + courseId + " not found.");
            return;
        }

        // Retrieve the grading components from the Course object
        List<Course.GradingComponent> components = course.getGradingComponents();
        if (components.isEmpty()) {
            System.out.println("No grading components defined for " + courseId + ".");
            return;
        }

        // Copy the enrolled students so we can iterate safely
        LinkedList<Student> enrolledStudents = new LinkedList<>(course.getEnrolledStudents());
        if (enrolledStudents.isEmpty()) {
            System.out.println("No students are currently enrolled in " + course.getCourseName() + ".");
            return;
        }

        // For each enrolled student, prompt for each component’s percentage
        for (Student student : enrolledStudents) {
            System.out.println("\nEntering grades for: " + student.getName() + " (ID: " + student.getId() + ")");
            double weightedTotal = 0.0;

            // Loop through all grading components
            for (Course.GradingComponent gc : components) {
                int compWeight = gc.getWeight();   // e.g. 15
                String compName = gc.getName();    // e.g. "Test 1"
                double earnedPct = -1.0;

                while (true) {
                    System.out.print("  Enter percentage (0–100) for \"" + compName + "\" (weight " + compWeight + "%): ");
                    String input = scanner.nextLine().trim();
                    try {
                        earnedPct = Double.parseDouble(input);
                        if (earnedPct < 0 || earnedPct > 100) {
                            System.out.println("    Please enter a number between 0 and 100.");
                        } else {
                            break;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("    Invalid format. Please enter a numeric value between 0 and 100.");
                    }
                }

                // Convert to weighted portion: (earnedPct / 100) * compWeight
                double portion = (earnedPct / 100.0) * compWeight;
                weightedTotal += portion;
            }

            // Round to two decimal places
            double finalPercentage = Math.round(weightedTotal * 100.0) / 100.0;
            String letterGrade = calculateLetterGrade(finalPercentage);

            // Save to grades.csv: studentId, courseId, letterGrade
            saveGrade(student.getId(), courseId, letterGrade);
            System.out.println("  → Weighted total: " + finalPercentage + "%  → Assigned letter grade: " + letterGrade);

            // Treat anything below "C" as fail
            if (isPassingLetter(letterGrade)) {
                student.completeCourse(courseId);
                course.getEnrolledStudents().remove(student);
                System.out.println("  " + student.getName() + " has completed the course " + courseId);
            } else {
                failCourse(student.getId(), courseId);
            }

            // After recording this student’s grade, attempt to enroll the next student
            checkAndEnrollNextStudentInCourse(courseId);
        }

        System.out.println("\nAll component‐based grades have been entered for course " + course.getCourseName() + ".");
    }

    /**
     * Helper: Converts a final percentage (0–100) into a letter grade:
     *   90–100 → A
     *   80– 89 → A-
     *   75– 79 → B+
     *   70– 74 → B
     *   65– 69 → B-
     *   60– 64 → C+
     *   55– 59 → C
     *   50– 54 → C-
     *   45– 49 → D+
     *   40– 44 → D
     *   35– 39 → D-
     *   30– 34 → E
     *    0– 29 → U
     */
    private String calculateLetterGrade(double pct) {
        if (pct >= 90)   return "A";
        if (pct >= 80)   return "A-";
        if (pct >= 75)   return "B+";
        if (pct >= 70)   return "B";
        if (pct >= 65)   return "B-";
        if (pct >= 60)   return "C+";
        if (pct >= 55)   return "C";
        if (pct >= 50)   return "C-";
        if (pct >= 45)   return "D+";
        if (pct >= 40)   return "D";
        if (pct >= 35)   return "D-";
        if (pct >= 30)   return "E";
        return "U";
    }

    // Helper to decide if letter grade is passing (C or above)
    private boolean isPassingLetter(String letterGrade) {
        switch (letterGrade) {
            case "A":
            case "A-":
            case "B+":
            case "B":
            case "B-":
            case "C+":
            case "C":
                return true;
            default:
                return false;
        }
    }

    // View failed courses for a student
    public void viewFailedCoursesForStudent(String studentId) {
        Student student = getStudentById(studentId);
        if (student != null) {
            System.out.println("Failed Courses for " + student.getName() + ":");
            if (student.getFailedCourses().isEmpty()) {
                System.out.println("No failed courses.");
            } else {
                for (String cid : student.getFailedCourses()) {
                    Course course = findCourseById(cid);
                    if (course != null) {
                        System.out.println(" - " + course.getCourseName() + " (" + course.getCourseId() + ")");
                    } else {
                        System.out.println(" - Course ID " + cid + " (details not available)");
                    }
                }
            }
        } else {
            System.out.println("Student with ID " + studentId + " not found.");
        }
    }

    // Fail a course for a student
    public void failCourse(String studentId, String courseId) {
        Student student = getStudentById(studentId);
        if (student == null) {
            System.out.println("Student with ID " + studentId + " not found.");
            return;
        }

        Course course = findCourseById(courseId);
        if (course == null) {
            System.out.println("Course with ID " + courseId + " not found.");
            return;
        }

        if (course.getEnrolledStudents().contains(student)) {
            course.getEnrolledStudents().remove(student);
            System.out.println(student.getName() + " has been removed from the enrolled list in course " + courseId);
        }

        student.failCourse(courseId);
        System.out.println(student.getName() + " has been added to the failed courses list for course " + courseId);
        saveStudentsToFile();
        saveCoursesToFile();
    }

    // View grades for a specific course
    public void viewGradesForCourse(String courseId) {
        File gradeFile = new File("grades.csv");
        if (!gradeFile.exists()) {
            System.out.println("Error: Grade file not found. Please ensure the grade CSV file has been created.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader("grades.csv"))) {
            String line;
            System.out.println("Grades for course: " + courseId);
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[1].equals(courseId)) {
                    System.out.println(" - Student ID: " + parts[0] + ", Grade: " + parts[2]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // View grades for a specific student
    public void viewGradesForStudent(String studentId) {
        File gradeFile = new File("grades.csv");
        if (!gradeFile.exists()) {
            System.out.println("No grades are available to be displayed.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader("grades.csv"))) {
            String line;
            boolean hasGrades = false;
            System.out.println("Grades for student ID: " + studentId);
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(studentId)) {
                    System.out.println(" - Course ID: " + parts[1] + ", Grade: " + parts[2]);
                    hasGrades = true;
                }
            }
            if (!hasGrades) {
                System.out.println("No grades found for student ID: " + studentId);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Modify a student's grade for a specific course
    public void modifyGradeForStudent(String studentId, String courseId, String newGrade) {
        Student student = getStudentById(studentId);
        if (student == null) {
            System.out.println("Student with ID " + studentId + " not found.");
            return;
        }

        String gradeFilePath = "grades.csv";
        File gradeFile = new File(gradeFilePath);
        if (!gradeFile.exists()) {
            System.out.println("Error: Grade file not found. Please ensure the grade CSV file has been created.");
            return;
        }

        Course course = findCourseById(courseId);
        if (course == null) {
            System.out.println("Course with ID " + courseId + " not found.");
            return;
        }

        boolean isEnrolled = course.getEnrolledStudents().contains(student);
        boolean hasCompleted = student.getCompletedCourses().contains(courseId);
        boolean hasFailed = student.getFailedCourses().contains(courseId);

        if (!isEnrolled && !hasCompleted && !hasFailed) {
            System.out.println("Student with ID " + studentId + " is neither enrolled nor has completed the course " + courseId + ".");
            return;
        }

        if (!isValidGrade(newGrade)) {
            System.out.println("Invalid grade entered. Please enter a valid grade (A+, A, A-, B+, B, B-, C+, C, F).");
            return;
        }

        modifyGradeInCSV(studentId, courseId, newGrade);

        if (!isPassingLetter(newGrade)) {
            student.failCourse(courseId);
            student.getCompletedCourses().remove(courseId);
        } else if (hasCompleted) {
            // Already completed, just update CSV; no changes to lists
        } else {
            student.completeCourse(courseId);
            student.getFailedCourses().remove(courseId);
        }

        saveStudentsToFile();
        saveCoursesToFile();
        System.out.println("Grade updated successfully for student ID " + studentId + " in course " + courseId);
    }

    // Validate grade format
    private boolean isValidGrade(String grade) {
        return grade.matches("A\\+|A|A-|B\\+|B|B-|C\\+|C|F");
    }

    // Save a grade entry to CSV
    public void saveGrade(String studentId, String courseId, String grade) {
        try (FileWriter writer = new FileWriter("grades.csv", true)) { // append mode
            writer.write(studentId + "," + courseId + "," + grade + "\n");
            System.out.println("Grade saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Update an existing grade line in the CSV
    private void modifyGradeInCSV(String studentId, String courseId, String newGrade) {
        File tempFile = new File("temp_grades.csv");
        File originalFile = new File("grades.csv");

        try (BufferedReader reader = new BufferedReader(new FileReader(originalFile));
             FileWriter writer = new FileWriter(tempFile)) {

            String line;
            boolean gradeUpdated = false;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(studentId) && parts[1].equals(courseId)) {
                    writer.write(studentId + "," + courseId + "," + newGrade + "\n");
                    gradeUpdated = true;
                } else {
                    writer.write(line + "\n");
                }
            }
            if (!gradeUpdated) {
                System.out.println("Grade entry not found for the specified student and course.");
            }
            System.out.println("Grade modified successfully in file.");
        } catch (IOException e) {
            System.out.println("Error updating grade file.");
            e.printStackTrace();
        }

        if (!originalFile.delete()) {
            System.out.println("Could not delete original file.");
        } else if (!tempFile.renameTo(originalFile)) {
            System.out.println("Could not rename temporary file.");
        }
    }

    // Save users to file
    private void saveUsersToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(userFilePath))) {
            oos.writeObject(users);
            System.out.println("User data saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createDefaultUsers() {
        users.add(new User("Shanaldo Carty", "Cinnamon Bun", "admin"));
    }

    // Authenticate a user
    public boolean authenticateUser(String username, String password, String role) {
        for (User user : users) {
            if (user.getUsername().equals(username)
                    && user.getPassword().equals(password)
                    && user.getRole().equals(role)) {
                return true;
            }
        }
        return false;
    }

    // Add a new user
    public void addUser(String username, String password, String role) {
        boolean exists = users.stream().anyMatch(user -> user.getUsername().equals(username));
        if (!exists) {
            users.add(new User(username, password, role));
            saveUsersToFile();
            System.out.println("User added successfully with username: " + username + " and ID as password.");
        }
    }

    // Find a course by ID
    public Course findCourseById(String courseId) {
        for (Course course : courses) {
            if (course.getCourseId().equals(courseId)) {
                return course;
            }
        }
        return null;
    }

    // Get a student by ID
    public Student getStudentById(String studentId) {
        for (Student student : students) {
            if (student.getId().equals(studentId)) {
                return student;
            }
        }
        return null;
    }

    // View detailed student information
    public void viewStudentDetails(String studentId) {
        Student student = getStudentById(studentId);
        if (student != null) {
            System.out.println("\nStudent ID: " + student.getId());
            System.out.println("Student Name: " + student.getName());
            System.out.println("Enrolled Courses: " + student.getEnrolledCourses());
            System.out.println("Completed Courses: " + student.getCompletedCourses());
            System.out.println("Failed Courses: " + student.getFailedCourses());
        } else {
            System.out.println("Student with ID " + studentId + " not found.");
        }
    }

    // Determine if a course is a prerequisite for any future course
    private boolean needsCourseForPrerequisite(Student student, String courseId) {
        for (Course course : courses) {
            LinkedList<String> prerequisites = course.getPrerequisites();
            if (prerequisites.contains(courseId) && !student.hasCompletedCourses(prerequisites)) {
                return true;
            }
        }
        return false;
    }

    // Load student data from a file
    private void loadStudentsFromFile() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(studentFilePath))) {
            students = (LinkedList<Student>) ois.readObject();
        } catch (FileNotFoundException e) {
            System.out.println("No previous student data found. Starting fresh.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Save student data to a file
    private void saveStudentsToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(studentFilePath))) {
            oos.writeObject(students);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load course data from a file
    private void loadCoursesFromFile() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(courseFilePath))) {
            courses = (LinkedList<Course>) ois.readObject();
            System.out.println("Course data loaded successfully.");
        } catch (FileNotFoundException e) {
            System.out.println("No previous course data found. Starting fresh.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Save course data to a file
    private void saveCoursesToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(courseFilePath))) {
            oos.writeObject(courses);
            System.out.println("Course data saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Remove a student from the waitlist of a specific course
    public void removeStudentFromWaitlist(String studentId, String courseId) {
        Course course = findCourseById(courseId);
        if (course != null) {
            Student student = getStudentById(studentId);
            if (student != null) {
                if (course.removeFromWaitlist(student)) {
                    course.saveWaitlist();
                    saveCoursesToFile();
                    System.out.println("Student " + student.getName() + " removed from waitlist for course " + course.getCourseName());
                } else {
                    System.out.println("Student is not on the waitlist for this course.");
                }
            } else {
                System.out.println("Student not found.");
            }
        } else {
            System.out.println("Course not found.");
        }
    }

    // Remove a student from the priority queue of a specific course
    public void removeStudentFromPriorityQueue(String studentId, String courseId) {
        Course course = findCourseById(courseId);
        if (course != null) {
            Student student = getStudentById(studentId);
            if (student != null) {
                if (course.removeFromPriorityQueue(student)) {
                    course.savePriorityQueue();
                    saveCoursesToFile();
                    System.out.println("Student " + student.getName() + " removed from priority queue for course " + course.getCourseName());
                } else {
                    System.out.println("Student is not in the priority queue for this course.");
                }
            } else {
                System.out.println("Student not found.");
            }
        } else {
            System.out.println("Course not found.");
        }
    }

    // View the waitlist for a specific course
    public void viewWaitlistByCourseId(String courseId) {
        Course course = findCourseById(courseId);
        if (course != null) {
            System.out.println("Waitlist for course " + course.getCourseName() + " (" + course.getCourseId() + "):");
            if (course.getWaitlist().isEmpty()) {
                System.out.println("No students on the waitlist.");
            } else {
                for (Student student : course.getWaitlist()) {
                    System.out.println(" - ID: " + student.getId() + ", Name: " + student.getName());
                }
            }
        } else {
            System.out.println("Course with ID " + courseId + " not found.");
        }
    }

    // View the priority queue for a specific course
    public void viewPriorityListByCourseId(String courseId) {
        Course course = findCourseById(courseId);
        if (course != null) {
            System.out.println("Priority Queue for course " + course.getCourseName() + " (" + course.getCourseId() + "):");
            if (course.getPriorityQueue().isEmpty()) {
                System.out.println("No students in the priority queue.");
            } else {
                for (Student student : course.getPriorityQueue()) {
                    System.out.println(" - ID: " + student.getId() + ", Name: " + student.getName());
                }
            }
        } else {
            System.out.println("Course with ID " + courseId + " not found.");
        }
    }

    // Display all courses
    public void displayAllCourses() {
        if (courses.isEmpty()) {
            System.out.println("No courses available to display.");
            return;
        }
        for (Course course : courses) {
            System.out.println(course);
        }
    }

    // Display all students
    public void displayAllStudents() {
        if (students.isEmpty()) {
            System.out.println("No students available to display.");
            return;
        }
        for (Student student : students) {
            System.out.println(student);
        }
    }
}
