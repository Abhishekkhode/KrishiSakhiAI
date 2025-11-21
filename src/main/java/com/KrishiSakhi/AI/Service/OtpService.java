//package com.KrishiSakhi.AI.Service;
//
//import com.KrishiSakhi.AI.Model.Otp;
//import com.KrishiSakhi.AI.Model.User;
//import com.KrishiSakhi.AI.Repository.OtpRepository;
//import com.KrishiSakhi.AI.Repository.UserRepository;
//import com.twilio.Twilio;
//import com.twilio.exception.ApiException; // Import the specific Twilio exception
//import com.twilio.rest.api.v2010.account.Message;
//import com.twilio.type.PhoneNumber;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import java.time.LocalDateTime;
//import java.util.Optional;
//import java.util.Random;
//
//@Service
//public class OtpService {
//
//    private final OtpRepository otpRepository;
//    private final UserRepository userRepository;
//
//    @Value("${twilio.account.sid}")
//    private String accountSid;
//    @Value("${twilio.auth.token}")
//    private String authToken;
//    @Value("${twilio.phone.number}")
//    private String twilioPhoneNumber;
//
//    // Static block to initialize Twilio upon service creation
//    public OtpService(OtpRepository otpRepository, UserRepository userRepository) {
//        this.otpRepository = otpRepository;
//        this.userRepository = userRepository;
//    }
//
//    public String generateAndSendOtp(String phoneNumber) {
//        // --- STEP 1: INPUT VALIDATION (FIX FOR THE ERROR) ---
//        if (phoneNumber == null || !phoneNumber.startsWith("+") || phoneNumber.length() < 10) {
//            throw new IllegalArgumentException("Phone number must be in E.164 format (e.g., +91XXXXXXXXXX).");
//        }
//
//        // Generate 6-digit OTP
//        String otpCode = String.format("%06d", new Random().nextInt(999999));
//
//        // Save OTP to DB with 5-minute expiry
//        Otp otp = new Otp();
//        otp.setPhoneNumber(phoneNumber);
//        otp.setOtpCode(otpCode);
//        otp.setExpiryTime(LocalDateTime.now().plusMinutes(5));
//        otpRepository.save(otp);
//
//        // --- STEP 2: REAL-TIME TWILIO SMS SENDING ---
//        try {
//            // Initialize Twilio client
//            Twilio.init(accountSid, authToken);
//
//            Message message = Message.creator(
//                    new PhoneNumber(phoneNumber), // To number (must be E.164 format)
//                    new PhoneNumber(twilioPhoneNumber), // From Twilio number
//                    "Your Krishi Sakhi verification code is: " + otpCode
//            ).create();
//
//            return "OTP sent successfully to " + phoneNumber;
//
//        } catch (ApiException e) {
//            // Catch Twilio API exceptions (e.g., invalid phone number, bad credentials)
//            otpRepository.delete(otp); // Clean up the OTP if sending failed
//
//            // Re-throw a specific error based on the Twilio exception message
//            if (e.getMessage().contains("Invalid 'To' Phone Number")) {
//                throw new RuntimeException("Twilio Error: Invalid phone number format or target number not supported. Ensure E.164 format.");
//            }
//            if (e.getMessage().contains("Authenticate")) {
//                throw new RuntimeException("Twilio Error: Authentication failed. Check your ACCOUNT_SID and AUTH_TOKEN.");
//            }
//            throw new RuntimeException("Twilio Error: Failed to send SMS. Check logs.");
//        }
//    }
//
//    // --- Verify OTP (Logic remains the same) ---
//    public User verifyOtp(String phoneNumber, String otpCode) throws Exception {
//        Optional<Otp> otpOptional = otpRepository.findByPhoneNumberAndOtpCode(phoneNumber, otpCode);
//
//        if (otpOptional.isPresent()) {
//            Otp otp = otpOptional.get();
//            if (otp.getExpiryTime().isAfter(LocalDateTime.now())) {
//
//                User user = userRepository.findByPhoneNumber(phoneNumber).orElseGet(() -> {
//                    // Create new user if not found
//                    User newUser = new User();
//                    newUser.setPhoneNumber(phoneNumber);
//                    newUser.setUsername("user_" + phoneNumber.substring(phoneNumber.length() - 4));
//                    return userRepository.save(newUser);
//                });
//
//                user.setVerified(true);
//                userRepository.save(user);
//                otpRepository.delete(otp); // OTP consumed
//                return user;
//            }
//        }
//        throw new Exception("OTP is invalid or expired.");
//    }
//}











package com.KrishiSakhi.AI.Service;

import com.KrishiSakhi.AI.Model.Otp;
import com.KrishiSakhi.AI.Model.User;
import com.KrishiSakhi.AI.Repository.OtpRepository;
import com.KrishiSakhi.AI.Repository.UserRepository;
import com.twilio.Twilio;
import com.twilio.exception.ApiException; // Import the specific Twilio exception
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
@Service
public class OtpService {

    private final OtpRepository otpRepository;
    private final UserRepository userRepository;

    @Value("${twilio.account.sid}")
    private String accountSid;
    @Value("${twilio.auth.token}")
    private String authToken;
    @Value("${twilio.phone.number}")
    private String twilioPhoneNumber;

    public OtpService(OtpRepository otpRepository, UserRepository userRepository) {
        this.otpRepository = otpRepository;
        this.userRepository = userRepository;
    }

    // --- UPDATED METHOD: Handles New Registration OR OTP Resend ---
//    @Transactional
//    public String generateAndSendOtp(String phoneNumber, String username) {
//        // Validation logic handles E.164 format check (from DTO)
//
//        String otpCode = String.format("%06d", new Random().nextInt(999999));
//
//        // --- 1. HANDLE USER CREATION/EXISTENCE (NEW vs RESEND) ---
//        Optional<User> existingUser = userRepository.findByPhoneNumber(phoneNumber);
//
//        User user = existingUser.orElseGet(() -> {
//            // If user doesn't exist, create a temporary unverified record (New Registration)
//            User newUser = new User();
//            newUser.setPhoneNumber(phoneNumber);
//            newUser.setUsername(username);
//            return userRepository.save(newUser);
//        });
//
//        // If the user exists and is already verified, we might stop the OTP process here,
//        // but for now, we assume this is a necessary step for security on every login.
//
//        // --- 2. HANDLE OTP RECORD (RESEND LOGIC) ---
//        // Find if an existing OTP record exists for this phone number
//        Optional<Otp> existingOtp = otpRepository.findByPhoneNumber(phoneNumber);
//        Otp otp = existingOtp.orElseGet(Otp::new); // Reuse the existing record or create a new one
//
//        // Update the OTP record with new data
//        otp.setPhoneNumber(phoneNumber);
//        otp.setOtpCode(otpCode);
//        otp.setExpiryTime(LocalDateTime.now().plusMinutes(5));
//        otpRepository.save(otp);
//
//        // --- 3. REAL-TIME TWILIO SMS SENDING ---
//        try {
//            Twilio.init(accountSid, authToken);
//
//            Message.creator(
//                    new PhoneNumber(phoneNumber),
//                    new PhoneNumber(twilioPhoneNumber),
//                    "Your Krishi Sakhi verification code is: " + otpCode + ". This code replaces any previous codes."
//            ).create();
//
//            return "New OTP sent successfully to " + phoneNumber;
//
//        } catch (ApiException e) {
//            // If Twilio fails, delete the newly created/updated OTP record
//            otpRepository.delete(otp);
//            throw new RuntimeException("Twilio Error: Failed to send SMS. Check logs.", e);
//        }
//    }
    @Transactional
    public String generateAndSendOtp(String phoneNumber, String tempUsername) {
        // NOTE: Input validation is handled by the @Pattern annotation in the DTO before this method is called.

        String otpCode = String.format("%06d", new Random().nextInt(999999));

        // --- 1. HANDLE USER CREATION/EXISTENCE (STEP 1 LOGIC) ---
        userRepository.findByPhoneNumber(phoneNumber).ifPresentOrElse(
                (user) -> { /* User exists, do nothing */ },
                () -> {
                    // If user doesn't exist, create a temporary unverified record
                    User newUser = new User();
                    newUser.setPhoneNumber(phoneNumber);
                    newUser.setUsername(tempUsername); // Set the temporary username
                    userRepository.save(newUser);
                }
        );

        // --- 2. HANDLE OTP RECORD (RESEND/UPDATE LOGIC) ---
        // Find if an existing OTP record exists for this phone number (for resend)
        Optional<Otp> existingOtp = otpRepository.findByPhoneNumber(phoneNumber);
        Otp otp = existingOtp.orElseGet(Otp::new); // Reuse the existing record or create a new one

        // Update the OTP record with new data and expiry time
        otp.setPhoneNumber(phoneNumber);
        otp.setOtpCode(otpCode);
        otp.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        otpRepository.save(otp);

        // --- 3. REAL-TIME TWILIO SMS SENDING ---
        try {
            Twilio.init(accountSid, authToken);

            Message.creator(
                    new PhoneNumber(phoneNumber),
                    new PhoneNumber(twilioPhoneNumber),
                    "Your Krishi Sakhi verification code is: " + otpCode + ". This code replaces any previous codes."
            ).create();

            return "New OTP sent successfully to " + phoneNumber;

        } catch (ApiException e) {
            // If Twilio fails (bad format, bad credentials), delete the newly created/updated OTP record
            otpRepository.delete(otp);
            // Re-throw as a RuntimeException for the controller to catch and return a 400 response
            throw new RuntimeException("Twilio Error: Failed to send SMS. Check credentials or number format.", e);
        }
    }

    // --- VERIFY OTP METHOD remains the same ---
    // ... (VerifyOtp logic) ...
    public User verifyOtp(String phoneNumber, String otpCode) throws Exception {
        Optional<Otp> otpOptional = otpRepository.findByPhoneNumberAndOtpCode(phoneNumber, otpCode);

        if (otpOptional.isPresent()) {
            Otp otp = otpOptional.get();
            if (otp.getExpiryTime().isAfter(LocalDateTime.now())) {

                User user = userRepository.findByPhoneNumber(phoneNumber)
                        .orElseThrow(() -> new RuntimeException("User profile not found. Please regenerate OTP."));

                user.setVerified(true);
                userRepository.save(user);
                otpRepository.delete(otp);
                return user;
            }
        }
        throw new Exception("OTP is invalid or expired.");
    }
}