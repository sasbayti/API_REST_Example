package com.example.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.entities.Producto;
import com.example.services.ProductoService;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

//Quiero que en la respuest avaya el producto pero tambien el estado en el que me la devuelve
 /** El metodo siguiente va a responder a una peticion (request) del tipo: 
  * http://localhost;8080/productos?page=1&size=4
  es decir, tiene que ser capaz de devolver un listado de productos paginados, o no, pero en 
  cualquier caso, ordenados por un criterio (nombre, descrpcion, etc.)

  La peticion anterior implica @RequestParam
 
  /productos/3 => PathVariable

  */
    @GetMapping
    public ResponseEntity<List<Producto>> findAll(@RequestParam(name = "page", required = false) Integer page,
                                                  @RequestParam(name = "size", required = false) Integer size){
                                    
        ResponseEntity<List<Producto>> responseEntity = null;

        List<Producto> productos = new ArrayList<>();

        Sort sortByNombre = Sort.by("nombre");

        if(page != null && size != null) {
            
            try {
              Pageable pageable = PageRequest.of(page, size, sortByNombre);
                Page<Producto> productosPaginados = productoService.findAll(pageable);
                productos = productosPaginados.getContent(); //Aqui estan los productos
                
               responseEntity = new ResponseEntity<List<Producto>>(productos, HttpStatus.OK);
            } catch (Exception e) {
               responseEntity = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } else {
            //sin paginacion, pero con ordenamiento
            try {
                productos = productoService.findAll(sortByNombre);
                responseEntity = new ResponseEntity<List<Producto>>(productos, HttpStatus.OK);
            } catch (Exception e) {
                responseEntity = new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
        }
            
        return responseEntity;
    }








    /** El metodo siguiente es de ejemplo para entender el formato de JSON, 
     * no tiene que ver en si con el proyecto
     */
 /*    @GetMapping
    public List<String> nombres(){
     
        List<String> nombres = Arrays.asList("Salma", "Judith", "Elisabet");

        return nombres;  
        }
   */
    
}