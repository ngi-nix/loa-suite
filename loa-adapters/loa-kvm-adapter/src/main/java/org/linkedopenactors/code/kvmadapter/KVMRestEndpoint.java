package org.linkedopenactors.code.kvmadapter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;

@Component
public class KVMRestEndpoint {

	@Value("${app.kvmUrl}")
	private String kvmUrl;

	private WebClient webClient;

	public KVMRestEndpoint() {
		this.webClient = WebClient.builder().build();
	}
	
	public List<KvmEntry> getChangedEntriesSince(long minutesInThePast) {
		
		LocalDateTime now = LocalDateTime.now();
		ZoneId zone = ZoneId.of("Europe/Berlin");
		ZoneOffset zoneOffSet = zone.getRules().getOffset(now);
		LocalDateTime until = LocalDateTime.now();		
		LocalDateTime since = until.minus(minutesInThePast, ChronoUnit.MINUTES);
		long sinceAsUnixTimestamp = since.toEpochSecond(zoneOffSet);
		long untilAsUnixTimestamp = until.toEpochSecond(zoneOffSet);

		String url = kvmUrl + "entries/recently-changed?since="+sinceAsUnixTimestamp+"&until="+untilAsUnixTimestamp+"&with_ratings=false&limit=1000&offset=0";
		
		Flux<KvmEntry> response = webClient 	
				.get().uri(url)
				.retrieve()
				.bodyToFlux(KvmEntry.class);

		List<KvmEntry> entries = response.collectList().block();		
		return entries;
	}
}
