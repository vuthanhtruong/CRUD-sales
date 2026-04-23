package com.example.demo.service;

import com.example.demo.dto.ColorDTO;
import com.example.demo.model.Color;
import com.example.demo.repository.ColorDAO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ColorServiceImpl implements ColorService {

    private final ColorDAO colorDAO;

    public ColorServiceImpl(ColorDAO colorDAO) {
        this.colorDAO = colorDAO;
    }

    @Override
    public void createColor(ColorDTO dto) {
        Color color = new Color();
        color.setId(dto.getId());
        color.setName(dto.getName());

        colorDAO.createColor(color);
    }

    @Override
    public List<ColorDTO> findAllColor() {
        return colorDAO.findAllColor()
                .stream()
                .map(color -> new ColorDTO(
                        color.getId(),
                        color.getName()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public void editColor(ColorDTO dto) {
        Color color = new Color();
        color.setId(dto.getId());
        color.setName(dto.getName());

        colorDAO.editColor(color);
    }

    @Override
    public void deleteColor(String id) {
        colorDAO.deleteColor(id);
    }

    @Override
    public Color getColorbyId(String id) {
        return colorDAO.getColorbyId(id);
    }
}