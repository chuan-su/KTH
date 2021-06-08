package se.sep.production.domain.model;

import se.sep.common.domain.model.AbstractEntity;
import se.sep.common.domain.model.SepId;

import java.util.List;

public class Project extends AbstractEntity {
  private String name;
  private String description;
  private int budget;
  private SepId createdBy;
  private List<Task> tasks;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getBudget() {
    return budget;
  }

  public void setBudget(int budget) {
    this.budget = budget;
  }

  public SepId getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(SepId createdBy) {
    this.createdBy = createdBy;
  }

  public List<Task> getTasks() {
    return tasks;
  }

  public void setTasks(List<Task> tasks) {
    this.tasks = tasks;
  }
}
