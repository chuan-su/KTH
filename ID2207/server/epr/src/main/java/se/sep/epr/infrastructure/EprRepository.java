package se.sep.epr.infrastructure;

import se.sep.common.domain.model.SepId;
import se.sep.epr.domain.model.Comment;
import se.sep.epr.domain.model.Epr;
import se.sep.epr.domain.model.Status;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface EprRepository {
  CompletableFuture<Epr> create(Epr epr);
  CompletableFuture<Epr> update(SepId eprId, Epr epr);

  CompletableFuture<Epr> updateStatus(SepId eprId, Epr epr);

  CompletableFuture<Epr> updateFinance(SepId eprId, Epr epr);

  CompletableFuture<Epr> findById(SepId eprId);

  CompletableFuture<List<Epr>> findAll();

  CompletableFuture<Epr> addComment(SepId sepId, Comment comment);
  CompletableFuture<List<Epr>> findByStatus(Status status);
}
