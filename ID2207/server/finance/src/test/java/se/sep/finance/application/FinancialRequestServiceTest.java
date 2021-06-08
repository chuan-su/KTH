package se.sep.finance.application;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.sep.common.domain.model.SepId;
import se.sep.finance.application.impl.FinancialRequestServiceImpl;
import se.sep.finance.domain.model.FinancialRequest;
import se.sep.finance.domain.model.Status;
import se.sep.finance.infrastructure.FinancialRequestRepository;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FinancialRequestServiceTest {

  @Mock
  private FinancialRequestRepository financialRequestRepository;

  @InjectMocks
  private FinanicalRequestService finanicalRequestService = new FinancialRequestServiceImpl();

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testCreateFinancialRequest() {
    // Given
    when(financialRequestRepository.create(any(FinancialRequest.class))).thenReturn(CompletableFuture.completedFuture(new FinancialRequest()));

    // When
    finanicalRequestService.create(new FinancialRequest()).join();

    // Then
    ArgumentCaptor<FinancialRequest> captor = ArgumentCaptor.forClass(FinancialRequest.class);
    verify(financialRequestRepository).create(captor.capture());
    assertEquals(Status.OPEN, captor.getValue().getStatus());
  }

  @Test
  public void testUpdateFinancialRequest() {
    // Given
    SepId financialRequestId = SepId.create();
    FinancialRequest financialRequest = new FinancialRequest();

    when(financialRequestRepository.update(any(SepId.class), any(FinancialRequest.class))).thenReturn(CompletableFuture.completedFuture(new FinancialRequest()));

    // When
    finanicalRequestService.update(financialRequestId, financialRequest).join();

    // Then
    verify(financialRequestRepository).update(eq(financialRequestId), eq(financialRequest));
  }

  @Test
  public void testUpdateFinancialRequestStatus() {
    // Given
    SepId financialRequestId = SepId.create();

    when(financialRequestRepository.updateStatus(any(SepId.class), any(Status.class))).thenReturn(CompletableFuture.completedFuture(new FinancialRequest()));

    // When
    finanicalRequestService.updateStatus(financialRequestId, Status.REJECTED).join();

    // Then
    verify(financialRequestRepository).updateStatus(eq(financialRequestId), eq(Status.REJECTED));
  }

  @Test
  public void testFindByStatus() {
    // Given
    when(financialRequestRepository.findByStatus(any(Status.class))).thenReturn(CompletableFuture.completedFuture(new ArrayList<>()));

    // When
    finanicalRequestService.findByStatus(Status.REJECTED).join();

    // Then
    verify(financialRequestRepository).findByStatus(eq(Status.REJECTED));
  }
}
