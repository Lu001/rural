package com.wuzhen.rural.service;


import com.wuzhen.rural.dao.BillDAO;
import com.wuzhen.rural.dao.TripDAO;
import com.wuzhen.rural.pojo.Bill;
import com.wuzhen.rural.pojo.Trip;
import com.wuzhen.rural.pojo.User;
import com.wuzhen.rural.util.Page4Navigator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TripService {
    @Autowired
    TripDAO tripDAO;

    public List<Trip> list(){
        Sort sort=new Sort(Sort.Direction.DESC,"id");
        return tripDAO.findAll(sort);
    }
    //添加
    public void add(Trip property){
        tripDAO.save(property);
    }
    //删除
    public void delete(int id){
        tripDAO.deleteById(id);
    }
    //编辑
    public Trip get(int id){
        return tripDAO.getOne(id);
    }
    //修改
    public void update(Trip bean) {
        tripDAO.save(bean);
    }
}

