package edu.cit.riconalla.dentalink.service;

import edu.cit.riconalla.dentalink.dto.RegisterRequest;
import edu.cit.riconalla.dentalink.dto.UpdateProfileRequest;
import edu.cit.riconalla.dentalink.dto.UserDto;
import edu.cit.riconalla.dentalink.entity.Role;
import edu.cit.riconalla.dentalink.entity.User;
import edu.cit.riconalla.dentalink.exception.EmailAlreadyExistsException;
import edu.cit.riconalla.dentalink.exception.InvalidCredentialsException;
import edu.cit.riconalla.dentalink.exception.ResourceNotFoundException;
import edu.cit.riconalla.dentalink.repository.UserRepository;
import edu.cit.riconalla.dentalink.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class UserService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB — C-11
    private static final List<String> ALLOWED_TYPES =
            Arrays.asList("image/jpeg", "image/png");

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final SupabaseStorageService storageService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository,
                       JwtUtil jwtUtil,
                       SupabaseStorageService storageService) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.storageService = storageService;
    }

    /**
     * Registers a new PATIENT account.
     * Returns the saved User so AuthService can generate a JWT and build the response.
     */
    public User registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.PATIENT);
        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    /**
     * Returns a UserDto for the authenticated user.
     * Used by GET /auth/me.
     */
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return new UserDto(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole().name(),
                user.getProfileImageUrl()
        );
    }

    /**
     * PUT /users/me/profile — SDD §5.3
     * Updates firstName, lastName, and optionally password.
     * Email is never updated (C-4).
     * currentPassword is required for any change.
     */
    public UserDto updateProfile(String callerEmail, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(callerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // currentPassword required — validates identity before any change
        if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
            throw new IllegalArgumentException("Current password is required");
        }
        if (user.getPassword() == null
                || !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        // Update name fields if provided
        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            user.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            user.setLastName(request.getLastName().trim());
        }

        // Update password if newPassword provided — min 8 chars
        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            if (request.getNewPassword().length() < 8) {
                throw new IllegalArgumentException("New password must be at least 8 characters");
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        User saved = userRepository.save(user);
        return new UserDto(
                saved.getUserId(),
                saved.getFirstName(),
                saved.getLastName(),
                saved.getEmail(),
                saved.getRole().name(),
                saved.getProfileImageUrl()
        );
    }

    /**
     * POST /users/me/upload-profile-picture — SDD §5.3
     * Validates file type (JPEG/PNG) and size (≤5 MB).
     * Uploads to Supabase Storage, persists URL. Returns profileImageUrl.
     */
    public String uploadProfilePicture(String callerEmail, MultipartFile file) throws IOException {
        // Validate file type — SDD §2.4, §3.2
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Invalid file type. Only JPEG and PNG are allowed.");
        }

        // Validate file size — C-11 (5 MB max)
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds the 5 MB limit.");
        }

        User user = userRepository.findByEmail(callerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String imageUrl = storageService.uploadFile(file.getBytes(), fileName);

        user.setProfileImageUrl(imageUrl);
        userRepository.save(user);

        return imageUrl;
    }
}