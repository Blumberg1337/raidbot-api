package de.affenbande.reloaded.raidapplicantsdistributor;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow.Builder;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

public class GoogleAuthorizationUtil {

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final Set<String> SCOPES = Collections.singleton("https://www.googleapis.com/auth/spreadsheets");
    private static final String CREDENTIALS_FILE_PATH = System.getenv("LOCAL_ENV_TESTING") != null ? Path
        .of("src", "main", "resources", "client-secrets.json")
        .toString() : Path.of("client-secrets.json").toString();

    public static Credential authorize(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        final InputStream in = new FileInputStream(CREDENTIALS_FILE_PATH);
        final GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        final GoogleAuthorizationCodeFlow flow = (new Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES))
            .setDataStoreFactory(new FileDataStoreFactory(new File("tokens")))
            .setAccessType("offline")
            .build();

        final LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();

        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
}
