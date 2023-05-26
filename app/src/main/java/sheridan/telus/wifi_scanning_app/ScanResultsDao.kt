package sheridan.telus.wifi_scanning_app

import androidx.room.*

@Dao
interface ScanResultsDao {

    @Query("SELECT * FROM scanResults WHERE buildingName = :buildingName AND floorNumber = :floorNumber")
    fun getResultsByBuildingAndFloor(buildingName: String, floorNumber: String): List<ScanResultsEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAllInternal(scanResults: List<ScanResultsEntity>)

    @Query("DELETE FROM scanResults")
    fun deleteAll()
    // Method to write data to a CSV file
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(scanResults: List<ScanResultsEntity>) {
        for (scanResult in scanResults) {
            val existingResults = getResultsByBuildingAndFloor(scanResult.buildingName?:"",
                scanResult.floorNumber?:""
            )
            if (existingResults.isNotEmpty()) {
                // Remove existing results on the same floor in the same building
                deleteAll()
            }
        }
        // Insert the new scan results
        insertAllInternal(scanResults)
    }
}