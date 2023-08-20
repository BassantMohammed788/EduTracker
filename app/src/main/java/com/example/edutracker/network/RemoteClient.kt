package com.example.edutracker.network

import android.content.Context
import android.widget.Toast
import com.example.edutracker.utilities.Constants
import com.example.edutracker.dataclasses.Assistant
import com.example.edutracker.dataclasses.Group
import com.example.edutracker.dataclasses.Lesson
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RemoteClient private constructor() : RemoteInterface {
    private val databaseReference: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference.child(Constants.DATABASE_NAME)
    }

    companion object {
        private var instance: RemoteClient? = null
        fun getInstance(): RemoteClient {
            return instance ?: synchronized(this) {
                val temp = RemoteClient()
                instance = temp
                temp
            }
        }
    }

    override fun addAssistant(assistant: Assistant, teacher_id: String): Flow<Boolean> = callbackFlow {
        val assistantRef = databaseReference.child(Constants.TEACHER_NODE).child(teacher_id)
            .child(Constants.ASSISTANT_NODE).child(assistant.email)

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val emailExists = dataSnapshot.exists()
                if (emailExists) {
                    trySend(false)
                } else {
                    assistantRef.setValue(assistant)
                        .addOnSuccessListener {
                            trySend(true)
                        }
                        .addOnFailureListener { exception ->
                            close(exception)
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        assistantRef.addListenerForSingleValueEvent(valueEventListener)

        awaitClose { assistantRef.removeEventListener(valueEventListener) }
    }
    override fun getAllAssistants(teacher_id: String): Flow<List<Assistant>> = callbackFlow {
        val assistantsRef = databaseReference.child(Constants.TEACHER_NODE).child(teacher_id).child(
            Constants.ASSISTANT_NODE
        )
        val assistantsList = mutableListOf<Assistant>()

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                assistantsList.clear()
                for (assistantSnapshot in dataSnapshot.children) {
                    val assistant = assistantSnapshot.getValue(Assistant::class.java)
                    assistant?.let { assistantsList.add(it) }
                }
                trySend(assistantsList.toList()).isSuccess // Emit the updated list
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException()) // Close the flow with an error
            }
        }

        assistantsRef.addValueEventListener(valueEventListener)

        // Remove the listener when the flow collector is cancelled
        awaitClose { assistantsRef.removeEventListener(valueEventListener) }
    }

    override fun getAssistantByEmail(teacher_id: String, email: String): Flow<Assistant> =
        callbackFlow {
            val assistantRef = databaseReference.child(Constants.TEACHER_NODE).child(teacher_id)
                .child(Constants.ASSISTANT_NODE).child(email)

            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val assistant = dataSnapshot.getValue(Assistant::class.java)
                    if (assistant != null) {
                        trySend(assistant).isSuccess // Emit the assistant to the flow
                    } else {
                        // Handle the case when assistant is null
                        // You can close the flow or take appropriate action
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException()) // Close the flow with an error
                }
            }
            assistantRef.addValueEventListener(valueEventListener)

            // Remove the listener when the flow collector is cancelled
            awaitClose { assistantRef.removeEventListener(valueEventListener) }
        }

    override fun updateAssistantData(teacher_id: String, email: String, updatedAssistant: Assistant): Flow<Assistant> = callbackFlow {
        val assistantRef = databaseReference.child(Constants.TEACHER_NODE).child(teacher_id)
            .child(Constants.ASSISTANT_NODE).child(email)
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                assistantRef.setValue(updatedAssistant)
                    .addOnSuccessListener {
                        trySend(updatedAssistant).isSuccess // Emit the updated assistant to the flow
                    }
                    .addOnFailureListener { exception ->
                        close(exception) // Close the flow with an error
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException()) // Close the flow with an error
            }
        }

        assistantRef.addListenerForSingleValueEvent(valueEventListener)

        // Remove the listener when the flow collector is cancelled
        awaitClose { assistantRef.removeEventListener(valueEventListener) }
    }
    override fun deleteAssistantByEmail(teacher_id: String, email: String) {
        val assistantRef = databaseReference.child(Constants.TEACHER_NODE).child(teacher_id)
            .child(Constants.ASSISTANT_NODE).child(email)
        assistantRef.removeValue()
    }


    override fun addGroup(group: Group, teacher_id: String, semester: String): Flow<Boolean> = callbackFlow {
        val groupRef = databaseReference.child(Constants.TEACHER_NODE).child(teacher_id)
            .child(semester).child(Constants.GRADE_NODE).child(group.gradeLevel).child(group.id)

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val emailExists = dataSnapshot.exists()
                if (emailExists) {
                    trySend(false)
                } else {
                    groupRef.setValue(group)
                        .addOnSuccessListener {
                            trySend(true)
                        }
                        .addOnFailureListener { exception ->
                            close(exception)
                        }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        groupRef.addListenerForSingleValueEvent(valueEventListener)

        awaitClose { groupRef.removeEventListener(valueEventListener) }
    }

    override fun getAllGroups(semester: String, teacher_id: String, gradeLevel: String): Flow<List<Group>> = callbackFlow {
        val groupsRef = databaseReference.child(Constants.TEACHER_NODE).child(teacher_id)
            .child(semester).child(Constants.GRADE_NODE).child(gradeLevel)
        val groupsList = mutableListOf<Group>()

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                groupsList.clear()
                for (assistantSnapshot in dataSnapshot.children) {
                    val assistant = assistantSnapshot.getValue(Group::class.java)
                    assistant?.let { groupsList.add(it) }
                }
                trySend(groupsList.toList()).isSuccess // Emit the updated list
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException()) // Close the flow with an error
            }
        }

        groupsRef.addValueEventListener(valueEventListener)

        // Remove the listener when the flow collector is cancelled
        awaitClose { groupsRef.removeEventListener(valueEventListener) }
    }

    override fun deleteGroup(semester: String, teacher_id: String, gradeLevel: String, group_id: String)
    {
        val groupsRef = databaseReference.child(Constants.TEACHER_NODE).child(teacher_id)
            .child(semester).child(Constants.GRADE_NODE).child(gradeLevel).child(group_id)
        groupsRef.removeValue()
    }

    override fun addLesson( teacher_id: String, semester: String,grade_level: String,month:String,lesson: Lesson): Flow<Boolean> = callbackFlow {
        val lessonRef = databaseReference.child(Constants.TEACHER_NODE).child(teacher_id)
            .child(semester).child(Constants.GRADE_NODE).child(grade_level).child(lesson.groupId).child(month).child(Constants.LESSON_NODE).child(lesson.id)

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val lessonExists = dataSnapshot.exists()
                if (lessonExists) {
                    trySend(false)
                } else {
                    lessonRef.setValue(lesson)
                        .addOnSuccessListener {
                            trySend(true)
                        }
                        .addOnFailureListener { exception ->
                            close(exception)
                        }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        lessonRef.addListenerForSingleValueEvent(valueEventListener)

        awaitClose { lessonRef.removeEventListener(valueEventListener) }
    }




}