package de.affenbande.reloaded.raidapplicantsdistributor;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.Sheets.Builder;
import com.google.api.services.sheets.v4.model.*;
import de.affenbande.reloaded.raidapplicantsdistributor.CharacterData.CharacterDataComparator;
import lombok.NonNull;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

public class GoogleSheet {

    private static final Logger LOG = Logger.getLogger(GoogleSheet.class.getName());

    private static Sheets createSheetsService() throws IOException, GeneralSecurityException {
        final String APPLICATION_NAME = "raid-applicants-distributor";
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
        final Sheets service = (new Builder(
            HTTP_TRANSPORT, JSON_FACTORY, GoogleAuthorizationUtil.authorize(HTTP_TRANSPORT)))
            .setApplicationName("raid-applicants-distributor")
            .build();

        LOG.info("service for gsheet created: " + service);

        return service;
    }

    private Spreadsheet create(@NonNull final String title) throws IOException, GeneralSecurityException {
        final Spreadsheet spreadsheet = createSheetsService()
            .spreadsheets()
            .create((new Spreadsheet()).setProperties((new SpreadsheetProperties()).setTitle(title)))
            .setFields("spreadsheetId")
            .execute();
        LOG.info("Spreadsheet ID: " + spreadsheet.getSpreadsheetId());
        return spreadsheet;
    }

    private List<Sheet> addSheets(@NonNull final String spreadSheetId, int suggestedRaidEventsSize)
        throws IOException, GeneralSecurityException {
        final List<Request> requests = new ArrayList<>();

        for (int i = 1; i <= suggestedRaidEventsSize; ++i) {
            final AddSheetRequest addSheetRequest = new AddSheetRequest().setProperties(
                new SheetProperties().setTitle("Raid-" + i));
            requests.add(new Request().setAddSheet(addSheetRequest));
        }

        final DeleteSheetRequest deleteSheetRequest = new DeleteSheetRequest().setSheetId(0);
        requests.add(new Request().setDeleteSheet(deleteSheetRequest));

        final AddSheetRequest addSheetRequest = new AddSheetRequest().setProperties(
            new SheetProperties().setTitle("Bench"));
        requests.add(new Request().setAddSheet(addSheetRequest));

        final BatchUpdateSpreadsheetRequest request = new BatchUpdateSpreadsheetRequest()
            .setRequests(requests)
            .setIncludeSpreadsheetInResponse(true);

        final BatchUpdateSpreadsheetResponse response = createSheetsService()
            .spreadsheets()
            .batchUpdate(spreadSheetId, request)
            .execute();

        return response.getUpdatedSpreadsheet().getSheets();
    }

    private Map<String, List<List<Object>>> addRaidData(
        @NonNull final String spreadSheetId, @NonNull final Set<SuggestedRaidEvent> suggestedRaidEvents,
        @NonNull final Set<CharacterData> bench, @NonNull final List<Sheet> sheets
    ) throws GeneralSecurityException, IOException {
        if (sheets.size() != suggestedRaidEvents.size() + 1) {
            throw new InputMismatchException("Spreadsheet.Sheets size (" + sheets.size() +
                                                 ") did not match the expected size of suggestedRaidEvents and bench " +
                                                 "(" + (suggestedRaidEvents.size() + 1) + ")");
        }
        final Map<String, List<List<Object>>> raidData = new TreeMap<>();
        final Set<SuggestedRaidEvent> suggestedRaidEventSet = new HashSet<>(suggestedRaidEvents);

        for (final Sheet sheet : sheets) {
            final SuggestedRaidEvent suggestedRaidEvent = suggestedRaidEventSet.stream().findAny().orElse(null);
            final List<List<Object>> rows = new ArrayList<>();
            if (suggestedRaidEvent != null) {
                final List<CharacterData> characters = new ArrayList<>(suggestedRaidEvent.getParticipants());
                characters.sort(new CharacterDataComparator());
                rows.add(List.of("Team " + suggestedRaidEvent.getRaidLead().getName()));
                rows.add(List.of(""));
                rows.add(List.of("Raidmember", "Role", "Class", "Specialization"));

                for (final CharacterData character : characters) {
                    rows.add(List.of(character.getName(), character.getRole(), character.getCharacterClass(),
                                     character.getSpec()
                    ));
                }
            } else if (bench.size() > 0) {
                final List<CharacterData> benchedPlayers = new ArrayList<>(bench);
                benchedPlayers.sort(new CharacterDataComparator());
                rows.add(List.of(sheet.getProperties().getTitle()));
                rows.add(List.of(""));
                rows.add(List.of("Raidmember", "Role", "Class", "Specialization"));

                for (final CharacterData benchedPlayer : benchedPlayers) {
                    rows.add(
                        List.of(benchedPlayer.getName(), benchedPlayer.getRole(), benchedPlayer.getCharacterClass(),
                                benchedPlayer.getSpec()
                        ));
                }
            }

            final ValueRange requestData = new ValueRange().setValues(rows);

            final AppendValuesResponse response = createSheetsService()
                .spreadsheets()
                .values()
                .append(spreadSheetId, sheet.getProperties().getTitle() + "!B2:M40", requestData)
                .setValueInputOption("USER_ENTERED")
                .setInsertDataOption("INSERT_ROWS")
                .setIncludeValuesInResponse(true)
                .execute();

            raidData.put(sheet.getProperties().getTitle(), response.getUpdates().getUpdatedData().getValues());

            suggestedRaidEventSet.remove(suggestedRaidEvent);
        }
        return raidData;
    }

    public String createRaidSheetForPossibleRaidingDay(
        final String raidingDay, final Set<SuggestedRaidEvent> suggestedRaidEvents, final Set<CharacterData> bench
    ) throws IOException, GeneralSecurityException {
        final LocalDateTime raidingDate = DateUtil.getNextRaidingDate(raidingDay);
        final String raidingDateFormatted = DateUtil.format(raidingDate);
        final String title = raidingDateFormatted + " - " +
            suggestedRaidEvents.stream().findAny().orElseThrow().getRaidDestination();

        final Spreadsheet spreadSheet = create(title);
        final String spreadSheetId = spreadSheet.getSpreadsheetId();
        final List<Sheet> sheets = addSheets(spreadSheetId, suggestedRaidEvents.size());

        addRaidData(spreadSheetId, suggestedRaidEvents, bench, sheets);

        return spreadSheetId;
    }
}
