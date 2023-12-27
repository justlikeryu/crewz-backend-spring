package com.example.recrewz.somoim;

import org.apache.ibatis.annotations.*;

import java.util.ArrayList;

@Mapper
public interface SomoimDao {
    @Insert("insert into somoim values(seq_somoim.nextval, #{moimno}, #{memberid}, #{title}, #{content}, #{jdate}, sysdate, #{loc}, #{total}, #{photo})")
    void insert(Somoim somoim);

    @Select("select * from somoim where moimno = #{no}")
    ArrayList<Somoim> selectAll(@Param("no") int no);

    @Select("select * from somoim where no = #{no}")
    Somoim select(@Param("no") int no);

    @Select("select s1.* from somoim s1, somoimsub s2 where s1.no = s2.somoimno and s2.memberid = #{id}")
    ArrayList<SomoimDTO> mysomoimlist(@Param("id") String id);

    @Select("select seq_somoim.currval from dual")
    int getNo();

    @Delete("delete somoim where no = #{no}")
    int delete(@Param("no") int no);

    @Update("update somoim set title = #{title}, content = #{content}, jdate = #{jdate}, loc = #{loc}, total = #{total}, phtot = #{photo} where no = #{no}")
    int update(Somoim somoim);
}
