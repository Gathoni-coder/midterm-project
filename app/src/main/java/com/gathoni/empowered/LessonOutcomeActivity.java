package com.gathoni.empowered;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LessonOutcomeActivity extends AppCompatActivity {

    private EditText etLessonOutcome, etAreasForImprovement;
    private TextView tvStudentName, tvStrandName;
    private String studentId, lessonId, strandId, substrandId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_outcome);

        tvStudentName = findViewById(R.id.tvStudentName);
        tvStrandName = findViewById(R.id.tvStrandName);
        etLessonOutcome = findViewById(R.id.etLessonOutcome);
        etAreasForImprovement = findViewById(R.id.etAreasForImprovement);
        Button btnSaveOutcome = findViewById(R.id.btnSaveOutcome);

        // Get data from Intent
        studentId = getIntent().getStringExtra("studentId");
        lessonId = getIntent().getStringExtra("lessonId");
        strandId = getIntent().getStringExtra("strandId");
        substrandId = getIntent().getStringExtra("substrandId");
        String studentName = getIntent().getStringExtra("studentName");
        String strandName = getIntent().getStringExtra("strandName");

        tvStudentName.setText("Student: " + studentName);
        tvStrandName.setText("Strand: " + strandName);

        btnSaveOutcome.setOnClickListener(v -> saveOutcome());
    }

    private void saveOutcome() {
        String outcome = etLessonOutcome.getText().toString().trim();
        String improvement = etAreasForImprovement.getText().toString().trim();

        if (outcome.isEmpty() || improvement.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Save to database (Firebase / Room)
        Map<String, Object> data = new HashMap<>();
        data.put("studentId", studentId);
        data.put("lessonId", lessonId);
        data.put("strandId", strandId);
        data.put("substrandId", substrandId);
        data.put("lessonOutcome", outcome);
        data.put("areasForImprovement", improvement);
        data.put("dateAdded", System.currentTimeMillis());

        FirebaseFirestore.getInstance().collection("student_strand_outcome")
                .add(data)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Outcome saved", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}



