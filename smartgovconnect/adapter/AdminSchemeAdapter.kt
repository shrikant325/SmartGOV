package com.example.smartgovconnect.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartgovconnect.R
import com.example.smartgovconnect.model.Domains
import com.example.smartgovconnect.model.Scheme
import com.example.smartgovconnect.ui.AdminDashboardActivity
import com.example.smartgovconnect.ui.SchemeDetailActivity

class AdminSchemeAdapter(
    private val schemes: MutableList<Scheme>,
    private val onDelete: (Scheme) -> Unit
) : RecyclerView.Adapter<AdminSchemeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_admin_scheme,
                parent,
                false
            )

        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(schemes[position])
    }

    override fun getItemCount(): Int =
        schemes.size

    fun updateList(newList: List<Scheme>) {
        schemes.clear()
        schemes.addAll(newList)
        notifyDataSetChanged()
    }

    inner class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val title: TextView =
            itemView.findViewById(R.id.itemTitle)

        private val domain: TextView =
            itemView.findViewById(R.id.itemDomain)

        private val editBtn: ImageView =
            itemView.findViewById(R.id.editBtn)

        private val deleteBtn: ImageView =
            itemView.findViewById(R.id.deleteBtn)

        fun bind(scheme: Scheme) {
            title.text = scheme.title

            val baseText =
                "${Domains.emojiOf(scheme.domain)} " +
                        "${Domains.labelOf(scheme.domain)} · " +
                        scheme.level

            domain.text =
                if (
                    scheme.level.equals(
                        "state",
                        ignoreCase = true
                    ) &&
                    scheme.state.isNotBlank()
                ) {
                    "$baseText · ${scheme.state}"
                } else {
                    baseText
                }

            itemView.setOnClickListener {
                val intent =
                    Intent(
                        itemView.context,
                        SchemeDetailActivity::class.java
                    ).apply {
                        putExtra(
                            SchemeDetailActivity.EXTRA_TITLE,
                            scheme.title
                        )
                        putExtra(
                            SchemeDetailActivity.EXTRA_DOMAIN,
                            scheme.domain
                        )
                        putExtra(
                            SchemeDetailActivity.EXTRA_LEVEL,
                            scheme.level
                        )
                        putExtra(
                            SchemeDetailActivity.EXTRA_ELIGIBILITY,
                            scheme.eligibility
                        )
                        putExtra(
                            SchemeDetailActivity.EXTRA_BENEFITS,
                            scheme.benefits
                        )
                        putExtra(
                            SchemeDetailActivity.EXTRA_APPLY_LINK,
                            scheme.applyLink
                        )
                        putExtra(
                            SchemeDetailActivity.EXTRA_IMAGE_URL,
                            scheme.imageUrl
                        )
                    }

                itemView.context.startActivity(
                    intent
                )
            }

            editBtn.setOnClickListener {
                val intent =
                    Intent(
                        itemView.context,
                        AdminDashboardActivity::class.java
                    ).apply {
                        putExtra("editMode", true)
                        putExtra("schemeId", scheme.id)
                        putExtra("title", scheme.title)
                        putExtra("domain", scheme.domain)
                        putExtra("level", scheme.level)
                        putExtra("state", scheme.state)
                        putExtra("eligibility", scheme.eligibility)
                        putExtra("benefits", scheme.benefits)
                        putExtra("applyLink", scheme.applyLink)
                        putExtra("imageUrl", scheme.imageUrl)
                        putExtra("incomeCap", scheme.incomeCap)
                    }

                itemView.context.startActivity(intent)
            }

            deleteBtn.setOnClickListener {
                onDelete(scheme)
            }
        }
    }
}