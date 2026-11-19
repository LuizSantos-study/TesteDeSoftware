package testesoftware.com.backend.e2e;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PdvE2ETest {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String FRONTEND_URL = "http://localhost:4200";

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        // Deixe visível para gravar o vídeo demonstrativo; descomente para rodar sem janela:
        // options.addArguments("--headless=new");
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void realizarLogin(String email, String senha) {
        driver.get(FRONTEND_URL + "/login");

        WebElement emailInput = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[formControlName='email']"))
        );
        WebElement senhaInput = driver.findElement(By.cssSelector("input[formControlName='senha']"));
        WebElement submitBtn = driver.findElement(By.cssSelector("button.primary-button[type='submit']"));

        emailInput.clear();
        emailInput.sendKeys(email);
        senhaInput.clear();
        senhaInput.sendKeys(senha);
        submitBtn.click();

        wait.until(ExpectedConditions.urlContains("/app/dashboard"));
    }

    // Caso 1: Login com credenciais válidas redireciona para o dashboard autenticado
    @Test
    void deveRealizarLoginComSucessoERedirecionarParaDashboard() {
        realizarLogin("lucas.caixa@farmacia.com", "123456");

        WebElement body = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.tagName("body"))
        );
        assertTrue(driver.getCurrentUrl().contains("/app/dashboard"));
        assertTrue(body.getText().contains("Lucas Lima"));
    }

    // Caso 2: Fluxo completo no PDV (Login -> Adicionar produto -> Criar venda -> Finalizar no modal)
    @Test
    void deveRegistrarEFinalizarVendaCompletaPeloPdv() {
        realizarLogin("lucas.caixa@farmacia.com", "123456");

        // Navega para a tela do PDV
        driver.get(FRONTEND_URL + "/app/pdv");

        // Aguarda o catálogo carregar e seleciona um produto não controlado (ex: Dipirona)
        WebElement produtoSelectEl = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector(".product-controls select"))
        );
        Select produtoSelect = new Select(produtoSelectEl);
        // Seleciona a opção que contém "Dipirona" (índice 4 no data.sql, ou busca pelo texto)
        produtoSelect.getOptions().stream()
            .filter(opt -> opt.getText().contains("Dipirona"))
            .findFirst()
            .ifPresent(WebElement::click);

        // Adiciona ao carrinho
        driver.findElement(By.cssSelector("button.add-button")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cart-list .cart-item")));

        // Seleciona a forma de pagamento (2º select dentro de .checkout-fields -> "Dinheiro em Espécie")
        List<WebElement> checkoutSelects = driver.findElements(By.cssSelector(".checkout-fields select"));
        Select pagamentoSelect = new Select(checkoutSelects.get(1));
        pagamentoSelect.selectByIndex(1);

        // Clica em "Criar venda pendente"
        WebElement criarVendaBtn = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector(".checkout-actions button.primary-button"))
        );
        criarVendaBtn.click();

        // No modal de confirmação, clica em "Finalizar venda"
        WebElement finalizarModalBtn = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector(".confirm-modal button.modal-button.primary"))
        );
        finalizarModalBtn.click();

        // Verifica a mensagem de sucesso na tela
        boolean finalizada = wait.until(
            ExpectedConditions.textToBePresentInElementLocated(
                By.cssSelector(".alert.success"),
                "Venda finalizada com sucesso."
            )
        );
        assertTrue(finalizada);
    }
}