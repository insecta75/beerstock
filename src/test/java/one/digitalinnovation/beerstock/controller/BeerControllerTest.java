package one.digitalinnovation.beerstock.controller;
import one.digitalinnovation.beerstock.builder.BeerDTOBuilder;
import one.digitalinnovation.beerstock.dto.BeerDTO;
import one.digitalinnovation.beerstock.dto.QuantityDTO;
import one.digitalinnovation.beerstock.exception.BeerNotFoundException;
import one.digitalinnovation.beerstock.service.BeerService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import java.util.Collections;
import static one.digitalinnovation.beerstock.utils.JsonConvertionUtils.asJsonString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(MockitoExtension.class)
public class BeerControllerTest {
    private static final String BEER_API_URL_PATH = "/api/v1/beers";
    private static final long VALID_BEER_ID = 1L;
    private static final long INVALID_BEER_ID = 2l;
    private static final String BEER_API_SUBPATH_INCREMENT_URL = "/increment";
    private static final String BEER_API_SUBPATH_DECREMENT_URL = "/decrement";

    private MockMvc mockMvc;

    @Mock
    private BeerService beerService;

    @InjectMocks
    private BeerController beerController;

    @BeforeEach
    void setUp() { //Antes de cada teste, necessario fazer a config do MockMvc;
        mockMvc = MockMvcBuilders.standaloneSetup(beerController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setViewResolvers((s, locale) -> new MappingJackson2JsonView()) //Mapeamento de Jackson para JSON;
                .build();
    }

    @Test //Teste de criacao de cerveja com sucesso;
    void whenPOSTIsCalledThenABeerIsCreated() throws Exception {
        //given:
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO(); //Retorna o objeto padrao com os valores, para o teste;
        //when:
        Mockito.when(beerService.createBeer(beerDTO)).thenReturn(beerDTO);
        //then:
        mockMvc.perform(post(BEER_API_URL_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(beerDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", Matchers.is(beerDTO.getName())))
                .andExpect(jsonPath("$.brand", Matchers.is(beerDTO.getBrand())))
                .andExpect(jsonPath("$.type", Matchers.is(beerDTO.getType().toString())));
    }
    @Test //Teste de criacao de cerveja sem sucesso (mostrando mensagem de erro na falta de um atributo);
    void whenPOSTIsCalledWithoutRequiredFieldThenAnErrorIsReturned() throws Exception {
        //given:
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        beerDTO.setBrand(null); //Tira a marca da cerveja;
        //then:
        mockMvc.perform(post(BEER_API_URL_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(beerDTO)))
                .andExpect(status().isBadRequest());
    }
    @Test //Teste busca de cerveja;
    void whenGETIsCalledWithValidNameThenOkStatusIsReturned() throws Exception {
        //given:
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        //when:
        Mockito.when(beerService.findByName(beerDTO.getName())).thenReturn(beerDTO);
        //then:
        mockMvc.perform(MockMvcRequestBuilders.get(BEER_API_URL_PATH+"/"+beerDTO.getName())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", Matchers.is(beerDTO.getName())))
                    .andExpect(jsonPath("$.brand", Matchers.is(beerDTO.getBrand())))
                    .andExpect(jsonPath("$.type", Matchers.is(beerDTO.getType().toString())));
    }
    @Test //Teste quando na busca nao e encontrado: 404
    void whenGETIsCalledWithoutRegisteredNameThenNotFoundStatusIsReturned() throws Exception {
        //given:
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        //when:
        Mockito.when(beerService.findByName(beerDTO.getName())).thenThrow(BeerNotFoundException.class);
        //then:
        mockMvc.perform(MockMvcRequestBuilders.get(BEER_API_URL_PATH+"/"+beerDTO.getName())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
    @Test //Teste simula o retorno com um ok de uma lista de cervejas;
    void whenGETListWithBeersIsCalledThenOkStatusIsReturned() throws Exception {
        //given:
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        //when:
        Mockito.when(beerService.listAll()).thenReturn(Collections.singletonList(beerDTO));
        //then:
        mockMvc.perform(MockMvcRequestBuilders.get(BEER_API_URL_PATH)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", Matchers.is(beerDTO.getName()))) //Retorna da lista a existencia do primeiro valor;
                .andExpect(jsonPath("$[0].brand", Matchers.is(beerDTO.getBrand())))
                .andExpect(jsonPath("$[0].type", Matchers.is(beerDTO.getType().toString())));
    }
    @Test //Teste simula o retorno com um ok de uma lista vazia de cervejas;
    void whenGETListWithoutBeersIsCalledThenOkStatusIsReturned() throws Exception {
        //given:
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        //when:
        Mockito.when(beerService.listAll()).thenReturn(Collections.singletonList(beerDTO));
        //then:
        mockMvc.perform(MockMvcRequestBuilders.get(BEER_API_URL_PATH)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
    @Test //Teste simula a remocao de uma cerveja (com um ID valido);
    void whenDELETEIsCalledWithValidIdThenNoContentStatusIsReturned() throws Exception {
        //given:
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        //when:
        Mockito.doNothing().when(beerService).deleteById(beerDTO.getId());
        //then:
        mockMvc.perform(MockMvcRequestBuilders.delete(BEER_API_URL_PATH+"/"+beerDTO.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
    @Test //Teste simula a remocao de uma cerveja (com um ID invalido);
    void whenDELETEIsCalledWithInvalidIdThenNotFoundStatusIsReturned() throws Exception {
        //when (vai lancar uma excecao para um ID invalido):
        Mockito.doThrow(BeerNotFoundException.class).when(beerService).deleteById(INVALID_BEER_ID);
        //then:
        mockMvc.perform(MockMvcRequestBuilders.delete(BEER_API_URL_PATH+"/"+INVALID_BEER_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
    @Test //Teste uma operacao de incremento;
    void whenPATCHIsCalledToIncrementDiscountThenOKstatusIsReturned() throws Exception {
        //given:
        QuantityDTO quantityDTO = QuantityDTO.builder()
                .quantity(10)
                .build();
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        beerDTO.setQuantity(beerDTO.getQuantity() + quantityDTO.getQuantity());
        //when (ID valido com o incremento):
        Mockito.when(beerService.increment(VALID_BEER_ID, quantityDTO.getQuantity())).thenReturn(beerDTO);
        //then:
        mockMvc.perform(MockMvcRequestBuilders.patch(BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_INCREMENT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(quantityDTO))).andExpect(status().isOk())
                .andExpect(jsonPath("$.name", Matchers.is(beerDTO.getName())))
                .andExpect(jsonPath("$.brand", Matchers.is(beerDTO.getBrand())))
                .andExpect(jsonPath("$.type", Matchers.is(beerDTO.getType().toString())))
                .andExpect(jsonPath("$.quantity", Matchers.is(beerDTO.getQuantity())));
    }
}