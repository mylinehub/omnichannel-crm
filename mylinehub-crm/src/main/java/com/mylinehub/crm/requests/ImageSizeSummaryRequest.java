package com.mylinehub.crm.requests;

import lombok.Data;

@Data
public class ImageSizeSummaryRequest {
    private Long imageSize;
    private Long iconImageSize;
    private Long doc1ImageSize;
    private Long doc2ImageSize;

    public ImageSizeSummaryRequest(Long imageSize, Long iconImageSize, Long doc1ImageSize, Long doc2ImageSize) {
        this.imageSize = imageSize;
        this.iconImageSize = iconImageSize;
        this.doc1ImageSize = doc1ImageSize;
        this.doc2ImageSize = doc2ImageSize;
    }

}

