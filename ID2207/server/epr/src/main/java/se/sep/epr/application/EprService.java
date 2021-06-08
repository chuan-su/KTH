package se.sep.epr.application;

import se.sep.common.domain.model.SepId;
import se.sep.epr.domain.model.Comment;
import se.sep.epr.domain.model.Epr;
import se.sep.epr.domain.model.Status;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface EprService {
  CompletableFuture<Epr> createEpr(Epr epr);
  CompletableFuture<Epr> updateEpr(SepId eprId, Epr epr);

  CompletableFuture<Epr> updateEprStatus(SepId eprId, Status status);

  CompletableFuture<Epr> addComment(SepId sepId, Comment comment);

  CompletableFuture<Epr> updateEprFinance(SepId eprId, Epr epr);

  CompletableFuture<List<Epr>> getMyEprList();
}
