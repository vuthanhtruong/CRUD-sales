package com.example.demo.repository;

import com.example.demo.dto.ColorDTO;
import com.example.demo.model.Color;

import java.util.List;

public interface ColorDAO {
    void createColor(Color color);
    List<Color> findAllColor();
    List<ColorDTO> findAllColorDTO();
    void editColor(Color color);
    void deleteColor(String id);
    Color getColorbyId(String id);
    ColorDTO getColorDTOById(String id);
}
