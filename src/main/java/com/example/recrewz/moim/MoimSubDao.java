package com.example.recrewz.moim;

import org.apache.ibatis.annotations.*;

@Mapper
public interface MoimSubDao {
    @Insert("insert into moimsub values(#{moimno}, #{memberid}, default, default)")
    int insert(MoimSubDTO dto);

    @Delete("delete from moimsub where moimno = #{no}")
    void delete(@Param("no") int no);

    @Select("select * from moimsub where moimno = #{moimno} and memberid = #{memberid}")
    MoimSubDTO select(@Param("moimno") int moimno, @Param("memberid") String memberid);

    @Delete("delete moimsub where moimno = #{moimno} and memberid = #{memberid}")
    int deleteMoim(@Param("moimno") int moimno, @Param("memberid") String memberid);
}
