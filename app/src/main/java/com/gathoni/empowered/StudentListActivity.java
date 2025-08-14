package com.gathoni.empowered;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class StudentListActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    List<Student> studentList = new ArrayList<>();
    StudentAdapter adapter;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list);

        recyclerView = findViewById(R.id.recyclerStudents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        loadStudents();
    }

    private void loadStudents() {
        db.collection("students").get().addOnSuccessListener(query -> {
            for (DocumentSnapshot doc : query.getDocuments()) {
                Student s = doc.toObject(Student.class);
                s.setId(doc.getId());
                studentList.add(s);
            }
           // adapter = new StudentAdapter(studentList, student -> {
               //Intent i = new Intent(this, StudentLessonsActivity.class);
               // i.putExtra("studentId", student.getId());
               // startActivity(i);
           // });
            recyclerView.setAdapter(adapter);
        });
    }
}


