package com.example.recrewz.somoim;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class SomoimService {
    private SomoimDao somoimDao;
    private SomoimSubDao somoimSubDao;

    public synchronized SomoimDTO add(SomoimDTO dto) {
        SomoimDTO item = null;

        try {
            // 소모임 추가
            somoimDao.insert(new Somoim(dto.getNo(), dto.getMoimno(), dto.getMemberid(), dto.getTitle(), dto.getContent(), dto.getJdate(), dto.getMdate(), dto.getLoc(), dto.getTotal(), dto.getMf().getOriginalFilename()));

            // 소모임 이력 추가
            int no = somoimDao.getNo();
            somoimSubDao.insert(new SomoimSub(no, dto.getMemberid(), 0));

            item = new SomoimDTO(somoimDao.select(no));
        } catch(DataAccessException e) {
            System.out.println(e.getMessage());
        }

        return item;
    }

    public boolean del(int no) {
        boolean flag = false;

        try {
            int check = somoimDao.delete(no);
            if(check == 1) {
                int cnt = somoimSubDao.delete(no);
                System.out.println("cnt: " + cnt);
                if(cnt >= 1) {
                    flag = true;
                }
            }
        } catch(DataAccessException e) {
            System.out.println(e.getMessage());
        }

        return flag;
    }

    public SomoimDTO getMoimNo(int no) {
        SomoimDTO dto = null;

        try {
            dto =  new SomoimDTO(somoimDao.select(no));
        } catch(DataAccessException e) {
            System.out.println(e.getMessage());
        }

        return dto;
    }

    public ArrayList<SomoimDTO> list(int no) {
        ArrayList<SomoimDTO> list = new ArrayList<>();

        for(Somoim somoim : somoimDao.selectAll(no)) {
            SomoimDTO dto = new SomoimDTO(somoim);

            list.add(dto);
        }

        return list;
    }

    public synchronized SomoimDTO edit(SomoimDTO dto) {
        SomoimDTO updateItem = null;

        try {
            int cnt = somoimDao.update(new Somoim(dto.getNo(), 0, null, dto.getTitle(), dto.getContent(), dto.getJdate(), null, dto.getLoc(), dto.getTotal(), dto.getMf().getOriginalFilename()));
            if(cnt > 0)
                updateItem = new SomoimDTO(somoimDao.select(dto.getNo()));
        } catch(DataAccessException e) {
            System.out.println(e.getCause());
        }

        return updateItem;
    }

    public SomoimSub check(int somoimno, String id) {
        SomoimSub item = somoimSubDao.somoimCheck(somoimno, id);

        return item;
    }

    public boolean somoimJoin(int somoimno, String id) {
        boolean flag = false;

        try {
            // 소모임 인원 확인
            Somoim somoim = somoimDao.select(somoimno);
            System.out.println("somoim: " + somoim);
            // 소모임 가입 인원 확인
            int partIn = somoimSubDao.selectCheckTotal(somoimno);
            System.out.println("총 가입자수: " + partIn);
            if(somoim.getTotal() <= partIn) {
                System.out.println("인원 초과");
                return flag;
            }
            else {
                int cnt = somoimSubDao.insert(new SomoimSub(somoimno, id, 0));
                if (cnt > 0)
                    flag = true;
            }
        } catch(DataAccessException e) {
            System.out.println("somoimjoin failed: " + e.getCause());
        }

        return flag;
    }

    public boolean somoimOut(int somoimno, String id) {
        boolean flag = false;

        try {
            int cnt = somoimSubDao.outSomoim(somoimno, id);
            System.out.println("out cnt: " + cnt);
            if(cnt > 0) {
                flag = true;
            }
        } catch(DataAccessException e){
            System.out.println("somoimout failed: " + e.getCause());
        }

        return flag;
    }

    public boolean reviewCheck(String id) {
        boolean flag = false;

        try {
            int check = somoimSubDao.reviewCheck(id);
            System.out.println("review check: " + check);
            if(check >= 1){
                flag = true;
            }
        } catch(DataAccessException e) {
            System.out.println("somoim reviewCheck: " );
        }

        return flag;
    }

    public ArrayList<SomoimDTO> mysomoim(String id) {
        ArrayList<SomoimDTO> list = somoimDao.mysomoimlist(id);

        return list;
    }

    @Autowired
    public void setSomoimDao(SomoimDao somoimDao) {
        this.somoimDao = somoimDao;
    }

    @Autowired
    public void setSomoimSubDao(SomoimSubDao somoimSubDao) {
        this.somoimSubDao = somoimSubDao;
    }
}
