package com.example.demo.repository;

import com.example.demo.model.Color;
import com.example.demo.model.Size;

import java.util.List;

public interface SizeDAO {
    Size CreateSize(Size size);
    Size editSize(Size size, String id);
    boolean deleteSize(String id);
    List<Size> getAllSizes();
    Size getSize(String id);
}
