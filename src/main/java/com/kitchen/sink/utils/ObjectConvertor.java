package com.kitchen.sink.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kitchen.sink.entity.User;
import com.kitchen.sink.exception.ObjectMappingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ObjectConvertor {
    @Autowired
    @Qualifier("dtoMapper")
    private ObjectMapper dtoMapper;

    public <T> T convert(Object obj, Class<T> clazz) {
        try {
            if (obj == null) {
                return null;
            }
            return dtoMapper.convertValue(obj, clazz);
        } catch (Exception e) {
            log.error("Error converting object", e);
            throw new ObjectMappingException("Error converting object" + e.getMessage());
        }
    }


    public String writeValueAsString(Object obj) {
        try {
            return dtoMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Error converting object", e);
            throw new ObjectMappingException("Error converting object " + e.getMessage());
        }
    }
}
