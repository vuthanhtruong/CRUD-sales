package com.example.demo.service;

import com.example.demo.dto.SizeDTO;
import com.example.demo.model.Size;
import com.example.demo.repository.SizeDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SizeServiceImpl implements SizeService {

    @Override
    public SizeDTO getSize(String id) {
        return sizeDAO.getSizeDTO(id);
    }

    @Autowired
    private SizeDAO sizeDAO;

    @Override
    public SizeDTO CreateSize(SizeDTO dto) {
        Size size = toEntity(dto);
        Size saved = sizeDAO.CreateSize(size);
        return toDTO(saved);
    }

    @Override
    public SizeDTO editSize(SizeDTO dto, String id) {
        Size size = toEntity(dto);
        Size updated = sizeDAO.editSize(size, id);
        return toDTO(updated);
    }

    @Override
    public boolean deleteSize(String id) {
        return sizeDAO.deleteSize(id);
    }

    @Override
    public List<SizeDTO> getAllSizes() {
        return sizeDAO.getAllSizeDTOs();
    }

    private SizeDTO toDTO(Size size) {
        return new SizeDTO(size.getId(), size.getName());
    }

    private Size toEntity(SizeDTO dto) {
        Size size = new Size();
        size.setId(dto.getId());
        size.setName(dto.getName());
        return size;
    }
}