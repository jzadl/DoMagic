package com.domagic.db

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class BootEntry(
    val id: String = "",
    val model: String = "",
    val manufacturer: String = "",
    val androidVersion: String = "",
    val buildNumber: String = "",
    val abi: String = "",
    val fingerprint: String = "",
    val downloadUrl: String = "",
    val uploadedBy: String = "community",
    val timestamp: Long = System.currentTimeMillis(),
    val verified: Boolean = false,    // manually verified by maintainer
    val downloadCount: Int = 0,
)

object BootImageRepository {

    private val db = Firebase.database.reference.child("boot_images")

    // ── Read ─────────────────────────────────────────────────────────────────

    fun observeAll(): Flow<List<BootEntry>> = callbackFlow {
        val listener = db.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val list = snapshot.children.mapNotNull { child ->
                    child.getValue(BootEntry::class.java)?.copy(id = child.key ?: "")
                }
                trySend(list)
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                close(error.toException())
            }
        })
        awaitClose { db.removeEventListener(listener) }
    }

    fun searchByModel(query: String): Flow<List<BootEntry>> = callbackFlow {
        val q = query.lowercase()
        val listener = db.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val list = snapshot.children
                    .mapNotNull { it.getValue(BootEntry::class.java)?.copy(id = it.key ?: "") }
                    .filter { entry ->
                        entry.model.lowercase().contains(q) ||
                        entry.manufacturer.lowercase().contains(q) ||
                        entry.buildNumber.lowercase().contains(q) ||
                        entry.androidVersion.contains(q)
                    }
                trySend(list)
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                close(error.toException())
            }
        })
        awaitClose { db.removeEventListener(listener) }
    }

    // ── Submit ───────────────────────────────────────────────────────────────

    suspend fun submitEntry(entry: BootEntry): String {
        val newRef = db.push()
        newRef.setValue(entry.copy(
            id = newRef.key ?: "",
            timestamp = System.currentTimeMillis()
        )).await()

        return newRef.key ?: ""
    }

    suspend fun incrementDownload(entryId: String) {
        val ref = db.child(entryId).child("downloadCount")
        ref.get().await().getValue(Int::class.java)?.let { current ->
            ref.setValue(current + 1).await()
        }
    }
}
