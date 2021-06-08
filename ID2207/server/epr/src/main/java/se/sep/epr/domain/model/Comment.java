package se.sep.epr.domain.model;

import se.sep.common.domain.model.AbstractEntity;

public class Comment extends AbstractEntity {
  private String comment;
  private User commentedBy;

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public User getCommentedBy() {
    return commentedBy;
  }

  public void setCommentedBy(User commentedBy) {
    this.commentedBy = commentedBy;
  }
}
