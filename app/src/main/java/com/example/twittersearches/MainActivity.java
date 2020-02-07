package com.example.twittersearches;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.IntentCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String SEARCHES = "searches";
    private EditText queryEditText;
    private EditText tagEditText;
    private FloatingActionButton saveFloatingActionButton;
    private SharedPreferences savedSearches;
    private List<String> tags;
    private SearchesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        queryEditText = ((TextInputLayout) findViewById(R.id.queryTextInputLayout)).getEditText();
        tagEditText = ((TextInputLayout) findViewById(R.id.tagTextInputLayout)).getEditText();
        saveFloatingActionButton = findViewById(R.id.fab);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        //  Get the shared references
        savedSearches = getSharedPreferences(SEARCHES, MODE_PRIVATE);

        // Get all saved values by key and order them
        tags = new ArrayList<>(savedSearches.getAll().keySet());
        Collections.sort(tags, String.CASE_INSENSITIVE_ORDER);

        //Set the recyclerview
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SearchesAdapter(tags, itemClickListener, itemLongClickListener);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // Attach listeners to edit texts and floating button
        queryEditText.addTextChangedListener(textWatcher);
        tagEditText.addTextChangedListener(textWatcher);
        saveFloatingActionButton.setOnClickListener(saveButtonListener);

        // Updates visibility of FAB
        updateSaveFAB();
    }

    private View.OnClickListener itemClickListener = v -> {
        String tag = ((TextView) v).getText().toString();
        String urlString = getUrlString(tag);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
        startActivity(intent);
    };

    private String getUrlString(String tag) {
        return getString(R.string.search_URL) + Uri.encode(savedSearches.getString(tag, ""), "UTF-8");
    }

    private View.OnLongClickListener itemLongClickListener = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {

            final String tag = ((TextView) v).getText().toString();

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(getString(R.string.share_edit_delete_title, tag))
                    .setItems(R.array.dialog_items, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                shareTag(tag);
                                break;
                            case 1:
                                tagEditText.setText(tag);
                                queryEditText.setText(savedSearches.getString(tag, ""));
                                break;
                            case 2:
                                deleteSearch(tag);
                                break;
                        }
                    });

            builder.setNegativeButton(getString(R.string.cancel), null);
            builder.create().show();

            return true;

        }
    };


    private void shareTag(String tag) {
        String urlString = getUrlString(tag);
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
        shareIntent.putExtra(Intent.EXTRA_TEXT, urlString);
        shareIntent.setType("text/html");
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_search)));
    }

    private View.OnClickListener saveButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String query = queryEditText.getText().toString();
            String tag = tagEditText.getText().toString();
            if (!query.isEmpty() && !tag.isEmpty()) {
                ContextCompat.getSystemService(MainActivity.this, InputMethodManager.class)
                        .hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
            addTaggedSearch(tag, query);
            queryEditText.setText("");
            tagEditText.setText("");
            queryEditText.requestFocus();
        }
    };

    private void addTaggedSearch(String tag, String query) {
        SharedPreferences.Editor edit = savedSearches.edit();
        edit.putString(tag, query);
        edit.apply();
        if (!tags.contains(tag)) {
            tags.add(tag);
            Collections.sort(tags, String.CASE_INSENSITIVE_ORDER);
            adapter.notifyDataSetChanged();
        }
    }

    private void deleteSearch(String tag) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.confirm_message, tag));
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                tags.remove(tag);
                SharedPreferences.Editor editor = savedSearches.edit();
                editor.remove(tag).apply();
                adapter.notifyDataSetChanged();
            }
        });
        builder.create().show();
    }

    private void deleteAll() {
        tags.clear();

        savedSearches.edit()
                .clear()
                .apply();

        adapter.notifyDataSetChanged();
    }

    private void updateSaveFAB() {
        if (queryEditText.getText().toString().isEmpty() || tagEditText.getText().toString().isEmpty()) {
            saveFloatingActionButton.hide();
        } else {
            saveFloatingActionButton.show();
        }
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            updateSaveFAB();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_all_searches:
                deleteAll();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
