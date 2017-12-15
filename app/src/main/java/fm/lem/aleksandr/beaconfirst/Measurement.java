package fm.lem.aleksandr.beaconfirst;


/**
 * Created by AlekSandR on 03.12.2017.
 */

public class Measurement {
    private String beaconId;
    private double rssi;

    public Measurement(String beaconId, double rssi) {
        this.beaconId = beaconId;
        this.rssi = rssi;
    }

    public String getBeaconId() {
        return beaconId;
    }

    public void setBeaconId(String beaconId) {
        this.beaconId = beaconId;
    }

    public double getRssi() {
        return rssi;
    }

    public void setRssi(double rssi) {
        this.rssi = rssi;
    }
}
