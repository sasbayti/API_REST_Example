package com.example.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder

public class FileUploadResponse {
    
    private String fileName;
    private String downloadURI;
    private long size;
}
