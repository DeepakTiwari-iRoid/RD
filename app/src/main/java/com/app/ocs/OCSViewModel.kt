package com.app.ocs

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OCSViewModel @Inject constructor() : ViewModel() {

     val uriList = mutableListOf<String>()

    fun updateList(value: String) {
        uriList.add(value)
    }

}


