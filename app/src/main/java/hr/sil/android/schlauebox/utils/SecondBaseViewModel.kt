package hr.sil.android.schlauebox.utils

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

abstract class SecondBaseViewModel<StateType, Event> : ViewModel() {

    protected val _state: MutableState<Resource<StateType>> = mutableStateOf(Resource.Loading())
    val state: State<Resource<StateType>>
        get() = _state

    protected abstract fun initialState(): StateType

    abstract fun onEvent(event: Event)
}
