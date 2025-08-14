package com.gathoni.empowered;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    private ListView lessonListView;
    private Button btnAddLesson;
    private FirebaseFirestore db;
    private ArrayAdapter<String> adapter;
    private List<String> lessonDisplayList = new ArrayList<>();
    private List<String> lessonIdList = new ArrayList<>();

    private String selectedDate = "";
    private String selectedSubject = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);

        Intent intent2 = new Intent(MainActivity.this, LoginActivity.class);

        startActivity(intent2);
        Intent intent3 = new Intent(MainActivity.this, StudentListActivity.class);

        startActivity(intent3);
        lessonListView = findViewById(R.id.lessonListView);
        btnAddLesson = findViewById(R.id.btnAddLesson);
        db = FirebaseFirestore.getInstance();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lessonDisplayList);
        lessonListView.setAdapter(adapter);

        fetchLessons();

        btnAddLesson.setOnClickListener(v -> showAddLessonDialog());

        lessonListView.setOnItemClickListener((parent, view, position, id) -> {
            String lessonId = lessonIdList.get(position);
            String[] parts = lessonDisplayList.get(position).split(" - ", 2);
            String subject = parts.length > 1 ? parts[1] : "";

            Intent intent = new Intent(MainActivity.this, SelectStudent.class);
            intent.putExtra("lessonId", lessonId);
            intent.putExtra("subject", subject);
            startActivity(intent);
        });
    }

    private void fetchLessons() {
        db.collection("lessons")
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Error loading lessons", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    lessonDisplayList.clear();
                    lessonIdList.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String subject = doc.getString("subject");
                        String date = doc.getString("date");
                        String id = doc.getId();

                        lessonDisplayList.add(date + " - " + subject);
                        lessonIdList.add(id);
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    private void showAddLessonDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_lesson, null);
        Spinner spinnerSubjects = dialogView.findViewById(R.id.spinnerSubjects);
        Button btnPickDate = dialogView.findViewById(R.id.btnPickDate);
        TextView txtSelectedDate = dialogView.findViewById(R.id.txtSelectedDate);

        String[] subjects = {"Math", "Science", "English", "ICT"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, subjects);
        spinnerSubjects.setAdapter(spinnerAdapter);

        final Calendar calendar = Calendar.getInstance();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        btnPickDate.setOnClickListener(v -> {
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                selectedDate = sdf.format(calendar.getTime());
                txtSelectedDate.setText(selectedDate);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        new android.app.AlertDialog.Builder(this)
                .setTitle("Add New Lesson")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    selectedSubject = spinnerSubjects.getSelectedItem().toString();

                    if (selectedDate.isEmpty()) {
                        Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> lessonData = new HashMap<>();
                    lessonData.put("date", selectedDate);
                    lessonData.put("subject", selectedSubject);

                    db.collection("lessons")
                            .add(lessonData)
                            .addOnSuccessListener(docRef -> {
                                Toast.makeText(this, "Lesson added", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to add lesson", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}


