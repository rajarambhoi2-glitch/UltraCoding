package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY lastModified DESC")
    fun getAllProjectsFlow(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: String): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProject(id: String)
}

@Dao
interface FileDao {
    @Query("SELECT * FROM files WHERE projectId = :projectId ORDER BY path ASC")
    fun getFilesForProjectFlow(projectId: String): Flow<List<FileEntity>>

    @Query("SELECT * FROM files WHERE projectId = :projectId ORDER BY path ASC")
    suspend fun getFilesForProject(projectId: String): List<FileEntity>

    @Query("SELECT * FROM files WHERE id = :id LIMIT 1")
    suspend fun getFileById(id: String): FileEntity?

    @Query("SELECT * FROM files WHERE projectId = :projectId AND path = :path LIMIT 1")
    suspend fun getFileByPath(projectId: String, path: String): FileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(file: FileEntity)

    @Update
    suspend fun update(file: FileEntity)

    @Query("DELETE FROM files WHERE id = :id")
    suspend fun deleteFile(id: String)

    @Query("DELETE FROM files WHERE projectId = :projectId")
    suspend fun deleteFilesForProject(projectId: String)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE projectId = :projectId ORDER BY timestamp ASC")
    fun getMessagesForProjectFlow(projectId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE projectId = :projectId")
    suspend fun clearHistory(projectId: String)
}

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: AppSettingsEntity)
}
