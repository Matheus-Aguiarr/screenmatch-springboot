package br.com.alura.screenmatch.main;


import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.text.html.Option;
import java.util.*;
import java.util.stream.Collectors;


public class Main {
    private Scanner sc = new Scanner(System.in);
    private final ConsumoApi consumo = new ConsumoApi();
    private final ConverteDados conversor = new ConverteDados();

    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=c8ba3d41";
    private List<DadosSerie> dadosSeries = new ArrayList<>();

    private Optional<Serie> serieBusca;

    @Autowired
    private SerieRepository repositorio;

    private List<Serie> series = new ArrayList<>();

    public Main(SerieRepository repositorio) {
        this.repositorio = repositorio;
    }


    public void exibeMenu() {

        var opcao = -1;
        while (opcao != 0 ) {
            var menu = """
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - Listar séries buscadas
                    4 - Buscar serie por titulo
                    5 - Buscar series por ator
                    6 - Top 5 series
                    7 - Buscar series por categoria 
                    8 - Filtrar Series
                    9 - Buscar episodio por trechos
                    10 - Buscar top 5 episodios por serie
                    11 - Buscar episodios a partir de uma data 
                   \s
                    0 - Sair                                \s
                   \s""";

            System.out.println(menu);

            opcao = sc.nextInt();
            sc.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarSeriePorAtor();
                    break;
                case 6:
                    buscarTop5Series();
                    break;
                case 7:
                    buscarSeriesPorCategoria();
                    break;
                case 8:
                    filtrarSeries();
                    break;
                case 9:
                    buscarEpisodioPorTrecho();
                    break;
                case 10: 
                    topEpisodiosPorSerie();
                    break;
                case 11:
                    buscarEpisodiosPorData();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    private void buscarEpisodiosPorData() {
        buscarSeriePorTitulo();
        if (serieBusca.isPresent()) {
            Serie serie = serieBusca.get();
            System.out.println("Qual o ano de lancamento");
            var anoLancamento = sc.nextInt();
            sc.nextLine();

            List<Episodio> episodiosAno = repositorio.episodioPorSerieEAno(serie, anoLancamento);
            episodiosAno.forEach(System.out::println);
        }
    }

    private void topEpisodiosPorSerie() {
        buscarSeriePorTitulo();
        if (serieBusca.isPresent()) {
            Serie serie = serieBusca.get();
            List<Episodio> topEpisodios = repositorio.topEpisodiosPorSerie(serie);
            topEpisodios.forEach(e -> System.out.printf("Serie: %s Temporada %s - Episodio %s - %s Avaliacao: %s\n", e.getSerie().getTitulo(), e.getTemporada(), e.getNumeroDoEpisodio(), e.getTitulo(), e.getAvaliacao()));
        }
    }

    private void buscarEpisodioPorTrecho() {
        System.out.println("Qual o nome do episodio para busca? ");
        var trechoEpisodio = sc.nextLine();
        List<Episodio> episodiosEncontrados = repositorio.episodiosPorTrecho(trechoEpisodio);
        episodiosEncontrados.forEach(e -> System.out.printf("Serie: %s Temporada %s - Episodio %s - %s Avaliacao: %s\n", e.getSerie().getTitulo(), e.getTemporada(), e.getNumeroDoEpisodio(), e.getTitulo(), e.getAvaliacao()));
    }

    private void filtrarSeries() {
        System.out.println("Qual o total de temporadas? ");
        var totalTemporadas = sc.nextInt();
        System.out.println("Qual a avaliacao minima? ");
        var avaliacaoMinima = sc.nextDouble();
        List<Serie> filterSerie = repositorio.findSeriePorTemporadaEAvaliacao(totalTemporadas, avaliacaoMinima);
        filterSerie.forEach(s -> System.out.println("Serie: " + s.getTitulo() + " avaliacao: " + s.getAvaliacao()));
    }

    private void buscarSeriesPorCategoria() {
        System.out.println("Deseja buscar series de que categoria/genero?");
        var nomeGenero = sc.nextLine();
        Categoria categoria = Categoria.fromPortugues(nomeGenero);
        List<Serie> seriesPorCategoria = repositorio.findByGenero(categoria);
        System.out.println("Series da categoria: " + nomeGenero);
        seriesPorCategoria.forEach(System.out::println);
    }

    private void buscarTop5Series() {
        List<Serie> serieTop = repositorio.findTop5ByOrderByAvaliacaoDesc();
        serieTop.forEach(s -> System.out.println(s.getTitulo() + " avaliacao: " + s.getAvaliacao()));
    }

    private void buscarSeriePorAtor() {
        System.out.println("Qual o nome para busca: ");
        var nomeAtor = sc.nextLine();
        List<Serie> seriesEcontradas = repositorio.findByAtoresContainingIgnoreCase(nomeAtor);
        System.out.println("Series em que " + nomeAtor + " Trabalhou: ");
        seriesEcontradas.forEach(s -> System.out.println(s.getTitulo()));
    }

    private void buscarSeriePorTitulo() {
        System.out.println("Escolha uma serie pelo nome:  ");
        var nomeSerie = sc.nextLine();
        serieBusca = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if(serieBusca.isPresent()) {
            System.out.println("Dados da serie: ");
            System.out.println(serieBusca.get());
        } else {
            System.out.println("Serie nao encontrada.");
        }
    }

    private void listarSeriesBuscadas() {
        series = repositorio.findAll(); //Busca tudo que esta cadastrado no repositorio
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        Serie serie = new Serie(dados);
        repositorio.save(serie);
        
        System.out.println(dados);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = sc.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void buscarEpisodioPorSerie() {
        listarSeriesBuscadas();
        System.out.println("Escolha uma série pelo nome: ");
        var nomeSerie = sc.nextLine();

        Optional<Serie> serie = repositorio.findByTituloContainingIgnoreCase(nomeSerie);
        if (serie.isPresent()) {

            var serieEncontrada = serie.get();
            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream().flatMap(d -> d.episodios().stream().map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());
            serieEncontrada.setEpisodios(episodios);
            repositorio.save(serieEncontrada);
        } else {
            System.out.println("Serie nao encontrada");
        }
    }
}

