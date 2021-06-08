package se.sep.finance.application;

import se.sep.common.domain.model.SepId;
import se.sep.finance.domain.model.FinancialRequest;
import se.sep.finance.domain.model.Status;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface FinanicalRequestService {

  CompletableFuture<FinancialRequest> create(FinancialRequest financialRequest);

  CompletableFuture<FinancialRequest> update(SepId objectId, FinancialRequest financialRequest);

  CompletableFuture<FinancialRequest> updateStatus(SepId sepId, Status status);

  CompletableFuture<List<FinancialRequest>> findByStatus(Status status);
}
