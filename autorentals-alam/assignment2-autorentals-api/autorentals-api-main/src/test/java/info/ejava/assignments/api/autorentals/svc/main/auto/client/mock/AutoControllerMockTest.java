package info.ejava.assignments.api.autorentals.svc.main.auto.client.mock;

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
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import info.ejava.assignments.api.autorenters.client.autos.AutosAPI;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoDTOFactory;
import info.ejava.assignments.api.autorenters.dto.autos.AutoListDTO;
import info.ejava.assignments.api.autorenters.dto.autos.AutoSearchParams;
import info.ejava.assignments.api.autorenters.svc.autos.AutosController;
import info.ejava.assignments.api.autorenters.svc.autos.AutosExceptionAdvice;
import info.ejava.assignments.api.autorenters.svc.autos.AutosService;
import info.ejava.examples.common.dto.DtoUtil;
import info.ejava.examples.common.dto.JsonUtil;
import info.ejava.examples.common.exceptions.ClientErrorException;
import lombok.extern.slf4j.Slf4j;

//@WebMvcTest(AutosController.class)  // Test slice for mock web mvc
@WebMvcTest
@Import(value = {AutosController.class,AutosExceptionAdvice.class})
@AutoConfigureMockMvc
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Slf4j
public class AutoControllerMockTest {
    
    @Autowired
    MockMvc mockMvc;

    
    static DtoUtil dtoUtil ;
    static AutoDTOFactory autoDTOFactory;
    AutoListDTO autos;

    @MockitoBean
    AutosService autosServiceMock;

    @Captor
    ArgumentCaptor<AutoDTO> dtoCaptor;

    @BeforeAll
    static void setup() {
        autoDTOFactory = new AutoDTOFactory();    
        dtoUtil = new JsonUtil();
    }

    @BeforeEach
    void init() {
            // without ID
        // autos = autoDTOFactory.listBuilder().make(5, 5);  
            // With ID
        autos = autoDTOFactory.listBuilder().make(5, 5, AutoDTOFactory.withId);
        //autos.setOffset(0);
        //autos.setLimit(0);
        autos.setTotal(autos.getAutos().size());
        autos.setKeywords("");
        //System.out.println("autos - "+ autos);
    }


    @Test
    //@Disabled
    void should_find_all_autos() throws Exception {
        
        
        String jsonResponse  = dtoUtil.marshal(autos);
        log.info("jsonResponse- {}", jsonResponse);

        BDDMockito.when(autosServiceMock.searchAutos(any(AutoSearchParams.class),any(Pageable.class)))
            .thenReturn(new PageImpl<AutoDTO>(autos.getAutos(), Pageable.unpaged(),autos.getAutos().size()));
        mockMvc.perform(MockMvcRequestBuilders.get(AutosAPI.AUTOS_PATH))
                                            .andDo(MockMvcResultHandlers.print())
                                            .andExpect(MockMvcResultMatchers.status().isOk())
                                            .andExpect(MockMvcResultMatchers.content().json(jsonResponse));

        verify(autosServiceMock,times(1)).searchAutos(any(AutoSearchParams.class),any(Pageable.class));
        BDDMockito.then(autosServiceMock).should(times(1)).searchAutos(any(AutoSearchParams.class),any(Pageable.class));
    }

    @Test
    void should_find_given_auto_by_valid_id() throws Exception{

        String jsonResponse = dtoUtil.marshal(autos.getAutos().get(0));
        BDDMockito.when(autosServiceMock.getAuto(anyString())).thenReturn(autos.getAutos().get(0));

        mockMvc.perform(MockMvcRequestBuilders.get(AutosAPI.AUTO_PATH.replace("{id}", autos.getAutos().get(0).getId())))
                            .andExpect(MockMvcResultMatchers.status().isOk())
                            .andExpect(MockMvcResultMatchers.content().json(jsonResponse));
    }

    @Test
    void should_not_find_given_auto_by_invalid_id() throws Exception{

        BDDMockito.when(autosServiceMock.getAuto(anyString()))
                    .thenThrow(ClientErrorException.NotFoundException.class);
        String uri = AutosAPI.AUTO_PATH.replace("{id}", autos.getAutos().get(0).getId());
        log.info("path - {}",uri);
        mockMvc.perform(MockMvcRequestBuilders.get(uri))
                            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void should_create_new_post_when_auto_is_valid() throws Exception{
        AutoDTO withID = autos.getAutos().get(0);
        AutoDTO withNullID = withID.withId(null);

        String json = dtoUtil.marshal(withNullID);
        //BDDMockito.when(autosServiceMock.createRenter(any(AutoDTO.class))).thenReturn(autos.getRenters().get(0));
        BDDMockito.when(autosServiceMock.createAuto(withNullID)).thenReturn(withID);
        mockMvc.perform(MockMvcRequestBuilders.post(AutosAPI.AUTOS_PATH)
                        .contentType("application/json")
                        .content(json))
                        .andExpect(MockMvcResultMatchers.status().isCreated());
    }

     @Test
    void should_not_create_auto_when_auto_is_invalid() throws Exception{
        AutoDTO withID = autos.getAutos().get(0);
        AutoDTO withNullInvalidID = withID.withId(null);
        withNullInvalidID.withDailyRate(null);
        

        String json = dtoUtil.marshal(withNullInvalidID);
        
        BDDMockito.when(autosServiceMock.createAuto(withNullInvalidID)).thenThrow(ClientErrorException.InvalidInputException.class);
        mockMvc.perform(MockMvcRequestBuilders.post(AutosAPI.AUTOS_PATH)
                        .contentType("application/json")
                        .content(json))
                        .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
    }

    @Test
    void should_update_auto_when_given_valid_auto() throws Exception {
        AutoDTO autoWithoutId = autoDTOFactory.listBuilder().make(1, 1).getAutos().get(0);
        autoWithoutId = autoWithoutId.withUsername("updated - "+autoWithoutId.getUsername());
        String id = "auto-123";
        AutoDTO renterWithId = autoWithoutId.withId(id);
        String json = dtoUtil.marshal(autoWithoutId);
        BDDMockito.when(autosServiceMock.updateAuto(id, autoWithoutId)).thenReturn(renterWithId);
        String uri = AutosAPI.AUTO_PATH.replace("{id}", id);
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                                    .contentType("application/json")
                                    .content(json))
                                    .andExpect(MockMvcResultMatchers.status().isOk());



    }

    @Test
    void should_delete_auto_with_given_id() throws Exception {

        String uri = AutosAPI.AUTO_PATH.replace("{id}", "auto-1");
        mockMvc.perform(MockMvcRequestBuilders.delete(uri))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
        
        verify(autosServiceMock,times(1)).removeAuto(anyString());
        BDDMockito.then(autosServiceMock).should(times(1)).removeAuto(anyString());
    }

    
    @Test
    void should_delete_all_autos() throws Exception {

        
        mockMvc.perform(MockMvcRequestBuilders.delete(AutosAPI.AUTOS_PATH))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
        
        verify(autosServiceMock,times(1)).removeAllAutos();
        BDDMockito.then(autosServiceMock).should(times(1)).removeAllAutos();
    }
    

    @Test
    void should_check_has_auto() throws Exception {
        BDDMockito.when(autosServiceMock.hasAuto(anyString())).thenReturn(true);
        String uri = AutosAPI.AUTO_PATH.replace("{id}", "auto-123");
        mockMvc.perform(MockMvcRequestBuilders.head(uri))
                .andExpect(MockMvcResultMatchers.status().isOk());

        BDDMockito.when(autosServiceMock.hasAuto(anyString())).thenReturn(false);
        String uri2 = AutosAPI.AUTO_PATH.replace("{id}", "auto-321");
        mockMvc.perform(MockMvcRequestBuilders.head(uri2))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        verify(autosServiceMock,times(2)).hasAuto(anyString());
        BDDMockito.then(autosServiceMock).should(times(2)).hasAuto(anyString());
    
    }

    }
