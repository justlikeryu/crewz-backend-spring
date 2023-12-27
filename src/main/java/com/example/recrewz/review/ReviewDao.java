package com.example.recrewz.review;

import org.apache.ibatis.annotations.*;

import java.util.ArrayList;

@Mapper
public interface ReviewDao {
    @Insert("insert into review values(#{no}, #{memberid}, #{somoimno}, #{title}, #{content}, #{photo1, jdbcType=VARCHAR}, #{photo2, jdbcType=VARCHAR}, #{photo3, jdbcType=VARCHAR}, sysdate)")
    int insert(Review review);

    @Select("select r.no as no, r.memberid as memberid, m.no as moimno, r.somoimno as somoimno, r.title as title, r.content as content, r.photo1 as photo1, r.photo2 as photo2, r.photo3 as photo3, r.mdate as mdate, c.no as categoryno, c.name as name" +
            " from review r, somoim s, moim m, category c" +
            " where r.somoimno = s.no" +
            " and s.moimno = m.no" +
            " and m.catno = c.no" +
            " and m.no = #{moimno} order by r.no desc")
    ArrayList<ReviewInfo> selectAll(@Param("moimno") int moimno);

    @Select("select s.title as somoimtitle, s.no as somoimno" +
            " from somoim s, somoimsub sb" +
            " where s.no = sb.somoimno" +
            " and s.moimno = #{moimno}" +
            " and sb.memberid = #{memberid}")
    ArrayList<ReviewInfo> selectMySomoimList(@Param("moimno") int moimno, @Param("memberid") String memberid);

    @Select("select r.no as no, r.memberid as memberid, m.no as moimno, r.somoimno as somoimno, r.title as title, r.content as content, r.photo1 as photo1, r.photo2 as photo2, r.photo3 as photo3, r.mdate as mdate, c.no as categoryno, c.name as name" +
            " from review r, somoim s, moim m, category c" +
            " where r.somoimno = s.no" +
            " and s.moimno = m.no" +
            " and m.catno = c.no" +
            " and r.no = #{no}")
    ReviewInfo get(@Param("no") int no);

    @Select("select r.no as no, r.memberid as memberid, m.no as moimno, r.somoimno as somoimno, r.title as title, r.content as content, r.photo1 as photo1, r.photo2 as photo2, r.photo3 as photo3, r.mdate as mdate, c.no as categoryno, c.name as name" +
            " from review r, somoim s, moim m, category c" +
            " where r.somoimno = s.no" +
            " and s.moimno = m.no" +
            " and m.catno = c.no" +
            " and r.no = #{no}")
    ReviewInfo getInfo(@Param("no") int no);

    @Select("select * from review where no = #{no}")
    Review select(@Param("no") int no);

    @Update("update review set title = #{title}, content = #{content}, photo1 = #{photo1}, photo2 = #{photo2}, photo3 = #{photo3} where no = #{no}")
    int update(Review review);

    @Delete("delete review where no = #{no}")
    int delete(int no);

    @Select("select seq_review.currval from dual")
    int currVal();

    @Select("alter sequence seq_review increment by -1")
    int seqMinus();

    @Select("select seq_review.nextval from dual")
    int nextVal();
}
