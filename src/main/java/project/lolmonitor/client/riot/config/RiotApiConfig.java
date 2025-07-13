package project.lolmonitor.client.riot.config;

import java.io.IOException;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import lombok.extern.slf4j.Slf4j;
import project.lolmonitor.client.riot.api.RiotAsiaApi;
import project.lolmonitor.client.riot.api.RiotDataDragonApi;
import project.lolmonitor.client.riot.api.RiotKoreaApi;

@Slf4j
@Configuration
public class RiotApiConfig {

	@Value("${riot.api.key}")
	private String riotApiKey;

	private static final Duration CONNECTION_TIMEOUT = Duration.ofSeconds(5);
	private static final Duration READ_TIMEOUT = Duration.ofSeconds(10);

	@Bean
	public RestClient riotAsiaRestClient() {
		return RestClient.builder()
			.baseUrl("https://asia.api.riotgames.com")
			.defaultHeader("X-Riot-Token", riotApiKey)
			.requestFactory(riotClientHttpRequestFactory())
			.requestInterceptor(this::logRequest)
			.build();
	}

	@Bean
	public RestClient riotKoreaRestClient() {
		return RestClient.builder()
			.baseUrl("https://kr.api.riotgames.com")
			.defaultHeader("X-Riot-Token", riotApiKey)
			.requestFactory(riotClientHttpRequestFactory())
			.requestInterceptor(this::logRequest)
			.build();
	}

	@Bean
	public RestClient riotDataDragonRestClient() {
		return RestClient.builder()
			.baseUrl("https://ddragon.leagueoflegends.com")
			.requestFactory(riotClientHttpRequestFactory())
			.requestInterceptor(this::logRequest)
			.build();
	}

	@Bean
	public RiotAsiaApi riotAsiaApi(@Qualifier("riotAsiaRestClient") RestClient riotAsiaRestClient) {
		var adapter = RestClientAdapter.create(riotAsiaRestClient);
		var proxy = HttpServiceProxyFactory.builderFor(adapter).build();
		return proxy.createClient(RiotAsiaApi.class);
	}

	@Bean
	public RiotKoreaApi riotKoreaApi(@Qualifier("riotKoreaRestClient") RestClient riotKoreaRestClient) {
		var adapter = RestClientAdapter.create(riotKoreaRestClient);
		var proxy = HttpServiceProxyFactory.builderFor(adapter).build();
		return proxy.createClient(RiotKoreaApi.class);
	}

	@Bean
	public RiotDataDragonApi riotChampionApi(@Qualifier("riotDataDragonRestClient") RestClient riotDataDragonRestClient) {
		var adapter = RestClientAdapter.create(riotDataDragonRestClient);
		var proxy = HttpServiceProxyFactory.builderFor(adapter).build();
		return proxy.createClient(RiotDataDragonApi.class);
	}

	@Bean
	public ClientHttpRequestFactory riotClientHttpRequestFactory() {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(CONNECTION_TIMEOUT);
		factory.setReadTimeout(READ_TIMEOUT);
		return factory;
	}

	private ClientHttpResponse logRequest(HttpRequest request, byte[] body,
		ClientHttpRequestExecution execution) throws IOException {
		log.info("Riot API 호출: {} {}", request.getMethod(), request.getURI());

		try {
			ClientHttpResponse response = execution.execute(request, body);
			log.info("Riot API 응답: {}", response.getStatusCode());
			return response;
		} catch (IOException e) {
			log.error("Riot API 호출 실패: {}", e.getMessage());
			throw e;
		}
	}
}
