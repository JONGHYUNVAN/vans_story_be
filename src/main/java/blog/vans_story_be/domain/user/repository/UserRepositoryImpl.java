package blog.vans_story_be.domain.user.repository;

import blog.vans_story_be.domain.user.entity.QUser;
import blog.vans_story_be.domain.user.entity.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * UserRepositoryCustom 인터페이스의 구현체
 * QueryDSL을 사용하여 사용자 쿼리를 구현합니다.
 * 
 * @author vans
 * @version 1.0.0
 * @since 2024.03.19
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * 모든 사용자 정보를 조회합니다.
     * 
     * @return {@code List<User>} 전체 사용자 목록
     * @implNote QueryDSL 동작:
     * <pre>
     * 1. selectFrom(user): User 엔티티의 모든 필드를 선택하고 FROM 절 생성
     *    - SELECT id, name, email, password,role, created_at, updated_at
     *    - FROM users user
     * 
     * 2. fetch(): 결과를 List<User>로 변환하여 반환
     * </pre>
     */
    @Override
    public List<User> findAllUsers() {
        QUser user = QUser.user;
        return 
        queryFactory
            .selectFrom(user)
            .fetch();
    }

    /**
     * 사용자 ID로 사용자를 조회합니다.
     * 
     * @param id 조회할 사용자 ID
     * @return {@code Optional<User>} 조회된 사용자 (존재하지 않을 경우 빈 Optional)
     * @implNote QueryDSL 동작:
     * <pre>
     * 1. selectFrom(user): User 엔티티의 모든 필드를 선택하고 FROM 절 생성
     *    - SELECT id, name, email, password, role, created_at, updated_at
     *    - FROM users user
     * 
     * 2. where(user.id.eq(id)): 주어진 ID와 일치하는 사용자 필터링
     * 
     * 3. fetchOne(): 결과를 단일 User 객체로 변환하여 반환
     * </pre>
     */
    @Override
    public Optional<User> findUserById(Long id) {
        QUser user = QUser.user;
        User foundUser = 
        queryFactory
            .selectFrom(user)
            .where(user.id.eq(id))
            .fetchOne();
        return Optional.ofNullable(foundUser);
    }
} 