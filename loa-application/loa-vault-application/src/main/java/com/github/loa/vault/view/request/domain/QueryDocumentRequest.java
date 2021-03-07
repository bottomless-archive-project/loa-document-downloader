package com.github.loa.vault.view.request.domain;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class QueryDocumentRequest {

    String documentId;
}
