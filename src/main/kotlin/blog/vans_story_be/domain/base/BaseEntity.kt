package blog.vans_story_be.domain.base

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

/**
 * 모든 엔티티의 기본이 되는 테이블 정의
 * 생성 시간과 수정 시간을 자동으로 관리합니다.
 */
object BaseTable : LongIdTable() {
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
}

/**
 * 모든 엔티티의 기본이 되는 추상 클래스
 * Exposed의 LongEntity를 상속받아 ID와 생성/수정 시간을 자동으로 관리합니다.
 */
abstract class BaseEntity(id: EntityID<Long>) : Entity<Long>(id) {
    var createdAt: LocalDateTime by BaseTable.createdAt
    var updatedAt: LocalDateTime by BaseTable.updatedAt

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        return id == (other as BaseEntity).id
    }

    override fun hashCode(): Int = id.hashCode()
} 