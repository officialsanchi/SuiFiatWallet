//package com.clyrafy.wallet.template;
//
//public class EmailTemplate {
//
//    public static String verifyEmailTemplate(String firstName, String verificationUrl) {
//        return String.format("""
//            Hi %s,
//
//            Welcome to ClyraFi — your programmable payments infrastructure.
//
//            Please confirm your email to access your dashboard and start building in sandbox mode.
//
//            👉 %s
//
//            – The ClyraFi Team
//            """, firstName, verificationUrl);
//    }
//
//    public static String emailVerifiedTemplate(String firstName, String dashboardUrl) {
//        return String.format("""
//            Hi %s,
//
//            Welcome — your sandbox is ready!
//
//            You can now explore the dashboard, test APIs, and start building.
//
//            👉 %s
//
//            – The ClyraFi Team
//            """, firstName, dashboardUrl);
//    }
//
//    public static String passwordResetTemplate(String firstName, String resetUrl) {
//        return String.format("""
//            Hi %s,
//
//            We received a request to reset your password.
//
//            Click the link below to reset it. This link will expire in 10 minutes.
//
//            👉 %s
//
//            If you did not request this, you can safely ignore this email.
//
//            – The ClyraFi Team
//            """, firstName, resetUrl);
//    }
//
//    public static String newLoginTemplate(String firstName, String secureAccountUrl) {
//        return String.format("""
//            Hi %s,
//
//            A new sign-in to your account was detected.
//
//            If this was you, no action is needed. If not, please secure your account immediately.
//
//            👉 %s
//
//            – The ClyraFi Team
//            """, firstName, secureAccountUrl);
//    }
//
//    public static String apiKeyCreatedTemplate(String firstName, String keysUrl) {
//        return String.format("""
//            Hi %s,
//
//            Your new sandbox API keys are ready.
//
//            You can view and manage them in your dashboard:
//
//            👉 %s
//
//            – The ClyraFi Team
//            """, firstName, keysUrl);
//    }
//}

package com.clyrafy.wallet.template;

public class EmailTemplate {

    private static String wrapHtml(String content) {
        return String.format("""
            <html>
              <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; background: #f9f9f9; padding: 20px;">
                <table width="100%%" cellpadding="0" cellspacing="0" style="max-width: 600px; margin: auto; background: white; border-radius: 8px; overflow: hidden;">
                  <tr style="background: #111827; color: white;">
                    <td style="padding: 16px; font-size: 18px; font-weight: bold; text-align: center;">
                      ClyraFi
                    </td>
                  </tr>
                  <tr>
                    <td style="padding: 24px;">
                      %s
                    </td>
                  </tr>
                  <tr style="background: #f3f4f6; font-size: 12px; color: #555;">
                    <td style="padding: 16px; text-align: center;">
                      © 2025 ClyraFi Inc. All rights reserved.<br>
                      Need help? <a href="mailto:support@clyrafi.com" style="color: #4CAF50; text-decoration: none;">Contact Support</a><br>
                      <a href="https://clyrafi.com/unsubscribe" style="color: #999; text-decoration: none;">Unsubscribe</a>
                    </td>
                  </tr>
                </table>
              </body>
            </html>
            """, content);
    }

    private static String wrapText(String content) {
        return String.format("""
            ClyraFi

            %s

            ---
            © 2025 ClyraFi Inc. All rights reserved.
            Need help? Email support@clyrafi.com
            Unsubscribe: https://clyrafi.com/unsubscribe
            """, content);
    }

    private static String baseHtml(String firstName, String message, String buttonText, String buttonUrl) {
        String content = String.format("""
            <p>Hi %s,</p>
            <p>%s</p>
            <p style="margin: 24px 0; text-align: center;">
              <a href="%s" style="background: #7B1FA2; color: white; padding: 12px 20px; text-decoration: none; border-radius: 6px; font-size: 14px;">
                %s
              </a>
            </p>
            <p>– The ClyraFi Team</p>
            """, firstName, message, buttonUrl, buttonText);

        return wrapHtml(content);
    }

    private static String baseText(String firstName, String message, String buttonText, String buttonUrl) {
        String content = String.format("""
            Hi %s,

            %s

            %s → %s

            – The ClyraFi Team
            """, firstName, message.replaceAll("<br><br>", "\n\n"), buttonText, buttonUrl);

        return wrapText(content);
    }

    // 🔹 Verify Email
    public static String verifyEmailHtml(String firstName, String verificationUrl) {
        return baseHtml(firstName,
                "Welcome to ClyraFi — your programmable payments infrastructure.<br><br>" +
                        "Please confirm your email to access your dashboard and start building in sandbox mode.",
                "Verify Email",
                verificationUrl);
    }
    public static String verifyEmailText(String firstName, String verificationUrl) {
        return baseText(firstName,
                "Welcome to ClyraFi — your programmable payments infrastructure.\n\n" +
                        "Please confirm your email to access your dashboard and start building in sandbox mode.",
                "Verify Email",
                verificationUrl);
    }

    // 🔹 Email Verified
    public static String emailVerifiedHtml(String firstName, String dashboardUrl) {
        return baseHtml(firstName,
                "Welcome — your sandbox is ready!<br><br>You can now explore the dashboard, test APIs, and start building.",
                "Open Dashboard",
                dashboardUrl);
    }
    public static String emailVerifiedText(String firstName, String dashboardUrl) {
        return baseText(firstName,
                "Welcome — your sandbox is ready!\n\nYou can now explore the dashboard, test APIs, and start building.",
                "Open Dashboard",
                dashboardUrl);
    }

    // 🔹 Password Reset
    public static String passwordResetHtml(String firstName, String resetUrl) {
        return baseHtml(firstName,
                "We received a request to reset your password.<br><br>This link will expire in 10 minutes.",
                "Reset Password",
                resetUrl);
    }
    public static String passwordResetText(String firstName, String resetUrl) {
        return baseText(firstName,
                "We received a request to reset your password.\n\nThis link will expire in 10 minutes.",
                "Reset Password",
                resetUrl);
    }

    // 🔹 New Login Alert
    public static String newLoginHtml(String firstName, String secureAccountUrl) {
        return baseHtml(firstName,
                "A new sign-in to your account was detected.<br><br>If this was you, no action is needed. If not, please secure your account immediately.",
                "Secure Account",
                secureAccountUrl);
    }
    public static String newLoginText(String firstName, String secureAccountUrl) {
        return baseText(firstName,
                "A new sign-in to your account was detected.\n\nIf this was you, no action is needed. If not, please secure your account immediately.",
                "Secure Account",
                secureAccountUrl);
    }

    // 🔹 API Key Created
    public static String apiKeyCreatedHtml(String firstName, String keysUrl) {
        return baseHtml(firstName,
                "Your new sandbox API keys are ready.<br><br>You can view and manage them in your dashboard:",
                "View Keys",
                keysUrl);
    }
    public static String apiKeyCreatedText(String firstName, String keysUrl) {
        return baseText(firstName,
                "Your new sandbox API keys are ready.\n\nYou can view and manage them in your dashboard:",
                "View Keys",
                keysUrl);
    }
}
