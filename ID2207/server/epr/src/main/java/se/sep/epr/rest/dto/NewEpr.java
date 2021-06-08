package se.sep.epr.rest.dto;

import se.sep.epr.domain.model.Preference;

import java.time.LocalDate;
import java.util.List;

public class NewEpr {
  public String clientName;
  public String eventType;

  public String numberOfAttendees;
  public String plannedBudget;

  public LocalDate eventDateFrom;
  public LocalDate eventDateTo;

  public List<Preference> preferences;
}
