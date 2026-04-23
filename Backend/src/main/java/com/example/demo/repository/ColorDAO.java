package com.example.demo.repository;

import com.example.demo.model.Color;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface ColorDAO {
    void createColor(Color color);
    List<Color> findAllColor();
    void editColor(Color color);
    void deleteColor(String id);
    Color getColorbyId(String id);
}
