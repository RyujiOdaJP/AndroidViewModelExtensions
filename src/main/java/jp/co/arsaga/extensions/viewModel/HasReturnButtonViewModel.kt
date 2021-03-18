package jp.co.arsaga.extensions.viewModel

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDirections

interface HasReturnButtonViewModel {
    var backNavDirections: NavDirections?

    val isReturnable: MutableLiveData<Boolean>

    fun initializeReturnButtonListenerFactory(): NavController.OnDestinationChangedListener = NavController.OnDestinationChangedListener { _: NavController, _: NavDestination, _: Bundle? ->
        isReturnable.value = true
        backNavDirections = null
    }
}