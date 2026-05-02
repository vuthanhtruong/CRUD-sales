package com.example.demo.repository;

import com.example.demo.dto.SizeDTO;
import com.example.demo.model.Size;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public class SizeDAOImpl implements SizeDAO {
    @Override
    public Size getSize(String id) {
        return entityManager.find(Size.class, id);
    }

    @Override
    public SizeDTO getSizeDTO(String id) {
        return entityManager.createQuery(
                        "SELECT new com.example.demo.dto.SizeDTO(s.id, s.name) FROM Size s WHERE s.id = :id",
                        SizeDTO.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Size CreateSize(Size size) {
        entityManager.persist(size);
        return size;
    }

    @Override
    public Size editSize(Size size, String id) {
        size.setId(id);
        return entityManager.merge(size);
    }

    @Override
    public boolean deleteSize(String id) {

        Size size = entityManager.find(Size.class, id);
        if (size == null) {
            return false;
        }

        // 🔥 XÓA TOÀN BỘ VARIANT LIÊN QUAN
        entityManager.createQuery(
                        "DELETE FROM ProductVariant pv WHERE pv.size.id = :sizeId"
                )
                .setParameter("sizeId", id)
                .executeUpdate();

        // 🔥 XÓA SIZE
        entityManager.remove(size);

        return true;
    }

    @Override
    public List<Size> getAllSizes() {
        return entityManager.createQuery("FROM Size", Size.class).getResultList();
    }

    @Override
    public List<SizeDTO> getAllSizeDTOs() {
        return entityManager.createQuery("SELECT new com.example.demo.dto.SizeDTO(s.id, s.name) FROM Size s", SizeDTO.class)
                .getResultList();
    }

}
