package ru.netology.test;

import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.chrome.ChromeOptions;
import ru.netology.data.DataHelper;
import ru.netology.data.DatabaseUtils;
import ru.netology.page.LoginPage;

import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginTest {

    @BeforeAll
    static void setUpAll() {
        ChromeOptions options = new ChromeOptions();
        // Отключаем проверку утечек и включаем режим инкогнито
        options.addArguments("--disable-features=PasswordLeakDetection");
        options.addArguments("--incognito");
        Configuration.browserCapabilities = options;
    }

    @BeforeEach
    void setUp() {
        open("http://localhost:9999");
        // Даем базе немного времени, чтобы очиститься
        DatabaseUtils.cleanDatabase();
    }

    @Test
    void shouldLoginSuccessfullyWithCodeFromDb() {
        var authInfo = DataHelper.getValidAuthInfo();
        var verificationPage = new LoginPage().validLogin(authInfo.getLogin(), authInfo.getPassword());

        String codeFromDb = DatabaseUtils.getAuthCode(authInfo.getLogin());
        var dashboardPage = verificationPage.validVerify(codeFromDb);

        // Небольшая пауза, чтобы страница успела прогрузиться
        try { Thread.sleep(2000); } catch (InterruptedException e) {}

        assertTrue(dashboardPage.isDashboardVisible(), "Должна открыться главная страница");
    }

    @Test
    void shouldBlockAfterThreeFailedAttempts() {
        var authInfo = DataHelper.getValidAuthInfo();
        var verificationPage = new LoginPage().validLogin(authInfo.getLogin(), authInfo.getPassword());

        for (int i = 0; i < 3; i++) {
            verificationPage = verificationPage.invalidVerify("000000");
        }

        String errorText = verificationPage.getErrorMessage();
        assertTrue(errorText.contains("блокировк") || errorText.contains("заблокирован"),
                "Должно появиться сообщение о блокировке. Фактический текст: " + errorText);
    }
}