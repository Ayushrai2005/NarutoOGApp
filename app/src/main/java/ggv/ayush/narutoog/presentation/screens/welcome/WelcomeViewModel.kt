package ggv.ayush.narutoog.presentation.screens.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ggv.ayush.narutoog.domain.use_cases.UseCases
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val useCases: UseCases
) : ViewModel() {
    fun saveOnBoardingState(completed: Boolean) {
        viewModelScope.launch (Dispatchers.IO){
            useCases.saveOnBoardingCase(completed)
        }
    }

}