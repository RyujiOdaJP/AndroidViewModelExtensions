package jp.co.arsaga.extensions.viewModel

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDirections

interface HasCloseButtonViewModel {
    var backNavDirections: NavDirections?

    val isClosable: MutableLiveData<Boolean>

    fun initializeCloseButtonListenerFactory(): NavController.OnDestinationChangedListener = NavController.OnDestinationChangedListener { _: NavController, _: NavDestination, _: Bundle? ->
        isClosable.value = true
        backNavDirections = null
    }
}