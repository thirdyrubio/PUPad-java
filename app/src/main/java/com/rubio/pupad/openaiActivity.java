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
    ImageButton backButton; // Added ImageButton for back button
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
        backButton = findViewById(R.id.back_button); // Initialize back button

        // Set up RecyclerView
        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true); // Display items from bottom
        recyclerView.setLayoutManager(llm);

        // Send button click listener
        sendButton.setOnClickListener(v -> {
            String question = messageEditText.getText().toString().trim();
            if (question.isEmpty()) {
                // Show error message if text field is blank
                Toast.makeText(openaiActivity.this, "Message cannot be blank", Toast.LENGTH_SHORT).show();
            } else {
                addToChat(question, Message.SENT_BY_ME); // Add user message to chat
                messageEditText.setText(""); // Clear message input
                callAPI(question); // Call OpenAI API with user question
                welcomeTextView.setVisibility(View.GONE); // Hide welcome text once user interacts
            }
        });

        // Back button click listener
        backButton.setOnClickListener(v -> {
            // Navigate back to EwanActivity
            Intent intent = new Intent(openaiActivity.this, ewan.class);
            startActivity(intent);
            finish(); // Finish current activity so back button won't come back here
        });
    }

    // Add a message to the chat
    void addToChat(String message, String sentBy) {
        runOnUiThread(() -> {
            messageList.add(new Message(message, sentBy));
            messageAdapter.notifyDataSetChanged(); // Notify adapter of data change
            recyclerView.smoothScrollToPosition(messageAdapter.getItemCount()); // Scroll to last message
        });
    }

    // Add response from OpenAI to the chat
    void addResponse(String response) {
        addToChat(response, Message.SENT_BY_BOT); // Add bot response to chat
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
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer sk-proj-iyNUfiuwJeR1Uqn5RFZoT3BlbkFJkXfg9EMZGRDPTlaVzvMF")
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
