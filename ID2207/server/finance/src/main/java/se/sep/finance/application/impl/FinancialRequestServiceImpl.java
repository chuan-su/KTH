package se.sep.finance.application.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.sep.common.domain.model.SepId;
import se.sep.finance.application.FinanicalRequestService;
import se.sep.finance.domain.model.FinancialRequest;
import se.sep.finance.domain.model.Status;
import se.sep.finance.infrastructure.FinancialRequestRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class FinancialRequestServiceImpl implements FinanicalRequestService {

  @Autowired
  FinancialRequestRepository financialRequestRepository;

  @Override
  public CompletableFuture<FinancialRequest> create(FinancialRequest financialRequest) {
    financialRequest.setStatus(Status.OPEN);

    return financialRequestRepository.create(financialRequest);
  }

  @Override
  public CompletableFuture<FinancialRequest> update(SepId sepId, FinancialRequest financialRequest) {
    return financialRequestRepository.update(sepId, financialRequest);
  }

  @Override
  public CompletableFuture<FinancialRequest> updateStatus(SepId sepId, Status status) {
    return financialRequestRepository.updateStatus(sepId, status);
  }

  @Override
  public CompletableFuture<List<FinancialRequest>> findByStatus(Status status) {
    return financialRequestRepository.findByStatus(status);
  }
}
