package edu.cit.riconalla.dentalink.service;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    private final OkHttpClient client = new OkHttpClient();

    public String uploadFile(byte[] fileBytes, String fileName) throws IOException {

        String uploadUrl = supabaseUrl + "/storage/v1/object/services/" + fileName;

        RequestBody body = RequestBody.create(
                fileBytes,
                MediaType.parse("image/png") // ✅ important
        );

        Request request = new Request.Builder()
                .url(uploadUrl)
                .addHeader("Authorization", "Bearer " + supabaseKey)
                .addHeader("apikey", supabaseKey) // ✅ REQUIRED
                .addHeader("Content-Type", "image/png")
                .put(body)
                .build();

        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new IOException("Upload failed: " + response.body().string());
        }

        return supabaseUrl + "/storage/v1/object/public/services/" + fileName;
    }
}