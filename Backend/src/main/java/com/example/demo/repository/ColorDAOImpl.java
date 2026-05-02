package com.example.demo.repository;

import com.example.demo.dto.ColorDTO;
import com.example.demo.model.Color;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public class ColorDAOImpl implements ColorDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void createColor(Color color) {
        entityManager.persist(color);
    }

    @Override
    public List<Color> findAllColor() {
        return entityManager
                .createQuery("SELECT c FROM Color c", Color.class)
                .getResultList();
    }

    @Override
    public void editColor(Color color) {
        entityManager.merge(color);
    }

    @Override
    public void deleteColor(String id) {

        Color color = entityManager.find(Color.class, id);
        if (color == null) return;

        // 🔥 XÓA TOÀN BỘ VARIANT LIÊN QUAN
        entityManager.createQuery(
                        "DELETE FROM ProductVariant pv WHERE pv.color.id = :colorId"
                )
                .setParameter("colorId", id)
                .executeUpdate();

        // 🔥 SAU ĐÓ MỚI XÓA COLOR
        entityManager.remove(color);
    }

    @Override
    public Color getColorbyId(String id) {
        return entityManager.find(Color.class, id);
    }

    @Override
    public ColorDTO getColorDTOById(String id) {
        return entityManager.createQuery(
                        "SELECT new com.example.demo.dto.ColorDTO(c.id, c.name) FROM Color c WHERE c.id = :id",
                        ColorDTO.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<ColorDTO> findAllColorDTO() {
        return entityManager
                .createQuery("SELECT new com.example.demo.dto.ColorDTO(c.id, c.name) FROM Color c", ColorDTO.class)
                .getResultList();
    }

}
