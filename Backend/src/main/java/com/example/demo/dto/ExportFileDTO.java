package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExportFileDTO {
    private String fileName;
    private String contentType;
    private byte[] data;
}
