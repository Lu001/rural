package com.wuzhen.rural.service;


import com.wuzhen.rural.pojo.Barrage;
import com.wuzhen.rural.dao.BarrageDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BarrageService {
    @Autowired
    BarrageDAO barrageDAO;
    public List<Barrage> list(){
        Sort sort=new Sort(Sort.Direction.DESC,"id");
        return barrageDAO.findAll(sort);
    }
    //添加
    public void add(Barrage barrage){
        barrageDAO.save(barrage);
    }

}

