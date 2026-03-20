package com.Tithaal.Wallet.filter;

import com.Tithaal.Wallet.entity.IdempotencyRecord;
import com.Tithaal.Wallet.entity.IdempotencyStatus;
import com.Tithaal.Wallet.repository.IdempotencyRecordRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class IdempotencyFilter extends OncePerRequestFilter {

    private final IdempotencyRecordRepository idempotencyRecordRepository;

    public IdempotencyFilter(IdempotencyRecordRepository idempotencyRecordRepository) {
        this.idempotencyRecordRepository = idempotencyRecordRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String idempotencyKey = request.getHeader("Idempotency-Key");

        if (idempotencyKey == null || idempotencyKey.trim().isEmpty() || !request.getMethod().equalsIgnoreCase("POST")) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<IdempotencyRecord> existingRecordOpt = idempotencyRecordRepository.findById(idempotencyKey);

        if (existingRecordOpt.isPresent()) {
            IdempotencyRecord existingRecord = existingRecordOpt.get();
            if (existingRecord.getStatus() == IdempotencyStatus.COMPLETED || existingRecord.getStatus() == IdempotencyStatus.FAILED) {
                response.setStatus(existingRecord.getResponseStatusCode());
                response.setContentType("application/json");
                if (existingRecord.getResponseBody() != null) {
                    PrintWriter out = response.getWriter();
                    out.print(existingRecord.getResponseBody());
                    out.flush();
                }
                return;
            } else if (existingRecord.getStatus() == IdempotencyStatus.PROCESSING) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                response.setContentType("application/json");
                PrintWriter out = response.getWriter();
                out.print("{\"error\": \"A request with this Idempotency-Key is currently being processed.\"}");
                out.flush();
                log.warn("Idempotency conflict: Request with key {} is already PROCESSING", idempotencyKey);
                return;
            }
        }

        IdempotencyRecord newRecord = IdempotencyRecord.builder()
                .idempotencyKey(idempotencyKey)
                .requestPath(request.getRequestURI())
                .status(IdempotencyStatus.PROCESSING)
                .build();

        try {
            idempotencyRecordRepository.saveAndFlush(newRecord);
        } catch (DataIntegrityViolationException e) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            out.print("{\"error\": \"A request with this Idempotency-Key is currently being processed by another thread.\"}");
            out.flush();
            log.warn("Idempotency conflict: Thread collision detected for key {}", idempotencyKey);
            return;
        }

        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(request, responseWrapper);
            
            newRecord.setStatus(IdempotencyStatus.COMPLETED);
            newRecord.setResponseStatusCode(responseWrapper.getStatus());
            byte[] responseArray = responseWrapper.getContentAsByteArray();
            newRecord.setResponseBody(new String(responseArray, responseWrapper.getCharacterEncoding()));
            idempotencyRecordRepository.saveAndFlush(newRecord);

            responseWrapper.copyBodyToResponse();
        } catch (Exception e) {
            newRecord.setStatus(IdempotencyStatus.FAILED);
            newRecord.setResponseStatusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            newRecord.setResponseBody("{\"error\": \"Internal server error occurred during processing.\"}");
            idempotencyRecordRepository.saveAndFlush(newRecord);
            log.error("Internal error processing request with idempotency key {}", idempotencyKey, e);
            throw e;
        }
    }
}
