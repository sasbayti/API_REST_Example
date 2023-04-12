package com.example.controllers;

import static org.mockito.ArgumentMatchers.any;
// Para seguir el enfoque BDD con Mockito
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.example.entities.FileUploadUtil;
import com.example.entities.Presentacion;
import com.example.entities.Producto;
import com.example.services.ProductoService;
import com.example.utilities.FileDownloadUtil;
import com.fasterxml.jackson.databind.ObjectMapper;


//@WebMvcTest No levanta contexto de Spring, que es el contexto? Spring lo mas importante es el core que tiene le contexto 
// La interfaz madre es application context, segun el tipo de aplicacion que estes usando, antes era BeanFactory pero ahora es 
// application COntext que levanta el contexto independientemente de la aplicacion que se le vaya a dar
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = Replace.NONE) // Los test los haria de la base de datos en memoria, replace none para que no sustituya la base de datos del producto
// replace es un atributo
// Para que la use tal cual está
// Por defecto quiere decir que la va usar pero pueda que no use los registros 
public class ProductoControllerTests {
    
    @Autowired
    private MockMvc mockMvc; // 

    @MockBean // Simulame este bean
    private ProductoService productoService;

    @Autowired
    private ObjectMapper objectMapper; //para serializar, cuando quiero hacer una peticion de http, si le mando un json de producto
    // a nivel de producto creo el objeto pero en ningun momento lo he convertido json
    // Serializar es convertir a un flujo json o, deserializar, de un flujo json a java

    @MockBean
    private FileUploadUtil fileUploadUtil;

    @MockBean
    private FileDownloadUtil fileDownloadUtil;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders
                    .webAppContextSetup(context)
                    .apply(springSecurity()).build();
            
    }
    // COn esto ya tengo creado el contexto
    @Test
    void testGuardarProducto() throws Exception {
        // given
        Presentacion presentacion = Presentacion.builder()
                .descripcion(null)
                .nombre("docena")
                .build();

        Producto producto = Producto.builder()
                .id(34L)
                .nombre("Camara")
                .descripcion("Resolucion Alta")
                .precio(2000.00)
                .stock(40)
                .presentacion(presentacion)
                .build();

        given(productoService.save(any(Producto.class)))
                .willAnswer(invocation -> invocation.getArgument(0)); // Vas a devolver el primer producto que tiene alli
                // COn MOckMVc le informo de lo que voy a mandar, objectmapper lo convierte a un flujo a un json
      // when
      String jsonStringProduct = objectMapper.writeValueAsString(producto);
      System.out.println(jsonStringProduct);
      ResultActions response = mockMvc
              .perform(post("/productos")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(jsonStringProduct));

      // then
      // Se espera a que el endpoint te de que no estes autorizado
      response.andDo(print())
              .andExpect(status().isUnauthorized());
      // .andExpect()
      // .andExpect(jsonPath("$.nombre", is(producto.getNombre())))
      // .andExpect(jsonPath("$.descripcion", is(producto.getDescripcion())));

  }
}