package blog.vans_story_be.config.security

import blog.vans_story_be.domain.user.entity.User
import blog.vans_story_be.domain.user.repository.UserRepository
import blog.vans_story_be.global.exception.CustomException
import mu.KotlinLogging
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

/**
 * Spring Security의 UserDetailsService 구현체입니다.
 * 
 * <p>이 서비스는 사용자 인증에 필요한 정보를 데이터베이스에서 로드하며,
 * Spring Security의 인증 프로세스에 통합됩니다.</p>
 * 
 * <h3>주요 기능:</h3>
 * <ul>
 *   <li>이메일 기반 사용자 조회</li>
 *   <li>User 엔티티를 UserDetails로 변환</li>
 *   <li>사용자 권한 정보 관리</li>
 * </ul>
 * 
 * <h3>인증 프로세스:</h3>
 * <ol>
 *   <li>사용자 이메일로 데이터베이스 조회</li>
 *   <li>User 엔티티를 UserDetails 객체로 변환</li>
 *   <li>사용자 권한 정보 설정</li>
 *   <li>인증 실패 시 예외 발생</li>
 * </ol>
 * 
 * <h3>사용 시나리오:</h3>
 * <ul>
 *   <li>로그인 시 사용자 인증</li>
 *   <li>JWT 토큰 생성 시 사용자 정보 로드</li>
 *   <li>권한 기반 접근 제어</li>
 * </ul>
 * 
 * @author vans
 * @version 1.0.0
 * @since 2025.06.07
 * @see org.springframework.security.core.userdetails.UserDetailsService
 * @see blog.vans_story_be.domain.user.entity.User
 * @see blog.vans_story_be.domain.user.repository.UserRepository
 */
@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {
    
    private val logger = KotlinLogging.logger {}

    /**
     * 이메일로 사용자의 상세 정보를 조회합니다.
     * 
     * <p>이 메서드는 Spring Security의 인증 프로세스에서 호출되며,
     * 사용자 인증에 필요한 모든 정보를 로드합니다.</p>
     * 
     * <h4>처리 과정:</h4>
     * <ol>
     *   <li>이메일로 사용자 조회</li>
     *   <li>User 엔티티를 UserDetails로 변환</li>
     *   <li>사용자를 찾을 수 없는 경우 예외 발생</li>
     * </ol>
     * 
     * <h4>예외 처리:</h4>
     * <ul>
     *   <li>사용자를 찾을 수 없는 경우 CustomException 발생</li>
     *   <li>데이터베이스 오류 시 예외 전파</li>
     * </ul>
     * 
     * @param email 조회할 이메일
     * @return UserDetails 사용자 상세 정보
     * @throws CustomException 사용자를 찾을 수 없는 경우
     */
    override fun loadUserByUsername(email: String): UserDetails {
        logger.info { "사용자 조회 시도: $email" }
        return userRepository.findByEmail(email)
            .map { user ->
                logger.info { "사용자 발견: ${user.email}, 역할: ${user.role}" }
                createUserDetails(user)
            }
            .orElseThrow { 
                logger.error { "사용자를 찾을 수 없습니다: $email" }
                CustomException("사용자를 찾을 수 없습니다.") 
            }
    }

    /**
     * User 엔티티를 UserDetails 객체로 변환합니다.
     * 
     * <p>이 메서드는 User 엔티티의 정보를 Spring Security가 이해할 수 있는
     * UserDetails 객체로 변환합니다.</p>
     * 
     * <h4>변환 정보:</h4>
     * <ul>
     *   <li>이메일을 username으로 사용</li>
     *   <li>암호화된 비밀번호</li>
     *   <li>사용자 역할 기반 권한 정보</li>
     * </ul>
     * 
     * <h4>보안 고려사항:</h4>
     * <ul>
     *   <li>비밀번호는 이미 암호화된 상태로 저장</li>
     *   <li>권한 정보는 SimpleGrantedAuthority로 변환</li>
     *   <li>사용자 역할은 ROLE_ 접두사 없이 저장</li>
     * </ul>
     * 
     * @param user User 엔티티
     * @return UserDetails 객체
     */
    private fun createUserDetails(user: User): UserDetails {
        val authority = SimpleGrantedAuthority(user.role.name)

        return org.springframework.security.core.userdetails.User.builder()
            .username(user.email)
            .password(user.password)
            .authorities(setOf(authority))
            .build()
    }
} 