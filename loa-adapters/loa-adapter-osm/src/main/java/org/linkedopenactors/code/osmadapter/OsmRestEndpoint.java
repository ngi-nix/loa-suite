package org.linkedopenactors.code.osmadapter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jetty.util.log.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Component
@Slf4j
public class OsmRestEndpoint {

	@Value("${app.osmUrl}")
	String osmUrl;
	@Value("${app.osmFilter}")
	String osmFilter;

	private WebClient webClient;

	public OsmRestEndpoint() {
		this.webClient = WebClient.builder().build();
	}

	public Flux<OsmEntry> getChangedEntriesSince(org.linkedopenactors.code.similaritychecker.BoundingBox bBox, LocalDateTime changedSince) {

//		LocalDateTime now = LocalDateTime.now();
//		ZoneId zone = ZoneId.of("Europe/Berlin");
//		ZoneOffset zoneOffSet = zone.getRules().getOffset(now);
//		LocalDateTime until = LocalDateTime.now();
//		LocalDateTime since = until.minus(minutesInThePast, ChronoUnit.MINUTES);
//		long sinceAsUnixTimestamp = since.toEpochSecond(zoneOffSet);
//		long untilAsUnixTimestamp = until.toEpochSecond(zoneOffSet);

//		  way["waterway"="stream"]({{bbox}})(newer:"2017-03-18T00:00:00Z");

// [bbox:48.67823860482997,9.026118164062503,48.878238604829974,9.266118164062501];

		String dateFilter = "";
		if (changedSince != null) {
			dateFilter="(newer:" + changedSince.format(DateTimeFormatter.ISO_INSTANT) + ")";
		}

		String url = osmUrl + "[bbox:" + bBox.toString() + "];node" + dateFilter + osmFilter;

//		log.info("OSM URL: " + url);

		Flux<OsmEntries> response = webClient
				.get().uri(url)
				.retrieve()
				.bodyToFlux(OsmEntries.class);

		//List<OsmEntry> entries = response.collectList().block();
		return response.flatMapIterable(e -> e.getElements());
	}

}
