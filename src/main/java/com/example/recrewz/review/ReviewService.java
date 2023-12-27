package com.example.recrewz.review;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReviewService {
    private ReviewDao reviewDao;

    public synchronized ReviewInfo add(ReviewDTO dto) {
        ReviewInfo empty = null;

        try {
            int nextVal = reviewDao.nextVal();
            System.out.println("nextVal: " + nextVal);
            int cnt = reviewDao.insert(new Review(nextVal, dto.getMemberid(), dto.getSomoimno(), dto.getTitle(), dto.getContent(), dto.getPhoto1(), dto.getPhoto2(), dto.getPhoto3(), null));
            if(cnt > 0) {
                int currVal = reviewDao.currVal();
                System.out.println("currVal: " + currVal);
                if((currVal) == nextVal)
                    empty = reviewDao.get(currVal);
            } else {
                int minus = reviewDao.seqMinus();
                System.out.println("minus: " + minus);
            }
        } catch(DataAccessException e) {
            System.out.println("add: " + e.getCause());
        }

        return empty;
    }

    public List<ReviewInfo> list(int moimno) {
        List<ReviewInfo> list = new ArrayList<>();

        try {
            list = reviewDao.selectAll(moimno).stream().toList();
        } catch(DataAccessException e) {
            System.out.println("list: " + e.getCause());
        }

        return list;
    }

    public ReviewDTO get(int no) {
        ReviewDTO dto = null;

        try {
            Review r = reviewDao.select(no);
            if(r != null)
                dto = new ReviewDTO(r.getNo(), r.getMemberid(), 0, r.getSomoimno(), r.getTitle(), r.getContent(), null, null, null, r.getPhoto1(), r.getPhoto2(), r.getPhoto3(), r.getDate());
        } catch(DataAccessException e) {
            System.out.println("get: " + e.getCause());
        }

        return dto;
    }

    public ReviewInfo info(int no) {
        ReviewInfo ri = null;

        try {
            ri = reviewDao.getInfo(no);
        } catch(DataAccessException e) {
            System.out.println("info: " + e.getCause());
        }

        return ri;
    }

    public synchronized ReviewDTO edit(ReviewDTO dto) {
        ReviewDTO empty = null;

        try {
            int cnt = reviewDao.update(new Review(dto.getNo(), dto.getMemberid(), dto.getSomoimno(), dto.getTitle(), dto.getContent(), dto.getPhoto1(), dto.getPhoto2(), dto.getPhoto3(), null));
            if(cnt > 0) {
                Review r = reviewDao.select(dto.getNo());
                empty = new ReviewDTO(r.getNo(), r.getMemberid(), 0, r.getSomoimno(), r.getTitle(), r.getContent(), null, null, null, r.getPhoto1(), r.getPhoto2(), r.getPhoto3(), null);
            }
        } catch(DataAccessException e) {
            System.out.println("edit: " + e.getCause());
        }

        return empty;
    }

    public synchronized boolean del(int no) {
        boolean flag = false;

        try {
            int cnt = reviewDao.delete(no);
            if(cnt > 0)
                flag = true;
        } catch(DataAccessException e) {
            System.out.println("del: " + e.getCause());
        }

        return flag;
    }

    public List<ReviewInfo> myList(int moimno, String memberid) {
        List<ReviewInfo> list = new ArrayList<>();

        try {
            list = reviewDao.selectMySomoimList(moimno, memberid).stream().toList();
        } catch(DataAccessException e) {
            System.out.println("list: " + e.getCause());
        }

        return list;
    }

    @Autowired
    public void setReviewDao(ReviewDao reviewDao) {
        this.reviewDao = reviewDao;
    }
}
