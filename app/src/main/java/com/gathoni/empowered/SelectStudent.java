package com.gathoni.empowered;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.*;
import android.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.*;

import java.util.*;

public class SelectStudent extends AppCompatActivity {

    EditText searchStudent;
    ListView studentListView;
    Button btnAddStudent, btnSaveStudents;

    FirebaseFirestore db;
    List<Map<String, Object>> studentList = new ArrayList<>();
    List<Map<String, Object>> filteredList = new ArrayList<>();
    List<Map<String, Object>> selectedStudents = new ArrayList<>();

    ArrayAdapter<String> adapter;
    String lessonId;
    String subject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_select_student);

        db = FirebaseFirestore.getInstance();

        searchStudent = findViewById(R.id.searchStudent);
        studentListView = findViewById(R.id.studentListView);
        btnAddStudent = findViewById(R.id.btnAddStudent);
        btnSaveStudents = findViewById(R.id.btnSaveStudents);

        // Get lesson info from intent
        lessonId = getIntent().getStringExtra("lessonId");
        subject = getIntent().getStringExtra("subject");

        loadStudents();

        searchStudent.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterStudents(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        studentListView.setOnItemClickListener((parent, view, position, id) -> {
            Map<String, Object> student = filteredList.get(position);
            if (!selectedStudents.contains(student)) {
                selectedStudents.add(student);
            } else {
                selectedStudents.remove(student);
            }
            // Refresh the list to show checkmarks
            adapter.notifyDataSetChanged();
        });

        btnAddStudent.setOnClickListener(v -> showAddStudentDialog());
        btnSaveStudents.setOnClickListener(v -> saveSelectedStudentsToLesson());
    }

    private void loadStudents() {
        db.collection("students")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    studentList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Map<String, Object> student = new HashMap<>();
                        student.put("id", doc.getId()); // Store doc ID
                        student.put("name", doc.getString("name"));
                        student.put("email", doc.getString("email"));
                        studentList.add(student);
                    }
                    filterStudents("");
                });
    }

    private void filterStudents(String query) {
        filteredList.clear();
        List<String> names = new ArrayList<>();
        for (Map<String, Object> student : studentList) {
            String name = student.get("name").toString();
            if (name.toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(student);
                String displayName = name;
                // Add checkmark if selected
                if (selectedStudents.contains(student)) {
                    displayName += " ✓";
                }
                names.add(displayName);
            }
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names);
        studentListView.setAdapter(adapter);
    }

    private void showAddStudentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Student");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_student, null);
        EditText nameInput = dialogView.findViewById(R.id.editName);
        EditText emailInput = dialogView.findViewById(R.id.editEmail);
        builder.setView(dialogView);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();

            if (!name.isEmpty() && !email.isEmpty()) {
                Map<String, Object> newStudent = new HashMap<>();
                newStudent.put("name", name);
                newStudent.put("email", email);

                db.collection("students")
                        .add(newStudent)
                        .addOnSuccessListener(documentReference -> {
                            newStudent.put("id", documentReference.getId()); // Add ID
                            studentList.add(newStudent);
                            filterStudents(searchStudent.getText().toString());
                            Toast.makeText(this, "Student added", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(this, "Both fields required", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void saveSelectedStudentsToLesson() {
        if (selectedStudents.isEmpty()) {
            Toast.makeText(this, "No students selected", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> studentIds = new ArrayList<>();
        for (Map<String, Object> student : selectedStudents) {
            studentIds.add(student.get("id").toString());
        }

        db.collection("lessons")
                .document(lessonId)
                .update("students", studentIds)
                .addOnSuccessListener(aVoid -> {
                    Intent intent = new Intent(SelectStudent.this, AssignStrandActivity.class);
                    intent.putExtra("lessonId", lessonId);
                    intent.putStringArrayListExtra("studentIds", new ArrayList<>(studentIds));
                    startActivity(intent);
                    finish();
                });
    }
}



