package com.common.data;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EntityTypeConverter implements AttributeConverter<EntityType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(EntityType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getId();
    }

    @Override
    public EntityType convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        return EntityType.fromId(dbData);
    }
}
