package org.wikipedia.talk

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import org.wikipedia.Constants
import org.wikipedia.R
import org.wikipedia.databinding.ItemTalkTopicBinding
import org.wikipedia.dataclient.discussiontools.ThreadItem
import org.wikipedia.page.PageTitle
import org.wikipedia.richtext.RichTextUtil
import org.wikipedia.util.DateUtil
import org.wikipedia.util.ResourceUtil
import org.wikipedia.util.StringUtil

class TalkTopicHolder internal constructor(
    private val binding: ItemTalkTopicBinding,
    private val context: Context,
    private val pageTitle: PageTitle,
    private val invokeSource: Constants.InvokeSource
) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {

    private val unreadTypeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    private var id: String = ""

    fun bindItem(threadItem: ThreadItem, searchQuery: String? = null) {
        id = threadItem.id
//        val seen = AppDatabase.instance.talkPageSeenDao().getTalkPageSeen(topic.getIndicatorSha()) != null
        // TODO: implement seen
        val seen = false

        var titleStr = RichTextUtil.stripHtml(threadItem.html).trim()
        if (titleStr.isEmpty()) {
            // build up a title based on the contents, massaging the html into plain text that
            // flows over a few lines...
            threadItem.replies.firstOrNull()?.let {
                titleStr = RichTextUtil.stripHtml(it.html).replace("\n", " ")
                if (titleStr.length > MAX_CHARS_NO_SUBJECT) {
                    titleStr = titleStr.substring(0, MAX_CHARS_NO_SUBJECT) + "…"
                }
            }
        }

        binding.topicTitleText.text = titleStr.ifEmpty { context.getString(R.string.talk_no_subject) }
        binding.topicTitleText.visibility = View.VISIBLE
        binding.topicTitleText.typeface = if (seen) Typeface.SANS_SERIF else unreadTypeface
        binding.topicTitleText.setTextColor(ResourceUtil.getThemedColor(context, if (seen) android.R.attr.textColorTertiary else R.attr.material_theme_primary_color))

        StringUtil.highlightAndBoldenText(binding.topicTitleText, searchQuery, true, Color.YELLOW)
        itemView.setOnClickListener(this)

        val allReplies = threadItem.allReplies

        if (allReplies.isEmpty()) {
            binding.topicBodyGroup.isVisible = false
            return
        }

        // Last comment
        binding.topicContentText.text = RichTextUtil.stripHtml(allReplies.last().html).trim()

        // Username with involved user number exclude the author
        val usersInvolved = allReplies.map { it.author }.distinct().size - 1
        val usernameText = allReplies.first().author + (if (usersInvolved > 1) " +${usersInvolved}" else "")
        binding.topicUsername.text = usernameText

        // Amount of replies, exclude the topic in replies[].
        binding.topicReplyNumber.text = (allReplies.size - 1).toString()

        // Last comment date
        val lastCommentDate = allReplies.maxByOrNull { it.timestamp }?.timestamp?.run { DateUtil.getDateAndTime(DateUtil.iso8601DateParse(this)) }
        binding.topicLastCommentDate.text = context.getString(R.string.talk_list_item_last_comment_date, lastCommentDate)
        binding.topicLastCommentDate.isVisible = !lastCommentDate.isNullOrEmpty()
    }

    override fun onClick(v: View?) {
        context.startActivity(TalkTopicActivity.newIntent(context, pageTitle, id, invokeSource))
    }

    companion object {
        private const val MAX_CHARS_NO_SUBJECT = 100
    }
}
