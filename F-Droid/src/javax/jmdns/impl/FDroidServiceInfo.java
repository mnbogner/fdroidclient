package javax.jmdns.impl;

import android.os.Parcel;
import android.os.Parcelable;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;

import javax.jmdns.ServiceInfo;

/**
 * The ServiceInfo class needs to be serialized in order to be sent as an Android broadcast.
 * In order to make it Parcelable (or Serializable for that matter), there are some package-scope
 * methods which needed to be used. Thus, this class is in the javax.jmdns.impl package so that
 * it can access those methods. This is as an alternative to modifying the source code of JmDNS.
 */
public class FDroidServiceInfo extends ServiceInfoImpl implements Parcelable {

    public FDroidServiceInfo(ServiceInfo info) {
        super(info);
    }

    public String getFingerprint() {
        return getPropertyString("fingerprint");
    }

    public String getRepoAddress() {
        return getURL(); // Automatically appends the "path" property if present, so no need to do it ourselves.
    }

    private static byte[] readBytes(Parcel in) {
        byte[] bytes = new byte[in.readInt()];
        in.readByteArray(bytes);
        return bytes;
    }

    public FDroidServiceInfo(Parcel in) {
        super(
            in.readString(),
            in.readString(),
            in.readString(),
            in.readInt(),
            in.readInt(),
            in.readInt(),
            in.readByte() != 0,
            readBytes(in)
        );

        int addressCount = in.readInt();
        for (int i = 0; i < addressCount; i ++) {
            try {
                addAddress((Inet4Address)Inet4Address.getByAddress(readBytes(in)));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        addressCount = in.readInt();
        for (int i = 0; i < addressCount; i ++) {
            try {
                addAddress((Inet6Address)Inet6Address.getByAddress(readBytes(in)));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getType());
        dest.writeString(getName());
        dest.writeString(getSubtype());
        dest.writeInt(getPort());
        dest.writeInt(getWeight());
        dest.writeInt(getPriority());
        dest.writeByte(isPersistent() ? (byte) 1 : (byte) 0);
        dest.writeInt(getTextBytes().length);
        dest.writeByteArray(getTextBytes());
        dest.writeInt(getInet4Addresses().length);
        for (int i = 0; i < getInet4Addresses().length; i ++) {
            Inet4Address address = getInet4Addresses()[i];
            dest.writeInt(address.getAddress().length);
            dest.writeByteArray(address.getAddress());
        }
        dest.writeInt(getInet6Addresses().length);
        for (int i = 0; i < getInet6Addresses().length; i ++) {
            Inet6Address address = getInet6Addresses()[i];
            dest.writeInt(address.getAddress().length);
            dest.writeByteArray(address.getAddress());
        }
    }

    public static final Parcelable.Creator<FDroidServiceInfo> CREATOR = new Parcelable.Creator<FDroidServiceInfo>() {
        public FDroidServiceInfo createFromParcel(Parcel source) {
            return new FDroidServiceInfo(source);
        }

        public FDroidServiceInfo[] newArray(int size) {
            return new FDroidServiceInfo[size];
        }
    };
}