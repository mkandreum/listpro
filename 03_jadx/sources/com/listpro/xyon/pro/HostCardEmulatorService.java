package com.listpro.xyon.pro;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import java.util.Locale;
import kotlin.Metadata;
import kotlin.UByte;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: HostCardEmulatorService.kt */
@Metadata(d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0012\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u0000 \r2\u00020\u0001:\u0001\rB\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u0010\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H\u0016J\u001a\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\t2\b\u0010\u000b\u001a\u0004\u0018\u00010\fH\u0016¨\u0006\u000e"}, d2 = {"Lcom/listpro/xyon/pro/HostCardEmulatorService;", "Landroid/nfc/cardemulation/HostApduService;", "<init>", "()V", "onDeactivated", "", "reason", "", "processCommandApdu", "", "commandApdu", "extras", "Landroid/os/Bundle;", "Companion", "app_release"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes.dex */
public final class HostCardEmulatorService extends HostApduService {
    private static final String AID;

    /* renamed from: Companion, reason: from kotlin metadata */
    public static final Companion INSTANCE;
    private static final String SELECT_APDU_HEADER;
    private static final byte[] SELECT_OK_SW;
    private static final byte[] UNKNOWN_CMD_SW;
    private static String groupData;

    /* compiled from: HostCardEmulatorService.kt */
    @Metadata(d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0007\n\u0002\u0010\u0012\n\u0002\b\u0006\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\u000f\u001a\u00020\r2\u0006\u0010\u0010\u001a\u00020\u0005J\u000e\u0010\u0011\u001a\u00020\u00052\u0006\u0010\u0012\u001a\u00020\rR\u001c\u0010\u0004\u001a\u0004\u0018\u00010\u0005X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\tR\u000e\u0010\n\u001a\u00020\u0005X\u0082D¢\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0005X\u0082D¢\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\rX\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\u0013"}, d2 = {"Lcom/listpro/xyon/pro/HostCardEmulatorService$Companion;", "", "<init>", "()V", "groupData", "", "getGroupData", "()Ljava/lang/String;", "setGroupData", "(Ljava/lang/String;)V", "AID", "SELECT_APDU_HEADER", "SELECT_OK_SW", "", "UNKNOWN_CMD_SW", "hexStringToByteArray", "s", "byteArrayToHexString", "bytes", "app_release"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }

        public final String getGroupData() {
            return HostCardEmulatorService.groupData;
        }

        public final void setGroupData(String str) {
            HostCardEmulatorService.groupData = str;
        }

        public final byte[] hexStringToByteArray(String s) {
            Intrinsics.checkNotNullParameter(s, "s");
            int length = s.length();
            byte[] bArr = new byte[length / 2];
            for (int i = 0; i < length; i += 2) {
                bArr[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
            }
            return bArr;
        }

        public final String byteArrayToHexString(byte[] bytes) {
            Intrinsics.checkNotNullParameter(bytes, "bytes");
            char[] cArr = new char[bytes.length * 2];
            int length = bytes.length;
            for (int i = 0; i < length; i++) {
                byte b = bytes[i];
                int i2 = i * 2;
                cArr[i2] = "0123456789ABCDEF".charAt((b & UByte.MAX_VALUE) >>> 4);
                cArr[i2 + 1] = "0123456789ABCDEF".charAt(b & 15);
            }
            return new String(cArr);
        }
    }

    static {
        Companion companion = new Companion(null);
        INSTANCE = companion;
        AID = "F0010203040506";
        SELECT_APDU_HEADER = "00A40400";
        SELECT_OK_SW = companion.hexStringToByteArray("9000");
        UNKNOWN_CMD_SW = companion.hexStringToByteArray("0000");
    }

    @Override // android.nfc.cardemulation.HostApduService
    public void onDeactivated(int reason) {
        groupData = null;
    }

    @Override // android.nfc.cardemulation.HostApduService
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        String str;
        Intrinsics.checkNotNullParameter(commandApdu, "commandApdu");
        String byteArrayToHexString = INSTANCE.byteArrayToHexString(commandApdu);
        String str2 = SELECT_APDU_HEADER;
        String substring = byteArrayToHexString.substring(0, str2.length());
        Intrinsics.checkNotNullExpressionValue(substring, "substring(...)");
        if (Intrinsics.areEqual(substring, str2)) {
            String str3 = AID;
            String substring2 = byteArrayToHexString.substring(10, str3.length() + 10);
            Intrinsics.checkNotNullExpressionValue(substring2, "substring(...)");
            Locale ROOT = Locale.ROOT;
            Intrinsics.checkNotNullExpressionValue(ROOT, "ROOT");
            String upperCase = substring2.toUpperCase(ROOT);
            Intrinsics.checkNotNullExpressionValue(upperCase, "toUpperCase(...)");
            if (Intrinsics.areEqual(upperCase, str3) && (str = groupData) != null) {
                byte[] byteArray = new NdefMessage(new NdefRecord[]{NdefRecord.createTextRecord("en", str)}).toByteArray();
                byte[] bArr = new byte[byteArray.length + 4];
                bArr[0] = (byte) (byteArray.length / 256);
                bArr[1] = (byte) (byteArray.length % 256);
                System.arraycopy(byteArray, 0, bArr, 2, byteArray.length);
                bArr[byteArray.length + 2] = -112;
                bArr[byteArray.length + 3] = 0;
                return bArr;
            }
        }
        return UNKNOWN_CMD_SW;
    }
}
