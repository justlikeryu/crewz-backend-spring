package com.example.recrewz.somoim;

import org.apache.ibatis.annotations.*;

@Mapper
public interface SomoimSubDao {
    @Insert("insert into somoimsub values(#{somoimno}, #{memberid}, #{partin})")
    int insert(SomoimSub sub);

    @Delete("delete somoimsub where somoimno = #{somoimno}")
    int delete(@Param("somoimno") int somoimno);

    @Select("select count(*) from somoimsub where somoimno = #{somoimno} ")
    int selectCheckTotal(@Param("somoimno") int somoimno);

    @Delete("delete somoimsub where somoimno = #{somoimno} and memberid = #{id}")
    int outSomoim(@Param("somoimno") int somoimno, @Param("id") String id);

    @Select("select * from somoimsub where somoimno = #{somoimno} and memberid = #{memberid}")
    SomoimSub somoimCheck(@Param("somoimno") int somoimno, @Param("memberid") String memberid);

    @Select("select count(*) from somoimsub where memberid = #{memberid}")
    int reviewCheck(@Param("memberid") String memberid);
}
