package blog.vans_story_be.domain.user.controller;

import blog.vans_story_be.domain.user.dto.UserDto;
import blog.vans_story_be.domain.user.service.UserService;
import blog.vans_story_be.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 관련 컨트롤러
 *
 * @author vans
 * @version 1.0.0
 * @since 2024.03.19
 */
@Tag(name = "User", description = "사용자 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(summary = "사용자 생성", description = "새로운 사용자를 생성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<UserDto.Response>> createUser(
            @Valid @RequestBody UserDto.CreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.createUser(request)));
    }

    @Operation(summary = "사용자 목록 조회", description = "모든 사용자 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDto.Response>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers()));
    }

    @Operation(summary = "사용자 조회", description = "특정 사용자를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto.Response>> getUser(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id)));
    }

    @Operation(summary = "사용자 정보 수정", description = "사용자 정보를 수정합니다.")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto.Response>> updateUser(
            @PathVariable("id") Long id,
            @Valid @RequestBody UserDto.UpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateUser(id, request)));
    }

    @Operation(summary = "사용자 삭제", description = "사용자를 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
} 