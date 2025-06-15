package com.example.movieticket.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieticket.data.model.Ticket
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import android.util.Log

data class MyTicketState(
    val tickets: List<Ticket> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentTicket: Ticket? = null
)

@HiltViewModel
class MyTicketViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = MutableStateFlow(MyTicketState())
    val state = _state.asStateFlow()

    init {
        loadTickets()
    }

    fun loadTickets() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                
                val userId = auth.currentUser?.uid
                Log.d("MyTicketViewModel", "Current userId: $userId")
                
                if (userId == null) {
                    Log.e("MyTicketViewModel", "User not logged in")
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "User not logged in"
                    )
                    return@launch
                }

                val ticketsRef = firestore.collection("tickets")
                    .whereEqualTo("userId", userId)
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
                
                Log.d("MyTicketViewModel", "Fetching tickets for user: $userId")
                
                val querySnapshot = ticketsRef.get().await()
                Log.d("MyTicketViewModel", "Found ${querySnapshot.size()} tickets")
                
                val tickets = querySnapshot.documents.mapNotNull { 
                    try {
                        val ticket = it.toObject(Ticket::class.java)
                        Log.d("MyTicketViewModel", "Parsed ticket: $ticket")
                        ticket
                    } catch (e: Exception) {
                        Log.e("MyTicketViewModel", "Error parsing ticket: ${e.message}")
                        null
                    }
                }.reversed()

                Log.d("MyTicketViewModel", "Final tickets list size: ${tickets.size}")
                
                _state.value = _state.value.copy(
                    tickets = tickets,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                Log.e("MyTicketViewModel", "Error loading tickets: ${e.message}")
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun getTicket(ticketId: String): Ticket? {
        // First try to get from current state
        val ticketFromState = state.value.tickets.find { it.id == ticketId }
        if (ticketFromState != null) {
            _state.value = _state.value.copy(currentTicket = ticketFromState)
            return ticketFromState
        }

        // If not found in state, load from Firestore
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                
                val userId = auth.currentUser?.uid
                if (userId == null) {
                    Log.e("MyTicketViewModel", "User not logged in")
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "User not logged in"
                    )
                    return@launch
                }

                val ticketDoc = firestore.collection("tickets")
                    .document(ticketId)
                    .get()
                    .await()

                if (ticketDoc.exists()) {
                    val ticket = ticketDoc.toObject(Ticket::class.java)
                    if (ticket != null && ticket.userId == userId) {
                        _state.value = _state.value.copy(
                            currentTicket = ticket,
                            isLoading = false,
                            error = null
                        )
                    } else {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "Ticket not found or unauthorized"
                        )
                    }
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Ticket not found"
                    )
                }
            } catch (e: Exception) {
                Log.e("MyTicketViewModel", "Error loading ticket: ${e.message}")
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
        return state.value.currentTicket
    }

    companion object {
        suspend fun checkSeatsAvailability(
            movieId: Int,
            date: String,
            time: String,
            seats: List<String>
        ): Boolean {
            return try {
                val existingTickets = FirebaseFirestore.getInstance()
                    .collection("tickets")
                    .whereEqualTo("movieId", movieId)
                    .whereEqualTo("date", date)
                    .whereEqualTo("time", time)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { it.toObject(Ticket::class.java) }

                val bookedSeats = existingTickets.flatMap { it.seats }
                !seats.any { it in bookedSeats }
            } catch (e: Exception) {
                false
            }
        }

        suspend fun saveTicket(ticket: Ticket): Boolean {
            return try {
                // Kiểm tra ghế trước khi lưu
                val seatsAvailable = checkSeatsAvailability(
                    ticket.movieId,
                    ticket.date,
                    ticket.time,
                    ticket.seats
                )

                if (!seatsAvailable) {
                    return false
                }

                // Lưu vé
                val ticketRef = FirebaseFirestore.getInstance().collection("tickets").document()
                val ticketWithId = ticket.copy(id = ticketRef.id)
                ticketRef.set(ticketWithId).await()
                true
            } catch (e: Exception) {
                false
            }
        }
    }
} 