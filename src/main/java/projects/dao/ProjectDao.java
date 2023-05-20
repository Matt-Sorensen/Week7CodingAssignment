package projects.dao;

import java.sql.SQLException;
import java.math.BigDecimal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import projects.entity.Category;
import projects.entity.Material;
import projects.entity.Project;
import projects.entity.Step;
import projects.exception.DbException;
import provided.util.DaoBase;

/**
 * This class uses JDBC to perform CRUD operations on the project tables.
 *
 */
@SuppressWarnings("unused")
public class ProjectDao extends DaoBase {
	// Define the table names for the database operations
	private static final String CATEGORY_TABLE = "category";
	private static final String MATERIAL_TABLE = "material";
	private static final String PROJECT_TABLE = "project";
	private static final String PROJECT_CATEGORY_TABLE = "project_category";
	private static final String STEP_TABLE = "step";

	/**
	 * Insert a project row into the project table.
	 * 
	 * @param project The project object to insert.
	 * @return The Project object with the primary key.
	 * @throws DbException Thrown if an error occurs inserting the row.
	 */
	public Project insertProject(Project project) {
		// @formatter:off
		String sql = ""
				+ "INSERT INTO " + PROJECT_TABLE + " "
				+ "(project_name, estimated_hours, actual_hours, difficulty, notes) "
				+ "VALUES "
				+ "(?, ?, ?, ?, ?)";
		// @formatter:on

		try (Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);

			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				setParameter(stmt, 1, project.getProjectName(), String.class);
				setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
				setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
				setParameter(stmt, 4, project.getDifficulty(), Integer.class);
				setParameter(stmt, 5, project.getNotes(), String.class);

				stmt.executeUpdate();

				Integer projectID = getLastInsertId(conn, PROJECT_TABLE);

				commitTransaction(conn);
				project.setProjectId(projectID);
				return project;
			} catch (Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		} catch (SQLException e) {
			throw new DbException(e);
		}
	}

	// This method fetches all projects from the project table. It starts a
	// transaction, prepares the SQL statement, executes the query, and commits the
	// transaction. In case of any exception, the transaction is rolled back.
	public List<Project> fetchAllProjects() {
		String sql = "SELECT * FROM " + PROJECT_TABLE + " ORDER BY project_name";

		try (Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);

			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				try (ResultSet rs = stmt.executeQuery()) {
					List<Project> projects = new LinkedList<>();

					while (rs.next()) {
						projects.add(extract(rs, Project.class));
					}

					return projects;
				}
			} catch (Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		} catch (SQLException e) {
			throw new DbException(e);
		}
	}

	// This method fetches a project by its ID from the project table. It also
	// fetches related materials, steps, and categories for the project. It starts a
	// transaction, prepares the SQL statement, executes the query, and commits the
	// transaction. In case of any exception, the transaction is rolled back.
	public Optional<Project> fetchProjectById(Integer projectId) {
		String sql = "SELECT * FROM " + PROJECT_TABLE + " WHERE project_id = ?";

		try (Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);

			try {
				Project project = null;

				try (PreparedStatement stmt = conn.prepareStatement(sql)) {
					setParameter(stmt, 1, projectId, Integer.class);

					try (ResultSet rs = stmt.executeQuery()) {
						if (rs.next()) {
							project = extract(rs, Project.class);
						}
					}
				}
				if (Objects.nonNull(project)) {
					project.getMaterials().addAll(fetchMaterialsForProject(conn, projectId));
					project.getSteps().addAll(fetchStepsForProject(conn, projectId));
					project.getCategories().addAll(fetchCategoriesForProject(conn, projectId));
				}

				commitTransaction(conn);

				return Optional.ofNullable(project);
			} catch (Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		} catch (SQLException e) {
			throw new DbException(e);
		}
	}

	// This method fetches categories related to a project by the project ID. It
	// prepares the SQL statement, executes the query, and extracts the categories
	// from the result set.
	private List<Category> fetchCategoriesForProject(Connection conn, Integer projectId) throws SQLException {
		//* @formatter:off
		String sql = ""
				+ "SELECT c.* FROM " + CATEGORY_TABLE + " c "
				+ "JOIN " + PROJECT_CATEGORY_TABLE + " pc USING (category_id) "
				+ "WHERE project_id = ?";
		//* @formatter:on

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParameter(stmt, 1, projectId, Integer.class);

			try (ResultSet rs = stmt.executeQuery()) {
				List<Category> categories = new LinkedList<>();

				while (rs.next()) {
					categories.add(extract(rs, Category.class));
				}

				return categories;
			}
		}
	}

	// This method fetches steps related to a project by the project ID. It prepares
	// the SQL statement, executes the query, and extracts the steps from the result
	// set.
	private List<Step> fetchStepsForProject(Connection conn, Integer projectId) throws SQLException {
		String sql = "SELECT * FROM " + STEP_TABLE + " WHERE project_id = ?";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParameter(stmt, 1, projectId, Integer.class);

			try (ResultSet rs = stmt.executeQuery()) {
				List<Step> steps = new LinkedList<>();

				while (rs.next()) {
					steps.add(extract(rs, Step.class));
				}

				return steps;
			}
		}
	}

	// This method fetches materials related to a project by the project ID. It
	// prepares the SQL statement, executes the query, and extracts the materials
	// from the result set.
	private List<Material> fetchMaterialsForProject(Connection conn, Integer projectId) throws SQLException {
		String sql = "SELECT * FROM " + MATERIAL_TABLE + " WHERE project_id = ?";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParameter(stmt, 1, projectId, Integer.class);

			try (ResultSet rs = stmt.executeQuery()) {
				List<Material> materials = new LinkedList<>();

				while (rs.next()) {
					materials.add(extract(rs, Material.class));
				}

				return materials;
			}
		}
	}

	// This method modifies the details of a project in the project table. It starts
	// a transaction, prepares the SQL statement, executes the update, and commits
	// the transaction. In case of any exception, the transaction is rolled back.
	public boolean modifyProjectDetails(Project project) {
		// @formatter:off
		String sql = ""
				+ "UPDATE " + PROJECT_TABLE + " SET "
				+ "project_name = ?, "
				+ "estimated_hours = ?, "
				+ "actual_hours = ?, "
				+ "difficulty = ?, "
				+ "notes = ? "
				+ "WHERE project_id = ?";
		// @formatter:on
		try (Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);

			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				setParameter(stmt, 1, project.getProjectName(), String.class);
				setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
				setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
				setParameter(stmt, 4, project.getDifficulty(), Integer.class);
				setParameter(stmt, 5, project.getNotes(), String.class);
				setParameter(stmt, 6, project.getProjectId(), Integer.class);

				boolean modified = stmt.executeUpdate() == 1;
				commitTransaction(conn);

				return modified;

			} catch (Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		} catch (SQLException e) {
			throw new DbException(e);
		}
	}

	// This method deletes a project from the project table by its ID. It starts a
	// transaction, prepares the SQL statement, executes the update, and commits the
	// transaction. In case of any exception, the transaction is rolled back.
	public boolean deleteProject(Integer projectId) {
		String sql = "DELETE FROM " + PROJECT_TABLE + " WHERE project_id = ?";

		try (Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);

			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				setParameter(stmt, 1, projectId, Integer.class);

				boolean deleted = stmt.executeUpdate() == 1;

				commitTransaction(conn);
				return deleted;
			} catch (Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		} catch (SQLException e) {
			throw new DbException(e);
		}
	}
}
