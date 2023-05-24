package sheridan.telus.wifi_scanning_app

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ScanResultsEntity::class], version = 1)
abstract class ScanResultsDatabase : RoomDatabase() {

    abstract fun scanResultsDao(): ScanResultsDao

    companion object {
        @Volatile
        private var INSTANCE: ScanResultsDatabase? = null

        fun getInstance(context: Context): ScanResultsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ScanResultsDatabase::class.java,
                    "scan_results_database"
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }


}