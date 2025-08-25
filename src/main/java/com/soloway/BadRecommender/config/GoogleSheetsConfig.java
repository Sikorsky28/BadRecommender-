package com.soloway.BadRecommender.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Configuration
public class GoogleSheetsConfig {

    @Value("${google.sheets.credentials-file:google-credentials.json}")
    private String credentialsFilePath;

    @Bean
    public Sheets sheetsService() throws IOException, GeneralSecurityException {
        try {
            // Пытаемся загрузить файл credentials
            GoogleCredential credential = GoogleCredential
                    .fromStream(new FileInputStream(credentialsFilePath))
                    .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

            System.out.println("✅ Google Sheets API успешно инициализирована");
            
            return new Sheets.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    credential)
                    .setApplicationName("Bad Recommender")
                    .build();
                    
        } catch (IOException e) {
            System.err.println("⚠️ Не удалось загрузить google-credentials.json: " + e.getMessage());
            System.err.println("⚠️ Google Sheets API будет недоступна, используются fallback данные");
            
            // Возвращаем null, чтобы приложение могло запуститься
            return null;
        }
    }
}
