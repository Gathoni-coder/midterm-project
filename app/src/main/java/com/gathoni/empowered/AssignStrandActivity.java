package com.gathoni.empowered;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.*;

public class AssignStrandActivity extends AppCompatActivity {

    private Spinner spinnerStrands, spinnerSubStrands;
    private Button btnAssign;
    private ImageButton btnAddSubStrand, btnAddStrand;
    private FirebaseFirestore db;

    private List<String> strands = new ArrayList<>();
    private List<String> strandIds = new ArrayList<>(); // store Firestore IDs
    private List<String> subStrands = new ArrayList<>();

    private ArrayAdapter<String> strandAdapter;
    private ArrayAdapter<String> subStrandAdapter;

    private String lessonId;
    private ArrayList<String> studentIds; // multiple student IDs

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_strand);

        spinnerStrands = findViewById(R.id.spinnerStrands);
        spinnerSubStrands = findViewById(R.id.spinnerSubStrands);
        btnAddStrand = findViewById(R.id.btnAddStrand);
        btnAddSubStrand = findViewById(R.id.btnAddSubStrand);
        btnAssign = findViewById(R.id.btnConfirmAssignment);

        lessonId = getIntent().getStringExtra("lessonId");
        studentIds = getIntent().getStringArrayListExtra("studentIds");

        db = FirebaseFirestore.getInstance();

        strandAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, strands);
        strandAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStrands.setAdapter(strandAdapter);

        subStrandAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, subStrands);
        subStrandAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubStrands.setAdapter(subStrandAdapter);

        loadStrands();

        spinnerStrands.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (pos >= 0 && pos < strandIds.size()) {
                    String strandId = strandIds.get(pos);
                    loadSubStrands(strandId);
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnAddStrand.setOnClickListener(v -> showAddDialog("strand"));
        btnAddSubStrand.setOnClickListener(v -> showAddDialog("substrand"));

        btnAssign.setOnClickListener(v -> assignStrand());
    }

    private void showAddDialog(String type) {
        EditText input = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("Add " + type)
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String value = input.getText().toString().trim();
                    if (value.isEmpty()) {
                        Toast.makeText(this, "Name required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (type.equals("strand")) {
                        // Save strand linked to lessonId
                        Map<String, Object> strandData = new HashMap<>();
                        strandData.put("name", value);
                        strandData.put("lessonId", lessonId);
                        db.collection("strands")
                                .add(strandData)
                                .addOnSuccessListener(doc -> loadStrands());
                    } else {
                        int pos = spinnerStrands.getSelectedItemPosition();
                        if (pos >= 0 && pos < strandIds.size()) {
                            String strandId = strandIds.get(pos);
                            db.collection("strands")
                                    .document(strandId)
                                    .collection("subStrands")
                                    .add(Collections.singletonMap("name", value))
                                    .addOnSuccessListener(doc -> loadSubStrands(strandId));
                        }
                    }
                }).setNegativeButton("Cancel", null).show();
    }

    private void loadStrands() {
        strands.clear();
        strandIds.clear();
        // Only get strands linked to this lessonId
        db.collection("strands")
                .whereEqualTo("lessonId", lessonId)
                .get()
                .addOnSuccessListener(query -> {
                    for (QueryDocumentSnapshot doc : query) {
                        strandIds.add(doc.getId());
                        strands.add(doc.getString("name"));
                    }
                    strandAdapter.notifyDataSetChanged();
                    subStrands.clear();
                    subStrandAdapter.notifyDataSetChanged();
                });
    }

    private void loadSubStrands(String strandId) {
        subStrands.clear();
        db.collection("strands")
                .document(strandId)
                .collection("subStrands")
                .get()
                .addOnSuccessListener(query -> {
                    for (QueryDocumentSnapshot doc : query) {
                        subStrands.add(doc.getString("name"));
                    }
                    subStrandAdapter.notifyDataSetChanged();
                });
    }

    private void assignStrand() {
        if (studentIds == null || studentIds.isEmpty()) {
            Toast.makeText(this, "No students to assign", Toast.LENGTH_SHORT).show();
            return;
        }
        if (spinnerStrands.getSelectedItem() == null || spinnerSubStrands.getSelectedItem() == null) {
            Toast.makeText(this, "Please select a strand and sub-strand", Toast.LENGTH_SHORT).show();
            return;
        }

        String strand = spinnerStrands.getSelectedItem().toString();
        String subStrand = spinnerSubStrands.getSelectedItem().toString();

        for (String studentId : studentIds) {
            Map<String, Object> assignment = new HashMap<>();
            assignment.put("lessonId", lessonId);
            assignment.put("studentId", studentId);
            assignment.put("strand", strand);
            assignment.put("subStrand", subStrand);

            db.collection("assignments").add(assignment);
            Intent intent = new Intent(AssignStrandActivity.this, LessonOutcomeActivity.class);
            intent.putExtra("lessonId", lessonId);
            intent.putExtra("strandId", strand);
            intent.putExtra("studentId", studentId);
            intent.putExtra("strandName", strand);
            intent.putExtra("studentName", studentId);
            startActivity(intent);
        }




        Toast.makeText(this, "Assigned to " + studentIds.size() + " students", Toast.LENGTH_SHORT).show();
        finish();
    }
}



