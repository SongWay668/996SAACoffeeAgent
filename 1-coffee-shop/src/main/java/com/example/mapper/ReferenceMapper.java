package com.example.mapper;

import com.example.entity.Reference;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ReferenceMapper {
    List<Reference> selectByRefType(@Param("refType") String refType);
    List<Reference> selectAll();
    String selectRefValue(@Param("refType") String refType, @Param("refCode") Integer refCode);
}
