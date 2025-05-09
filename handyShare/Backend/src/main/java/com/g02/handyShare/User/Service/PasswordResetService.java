package com.g02.handyShare.User.Service;

import com.g02.handyShare.Constants;
import com.g02.handyShare.User.DTO.ForgotPasswordRequest;
import com.g02.handyShare.User.Entity.User;
import com.g02.handyShare.User.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private Constants constants;

    public String forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null) {
            return "User not found";
        }

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        long token_expiry_duration = 60 * 60 * 1000;
        user.setResetTokenExpiry(new Date(System.currentTimeMillis() + token_expiry_duration)); // 1 hour expiry
        userRepository.save(user);

        String resetLink = constants.FRONT_END_HOST + "/change-password?token=" + token;
        emailService.sendEmailToResetPassword(user.getEmail(), "Password Reset", resetLink);
        
        return "Password reset link sent to " + user.getEmail();
    }

    public String resetPassword(String token, String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return "Invalid or expired token"; // Return an appropriate message if password is empty
        }
        User user = userRepository.findByResetToken(token);
        if (user == null || user.getResetTokenExpiry().before(new Date())) {
            return "Invalid or expired token";
        }

        user.setPassword(new BCryptPasswordEncoder().encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        return "Password reset successful";
    }
}
