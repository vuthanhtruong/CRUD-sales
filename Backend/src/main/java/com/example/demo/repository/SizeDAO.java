package com.example.demo.repository;

import com.example.demo.dto.SizeDTO;
import com.example.demo.model.Size;

import java.util.List;

public interface SizeDAO {
    Size getSize(String id);
    SizeDTO getSizeDTO(String id);
    Size CreateSize(Size size);
    Size editSize(Size size, String id);
    boolean deleteSize(String id);
    List<Size> getAllSizes();
    List<SizeDTO> getAllSizeDTOs();
}
