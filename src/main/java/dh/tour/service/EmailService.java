package dh.tour.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String emailRemitente;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    // 1. Metodo para Bienvenida (El que te faltaba y causaba el error)
    public void enviarEmailBienvenida(String email, String nombre) {
        String contenido = "<h1>¡Bienvenido, " + nombre + "!</h1>" +
                "<p>Gracias por registrarte en Digital Tours. Explora nuestros mejores destinos.</p>";
        enviarHtml(email, "Bienvenido a Digital Tours", contenido);
    }

    // 2. Método para Recuperación (Con Link en lugar de solo texto)
    public void enviarEmailRecuperacion(String email, String token) {
        String url = frontendUrl + "/reset-password?token=" + token;
        String contenido = "<h3>Solicitud de cambio de contraseña</h3>" +
                "<p>Haz clic en el siguiente enlace para restablecer tu clave:</p>" +
                "<a href='" + url + "' style='background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>Restablecer Contraseña</a>" +
                "<p>Si no solicitaste este cambio, ignora este correo.</p>";

        enviarHtml(email, "Recuperar Contraseña - Digital Tours", contenido);
    }

    private void enviarHtml(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // IMPORTANTE: Si no has verificado un dominio, usa este remitente:
            helper.setFrom("onboarding@resend.dev");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("✅ ¡Increíble! Email enviado a través de Resend a: " + to);
        } catch (Exception e) {
            System.err.println("❌ Fallo en Resend: " + e.getMessage());
        }
    }
}