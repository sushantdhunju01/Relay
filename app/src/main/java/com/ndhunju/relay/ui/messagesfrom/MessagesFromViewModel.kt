package com.ndhunju.relay.ui.messagesfrom

import android.telephony.SmsManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndhunju.relay.api.Result
import com.ndhunju.relay.data.SmsInfoRepository
import com.ndhunju.relay.service.AppStateBroadcastService
import com.ndhunju.relay.service.DeviceSmsReaderService
import com.ndhunju.relay.ui.messages.Message
import com.ndhunju.relay.util.extensions.asState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MessagesFromViewModel(
    private val appStateBroadcastService: AppStateBroadcastService,
    private val deviceSmsReaderService: DeviceSmsReaderService,
    private val smsInfoRepository: SmsInfoRepository,
): ViewModel() {

    lateinit var senderAddress: String
    lateinit var threadId: String

    private var _messagesInThread: SnapshotStateList<Message> = mutableStateListOf()
    var messagesInThread: List<Message> = _messagesInThread

    /**
     * NOTE: This is better way than declaring
     * _isLoading = mutableStateOf()
     * isLoading = _isLoading.asState()
     */
    var isLoading by mutableStateOf(true)
        private set

    private var _textMessage = mutableStateOf("")
    var textMessage = _textMessage.asState()

    var onTextMessageChange: ((String) -> Unit) = {
        _textMessage.value = it
    }

    private var latestNewMessageTimeStamp: Long = System.currentTimeMillis()

    private val newMessageObserver = Observer<Long> { newMessageTimeStamp ->
        viewModelScope.launch(Dispatchers.IO) {
            val newMessages = deviceSmsReaderService.getMessagesSince(latestNewMessageTimeStamp)
            latestNewMessageTimeStamp = newMessageTimeStamp
            newMessages.filter { it.threadId == threadId }
                .forEach(onNewSmsReceived)
        }
    }

    private val newSyncedMessageObserver = Observer<List<Message>> { newMessages ->
        newMessages.forEach(onNewSyncedMessage)
    }

    init {
        appStateBroadcastService.newMessagesReceivedTime.observeForever(newMessageObserver)
        appStateBroadcastService.newSyncedMessages.observeForever(newSyncedMessageObserver)
    }

    override fun onCleared() {
        appStateBroadcastService.newMessagesReceivedTime.removeObserver(newMessageObserver)
        appStateBroadcastService.newSyncedMessages.removeObserver(newSyncedMessageObserver)
        super.onCleared()
    }

    private val onNewSmsReceived: (Message) -> Unit = { messageFromAndroidDb ->
        // Update the UI to the the latest SMS
        viewModelScope.launch {
            // NOTE: Since the layout is reversed, add new item to the top
            _messagesInThread.add(0, messageFromAndroidDb)
        }
    }

    private val onNewSyncedMessage: (Message) -> Unit = { newSyncedMessage ->
        viewModelScope.launch {
            // Update the UI
            val oldLastMessageIndex = findIndexOfMessage(newSyncedMessage)
            if (oldLastMessageIndex > -1) {
                // Update the icon based on update call status
                updateMessageAt(oldLastMessageIndex, syncStatus = newSyncedMessage.syncStatus)
            }
        }
    }

    /**
     * Finds the index of [message] in [_lastMessageForEachThread]
     */
    private fun findIndexOfMessage(message: Message): Int {
        // Find the thread in which the message is sent to
        _messagesInThread.forEachIndexed { index, lastMessage ->
            if (lastMessage.threadId == message.threadId) {
                return index
            }
        }
        return -1
    }

    /**
     * Updates the [Message] at [index] with passed non null values
     */
    private fun updateMessageAt(
        index: Int,
        body: String? = null,
        date: Long? = null,
        syncStatus: Result<Void>? = null
    ) {
        val exitingCopy = _messagesInThread[index]
        _messagesInThread[index].copy(
            body = body ?: exitingCopy.body,
            date = date ?: exitingCopy.date,
            syncStatus = syncStatus ?: exitingCopy.syncStatus
        ).let {
            _messagesInThread[index] = it
        }
    }

    /**
     * Returns list of message for passed [threadId]
     */
    fun getSmsByThreadId(threadId: String) {
        viewModelScope.launch {
            isLoading = true
            val messages = deviceSmsReaderService.getSmsByThreadId(threadId)
            // Populate the syncStatus of each message based on info stored in local database
            smsInfoRepository.getSmsInfoForEachIdInAndroidDb(
                messages.map { message -> message.idInAndroidDb }
            ).forEachIndexed { i, smsInfo ->
                messages[i].syncStatus = smsInfo?.syncStatus
            }

            _messagesInThread.clear()
            // Update the state with the messages.
            _messagesInThread.addAll(messages)
            isLoading = false
        }
    }

    fun sendTextMessage() {
        // TODO: Send text message

    }
}