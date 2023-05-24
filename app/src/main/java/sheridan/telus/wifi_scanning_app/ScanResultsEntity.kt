package sheridan.telus.wifi_scanning_app

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scanResults")
data class ScanResultsEntity(

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    @ColumnInfo(name = "buildingName")
    var buildingName: String? = null,

    @ColumnInfo(name = "floorNumber")
    var floorNumber: String? = null,

    @ColumnInfo(name = "SSID")
    var ssid: String? = null,

    @ColumnInfo(name = "CAPABILITIES")
    var capabilities: String? = null,

    @ColumnInfo(name = "BSSID")
    var bssid: String ? = null,

    @ColumnInfo(name = "signalStrength")
    var signalStrength: Int? = null

)