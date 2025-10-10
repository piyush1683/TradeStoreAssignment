package com.sample.trade.common.model;

import com.sample.trade.common.validation.MaturityDateValid;
import com.sample.trade.common.validation.TradeExpiryValid;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

@MaturityDateValid(failureMessage = "Trade maturity date is in the past and not acceptable")
public class TradeRecord implements Comparable<TradeRecord> {
    @NotBlank(message = "Trade ID cannot be blank")
    private String tradeId;

    @Positive(message = "Version must be positive")
    private int version;

    @NotBlank(message = "Counter Party ID cannot be blank")
    private String counterPartyId;

    @NotBlank(message = "Book ID cannot be blank")
    private String bookId;

    @NotNull(message = "Maturity date cannot be null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "uuuu-MM-dd")
    private LocalDate maturityDate;

    @NotNull(message = "Created date cannot be null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "uuuu-MM-dd")
    private LocalDate createdDate;

    private String expired;

    @NotBlank(message = "Request ID cannot be blank")
    private String requestId;

    public TradeRecord() {
    }

    public TradeRecord(String tradeId, int version, String counterPartyId, String bookId,
            LocalDate maturityDate, LocalDate createdDate, String expired, String requestId) {
        this.tradeId = tradeId;
        this.version = version;
        this.counterPartyId = counterPartyId;
        this.bookId = bookId;
        this.maturityDate = maturityDate;
        this.createdDate = createdDate;
        this.expired = expired;
        this.requestId = requestId;
    }

    // Getters and Setters
    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getCounterPartyId() {
        return counterPartyId;
    }

    public void setCounterPartyId(String counterPartyId) {
        this.counterPartyId = counterPartyId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public LocalDate getMaturityDate() {
        return maturityDate;
    }

    public void setMaturityDate(LocalDate maturityDate) {
        this.maturityDate = maturityDate;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public String getExpired() {
        return expired;
    }

    public void setExpired(String expired) {
        this.expired = expired;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    // Will implement later
    @Override
    public int compareTo(TradeRecord other) {
        int idCompare = this.tradeId.compareTo(other.tradeId);
        if (idCompare != 0) {
            return idCompare;
        }
        return Integer.compare(this.version, other.version);
    }
}
