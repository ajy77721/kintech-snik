package com.kitchen.sink.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kitchen.sink.entity.Member;
import com.kitchen.sink.entity.User;
import com.kitchen.sink.entity.UserSession;
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
            T t = dtoMapper.convertValue(obj, clazz);
//            if(t instanceof User user){
//                if(user.getEmail() != null){
//                    user.setEmail(user.getEmail().toLowerCase());
//                }
//            } else if (t instanceof UserSession userSession){
//                if(userSession.getEmail() != null){
//                    userSession.setEmail(userSession.getEmail().toLowerCase());
//                }
//            }else if (t instanceof Member member){
//                    if(member.getEmail() != null){
//                        member.setEmail(member.getEmail().toLowerCase());
//                    }
//                }
            return t;

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
