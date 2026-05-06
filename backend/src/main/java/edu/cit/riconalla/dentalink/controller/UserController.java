package edu.cit.riconalla.dentalink.controller;

import edu.cit.riconalla.dentalink.dto.ApiResponse;
import edu.cit.riconalla.dentalink.dto.UpdateProfileRequest;
import edu.cit.riconalla.dentalink.dto.UserDto;
import edu.cit.riconalla.dentalink.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * PUT /api/v1/users/me/profile — Bearer JWT (PATIENT) — SDD §5.3
     * Updates firstName, lastName, optionally password.
     * Email is NOT updatable (C-4).
     * currentPassword is required.
     */
    @PutMapping("/me/profile")
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(
            @RequestBody UpdateProfileRequest request,
            Authentication auth
    ) {
        UserDto updated = userService.updateProfile(auth.getName(), request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * POST /api/v1/users/me/upload-profile-picture — Bearer JWT (PATIENT) — SDD §5.3
     * Content-Type: multipart/form-data
     * Form field: file (JPEG or PNG, max 5 MB)
     */
    @PostMapping(value = "/me/upload-profile-picture", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadProfilePicture(
            @RequestParam("file") MultipartFile file,
            Authentication auth
    ) {
        try {
            String imageUrl = userService.uploadProfilePicture(auth.getName(), file);
            return ResponseEntity.ok(ApiResponse.success(Map.of("profileImageUrl", imageUrl)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("INVALID_FILE", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("UPLOAD_FAILED", e.getMessage()));
        }
    }
}