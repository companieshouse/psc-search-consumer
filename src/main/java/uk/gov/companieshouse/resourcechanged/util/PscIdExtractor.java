package uk.gov.companieshouse.resourcechanged.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.psc.ListSummary;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts the PSC ID from the embedded links in a ListSummary object.
 * 
 * The PSC ID is found in the notifications URL path:
 * /persons-with-significant-control/{pscId}/notifications
 */
@Component
public class PscIdExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(PscIdExtractor.class);
    
    private static final Pattern PSC_ID_PATTERN = 
        Pattern.compile("(?<=persons-with-significant-control/)([^/]+)(?=/notifications)");
    private static final String PSC_LINKS_KEY_UNDERSCORE = "persons_with_significant_control";
    private static final String PSC_LINKS_KEY_HYPHEN = "persons-with-significant-control";

    /**
     * Extracts the PSC ID from the ListSummary object's links.
     *
     * @param listSummary the ListSummary containing the links with embedded PSC ID
     * @return an Optional containing the extracted PSC ID, or empty if extraction failed
     */
    public Optional<String> extractPscId(ListSummary listSummary) {
        if (listSummary == null) {
            LOGGER.warn("ListSummary is null, cannot extract PSC ID");
            return Optional.empty();
        }

        try {
            Object links = listSummary.getLinks();
            String pscId = extractFromLinksObject(links);
            if (pscId != null && !pscId.isEmpty()) {
                LOGGER.debug("Extracted PSC ID from ListSummary links: {}", pscId);
                return Optional.of(pscId);
            }
        } catch (Exception e) {
            LOGGER.warn("Error extracting PSC ID from ListSummary links", e);
        }

        LOGGER.warn("Could not extract PSC ID from notifications link");
        return Optional.empty();
    }

    /**
     * Extracts the PSC ID from a links object deserialized from the Kafka message payload.
     * Expects the links object to be a Map containing a nested map under either "persons_with_significant_control" or
     * "persons-with-significant-control", which then has a "notifications" URL where the PSC ID is extracted from
     *
     * @param linksObject the links object from the deserialized ListSummary
     * @return the extracted PSC ID, or null if not present or not in expected format
     */
    private String extractFromLinksObject(Object linksObject) {
        if (!(linksObject instanceof Map)) {
            return null;
        }
        Map<?, ?> linksMap = (Map<?, ?>) linksObject;
        Object pscObj = linksMap.get(PSC_LINKS_KEY_UNDERSCORE);
        if (pscObj == null) {
            pscObj = linksMap.get(PSC_LINKS_KEY_HYPHEN);
        }
        if (pscObj instanceof Map) {
            Object notifUrl = ((Map<?, ?>) pscObj).get("notifications");
            if (notifUrl != null) {
                return extractIdFromUrl(notifUrl.toString());
            }
        }
        return null;
    }

    /**
     * Extracts the PSC ID from a notifications URL using regex
     */
    private String extractIdFromUrl(String notificationsUrl) {
        Matcher matcher = PSC_ID_PATTERN.matcher(notificationsUrl);
        if (matcher.find()) {
            String pscId = matcher.group(1);
            LOGGER.debug("Extracted PSC ID from URL: {} → {}", notificationsUrl, pscId);
            return pscId;
        }
        return null;
    }
}
