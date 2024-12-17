package com.example.flame

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.flame.dataClass.FirebaseDb
import com.example.flame.databinding.ActivityMainBinding
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private val realtimeDb = FirebaseDb.databaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.history.setOnClickListener {
            startActivity(Intent(this, History::class.java))
            finish()
        }

        binding.contact.setOnClickListener {
            // Replace with the phone number you want
            val phoneNumber = "tel:09666959711"
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse(phoneNumber)
            }
            startActivity(dialIntent)
        }

        fetch()

    }

    private fun fetch() {
        val fetchSchedule = realtimeDb.child("sensorData")

        fetchSchedule
            .limitToLast(1)
            .addChildEventListener(object : ChildEventListener {

                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    if(snapshot.exists()){
                        binding.alertType.text = snapshot.child("alerttype").getValue(String::class.java)
                        binding.location.text = snapshot.child("location").getValue(String::class.java)
                        binding.status.text = snapshot.child("status").getValue(String::class.java)
                        binding.timeStamp.text = snapshot.child("time").getValue(String::class.java)
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onChildRemoved(snapshot: DataSnapshot) {}

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()

                }

            })
    }
}