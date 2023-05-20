package projects;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import projects.entity.Project;
import projects.exception.DbException;
import projects.service.ProjectService;

/**
 * This class is a menu driven application that accepts user input from the
 * console. It then performs CRUD operations on the project tables.
 */

public class ProjectsApp {

	// Initialize a scanner for user input, the project service for CRUD operations,
	// and a project object to hold the current project being worked on*/
	private Scanner scanner = new Scanner(System.in);
	private ProjectService projectService = new ProjectService();
	private Project curProject = null;

	// @formatter:off
	// List of operations that the user can select from the menu
	private List<String> operations = List.of(
			"1) Add a project",
			"2) List projects",
			"3) Select a project",
			"4) Update project details",
			"5) Delete a project"
	);
	// @formatter:on

	/**
	 * Entry point for Java application. Initializes the ProjectsApp and starts
	 * processing user selections.
	 * 
	 * @param args Unused.
	 */

	public static void main(String[] args) {
		new ProjectsApp().processUserSelections();
	}

	/**
	 * This method prints the operations, get a user menu selection, and performs
	 * the requested operation. It repeats until the user requests that the
	 * application terminates. It handles exceptions that might be thrown during the
	 * process and notifies the user of errors.
	 */

	private void processUserSelections() {
		boolean done = false;

		while (!done) {
			try {
				int selection = getUserSelection();

				switch (selection) {
				case -1:
					done = exitMenu();
					break;

				case 1:
					createProject();
					break;

				case 2:
					listProjects();
					break;

				case 3:
					selectProject();
					break;

				case 4:
					updateProjectDetails();
					break;

				case 5:
					deleteProject();
					break;

				default:
					System.out.println("\n" + selection + " is not a valid selection. Try again.");
					break;
				}
			} catch (Exception e) {
				System.out.println("\nError: " + e + "Try again.");
			}
		}
	}

	// This method deletes a project based on user's input of project ID. If the
	// current project is the one being deleted, it sets the current project to
	// null.
	private void deleteProject() {
		listProjects();

		Integer projectId = getIntInput("Enter the ID of the project to delete");

		projectService.deleteProject(projectId);
		System.out.println("Project " + projectId + " was deleted successfully.");

		if (Objects.nonNull(curProject) && curProject.getProjectId().equals(projectId)) {
			curProject = null;
		}
	}

	// This method updates the details of the current project. If no project is
	// currently selected, it notifies the user to select a project first.
	private void updateProjectDetails() {
		if (Objects.isNull(curProject)) {
			System.out.println("\nError: You are not working with a project. Please select a project first.");
			return;
		}
		String projectName = getStringInput("Enter the project name [" + curProject.getProjectName() + "]");

		BigDecimal estimatedHours = getDecimalInput(
				"Enter the estimated hours [" + curProject.getEstimatedHours() + "]");

		BigDecimal actualHours = getDecimalInput("Enter the actual hours [" + curProject.getActualHours() + "]");

		Integer difficulty = getIntInput("Enter the project difficulty (1-5) [" + curProject.getDifficulty() + "]");

		String notes = getStringInput("Enter the project notes[" + curProject.getNotes() + "]");

		Project project = new Project();

		project.setProjectId(curProject.getProjectId());
		project.setProjectName(Objects.isNull(projectName) ? curProject.getProjectName() : projectName);

		project.setEstimatedHours(Objects.isNull(estimatedHours) ? curProject.getEstimatedHours() : estimatedHours);
		project.setActualHours(Objects.isNull(actualHours) ? curProject.getActualHours() : actualHours);
		project.setDifficulty(Objects.isNull(difficulty) ? curProject.getDifficulty() : difficulty);
		project.setNotes(Objects.isNull(notes) ? curProject.getNotes() : notes);

		projectService.modifyProjectDetails(project);

		curProject = projectService.fetchProjectById(curProject.getProjectId());
	}

	// This method allows the user to select a project by its ID. If an invalid ID
	// is entered, an exception is thrown.
	private void selectProject() {
		listProjects();
		Integer projectId = getIntInput("Enter a project ID to select a project");

		/* Unselect the current project. */
		curProject = null;

		/* This will throw an exception if an invalid project ID is entered. */
		curProject = projectService.fetchProjectById(projectId);

		if (curProject == null) {
			System.out.println("Invalid project ID selected.");
		}
	}
	
	// This method lists all the projects in the project service.
	private void listProjects() {
		List<Project> projects = projectService.fetchAllProjects();

		System.out.println("\nProjects:");

		projects.forEach(
				project -> System.out.println("   " + project.getProjectId() + ": " + project.getProjectName()));
	}

	/**
	 * Gather user input for a project row then call the project service to create
	 * the row.
	 */

	private void createProject() {
		String projectName = getStringInput("Enter the project name");
		BigDecimal estimatedHours = getDecimalInput("Enter the estimated hours");
		BigDecimal actualHours = getDecimalInput("Enter the actual hours");
		Integer difficulty = getIntInput("Enter the project difficulty (1-5)");
		String notes = getStringInput("Enter the project notes");

		Project project = new Project();

		project.setProjectName(projectName);
		project.setEstimatedHours(estimatedHours);
		project.setActualHours(actualHours);
		project.setDifficulty(difficulty);
		project.setNotes(notes);

		Project dbProject = projectService.addProject(project);
		System.out.println("You have successfully create project: " + dbProject);

	}

	/**
	 * Gets the user's input from the console and converts it to a BigDecimal.
	 * 
	 * @param prompt The prompt to display on the console.
	 * @return A BigDecimal value if successful.
	 * @throws DbException Thrown if an error occurs converting the number to a
	 *                     BigDecimal.
	 */
	private BigDecimal getDecimalInput(String prompt) {
		String input = getStringInput(prompt);

		if (Objects.isNull(input)) {
			return null;
		}

		try {
			return new BigDecimal(input).setScale(2);
		} catch (NumberFormatException e) {
			throw new DbException(input + " is not a valid decimal number. ");
		}
	}

	/**
	 * Called when the user wants to exit the application. It prints a message and
	 * returns {@code true} to terminate the app.
	 * 
	 * @return {@code true}
	 */
	private boolean exitMenu() {
		System.out.println("Exiting the menu.");
		return true;
	}

	/**
	 * This method prints the available menu selections. It then gets the user's
	 * selection from the console and converts it to an int.
	 * 
	 * @return The menu selection as an int or -1 if nothing is selected.
	 */
	private int getUserSelection() {
		printOperations();

		Integer input = getIntInput("Enter a menu selection");

		return Objects.isNull(input) ? -1 : input;
	}

	/**
	 * Prints a prompt on the console and then gets the user's input from the
	 * console. It then converts the input to an Integer.
	 * 
	 * @param prompt The prompt to print.
	 * 
	 * @return If the user enters nothing, {@code null} is returned. Otherwise, the
	 *         input is converted to an Integer.
	 * @throws DbException Thrown if the input is not a valid Integer.
	 */
	private Integer getIntInput(String prompt) {
		String input = getStringInput(prompt);

		if (Objects.isNull(input)) {
			return null;
		}

		try {
			return Integer.valueOf(input);
		} catch (NumberFormatException e) {
			throw new DbException(input + " is not a valid number. ");
		}
	}

	/**
	 * Prints a prompt on the console and then gets the user's input from the
	 * console. If the user enters nothing, {@code null} is returned. Otherwise, the
	 * trimmed input is returned.
	 * 
	 * @param prompt The prompt to print.
	 * @return The user's input or {@code null}.
	 */
	private String getStringInput(String prompt) {
		System.out.print(prompt + ": ");
		String input = scanner.nextLine();

		return input.isBlank() ? null : input.trim();
	}

	/**
	 * Print the menu selections, one per line.
	 */
	private void printOperations() {
		System.out.println("\nThese are the available selections. Press the Enter key to quit:");

		/* With Lambda expression */
		operations.forEach(line -> System.out.println("  " + line));

		if (Objects.isNull(curProject)) {
			System.out.println("\nYou are not working with a project.");
		} else {
			System.out.println("\nYou are working with project: " + curProject);
		}
	}
}
