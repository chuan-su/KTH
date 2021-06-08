package se.sep.finance.infrastructure;

import org.bson.types.ObjectId;
import se.sep.common.domain.model.SepId;
import se.sep.finance.domain.model.FinancialRequest;
import se.sep.finance.domain.model.Status;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface FinancialRequestRepository {

  CompletableFuture<FinancialRequest> create(FinancialRequest financialRequest);

  CompletableFuture<FinancialRequest> update(SepId sepId, FinancialRequest financialRequest);

  CompletableFuture<List<FinancialRequest>> findByStatus(Status status);

  CompletableFuture<FinancialRequest> updateStatus(SepId sepId, Status status);
}
