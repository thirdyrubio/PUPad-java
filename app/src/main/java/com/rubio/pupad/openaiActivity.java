package com.rubio.pupad;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class openaiActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    TextView welcomeTextView;
    EditText messageEditText;
    ImageButton sendButton;
    ImageButton backButton;
    List<Message> messageList;
    MessageAdapter messageAdapter;
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_openai);
        messageList = new ArrayList<>();

        // Initialize views
        recyclerView = findViewById(R.id.recycler_view);
        welcomeTextView = findViewById(R.id.welcome_text);
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_btn);
        backButton = findViewById(R.id.back_button);

        // Set up RecyclerView
        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);

        // Send button click listener
        sendButton.setOnClickListener(v -> {
            String question = messageEditText.getText().toString().trim();
            if (question.isEmpty()) {
                Toast.makeText(openaiActivity.this, "Message cannot be blank", Toast.LENGTH_SHORT).show();
            } else {
                addToChat(question, Message.SENT_BY_ME);
                messageEditText.setText("");
                callAPI(question);
                welcomeTextView.setVisibility(View.GONE);
            }
        });

        // Back button click listener
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(openaiActivity.this, ewan.class);
            startActivity(intent);
            finish();
        });

        // Generate and store secret key if not already generated
        try {
            KeyStoreUtils.generateSecretKey();

            // Check if the API key is already stored
            String storedApiKey = ApiKeyManager.getApiKey(this);
            if (storedApiKey == null) {
                // Save API key securely (do this only once, not every time the activity is created)
                String apiKey = "sk-proj-oppPH7uomB0JH1ABHqDhT3BlbkFJASxpp9ue1yytkDqMGtm3"; // Replace with your actual API key
                ApiKeyManager.saveApiKey(this, apiKey);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Add a message to the chat
    void addToChat(String message, String sentBy) {
        runOnUiThread(() -> {
            messageList.add(new Message(message, sentBy));
            messageAdapter.notifyDataSetChanged();
            recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
        });
    }

    // Add response from OpenAI to the chat
    void addResponse(String response) {
        addToChat(response, Message.SENT_BY_BOT);
    }

    // Call OpenAI API with user question
    void callAPI(String question) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", "gpt-3.5-turbo");
            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", question);
            messages.put(message);
            jsonBody.put("messages", messages);
            jsonBody.put("max_tokens", 4000);
            jsonBody.put("temperature", 0);
        } catch (JSONException e) {
            e.printStackTrace();
            addResponse("Failed to create JSON body.");
            return;
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        String apiKey;
        try {
            apiKey = ApiKeyManager.getApiKey(this);
            if (apiKey == null) {
                addResponse("API key not found.");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            addResponse("Failed to retrieve API key.");
            return;
        }

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponse("Failed to load response due to " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONArray choices = jsonObject.getJSONArray("choices");
                        String result = choices.getJSONObject(0).getJSONObject("message").getString("content");
                        addResponse(result.trim());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        addResponse("Failed to parse response.");
                    }
                } else {
                    addResponse("Failed to load response: " + response.message());
                }
            }
        });
    }
}
