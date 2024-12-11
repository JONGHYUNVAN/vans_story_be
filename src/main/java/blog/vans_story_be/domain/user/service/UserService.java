package blog.vans_story_be.domain.user.service;

import blog.vans_story_be.domain.user.dto.UserDto;
import blog.vans_story_be.domain.user.entity.Role;
import blog.vans_story_be.domain.user.entity.User;
import blog.vans_story_be.domain.user.mapper.UserMapper;
import blog.vans_story_be.domain.user.repository.UserRepository;
import blog.vans_story_be.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 사용자 관련 비즈니스 로직을 처리하는 서비스 클래스
 * 사용자 생성, 조회, 수정, 삭제 기능을 제공합니다.
 * 
 * @author vans
 * @version 1.0.0
 * @since 2024.12.04
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 관리자 계정을 생성합니다.
     * 
     * @param request {@link UserDto.CreateRequest} 사용자 생성 요청 정보
     * @return {@link UserDto.Response} 생성된 관리자 정보
     */
    @Transactional
    public UserDto.Response createAdmin(UserDto.CreateRequest request) {
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.ADMIN);
        
        userRepository.save(user);
        return userMapper.toDto(user);
    }

    /**
     * 일반 사용자 계정을 생성합니다.
     * 
     * @param request {@link UserDto.CreateRequest} 사용자 생성 요청 정보
     * @return {@link UserDto.Response} 생성된 사용자 정보
     * @throws CustomException 사용자명 또는 이메일이 이미 존재하는 경우
     */
    @Transactional
    public UserDto.Response createUser(UserDto.CreateRequest request) {
        if (userRepository.existsByName(request.getName())) {
            throw new CustomException("이미 존재하는 사용자명입니다.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException("이미 존재하는 이메일입니다.");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);  // 기본 역할을 USER로 설정
        
        userRepository.save(user);
        return userMapper.toDto(user);
    }

    /**
     * 모든 사용자 정보를 조회합니다.
     * 
     * @return {@code List<UserDto.Response>} 사용자 목록
     * @see UserRepository#findAllUsers()
     */
    @Transactional(readOnly = true)
    public List<UserDto.Response> getAllUsers() {
        return userRepository.findAllUsers().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * ID로 사용자를 조회합니다.
     * 
     * @param id 사용자 ID
     * @return {@link UserDto.Response} 조회된 사용자 정보
     * @throws CustomException 사용자를 찾을 수 없는 경우
     * @see UserRepository#findUserById(Long)
     */
    @Transactional(readOnly = true)
    public UserDto.Response getUserById(Long id) {
        User user = userRepository.findUserById(id)
                .orElseThrow(() -> new CustomException("User not found"));
        return userMapper.toDto(user);
    }

    /**
     * 사용자 정보를 수정합니다.
     * 
     * @param id 수정할 사용자 ID
     * @param request {@link UserDto.UpdateRequest} 수정할 사용자 정보
     * @return {@link UserDto.Response} 수정된 사용자 정보
     * @throws CustomException 사용자를 찾을 수 없는 경우
     * @see UserRepository#findUserById(Long)
     */
    @Transactional
    public UserDto.Response updateUser(Long id, UserDto.UpdateRequest request) {
        User user = userRepository.findUserById(id)
                .orElseThrow(() -> new CustomException("User not found"));

        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return userMapper.toDto(user);
    }

    /**
     * 사용자를 삭제합니다.
     * 
     * @param id 삭제할 사용자 ID
     * @throws CustomException 사용자를 찾을 수 없는 경우
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findUserById(id)
                .orElseThrow(() -> new CustomException("User not found"));
        userRepository.delete(user);
    }

    /**
     * 사용자명 존재 여부를 확인합니다.
     * 
     * @param username 확인할 사용자명
     * @return boolean 사용자명 존재 여부
     */
    public boolean existsByName(String name) {
        return userRepository.existsByName(name);
    }
} 