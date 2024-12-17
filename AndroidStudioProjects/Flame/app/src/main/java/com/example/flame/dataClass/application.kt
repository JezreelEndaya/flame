package com.example.flame.dataClass

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object FirebaseDb {
    private const val databaseUrl = "https://housemonitor-c1f5f-default-rtdb.asia-southeast1.firebasedatabase.app"
    private val realtimeDb: FirebaseDatabase = FirebaseDatabase.getInstance(databaseUrl)
    val databaseReference: DatabaseReference = realtimeDb.reference
}