package live.lafi.presentation.chat_room

import android.animation.Animator
import android.content.Intent
import android.graphics.Rect
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import live.lafi.domain.model.chat.ChatContentInfo
import live.lafi.presentation.R
import live.lafi.presentation.databinding.ActivityChatRoomBinding
import live.lafi.util.base.BaseActivity
import live.lafi.util.ext.hideKeyboard
import live.lafi.util.public_model.ContentManager
import live.lafi.util.service.ChatContentService
import timber.log.Timber
import java.util.LinkedList
import java.util.Queue

@AndroidEntryPoint
class ChatRoomActivity : BaseActivity<ActivityChatRoomBinding>(R.layout.activity_chat_room) {
    companion object {
        const val CHAT_ROOM_SRL = "chat_room_srl"
        const val CHAT_ROOM_TITLE = "chat_room_title"
    }

    private val viewModel: ChatRoomViewModel by viewModels()

    private val chatContentAdapter by lazy { ChatContentListAdapter() }

    private val chatRoomSrl by lazy { intent.getLongExtra(CHAT_ROOM_SRL, 0L) }
    private val chatRoomTitle by lazy { intent.getStringExtra(CHAT_ROOM_TITLE) ?: "" }

    private var chatContentEndScrollFlag = false
    private var chatContentScrollTimeTick = System.currentTimeMillis()

    // 현재 스크롤 관련 변수
    private var chatContentScrollTopPercent: Double? = null
    private var chatContentScrollOnePagePercent: Double? = null

    private val touchQueue: Queue<Int> = LinkedList()

    private var isLottieAnimatorRunning = false

    override fun setupUi() {
        if (chatRoomSrl == 0L) {
            showToast("잘못 된 접근입니다.")
            finish()
        }

        with(binding) {
            tvChatRoomTitle.text = chatRoomTitle

            rvChatContent.apply {
                adapter = chatContentAdapter
                layoutManager = LinearLayoutManager(this@ChatRoomActivity).apply {
                    orientation = LinearLayoutManager.VERTICAL
                    reverseLayout = false
                    stackFromEnd = true
                    setHasFixedSize(true)
                }
                itemAnimator = null
            }

            etTextMessage.requestFocus()
        }
    }

    override fun subscribeUi() {
        with(viewModel) {
            scopeIO.launch {
                getAllChatContentWithChatRoomSrl(chatRoomSrl = chatRoomSrl).collectLatest { chatContentList ->
                    val mappedPairs = mutableListOf<ChatContentInfo>()
                    chatContentList.forEach { chatContent ->
                        if (chatContent.parentChatContentSrl == 0L) { // 질문인 경우
                            chatContentList.find { it.parentChatContentSrl == chatContent.chatContentSrl }?.let {
                                mappedPairs.add(chatContent) // 질문 추가
                                mappedPairs.add(it) // 해당 답변 추가
                            } ?: run {
                                mappedPairs.add(chatContent) // 질문 추가
                                if (chatContent.status == "wait" || chatContent.status == "request") {
                                    mappedPairs.add(
                                        ChatContentInfo(
                                            chatContentSrl = 0L,
                                            chatRoomSrl = chatContent.chatRoomSrl,
                                            parentChatContentSrl = chatContent.chatContentSrl,
                                            role = "assistant",
                                            content = "",
                                            contentSummary = "",
                                            contentTranslate = "",
                                            useToken = 0,
                                            status = "loading",
                                            updateDate = chatContent.updateDate,
                                            createDate = chatContent.createDate
                                        )
                                    ) // 로딩중.. 추가
                                }
                            }
                        }
                    }

                    val chatContentItemList = mappedPairs.map { chatContent ->
                        val viewType = if (chatContent.status == "loading") {
                            ChatContentItem.ViewType.CHAT_CONTENT_OTHER_LOADING
                        } else if (chatContent.role == "user") {
                            ChatContentItem.ViewType.CHAT_CONTENT_MY_TEXT
                        } else {
                            ChatContentItem.ViewType.CHAT_CONTENT_OTHER_TEXT
                        }

                        ChatContentItem(
                            viewType = viewType,
                            chatContentSrl = chatContent.chatContentSrl,
                            content = chatContent.content,
                            profileUri = "",
                            nickname = chatRoomTitle,
                            createDate = chatContent.createDate
                        )
                    }

                    withContext(Dispatchers.Main) {
                        chatContentAdapter.submitList(chatContentItemList) {
                            var isScrollBottom = false
                            if (chatContentEndScrollFlag) {
                                isScrollBottom = true
                            }

                            if (!isScrollBottom && chatContentScrollTopPercent != null && chatContentScrollOnePagePercent != null) {
                                val bottomScrollPercent = chatContentScrollTopPercent!! + chatContentScrollOnePagePercent!!
                                if ((100-bottomScrollPercent)*2 < chatContentScrollOnePagePercent!!) {
                                    isScrollBottom = true
                                } else {
                                    // 새로운 메세지가 왔는디..? 스크롤은 위에 있네..
                                }
                            }

                            if (binding.rvChatContent.adapter != null && binding.rvChatContent.adapter!!.itemCount > 0) {
                                binding.rvChatContent.post {
                                    if (isScrollBottom) {
                                        chatContentEndScrollFlag = false
                                        binding.rvChatContent.scrollToPosition(
                                            binding.rvChatContent.adapter!!.itemCount - 1
                                        )
                                    }
                                }
                            }
                        }
                        //binding.rvChatContent.scrollToPosition(chatContentItemList.size - 1)
                    }
                }
        }   }
    }

    override fun initListener() {
        with(binding) {
            flBackButton.setOnClickListener { finish() }
            flSendButton.setOnClickListener { sendMessage(etTextMessage.text.toString()) }

            etTextMessage.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun afterTextChanged(s: Editable?) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s.isNullOrBlank()) {
                        flSendButton.visibility = View.GONE
                    } else {
                        flSendButton.visibility = View.VISIBLE
                    }
                }
            })

            ltLogo.addAnimatorListener(object : Animator.AnimatorListener{
                override fun onAnimationStart(animation: Animator) {
                    // 애니메이션 시작 될 때
                    isLottieAnimatorRunning = true
                }

                override fun onAnimationEnd(animation: Animator) {
                    // 애니메이션 종료 될 때
                    isLottieAnimatorRunning = false
                }

                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) { }
            })

            rvChatContent.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    chatContentScrollTimeTick = System.currentTimeMillis()

                    chatContentScrollTopPercent = (recyclerView.computeVerticalScrollOffset().toDouble() / recyclerView.computeVerticalScrollRange().toDouble()) * 100
                    chatContentScrollOnePagePercent = (recyclerView.computeVerticalScrollExtent().toDouble() / recyclerView.computeVerticalScrollRange().toDouble()) * 100
                    val bottomScrollPercent = chatContentScrollTopPercent!! + chatContentScrollOnePagePercent!!

                    Timber.tag("whk__").d("bottomScrollPercent : $bottomScrollPercent / (100-bottomScrollPercent)*2 : ${(100-bottomScrollPercent)*2} // mScrollOnePagePercent : $chatContentScrollOnePagePercent")
                }
            })
        }
    }

    override fun initData() {}

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            val view = currentFocus
            if (view is EditText) {
                if (event.action == MotionEvent.ACTION_UP) {
                    val outRect = Rect()
                    view.getGlobalVisibleRect(outRect)
                    if (event.rawY < outRect.top || event.rawY > outRect.bottom) {
                        Timber.tag("whk__").d("touchQueue.filter { it == MotionEvent.ACTION_MOVE }.size : ${touchQueue.filter { it == MotionEvent.ACTION_MOVE }.size}")
                        if (System.currentTimeMillis() - chatContentScrollTimeTick >= 300L && (touchQueue.filter { it == MotionEvent.ACTION_MOVE }.size) < 8) {
                            hideKeyboard()
                            view.clearFocus()
                        }
                    }

                    touchQueue.clear()
                } else {
                    if (touchQueue.size > 30) {
                        touchQueue.remove()
                    }
                    touchQueue.add(event.action)
                }
            }
        }

        return super.dispatchTouchEvent(event)
    }

    private fun sendMessage(message: String) {
        if (message.isNotBlank()) {
            chatContentEndScrollFlag = true
            viewModel.sendChatContent(
                chatRoomSrl = chatRoomSrl,
                content = message
            )
            binding.etTextMessage.text.clear()

            if (!isLottieAnimatorRunning) {
                binding.ltLogo.playAnimation()
            }

            if (!ContentManager.isContentServiceRunning()) {
                startService(
                    Intent(applicationContext, ChatContentService::class.java)
                )
            }
        }
    }
}