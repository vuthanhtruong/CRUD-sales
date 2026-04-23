package com.example.demo.service;

import com.example.demo.dto.ColorDTO;
import com.example.demo.model.Color;

import java.util.List;

public interface ColorService {
    void createColor(ColorDTO color);
    List<ColorDTO> findAllColor();
    void editColor(ColorDTO color);
    void deleteColor(String id);
    Color getColorbyId(String id);
}
