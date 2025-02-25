package blog.vans_story_be.domain.user.controller;
import blog.vans_story_be.domain.user.dto.UserDto;
import blog.vans_story_be.domain.user.service.UserService;
import blog.vans_story_be.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

import blog.vans_story_be.domain.auth.annotation.RequireApiKey;

/**
 * 사용자 관련 API를 처리하는 컨트롤러 클래스입니다.
 * <p>
 * 이 클래스는 사용자의 CRUD 작업을 처리하며,
 * RESTful API 엔드포인트를 제공합니다.
 * </p>
 *
 * @author vans
 * @version 1.0.0
 * @since 2024.03.19
 */
@Tag(name = "User", description = "사용자 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class UserController {
    private final UserService userService;


    /**
     * 새로운 사용자를 생성합니다.
     *
     * @param request 사용자 생성 요청 데이터
     * @return 생성된 사용자 정보와 함께 200 OK 응답
     */
    @Operation(summary = "사용자 생성", description = "새로운 사용자를 생성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<UserDto.Response>> createUser(
            @Valid @RequestBody UserDto.CreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.createUser(request)));
    }

    /**
     * 모든 사용자 목록을 조회합니다.
     *
     * @return 전체 사용자 목록과 함께 200 OK 응답
     */
    @Operation(summary = "사용자 목록 조회", description = "모든 사용자 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDto.Response>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers()));
    }

    /**
     * 특정 ID의 사용자를 조회합니다.
     *
     * @param id 조회할 사용자의 ID
     * @return 조회된 사용자 정보와 함께 200 OK 응답
     * @throws blog.vans_story_be.global.exception.CustomException 사용자를 찾을 수 없는 경우
     */
    @Operation(summary = "사용자 조회", description = "특정 사용자를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto.Response>> getUser(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id)));
    }

    /**
     * 특정 ID의 사용자 정보를 수정합니다.
     *
     * @param id 수정할 사용자의 ID
     * @param request 수정할 사용자 정보
     * @return 수정된 사용자 정보와 함께 200 OK 응답
     * @throws blog.vans_story_be.global.exception.CustomException 사용자를 찾을 수 없는 경우
     */
    @Operation(summary = "사용자 정보 수정", description = "사용자 정보를 수정합니다.")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto.Response>> updateUser(
            @PathVariable("id") Long id,
            @Valid @RequestBody UserDto.UpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateUser(id, request)));
    }

    /**
     * 특정 ID의 사용자를 삭제합니다.
     *
     * @param id 삭제할 사용자의 ID
     * @return 성공 응답과 함께 200 OK
     * @throws blog.vans_story_be.global.exception.CustomException 사용자를 찾을 수 없는 경우
     */
    @Operation(summary = "사용자 삭제", description = "사용자를 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 이메일로 사용자의 닉네임을 조회합니다.
     *
     * @param email 조회할 사용자의 이메일
     * @return 사용자의 닉네임 정보와 함께 200 OK 응답
     */
    @RequireApiKey
    @SecurityRequirement(name = "ApiKeyAuth")
    @Operation(
        summary = "이메일로 닉네임 조회", 
        description = "이메일로 사용자의 닉네임을 조회합니다."
    )
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<String>> getNicknameByEmail(
        @Parameter(
            name = "email",
            description = "조회할 사용자의 이메일",
            example = "user@example.com",
            in = ParameterIn.PATH,
            required = true
        )
        @PathVariable("email") String email
    ) {
        return ResponseEntity.ok(ApiResponse.success(userService.getNicknameByEmail(email)));
    }
} 