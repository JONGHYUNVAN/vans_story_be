package blog.vans_story_be.domain.base

import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

/**
 * 모든 엔티티의 기본이 되는 추상 클래스
 * 생성 시간과 수정 시간을 자동으로 관리합니다.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity {
    /** 엔티티 생성 시간, LocalDateTime 타입 */
    @CreatedDate
    var createdAt: LocalDateTime? = null
        protected set

    /** 엔티티 최종 수정 시간, LocalDateTime 타입 */
    @LastModifiedDate
    var updatedAt: LocalDateTime? = null
        protected set
} 