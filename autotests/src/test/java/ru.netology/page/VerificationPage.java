package ru.netology.page;

import com.codeborne.selenide.SelenideElement;
import static com.codeborne.selenide.Selenide.$;

public class VerificationPage {
    private final SelenideElement codeField = $("[data-test-id='code'] input");
    private final SelenideElement verifyButton = $("[data-test-id='action-verify']");
    private final SelenideElement errorMessage = $("[data-test-id='error-notification']");

    public DashboardPage validVerify(String code) {
        codeField.setValue(code);
        verifyButton.click();
        return new DashboardPage();
    }

    public VerificationPage invalidVerify(String code) {
        codeField.setValue(code);
        verifyButton.click();
        return this;
    }

    public String getErrorMessage() {
        return errorMessage.getText();
    }
}