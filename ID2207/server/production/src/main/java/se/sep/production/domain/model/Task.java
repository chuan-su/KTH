package se.sep.production.domain.model;

import se.sep.common.domain.model.AbstractEntity;
import se.sep.common.domain.model.SepId;
import se.sep.security.domain.model.Team;


public class Task extends AbstractEntity {

  private String title;
  private String description;
  private SepId assignedUserId;
  private Team  assignedTeam;
  private Priority priority;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public SepId getAssignedUserId() {
    return assignedUserId;
  }

  public void setAssignedUserId(SepId assignedUserId) {
    this.assignedUserId = assignedUserId;
  }

  public Team getAssignedTeam() {
    return assignedTeam;
  }

  public void setAssignedTeam(Team assignedTeam) {
    this.assignedTeam = assignedTeam;
  }

  public Priority getPriority() {
    return priority;
  }

  public void setPriority(Priority priority) {
    this.priority = priority;
  }
}
