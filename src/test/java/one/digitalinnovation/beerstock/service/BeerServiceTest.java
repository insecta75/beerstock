package one.digitalinnovation.beerstock.service;
import one.digitalinnovation.beerstock.builder.BeerDTOBuilder;
import one.digitalinnovation.beerstock.dto.BeerDTO;
import one.digitalinnovation.beerstock.entity.Beer;
import one.digitalinnovation.beerstock.exception.BeerAlreadyRegisteredException;
import one.digitalinnovation.beerstock.exception.BeerNotFoundException;
import one.digitalinnovation.beerstock.exception.BeerStockExceededException;
import one.digitalinnovation.beerstock.mapper.BeerMapper;
import one.digitalinnovation.beerstock.repository.BeerRepository;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;


//Para rodar essa classe de teste Unitario, necessario usar extensao do Mockito;
@ExtendWith(MockitoExtension.class)
public class BeerServiceTest {

    private static final long INVALID_BEER_ID = 1L;

    @Mock
    private BeerRepository beerRepository;
    //BeerMapper: ajuda no desenvolvimento, para sintetizar o codigo (inserido como constante na classe);
    private BeerMapper beerMapper = BeerMapper.INSTANCE;

    @InjectMocks
    private BeerService beerService; //Para criar um objeto duble da classe, para testar indiretamente;

    @Test //Teste para criar a cerveja com sucesso;
    void whenBeerInformedThenItShouldBeCreated() throws BeerAlreadyRegisteredException {
        //given:
        BeerDTO expectedbeerDTO = BeerDTOBuilder.builder().build().toBeerDTO(); //Retorna o objeto padrao com os valores, para o teste;
        Beer expectedSavedBeer = beerMapper.toModel(expectedbeerDTO); //Foi criada uma cerverja;
        //when (o Professor fez um importe estatico do Mockito; eu deixei como esta):
        Mockito.when(beerRepository.findByName(expectedbeerDTO.getName())).thenReturn(Optional.empty());
        Mockito.when(beerRepository.save(expectedSavedBeer)).thenReturn(expectedSavedBeer);
        //then:
        BeerDTO createdBeerDTO = beerService.createBeer(expectedbeerDTO);

        //assertEquals(expectedbeerDTO.getId(), createdBeerDTO.getId());
        //assertEquals(expectedbeerDTO.getName(), createdBeerDTO.getName());
        //Hamcrest:
        MatcherAssert.assertThat(createdBeerDTO.getId(), Matchers.is(Matchers.equalTo(expectedbeerDTO.getId())));
        MatcherAssert.assertThat(createdBeerDTO.getName(), Matchers.is(Matchers.equalTo(expectedbeerDTO.getName())));
        MatcherAssert.assertThat(createdBeerDTO.getQuantity(), Matchers.is(Matchers.equalTo(expectedbeerDTO.getQuantity())));
        MatcherAssert.assertThat(createdBeerDTO.getQuantity(), Matchers.is(Matchers.greaterThan(2)));
    }
    @Test //Teste que simula uma cerveja que foi cadastrada no sistema;
    void whenAlreadyRegisteredBeerInformedThenAnExceptionShouldBeThrown() throws BeerAlreadyRegisteredException {
        //given:
        BeerDTO expectedbeerDTO = BeerDTOBuilder.builder().build().toBeerDTO(); //Retorna o objeto padrao com os valores, para o teste;
        Beer duplicatedBeer = beerMapper.toModel(expectedbeerDTO); //Foi criada uma cerverja;
        //when:
        Mockito.when(beerRepository.findByName(expectedbeerDTO.getName())).thenReturn(Optional.of(duplicatedBeer));
        //then (verifica que a excecao foi lancada e tratada):
        //beerService.createBeer(expectedbeerDTO);
        assertThrows(BeerAlreadyRegisteredException.class, () -> beerService.createBeer(expectedbeerDTO));
    }
    @Test //Teste que simula busca de cervejas;
    void whenValidBeerNameIsGivenThenReturnABeer() throws BeerNotFoundException {
        //given:
        BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO(); //Retorna o objeto padrao com os valores, para o teste;
        Beer expectedFoundBeer = beerMapper.toModel(expectedFoundBeerDTO); //Foi criada uma cerverja;
        //when:
        Mockito.when(beerRepository.findByName(expectedFoundBeer.getName())).thenReturn(Optional.of(expectedFoundBeer));
        //then:
        BeerDTO foundBeerDTO = beerService.findByName(expectedFoundBeerDTO.getName());
        MatcherAssert.assertThat(foundBeerDTO, Matchers.is(expectedFoundBeerDTO));
    }
    @Test //Teste que simula a busca de uma cerveja que nao esta cadastrada no sistema;
    void whenNotRegisteredBeerNameIsGivenThenThrowAnException() {
        //given (nao vai ter acesso a uma entidade de fato):
        BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        //when:
        Mockito.when(beerRepository.findByName(expectedFoundBeerDTO.getName())).thenReturn(Optional.empty());
        //then (a excecao sera lancada no caso de uma cerveja nao cadastrada):
        assertThrows(BeerNotFoundException.class, ()-> beerService.findByName(expectedFoundBeerDTO.getName()));
    }
    @Test //Teste simula o retorno de uma lista de cervejas;
    void whenListBeerIsCalledThenReturnAListOfBeers() {
        //given:
        BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedFoundBeer = beerMapper.toModel(expectedFoundBeerDTO);
        //when:
        Mockito.when(beerRepository.findAll()).thenReturn(Collections.singletonList(expectedFoundBeer));
        //then:
        List<BeerDTO> foundListBeerDTO = beerService.listAll();
        MatcherAssert.assertThat(foundListBeerDTO, Matchers.is(not(empty())));
        MatcherAssert.assertThat(foundListBeerDTO.get(0), Matchers.is(equalTo(expectedFoundBeerDTO)));
    }
    @Test //Teste simula o nao retorno de uma lista de cervejas;
    void whenListBeerIsCalledThenReturnAnEmptyListOfBeers() {
        //when:
        Mockito.when(beerRepository.findAll()).thenReturn(Collections.EMPTY_LIST);
        //then:
        List<BeerDTO> foundListBeerDTO = beerService.listAll();
        MatcherAssert.assertThat(foundListBeerDTO, Matchers.is(empty()));
    }
    @Test //Teste simula a remocao de uma cerveja da lista;
    void whenExclusionIsCalledWithValidIdThenABeerShouldBeDeleted() throws BeerNotFoundException{
        //given:
        BeerDTO expectedDeletedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedDeletedBeer = beerMapper.toModel(expectedDeletedBeerDTO);
        //when:
        Mockito.when(beerRepository.findById(expectedDeletedBeerDTO.getId())).thenReturn(Optional.of(expectedDeletedBeer));
        Mockito.doNothing().when(beerRepository).deleteById(expectedDeletedBeerDTO.getId());
        //then:
        beerService.deleteById(expectedDeletedBeerDTO.getId());
        Mockito.verify(beerRepository, Mockito.times(1)).findById(expectedDeletedBeerDTO.getId());
        Mockito.verify(beerRepository, Mockito.times(1)).deleteById(expectedDeletedBeerDTO.getId());
    }
    @Test //Teste simula o aumento de cervejas na lista;
    void whenIncrementIsCalledThenIncrementBeerStock() throws BeerNotFoundException, BeerStockExceededException {
        //given:
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);
        //when:
        Mockito.when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
        Mockito.when(beerRepository.save(expectedBeer)).thenReturn(expectedBeer);
        int quantityToIncrement = 10; //Quantas cervejas serao aumentadas;
        int expectedQuantityAfterIncrement = expectedBeerDTO.getQuantity() + quantityToIncrement;
        //then:
        BeerDTO incrementedBeerDTO = beerService.increment(expectedBeerDTO.getId(), quantityToIncrement);
        MatcherAssert.assertThat(expectedQuantityAfterIncrement, equalTo(incrementedBeerDTO.getQuantity()));
        MatcherAssert.assertThat(expectedQuantityAfterIncrement, lessThan(expectedBeerDTO.getMax()));
    }
    @Test //Teste simula a excecao quando ocorre um aumento maior do limite;
    void whenIncrementIsGreatherThanMaxThenThrowException() {
        //given:
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);
        //when:
        Mockito.when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
        //then:
        int quantityToIncrement = 80;
        assertThrows(BeerStockExceededException.class, () -> beerService.increment(expectedBeerDTO.getId(), quantityToIncrement));
    }
    @Test
    void whenIncrementAfterSumIsGreatherThanMaxThenThrowException() {
        //given:
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);
        //when:
        Mockito.when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
        //then:
        int quantityToIncrement = 45;
        assertThrows(BeerStockExceededException.class, () -> beerService.increment(expectedBeerDTO.getId(), quantityToIncrement));
    }

    @Test //Teste simula um aumento com um ID invalido;
    void whenIncrementIsCalledWithInvalidIdThenThrowException() {
        int quantityToIncrement = 10;
        //when:
        Mockito.when(beerRepository.findById(INVALID_BEER_ID)).thenReturn(Optional.empty());
        //then:
        assertThrows(BeerNotFoundException.class, () -> beerService.increment(INVALID_BEER_ID, quantityToIncrement));
    }
}