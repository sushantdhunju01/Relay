package com.ndhunju.relay.ui.messages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.ndhunju.relay.R
import com.ndhunju.relay.ui.mockMessages
import com.ndhunju.relay.ui.theme.LocalDimens
import com.ndhunju.relay.util.LogCompositions
import com.ndhunju.relay.util.dateFormat

@Preview(showBackground = true)
@Composable
fun MessageListItemPreview() {
    MessageListItem(
        message = mockMessages.first(),
        isSynced = true,
        onClick = {}
    )
}

@Composable
fun MessageListItem(
    message: Message,
    isSynced: Boolean,
    onClick: (Message) -> Unit
) {
    ConstraintLayout(modifier = Modifier
        .clickable { onClick(message) }
    ) {
        val (divider, from, body, date, status) = createRefs()
        val itemVerticalPadding = LocalDimens.current.itemPaddingVertical
        val contentHorizontalPadding = LocalDimens.current.contentPaddingHorizontal

        LogCompositions(tag = "MessageListItem", msg = "MessageListItem scope")

        Divider(Modifier.constrainAs(divider) {
            top.linkTo(parent.top)
            width = Dimension.fillToConstraints
        })

        Text(text = message.from, Modifier.constrainAs(from) {
            top.linkTo(parent.top, itemVerticalPadding)
            start.linkTo(parent.start, contentHorizontalPadding)
            width = Dimension.fillToConstraints
        },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )


        Text(
            text = message.getFormattedTime(),
            Modifier.constrainAs(date) {
                linkTo(
                    start = from.end,
                    end = parent.end,
                    startMargin = 4.dp,
                    endMargin = contentHorizontalPadding,
                    bias = 1f,
                )
                top.linkTo(parent.top, itemVerticalPadding)
            })

        Icon(
            painterResource(
                id = if (isSynced) R.drawable.baseline_synced_24
                else R.drawable.baseline_un_synced_24),
            stringResource(
                id = if (isSynced) R.string.image_description_synced_logo
                else R.string.image_description_un_synced_logo),
            Modifier
                .padding(start = 8.dp)
                .size(16.dp)
                .constrainAs(status) {
                    end.linkTo(parent.end, contentHorizontalPadding)
                    top.linkTo(date.bottom, 4.dp)
                    bottom.linkTo(parent.bottom, itemVerticalPadding)
                }
        )

        Text(
            text = message.body, // + "\n" + message.toString(), // for debugging
            Modifier
                .constrainAs(body) {
                    top.linkTo(from.bottom)
                    linkTo(
                        start = parent.start,
                        end = status.start,
                        startMargin = contentHorizontalPadding,
                        endMargin = 8.dp,
                        bias = 0f,
                    )
                    bottom.linkTo(parent.bottom, itemVerticalPadding)
                    width = Dimension.fillToConstraints
                },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

data class Message(
    val threadId: String,
    val from: String,
    val body: String,
    val date: String,
    val type: String,
    val extra: String? = null
) {
    fun getFormattedTime(): String {
        val dateAsLong = date.toLongOrNull() ?: 0
        return dateFormat.format(dateAsLong)
    }

    fun isSentByUser(): Boolean {
        return type == "2"
    }
}