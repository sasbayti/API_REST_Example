package com.example.services;

import static org.assertj.core.api.Assertions.assertThat;
// Para seguir el enfoque de BDD con Mockito
import static org.mockito.BDDMockito.given;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

import com.example.dao.PresentacionDao;
import com.example.dao.ProductoDao;
import com.example.entities.Presentacion;
import com.example.entities.Producto;

@ExtendWith(MockitoExtension.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class ProductoServiceTests {
    // Vamos a simular la inyeccion porque no ncesita levantar toda la aplicacion
    // para implementar la capa
    @Mock
    private ProductoDao productoDao;
    @Mock
    private PresentacionDao presentacionDao;

    // Ponemos la implementacion porque cuando se simula algo se pone la clase
    @InjectMocks
    private ProductoServiceImpl productoService;

    private Producto producto;

    @BeforeEach
    void setUp() {
        Presentacion presentacion = Presentacion.builder().descripcion(null)
                .nombre("unidades").build();

        producto = Producto.builder().id(20L)
                .nombre("Google Pixel 7")
                .descripcion("Telefono de google")
                .precio(800.0)
                .stock(1000)
                .imagenProducto(null)
                .presentacion(presentacion)
                .build();
    }

    @Test
    @DisplayName("Test de servicio para persistir un producto")
    public void testSaveProducto() {

        // given

        given(productoDao.save(producto)).willReturn(producto);

        // when

        Producto productoSaved = productoService.save(producto);

        // Then
        assertThat(productoSaved).isNotNull();
    }

    @DisplayName("Recupera una lista vacia de productos")
    @Test
    public void testEmptyProductList() {

        // given
        given(productoDao.findAll()).willReturn(Collections.emptyList());

        // When
        List<Producto> productos = productoDao.findAll();

        // then
        assertThat(productos).isEmpty();
    }
}
