package com.example.smartgovconnect.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.smartgovconnect.R
import com.example.smartgovconnect.model.Domains
import com.example.smartgovconnect.model.Scheme
import com.example.smartgovconnect.ui.SchemeDetailActivity
import com.google.android.material.button.MaterialButton

class SchemeAdapter(
    private var schemes: List<Scheme>
) : RecyclerView.Adapter<SchemeAdapter.SchemeViewHolder>() {

    class SchemeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.schemeTitle)
        val domain: TextView = itemView.findViewById(R.id.schemeDomain)
        val level: TextView = itemView.findViewById(R.id.schemeLevel)
        val eligibility: TextView = itemView.findViewById(R.id.schemeEligibility)
        val benefits: TextView = itemView.findViewById(R.id.schemeBenefits)
        val image: ImageView = itemView.findViewById(R.id.schemeImage)
        val placeholder: TextView = itemView.findViewById(R.id.schemeImagePlaceholder)
        val applyBtn: MaterialButton = itemView.findViewById(R.id.applyBtn)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SchemeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scheme, parent, false)

        return SchemeViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: SchemeViewHolder,
        position: Int
    ) {
        val scheme = schemes[position]

        val domainLabel = Domains.labelOf(scheme.domain)
        val domainEmoji = Domains.emojiOf(scheme.domain)

        holder.title.text = scheme.title
        holder.domain.text = "$domainEmoji $domainLabel"
        holder.eligibility.text = "Eligibility: ${scheme.eligibility}"
        holder.benefits.text = "Benefits: ${scheme.benefits}"

        holder.level.text =
            if (scheme.level.equals("state", ignoreCase = true)) {
                "State: ${scheme.state}"
            } else {
                "Central Government"
            }

        holder.placeholder.text = domainEmoji
        holder.placeholder.visibility = View.VISIBLE

        if (scheme.imageUrl.isNotBlank()) {
            holder.image.visibility = View.VISIBLE

            Glide.with(holder.itemView.context)
                .load(scheme.imageUrl)
                .centerCrop()
                .into(holder.image)
        } else {
            holder.image.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            openDetails(holder.itemView, scheme)
        }

        holder.applyBtn.setOnClickListener {
            openDetails(holder.itemView, scheme)
        }
    }

    private fun openDetails(
        view: View,
        scheme: Scheme
    ) {
        val intent = Intent(
            view.context,
            SchemeDetailActivity::class.java
        ).apply {
            putExtra(SchemeDetailActivity.EXTRA_TITLE, scheme.title)
            putExtra(SchemeDetailActivity.EXTRA_DOMAIN, scheme.domain)
            putExtra(SchemeDetailActivity.EXTRA_LEVEL, scheme.level)
            putExtra(SchemeDetailActivity.EXTRA_STATE, scheme.state)
            putExtra(SchemeDetailActivity.EXTRA_ELIGIBILITY, scheme.eligibility)
            putExtra(SchemeDetailActivity.EXTRA_BENEFITS, scheme.benefits)
            putExtra(SchemeDetailActivity.EXTRA_APPLY_LINK, scheme.applyLink)
            putExtra(SchemeDetailActivity.EXTRA_IMAGE_URL, scheme.imageUrl)
            putExtra(SchemeDetailActivity.EXTRA_INCOME_CAP, scheme.incomeCap)
        }

        view.context.startActivity(intent)
    }

    override fun getItemCount(): Int {
        return schemes.size
    }

    fun updateList(newList: List<Scheme>) {
        schemes = newList
        notifyDataSetChanged()
    }

    fun updateSchemes(newList: List<Scheme>) {
        schemes = newList
        notifyDataSetChanged()
    }
}