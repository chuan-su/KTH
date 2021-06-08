package se.sep.epr.rest.mappers;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import se.sep.epr.domain.model.Comment;
import se.sep.epr.rest.dto.CommentDto;
import se.sep.epr.rest.dto.NewComment;

@Mapper(componentModel = "spring" , unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EprCommentMapper {
  @InheritInverseConfiguration
  CommentDto fromComment(Comment comment);

  Comment toComment(NewComment newComment);
}
