package com.ndhunju.relay.ui

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.ndhunju.relay.R
import com.ndhunju.relay.api.Result
import com.ndhunju.relay.ui.custom.SyncStatusIcon
import com.ndhunju.relay.ui.messages.Message
import com.ndhunju.relay.ui.theme.Colors
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert
import org.junit.Rule
import org.junit.Test


class MainScreenUiTest {

    @get:Rule val composeTestRule = createComposeRule()
    private val context by lazy {
        // Use targetContext to get the resources from the main folder.
        // Use context to get the resources from the test folder.
        InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun givenTheSyncStatusOfTheMessageTheCorrespondingSyncStatusIconTintShouldBeUsed() {
        composeTestRule.setContent {
            // Initialize MainContent with fake messages
            val fakeMessages = remember { mutableStateListOf<Message>() }
            fakeMessages.addAll(com.ndhunju.relay.ui.fakeMessages)
            MainContent(
                lastMessageList = fakeMessages,
                showErrorMessageForPermissionDenied = MutableStateFlow(false).collectAsState()
            )
        }

        fakeMessages.forEach { fakeMessage ->
            assertSyncStatusIconMatchesForGivenMessage(fakeMessage)
        }

    }

    private fun assertSyncStatusIconMatchesForGivenMessage(message: Message) {
        val imageBitmapOfSyncIcon = composeTestRule.onNode(
            /** This alone returns multiple nodes as same description is used for each item **/
            hasContentDescription(context.getString(R.string.image_description_sync_status_logo))
                /** Filter down to [SyncStatusIcon] of the first message **/
                .and(hasParent(hasAnyChild(hasText(message.body)))),
            true
        ).captureToImage()

        /** Get the pixel of a non transparent area in [R.drawable.baseline_sync_status_24] icon **/
        val pixel = imageBitmapOfSyncIcon.asAndroidBitmap().getPixel(
            imageBitmapOfSyncIcon.width / 3,
            imageBitmapOfSyncIcon.height / 3
        )

        val expectedIconTintColor = if (message.syncStatus is Result.Success) {
            Colors().success
        } else {
            Colors().failure
        }

        Assert.assertEquals("Failed for $message",expectedIconTintColor.toArgb(), pixel)
    }
}
