package jp.co.arsaga.extensions.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavDirections

interface HasCloseButtonViewModel {
    var backNavDirections: NavDirections?

    val isClosable: MutableLiveData<Boolean>
}