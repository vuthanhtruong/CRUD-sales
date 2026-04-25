package com.example.demo.config;

import com.example.demo.model.*;
import com.example.demo.repository.AccountDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component
@Transactional
public class DataSeeder implements CommandLineRunner {

    private final AccountDAO accountDAO;
    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager em;

    public DataSeeder(AccountDAO accountDAO,
                      PasswordEncoder passwordEncoder) {
        this.accountDAO = accountDAO;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        seedAdmin();
        seedProductTypes();
        seedSizes();
        seedColors();
        seedProductsAndVariants();
        seedCoupons();
        seedCart();

        System.out.println("===== SEEDING DONE =====");
    }

    // ================= ADMIN =================
    private void seedAdmin() {
        if (accountDAO.getAccountByUsername("admin") == null) {

            Admin adminUser = new Admin();
            adminUser.setId("admin01");
            adminUser.setFirstName("System");
            adminUser.setLastName("Admin");
            adminUser.setPhone("0123456789");
            adminUser.setEmail("admin.com");
            adminUser.setAddress("System");
            adminUser.setGender(Gender.FEMALE);
            adminUser.setBirthday(LocalDate.of(2000, 1, 1));

            Account account = new Account();
            account.setId("admin01");
            account.setUsername("admin");
            account.setPassword(passwordEncoder.encode("admin123"));
            account.setUser(adminUser);

            accountDAO.createUser(account);

            System.out.println("ADMIN CREATED");
        }
    }

    // ================= PRODUCT TYPE =================
    private void seedProductTypes() {
        if (em.find(ProductType.class, "t1") == null) {

            em.persist(new ProductType("t1", "T-Shirt"));
            em.persist(new ProductType("t2", "Shoes"));
            em.persist(new ProductType("t3", "Jacket"));

            System.out.println("ProductType seeded");
        }
    }

    // ================= SIZE =================
    private void seedSizes() {
        if (em.find(Size.class, "s") == null) {

            em.persist(new Size("s", "S"));
            em.persist(new Size("m", "M"));
            em.persist(new Size("l", "L"));

            System.out.println("Size seeded");
        }
    }

    // ================= COLOR =================
    private void seedColors() {
        if (em.find(Color.class, "red") == null) {

            em.persist(new Color("red", "Red"));
            em.persist(new Color("blue", "Blue"));
            em.persist(new Color("black", "Black"));

            System.out.println("Color seeded");
        }
    }

    private void seedCart() {

        // tạo cart cho admin
        Admin admin = em.find(Admin.class, "admin01");
        if (admin == null) return;

        Long count = em.createQuery(
                        "SELECT COUNT(c) FROM Cart c WHERE c.user.id = :id", Long.class)
                .setParameter("id", admin.getId())
                .getSingleResult();

        if (count == 0) {
            Cart cart = new Cart();
            cart.setUser(admin);

            em.persist(cart);

            System.out.println("Cart created for admin");
        }
    }

    private void seedCoupons() {
        Long count = em.createQuery("SELECT COUNT(c) FROM Coupon c", Long.class).getSingleResult();
        if (count > 0) return;

        Coupon welcome = new Coupon();
        welcome.setCode("WELCOME10");
        welcome.setName("Welcome 10% off");
        welcome.setDiscountType(DiscountType.PERCENTAGE);
        welcome.setDiscountValue(BigDecimal.valueOf(10));
        welcome.setMinOrderAmount(BigDecimal.valueOf(150000));
        welcome.setMaxDiscountAmount(BigDecimal.valueOf(50000));
        welcome.setUsageLimit(200);
        welcome.setUsedCount(0);
        welcome.setActive(true);
        welcome.setStartsAt(LocalDateTime.now().minusDays(1));
        welcome.setExpiresAt(LocalDateTime.now().plusMonths(3));
        em.persist(welcome);

        Coupon freeship = new Coupon();
        freeship.setCode("SHIP30K");
        freeship.setName("Shipping support 30K");
        freeship.setDiscountType(DiscountType.FIXED_AMOUNT);
        freeship.setDiscountValue(BigDecimal.valueOf(30000));
        freeship.setMinOrderAmount(BigDecimal.valueOf(250000));
        freeship.setUsageLimit(300);
        freeship.setUsedCount(0);
        freeship.setActive(true);
        freeship.setStartsAt(LocalDateTime.now().minusDays(1));
        freeship.setExpiresAt(LocalDateTime.now().plusMonths(2));
        em.persist(freeship);

        System.out.println("Coupons seeded");
    }

    // ================= PRODUCT + VARIANT =================
    private void seedProductsAndVariants() {

        if (em.find(Product.class, "p1") != null) return;

        Admin admin = em.find(Admin.class, "admin01");

        List<ProductType> types =
                em.createQuery("SELECT t FROM ProductType t", ProductType.class).getResultList();

        List<Size> sizes =
                em.createQuery("SELECT s FROM Size s", Size.class).getResultList();

        List<Color> colors =
                em.createQuery("SELECT c FROM Color c", Color.class).getResultList();

        Random random = new Random();

        for (int i = 1; i <= 30; i++) {

            Product p = new Product();
            p.setProductId("p" + i);
            p.setProductName("Product " + i);

            // ACTIVE / INACTIVE logic
            p.setStatus(i % 5 == 0 ? ProductStatus.INACTIVE : ProductStatus.ACTIVE);

            p.setProductType(types.get(random.nextInt(types.size())));
            p.setCreatedBy(admin);

            // ================= VND PRICE =================
            // 99,000 -> 1,500,000 VND
            int priceVnd = 99000 + random.nextInt(1400000);
            p.setPrice(BigDecimal.valueOf(priceVnd));

            p.setDescription("Description for product " + i);

            em.persist(p);

            // ================= VARIANTS =================
            for (Size s : sizes) {
                for (Color c : colors) {

                    if (random.nextBoolean()) {

                        ProductVariant variant = new ProductVariant();

                        ProductVariantId id = new ProductVariantId(
                                p.getProductId(),
                                s.getId(),
                                c.getId()
                        );

                        variant.setId(id);
                        variant.setProduct(p);
                        variant.setSize(s);
                        variant.setColor(c);
                        variant.setQuantity(random.nextInt(50) + 1);

                        em.persist(variant);
                    }
                }
            }
        }

        System.out.println("Products + Variants seeded (VND)");
    }
}