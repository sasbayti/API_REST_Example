package com.example.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.entities.FileUploadUtil;
import com.example.entities.Producto;
import com.example.model.FileUploadResponse;
import com.example.services.ProductoService;
import com.example.utilities.FileDownloadUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ProductoController {
    // Tambien se puede inyectar por constructor
    private final FileDownloadUtil fileDownloadUtil;
   
   /* @Autowired
    private FileDownloadUtil fileDownloadUtil; */

    @Autowired
    private ProductoService productoService;

    @Autowired
    private FileUploadUtil fileUploadUtil;
    // Quiero que en la respuest avaya el producto pero tambien el estado en el que
    // me la devuelve
    /**
     * El metodo siguiente va a responder a una peticion (request) del tipo:
     * http://localhost;8080/productos?page=1&size=4
     * es decir, tiene que ser capaz de devolver un listado de productos paginados,
     * o no, pero en
     * cualquier caso, ordenados por un criterio (nombre, descrpcion, etc.)
     * 
     * La peticion anterior implica @RequestParam
     * 
     * /productos/3 => PathVariable
     * 
     */
    @GetMapping
    public ResponseEntity<List<Producto>> findAll(@RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {

        ResponseEntity<List<Producto>> responseEntity = null;

        List<Producto> productos = new ArrayList<>();

        Sort sortByNombre = Sort.by("nombre");

        if (page != null && size != null) {

            try {
                Pageable pageable = PageRequest.of(page, size, sortByNombre);
                Page<Producto> productosPaginados = productoService.findAll(pageable);
                productos = productosPaginados.getContent(); // Aqui estan los productos

                responseEntity = new ResponseEntity<List<Producto>>(productos, HttpStatus.OK);
            } catch (Exception e) {
                responseEntity = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } else {
            // sin paginacion, pero con ordenamiento
            try {
                productos = productoService.findAll(sortByNombre);
                responseEntity = new ResponseEntity<List<Producto>>(productos, HttpStatus.OK);
            } catch (Exception e) {
                responseEntity = new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
        }

        return responseEntity;
    }

    /**
     * Recupera un producto por el id
     * va a responder a una peticion del tipo, por ejemplo:
     * http://localhost:8080/productos/2
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> findById(@PathVariable(name = "id") Integer id) {

        ResponseEntity<Map<String, Object>> responseEntity = null;

        Map<String, Object> responseAsMap = new HashMap<>();
        try {
            Producto producto = productoService.findById(id);
            if (producto != null) {
                String succesMessage = "Se ha encontrado el producto con id: " + id;
                responseAsMap.put("mensaje", succesMessage);
                responseAsMap.put("producto", producto);
                responseEntity = new ResponseEntity<Map<String, Object>>(responseAsMap, HttpStatus.OK);
            } else {
                String errorMessage = "No se ha encontrado el producto con id: " + id;
                responseAsMap.put("error", errorMessage);
                responseEntity = new ResponseEntity<Map<String, Object>>(responseAsMap, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            String errorGrave = "Error grave";
            responseAsMap.put("error", errorGrave);
            responseEntity = new ResponseEntity<Map<String, Object>>(responseAsMap, HttpStatus.INTERNAL_SERVER_ERROR);

        }
        return responseEntity;

    }

     // Guardar (Persistir), un producto, con su presentacion en la base de datos
    // Para probarlo con POSTMAN: Body -> form-data -> producto -> CONTENT TYPE ->
    // application/json
    // no se puede dejar el content type en Auto, porque de lo contrario asume
    // application/octet-stream
    // y genera una exception MediaTypeNotSupported
    /**
     * Persiste un producto en la base de datos
     * 
     * 
     */
    // @throws IOException
    @PostMapping(consumes = "multipart/form-data") // Va a recibir los datos del formulario por eso postMapping,
                 // con el verbo post sabe que lo que quiere es eso
    @Transactional
    public ResponseEntity<Map<String, Object>> insert(@Valid @RequestPart(name = "producto") Producto producto,
                                                     BindingResult result,
                                                     @RequestPart(name = "file") MultipartFile file) throws IOException {
        // Para que valide lo que llega

        Map<String, Object> responseAsMap = new HashMap<>();

        ResponseEntity<Map<String, Object>> responseEntity = null;
        /** Primero comprobar si hay errores en el producto recibido */
        if (result.hasErrors()) {
            List<String> errorMessages = new ArrayList<>();
            for (ObjectError error : result.getAllErrors()) {

                errorMessages.add(error.getDefaultMessage());

            }
            responseAsMap.put("errores", errorMessages);

            responseEntity = new ResponseEntity<Map<String, Object>>(responseAsMap, HttpStatus.BAD_REQUEST);

            return responseEntity; // si hay error no quiero que se guarde el producto
        }

        // Si no hay errores, entonces persistimos el producto.
        // COmprobando previamente si nos han enviado una imagen o un archivo
        
        if(!file.isEmpty()){
            String fileCode = fileUploadUtil.saveFile(file.getOriginalFilename(), file);
            producto.setImagenProducto(fileCode + "-" + file.getOriginalFilename());
            
            // Devolver informacion respecto al file recibido

            FileUploadResponse fileUploadResponse = FileUploadResponse.builder()
                                                    .fileName(fileCode + "-" + file.getOriginalFilename())
                                                    .downloadURI("/productos/downloadFile/" + fileCode + "-" 
                                                            + file.getOriginalFilename())
                                                    .size(file.getSize())
                                                    .build();

            responseAsMap.put("info de la imagen: ", fileUploadResponse);
        }
        Producto productoDB = productoService.save(producto);
        try {
            if (productoDB != null) { // Aqui estoy haciendo la validacion de si se ha guardado
                String mensaje = "El producto se ha creado correctamente";
                responseAsMap.put("mensaje", mensaje);
                responseAsMap.put("producto", productoDB);
                responseEntity = new ResponseEntity<Map<String, Object>>(responseAsMap, HttpStatus.CREATED);

            } else {
                String mensaje = "El producto no se ha creado";
                responseAsMap.put("mensaje", mensaje);
                responseEntity = new ResponseEntity<Map<String, Object>>(responseAsMap, HttpStatus.NOT_ACCEPTABLE);
            }
        } catch (DataAccessException e) {
            // Tipo de error de DAtaAccesException tipo de error controlado
            String errorGrave = "Ha tenido lugar un error grave y la causa más probable puede ser" +
                    e.getMostSpecificCause(); // Lo que te devuelve aqui es lo mas cercano al error mas probable (ej
                                              // caused by)
            responseAsMap.put("errorGrave", errorGrave);
            responseEntity = new ResponseEntity<Map<String, Object>>(responseAsMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return responseEntity;
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Map<String, Object>> update(@Valid @RequestBody Producto producto, BindingResult result,
            @PathVariable(name = "id") Integer id) {
        // Para que valide lo que llega

        Map<String, Object> responseAsMap = new HashMap<>();

        ResponseEntity<Map<String, Object>> responseEntity = null;
        /** Primero comprobar si hay errores en el producto recibido */
        if (result.hasErrors()) {
            List<String> errorMessages = new ArrayList<>();
            for (ObjectError error : result.getAllErrors()) {

                errorMessages.add(error.getDefaultMessage());

            }
            responseAsMap.put("errores", errorMessages);

            responseEntity = new ResponseEntity<Map<String, Object>>(responseAsMap, HttpStatus.BAD_REQUEST);

            return responseEntity; // si hay error no quiero que se guarde el producto
        }

        // Vinculamos el id que se recibe con el producto
        producto.setId(id);
        // Si no hay errores, entonces actualizamos el producto.
        Producto productoDB = productoService.save(producto);
        try {
            if (productoDB != null) { // Aqui estoy haciendo la validacion de si se ha guardado
                String mensaje = "El producto se ha actualizado correctamente";
                responseAsMap.put("mensaje", mensaje);
                responseAsMap.put("producto", productoDB);
                responseEntity = new ResponseEntity<Map<String, Object>>(responseAsMap, HttpStatus.OK);

            } else {
                String mensaje = "El producto no se ha actualizado";
                responseAsMap.put("mensaje", mensaje);
                responseEntity = new ResponseEntity<Map<String, Object>>(responseAsMap, HttpStatus.NOT_ACCEPTABLE);
            }
        } catch (DataAccessException e) {
            // Tipo de error de DAtaAccesException tipo de error controlado
            String errorGrave = "Ha tenido lugar un error grave y la causa más probable puede ser" +
                    e.getMostSpecificCause(); // Lo que te devuelve aqui es lo mas cercano al error mas probable (ej
                                              // caused by)
            responseAsMap.put("errorGrave", errorGrave);
            responseEntity = new ResponseEntity<Map<String, Object>>(responseAsMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return responseEntity;
    }
    @Transactional
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminaProducto(@PathVariable(name = "id") Integer id){
        ResponseEntity<String> responseEntity = null;
       
        Producto producto = productoService.findById(id);
      
        try {
            if (producto != null) {
            String mensaje = "El producto se ha borrado correctamente";
            productoService.delete(producto);
            responseEntity = new ResponseEntity<String>(mensaje, HttpStatus.OK);
        } else{
            responseEntity = new ResponseEntity<String>("No existe el producto",HttpStatus.NO_CONTENT);
        }
    } catch (DataAccessException e) {
           e.getMostSpecificCause();
            String errorGrave = "Error grave";
            responseEntity = new ResponseEntity<String>(errorGrave, HttpStatus.INTERNAL_SERVER_ERROR);
            
        }
        return responseEntity;
    }
    // La forma del profesor
    // @DeleteMapping("/{id}")
    // @Transactional
    // public ResponseEntity<String> delete(@PathVariable(name = "id") Integer id){
    //     ResponseEntity<String> responseEntity = null;
        
    //     try {
    //         // Recuperamos el producto
            
    //         Producto producto = productoService.findById(id);
    //         if(producto != null){
    //         productoService.delete(producto);
    //         responseEntity = new ResponseEntity<String>("Borrado exitosamente", HttpStatus.OK);
    //         }
    //         else{
    //             responseEntity = new ResponseEntity<String>("No se ha encontrado el producto", HttpStatus.NOT_FOUND);  
    //         }
    //     } catch (DataAccessException e) {
    //         e.getMostSpecificCause();
    //         responseEntity = new ResponseEntity<String>("Error Fatal", HttpStatus.INTERNAL_SERVER_ERROR);
    //     }

    //     return responseEntity;
    // }

    // Metodo que va a hacer uso de la clase fildeDownloadUtil
       /**
     *  Implementa filedownnload end point API 
     **/    
    @GetMapping("/downloadFile/{fileCode}")
    public ResponseEntity<?> downloadFile(@PathVariable(name = "fileCode") String fileCode) { // Devuelve un generico de cualquier cosa

        Resource resource = null; // EL objetivo es que me devuelva un recurso

        try {
            resource = fileDownloadUtil.getFileAsResource(fileCode);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build(); // Otra manera de devolver responseEntity
        }

        if (resource == null) {
            return new ResponseEntity<>("File not found ", HttpStatus.NOT_FOUND);
        }

        String contentType = "application/octet-stream";
        String headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";

        return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(contentType)) //Hay que especificarle el tipo contenttype, viene de arriva
        .header(HttpHeaders.CONTENT_DISPOSITION, headerValue) // En la cabecera te digo que te mando un archivo como atachment
        .body(resource);

        // TOdo lo que es imagen o hipertexto va con el get
    }

    /**
     * El metodo siguiente es de ejemplo para entender el formato de JSON,
     * no tiene que ver en si con el proyecto
     */
    /*
     * @GetMapping
     * public List<String> nombres(){
     * 
     * List<String> nombres = Arrays.asList("Salma", "Judith", "Elisabet");
     * 
     * return nombres;
     * }
     */

}