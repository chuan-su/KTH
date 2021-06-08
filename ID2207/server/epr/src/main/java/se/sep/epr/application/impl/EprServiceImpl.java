package se.sep.epr.application.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.sep.common.domain.model.SepId;
import se.sep.common.exceptions.HttpUnauthorizedException;
import se.sep.epr.application.EprService;
import se.sep.epr.domain.model.Comment;
import se.sep.epr.domain.model.Epr;
import se.sep.epr.domain.model.Status;
import se.sep.epr.domain.model.User;
import se.sep.epr.infrastructure.EprRepository;
import se.sep.security.application.AuthService;
import se.sep.security.domain.model.Role;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class EprServiceImpl implements EprService {
  @Autowired
  private DomainUserMapper domainUserMapper;

  @Autowired
  private EprRepository eprRepository;

  @Autowired
  private AuthService authService;

  @Override
  public CompletableFuture<Epr> createEpr(Epr epr) {
    User user = domainUserMapper.toUser(authService.getUserFromCurrentSession());

    epr.setLastModifiedBy(user);
    epr.setStatus(Status.OPEN);

    return eprRepository.create(epr);
  }

  @Override
  public CompletableFuture<Epr> updateEpr(SepId eprId, Epr epr) {
    User user = domainUserMapper.toUser(authService.getUserFromCurrentSession());
    epr.setLastModifiedBy(user);
    epr.setStatus(Status.OPEN);

    return eprRepository.update(eprId, epr);
  }

  @Override
  public CompletableFuture<Epr> updateEprStatus(SepId eprId, Status status) {
    User user = domainUserMapper.toUser(authService.getUserFromCurrentSession());

    Epr epr = new Epr();
    epr.setLastModifiedBy(user);
    epr.setLastModifiedAt(LocalDateTime.now());

    if (Objects.equals(Status.APPROVED, status) && Objects.equals(Role.ADMINISTRATION_MANAGER, user.getRole())) {
      epr.setStatus(Status.PRODUCTION);
    } else {
      epr.setStatus(status);
    }

    return eprRepository.updateStatus(eprId, epr);
  }

  @Override
  public CompletableFuture<Epr> addComment(SepId eprId, Comment comment) {
    User user = domainUserMapper.toUser(authService.getUserFromCurrentSession());
    comment.setCommentedBy(user);

    return eprRepository.addComment(eprId, comment);
  }

  @Override
  public CompletableFuture<Epr> updateEprFinance(SepId eprId, Epr epr) {
    User user = domainUserMapper.toUser(authService.getUserFromCurrentSession());
    epr.setLastModifiedBy(user);

    if (!Objects.equals(Role.FINANCIAL_MANAGER, user.getRole())) {
      throw new HttpUnauthorizedException("Only Financial Manager allowed to update");
    }

    return eprRepository.updateFinance(eprId, epr);
  }

  @Override
  public CompletableFuture<List<Epr>> getMyEprList() {
    User user = domainUserMapper.toUser(authService.getUserFromCurrentSession());

    switch (user.getRole()) {
      case PRODUCTION_MANAGER:
        return eprRepository.findByStatus(Status.PRODUCTION);
      case FINANCIAL_MANAGER:
        return eprRepository.findByStatus(Status.APPROVED)
          .thenApply(eprs -> eprs.stream()
            .filter(epr -> Objects.equals(Role.CUSTOMER_SERVICE_SENIOR_OFFICER, epr.getLastModifiedBy().getRole()))
            .collect(Collectors.toList()));
      case CUSTOMER_SERVICE_SENIOR_OFFICER:
        return eprRepository.findAll()
          .thenApply(eprs -> eprs.stream()
            .filter(epr -> Objects.equals(Status.OPEN, epr.getStatus()) || Objects.equals(Role.ADMINISTRATION_MANAGER, epr.getLastModifiedBy().getRole()))
            .collect(Collectors.toList()));
      case ADMINISTRATION_MANAGER:
        return eprRepository.findByStatus(Status.APPROVED)
          .thenApply(eprs -> eprs.stream()
            .filter(epr -> Objects.equals(Role.FINANCIAL_MANAGER, epr.getLastModifiedBy().getRole()))
            .collect(Collectors.toList()));
      default:
        throw new HttpUnauthorizedException("You are not allowed to view EPRs");
    }
  }
}
