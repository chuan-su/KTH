package se.sep.common.domain.model;

import java.time.LocalDateTime;

public abstract class AbstractEntity {
  protected SepId sepId;
  protected LocalDateTime createdAt;
  protected LocalDateTime lastModifiedAt;

  public AbstractEntity() {
    this.sepId = SepId.create();
    this.createdAt = LocalDateTime.now();
    this.lastModifiedAt = LocalDateTime.now();
  }

  public SepId getSepId() {
    return sepId;
  }

  public void setSepId(SepId sepId) {
    this.sepId = sepId;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getLastModifiedAt() {
    return lastModifiedAt;
  }

  public void setLastModifiedAt(LocalDateTime lastModifiedAt) {
    this.lastModifiedAt = lastModifiedAt;
  }
}
