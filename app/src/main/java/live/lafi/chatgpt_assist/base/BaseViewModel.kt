package live.lafi.chatgpt_assist.base

import androidx.lifecycle.ViewModel

abstract class BaseViewModel: ViewModel() {
    override fun onCleared() {
        super.onCleared()
    }
}