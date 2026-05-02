package com.example.demo.service;

import com.example.demo.dto.ColorDTO;
import java.util.List;

public interface ColorService {
    void createColor(ColorDTO color);
    List<ColorDTO> findAllColor();
    void editColor(ColorDTO color);
    void deleteColor(String id);
    ColorDTO getColorbyId(String id);
}
