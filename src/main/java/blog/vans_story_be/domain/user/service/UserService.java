package blog.vans_story_be.domain.user.service;

import blog.vans_story_be.domain.user.dto.UserDto;
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

    @Transactional
    public UserDto.Response createUser(UserDto.CreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new CustomException("이미 존재하는 사용자명입니다.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException("이미 존재하는 이메일입니다.");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        userRepository.save(user);
        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserDto.Response> getAllUsers() {
        return userRepository.findAllUsers().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDto.Response getUserById(Long id) {
        User user = userRepository.findUserById(id)
                .orElseThrow(() -> new CustomException("User not found"));
        return userMapper.toDto(user);
    }

    @Transactional
    public UserDto.Response updateUser(Long id, UserDto.UpdateRequest request) {
        User user = userRepository.findUserById(id)
                .orElseThrow(() -> new CustomException("User not found"));

        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPassword() != null) {
            user.setPassword(request.getPassword());
        }

        return userMapper.toDto(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findUserById(id)
                .orElseThrow(() -> new CustomException("User not found"));
        userRepository.delete(user);
    }
} 