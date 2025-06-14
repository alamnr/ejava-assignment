package info.ejava.assignments.api.autorentals.svc.main.renter.client.mock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;

import info.ejava.assignments.api.autorenters.client.renters.RentersAPI;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTOFactory;
import info.ejava.assignments.api.autorenters.dto.renters.RenterListDTO;
import info.ejava.assignments.api.autorenters.svc.renters.RenterService;
import info.ejava.assignments.api.autorenters.svc.renters.RentersController;
import info.ejava.assignments.api.autorenters.svc.verify.MyController;
import info.ejava.examples.common.dto.DtoUtil;
import info.ejava.examples.common.dto.JsonUtil;
import lombok.extern.slf4j.Slf4j;

//@WebMvcTest(RentersController.class)  // Test slice for mock web mvc
@WebMvcTest
@Import(RentersController.class)
//@Import(MyController.class)
//@WebMvcTest(MyController.class)
@AutoConfigureMockMvc
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Slf4j
class RenterControllerMockTest {
    
    @Autowired
    MockMvc mockMvc;

    
    static DtoUtil dtoUtil ;
    static RenterDTOFactory renterDTOFactory;
    RenterListDTO renters;

    @MockitoBean
    RenterService renterServiceMock;

    @Captor
    ArgumentCaptor<RenterDTO> dtoCaptor;

    @BeforeAll
    static void setup() {
        renterDTOFactory = new RenterDTOFactory();    
        dtoUtil = new JsonUtil();
    }

    @BeforeEach
    void init() {
            // without ID
        // renters = renterDTOFactory.listBuilder().make(5, 5);  
            // With ID
        renters = renterDTOFactory.listBuilder().make(5, 5, RenterDTOFactory.withId);
        renters.setOffset(0);
        renters.setLimit(0);
        renters.setTotal(renters.getRenters().size());
        renters.setKeywords("");
        //System.out.println("renters - "+ renters);
    }


    @Test
    //@Disabled
    void should_find_all_test() throws Exception {
        // String jsonResponse = """
        //         [ {
        //                 "id" : "2",
        //                 "firstName" : "Gertude",
        //                 "lastName" : "Mraz",
        //                 "dob" : "1971-04-07",
        //                 "email" : "gertude.mraz@gmail.com"
        //             }, {
        //                 "id" : "3",
        //                 "firstName" : "Sharice",
        //                 "lastName" : "Reichel",
        //                 "dob" : "1941-10-10",
        //                 "email" : "sharice.reichel@gmail.com"
        //             }, {
        //                 "id" : "4",
        //                 "firstName" : "Tempie",
        //                 "lastName" : "Beier",
        //                 "dob" : "1934-09-15",
        //                 "email" : "tempie.beier@hotmail.com"
        //             } ]
        //         """;

        
        String jsonResponse  = dtoUtil.marshal(renters);
        log.info("jsonResponse- {}", jsonResponse);

        BDDMockito.when(renterServiceMock.getRenters(Pageable.unpaged()))
            .thenReturn(new PageImpl<RenterDTO>(renters.getRenters(), Pageable.unpaged(),renters.getRenters().size()));
        mockMvc.perform(MockMvcRequestBuilders.get(RentersAPI.RENTERS_PATH))
       //mockMvc.perform(MockMvcRequestBuilders.get("/api/hello"))
                                            .andExpect(MockMvcResultMatchers.status().isOk())
                                            .andExpect(MockMvcResultMatchers.content().json(jsonResponse));

        verify(renterServiceMock,times(1)).getRenters(any(Pageable.class));
        BDDMockito.then(renterServiceMock).should(times(1)).getRenters(any(Pageable.class));
    }
}
