package uz.muhammadyusuf.kurbonov.myclinic.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _searchResult = MutableLiveData<SearchStates>(SearchStates.Loading)

    val searchResult: LiveData<SearchStates> = _searchResult

    fun searchInDatabase(phone: String) {
        viewModelScope.launch {
            _searchResult.value = SearchStates.Loading
            delay(2000)
            _searchResult.value = SearchStates.NotFound
        }
    }

}