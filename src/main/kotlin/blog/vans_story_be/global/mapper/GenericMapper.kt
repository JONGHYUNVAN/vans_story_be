package blog.vans_story_be.global.mapper

import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * 엔티티와 DTO 간의 자동 매핑을 처리하는 유틸리티 클래스
 * 동일한 이름의 필드들을 자동으로 매핑합니다.
 */
object GenericMapper {

    /**
     * DTO → Entity 변환 (있는 필드만 매핑)
     */
    inline fun <reified D : Any, reified E : Any> toEntity(dto: D, entity: E): E {
        val dtoProps = dto::class.memberProperties.associateBy { it.name }
        val entityProps = entity::class.memberProperties.associateBy { it.name }

        entityProps.forEach { (name, prop) ->
            dtoProps[name]?.let { dtoProp ->
                try {
                    if (prop is KMutableProperty1<*, *>) {
                        @Suppress("UNCHECKED_CAST")
                        (prop as KMutableProperty1<E, Any?>).setter.call(entity, dtoProp.getter.call(dto))
                    }
                } catch (_: Exception) {
                    // 타입 불일치 또는 접근 불가: 무시
                }
            }
        }
        return entity
    }

    /**
     * Entity → DTO 변환 (기본 생성자 있는 경우만 생성)
     */
    inline fun <reified E : Any, reified D : Any> toDto(entity: E): D? {
        val dtoInstance: D? = try {
            D::class.objectInstance ?: D::class.createInstance()
        } catch (_: Exception) {
            null
        }
        return dtoInstance?.let { toDto(entity, it) }
    }

    /**
     * Entity → DTO 변환 (기존 인스턴스에 있는 필드만 매핑)
     */
    inline fun <reified E : Any, reified D : Any> toDto(entity: E, dto: D): D {
        val entityProps = entity::class.memberProperties.associateBy { it.name }
        val dtoProps = dto::class.memberProperties.associateBy { it.name }

        dtoProps.forEach { (name, prop) ->
            entityProps[name]?.let { entityProp ->
                try {
                    if (prop is KMutableProperty1<*, *>) {
                        @Suppress("UNCHECKED_CAST")
                        (prop as KMutableProperty1<D, Any?>).setter.call(dto, entityProp.getter.call(entity))
                    }
                } catch (_: Exception) {
                    // 타입 불일치 또는 접근 불가: 무시
                }
            }
        }
        return dto
    }
}
