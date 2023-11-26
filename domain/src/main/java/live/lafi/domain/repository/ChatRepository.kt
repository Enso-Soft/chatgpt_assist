package live.lafi.domain.repository

import kotlinx.coroutines.flow.Flow
import live.lafi.domain.model.chat.ChatRoomInfo
import live.lafi.domain.model.chat.ChatRoomSystemRoleInfo

interface ChatRepository {
    suspend fun insertChatRoom(
        title: String,
        profileUri: String?,
    ): Long

    suspend fun getAllChatRoom(): Flow<List<ChatRoomInfo>>

    suspend fun getChatRoom(chatRoomSrl: Long): ChatRoomInfo

    suspend fun deleteChatRoom(chatRoomSrl: Long)

    suspend fun getChatRoomSystemRole(chatRoomSrl: Long): Flow<List<ChatRoomSystemRoleInfo>>

    suspend fun insertChatRoomSystemRole(
        chatRoomSrl: Long,
        roleContent: String
    )

    suspend fun insertChatRoomSystemRoleList(
        list: List<ChatRoomSystemRoleInfo>
    )

    suspend fun updateChatRoomSystemRole(
        chatRoomSystemRoleInfo: ChatRoomSystemRoleInfo
    )

    suspend fun updateChatRoomSystemRoleList(
        chatRoomSystemRoleInfoList: List<ChatRoomSystemRoleInfo>
    )
}