package com.example.recrewz.moim;

import org.apache.ibatis.annotations.*;

import java.util.ArrayList;

@Mapper
public interface MoimDao {
    @Insert("insert into moim values(seq_moim.nextval, #{catno}, #{memberid}, #{info}, #{title}, #{content}, #{photo1, jdbcType=VARCHAR}, #{photo2, jdbcType=VARCHAR}, #{photo3, jdbcType=VARCHAR}, default, default)")
    void insert(Moim moim);

    @Select("select * from moim where no = #{no}")
    MoimDTO select(@Param("no") int no);

    @Select("select * from moim where catno = #{catno}")
    ArrayList<MoimDTO> selectAll(@Param("catno") int catno);

    @Select("select * from moim where memberid = #{memberid}")
    ArrayList<MoimDTO> selectMyAll(@Param("memberid") String memberid);

    @Delete("delete from moim where no = #{no}")
    void delete(@Param("no") int no);

    @Select("select seq_moim.currval from dual")
    int selectNo();

    @Update("update moim set content=#{content}, title=#{title}, info=#{info}, photo1=#{photo1}, photo2=#{photo2}, photo3=#{photo3} where no=#{no}")
    void update(MoimDTO dto);
}
