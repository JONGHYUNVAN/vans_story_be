package blog.vans_story_be.global.mapper

import org.mapstruct.BeanMapping
import org.mapstruct.MappingTarget
import org.mapstruct.NullValuePropertyMappingStrategy

/**
 * 엔티티와 DTO 간의 변환을 처리하는 제네릭 매퍼 인터페이스
 *
 * @param D DTO 타입
 * @param E 엔티티 타입
 * @author vans
 * @version 1.0.0
 * @since 2024.12.04
 */
interface GenericMapper<D, E> {
    /**
     * DTO -> Entity 변환
     */
    fun toEntity(dto: D): E

    /**
     * Entity -> DTO 변환
     */
    fun toDto(entity: E): D

    /**
     * Entity 업데이트
     * null 값은 무시됩니다.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    fun updateEntity(dto: D, @MappingTarget entity: E)
} 