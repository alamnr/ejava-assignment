package info.ejava.assignments.api.autorentals.svc.main.renter.client.mock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import info.ejava.assignments.api.autorenters.client.renters.RentersAPI;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTOFactory;
import info.ejava.assignments.api.autorenters.dto.renters.RenterListDTO;
import info.ejava.assignments.api.autorenters.svc.renters.RentersService;
import info.ejava.assignments.api.autorenters.svc.renters.RentersController;
import info.ejava.assignments.api.autorenters.svc.renters.RentersExceptionAdvice;
import info.ejava.examples.common.dto.DtoUtil;
import info.ejava.examples.common.dto.JsonUtil;
import info.ejava.examples.common.exceptions.ClientErrorException;
import lombok.extern.slf4j.Slf4j;

//@WebMvcTest(RentersController.class)  // Test slice for mock web mvc
@WebMvcTest
@Import(value = {RentersController.class,RentersExceptionAdvice.class})
//@Import(MyController.class)
//@WebMvcTest(MyController.class)
@AutoConfigureMockMvc
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Slf4j
public class RenterControllerMockTest {
    
    @Autowired
    MockMvc mockMvc;

    
    static DtoUtil dtoUtil ;
    static RenterDTOFactory renterDTOFactory;
    RenterListDTO renters;

    @MockitoBean
    RentersService renterServiceMock;

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
    void should_find_all_renters() throws Exception {
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

    @Test
    void should_find_given_renter_by_valid_id() throws Exception{

        String jsonResponse = dtoUtil.marshal(renters.getRenters().get(0));
        BDDMockito.when(renterServiceMock.getRenter(anyString())).thenReturn(renters.getRenters().get(0));

        mockMvc.perform(MockMvcRequestBuilders.get(RentersAPI.RENTER_PATH.replace("{id}", renters.getRenters().get(0).getId())))
                            .andExpect(MockMvcResultMatchers.status().isOk())
                            .andExpect(MockMvcResultMatchers.content().json(jsonResponse));
    }

    @Test
    void should_not_find_given_renter_by_invalid_id() throws Exception{

        BDDMockito.when(renterServiceMock.getRenter(anyString()))
                    .thenThrow(ClientErrorException.NotFoundException.class);
        String uri = RentersAPI.RENTER_PATH.replace("{id}", renters.getRenters().get(0).getId());
        log.info("path - {}",uri);
        mockMvc.perform(MockMvcRequestBuilders.get(uri))
                            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void should_create_new_post_when_renter_is_valid() throws Exception{
        RenterDTO withID = renters.getRenters().get(0);
        RenterDTO withNullID = withID.withId(null);

        String json = dtoUtil.marshal(withNullID);
        //BDDMockito.when(renterServiceMock.createRenter(any(RenterDTO.class))).thenReturn(renters.getRenters().get(0));
        BDDMockito.when(renterServiceMock.createRenter(withNullID)).thenReturn(withID);
        mockMvc.perform(MockMvcRequestBuilders.post(RentersAPI.RENTERS_PATH)
                        .contentType("application/json")
                        .content(json))
                        .andExpect(MockMvcResultMatchers.status().isCreated());
    }

     @Test
    void should_create_new_post_when_renter_is_invalid() throws Exception{
        RenterDTO withID = renters.getRenters().get(0);
        RenterDTO withNullInvalidID = withID.withId(null);
        withNullInvalidID.withFirstName("");
        withNullInvalidID.withEmail("");

        String json = dtoUtil.marshal(withNullInvalidID);
        
        BDDMockito.when(renterServiceMock.createRenter(withNullInvalidID)).thenThrow(ClientErrorException.InvalidInputException.class);
        mockMvc.perform(MockMvcRequestBuilders.post(RentersAPI.RENTERS_PATH)
                        .contentType("application/json")
                        .content(json))
                        .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
    }


    @Test
    void should_update_post_when_given_valid_post() throws Exception {
        RenterDTO renterWithoutId = renterDTOFactory.listBuilder().make(1, 1).getRenters().get(0);
        renterWithoutId = renterWithoutId.withFirstName("updated - "+renterWithoutId.getFirstName());
        String id = "renter-123";
        RenterDTO renterWithId = renterWithoutId.withId(id);
        String json = dtoUtil.marshal(renterWithoutId);
        BDDMockito.when(renterServiceMock.updateRenter(id, renterWithoutId)).thenReturn(renterWithId);
        String uri = RentersAPI.RENTER_PATH.replace("{id}", id);
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                                    .contentType("application/json")
                                    .content(json))
                                    .andExpect(MockMvcResultMatchers.status().isOk());



    }

    @Test
    void should_delete_renter_with_given_id() throws Exception {

        String uri = RentersAPI.RENTER_PATH.replace("{id}", "renter-1");
        mockMvc.perform(MockMvcRequestBuilders.delete(uri))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
        
        verify(renterServiceMock,times(1)).removeRenter(anyString());
        BDDMockito.then(renterServiceMock).should(times(1)).removeRenter(anyString());
    }

    
    @Test
    void should_delete_all_renters() throws Exception {

        
        mockMvc.perform(MockMvcRequestBuilders.delete(RentersAPI.RENTERS_PATH))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
        
        verify(renterServiceMock,times(1)).removeAllRenters();
        BDDMockito.then(renterServiceMock).should(times(1)).removeAllRenters();
    }

    @Test
    void should_check_has_renter() throws Exception {
        BDDMockito.when(renterServiceMock.hasRenter(anyString())).thenReturn(true);
        String uri = RentersAPI.RENTER_PATH.replace("{id}", "renter-123");
        mockMvc.perform(MockMvcRequestBuilders.head(uri))
                .andExpect(MockMvcResultMatchers.status().isOk());

        BDDMockito.when(renterServiceMock.hasRenter(anyString())).thenReturn(false);
        String uri2 = RentersAPI.RENTER_PATH.replace("{id}", "renter-321");
        mockMvc.perform(MockMvcRequestBuilders.head(uri2))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        verify(renterServiceMock,times(2)).hasRenter(anyString());
        BDDMockito.then(renterServiceMock).should(times(2)).hasRenter(anyString());
    
    }

}
