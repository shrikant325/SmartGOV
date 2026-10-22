package com.example.smartgovconnect.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.smartgovconnect.R
import com.example.smartgovconnect.model.Domains
import com.example.smartgovconnect.model.IncomeBrackets
import com.google.android.material.button.MaterialButton

class SchemeDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_DOMAIN = "extra_domain"
        const val EXTRA_LEVEL = "extra_level"
        const val EXTRA_STATE = "extra_state"
        const val EXTRA_ELIGIBILITY = "extra_eligibility"
        const val EXTRA_BENEFITS = "extra_benefits"
        const val EXTRA_APPLY_LINK = "extra_apply_link"
        const val EXTRA_IMAGE_URL = "extra_image_url"
        const val EXTRA_INCOME_CAP = "extra_income_cap"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scheme_detail)

        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        val domain = intent.getStringExtra(EXTRA_DOMAIN).orEmpty()
        val level = intent.getStringExtra(EXTRA_LEVEL).orEmpty()
        val state = intent.getStringExtra(EXTRA_STATE).orEmpty()
        val eligibility = intent.getStringExtra(EXTRA_ELIGIBILITY).orEmpty()
        val benefits = intent.getStringExtra(EXTRA_BENEFITS).orEmpty()
        val applyLink = intent.getStringExtra(EXTRA_APPLY_LINK).orEmpty()
        val imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL).orEmpty()
        val incomeCap = intent.getLongExtra(EXTRA_INCOME_CAP, -1L)

        val detailImage = findViewById<ImageView>(R.id.detailImage)
        val placeholder = findViewById<TextView>(R.id.detailImagePlaceholder)
        val detailTitle = findViewById<TextView>(R.id.detailTitle)
        val detailDomain = findViewById<TextView>(R.id.detailDomainBadge)
        val detailLevel = findViewById<TextView>(R.id.detailLevel)
        val detailEligibility = findViewById<TextView>(R.id.detailEligibility)
        val detailBenefits = findViewById<TextView>(R.id.detailBenefits)
        val detailIncomeCap = findViewById<TextView>(R.id.detailIncomeCap)

        val emoji = Domains.emojiOf(domain)
        val domainLabel = Domains.labelOf(domain)

        detailTitle.text = title
        detailDomain.text = "$emoji $domainLabel"

        detailLevel.text =
            if (level.equals("state", ignoreCase = true)) {
                if (state.isNotBlank()) {
                    "State Government Scheme • $state"
                } else {
                    "State Government Scheme"
                }
            } else {
                "Central Government Scheme"
            }

        detailEligibility.text = eligibility
        detailBenefits.text = benefits

        placeholder.text = emoji
        placeholder.visibility = View.VISIBLE
        detailImage.visibility = View.GONE

        if (imageUrl.isNotBlank()) {
            detailImage.visibility = View.VISIBLE

            Glide.with(this)
                .load(imageUrl)
                .centerCrop()
                .into(detailImage)
        }

        if (Domains.isIncomeDomain(domain) && incomeCap >= 0) {
            detailIncomeCap.visibility = View.VISIBLE
            detailIncomeCap.text =
                "💵 Eligible income: ${IncomeBrackets.labelFor(incomeCap)}"
        } else {
            detailIncomeCap.visibility = View.GONE
        }

        findViewById<ImageView>(R.id.backBtn).setOnClickListener {
            finish()
        }

        findViewById<MaterialButton>(R.id.detailApplyBtn)
            .setOnClickListener {
                if (applyLink.isBlank()) {
                    Toast.makeText(
                        this,
                        "No application link was provided for this scheme.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    try {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(applyLink)
                            )
                        )
                    } catch (_: Exception) {
                        Toast.makeText(
                            this,
                            "Couldn't open this application link.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }
}