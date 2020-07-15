package jp.co.arsaga.extensions.viewModel

import androidx.lifecycle.MutableLiveData

interface ViewModelBusyCheck {
    val isBusy: MutableLiveData<Boolean>
}

@Suppress("NOTHING_TO_INLINE")
inline fun busyCheck(viewModelBusyCheck: ViewModelBusyCheck) {
    if (viewModelBusyCheck.isBusy.value == true) return
    else viewModelBusyCheck.isBusy.value = true
}