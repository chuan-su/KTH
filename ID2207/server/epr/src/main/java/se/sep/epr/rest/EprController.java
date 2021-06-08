package se.sep.epr.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import se.sep.common.domain.model.SepId;
import se.sep.epr.application.EprService;
import se.sep.epr.domain.model.Comment;
import se.sep.epr.domain.model.Epr;
import se.sep.epr.domain.model.Status;
import se.sep.epr.rest.dto.EprDto;
import se.sep.epr.rest.dto.EprFinanceUpdate;
import se.sep.epr.rest.dto.NewComment;
import se.sep.epr.rest.dto.NewEpr;
import se.sep.epr.rest.mappers.EprCommentMapper;
import se.sep.epr.rest.mappers.EprMapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
public class EprController {
  @Autowired
  private EprMapper eprMapper;

  @Autowired
  private EprCommentMapper commentMapper;

  @Autowired
  private EprService eprService;

  @RequestMapping(value = "/api/epr", method = RequestMethod.POST)
  public CompletableFuture<EprDto> createEpr(@RequestBody NewEpr newEpr) {
    Epr epr = eprMapper.toEpr(newEpr);

    return eprService.createEpr(epr)
      .thenApply(eprMapper::fromEpr);
  }

  @RequestMapping(value = "/api/epr/{eprId}", method = RequestMethod.PUT)
  public CompletableFuture<EprDto> updateEpr(@PathVariable SepId eprId, @RequestBody NewEpr newEpr) {
    Epr epr = eprMapper.toEpr(newEpr);

    return eprService.updateEpr(eprId, epr)
      .thenApply(eprMapper::fromEpr);
  }

  @RequestMapping(value = "/api/epr/{eprId}/approve", method = RequestMethod.PUT)
  public CompletableFuture<EprDto> approveEpr(@PathVariable SepId eprId) {
    return eprService.updateEprStatus(eprId, Status.APPROVED)
      .thenApply(eprMapper::fromEpr);
  }

  @RequestMapping(value = "/api/epr/{eprId}/reject", method = RequestMethod.PUT)
  public CompletableFuture<EprDto> rejectEpr(@PathVariable SepId eprId) {
    return eprService.updateEprStatus(eprId, Status.REJECTED)
      .thenApply(eprMapper::fromEpr);
  }

  @RequestMapping(value="/api/epr/me", method = RequestMethod.GET)
  public CompletableFuture<List<EprDto>> getMyEprList() {
    return eprService.getMyEprList()
      .thenApply(eprs -> eprs.stream().map(eprMapper::fromEpr).collect(Collectors.toList()));
  }

  @RequestMapping(value = "/api/epr/{eprId}/comments", method = RequestMethod.POST)
  public CompletableFuture<EprDto> addComment(@PathVariable SepId eprId, @RequestBody NewComment newComment) {
    Comment comment = commentMapper.toComment(newComment);
    return eprService.addComment(eprId, comment)
      .thenApply(eprMapper::fromEpr);
  }

  @RequestMapping(value = "/api/epr/{eprId}/finance", method = RequestMethod.PUT)
  public CompletableFuture<EprDto> updateEprFinance(@PathVariable SepId eprId, @RequestBody EprFinanceUpdate eprFinanceUpdate) {
    Epr epr = eprMapper.toEpr(eprFinanceUpdate);
    return eprService.updateEprFinance(eprId, epr)
      .thenApply(eprMapper::fromEpr);
  }
}
