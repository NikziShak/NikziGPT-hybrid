package com.nikzigpt.repository

import android.content.Context
import androidx.room.*
import com.nikzigpt.data.AIModel
import com.nikzigpt.data.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val role: String,
    val content: String,
    val timestamp: Long,
    val isStreaming: Boolean,
    val modelId: String?,
    val sessionId: String
)

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val modelId: String?,
    val messageCount: Int
)

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>)
    
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessages(sessionId: String): Flow<List<ChatMessageEntity>>
    
    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessages(sessionId: String)
    
    @Query("DELETE FROM chat_messages")
    suspend fun deleteAllMessages()
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSessionEntity)
    
    @Query("SELECT * FROM chat_sessions ORDER BY updatedAt DESC")
    fun getAllSessions(): Flow<List<ChatSessionEntity>>
    
    @Query("SELECT * FROM chat_sessions WHERE id = :sessionId")
    suspend fun getSession(sessionId: String): ChatSessionEntity?
    
    @Query("UPDATE chat_sessions SET updatedAt = :updatedAt, messageCount = :messageCount WHERE id = :sessionId")
    suspend fun updateSession(sessionId: String, updatedAt: Long, messageCount: Int)
    
    @Query("UPDATE chat_sessions SET title = :title WHERE id = :sessionId")
    suspend fun updateSessionTitle(sessionId: String, title: String)
    
    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: String)
}

@Entity(tableName = "cached_models")
data class CachedModelEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val provider: String,
    val contextLength: Int,
    val pricingPrompt: String,
    val pricingCompletion: String,
    val pricingCurrency: String,
    val isFree: Boolean,
    val capabilities: String, // JSON string
    val architectureModality: String?,
    val architectureTokenizer: String?,
    val architectureInstructType: String?,
    val cachedAt: Long
)

@Dao
interface ModelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModels(models: List<CachedModelEntity>)
    
    @Query("SELECT * FROM cached_models WHERE isFree = 1 ORDER BY provider, name")
    fun getFreeModels(): Flow<List<CachedModelEntity>>
    
    @Query("SELECT * FROM cached_models ORDER BY provider, name")
    fun getAllModels(): Flow<List<CachedModelEntity>>
    
    @Query("DELETE FROM cached_models")
    suspend fun clearModels()
}

@Database(entities = [ChatMessageEntity::class, ChatSessionEntity::class, CachedModelEntity::class], version = 1, exportSchema = false)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun modelDao(): ModelDao
    
    companion object {
        @Volatile private var INSTANCE: ChatDatabase? = null
        
        fun getDatabase(context: Context): ChatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChatDatabase::class.java,
                    "nikzigpt_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class ChatRepository(private val context: Context) {
    private val database = ChatDatabase.getDatabase(context)
    private val chatDao = database.chatDao()
    private val modelDao = database.modelDao()
    private val json = Json { ignoreUnknownKeys = true }
    
    // Current session ID
    private var currentSessionId: String = java.util.UUID.randomUUID().toString()
    
    fun getCurrentSessionId(): String = currentSessionId
    
    fun newSession() {
        currentSessionId = java.util.UUID.randomUUID().toString()
    }
    
    fun setSessionId(sessionId: String) {
        currentSessionId = sessionId
    }
    
    // Messages
    fun getMessages(): Flow<List<ChatMessage>> = chatDao.getMessages(currentSessionId)
        .map { entities ->
            entities.map { entity ->
                ChatMessage(
                    role = entity.role,
                    content = entity.content,
                    timestamp = entity.timestamp,
                    id = entity.id,
                    isStreaming = entity.isStreaming,
                    modelId = entity.modelId
                )
            }
        }
    
    suspend fun addMessage(message: ChatMessage) {
        val entity = ChatMessageEntity(
            id = message.id,
            role = message.role,
            content = message.content,
            timestamp = message.timestamp,
            isStreaming = message.isStreaming,
            modelId = message.modelId,
            sessionId = currentSessionId
        )
        chatDao.insertMessage(entity)
        updateSessionMessageCount()
    }
    
    suspend fun updateMessage(message: ChatMessage) {
        val entity = ChatMessageEntity(
            id = message.id,
            role = message.role,
            content = message.content,
            timestamp = message.timestamp,
            isStreaming = message.isStreaming,
            modelId = message.modelId,
            sessionId = currentSessionId
        )
        chatDao.insertMessage(entity)
    }
    
    suspend fun deleteMessage(messageId: String) {
        // We'd need a specific query for this, for now just rebuild
    }
    
    suspend fun clearCurrentSession() {
        chatDao.deleteMessages(currentSessionId)
    }
    
    // Sessions
    fun getAllSessions(): Flow<List<ChatSession>> = chatDao.getAllSessions()
        .map { entities ->
            entities.map { entity ->
                ChatSession(
                    id = entity.id,
                    title = entity.title,
                    createdAt = entity.createdAt,
                    updatedAt = entity.updatedAt,
                    modelId = entity.modelId,
                    messageCount = entity.messageCount
                )
            }
        }
    
    suspend fun createSession(title: String = "New Chat", modelId: String? = null): ChatSession {
        val now = System.currentTimeMillis()
        val session = ChatSessionEntity(
            id = currentSessionId,
            title = title,
            createdAt = now,
            updatedAt = now,
            modelId = modelId,
            messageCount = 0
        )
        chatDao.insertSession(session)
        return ChatSession(
            id = session.id,
            title = session.title,
            createdAt = session.createdAt,
            updatedAt = session.updatedAt,
            modelId = session.modelId,
            messageCount = session.messageCount
        )
    }
    
    suspend fun updateSessionTitle(title: String) {
        chatDao.updateSessionTitle(currentSessionId, title)
    }
    
    private suspend fun updateSessionMessageCount() {
        val count = chatDao.getMessages(currentSessionId).first().size
        chatDao.updateSession(currentSessionId, System.currentTimeMillis(), count)
    }
    
    suspend fun deleteSession(sessionId: String) {
        chatDao.deleteSession(sessionId)
        chatDao.deleteMessages(sessionId)
        if (sessionId == currentSessionId) {
            newSession()
        }
    }
    
    // Models
    fun getFreeModels(): Flow<List<AIModel>> = modelDao.getFreeModels()
        .map { entities ->
            entities.map { entity ->
                AIModel(
                    id = entity.id,
                    name = entity.name,
                    description = entity.description,
                    provider = entity.provider,
                    contextLength = entity.contextLength,
                    pricing = com.nikzigpt.data.ModelPricing(
                        prompt = entity.pricingPrompt,
                        completion = entity.pricingCompletion,
                        currency = entity.pricingCurrency
                    ),
                    isFree = entity.isFree,
                    capabilities = json.decodeFromString(entity.capabilities),
                    architecture = if (entity.architectureModality != null) {
                        com.nikzigpt.data.ModelArchitecture(
                            modality = entity.architectureModality!!,
                            tokenizer = entity.architectureTokenizer!!,
                            instructType = entity.architectureInstructType
                        )
                    } else null
                )
            }
        }
    
    fun getAllModels(): Flow<List<AIModel>> = modelDao.getAllModels()
        .map { entities ->
            entities.map { entity ->
                AIModel(
                    id = entity.id,
                    name = entity.name,
                    description = entity.description,
                    provider = entity.provider,
                    contextLength = entity.contextLength,
                    pricing = com.nikzigpt.data.ModelPricing(
                        prompt = entity.pricingPrompt,
                        completion = entity.pricingCompletion,
                        currency = entity.pricingCurrency
                    ),
                    isFree = entity.isFree,
                    capabilities = json.decodeFromString(entity.capabilities),
                    architecture = if (entity.architectureModality != null) {
                        com.nikzigpt.data.ModelArchitecture(
                            modality = entity.architectureModality!!,
                            tokenizer = entity.architectureTokenizer!!,
                            instructType = entity.architectureInstructType
                        )
                    } else null
                )
            }
        }
    
    suspend fun cacheModels(models: List<AIModel>) {
        val entities = models.map { model ->
            CachedModelEntity(
                id = model.id,
                name = model.name,
                description = model.description,
                provider = model.provider,
                contextLength = model.contextLength,
                pricingPrompt = model.pricing.prompt,
                pricingCompletion = model.pricing.completion,
                pricingCurrency = model.pricing.currency,
                isFree = model.isFree,
                capabilities = json.encodeToString(model.capabilities),
                architectureModality = model.architecture?.modality,
                architectureTokenizer = model.architecture?.tokenizer,
                architectureInstructType = model.architecture?.instructType,
                cachedAt = System.currentTimeMillis()
            )
        }
        modelDao.insertModels(entities)
    }
    
    suspend fun clearModelCache() {
        modelDao.clearModels()
    }
}

data class ChatSession(
    val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val modelId: String?,
    val messageCount: Int
)