package com.example.services;

import com.example.entities.Presentacion;

public interface PresentacionService {
    public Presentacion findById(long id);
    public Presentacion save(Presentacion presentacion);
    public void delete(Presentacion presentacion);

}
