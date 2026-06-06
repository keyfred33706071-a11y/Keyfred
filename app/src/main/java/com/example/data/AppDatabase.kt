package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "completed_projects")
data class CompletedProjectEntity(
    @PrimaryKey val id: String,
    val completedAt: Long
)

@Entity(tableName = "community_projects")
data class CommunityProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String,
    val materials: String,
    val steps: String,
    val author: String,
    val timestamp: Long,
    val likes: Int = 0
)

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val username: String = "Estudiante",
    val bio: String = "Estudiante - Laboratorio EcoReEngine",
    val avatarIndex: Int = 1,
    val extraPoints: Int = 0,
    val gameCompletedCount: Int = 0
)

@Dao
interface ProjectDao {
    @Query("SELECT * FROM completed_projects")
    fun getCompletedProjectsStream(): Flow<List<CompletedProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markProjectAsCompleted(project: CompletedProjectEntity)

    @Query("DELETE FROM completed_projects WHERE id = :id")
    suspend fun removeCompletedProject(id: String)

    @Query("SELECT * FROM community_projects ORDER BY timestamp DESC")
    fun getCommunityProjectsStream(): Flow<List<CommunityProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommunityProject(project: CommunityProjectEntity)

    @Query("UPDATE community_projects SET likes = likes + 1 WHERE id = :id")
    suspend fun likeCommunityProject(id: Int)

    @Query("SELECT * FROM user_settings WHERE id = 1")
    fun getUserSettingsStream(): Flow<UserSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserSettings(settings: UserSettingsEntity)
}

@Database(entities = [CompletedProjectEntity::class, CommunityProjectEntity::class, UserSettingsEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract val projectDao: ProjectDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ecoreengine_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class ProjectRepository(private val dao: ProjectDao) {
    val completedProjects: Flow<List<CompletedProjectEntity>> = dao.getCompletedProjectsStream()
    val communityProjects: Flow<List<CommunityProjectEntity>> = dao.getCommunityProjectsStream()
    val userSettings: Flow<UserSettingsEntity?> = dao.getUserSettingsStream()

    suspend fun completeProject(projectId: String) {
        dao.markProjectAsCompleted(CompletedProjectEntity(projectId, System.currentTimeMillis()))
    }

    suspend fun uncompleteProject(projectId: String) {
        dao.removeCompletedProject(projectId)
    }

    suspend fun addCommunityProject(project: CommunityProjectEntity) {
        dao.insertCommunityProject(project)
    }

    suspend fun likeCommunityProject(id: Int) {
        dao.likeCommunityProject(id)
    }

    suspend fun saveUserSettings(settings: UserSettingsEntity) {
        dao.saveUserSettings(settings)
    }
}
