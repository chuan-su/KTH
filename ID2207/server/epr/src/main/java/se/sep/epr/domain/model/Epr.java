package se.sep.epr.domain.model;

import se.sep.common.domain.model.AbstractEntity;

import java.time.LocalDate;
import java.util.List;

public class Epr extends AbstractEntity {
  private String clientName;
  private String eventType;
  private String description;
  private int numberOfAttendees;
  private int plannedBudget;
  private LocalDate eventDateFrom;
  private LocalDate eventDateTo;

  private List<Preference> preferences;

  private Status status;
  private User lastModifiedBy;

  private int suggestedDiscount;
  private int suggestedBudget;

  private List<Comment> comments;

  public String getClientName() {
    return clientName;
  }

  public void setClientName(String clientName) {
    this.clientName = clientName;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getNumberOfAttendees() {
    return numberOfAttendees;
  }

  public void setNumberOfAttendees(int numberOfAttendees) {
    this.numberOfAttendees = numberOfAttendees;
  }

  public int getPlannedBudget() {
    return plannedBudget;
  }

  public void setPlannedBudget(int plannedBudget) {
    this.plannedBudget = plannedBudget;
  }

  public LocalDate getEventDateFrom() {
    return eventDateFrom;
  }

  public void setEventDateFrom(LocalDate eventDateFrom) {
    this.eventDateFrom = eventDateFrom;
  }

  public LocalDate getEventDateTo() {
    return eventDateTo;
  }

  public void setEventDateTo(LocalDate eventDateTo) {
    this.eventDateTo = eventDateTo;
  }

  public List<Preference> getPreferences() {
    return preferences;
  }

  public void setPreferences(List<Preference> preferences) {
    this.preferences = preferences;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public User getLastModifiedBy() {
    return lastModifiedBy;
  }

  public void setLastModifiedBy(User lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
  }

  public int getSuggestedDiscount() {
    return suggestedDiscount;
  }

  public void setSuggestedDiscount(int suggestedDiscount) {
    this.suggestedDiscount = suggestedDiscount;
  }

  public int getSuggestedBudget() {
    return suggestedBudget;
  }

  public void setSuggestedBudget(int suggestedBudget) {
    this.suggestedBudget = suggestedBudget;
  }

  public List<Comment> getComments() {
    return comments;
  }

  public void setComments(List<Comment> comments) {
    this.comments = comments;
  }
}
