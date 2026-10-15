.class public final Lcom/listpro/xyon/pro/HostCardEmulatorService;
.super Landroid/nfc/cardemulation/HostApduService;
.source "HostCardEmulatorService.kt"


# annotations
.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lcom/listpro/xyon/pro/HostCardEmulatorService$Companion;
    }
.end annotation

.annotation runtime Lkotlin/Metadata;
    d1 = {
        "\u0000(\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0008\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u0008\n\u0000\n\u0002\u0010\u0012\n\u0002\u0008\u0002\n\u0002\u0018\u0002\n\u0002\u0008\u0002\u0018\u0000 \r2\u00020\u0001:\u0001\rB\u0007\u00a2\u0006\u0004\u0008\u0002\u0010\u0003J\u0010\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H\u0016J\u001a\u0010\u0008\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\t2\u0008\u0010\u000b\u001a\u0004\u0018\u00010\u000cH\u0016\u00a8\u0006\u000e"
    }
    d2 = {
        "Lcom/listpro/xyon/pro/HostCardEmulatorService;",
        "Landroid/nfc/cardemulation/HostApduService;",
        "<init>",
        "()V",
        "onDeactivated",
        "",
        "reason",
        "",
        "processCommandApdu",
        "",
        "commandApdu",
        "extras",
        "Landroid/os/Bundle;",
        "Companion",
        "app_release"
    }
    k = 0x1
    mv = {
        0x2,
        0x0,
        0x0
    }
    xi = 0x30
.end annotation


# static fields
.field private static final AID:Ljava/lang/String;

.field public static final Companion:Lcom/listpro/xyon/pro/HostCardEmulatorService$Companion;

.field private static final SELECT_APDU_HEADER:Ljava/lang/String;

.field private static final SELECT_OK_SW:[B

.field private static final UNKNOWN_CMD_SW:[B

.field private static groupData:Ljava/lang/String;


# direct methods
.method static constructor <clinit>()V
    .locals 2

    new-instance v0, Lcom/listpro/xyon/pro/HostCardEmulatorService$Companion;

    const/4 v1, 0x0

    invoke-direct {v0, v1}, Lcom/listpro/xyon/pro/HostCardEmulatorService$Companion;-><init>(Lkotlin/jvm/internal/DefaultConstructorMarker;)V

    sput-object v0, Lcom/listpro/xyon/pro/HostCardEmulatorService;->Companion:Lcom/listpro/xyon/pro/HostCardEmulatorService$Companion;

    .line 13
    const-string v1, "F0010203040506"

    sput-object v1, Lcom/listpro/xyon/pro/HostCardEmulatorService;->AID:Ljava/lang/String;

    .line 14
    const-string v1, "00A40400"

    sput-object v1, Lcom/listpro/xyon/pro/HostCardEmulatorService;->SELECT_APDU_HEADER:Ljava/lang/String;

    .line 15
    const-string v1, "9000"

    invoke-virtual {v0, v1}, Lcom/listpro/xyon/pro/HostCardEmulatorService$Companion;->hexStringToByteArray(Ljava/lang/String;)[B

    move-result-object v1

    sput-object v1, Lcom/listpro/xyon/pro/HostCardEmulatorService;->SELECT_OK_SW:[B

    .line 16
    const-string v1, "0000"

    invoke-virtual {v0, v1}, Lcom/listpro/xyon/pro/HostCardEmulatorService$Companion;->hexStringToByteArray(Ljava/lang/String;)[B

    move-result-object v0

    sput-object v0, Lcom/listpro/xyon/pro/HostCardEmulatorService;->UNKNOWN_CMD_SW:[B

    return-void
.end method

.method public constructor <init>()V
    .locals 0

    .line 9
    invoke-direct {p0}, Landroid/nfc/cardemulation/HostApduService;-><init>()V

    return-void
.end method

.method public static final synthetic access$getGroupData$cp()Ljava/lang/String;
    .locals 1

    .line 9
    sget-object v0, Lcom/listpro/xyon/pro/HostCardEmulatorService;->groupData:Ljava/lang/String;

    return-object v0
.end method

.method public static final synthetic access$setGroupData$cp(Ljava/lang/String;)V
    .locals 0

    .line 9
    sput-object p0, Lcom/listpro/xyon/pro/HostCardEmulatorService;->groupData:Ljava/lang/String;

    return-void
.end method


# virtual methods
.method public onDeactivated(I)V
    .locals 0

    const/4 p1, 0x0

    .line 41
    sput-object p1, Lcom/listpro/xyon/pro/HostCardEmulatorService;->groupData:Ljava/lang/String;

    return-void
.end method

.method public processCommandApdu([BLandroid/os/Bundle;)[B
    .locals 4

    const-string p2, "commandApdu"

    invoke-static {p1, p2}, Lkotlin/jvm/internal/Intrinsics;->checkNotNullParameter(Ljava/lang/Object;Ljava/lang/String;)V

    .line 45
    sget-object p2, Lcom/listpro/xyon/pro/HostCardEmulatorService;->Companion:Lcom/listpro/xyon/pro/HostCardEmulatorService$Companion;

    invoke-virtual {p2, p1}, Lcom/listpro/xyon/pro/HostCardEmulatorService$Companion;->byteArrayToHexString([B)Ljava/lang/String;

    move-result-object p1

    .line 47
    sget-object p2, Lcom/listpro/xyon/pro/HostCardEmulatorService;->SELECT_APDU_HEADER:Ljava/lang/String;

    invoke-virtual {p2}, Ljava/lang/String;->length()I

    move-result v0

    const/4 v1, 0x0

    invoke-virtual {p1, v1, v0}, Ljava/lang/String;->substring(II)Ljava/lang/String;

    move-result-object v0

    const-string v2, "substring(...)"

    invoke-static {v0, v2}, Lkotlin/jvm/internal/Intrinsics;->checkNotNullExpressionValue(Ljava/lang/Object;Ljava/lang/String;)V

    invoke-static {v0, p2}, Lkotlin/jvm/internal/Intrinsics;->areEqual(Ljava/lang/Object;Ljava/lang/Object;)Z

    move-result p2

    if-eqz p2, :cond_0

    .line 48
    sget-object p2, Lcom/listpro/xyon/pro/HostCardEmulatorService;->AID:Ljava/lang/String;

    invoke-virtual {p2}, Ljava/lang/String;->length()I

    move-result v0

    const/16 v3, 0xa

    add-int/2addr v0, v3

    invoke-virtual {p1, v3, v0}, Ljava/lang/String;->substring(II)Ljava/lang/String;

    move-result-object p1

    invoke-static {p1, v2}, Lkotlin/jvm/internal/Intrinsics;->checkNotNullExpressionValue(Ljava/lang/Object;Ljava/lang/String;)V

    .line 49
    sget-object v0, Ljava/util/Locale;->ROOT:Ljava/util/Locale;

    const-string v2, "ROOT"

    invoke-static {v0, v2}, Lkotlin/jvm/internal/Intrinsics;->checkNotNullExpressionValue(Ljava/lang/Object;Ljava/lang/String;)V

    invoke-virtual {p1, v0}, Ljava/lang/String;->toUpperCase(Ljava/util/Locale;)Ljava/lang/String;

    move-result-object p1

    const-string v0, "toUpperCase(...)"

    invoke-static {p1, v0}, Lkotlin/jvm/internal/Intrinsics;->checkNotNullExpressionValue(Ljava/lang/Object;Ljava/lang/String;)V

    invoke-static {p1, p2}, Lkotlin/jvm/internal/Intrinsics;->areEqual(Ljava/lang/Object;Ljava/lang/Object;)Z

    move-result p1

    if-eqz p1, :cond_0

    .line 50
    sget-object p1, Lcom/listpro/xyon/pro/HostCardEmulatorService;->groupData:Ljava/lang/String;

    if-eqz p1, :cond_0

    .line 52
    const-string p2, "en"

    invoke-static {p2, p1}, Landroid/nfc/NdefRecord;->createTextRecord(Ljava/lang/String;Ljava/lang/String;)Landroid/nfc/NdefRecord;

    move-result-object p1

    .line 53
    new-instance p2, Landroid/nfc/NdefMessage;

    const/4 v0, 0x1

    new-array v2, v0, [Landroid/nfc/NdefRecord;

    aput-object p1, v2, v1

    invoke-direct {p2, v2}, Landroid/nfc/NdefMessage;-><init>([Landroid/nfc/NdefRecord;)V

    .line 54
    invoke-virtual {p2}, Landroid/nfc/NdefMessage;->toByteArray()[B

    move-result-object p1

    .line 58
    array-length p2, p1

    add-int/lit8 p2, p2, 0x4

    new-array p2, p2, [B

    .line 61
    array-length v2, p1

    div-int/lit16 v2, v2, 0x100

    int-to-byte v2, v2

    aput-byte v2, p2, v1

    .line 62
    array-length v2, p1

    rem-int/lit16 v2, v2, 0x100

    int-to-byte v2, v2

    aput-byte v2, p2, v0

    .line 65
    array-length v0, p1

    const/4 v2, 0x2

    invoke-static {p1, v1, p2, v2, v0}, Ljava/lang/System;->arraycopy(Ljava/lang/Object;ILjava/lang/Object;II)V

    .line 68
    array-length v0, p1

    add-int/2addr v0, v2

    const/16 v2, -0x70

    aput-byte v2, p2, v0

    .line 69
    array-length p1, p1

    add-int/lit8 p1, p1, 0x3

    aput-byte v1, p2, p1

    return-object p2

    .line 75
    :cond_0
    sget-object p1, Lcom/listpro/xyon/pro/HostCardEmulatorService;->UNKNOWN_CMD_SW:[B

    return-object p1
.end method
