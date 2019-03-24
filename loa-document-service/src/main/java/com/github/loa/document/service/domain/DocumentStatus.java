package com.github.loa.document.service.domain;

public enum DocumentStatus {

    /**
     * A document is under crawl, just initialized.
     */
    UNDER_CRAWL,
    /**
     * The download was failed.
     */
    FAILED,
    /**
     * Used when a document is a duplicate of an other document already crawled.
     */
    DUPLICATE,
    /**
     * Used when the file downloaded was not a valid pdf.
     */
    INVALID,
    /**
     * The document was successfully downloaded and moved to the target area.
     */
    DOWNLOADED,
    /**
     * Failed while processing the downloaded material.
     */
    PROCESSING_FAILURE
}
