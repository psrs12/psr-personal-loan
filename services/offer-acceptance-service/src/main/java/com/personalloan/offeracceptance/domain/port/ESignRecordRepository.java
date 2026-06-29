package com.personalloan.offeracceptance.domain.port;

import com.personalloan.offeracceptance.domain.offer.ESignRecord;

public interface ESignRecordRepository {
    void save(ESignRecord record);
}
