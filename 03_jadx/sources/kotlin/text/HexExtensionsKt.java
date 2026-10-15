package kotlin.text;

import androidx.constraintlayout.widget.ConstraintLayout;
import java.util.Arrays;
import kotlin.KotlinNothingValueException;
import kotlin.Metadata;
import kotlin.UByte;
import kotlin.ULong;
import kotlin.UShort;
import kotlin.collections.AbstractList;
import kotlin.collections.ArraysKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.RangesKt;
import kotlin.text.HexFormat;

/* compiled from: HexExtensions.kt */
@Metadata(d1 = {"\u0000v\n\u0000\n\u0002\u0010\u0015\n\u0002\b\u0007\n\u0002\u0010\u0016\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0013\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\b\n\u0002\u0010\u0012\n\u0002\b\u0002\n\u0002\u0010\u0019\n\u0002\b\u0004\n\u0002\u0010\u0005\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010\n\n\u0002\b\u0005\n\u0002\u0010\u0001\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0007\u001a \u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u000e2\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u0011H\u0002\u001a\u0010\u0010\u0013\u001a\u00020\u00112\u0006\u0010\u0014\u001a\u00020\u000eH\u0002\u001a(\u0010\u0015\u001a\u00020\u00112\u0006\u0010\u0016\u001a\u00020\u00112\u0006\u0010\u0017\u001a\u00020\u00112\u0006\u0010\u0018\u001a\u00020\u00112\u0006\u0010\u0019\u001a\u00020\u0011H\u0002\u001a@\u0010\u0015\u001a\u00020\u00112\u0006\u0010\u0016\u001a\u00020\u00112\u0006\u0010\u001a\u001a\u00020\u00112\u0006\u0010\u001b\u001a\u00020\u00112\u0006\u0010\u001c\u001a\u00020\u00112\u0006\u0010\u0017\u001a\u00020\u00112\u0006\u0010\u0018\u001a\u00020\u00112\u0006\u0010\u0019\u001a\u00020\u0011H\u0000\u001a@\u0010\u001d\u001a\u00020\u00112\u0006\u0010\u001e\u001a\u00020\u00112\u0006\u0010\u001a\u001a\u00020\u00112\u0006\u0010\u001b\u001a\u00020\u00112\u0006\u0010\u001c\u001a\u00020\u00112\u0006\u0010\u0017\u001a\u00020\u00112\u0006\u0010\u0018\u001a\u00020\u00112\u0006\u0010\u0019\u001a\u00020\u0011H\u0000\u001a \u0010\u001f\u001a\u00020\u000e2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u000e2\u0006\u0010\u0012\u001a\u00020\u0011H\u0002\u001a5\u0010 \u001a\u00020\u0011*\u00020\u000b2\u0006\u0010!\u001a\u00020\u00112\u0006\u0010\"\u001a\u00020\u00112\u0006\u0010#\u001a\u00020\u000b2\u0006\u0010$\u001a\u00020%2\u0006\u0010&\u001a\u00020\u000bH\u0082\b\u001a\u001c\u0010'\u001a\u00020\u0011*\u00020\u000b2\u0006\u0010!\u001a\u00020\u00112\u0006\u0010\"\u001a\u00020\u0011H\u0002\u001a$\u0010(\u001a\u00020)*\u00020\u000b2\u0006\u0010*\u001a\u00020\u00112\u0006\u0010\"\u001a\u00020\u00112\u0006\u0010+\u001a\u00020\u0011H\u0002\u001a<\u0010,\u001a\u00020)*\u00020\u000b2\u0006\u0010*\u001a\u00020\u00112\u0006\u0010\"\u001a\u00020\u00112\u0006\u0010-\u001a\u00020\u000b2\u0006\u0010.\u001a\u00020\u000b2\u0006\u0010$\u001a\u00020%2\u0006\u0010+\u001a\u00020\u0011H\u0002\u001a\u001c\u0010/\u001a\u00020)*\u00020\u000b2\u0006\u0010*\u001a\u00020\u00112\u0006\u0010\"\u001a\u00020\u0011H\u0002\u001a\u0015\u00100\u001a\u00020\u0011*\u00020\u000b2\u0006\u0010!\u001a\u00020\u0011H\u0082\b\u001a,\u00101\u001a\u00020\u0011*\u0002022\u0006\u0010!\u001a\u00020\u00112\u0006\u00103\u001a\u00020\u00012\u0006\u00104\u001a\u0002052\u0006\u00106\u001a\u00020\u0011H\u0002\u001a<\u00101\u001a\u00020\u0011*\u0002022\u0006\u0010!\u001a\u00020\u00112\u0006\u00107\u001a\u00020\u000b2\u0006\u00108\u001a\u00020\u000b2\u0006\u00103\u001a\u00020\u00012\u0006\u00104\u001a\u0002052\u0006\u00106\u001a\u00020\u0011H\u0002\u001a*\u00109\u001a\u00020:*\u00020\u000b2\b\b\u0002\u0010*\u001a\u00020\u00112\b\b\u0002\u0010\"\u001a\u00020\u00112\b\b\u0002\u0010;\u001a\u00020<H\u0003\u001a\u0016\u00109\u001a\u00020:*\u00020\u000b2\b\b\u0002\u0010;\u001a\u00020<H\u0007\u001a*\u0010=\u001a\u000202*\u00020\u000b2\b\b\u0002\u0010*\u001a\u00020\u00112\b\b\u0002\u0010\"\u001a\u00020\u00112\b\b\u0002\u0010;\u001a\u00020<H\u0003\u001a\u0016\u0010=\u001a\u000202*\u00020\u000b2\b\b\u0002\u0010;\u001a\u00020<H\u0007\u001a&\u0010>\u001a\u0004\u0018\u000102*\u00020\u000b2\u0006\u0010*\u001a\u00020\u00112\u0006\u0010\"\u001a\u00020\u00112\u0006\u0010?\u001a\u00020@H\u0003\u001a&\u0010A\u001a\u0004\u0018\u000102*\u00020\u000b2\u0006\u0010*\u001a\u00020\u00112\u0006\u0010\"\u001a\u00020\u00112\u0006\u0010?\u001a\u00020@H\u0003\u001a&\u0010B\u001a\u0004\u0018\u000102*\u00020\u000b2\u0006\u0010*\u001a\u00020\u00112\u0006\u0010\"\u001a\u00020\u00112\u0006\u0010?\u001a\u00020@H\u0003\u001a$\u0010C\u001a\u000202*\u00020\u000b2\u0006\u0010*\u001a\u00020\u00112\u0006\u0010\"\u001a\u00020\u00112\u0006\u0010?\u001a\u00020@H\u0003\u001a*\u0010D\u001a\u00020\u0011*\u00020\u000b2\b\b\u0002\u0010*\u001a\u00020\u00112\b\b\u0002\u0010\"\u001a\u00020\u00112\b\b\u0002\u0010;\u001a\u00020<H\u0003\u001a\u0016\u0010D\u001a\u00020\u0011*\u00020\u000b2\b\b\u0002\u0010;\u001a\u00020<H\u0007\u001a,\u0010E\u001a\u00020\u0011*\u00020\u000b2\u0006\u0010*\u001a\u00020\u00112\u0006\u0010\"\u001a\u00020\u00112\u0006\u0010;\u001a\u00020<2\u0006\u0010+\u001a\u00020\u0011H\u0003\u001a*\u0010F\u001a\u00020\u000e*\u00020\u000b2\b\b\u0002\u0010*\u001a\u00020\u00112\b\b\u0002\u0010\"\u001a\u00020\u00112\b\b\u0002\u0010;\u001a\u00020<H\u0001\u001a\u0016\u0010F\u001a\u00020\u000e*\u00020\u000b2\b\b\u0002\u0010;\u001a\u00020<H\u0007\u001a,\u0010G\u001a\u00020\u000e*\u00020\u000b2\u0006\u0010*\u001a\u00020\u00112\u0006\u0010\"\u001a\u00020\u00112\u0006\u0010;\u001a\u00020<2\u0006\u0010+\u001a\u00020\u0011H\u0003\u001a*\u0010H\u001a\u00020I*\u00020\u000b2\b\b\u0002\u0010*\u001a\u00020\u00112\b\b\u0002\u0010\"\u001a\u00020\u00112\b\b\u0002\u0010;\u001a\u00020<H\u0003\u001a\u0016\u0010H\u001a\u00020I*\u00020\u000b2\b\b\u0002\u0010;\u001a\u00020<H\u0007\u001a\u0015\u0010J\u001a\u00020\u000e*\u00020\u000b2\u0006\u0010!\u001a\u00020\u0011H\u0082\b\u001a\u0014\u0010K\u001a\u00020:*\u00020\u000b2\u0006\u0010!\u001a\u00020\u0011H\u0002\u001a\u001c\u0010L\u001a\u00020\u0011*\u00020\u000b2\u0006\u0010*\u001a\u00020\u00112\u0006\u0010\"\u001a\u00020\u0011H\u0002\u001a\u001c\u0010M\u001a\u00020\u000e*\u00020\u000b2\u0006\u0010*\u001a\u00020\u00112\u0006\u0010\"\u001a\u00020\u0011H\u0002\u001a\u0014\u0010N\u001a\u00020O*\u00020\u000b2\u0006\u0010!\u001a\u00020\u0011H\u0002\u001a,\u0010P\u001a\u00020)*\u00020\u000b2\u0006\u0010*\u001a\u00020\u00112\u0006\u0010\"\u001a\u00020\u00112\u0006\u0010Q\u001a\u00020\u000b2\u0006\u0010R\u001a\u00020\u0011H\u0002\u001a,\u0010S\u001a\u00020)*\u00020\u000b2\u0006\u0010*\u001a\u00020\u00112\u0006\u0010\"\u001a\u00020\u00112\u0006\u0010-\u001a\u00020\u000b2\u0006\u0010.\u001a\u00020\u000bH\u0002\u001a,\u0010T\u001a\u00020)*\u00020\u000b2\u0006\u0010!\u001a\u00020\u00112\u0006\u0010\"\u001a\u00020\u00112\u0006\u0010#\u001a\u00020\u000b2\u0006\u0010&\u001a\u00020\u000bH\u0002\u001a\u001c\u0010U\u001a\u00020\u0011*\u00020\u000b2\u0006\u00104\u001a\u0002052\u0006\u00106\u001a\u00020\u0011H\u0002\u001a\u0016\u0010V\u001a\u00020\u000b*\u00020:2\b\b\u0002\u0010;\u001a\u00020<H\u0007\u001a*\u0010V\u001a\u00020\u000b*\u0002022\b\b\u0002\u0010*\u001a\u00020\u00112\b\b\u0002\u0010\"\u001a\u00020\u00112\b\b\u0002\u0010;\u001a\u00020<H\u0007\u001a\u0016\u0010V\u001a\u00020\u000b*\u0002022\b\b\u0002\u0010;\u001a\u00020<H\u0007\u001a\u0016\u0010V\u001a\u00020\u000b*\u00020\u00112\b\b\u0002\u0010;\u001a\u00020<H\u0007\u001a\u0016\u0010V\u001a\u00020\u000b*\u00020\u000e2\b\b\u0002\u0010;\u001a\u00020<H\u0007\u001a\u0016\u0010V\u001a\u00020\u000b*\u00020I2\b\b\u0002\u0010;\u001a\u00020<H\u0007\u001a$\u0010W\u001a\u00020\u000b*\u00020\u000e2\u0006\u0010X\u001a\u00020Y2\u0006\u0010Z\u001a\u00020\u000b2\u0006\u0010[\u001a\u00020\u0011H\u0003\u001a,\u0010\\\u001a\u00020\u000b*\u0002022\u0006\u0010*\u001a\u00020\u00112\u0006\u0010\"\u001a\u00020\u00112\u0006\u0010?\u001a\u00020@2\u0006\u00103\u001a\u00020\u0001H\u0003\u001a,\u0010]\u001a\u00020\u000b*\u0002022\u0006\u0010*\u001a\u00020\u00112\u0006\u0010\"\u001a\u00020\u00112\u0006\u0010?\u001a\u00020@2\u0006\u00103\u001a\u00020\u0001H\u0003\u001a,\u0010^\u001a\u00020\u000b*\u0002022\u0006\u0010*\u001a\u00020\u00112\u0006\u0010\"\u001a\u00020\u00112\u0006\u0010?\u001a\u00020@2\u0006\u00103\u001a\u00020\u0001H\u0003\u001a,\u0010_\u001a\u00020\u000b*\u0002022\u0006\u0010*\u001a\u00020\u00112\u0006\u0010\"\u001a\u00020\u00112\u0006\u0010?\u001a\u00020@2\u0006\u00103\u001a\u00020\u0001H\u0003\"\u001c\u0010\u0000\u001a\u00020\u00018\u0000X\u0081\u0004¢\u0006\u000e\n\u0000\u0012\u0004\b\u0002\u0010\u0003\u001a\u0004\b\u0004\u0010\u0005\"\u000e\u0010\u0006\u001a\u00020\u0001X\u0082\u0004¢\u0006\u0002\n\u0000\"\u000e\u0010\u0007\u001a\u00020\u0001X\u0082\u0004¢\u0006\u0002\n\u0000\"\u000e\u0010\b\u001a\u00020\tX\u0082\u0004¢\u0006\u0002\n\u0000\"\u000e\u0010\n\u001a\u00020\u000bX\u0082T¢\u0006\u0002\n\u0000\"\u000e\u0010\f\u001a\u00020\u000bX\u0082T¢\u0006\u0002\n\u0000¨\u0006`"}, d2 = {"BYTE_TO_LOWER_CASE_HEX_DIGITS", "", "getBYTE_TO_LOWER_CASE_HEX_DIGITS$annotations", "()V", "getBYTE_TO_LOWER_CASE_HEX_DIGITS", "()[I", "BYTE_TO_UPPER_CASE_HEX_DIGITS", "HEX_DIGITS_TO_DECIMAL", "HEX_DIGITS_TO_LONG_DECIMAL", "", "LOWER_CASE_HEX_DIGITS", "", "UPPER_CASE_HEX_DIGITS", "charsPerSet", "", "charsPerElement", "elementsPerSet", "", "elementSeparatorLength", "checkFormatLength", "formatLength", "formattedStringLength", "numberOfBytes", "byteSeparatorLength", "bytePrefixLength", "byteSuffixLength", "bytesPerLine", "bytesPerGroup", "groupSeparatorLength", "parsedByteArrayMaxSize", "stringLength", "wholeElementsPerSet", "checkContainsAt", "index", "endIndex", "part", "ignoreCase", "", "partName", "checkNewLineAt", "checkNumberOfDigits", "", "startIndex", "typeHexLength", "checkPrefixSuffixNumberOfDigits", "prefix", "suffix", "checkZeroDigits", "decimalFromHexDigitAt", "formatByteAt", "", "byteToDigits", "destination", "", "destinationOffset", "bytePrefix", "byteSuffix", "hexToByte", "", "format", "Lkotlin/text/HexFormat;", "hexToByteArray", "hexToByteArrayNoLineAndGroupSeparator", "bytesFormat", "Lkotlin/text/HexFormat$BytesHexFormat;", "hexToByteArrayNoLineAndGroupSeparatorSlowPath", "hexToByteArrayShortByteSeparatorNoPrefixAndSuffix", "hexToByteArraySlowPath", "hexToInt", "hexToIntImpl", "hexToLong", "hexToLongImpl", "hexToShort", "", "longDecimalFromHexDigitAt", "parseByteAt", "parseInt", "parseLong", "throwInvalidDigitAt", "", "throwInvalidNumberOfDigits", "specifier", "expected", "throwInvalidPrefixSuffix", "throwNotContainedAt", "toCharArrayIfNotEmpty", "toHexString", "toHexStringImpl", "numberFormat", "Lkotlin/text/HexFormat$NumberHexFormat;", "digits", "bits", "toHexStringNoLineAndGroupSeparator", "toHexStringNoLineAndGroupSeparatorSlowPath", "toHexStringShortByteSeparatorNoPrefixAndSuffix", "toHexStringSlowPath", "kotlin-stdlib"}, k = 2, mv = {1, 9, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes.dex */
public final class HexExtensionsKt {
    private static final int[] BYTE_TO_LOWER_CASE_HEX_DIGITS;
    private static final int[] BYTE_TO_UPPER_CASE_HEX_DIGITS;
    private static final int[] HEX_DIGITS_TO_DECIMAL;
    private static final long[] HEX_DIGITS_TO_LONG_DECIMAL;
    private static final String LOWER_CASE_HEX_DIGITS = "0123456789abcdef";
    private static final String UPPER_CASE_HEX_DIGITS = "0123456789ABCDEF";

    public static /* synthetic */ void getBYTE_TO_LOWER_CASE_HEX_DIGITS$annotations() {
    }

    static {
        int[] iArr = new int[256];
        int i = 0;
        for (int i2 = 0; i2 < 256; i2++) {
            iArr[i2] = LOWER_CASE_HEX_DIGITS.charAt(i2 & 15) | (LOWER_CASE_HEX_DIGITS.charAt(i2 >> 4) << '\b');
        }
        BYTE_TO_LOWER_CASE_HEX_DIGITS = iArr;
        int[] iArr2 = new int[256];
        for (int i3 = 0; i3 < 256; i3++) {
            iArr2[i3] = UPPER_CASE_HEX_DIGITS.charAt(i3 & 15) | (UPPER_CASE_HEX_DIGITS.charAt(i3 >> 4) << '\b');
        }
        BYTE_TO_UPPER_CASE_HEX_DIGITS = iArr2;
        int[] iArr3 = new int[256];
        for (int i4 = 0; i4 < 256; i4++) {
            iArr3[i4] = -1;
        }
        int i5 = 0;
        int i6 = 0;
        while (i5 < r3.length()) {
            iArr3[r3.charAt(i5)] = i6;
            i5++;
            i6++;
        }
        int i7 = 0;
        int i8 = 0;
        while (i7 < r3.length()) {
            iArr3[r3.charAt(i7)] = i8;
            i7++;
            i8++;
        }
        HEX_DIGITS_TO_DECIMAL = iArr3;
        long[] jArr = new long[256];
        for (int i9 = 0; i9 < 256; i9++) {
            jArr[i9] = -1;
        }
        int i10 = 0;
        int i11 = 0;
        while (i10 < r4.length()) {
            jArr[r4.charAt(i10)] = i11;
            i10++;
            i11++;
        }
        int i12 = 0;
        while (i < r5.length()) {
            jArr[r5.charAt(i)] = i12;
            i++;
            i12++;
        }
        HEX_DIGITS_TO_LONG_DECIMAL = jArr;
    }

    public static final int[] getBYTE_TO_LOWER_CASE_HEX_DIGITS() {
        return BYTE_TO_LOWER_CASE_HEX_DIGITS;
    }

    public static final String toHexString(byte[] bArr, HexFormat format) {
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        Intrinsics.checkNotNullParameter(format, "format");
        return toHexString(bArr, 0, bArr.length, format);
    }

    public static /* synthetic */ String toHexString$default(byte[] bArr, HexFormat hexFormat, int i, Object obj) {
        if ((i & 1) != 0) {
            hexFormat = HexFormat.INSTANCE.getDefault();
        }
        return toHexString(bArr, hexFormat);
    }

    public static /* synthetic */ String toHexString$default(byte[] bArr, int i, int i2, HexFormat hexFormat, int i3, Object obj) {
        if ((i3 & 1) != 0) {
            i = 0;
        }
        if ((i3 & 2) != 0) {
            i2 = bArr.length;
        }
        if ((i3 & 4) != 0) {
            hexFormat = HexFormat.INSTANCE.getDefault();
        }
        return toHexString(bArr, i, i2, hexFormat);
    }

    public static final String toHexString(byte[] bArr, int i, int i2, HexFormat format) {
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        Intrinsics.checkNotNullParameter(format, "format");
        AbstractList.INSTANCE.checkBoundsIndexes$kotlin_stdlib(i, i2, bArr.length);
        if (i == i2) {
            return "";
        }
        int[] iArr = format.getUpperCase() ? BYTE_TO_UPPER_CASE_HEX_DIGITS : BYTE_TO_LOWER_CASE_HEX_DIGITS;
        HexFormat.BytesHexFormat bytes = format.getBytes();
        if (bytes.getNoLineAndGroupSeparator()) {
            return toHexStringNoLineAndGroupSeparator(bArr, i, i2, bytes, iArr);
        }
        return toHexStringSlowPath(bArr, i, i2, bytes, iArr);
    }

    private static final String toHexStringNoLineAndGroupSeparator(byte[] bArr, int i, int i2, HexFormat.BytesHexFormat bytesHexFormat, int[] iArr) {
        if (bytesHexFormat.getShortByteSeparatorNoPrefixAndSuffix()) {
            return toHexStringShortByteSeparatorNoPrefixAndSuffix(bArr, i, i2, bytesHexFormat, iArr);
        }
        return toHexStringNoLineAndGroupSeparatorSlowPath(bArr, i, i2, bytesHexFormat, iArr);
    }

    private static final String toHexStringShortByteSeparatorNoPrefixAndSuffix(byte[] bArr, int i, int i2, HexFormat.BytesHexFormat bytesHexFormat, int[] iArr) {
        int length = bytesHexFormat.getByteSeparator().length();
        if (length > 1) {
            throw new IllegalArgumentException("Failed requirement.".toString());
        }
        int i3 = i2 - i;
        int i4 = 0;
        if (length == 0) {
            char[] cArr = new char[checkFormatLength(i3 * 2)];
            while (i < i2) {
                i4 = formatByteAt(bArr, i, iArr, cArr, i4);
                i++;
            }
            return StringsKt.concatToString(cArr);
        }
        char[] cArr2 = new char[checkFormatLength((i3 * 3) - 1)];
        char charAt = bytesHexFormat.getByteSeparator().charAt(0);
        int formatByteAt = formatByteAt(bArr, i, iArr, cArr2, 0);
        for (int i5 = i + 1; i5 < i2; i5++) {
            cArr2[formatByteAt] = charAt;
            formatByteAt = formatByteAt(bArr, i5, iArr, cArr2, formatByteAt + 1);
        }
        return StringsKt.concatToString(cArr2);
    }

    private static final String toHexStringNoLineAndGroupSeparatorSlowPath(byte[] bArr, int i, int i2, HexFormat.BytesHexFormat bytesHexFormat, int[] iArr) {
        String bytePrefix = bytesHexFormat.getBytePrefix();
        String byteSuffix = bytesHexFormat.getByteSuffix();
        String byteSeparator = bytesHexFormat.getByteSeparator();
        char[] cArr = new char[formattedStringLength(i2 - i, byteSeparator.length(), bytePrefix.length(), byteSuffix.length())];
        int formatByteAt = formatByteAt(bArr, i, bytePrefix, byteSuffix, iArr, cArr, 0);
        for (int i3 = i + 1; i3 < i2; i3++) {
            formatByteAt = formatByteAt(bArr, i3, bytePrefix, byteSuffix, iArr, cArr, toCharArrayIfNotEmpty(byteSeparator, cArr, formatByteAt));
        }
        return StringsKt.concatToString(cArr);
    }

    private static final String toHexStringSlowPath(byte[] bArr, int i, int i2, HexFormat.BytesHexFormat bytesHexFormat, int[] iArr) {
        int i3;
        int i4;
        int bytesPerLine = bytesHexFormat.getBytesPerLine();
        int bytesPerGroup = bytesHexFormat.getBytesPerGroup();
        String bytePrefix = bytesHexFormat.getBytePrefix();
        String byteSuffix = bytesHexFormat.getByteSuffix();
        String byteSeparator = bytesHexFormat.getByteSeparator();
        String groupSeparator = bytesHexFormat.getGroupSeparator();
        int formattedStringLength = formattedStringLength(i2 - i, bytesPerLine, bytesPerGroup, groupSeparator.length(), byteSeparator.length(), bytePrefix.length(), byteSuffix.length());
        char[] cArr = new char[formattedStringLength];
        int i5 = i;
        int i6 = 0;
        int i7 = 0;
        int i8 = 0;
        while (i5 < i2) {
            if (i7 == bytesPerLine) {
                cArr[i6] = '\n';
                i6++;
                i3 = 0;
                i4 = 0;
            } else if (i8 == bytesPerGroup) {
                i6 = toCharArrayIfNotEmpty(groupSeparator, cArr, i6);
                i3 = i7;
                i4 = 0;
            } else {
                i3 = i7;
                i4 = i8;
            }
            if (i4 != 0) {
                i6 = toCharArrayIfNotEmpty(byteSeparator, cArr, i6);
            }
            String str = bytePrefix;
            int formatByteAt = formatByteAt(bArr, i5, str, byteSuffix, iArr, cArr, i6);
            i5++;
            i8 = i4 + 1;
            i6 = formatByteAt;
            bytePrefix = str;
            i7 = i3 + 1;
        }
        if (i6 != formattedStringLength) {
            throw new IllegalStateException("Check failed.".toString());
        }
        return StringsKt.concatToString(cArr);
    }

    private static final int formatByteAt(byte[] bArr, int i, String str, String str2, int[] iArr, char[] cArr, int i2) {
        return toCharArrayIfNotEmpty(str2, cArr, formatByteAt(bArr, i, iArr, cArr, toCharArrayIfNotEmpty(str, cArr, i2)));
    }

    private static final int formatByteAt(byte[] bArr, int i, int[] iArr, char[] cArr, int i2) {
        int i3 = iArr[bArr[i] & UByte.MAX_VALUE];
        cArr[i2] = (char) (i3 >> 8);
        cArr[i2 + 1] = (char) (i3 & 255);
        return i2 + 2;
    }

    private static final int formattedStringLength(int i, int i2, int i3, int i4) {
        if (i <= 0) {
            throw new IllegalArgumentException("Failed requirement.".toString());
        }
        long j = i2;
        return checkFormatLength((i * (((i3 + 2) + i4) + j)) - j);
    }

    public static final int formattedStringLength(int i, int i2, int i3, int i4, int i5, int i6, int i7) {
        if (i <= 0) {
            throw new IllegalArgumentException("Failed requirement.".toString());
        }
        int i8 = (i - 1) / i2;
        int i9 = (i2 - 1) / i3;
        int i10 = i % i2;
        if (i10 != 0) {
            i2 = i10;
        }
        return checkFormatLength(i8 + (((i9 * i8) + ((i2 - 1) / i3)) * i4) + (((r0 - i8) - r2) * i5) + (i * (i6 + 2 + i7)));
    }

    private static final int checkFormatLength(long j) {
        if (0 > j || j > 2147483647L) {
            throw new IllegalArgumentException("The resulting string length is too big: " + ((Object) ULong.m385toStringimpl(ULong.m339constructorimpl(j))));
        }
        return (int) j;
    }

    public static final byte[] hexToByteArray(String str, HexFormat format) {
        Intrinsics.checkNotNullParameter(str, "<this>");
        Intrinsics.checkNotNullParameter(format, "format");
        return hexToByteArray(str, 0, str.length(), format);
    }

    public static /* synthetic */ byte[] hexToByteArray$default(String str, HexFormat hexFormat, int i, Object obj) {
        if ((i & 1) != 0) {
            hexFormat = HexFormat.INSTANCE.getDefault();
        }
        return hexToByteArray(str, hexFormat);
    }

    static /* synthetic */ byte[] hexToByteArray$default(String str, int i, int i2, HexFormat hexFormat, int i3, Object obj) {
        if ((i3 & 1) != 0) {
            i = 0;
        }
        if ((i3 & 2) != 0) {
            i2 = str.length();
        }
        if ((i3 & 4) != 0) {
            hexFormat = HexFormat.INSTANCE.getDefault();
        }
        return hexToByteArray(str, i, i2, hexFormat);
    }

    private static final byte[] hexToByteArray(String str, int i, int i2, HexFormat hexFormat) {
        byte[] hexToByteArrayNoLineAndGroupSeparator;
        AbstractList.INSTANCE.checkBoundsIndexes$kotlin_stdlib(i, i2, str.length());
        if (i == i2) {
            return new byte[0];
        }
        HexFormat.BytesHexFormat bytes = hexFormat.getBytes();
        return (!bytes.getNoLineAndGroupSeparator() || (hexToByteArrayNoLineAndGroupSeparator = hexToByteArrayNoLineAndGroupSeparator(str, i, i2, bytes)) == null) ? hexToByteArraySlowPath(str, i, i2, bytes) : hexToByteArrayNoLineAndGroupSeparator;
    }

    private static final byte[] hexToByteArrayNoLineAndGroupSeparator(String str, int i, int i2, HexFormat.BytesHexFormat bytesHexFormat) {
        if (bytesHexFormat.getShortByteSeparatorNoPrefixAndSuffix()) {
            return hexToByteArrayShortByteSeparatorNoPrefixAndSuffix(str, i, i2, bytesHexFormat);
        }
        return hexToByteArrayNoLineAndGroupSeparatorSlowPath(str, i, i2, bytesHexFormat);
    }

    private static final byte[] hexToByteArrayShortByteSeparatorNoPrefixAndSuffix(String str, int i, int i2, HexFormat.BytesHexFormat bytesHexFormat) {
        int length = bytesHexFormat.getByteSeparator().length();
        if (length > 1) {
            throw new IllegalArgumentException("Failed requirement.".toString());
        }
        int i3 = i2 - i;
        int i4 = 2;
        if (length == 0) {
            if ((i3 & 1) != 0) {
                return null;
            }
            int i5 = i3 >> 1;
            byte[] bArr = new byte[i5];
            int i6 = 0;
            for (int i7 = 0; i7 < i5; i7++) {
                bArr[i7] = parseByteAt(str, i6);
                i6 += 2;
            }
            return bArr;
        }
        if (i3 % 3 != 2) {
            return null;
        }
        int i8 = (i3 / 3) + 1;
        byte[] bArr2 = new byte[i8];
        char charAt = bytesHexFormat.getByteSeparator().charAt(0);
        bArr2[0] = parseByteAt(str, 0);
        for (int i9 = 1; i9 < i8; i9++) {
            if (str.charAt(i4) != charAt) {
                String byteSeparator = bytesHexFormat.getByteSeparator();
                boolean ignoreCase = bytesHexFormat.getIgnoreCase();
                String str2 = byteSeparator;
                if (str2.length() != 0) {
                    int length2 = str2.length();
                    for (int i10 = 0; i10 < length2; i10++) {
                        if (!CharsKt.equals(byteSeparator.charAt(i10), str.charAt(i4 + i10), ignoreCase)) {
                            throwNotContainedAt(str, i4, i2, byteSeparator, "byte separator");
                        }
                    }
                    byteSeparator.length();
                }
            }
            bArr2[i9] = parseByteAt(str, i4 + 1);
            i4 += 3;
        }
        return bArr2;
    }

    private static final byte[] hexToByteArrayNoLineAndGroupSeparatorSlowPath(String str, int i, int i2, HexFormat.BytesHexFormat bytesHexFormat) {
        String bytePrefix = bytesHexFormat.getBytePrefix();
        String byteSuffix = bytesHexFormat.getByteSuffix();
        String byteSeparator = bytesHexFormat.getByteSeparator();
        long length = byteSeparator.length();
        long length2 = bytePrefix.length() + 2 + byteSuffix.length() + length;
        long j = i2 - i;
        int i3 = (int) ((j + length) / length2);
        if ((i3 * length2) - length != j) {
            return null;
        }
        boolean ignoreCase = bytesHexFormat.getIgnoreCase();
        byte[] bArr = new byte[i3];
        String str2 = bytePrefix;
        if (str2.length() != 0) {
            int length3 = str2.length();
            for (int i4 = 0; i4 < length3; i4++) {
                if (!CharsKt.equals(bytePrefix.charAt(i4), str.charAt(i + i4), ignoreCase)) {
                    throwNotContainedAt(str, i, i2, bytePrefix, "byte prefix");
                }
            }
            i += bytePrefix.length();
        }
        String str3 = byteSuffix + byteSeparator + bytePrefix;
        int i5 = i3 - 1;
        for (int i6 = 0; i6 < i5; i6++) {
            bArr[i6] = parseByteAt(str, i);
            i += 2;
            String str4 = str3;
            if (str4.length() != 0) {
                int length4 = str4.length();
                for (int i7 = 0; i7 < length4; i7++) {
                    if (!CharsKt.equals(str3.charAt(i7), str.charAt(i + i7), ignoreCase)) {
                        throwNotContainedAt(str, i, i2, str3, "byte suffix + byte separator + byte prefix");
                    }
                }
                i += str3.length();
            }
        }
        bArr[i5] = parseByteAt(str, i);
        int i8 = i + 2;
        String str5 = byteSuffix;
        if (str5.length() == 0) {
            return bArr;
        }
        int length5 = str5.length();
        for (int i9 = 0; i9 < length5; i9++) {
            if (!CharsKt.equals(byteSuffix.charAt(i9), str.charAt(i8 + i9), ignoreCase)) {
                throwNotContainedAt(str, i8, i2, byteSuffix, "byte suffix");
            }
        }
        byteSuffix.length();
        return bArr;
    }

    /* JADX WARN: Removed duplicated region for block: B:20:0x0100  */
    /* JADX WARN: Removed duplicated region for block: B:23:0x0119  */
    /* JADX WARN: Removed duplicated region for block: B:26:0x011c  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x00d4  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private static final byte[] hexToByteArraySlowPath(String str, int i, int i2, HexFormat.BytesHexFormat bytesHexFormat) {
        int i3;
        int i4;
        String str2;
        String str3;
        int i5;
        int bytesPerLine = bytesHexFormat.getBytesPerLine();
        int bytesPerGroup = bytesHexFormat.getBytesPerGroup();
        String bytePrefix = bytesHexFormat.getBytePrefix();
        String byteSuffix = bytesHexFormat.getByteSuffix();
        String byteSeparator = bytesHexFormat.getByteSeparator();
        String groupSeparator = bytesHexFormat.getGroupSeparator();
        boolean ignoreCase = bytesHexFormat.getIgnoreCase();
        int parsedByteArrayMaxSize = parsedByteArrayMaxSize(i2 - i, bytesPerLine, bytesPerGroup, groupSeparator.length(), byteSeparator.length(), bytePrefix.length(), byteSuffix.length());
        byte[] bArr = new byte[parsedByteArrayMaxSize];
        int i6 = i;
        int i7 = 0;
        int i8 = 0;
        int i9 = 0;
        while (i6 < i2) {
            if (i8 == bytesPerLine) {
                i6 = checkNewLineAt(str, i6, i2);
                i3 = bytesPerLine;
                i4 = bytesPerGroup;
                i8 = 0;
            } else if (i9 == bytesPerGroup) {
                String str4 = groupSeparator;
                if (str4.length() == 0) {
                    i3 = bytesPerLine;
                    i4 = bytesPerGroup;
                } else {
                    int length = str4.length();
                    int i10 = 0;
                    while (i10 < length) {
                        int i11 = bytesPerLine;
                        int i12 = bytesPerGroup;
                        if (!CharsKt.equals(groupSeparator.charAt(i10), str.charAt(i6 + i10), ignoreCase)) {
                            throwNotContainedAt(str, i6, i2, groupSeparator, "group separator");
                        }
                        i10++;
                        bytesPerLine = i11;
                        bytesPerGroup = i12;
                    }
                    i3 = bytesPerLine;
                    i4 = bytesPerGroup;
                    i6 += groupSeparator.length();
                }
            } else {
                i3 = bytesPerLine;
                i4 = bytesPerGroup;
                if (i9 != 0) {
                    String str5 = byteSeparator;
                    if (str5.length() != 0) {
                        int length2 = str5.length();
                        int i13 = 0;
                        while (i13 < length2) {
                            int i14 = length2;
                            if (!CharsKt.equals(byteSeparator.charAt(i13), str.charAt(i6 + i13), ignoreCase)) {
                                throwNotContainedAt(str, i6, i2, byteSeparator, "byte separator");
                            }
                            i13++;
                            length2 = i14;
                        }
                        i6 += byteSeparator.length();
                    }
                }
                i8++;
                i9++;
                str2 = bytePrefix;
                if (str2.length() != 0) {
                    int length3 = str2.length();
                    int i15 = 0;
                    while (i15 < length3) {
                        int i16 = length3;
                        if (!CharsKt.equals(bytePrefix.charAt(i15), str.charAt(i6 + i15), ignoreCase)) {
                            throwNotContainedAt(str, i6, i2, bytePrefix, "byte prefix");
                        }
                        i15++;
                        length3 = i16;
                    }
                    i6 += bytePrefix.length();
                }
                if (i2 - 2 < i6) {
                    throwInvalidNumberOfDigits(str, i6, i2, "exactly", 2);
                }
                int i17 = i7 + 1;
                bArr[i7] = parseByteAt(str, i6);
                i6 += 2;
                str3 = byteSuffix;
                if (str3.length() != 0) {
                    i5 = i17;
                } else {
                    int length4 = str3.length();
                    int i18 = 0;
                    while (i18 < length4) {
                        int i19 = i17;
                        if (!CharsKt.equals(byteSuffix.charAt(i18), str.charAt(i6 + i18), ignoreCase)) {
                            throwNotContainedAt(str, i6, i2, byteSuffix, "byte suffix");
                        }
                        i18++;
                        i17 = i19;
                    }
                    i5 = i17;
                    i6 += byteSuffix.length();
                }
                i7 = i5;
                bytesPerLine = i3;
                bytesPerGroup = i4;
            }
            i9 = 0;
            i8++;
            i9++;
            str2 = bytePrefix;
            if (str2.length() != 0) {
            }
            if (i2 - 2 < i6) {
            }
            int i172 = i7 + 1;
            bArr[i7] = parseByteAt(str, i6);
            i6 += 2;
            str3 = byteSuffix;
            if (str3.length() != 0) {
            }
            i7 = i5;
            bytesPerLine = i3;
            bytesPerGroup = i4;
        }
        if (i7 == parsedByteArrayMaxSize) {
            return bArr;
        }
        byte[] copyOf = Arrays.copyOf(bArr, i7);
        Intrinsics.checkNotNullExpressionValue(copyOf, "copyOf(...)");
        return copyOf;
    }

    public static final int parsedByteArrayMaxSize(int i, int i2, int i3, int i4, int i5, int i6, int i7) {
        long charsPerSet;
        if (i <= 0) {
            throw new IllegalArgumentException("Failed requirement.".toString());
        }
        long j = i6 + 2 + i7;
        long charsPerSet2 = charsPerSet(j, i3, i5);
        if (i2 <= i3) {
            charsPerSet = charsPerSet(j, i2, i5);
        } else {
            charsPerSet = charsPerSet(charsPerSet2, i2 / i3, i4);
            int i8 = i2 % i3;
            if (i8 != 0) {
                charsPerSet = charsPerSet + i4 + charsPerSet(j, i8, i5);
            }
        }
        long j2 = i;
        long wholeElementsPerSet = wholeElementsPerSet(j2, charsPerSet, 1);
        long j3 = j2 - ((charsPerSet + 1) * wholeElementsPerSet);
        long wholeElementsPerSet2 = wholeElementsPerSet(j3, charsPerSet2, i4);
        long j4 = j3 - ((charsPerSet2 + i4) * wholeElementsPerSet2);
        long wholeElementsPerSet3 = wholeElementsPerSet(j4, j, i5);
        return (int) ((wholeElementsPerSet * i2) + (wholeElementsPerSet2 * i3) + wholeElementsPerSet3 + (j4 - ((j + ((long) i5)) * wholeElementsPerSet3) > 0 ? 1 : 0));
    }

    private static final long charsPerSet(long j, int i, int i2) {
        if (i <= 0) {
            throw new IllegalArgumentException("Failed requirement.".toString());
        }
        long j2 = i;
        return (j * j2) + (i2 * (j2 - 1));
    }

    private static final long wholeElementsPerSet(long j, long j2, int i) {
        if (j <= 0 || j2 <= 0) {
            return 0L;
        }
        long j3 = i;
        return (j + j3) / (j2 + j3);
    }

    private static final int checkNewLineAt(String str, int i, int i2) {
        if (str.charAt(i) == '\r') {
            int i3 = i + 1;
            return (i3 >= i2 || str.charAt(i3) != '\n') ? i3 : i + 2;
        }
        if (str.charAt(i) == '\n') {
            return i + 1;
        }
        throw new NumberFormatException("Expected a new line at index " + i + ", but was " + str.charAt(i));
    }

    public static /* synthetic */ String toHexString$default(byte b, HexFormat hexFormat, int i, Object obj) {
        if ((i & 1) != 0) {
            hexFormat = HexFormat.INSTANCE.getDefault();
        }
        return toHexString(b, hexFormat);
    }

    public static final String toHexString(byte b, HexFormat format) {
        Intrinsics.checkNotNullParameter(format, "format");
        String str = format.getUpperCase() ? UPPER_CASE_HEX_DIGITS : LOWER_CASE_HEX_DIGITS;
        HexFormat.NumberHexFormat number = format.getNumber();
        if (number.getIsDigitsOnlyAndNoPadding()) {
            char[] cArr = {str.charAt((b >> 4) & 15), str.charAt(b & 15)};
            if (number.getRemoveLeadingZeros()) {
                return StringsKt.concatToString$default(cArr, RangesKt.coerceAtMost((Integer.numberOfLeadingZeros(b & UByte.MAX_VALUE) - 24) >> 2, 1), 0, 2, null);
            }
            return StringsKt.concatToString(cArr);
        }
        return toHexStringImpl(b, number, str, 8);
    }

    public static final byte hexToByte(String str, HexFormat format) {
        Intrinsics.checkNotNullParameter(str, "<this>");
        Intrinsics.checkNotNullParameter(format, "format");
        return hexToByte(str, 0, str.length(), format);
    }

    public static /* synthetic */ byte hexToByte$default(String str, HexFormat hexFormat, int i, Object obj) {
        if ((i & 1) != 0) {
            hexFormat = HexFormat.INSTANCE.getDefault();
        }
        return hexToByte(str, hexFormat);
    }

    static /* synthetic */ byte hexToByte$default(String str, int i, int i2, HexFormat hexFormat, int i3, Object obj) {
        if ((i3 & 1) != 0) {
            i = 0;
        }
        if ((i3 & 2) != 0) {
            i2 = str.length();
        }
        if ((i3 & 4) != 0) {
            hexFormat = HexFormat.INSTANCE.getDefault();
        }
        return hexToByte(str, i, i2, hexFormat);
    }

    private static final byte hexToByte(String str, int i, int i2, HexFormat hexFormat) {
        return (byte) hexToIntImpl(str, i, i2, hexFormat, 2);
    }

    public static /* synthetic */ String toHexString$default(short s, HexFormat hexFormat, int i, Object obj) {
        if ((i & 1) != 0) {
            hexFormat = HexFormat.INSTANCE.getDefault();
        }
        return toHexString(s, hexFormat);
    }

    public static final String toHexString(short s, HexFormat format) {
        Intrinsics.checkNotNullParameter(format, "format");
        String str = format.getUpperCase() ? UPPER_CASE_HEX_DIGITS : LOWER_CASE_HEX_DIGITS;
        HexFormat.NumberHexFormat number = format.getNumber();
        if (number.getIsDigitsOnlyAndNoPadding()) {
            char[] cArr = {str.charAt((s >> 12) & 15), str.charAt((s >> 8) & 15), str.charAt((s >> 4) & 15), str.charAt(s & 15)};
            if (number.getRemoveLeadingZeros()) {
                return StringsKt.concatToString$default(cArr, RangesKt.coerceAtMost((Integer.numberOfLeadingZeros(s & UShort.MAX_VALUE) - 16) >> 2, 3), 0, 2, null);
            }
            return StringsKt.concatToString(cArr);
        }
        return toHexStringImpl(s, number, str, 16);
    }

    public static final short hexToShort(String str, HexFormat format) {
        Intrinsics.checkNotNullParameter(str, "<this>");
        Intrinsics.checkNotNullParameter(format, "format");
        return hexToShort(str, 0, str.length(), format);
    }

    public static /* synthetic */ short hexToShort$default(String str, HexFormat hexFormat, int i, Object obj) {
        if ((i & 1) != 0) {
            hexFormat = HexFormat.INSTANCE.getDefault();
        }
        return hexToShort(str, hexFormat);
    }

    static /* synthetic */ short hexToShort$default(String str, int i, int i2, HexFormat hexFormat, int i3, Object obj) {
        if ((i3 & 1) != 0) {
            i = 0;
        }
        if ((i3 & 2) != 0) {
            i2 = str.length();
        }
        if ((i3 & 4) != 0) {
            hexFormat = HexFormat.INSTANCE.getDefault();
        }
        return hexToShort(str, i, i2, hexFormat);
    }

    private static final short hexToShort(String str, int i, int i2, HexFormat hexFormat) {
        return (short) hexToIntImpl(str, i, i2, hexFormat, 4);
    }

    public static /* synthetic */ String toHexString$default(int i, HexFormat hexFormat, int i2, Object obj) {
        if ((i2 & 1) != 0) {
            hexFormat = HexFormat.INSTANCE.getDefault();
        }
        return toHexString(i, hexFormat);
    }

    public static final String toHexString(int i, HexFormat format) {
        Intrinsics.checkNotNullParameter(format, "format");
        String str = format.getUpperCase() ? UPPER_CASE_HEX_DIGITS : LOWER_CASE_HEX_DIGITS;
        HexFormat.NumberHexFormat number = format.getNumber();
        if (number.getIsDigitsOnlyAndNoPadding()) {
            char[] cArr = {str.charAt((i >> 28) & 15), str.charAt((i >> 24) & 15), str.charAt((i >> 20) & 15), str.charAt((i >> 16) & 15), str.charAt((i >> 12) & 15), str.charAt((i >> 8) & 15), str.charAt((i >> 4) & 15), str.charAt(i & 15)};
            if (number.getRemoveLeadingZeros()) {
                return StringsKt.concatToString$default(cArr, RangesKt.coerceAtMost(Integer.numberOfLeadingZeros(i) >> 2, 7), 0, 2, null);
            }
            return StringsKt.concatToString(cArr);
        }
        return toHexStringImpl(i, number, str, 32);
    }

    public static final int hexToInt(String str, HexFormat format) {
        Intrinsics.checkNotNullParameter(str, "<this>");
        Intrinsics.checkNotNullParameter(format, "format");
        return hexToInt(str, 0, str.length(), format);
    }

    public static /* synthetic */ int hexToInt$default(String str, HexFormat hexFormat, int i, Object obj) {
        if ((i & 1) != 0) {
            hexFormat = HexFormat.INSTANCE.getDefault();
        }
        return hexToInt(str, hexFormat);
    }

    static /* synthetic */ int hexToInt$default(String str, int i, int i2, HexFormat hexFormat, int i3, Object obj) {
        if ((i3 & 1) != 0) {
            i = 0;
        }
        if ((i3 & 2) != 0) {
            i2 = str.length();
        }
        if ((i3 & 4) != 0) {
            hexFormat = HexFormat.INSTANCE.getDefault();
        }
        return hexToInt(str, i, i2, hexFormat);
    }

    private static final int hexToInt(String str, int i, int i2, HexFormat hexFormat) {
        return hexToIntImpl(str, i, i2, hexFormat, 8);
    }

    public static /* synthetic */ String toHexString$default(long j, HexFormat hexFormat, int i, Object obj) {
        if ((i & 1) != 0) {
            hexFormat = HexFormat.INSTANCE.getDefault();
        }
        return toHexString(j, hexFormat);
    }

    public static final String toHexString(long j, HexFormat format) {
        Intrinsics.checkNotNullParameter(format, "format");
        String str = format.getUpperCase() ? UPPER_CASE_HEX_DIGITS : LOWER_CASE_HEX_DIGITS;
        HexFormat.NumberHexFormat number = format.getNumber();
        if (number.getIsDigitsOnlyAndNoPadding()) {
            char[] cArr = {str.charAt((int) ((j >> 60) & 15)), str.charAt((int) ((j >> 56) & 15)), str.charAt((int) ((j >> 52) & 15)), str.charAt((int) ((j >> 48) & 15)), str.charAt((int) ((j >> 44) & 15)), str.charAt((int) ((j >> 40) & 15)), str.charAt((int) ((j >> 36) & 15)), str.charAt((int) ((j >> 32) & 15)), str.charAt((int) ((j >> 28) & 15)), str.charAt((int) ((j >> 24) & 15)), str.charAt((int) ((j >> 20) & 15)), str.charAt((int) ((j >> 16) & 15)), str.charAt((int) ((j >> 12) & 15)), str.charAt((int) ((j >> 8) & 15)), str.charAt((int) ((j >> 4) & 15)), str.charAt((int) (j & 15))};
            if (number.getRemoveLeadingZeros()) {
                return StringsKt.concatToString$default(cArr, RangesKt.coerceAtMost(Long.numberOfLeadingZeros(j) >> 2, 15), 0, 2, null);
            }
            return StringsKt.concatToString(cArr);
        }
        return toHexStringImpl(j, number, str, 64);
    }

    public static final long hexToLong(String str, HexFormat format) {
        Intrinsics.checkNotNullParameter(str, "<this>");
        Intrinsics.checkNotNullParameter(format, "format");
        return hexToLong(str, 0, str.length(), format);
    }

    public static /* synthetic */ long hexToLong$default(String str, HexFormat hexFormat, int i, Object obj) {
        if ((i & 1) != 0) {
            hexFormat = HexFormat.INSTANCE.getDefault();
        }
        return hexToLong(str, hexFormat);
    }

    public static /* synthetic */ long hexToLong$default(String str, int i, int i2, HexFormat hexFormat, int i3, Object obj) {
        if ((i3 & 1) != 0) {
            i = 0;
        }
        if ((i3 & 2) != 0) {
            i2 = str.length();
        }
        if ((i3 & 4) != 0) {
            hexFormat = HexFormat.INSTANCE.getDefault();
        }
        return hexToLong(str, i, i2, hexFormat);
    }

    public static final long hexToLong(String str, int i, int i2, HexFormat format) {
        Intrinsics.checkNotNullParameter(str, "<this>");
        Intrinsics.checkNotNullParameter(format, "format");
        return hexToLongImpl(str, i, i2, format, 16);
    }

    private static final String toHexStringImpl(long j, HexFormat.NumberHexFormat numberHexFormat, String str, int i) {
        if ((i & 3) != 0) {
            throw new IllegalArgumentException("Failed requirement.".toString());
        }
        int i2 = i >> 2;
        int minLength = numberHexFormat.getMinLength();
        int coerceAtLeast = RangesKt.coerceAtLeast(minLength - i2, 0);
        String prefix = numberHexFormat.getPrefix();
        String suffix = numberHexFormat.getSuffix();
        boolean removeLeadingZeros = numberHexFormat.getRemoveLeadingZeros();
        int checkFormatLength = checkFormatLength(prefix.length() + coerceAtLeast + i2 + suffix.length());
        char[] cArr = new char[checkFormatLength];
        int charArrayIfNotEmpty = toCharArrayIfNotEmpty(prefix, cArr, 0);
        if (coerceAtLeast > 0) {
            int i3 = coerceAtLeast + charArrayIfNotEmpty;
            ArraysKt.fill(cArr, str.charAt(0), charArrayIfNotEmpty, i3);
            charArrayIfNotEmpty = i3;
        }
        int i4 = i;
        for (int i5 = 0; i5 < i2; i5++) {
            i4 -= 4;
            int i6 = (int) ((j >> i4) & 15);
            removeLeadingZeros = removeLeadingZeros && i6 == 0 && (i4 >> 2) >= minLength;
            if (!removeLeadingZeros) {
                cArr[charArrayIfNotEmpty] = str.charAt(i6);
                charArrayIfNotEmpty++;
            }
        }
        int charArrayIfNotEmpty2 = toCharArrayIfNotEmpty(suffix, cArr, charArrayIfNotEmpty);
        return charArrayIfNotEmpty2 == checkFormatLength ? StringsKt.concatToString(cArr) : StringsKt.concatToString$default(cArr, 0, charArrayIfNotEmpty2, 1, null);
    }

    private static final int toCharArrayIfNotEmpty(String str, char[] cArr, int i) {
        int length = str.length();
        if (length != 0) {
            if (length == 1) {
                cArr[i] = str.charAt(0);
            } else {
                int length2 = str.length();
                Intrinsics.checkNotNull(str, "null cannot be cast to non-null type java.lang.String");
                str.getChars(0, length2, cArr, i);
            }
        }
        return i + str.length();
    }

    private static final int hexToIntImpl(String str, int i, int i2, HexFormat hexFormat, int i3) {
        AbstractList.INSTANCE.checkBoundsIndexes$kotlin_stdlib(i, i2, str.length());
        HexFormat.NumberHexFormat number = hexFormat.getNumber();
        if (number.getIsDigitsOnly()) {
            checkNumberOfDigits(str, i, i2, i3);
            return parseInt(str, i, i2);
        }
        String prefix = number.getPrefix();
        String suffix = number.getSuffix();
        checkPrefixSuffixNumberOfDigits(str, i, i2, prefix, suffix, number.getIgnoreCase(), i3);
        return parseInt(str, i + prefix.length(), i2 - suffix.length());
    }

    private static final long hexToLongImpl(String str, int i, int i2, HexFormat hexFormat, int i3) {
        AbstractList.INSTANCE.checkBoundsIndexes$kotlin_stdlib(i, i2, str.length());
        HexFormat.NumberHexFormat number = hexFormat.getNumber();
        if (number.getIsDigitsOnly()) {
            checkNumberOfDigits(str, i, i2, i3);
            return parseLong(str, i, i2);
        }
        String prefix = number.getPrefix();
        String suffix = number.getSuffix();
        checkPrefixSuffixNumberOfDigits(str, i, i2, prefix, suffix, number.getIgnoreCase(), i3);
        return parseLong(str, i + prefix.length(), i2 - suffix.length());
    }

    private static final void checkPrefixSuffixNumberOfDigits(String str, int i, int i2, String str2, String str3, boolean z, int i3) {
        if ((i2 - i) - str2.length() <= str3.length()) {
            throwInvalidPrefixSuffix(str, i, i2, str2, str3);
        }
        String str4 = str2;
        if (str4.length() != 0) {
            int length = str4.length();
            for (int i4 = 0; i4 < length; i4++) {
                if (!CharsKt.equals(str2.charAt(i4), str.charAt(i + i4), z)) {
                    throwNotContainedAt(str, i, i2, str2, "prefix");
                }
            }
            i += str2.length();
        }
        int length2 = i2 - str3.length();
        String str5 = str3;
        if (str5.length() != 0) {
            int length3 = str5.length();
            for (int i5 = 0; i5 < length3; i5++) {
                if (!CharsKt.equals(str3.charAt(i5), str.charAt(length2 + i5), z)) {
                    throwNotContainedAt(str, length2, i2, str3, "suffix");
                }
            }
            str3.length();
        }
        checkNumberOfDigits(str, i, length2, i3);
    }

    private static final void checkNumberOfDigits(String str, int i, int i2, int i3) {
        int i4 = i2 - i;
        if (i4 < 1) {
            throwInvalidNumberOfDigits(str, i, i2, "at least", 1);
        } else if (i4 > i3) {
            checkZeroDigits(str, i, (i4 + i) - i3);
        }
    }

    private static final void checkZeroDigits(String str, int i, int i2) {
        while (i < i2) {
            if (str.charAt(i) != '0') {
                throw new NumberFormatException("Expected the hexadecimal digit '0' at index " + i + ", but was '" + str.charAt(i) + "'.\nThe result won't fit the type being parsed.");
            }
            i++;
        }
    }

    private static final int checkContainsAt(String str, int i, int i2, String str2, boolean z, String str3) {
        String str4 = str2;
        if (str4.length() == 0) {
            return i;
        }
        int length = str4.length();
        for (int i3 = 0; i3 < length; i3++) {
            if (!CharsKt.equals(str2.charAt(i3), str.charAt(i + i3), z)) {
                throwNotContainedAt(str, i, i2, str2, str3);
            }
        }
        return i + str2.length();
    }

    private static final int decimalFromHexDigitAt(String str, int i) {
        int i2;
        char charAt = str.charAt(i);
        if ((charAt >>> '\b') == 0 && (i2 = HEX_DIGITS_TO_DECIMAL[charAt]) >= 0) {
            return i2;
        }
        throwInvalidDigitAt(str, i);
        throw new KotlinNothingValueException();
    }

    private static final long longDecimalFromHexDigitAt(String str, int i) {
        char charAt = str.charAt(i);
        if ((charAt >>> '\b') == 0) {
            long j = HEX_DIGITS_TO_LONG_DECIMAL[charAt];
            if (j >= 0) {
                return j;
            }
        }
        throwInvalidDigitAt(str, i);
        throw new KotlinNothingValueException();
    }

    private static final void throwInvalidNumberOfDigits(String str, int i, int i2, String str2, int i3) {
        Intrinsics.checkNotNull(str, "null cannot be cast to non-null type java.lang.String");
        String substring = str.substring(i, i2);
        Intrinsics.checkNotNullExpressionValue(substring, "substring(...)");
        throw new NumberFormatException("Expected " + str2 + ' ' + i3 + " hexadecimal digits at index " + i + ", but was \"" + substring + "\" of length " + (i2 - i));
    }

    private static final void throwNotContainedAt(String str, int i, int i2, String str2, String str3) {
        int coerceAtMost = RangesKt.coerceAtMost(str2.length() + i, i2);
        Intrinsics.checkNotNull(str, "null cannot be cast to non-null type java.lang.String");
        String substring = str.substring(i, coerceAtMost);
        Intrinsics.checkNotNullExpressionValue(substring, "substring(...)");
        throw new NumberFormatException("Expected " + str3 + " \"" + str2 + "\" at index " + i + ", but was " + substring);
    }

    private static final void throwInvalidPrefixSuffix(String str, int i, int i2, String str2, String str3) {
        Intrinsics.checkNotNull(str, "null cannot be cast to non-null type java.lang.String");
        String substring = str.substring(i, i2);
        Intrinsics.checkNotNullExpressionValue(substring, "substring(...)");
        throw new NumberFormatException("Expected a hexadecimal number with prefix \"" + str2 + "\" and suffix \"" + str3 + "\", but was " + substring);
    }

    private static final Void throwInvalidDigitAt(String str, int i) {
        throw new NumberFormatException("Expected a hexadecimal digit at index " + i + ", but was " + str.charAt(i));
    }

    private static final byte parseByteAt(String str, int i) {
        int[] iArr;
        int i2;
        int i3;
        char charAt = str.charAt(i);
        if ((charAt >>> '\b') != 0 || (i2 = (iArr = HEX_DIGITS_TO_DECIMAL)[charAt]) < 0) {
            throwInvalidDigitAt(str, i);
            throw new KotlinNothingValueException();
        }
        int i4 = i + 1;
        char charAt2 = str.charAt(i4);
        if ((charAt2 >>> '\b') == 0 && (i3 = iArr[charAt2]) >= 0) {
            return (byte) ((i2 << 4) | i3);
        }
        throwInvalidDigitAt(str, i4);
        throw new KotlinNothingValueException();
    }

    private static final int parseInt(String str, int i, int i2) {
        int i3;
        int i4 = 0;
        while (i < i2) {
            int i5 = i4 << 4;
            char charAt = str.charAt(i);
            if ((charAt >>> '\b') != 0 || (i3 = HEX_DIGITS_TO_DECIMAL[charAt]) < 0) {
                throwInvalidDigitAt(str, i);
                throw new KotlinNothingValueException();
            }
            i4 = i5 | i3;
            i++;
        }
        return i4;
    }

    private static final long parseLong(String str, int i, int i2) {
        long j = 0;
        while (i < i2) {
            long j2 = j << 4;
            char charAt = str.charAt(i);
            if ((charAt >>> '\b') == 0) {
                long j3 = HEX_DIGITS_TO_LONG_DECIMAL[charAt];
                if (j3 >= 0) {
                    j = j2 | j3;
                    i++;
                }
            }
            throwInvalidDigitAt(str, i);
            throw new KotlinNothingValueException();
        }
        return j;
    }
}
