package web_prac.web;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.Select;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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
class SeleniumSystemTests {

    @LocalServerPort
    private int port;

    private WebDriver driver;

    @BeforeEach
    void setUp() {
        driver = new HtmlUnitDriver(false);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void partnerRegistrationKeepsEnteredValuesAndShowsValidationErrors() {
        open("/partners/new");

        selectByValue("type", "PROVIDER");
        fill("name", "ООО Тестовый поставщик");
        fill("address", "Москва, Тестовая, 1");
        fill("phone", "phone");
        fill("email", "wrong-email");
        submitPrimary();

        verifyPageContains("Телефон должен содержать от 10 до 12 цифр");
        verifyPageContains("E-mail должен содержать @");
        verifyInputValue("name", "ООО Тестовый поставщик");
        verifyInputValue("phone", "phone");
        verifyInputValue("email", "wrong-email");

        fill("phone", "79991234567");
        fill("email", "test-provider@example.com");
        submitPrimary();

        verifyCurrentPath("/partners");
        verifyPageContains("ООО Тестовый поставщик");
        verifyPageContains("79991234567");
    }

    @Test
    void placeCreationKeepsValuesAfterDuplicateErrorAndThenAddsPlace() {
        open("/places/new");

        fill("roomNum", "1");
        fill("shelfNum", "1");
        fill("kgLimit", "99");
        submitPrimary();

        verifyPageContains("Такое место хранения уже существует");
        verifyInputValue("roomNum", "1");
        verifyInputValue("shelfNum", "1");
        verifyNumericInputValue("kgLimit", "99");

        fill("roomNum", "9");
        fill("shelfNum", "4");
        fill("kgLimit", "99");
        submitPrimary();

        verifyCurrentPath("/places");
        verifyPageContains("Место хранения успешно добавлено");
        verifyPageContains("9");
        verifyPageContains("4");
        verifyPageContains("99");
    }

    @Test
    void productCanBeCreatedFoundAndDeletedWhenUnused() {
        open("/products/new");

        fill("title", "Тестовый товар Selenium");
        selectByText("productTypeId", "Продукты");
        selectByValue("unit", "PCS");
        fill("kgPerUnit", "0.50");
        submitPrimary();

        verifyCurrentPath("/products");
        verifyPageContains("Тестовый товар Selenium");

        fill("title", "Тестовый товар Selenium");
        submitButton("Найти");
        verifyPageContains("Тестовый товар Selenium");

        submitDeleteForRow("Тестовый товар Selenium");
        verifyPageContains("Товар удален");
        verifyPageDoesNotContain("Тестовый товар Selenium");
    }

    @Test
    void productFiltersWorkByTypeProviderStockStatusAndPlace() {
        open("/products");
        selectByText("typeId", "Продукты");
        submitButton("Найти");
        verifyTableContains("Молоко 1л пастеризованное");
        verifyTableDoesNotContain("Чайник электрический");

        open("/products");
        selectByText("providerId", "ООО \"Молочный мир\"");
        submitButton("Найти");
        verifyTableContains("Сахар-песок");
        verifyTableDoesNotContain("Чайник электрический");

        open("/products");
        driver.findElement(By.name("inStockOnly")).click();
        submitButton("Найти");
        verifyTableContains("Чайник электрический");
        verifyTableDoesNotContain("Кроссовки (пара)");

        open("/products");
        selectByValue("status", "SPOILED");
        submitButton("Найти");
        verifyTableContains("Сыр твердый");
        verifyTableDoesNotContain("Молоко 1л пастеризованное");

        open("/products");
        selectByText("placeId", "Комната 1 / полка 3");
        submitButton("Найти");
        verifyTableContains("Чайник электрический");
        verifyTableDoesNotContain("Молоко 1л пастеризованное");
    }

    @Test
    void productCanBeEdited() {
        open("/products");

        clickEditForRow("Кроссовки (пара)");
        fill("title", "Кроссовки Selenium");
        submitPrimary();

        verifyCurrentPath("/products");
        verifyPageContains("Товар успешно обновлен");
        verifyTableContains("Кроссовки Selenium");
        verifyTableDoesNotContain("Кроссовки (пара)");
    }

    @Test
    void usedProductDeleteShowsErrorAndHistoryStaysVisible() {
        open("/products");

        submitDeleteForRow("Молоко 1л пастеризованное");

        verifyPageContains("Нельзя удалить товар");
        verifyPageContains("Молоко 1л пастеризованное");

        open("/operations");
        verifyPageContains("Молоко 1л пастеризованное");
    }

    @Test
    void operationFiltersWorkByKindPeriodPartnerAndProduct() {
        open("/operations");
        selectByValue("kind", "SUPPLY");
        submitButton("Найти");
        verifyEveryTableRowContains("Поставка");
        verifyNoTableRowContains("Выдача");

        open("/operations");
        selectByValue("kind", "RECEPTION");
        selectByText("productId", "Чайник электрический");
        submitButton("Найти");
        verifyEveryTableRowContains("Выдача");
        verifyEveryTableRowContains("Чайник электрический");
        verifyNoTableRowContains("Молоко 1л пастеризованное");

        open("/operations");
        selectByText("partnerId", "Кафе \"ЛанчБокс\"");
        submitButton("Найти");
        verifyEveryTableRowContains("Кафе \"ЛанчБокс\"");
        verifyTableContains("Сыр твердый");

        open("/operations?from=2026-02-12T00%3A00&to=2026-02-12T23%3A59");
        verifyTableContains("Стиральный порошок");
        verifyTableDoesNotContain("Чайник электрический");
    }

    @Test
    void supplyChecksFreeSpaceShowsErrorAndCreatesValidSupply() {
        open("/operations/new-supply");

        selectByText("partnerId", "ООО \"Молочный мир\"");
        selectByText("productId", "Молоко 1л пастеризованное");
        fill("time", dateTime(1));
        fill("expiresAt", dateTime(20));
        fill("amount", "100000");
        submitButton("Проверить наличие места");

        verifyPageContains("недостаточно свободного места");
        verifyNumericInputValue("amount", "100000");

        fill("amount", "5");
        submitButton("Проверить наличие места");
        verifyPageContains("Свободное место найдено");
        submitButton("Подтвердить");

        verifyCurrentPath("/operations");
        verifyPageContains("Поставка успешно оформлена");
        verifyPageContains("Молоко 1л пастеризованное");
        verifyPageContains("ООО \"Молочный мир\"");
    }

    @Test
    void receptionShowsInsufficientStockErrorAndCreatesValidReception() {
        open("/operations/new-reception");

        selectByText("partnerId", "Кафе \"ЛанчБокс\"");
        selectByText("productId", "Сахар-песок");
        fill("time", dateTime(1));
        fill("amount", "1");
        submitButton("Подтвердить");

        verifyPageContains("На складе недостаточно товара для выдачи");
        verifyNumericInputValue("amount", "1");

        selectByText("productId", "Чайник электрический");
        fill("amount", "1");
        submitButton("Подтвердить");

        verifyCurrentPath("/operations");
        verifyPageContains("Выдача успешно оформлена");
        verifyPageContains("Кафе \"ЛанчБокс\"");
        verifyPageContains("Чайник электрический");
    }

    @Test
    void partnerCanBeEditedDeletedAndUsedPartnerCannotBeDeleted() {
        open("/partners/new");
        selectByValue("type", "CONSUMER");
        fill("name", "Партнер для редактирования");
        fill("address", "Москва");
        fill("phone", "79990000111");
        fill("email", "edit@example.com");
        submitPrimary();

        verifyCurrentPath("/partners");
        verifyTableContains("Партнер для редактирования");

        clickEditForRow("Партнер для редактирования");
        fill("name", "Партнер после редактирования");
        fill("phone", "79990000222");
        submitPrimary();

        verifyTableContains("Партнер после редактирования");
        verifyTableContains("79990000222");
        submitDeleteForRow("Партнер после редактирования");
        verifyPageContains("Партнер удален");
        verifyTableDoesNotContain("Партнер после редактирования");

        submitDeleteForRow("ООО \"Молочный мир\"");
        verifyPageContains("Нельзя удалить партнера");
        verifyTableContains("ООО \"Молочный мир\"");
    }

    @Test
    void placesCanBeFilteredEditedDeletedAndUsedPlaceCannotBeDeleted() {
        open("/places");
        fill("roomNum", "2");
        submitButton("Найти");
        verifyEveryTableRowCellEquals(0, "2");
        verifyNoTableRowContains("200.00");

        open("/places/new");
        fill("roomNum", "9");
        fill("shelfNum", "8");
        fill("kgLimit", "77");
        submitPrimary();

        verifyCurrentPath("/places");
        WebElement createdRow = findRowContainingAll("9", "8", "77");
        createdRow.findElement(By.cssSelector("a.button.small")).click();
        fill("shelfNum", "9");
        fill("kgLimit", "88");
        submitPrimary();

        verifyTableContains("88.00");
        WebElement editedRow = findRowContainingAll("9", "9", "88.00");
        editedRow.findElement(By.cssSelector("button.danger[type='submit']")).click();
        verifyPageContains("Место хранения удалено");
        verifyTableDoesNotContain("88.00");

        WebElement usedPlace = findRowByCells("1", "1");
        usedPlace.findElement(By.cssSelector("button.danger[type='submit']")).click();
        verifyPageContains("Нельзя удалить место хранения");
        assertTrue(findRowByCells("1", "1").isDisplayed());
    }

    @Test
    void homePageShowsStatisticsExpiringProductsAndRecentOperations() {
        open("/");

        verifyPageContains("Складской учет");
        verifyPageContains("Всего товаров");
        verifyPageContains("7");
        verifyPageContains("Скоро истекает срок годности");
        verifyPageContains("Молоко 1л пастеризованное");
        verifyPageContains("Последние операции");
        verifyPageContains("Сеть магазинов \"У дома\"");
    }

    private void open(String path) {
        driver.get("http://localhost:" + port + path);
    }

    private void fill(String name, String value) {
        WebElement input = driver.findElement(By.name(name));
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(value);
    }

    private void selectByValue(String name, String value) {
        new Select(driver.findElement(By.name(name))).selectByValue(value);
    }

    private void selectByText(String name, String text) {
        new Select(driver.findElement(By.name(name))).selectByVisibleText(text);
    }

    private void submitPrimary() {
        driver.findElement(By.cssSelector("button.primary[type='submit']")).click();
    }

    private void submitButton(String text) {
        driver.findElements(By.cssSelector("button[type='submit']")).stream()
                .filter(button -> button.getText().trim().equals(text))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Submit button not found: " + text))
                .click();
    }

    private void submitDeleteForRow(String rowText) {
        WebElement row = findRowContaining(rowText);
        row.findElement(By.cssSelector("button.danger[type='submit']")).click();
    }

    private void clickEditForRow(String rowText) {
        WebElement row = findRowContaining(rowText);
        row.findElement(By.cssSelector("a.button.small")).click();
    }

    private WebElement findRowContaining(String text) {
        List<WebElement> rows = driver.findElements(By.cssSelector("tbody tr"));
        return rows.stream()
                .filter(row -> row.getText().contains(text))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Row not found: " + text));
    }

    private WebElement findRowContainingAll(String... texts) {
        List<WebElement> rows = driver.findElements(By.cssSelector("tbody tr"));
        return rows.stream()
                .filter(row -> {
                    String rowText = row.getText();
                    for (String text : texts) {
                        if (!rowText.contains(text)) {
                            return false;
                        }
                    }
                    return true;
                })
                .findFirst()
                .orElseThrow(() -> new AssertionError("Row not found"));
    }

    private WebElement findRowByCells(String firstCell, String secondCell) {
        return driver.findElements(By.cssSelector("tbody tr")).stream()
                .filter(row -> {
                    List<WebElement> cells = row.findElements(By.cssSelector("td"));
                    return cells.size() >= 2
                            && cells.get(0).getText().trim().equals(firstCell)
                            && cells.get(1).getText().trim().equals(secondCell);
                })
                .findFirst()
                .orElseThrow(() -> new AssertionError("Row not found by cells: " + firstCell + ", " + secondCell));
    }

    private void verifyPageContains(String text) {
        assertTrue(driver.getPageSource().contains(text), "Expected page to contain: " + text);
    }

    private void verifyPageDoesNotContain(String text) {
        assertFalse(driver.getPageSource().contains(text), "Expected page not to contain: " + text);
    }

    private void verifyTableContains(String text) {
        assertTrue(tableText().contains(text), "Expected table to contain: " + text);
    }

    private void verifyTableDoesNotContain(String text) {
        assertFalse(tableText().contains(text), "Expected table not to contain: " + text);
    }

    private void verifyEveryTableRowContains(String text) {
        List<WebElement> rows = nonEmptyTableRows();
        assertFalse(rows.isEmpty(), "Expected table to have rows");
        for (WebElement row : rows) {
            assertTrue(row.getText().contains(text), "Expected row to contain " + text + ": " + row.getText());
        }
    }

    private void verifyNoTableRowContains(String text) {
        for (WebElement row : nonEmptyTableRows()) {
            assertFalse(row.getText().contains(text), "Expected row not to contain " + text + ": " + row.getText());
        }
    }

    private void verifyEveryTableRowCellEquals(int cellIndex, String value) {
        List<WebElement> rows = nonEmptyTableRows();
        assertFalse(rows.isEmpty(), "Expected table to have rows");
        for (WebElement row : rows) {
            List<WebElement> cells = row.findElements(By.cssSelector("td"));
            assertTrue(cells.size() > cellIndex, "Expected row to have cell index " + cellIndex);
            assertEquals(value, cells.get(cellIndex).getText().trim());
        }
    }

    private List<WebElement> nonEmptyTableRows() {
        return driver.findElements(By.cssSelector("tbody tr")).stream()
                .filter(row -> !row.getText().contains("не найдены"))
                .filter(row -> !row.getText().contains("пока нет"))
                .toList();
    }

    private String tableText() {
        return driver.findElement(By.cssSelector("table")).getText();
    }

    private void verifyInputValue(String name, String value) {
        assertTrue(
                value.equals(driver.findElement(By.name(name)).getAttribute("value")),
                "Expected input " + name + " to keep value " + value
        );
    }

    private void verifyNumericInputValue(String name, String value) {
        String actual = driver.findElement(By.name(name)).getAttribute("value");
        assertTrue(
                Double.compare(Double.parseDouble(value), Double.parseDouble(actual)) == 0,
                "Expected input " + name + " to keep numeric value " + value + ", actual: " + actual
        );
    }

    private void verifyCurrentPath(String path) {
        assertTrue(driver.getCurrentUrl().contains(path), "Expected URL to contain path: " + path);
    }

    private String dateTime(int daysFromNow) {
        return LocalDateTime.now()
                .plusDays(daysFromNow)
                .withSecond(0)
                .withNano(0)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
    }
}
