package sheridan.telus.wifi_scanning_app

import androidx.room.*

@Dao
interface ScanResultsDao {

    @Query("SELECT * FROM scanResults")
    fun getAll(): List<ScanResultsEntity>

    @Delete
    fun delete(scanResults: ScanResultsEntity)

}