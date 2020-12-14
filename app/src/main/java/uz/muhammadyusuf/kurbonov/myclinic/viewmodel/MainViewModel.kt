package uz.muhammadyusuf.kurbonov.myclinic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val repository: ContactsRepository) : ViewModel() {

    private val _searchResult = MutableStateFlow<SearchStates>(SearchStates.Loading)

    val searchResult: StateFlow<SearchStates> = _searchResult

    fun searchInDatabase(phone: String) {
        viewModelScope.launch {
            _searchResult.value = SearchStates.Loading
            val result = repository.getContact(phone)
            if (result == null)
                _searchResult.value = SearchStates.NotFound
            else
                _searchResult.value = SearchStates.Found(result)
        }
    }

}