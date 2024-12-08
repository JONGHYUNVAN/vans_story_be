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
     * 모든 사용자를 조회합니다.
     * Generated SQL:
     * <pre>
     * SELECT 
     *     user.id         AS user_id,
     *     user.username   AS user_username,
     *     user.email      AS user_email,
     *     user.password   AS user_password,
     *     user.role       AS user_role,
     *     user.created_at AS user_created_at,
     *     user.updated_at AS user_updated_at
     * FROM 
     *     users user
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
     * Generated SQL:
     * <pre>
     * SELECT 
     *     user.id         AS user_id,
     *     user.username   AS user_username,
     *     user.email      AS user_email,
     *     user.password   AS user_password,
     *     user.role       AS user_role,
     *     user.created_at AS user_created_at,
     *     user.updated_at AS user_updated_at
     * FROM 
     *     users user
     * WHERE 
     *     user.id = ?
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