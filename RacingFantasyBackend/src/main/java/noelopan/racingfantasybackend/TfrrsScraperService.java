package noelopan.racingfantasybackend;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TfrrsScraperService {

    @Autowired
    private PerformanceRepository performanceRepository;

    @Autowired
    private AthleteRepository athleteRepository;

    /**
     * Existing meet scraper.
     * This remains focused on reading meet results and saving Performances.
     */
    public void scrapeMeet(String meetUrl, Integer weekNumber) {
        try {
            Document doc = Jsoup.connect(meetUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .get();

            Set<String> hiddenClasses = new HashSet<>();
            Elements styleTags = doc.select("style");
            Pattern pattern = Pattern.compile("\\.([a-zA-Z0-9_-]+)\\s*\\{[^}]*display:\\s*none");

            for (Element style : styleTags) {
                Matcher matcher = pattern.matcher(style.data());
                while (matcher.find()) {
                    hiddenClasses.add(matcher.group(1));
                }
            }

            Elements eventHeaders = doc.select("h3");
            if (!eventHeaders.isEmpty()) {
                eventHeaders.remove(0);
            }


            for (Element h3 : eventHeaders) {
                String eventName = h3.text().trim().toUpperCase();

                // Skip events not currently supported in fantasy scoring
                if (eventName.toUpperCase().contains("RELAY")
                        || eventName.toUpperCase().contains("WEIGHT THROW")) {
                    continue;
                }

                boolean isPrelim = eventName.toUpperCase().contains("PRELIM");
                boolean isFinal = eventName.toUpperCase().contains("FINAL");
                if(eventName.contains(",")){
                    eventName = eventName.replace(",", "");
                }
                if(eventName.contains("FINALS")){
                    eventName = eventName.replace(" FINALS","");
                }else if(eventName.contains("PRELIMINARIES")){
                    eventName = eventName.replace(" PRELIMINARIES","");
                }else if(eventName.contains("FINAL")){
                    eventName = eventName.replace(" FINAL","");
                }else if(eventName.contains("PRELIMS")){
                    eventName = eventName.replace(" PRELIMS","");
                }else if(eventName.contains("PRELIM")){
                    eventName = eventName.replace(" PRELIM","");
                }

                String eventConstantsName = EventMatcher.matchEvent(eventName);
                TrackEventConstants eventConstants = TrackEventConstants.valueOf(eventConstantsName);

                Element tableWrapper = h3.parent();
                if (tableWrapper == null) {
                    continue;
                }

                Element table = tableWrapper.nextElementSiblings().select("table").first();
                if (table == null) {
                    continue;
                }

                Element headerRow = table.selectFirst("thead tr");
                if (headerRow == null) {
                    continue;
                }

                Elements headers = headerRow.select("th");
                int timeOrMarkIndex = -1;
                boolean isTrackEvent = true;

                for (int j = 0; j < headers.size(); j++) {
                    Element th = headers.get(j);
                    boolean isHidden = false;

                    for (String className : th.classNames()) {
                        if (hiddenClasses.contains(className)) {
                            isHidden = true;
                            break;
                        }
                    }

                    if (!isHidden) {
                        String headerText = th.text().trim().toUpperCase();
                        if (headerText.equals("TIME")) {
                            timeOrMarkIndex = j;
                            break;
                        } else if (headerText.equals("MARK") || headerText.equals("POINTS")) {
                            timeOrMarkIndex = j;
                            isTrackEvent = false;
                            break;
                        }
                    }
                }

                Elements rows = table.select("tbody tr");

                for (Element row : rows) {
                    Elements columns = row.select("td");

                    if (timeOrMarkIndex == -1 || columns.size() <= timeOrMarkIndex) {
                        continue;
                    }

                    String place = columns.get(0).text().trim();
                    String athleteName = columns.get(1).text().trim();
                    String year = columns.get(2).text().trim();
                    String school = normalizeSchoolName(columns.get(3).text().trim());
                    String rawMark = columns.get(timeOrMarkIndex).text().trim();

                    if (year.isEmpty() || school.equalsIgnoreCase("Unattached") || athleteName.isEmpty()) {
                        continue;
                    }

                    Athlete athlete = athleteRepository.findByNameIgnoreCaseAndSchoolIgnoreCase(athleteName, school)
                            .orElseGet(() -> {
                                Athlete newAthlete = new Athlete();
                                newAthlete.setName(athleteName);
                                newAthlete.setSchool(school);
                                return athleteRepository.save(newAthlete);
                            });

                    Double decimalMark = PerformanceParser.parseMarkToDecimal(rawMark, eventName);
                    int points;

                    if (decimalMark != null && eventConstants != null) {
                        if (isTrackEvent) {
                            points = WorldAthleticsCalculator.calculateTrackPoints(
                                    decimalMark,
                                    eventConstants.getA(),
                                    eventConstants.getB(),
                                    eventConstants.getC()
                            );
                        } else {
                            points = WorldAthleticsCalculator.calculateFieldPoints(
                                    decimalMark,
                                    eventConstants.getA(),
                                    eventConstants.getB(),
                                    eventConstants.getC()
                            );
                        }
                    } else {
                        continue;
                    }

                        if(isPrelim || isFinal) {
                            System.out.println(eventName);
                            Optional<Performance> testPerf = performanceRepository.findByAthleteAndEventNameIgnoreCaseAndWeekNumber(athlete, eventName, weekNumber);
                            if (testPerf.isPresent()) {
                                System.out.println(testPerf.get());
                                System.out.println(testPerf.get().getPoints());
                            }
                            Performance perf = performanceRepository.findByAthleteAndEventNameIgnoreCaseAndWeekNumber(athlete, eventConstantsName, weekNumber)
                                    .orElseGet(() ->{
                                        Performance newPerf = new Performance();
                                        if(isFinal){
                                            newPerf.setPlace(place);
                                        }
                                        newPerf.setEventName(eventConstantsName);
                                        newPerf.setAthlete(athlete);
                                        newPerf.setYear(year);
                                        newPerf.setSchool(school);
                                        newPerf.setDisplayMark(rawMark);
                                        newPerf.setDecimalMark(decimalMark);
                                        newPerf.setPoints(points);
                                        newPerf.setWeekNumber(weekNumber);
                                        return performanceRepository.save(newPerf);
                                    });
                            if(isFinal){
                                perf.setPlace(place);
                            }
                            if(points > perf.getPoints()){
                                System.out.println(athleteName + ": Previous perf points:" + perf.getPoints() + ", current Points: " + points);
                                perf.setPoints(points);
                                perf.setDecimalMark(decimalMark);
                                perf.setDisplayMark(rawMark);
                            }
                            performanceRepository.save(perf);
                        }else {
                            Performance perf = new Performance();
                            perf.setEventName(eventConstantsName);
                            perf.setPlace(place);
                            perf.setAthlete(athlete);
                            perf.setYear(year);
                            perf.setSchool(school);
                            perf.setDisplayMark(rawMark);
                            perf.setDecimalMark(decimalMark);
                            perf.setPoints(points);
                            perf.setWeekNumber(weekNumber);

                            performanceRepository.save(perf);
                        }
                }
            }

            System.out.println("Successfully scraped, scored, and saved data from: " + meetUrl);

        } catch (IOException e) {
            System.err.println("Failed to scrape TFRRS: " + e.getMessage());
        }
    }

    /**
     * Preseason-style roster scrape.
     * Reads a TFRRS roster page, creates/updates all athletes for that team,
     * stores tfrrsUrl/tfrrsId, and populates each athlete's best event.
     *
     * gender should be "mens" or "womens"
     */
    public void scrapeRoster(String rosterUrl, String gender) {
        try {
            Document doc = Jsoup.connect(rosterUrl)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            String schoolName = normalizeSchoolName(extractSchoolName(doc));
            Elements athleteLinks = doc.select("a[href*=/athletes/]");

            Set<String> processedUrls = new HashSet<>();

            for (Element athleteLink : athleteLinks) {
                String tfrrsUrl = athleteLink.absUrl("href").trim();
                if (tfrrsUrl.isEmpty() || !tfrrsUrl.contains("/athletes/")) {
                    continue;
                }

                if (processedUrls.contains(tfrrsUrl)) {
                    continue;
                }
                processedUrls.add(tfrrsUrl);

                String athleteName = normalizeRosterName(athleteLink.text());
                if (athleteName.isEmpty()) {
                    continue;
                }

                Athlete athlete = athleteRepository.findByNameIgnoreCaseAndSchoolIgnoreCase(athleteName, schoolName)
                        .orElseGet(() -> {
                            Athlete newAthlete = new Athlete();
                            newAthlete.setName(athleteName);
                            newAthlete.setSchool(schoolName);
                            return athleteRepository.save(newAthlete);
                        });

                athlete.setTfrrsUrl(tfrrsUrl);
                athlete.setTfrrsId(extractTfrrsId(tfrrsUrl));
                athleteRepository.save(athlete);

                populateCollegeBest(athlete, gender);
            }

            System.out.println("Successfully scraped roster from: " + rosterUrl);

        } catch (IOException e) {
            System.err.println("Failed to scrape TFRRS roster: " + e.getMessage());
        }
    }

    /**
     * Visits an athlete page and tries to determine their best fantasy event
     * from their listed college bests / PR table.
     */
    public void populateCollegeBest(Athlete athlete, String gender) {
        if (athlete == null || athlete.getTfrrsUrl() == null || athlete.getTfrrsUrl().isBlank()) {
            return;
        }

        try {
            Document doc = Jsoup.connect(athlete.getTfrrsUrl())
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            Elements rows = doc.select("table tbody tr");

            for (Element row : rows) {
                Elements cols = row.select("td");

                // TFRRS commonly lays out PR rows as:
                // Event1 | Mark1 | Event2 | Mark2
                if (cols.size() >= 2) {
                    addPrIfValid(athlete, cols.get(0).text(), cols.get(1).text(), gender);
                }

                if (cols.size() >= 4) {
                    addPrIfValid(athlete, cols.get(2).text(), cols.get(3).text(), gender);
                }
            }

            athleteRepository.save(athlete);

        } catch (IOException e) {
            System.err.println("Failed to fetch PRs for " + athlete.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Takes a single PR entry, maps it to one of your enum events, calculates
     * points, and updates the athlete if this is their best event so far.
     */
    private void addPrIfValid(Athlete athlete, String event, String mark, String gender) {
        if (athlete == null || event == null || mark == null) {
            return;
        }

        event = event.trim();
        mark = mark.trim();

        if (event.isEmpty() || mark.isEmpty()) {
            return;
        }

        String enumEventName = convertPrEventToEnum(event, gender);
        if (enumEventName == null) {
            return;
        }

        String cleanedMark = extractCleanPerformanceMark(mark);
        if (cleanedMark == null || cleanedMark.isBlank()) {
            return;
        }

        TrackEventConstants eventConstants;
        try {
            eventConstants = TrackEventConstants.valueOf(enumEventName);
        } catch (IllegalArgumentException e) {
            return;
        }

        Double decimalMark = PerformanceParser.parseMarkToDecimal(cleanedMark, enumEventName);
        if (decimalMark == null) {
            return;
        }

        boolean isTrackEvent = isTrackEvent(enumEventName);
        int points;

        if (isTrackEvent) {
            points = WorldAthleticsCalculator.calculateTrackPoints(
                    decimalMark,
                    eventConstants.getA(),
                    eventConstants.getB(),
                    eventConstants.getC()
            );
        } else {
            points = WorldAthleticsCalculator.calculateFieldPoints(
                    decimalMark,
                    eventConstants.getA(),
                    eventConstants.getB(),
                    eventConstants.getC()
            );
        }

        athlete.trySetBestEventAndPoints(enumEventName, cleanedMark, points);
    }

    private String extractSchoolName(Document doc) {
        Element h3 = doc.selectFirst("h3");
        if (h3 != null && !h3.text().isBlank()) {
            return h3.text().trim();
        }

        Element title = doc.selectFirst("title");
        if (title != null && !title.text().isBlank()) {
            return title.text().trim();
        }

        return "Unknown School";
    }

    /**
     * Converts roster names like "McColm, Branden" to "Branden McColm".
     */
    private String normalizeRosterName(String rawName) {
        if (rawName == null) {
            return "";
        }

        String cleaned = rawName.trim();
        if (cleaned.isEmpty()) {
            return "";
        }

        if (cleaned.contains(",")) {
            String[] parts = cleaned.split(",");
            if (parts.length >= 2) {
                String last = parts[0].trim();
                String first = parts[1].trim();
                return (first + " " + last).trim();
            }
        }

        return cleaned;
    }

    private String extractTfrrsId(String tfrrsUrl) {
        if (tfrrsUrl == null) {
            return null;
        }

        Matcher matcher = Pattern.compile("/athletes/(\\d+)").matcher(tfrrsUrl);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    /**
     * Converts shorthand PR labels from an athlete page into your enum names.
     * Returns something like:
     * MENS_1500_METERS
     * WOMENS_LONG_JUMP
     * MENS_60_HURDLES
     */
    private String convertPrEventToEnum(String event, String gender) {
        if (event == null || gender == null) {
            return null;
        }

        String prefix = normalizeGenderPrefix(gender);
        if (prefix == null) {
            return null;
        }

        String clean = event.trim().toUpperCase()
                .replace(".", "")
                .replace("'", "")
                .replace("\"", "");

        if (clean.isEmpty()) {
            return null;
        }

        // Skip XC / relays / unsupported events for now
        if (clean.contains("XC") || clean.contains("RELAY") || clean.equals("WT") || clean.contains("WEIGHT")) {
            return null;
        }

        switch (clean) {
            case "HJ":
                return prefix + "_HIGH_JUMP";
            case "PV":
                return prefix + "_POLE_VAULT";
            case "LJ":
                return prefix + "_LONG_JUMP";
            case "TJ":
                return prefix + "_TRIPLE_JUMP";
            case "SP":
                return prefix + "_SHOT_PUT";
            case "DT":
                return prefix + "_DISCUS_THROW";
            case "JT":
                return prefix + "_JAVELIN_THROW";
            case "HT":
                return prefix + "_HAMMER_THROW";
            case "MILE":
                return prefix + "_MILE";
            case "DEC":
                return prefix.equals("MENS") ? "MENS_DECATHLON" : null;
            case "HEP":
                return prefix.equals("MENS") ? "MENS_HEPTATHLON_ST" : "WOMENS_HEPTATHLON";
            case "PENT":
                return prefix.equals("WOMENS") ? "WOMENS_PENTATHLON_ST" : null;
        }

        // 60H, 110H, 100H, etc.
        if (clean.matches("\\d+H")) {
            String distance = clean.substring(0, clean.length() - 1);
            return prefix + "_" + distance + "_HURDLES";
        }

        // 3000S, 2000S
        if (clean.matches("\\d+S")) {
            String distance = clean.substring(0, clean.length() - 1);
            return prefix + "_" + distance + "_STEEPLECHASE";
        }

        // plain numeric events: 60, 100, 200, 800, 1500, etc.
        if (clean.matches("\\d+")) {
            return prefix + "_" + clean + "_METERS";
        }

        // Some athlete pages may already show fuller names
        clean = clean.replace(" ", "_");
        if (clean.equals("LONG_JUMP")) return prefix + "_LONG_JUMP";
        if (clean.equals("TRIPLE_JUMP")) return prefix + "_TRIPLE_JUMP";
        if (clean.equals("HIGH_JUMP")) return prefix + "_HIGH_JUMP";
        if (clean.equals("POLE_VAULT")) return prefix + "_POLE_VAULT";
        if (clean.equals("SHOT_PUT")) return prefix + "_SHOT_PUT";
        if (clean.equals("DISCUS_THROW") || clean.equals("DISCUS")) return prefix + "_DISCUS_THROW";
        if (clean.equals("JAVELIN_THROW") || clean.equals("JAVELIN")) return prefix + "_JAVELIN_THROW";
        if (clean.equals("HAMMER_THROW") || clean.equals("HAMMER")) return prefix + "_HAMMER_THROW";
        if (clean.equals("MILE")) return prefix + "_MILE";

        return null;
    }

    private String normalizeGenderPrefix(String gender) {
        if (gender == null) {
            return null;
        }

        String clean = gender.trim().toUpperCase();
        if (clean.equals("MENS") || clean.equals("MEN") || clean.equals("MALE")) {
            return "MENS";
        }
        if (clean.equals("WOMENS") || clean.equals("WOMEN") || clean.equals("FEMALE")) {
            return "WOMENS";
        }
        return null;
    }

    private boolean isTrackEvent(String enumEventName) {
        if (enumEventName == null) {
            return false;
        }

        return !(enumEventName.contains("JUMP")
                || enumEventName.contains("VAULT")
                || enumEventName.contains("THROW")
                || enumEventName.contains("SHOT_PUT")
                || enumEventName.contains("PENTATHLON")
                || enumEventName.contains("HEPTATHLON")
                || enumEventName.contains("DECATHLON"));
    }

    private String extractCleanPerformanceMark(String rawMark) {
        if (rawMark == null) {
            return null;
        }

        String clean = rawMark.trim().toUpperCase().replace('\u00A0', ' ');
        if (clean.isBlank()) {
            return null;
        }

        // Reject non-performances immediately
        if (containsNonPerformance(clean)) {
            return null;
        }

        // Prefer a time first: 1:52.33, 14:35.24, etc.
        java.util.regex.Matcher timeMatcher =
                java.util.regex.Pattern.compile("(\\d+:\\d+(?:\\.\\d+)?)").matcher(clean);

        if (timeMatcher.find()) {
            return timeMatcher.group(1);
        }

        // Then prefer a metric mark with unit: 4.24m, 6.85m, etc.
        java.util.regex.Matcher metricMatcher =
                java.util.regex.Pattern.compile("(\\d+(?:\\.\\d+)?\\s*M)\\b").matcher(clean);

        if (metricMatcher.find()) {
            return metricMatcher.group(1).replaceAll("\\s+", "");
        }

        // Then fallback to first plain number: 12.24, 46.88, etc.
        java.util.regex.Matcher numberMatcher =
                java.util.regex.Pattern.compile("(\\d+(?:\\.\\d+)?)").matcher(clean);

        if (numberMatcher.find()) {
            return numberMatcher.group(1);
        }

        return null;
    }

    private boolean containsNonPerformance(String text) {
        return text.contains("DNF")
                || text.contains("DNS")
                || text.contains("DQ")
                || text.contains("NH")
                || text.contains("NM")
                || text.contains("FOUL")
                || text.equals("FS");
    }

    private String normalizeSchoolName(String school) {
        if (school == null) {
            return "";
        }

        String cleaned = school.trim().toUpperCase();
        return cleaned;
    }

}