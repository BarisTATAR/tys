package com.tys.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EarlyCheckOutOutcome {
    private boolean reservationClosed;
    private String kbsWarning;
}
