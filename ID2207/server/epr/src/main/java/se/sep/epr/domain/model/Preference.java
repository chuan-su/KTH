package se.sep.epr.domain.model;

public class Preference {
  private PreferenceArea preferenceArea;
  private String description;

  public PreferenceArea getPreferenceArea() {
    return preferenceArea;
  }

  public void setPreferenceArea(PreferenceArea preferenceArea) {
    this.preferenceArea = preferenceArea;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
