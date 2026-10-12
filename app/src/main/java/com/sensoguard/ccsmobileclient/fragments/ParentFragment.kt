package com.sensoguard.ccsmobileclient.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.sensoguard.ccsmobileclient.global.configurationLanguage

/**
 * the parent of the fragments of the app (set the selected language)
 */
open class ParentFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configurationLanguage(requireActivity())
    }
}
