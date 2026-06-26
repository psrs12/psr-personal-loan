package com.personalloan.applicationmanagement.domain.application.port;

import com.personalloan.applicationmanagement.domain.application.ApplicationOffer;

public interface ApplicationOfferRepository {
    ApplicationOffer save(ApplicationOffer applicationOffer);
}
