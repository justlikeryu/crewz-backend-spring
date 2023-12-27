package com.example.recrewz.moim;

import com.example.recrewz.category.Category;
import com.example.recrewz.category.CategoryDao;
import com.example.recrewz.category.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;

@Service
public class MoimService {
    private MoimDao moimDao;
    private MoimSubDao moimSubDao;
    private CategoryDao categoryDao;

    public MoimDTO moimAdd(MoimDTO dto) {
        MoimDTO moim = null;
        try {
            MultipartFile[] mf = dto.getPhoto();
            moimDao.insert(new Moim(dto.getNo(), dto.getCatno(), dto.getMemberid(), dto.getInfo(), dto.getTitle(), dto.getContent(), mf[0].getOriginalFilename(), mf[1].getOriginalFilename(), mf[2].getOriginalFilename(), null, 1));
            mf = null;

            int no = moimDao.selectNo();
            moim = moimDao.select(no);

            // 모임 이력에 주최자 추가
            moimSubDao.insert(new MoimSubDTO(no, dto.getMemberid(), null, 0));

            // 카테고리 토탈에 추가
            Category category = categoryDao.getCategoryName(dto.getCatno());
            categoryDao.setTotal(category.getTotal() + 1, dto.getCatno());
        } catch(DataAccessException e) {
            System.out.println(e.getMessage());
        }

        return moim;
    }

    public boolean moimDelete(int no) {
        boolean flag = false;

        try {
            // 모임 삭제
            moimDao.delete(no);

            // 모임 이력 삭제
            moimSubDao.delete(no);

            // 카테고리 토탈에서 삭제
            Category category = categoryDao.getCategoryName(no);
            categoryDao.setTotal(category.getTotal() - 1, no);

            flag = true;
        } catch(DataAccessException e) {
            System.out.println(e.getMessage());
        }

        return flag;
    }

    public MoimDTO moimInfo(int no) {
        MoimDTO moim = null;

        try {
            moim = moimDao.select(no);
        } catch(DataAccessException e) {
            System.out.println(e.getMessage());
        }

        return moim;
    }

    public MoimDTO moimEdit(MoimDTO dto) {
        MoimDTO moim = null;

        try {
            moimDao.update(dto);
            moim = moimDao.select(dto.getNo());
        } catch(DataAccessException e) {
            System.out.println(e.getMessage());
        }

        return moim;
    }

    public boolean join(int moimno, String memberid) {
        boolean flag = false;

        int cnt = moimSubDao.insert(new MoimSubDTO(moimno, memberid, null, 0));
        if(cnt > 0) {
            flag = true;
        }

        return flag;
    }

    public boolean out(int moimno, String memberid) {
        boolean flag = false;

        int cnt = moimSubDao.deleteMoim(moimno, memberid);
        if(cnt > 0) {
            flag = true;
        }

        return flag;
    }

    public ArrayList<MoimDTO> list(int catno) {
        return moimDao.selectAll(catno);
    }

    public ArrayList<MoimDTO> mymoimList(String id) {
        return moimDao.selectMyAll(id);
    }

    public MoimSubDTO getSub(int moimno, String memberid) {
        return moimSubDao.select(moimno, memberid);
    }

    @Autowired
    public void setDao(MoimDao moimDao) {
        this.moimDao = moimDao;
    }

    @Autowired
    public void setMoimSubDao(MoimSubDao moimSubDao) {
        this.moimSubDao = moimSubDao;
    }

    @Autowired
    public void setCategoryDao(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }
}
