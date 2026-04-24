package com.example.demo.service;

import com.example.demo.dto.ColorDTO;
import com.example.demo.dto.ProductVariantDTO;
import com.example.demo.dto.SizeDTO;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductVariantServiceImpl implements ProductVariantService {
    @Override
    public int getQuantity(String productId, String sizeId, String colorId) {
        return variantDAO.getQuantity(productId, sizeId, colorId);
    }

    @Override
    public boolean decreaseQuantity(String productId, String sizeId, String colorId, int amount) {

        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be > 0");
        }

        int updatedRows = variantDAO.decreaseQuantity(productId, sizeId, colorId, amount);

        if (updatedRows == 0) {
            throw new RuntimeException("Not enough stock or variant not found");
        }

        return true;
    }

    @Override
    public List<SizeDTO> findSizesByProductId(String productId) {

        List<Size> sizes = variantDAO.findSizesByProductId(productId);

        return sizes.stream()
                .map(s -> new SizeDTO(s.getId(), s.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ColorDTO> findColorsByProductId(String productId) {

        List<Color> colors = variantDAO.findColorsByProductId(productId);

        return colors.stream()
                .map(c -> new ColorDTO(c.getId(), c.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public List<SizeDTO> findUnusedSizesByProductId(String productId) {

        List<Size> allSizes = sizeDAO.getAllSizes();

        List<ProductVariant> usedVariants = variantDAO.findByProductId(productId);

        List<String> usedSizeIds = usedVariants.stream()
                .map(v -> v.getSize().getId())
                .toList();

        return allSizes.stream()
                .filter(size -> !usedSizeIds.contains(size.getId()))
                .map(size -> new SizeDTO(size.getId(), size.getName()))
                .toList();
    }

    @Override
    public List<ColorDTO> findUnusedColorsByProductId(String productId) {

        List<Color> allColors = colorDAO.findAllColor();

        List<ProductVariant> usedVariants = variantDAO.findByProductId(productId);

        List<String> usedColorIds = usedVariants.stream()
                .map(v -> v.getColor().getId())
                .toList();

        return allColors.stream()
                .filter(color -> !usedColorIds.contains(color.getId()))
                .map(color -> new ColorDTO(color.getId(), color.getName()))
                .toList();
    }

    @Autowired
    private ProductVariantDAO variantDAO;

    @Autowired
    private ProductDAO productDAO;

    @Autowired
    private SizeDAO sizeDAO;

    @Autowired
    private ColorDAO colorDAO;

    @Override
    public List<ProductVariantDTO> findAll() {
        return variantDAO.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductVariantDTO findById(String productId, String sizeId, String colorId) {

        ProductVariantId id = new ProductVariantId(productId, sizeId, colorId);

        ProductVariant variant = variantDAO.findById(id);

        return toDTO(variant);
    }

    @Override
    public List<ProductVariantDTO> findByProductId(String productId) {

        return variantDAO.findByProductId(productId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductVariantDTO create(ProductVariantDTO dto) {

        ProductVariant entity = toEntity(dto);

        variantDAO.create(entity);

        return toDTO(entity);
    }

    @Override
    public ProductVariantDTO update(ProductVariantDTO dto) {

        ProductVariant entity = toEntity(dto);

        variantDAO.update(entity);

        return toDTO(entity);
    }

    @Override
    public boolean delete(String productId, String sizeId, String colorId) {

        ProductVariantId id = new ProductVariantId(productId, sizeId, colorId);

        ProductVariant entity = variantDAO.findById(id);

        if (entity == null) return false;

        variantDAO.delete(entity);

        return true;
    }

    @Override
    public List<ProductVariantDTO> createBatch(List<ProductVariantDTO> dtos) {

        List<ProductVariant> entities = dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());

        List<ProductVariant> saved = variantDAO.createBatch(entities);

        return saved.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // =========================
    // MAPPER: ENTITY -> DTO
    // =========================
    private ProductVariantDTO toDTO(ProductVariant v) {

        if (v == null) return null;

        return new ProductVariantDTO(
                v.getId().getProductId(),
                v.getId().getSizeId(),
                v.getId().getColorId(),
                v.getProduct() != null ? v.getProduct().getProductName() : null,
                v.getSize() != null ? v.getSize().getName() : null,
                v.getColor() != null ? v.getColor().getName() : null,
                v.getQuantity()
        );
    }

    // =========================
    // MAPPER: DTO -> ENTITY
    // =========================
    private ProductVariant toEntity(ProductVariantDTO dto) {

        if (dto == null) return null;

        ProductVariant entity = new ProductVariant();

        ProductVariantId id = new ProductVariantId(
                dto.getProductId(),
                dto.getSizeId(),
                dto.getColorId()
        );

        entity.setId(id);

        entity.setProduct(productDAO.findById(dto.getProductId()));
        entity.setSize(sizeDAO.getSize(dto.getSizeId()));
        entity.setColor(colorDAO.getColorbyId(dto.getColorId()));

        entity.setQuantity(dto.getQuantity());

        return entity;
    }
}