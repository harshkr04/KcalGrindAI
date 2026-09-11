package com.lumina.nutrition.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.nutrition.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface LaunchDestination {
    data object Loading : LaunchDestination
    data object Home : LaunchDestination
    data object Welcome : LaunchDestination
}

@HiltViewModel
class LaunchViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _destination = MutableStateFlow<LaunchDestination>(LaunchDestination.Loading)
    val destination: StateFlow<LaunchDestination> = _destination.asStateFlow()

    init {
        checkUserProfile()
    }

    fun checkUserProfile() {
        viewModelScope.launch {
            val profile = userProfileRepository.getProfile()
            if (profile != null) {
                _destination.value = LaunchDestination.Home
            } else {
                _destination.value = LaunchDestination.Welcome
            }
        }
    }
}
