package se.sep.finance.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import se.sep.common.domain.model.SepId;
import se.sep.finance.application.FinanicalRequestService;
import se.sep.finance.domain.model.FinancialRequest;
import se.sep.finance.domain.model.Status;
import se.sep.finance.rest.mappers.FinancialRequestMapper;
import se.sep.finance.rest.dto.FinancialRequestDto;
import se.sep.finance.rest.dto.NewFinancialRequest;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.stream.Collectors.toList;

@RestController
public class FinancialRequestController {
  private static Logger logger = LoggerFactory.getLogger(FinancialRequestController.class);

  @Autowired
  private FinancialRequestMapper mapper;

  @Autowired
  private FinanicalRequestService finanicalRequestService;

  @RequestMapping(value = "/api/financial/requests", method = RequestMethod.POST)
  public CompletableFuture<FinancialRequestDto> createFinancialRequest(@RequestBody NewFinancialRequest newFinancialRequest) {
    logger.info("receive create financial request");
    FinancialRequest financialRequest = mapper.toFinancialRequest(newFinancialRequest);
    return finanicalRequestService.create(financialRequest).thenApply(mapper::fromFinancialRequest);
  }

  @RequestMapping(value = "/api/financial/requests/{status}", method = RequestMethod.GET)
  public CompletableFuture<List<FinancialRequestDto>> findFinancialRequestsByStatus(@PathVariable Status status) {
    logger.info("receive get request");
    return finanicalRequestService.findByStatus(status)
      .thenApply(list -> list.stream().map(mapper::fromFinancialRequest).collect(toList()));
  }

  @RequestMapping(value = "/api/financial/requests/{sepId}/status/{status}", method = RequestMethod.PUT)
  public CompletableFuture<FinancialRequestDto> updateFinancialRequestStatus(@PathVariable SepId sepId, @PathVariable Status status) {
    return finanicalRequestService.updateStatus(sepId, status)
      .thenApply(mapper::fromFinancialRequest);
  }
}
