package com.example.smartgovconnect.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartgovconnect.R

class AdminUserAdapter(
    private val onUserClick: (AdminUser) -> Unit
) : RecyclerView.Adapter<AdminUserAdapter.UserViewHolder>() {

    data class AdminUser(
        val uid: String,
        val name: String,
        val email: String,
        val phone: String,
        val age: String,
        val state: String,
        val income: Long?,
        val priorities: List<String>
    )

    private val users = mutableListOf<AdminUser>()

    fun updateUsers(newUsers: List<AdminUser>) {
        users.clear()
        users.addAll(newUsers)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val avatar: TextView = itemView.findViewById(R.id.userAvatar)
        private val name: TextView = itemView.findViewById(R.id.userName)
        private val phone: TextView = itemView.findViewById(R.id.userPhone)
        private val state: TextView = itemView.findViewById(R.id.userState)
        private val priorities: TextView = itemView.findViewById(R.id.userPriorities)

        fun bind(user: AdminUser) {
            val displayName = user.name.ifBlank { "Unknown User" }

            avatar.text = displayName.take(1).uppercase()
            name.text = displayName
            phone.text = if (user.phone.isBlank()) "Phone not available" else user.phone
            state.text = if (user.state.isBlank()) "State not available" else user.state

            priorities.text = when {
                user.priorities.isEmpty() -> "No priorities selected"
                user.priorities.size == 1 -> "1 priority selected"
                else -> "${user.priorities.size} priorities selected"
            }

            itemView.setOnClickListener {
                onUserClick(user)
            }
        }
    }
}