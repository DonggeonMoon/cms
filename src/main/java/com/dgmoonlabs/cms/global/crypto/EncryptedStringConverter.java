package com.dgmoonlabs.cms.global.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {
    public static final int COLUMN_LENGTH = 2000;

    @Override
    public String convertToDatabaseColumn(final String attribute) {
        return DataKeys.protect(attribute);
    }

    @Override
    public String convertToEntityAttribute(final String dbData) {
        return DataKeys.reveal(dbData);
    }
}
