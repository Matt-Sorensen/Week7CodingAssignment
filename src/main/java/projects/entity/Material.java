/**
 * 
 */
package projects.entity;

import java.math.BigDecimal;

/**
 * The Material class represents a material entity in a project.
 * 
 * @author Promineo
 *
 */
public class Material {
	private Integer materialId;
	private Integer projectId;
	private String materialName;
	private Integer numRequired;
	private BigDecimal cost;

	// The unique ID of the material.
	public Integer getMaterialId() {
		return materialId;
	}

	// The ID of the project to which this material is related.
	public void setMaterialId(Integer materialId) {
		this.materialId = materialId;
	}

	// The name of the material.
	public Integer getProjectId() {
		return projectId;
	}

	// The number of this material required for the project.
	public void setProjectId(Integer projectId) {
		this.projectId = projectId;
	}

	// The cost of the material.
	public String getMaterialName() {
		return materialName;
	}

	public void setMaterialName(String materialName) {
		this.materialName = materialName;
	}

	public Integer getNumRequired() {
		return numRequired;
	}

	public void setNumRequired(Integer numRequired) {
		this.numRequired = numRequired;
	}

	public BigDecimal getCost() {
		return cost;
	}

	public void setCost(BigDecimal cost) {
		this.cost = cost;
	}

	// Overridden toString() method to provide a string representation of the
	// material, including its ID, name, required quantity, and cost.
	@Override
	public String toString() {
		return "ID=" + materialId + ", materialName=" + materialName + ", numRequired=" + numRequired + ", cost="
				+ cost;
	}
}
