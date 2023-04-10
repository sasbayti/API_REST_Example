package com.example.services;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.dao.PresentacionDao;
import com.example.entities.Presentacion;

public class PresentacionServiceimpl implements PresentacionService {

    @Autowired
    private PresentacionDao presentacionDao;

    @Override
    public Presentacion findById(long id) {
        return presentacionDao.findById(id).get();
    }

    @Override
    public Presentacion save(Presentacion presentacion) {
      return presentacionDao.save(presentacion);
    }

    @Override
    public void delete(Presentacion presentacion) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }
    
}
