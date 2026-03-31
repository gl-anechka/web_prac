package web_prac.DAO.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import web_prac.model.Partner;
import web_prac.model.Place;
import web_prac.model.Product;
import web_prac.model.ProductType;
import web_prac.model.Reception;
import web_prac.model.Supply;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Sql(
        statements = "DROP ALL OBJECTS",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)
)
@Sql(
        scripts = {"/sql/create.sql", "/sql/init.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED, encoding = "UTF-8")
)
abstract class DaoTestSupport {

    @PersistenceContext
    protected EntityManager entityManager;

    protected ProductType productType(int id) {
        return entityManager.find(ProductType.class, id);
    }

    protected Product product(int id) {
        return entityManager.find(Product.class, id);
    }

    protected Partner partner(int id) {
        return entityManager.find(Partner.class, id);
    }

    protected Place place(int id) {
        return entityManager.find(Place.class, id);
    }

    protected Supply supply(int id) {
        return entityManager.find(Supply.class, id);
    }

    protected Reception reception(int id) {
        return entityManager.find(Reception.class, id);
    }
}
