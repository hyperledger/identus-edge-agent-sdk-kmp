package org.hyperledger.identus.walletsdk.sampleapp

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.TextView

interface Logger {
    fun error(text: String)
    fun warning(text: String)
    fun info(text: String)
}

class TextFieldLogger(private val textView: TextView) : Logger {
    private val logs: MutableList<SpannableString> = mutableListOf()

    init {
        textView.text = ""
    }

    override fun error(text: String) {
        val spannableText = SpannableString(text)
        spannableText.setSpan(ForegroundColorSpan(Color.RED), 0, spannableText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        logs.add(spannableText)
        textView.text = ""
        logs.reversed().forEach {
            textView.append(it)
        }
    }

    override fun warning(text: String) {
        val spannableText = SpannableString(text)
        spannableText.setSpan(ForegroundColorSpan(Color.YELLOW), 0, spannableText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        logs.add(spannableText)
        textView.text = ""
        logs.reversed().forEach {
            textView.append(it)
        }
    }

    override fun info(text: String) {
        val spannableText = SpannableString(text)
        spannableText.setSpan(ForegroundColorSpan(Color.BLUE), 0, spannableText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        logs.add(spannableText)
        textView.text = ""
        logs.reversed().forEach {
            textView.append(it)
        }
    }
}
