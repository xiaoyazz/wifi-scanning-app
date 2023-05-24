package sheridan.telus.wifi_scanning_app

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scanResults")
data class ScanResultsEntity(

    @PrimaryKey(autoGenerate = true)
    var id: Int,

    @ColumnInfo(name = "SSID")
    var ssid: String,

    @ColumnInfo(name = "CAPABILITIES")
    var capabilities: String,

    @ColumnInfo(name = "BSSID")
    var bssid: String,

    @ColumnInfo(name = "signalStrength")
    var signalStrength: String

)