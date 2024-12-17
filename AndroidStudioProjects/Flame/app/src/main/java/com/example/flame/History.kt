package com.example.flame

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.alimentapet.adapter.MyAdapter
import com.example.flame.dataClass.FirebaseDb
import com.example.flame.dataClass.history
import com.example.flame.databinding.ActivityHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class History : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding

    private lateinit var recyclerView: RecyclerView
    private val historyList = mutableListOf<history>()
    private lateinit var myAdapter: MyAdapter

    private val realtimeDb = FirebaseDb.databaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = binding.historyView
        recyclerView.layoutManager = LinearLayoutManager(this)
        myAdapter = MyAdapter(historyList)
        recyclerView.adapter = myAdapter

        binding.home.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        fetchAlerts()
    }

    private fun fetchAlerts() {
        val fetchSchedule = realtimeDb.child("sensorData")

        fetchSchedule
            .addChildEventListener(object : ChildEventListener {

                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    if(snapshot.exists()){
                        val alarm = snapshot.getValue(history::class.java)
                        alarm?.let {
                            it.id = snapshot.key.toString()
                            historyList.add(it)
                            myAdapter.notifyDataSetChanged()
                        }
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onChildRemoved(snapshot: DataSnapshot) {}

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@History,
                        "Error: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()

                }

            })
    }
}