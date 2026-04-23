package com.example.demo.service;

import com.example.demo.dto.SizeDTO;
import com.example.demo.model.Size;

import java.util.List;

public interface SizeService {
    SizeDTO CreateSize(SizeDTO size);
    SizeDTO editSize(SizeDTO size, String id);
    boolean deleteSize(String id);
    List<SizeDTO> getAllSizes();
    SizeDTO getSize(String id);
}
