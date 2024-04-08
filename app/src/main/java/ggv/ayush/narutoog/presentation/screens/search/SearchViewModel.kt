package ggv.ayush.narutoog.presentation.screens.search

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import ggv.ayush.narutoog.domain.model.Hero
import ggv.ayush.narutoog.domain.use_cases.UseCases
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val useCases: UseCases
) : ViewModel() {

    private val _searchQuery = mutableStateOf("")
    val searchQuery = _searchQuery

    private val _searchedHeroes = MutableStateFlow<PagingData<Hero>>(PagingData.empty())
    val searchedHeroes = _searchedHeroes
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun searchHeroes( query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            useCases.searchHeroesUseCase(query).cachedIn(viewModelScope).collect {
                _searchedHeroes.value = it
            }

        }
    }


}