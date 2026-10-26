package com.clinica.limatambo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarCorreoRecuperacion(String destinatario, String enlaceRecuperacion) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(destinatario);
        mensaje.setSubject("Recuperación de Contraseña - Clínica Limatambo");
        mensaje.setText("Hola,\n\n" +
                "Has solicitado recuperar tu contraseña. Por favor, haz clic en el siguiente enlace para restablecerla:\n"
                +
                enlaceRecuperacion + "\n\n" +
                "Si no solicitaste esto, puedes ignorar este correo.\n\n" +
                "Atentamente,\n" +
                "Clínica Limatambo");

        mailSender.send(mensaje);
    }

    public void enviarCorreoCancelacionCita(String destinatario, String nombrePaciente, String fecha, String hora) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(destinatario);
        mensaje.setSubject("Aviso Importante: Cancelación de Cita Médica - Clínica Limatambo");
        mensaje.setText("Hola " + nombrePaciente + ",\n\n" +
                "Lamentamos informarte que tu cita médica programada para el día " + fecha + " a las " + hora + " ha sido cancelada por el médico debido a una urgencia médica inesperada.\n\n" +
                "Por favor, ingresa a nuestro portal para reprogramar tu cita lo antes posible.\n\n" +
                "Pedimos disculpas por los inconvenientes generados.\n\n" +
                "Atentamente,\n" +
                "Clínica Limatambo");
        mailSender.send(mensaje);
    }

    public void enviarBoletaConAdjunto(String destinatario, byte[] pdfAdjunto, String numeroBoleta) {
        try {
            jakarta.mail.internet.MimeMessage mensaje = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(mensaje, true);
            
            helper.setTo(destinatario);
            helper.setSubject("Tu Boleta Electrónica - Clínica Limatambo");
            helper.setText("Hola,\n\nGracias por atenderte en Clínica Limatambo. Adjunto encontrarás tu comprobante digital de atención médica (" + numeroBoleta + ").\n\nSaludos,\nClínica Limatambo.");
            
            helper.addAttachment(numeroBoleta + ".pdf", new org.springframework.core.io.ByteArrayResource(pdfAdjunto));
            
            mailSender.send(mensaje);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void enviarCorreoConfirmacionCita(String destinatario, String nombrePaciente, String fecha, String hora) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(destinatario);
        mensaje.setSubject("Cita Confirmada - Clínica Limatambo");
        mensaje.setText("Hola " + nombrePaciente + ",\n\n" +
                "Te escribimos para confirmar tu asistencia a la cita médica programada para el día " + fecha + " a las " + hora + ".\n\n" +
                "Te esperamos.\n\n" +
                "Atentamente,\n" +
                "Clínica Limatambo");
        mailSender.send(mensaje);
    }
}
