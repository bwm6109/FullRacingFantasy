package noelopan.racingfantasybackend;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TfrrsScraperService {

    @Autowired
    private PerformanceRepository performanceRepository;
    @Autowired
    private AthleteRepository athleteRepository;

    public void scrapeMeet(String meetUrl, Integer weekNumber) {
        try {
            Document doc = Jsoup.connect(meetUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .get();

            Set<String> hiddenClasses = new HashSet<>();
            Elements styleTags = doc.select("style");
            Pattern pattern = Pattern.compile("\\.([a-zA-Z0-9_-]+)\\s*\\{[^}]*display:\\s*none");

            for(Element style : styleTags) {
                Matcher matcher = pattern.matcher(style.data());
                while(matcher.find()) {
                    hiddenClasses.add(matcher.group(1));
                }
            }

            Elements eventHeaders = doc.select("h3");
            eventHeaders.remove(0);

            for(Element h3 : eventHeaders) {
                String eventName = h3.text().trim();
                // World Athletics does not have Weight Throw as an official event
                if(eventName.toUpperCase().contains("RELAY") || eventName.toUpperCase().contains("WEIGHT THROW")) {
                    continue; // Skip relays for now
                }

                // Match the event to your enum to get the A, B, C constants
                TrackEventConstants eventConstants = EventMatcher.matchEvent(eventName);

                Element tableWrapper = h3.parent();
                if (tableWrapper == null) continue;

                Element table = tableWrapper.nextElementSiblings().select("table").first();

                if (table != null) {
                    Element headerRow = table.selectFirst("thead tr");
                    if (headerRow == null) continue;

                    Elements headers = headerRow.select("th");
                    int timeOrMarkIndex = -1;
                    boolean isTrackEvent = true; // Default to true, update if we find "MARK"

                    for(int j = 0; j < headers.size(); j++){
                        Element th = headers.get(j);
                        boolean isHidden = false;

                        for(String className : th.classNames()){
                            if(hiddenClasses.contains(className)){
                                isHidden = true;
                                break;
                            }
                        }

                        if(!isHidden){
                            String headerText = th.text().trim().toUpperCase();
                            if(headerText.equals("TIME")) {
                                timeOrMarkIndex = j;
                                isTrackEvent = true;
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

                        if (timeOrMarkIndex != -1 && columns.size() > timeOrMarkIndex) {
                            String place = columns.get(0).text();
                            String athleteName = columns.get(1).text();
                            String year = columns.get(2).text();
                            String school = columns.get(3).text();
                            String rawMark = columns.get(timeOrMarkIndex).text();

                            if(year.equals("") || school.equals("Unattached") || athleteName.isEmpty()) {
                                continue;
                            }

                            Athlete athlete = athleteRepository.findByNameAndSchool(athleteName, school).orElseGet(() -> {
                                Athlete newAthlete = new Athlete();
                                newAthlete.setName(athleteName);
                                newAthlete.setSchool(school);
                                return athleteRepository.save(newAthlete);
                            });

                            // 1. Parse the String to a Decimal
                            Double decimalMark = PerformanceParser.parseMarkToDecimal(rawMark, eventName);
                            Integer points = 0;
                            // 2. Calculate points if we have a valid mark AND matched the event constants
                            if (decimalMark != null && eventConstants != null) {
                                if (isTrackEvent) {
                                    points = WorldAthleticsCalculator.calculateTrackPoints(
                                            decimalMark, eventConstants.getA(), eventConstants.getB(), eventConstants.getC());
                                } else {
                                    points = WorldAthleticsCalculator.calculateFieldPoints(
                                            decimalMark, eventConstants.getA(), eventConstants.getB(), eventConstants.getC());
                                }
                            }

                            // 3. Save to Database
                            if (decimalMark != null) {
                                Performance perf = new Performance();
                                perf.setEventName(eventName);
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
                }
            }
            System.out.println("Successfully scraped, scored, and saved data from: " + meetUrl);

        } catch (IOException e) {
            System.err.println("Failed to scrape TFRRS: " + e.getMessage());
        }
    }
}