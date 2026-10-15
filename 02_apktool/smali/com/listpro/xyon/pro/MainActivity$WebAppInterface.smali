.class public final Lcom/listpro/xyon/pro/MainActivity$WebAppInterface;
.super Ljava/lang/Object;
.source "MainActivity.kt"


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcom/listpro/xyon/pro/MainActivity;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x11
    name = "WebAppInterface"
.end annotation

.annotation runtime Lkotlin/Metadata;
    d1 = {
        "\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\u0008\u0003\n\u0002\u0010\u0008\n\u0000\n\u0002\u0010\u000e\n\u0002\u0008\u0003\n\u0002\u0010\u0002\n\u0002\u0008\u0003\u0008\u0086\u0004\u0018\u00002\u00020\u0001B\u0007\u00a2\u0006\u0004\u0008\u0002\u0010\u0003J\u0008\u0010\u0004\u001a\u00020\u0005H\u0007J\u0018\u0010\u0006\u001a\u00020\u00072\u0006\u0010\u0008\u001a\u00020\u00052\u0006\u0010\t\u001a\u00020\u0005H\u0007J\u0010\u0010\n\u001a\u00020\u000b2\u0006\u0010\u000c\u001a\u00020\u0007H\u0007J\u0008\u0010\r\u001a\u00020\u000bH\u0007\u00a8\u0006\u000e"
    }
    d2 = {
        "Lcom/listpro/xyon/pro/MainActivity$WebAppInterface;",
        "",
        "<init>",
        "(Lcom/listpro/xyon/pro/MainActivity;)V",
        "getProductCount",
        "",
        "getProductChunk",
        "",
        "offset",
        "limit",
        "saveBackup",
        "",
        "jsonData",
        "requestBackupFile",
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


# instance fields
.field final synthetic this$0:Lcom/listpro/xyon/pro/MainActivity;


# direct methods
.method public constructor <init>(Lcom/listpro/xyon/pro/MainActivity;)V
    .locals 0
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "()V"
        }
    .end annotation

    .line 71
    iput-object p1, p0, Lcom/listpro/xyon/pro/MainActivity$WebAppInterface;->this$0:Lcom/listpro/xyon/pro/MainActivity;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public final getProductChunk(II)Ljava/lang/String;
    .locals 1
    .annotation runtime Landroid/webkit/JavascriptInterface;
    .end annotation

    .line 77
    iget-object v0, p0, Lcom/listpro/xyon/pro/MainActivity$WebAppInterface;->this$0:Lcom/listpro/xyon/pro/MainActivity;

    invoke-static {v0}, Lcom/listpro/xyon/pro/MainActivity;->access$getProductLines$p(Lcom/listpro/xyon/pro/MainActivity;)Ljava/util/List;

    move-result-object v0

    invoke-interface {v0}, Ljava/util/List;->size()I

    move-result v0

    if-lt p1, v0, :cond_0

    const-string p1, "[]"

    return-object p1

    :cond_0
    add-int/2addr p2, p1

    .line 78
    iget-object v0, p0, Lcom/listpro/xyon/pro/MainActivity$WebAppInterface;->this$0:Lcom/listpro/xyon/pro/MainActivity;

    invoke-static {v0}, Lcom/listpro/xyon/pro/MainActivity;->access$getProductLines$p(Lcom/listpro/xyon/pro/MainActivity;)Ljava/util/List;

    move-result-object v0

    invoke-interface {v0}, Ljava/util/List;->size()I

    move-result v0

    invoke-static {p2, v0}, Lkotlin/ranges/RangesKt;->coerceAtMost(II)I

    move-result p2

    .line 79
    iget-object v0, p0, Lcom/listpro/xyon/pro/MainActivity$WebAppInterface;->this$0:Lcom/listpro/xyon/pro/MainActivity;

    invoke-static {v0}, Lcom/listpro/xyon/pro/MainActivity;->access$getProductLines$p(Lcom/listpro/xyon/pro/MainActivity;)Ljava/util/List;

    move-result-object v0

    invoke-interface {v0, p1, p2}, Ljava/util/List;->subList(II)Ljava/util/List;

    move-result-object p1

    .line 80
    iget-object p2, p0, Lcom/listpro/xyon/pro/MainActivity$WebAppInterface;->this$0:Lcom/listpro/xyon/pro/MainActivity;

    invoke-static {p2}, Lcom/listpro/xyon/pro/MainActivity;->access$getCsvHeaders$p(Lcom/listpro/xyon/pro/MainActivity;)Ljava/util/List;

    move-result-object v0

    invoke-static {p2, p1, v0}, Lcom/listpro/xyon/pro/MainActivity;->access$convertChunkToJson(Lcom/listpro/xyon/pro/MainActivity;Ljava/util/List;Ljava/util/List;)Ljava/lang/String;

    move-result-object p1

    return-object p1
.end method

.method public final getProductCount()I
    .locals 1
    .annotation runtime Landroid/webkit/JavascriptInterface;
    .end annotation

    .line 73
    iget-object v0, p0, Lcom/listpro/xyon/pro/MainActivity$WebAppInterface;->this$0:Lcom/listpro/xyon/pro/MainActivity;

    invoke-static {v0}, Lcom/listpro/xyon/pro/MainActivity;->access$getProductLines$p(Lcom/listpro/xyon/pro/MainActivity;)Ljava/util/List;

    move-result-object v0

    invoke-interface {v0}, Ljava/util/List;->size()I

    move-result v0

    return v0
.end method

.method public final requestBackupFile()V
    .locals 3
    .annotation runtime Landroid/webkit/JavascriptInterface;
    .end annotation

    .line 97
    new-instance v0, Landroid/content/Intent;

    const-string v1, "android.intent.action.OPEN_DOCUMENT"

    invoke-direct {v0, v1}, Landroid/content/Intent;-><init>(Ljava/lang/String;)V

    .line 98
    const-string v1, "android.intent.category.OPENABLE"

    invoke-virtual {v0, v1}, Landroid/content/Intent;->addCategory(Ljava/lang/String;)Landroid/content/Intent;

    .line 99
    const-string v1, "application/json"

    invoke-virtual {v0, v1}, Landroid/content/Intent;->setType(Ljava/lang/String;)Landroid/content/Intent;

    .line 101
    iget-object v1, p0, Lcom/listpro/xyon/pro/MainActivity$WebAppInterface;->this$0:Lcom/listpro/xyon/pro/MainActivity;

    invoke-static {v1}, Lcom/listpro/xyon/pro/MainActivity;->access$getPICK_FILE_REQUEST_CODE$p(Lcom/listpro/xyon/pro/MainActivity;)I

    move-result v2

    invoke-virtual {v1, v0, v2}, Lcom/listpro/xyon/pro/MainActivity;->startActivityForResult(Landroid/content/Intent;I)V

    return-void
.end method

.method public final saveBackup(Ljava/lang/String;)V
    .locals 3
    .annotation runtime Landroid/webkit/JavascriptInterface;
    .end annotation

    const-string v0, "jsonData"

    invoke-static {p1, v0}, Lkotlin/jvm/internal/Intrinsics;->checkNotNullParameter(Ljava/lang/Object;Ljava/lang/String;)V

    .line 85
    new-instance v0, Landroid/content/Intent;

    const-string v1, "android.intent.action.CREATE_DOCUMENT"

    invoke-direct {v0, v1}, Landroid/content/Intent;-><init>(Ljava/lang/String;)V

    .line 86
    const-string v1, "android.intent.category.OPENABLE"

    invoke-virtual {v0, v1}, Landroid/content/Intent;->addCategory(Ljava/lang/String;)Landroid/content/Intent;

    .line 87
    const-string v1, "application/json"

    invoke-virtual {v0, v1}, Landroid/content/Intent;->setType(Ljava/lang/String;)Landroid/content/Intent;

    .line 88
    const-string v1, "android.intent.extra.TITLE"

    const-string v2, "listpro_backup.json"

    invoke-virtual {v0, v1, v2}, Landroid/content/Intent;->putExtra(Ljava/lang/String;Ljava/lang/String;)Landroid/content/Intent;

    .line 90
    iget-object v1, p0, Lcom/listpro/xyon/pro/MainActivity$WebAppInterface;->this$0:Lcom/listpro/xyon/pro/MainActivity;

    invoke-static {v1}, Lcom/listpro/xyon/pro/MainActivity;->access$getCREATE_FILE_REQUEST_CODE$p(Lcom/listpro/xyon/pro/MainActivity;)I

    move-result v2

    invoke-virtual {v1, v0, v2}, Lcom/listpro/xyon/pro/MainActivity;->startActivityForResult(Landroid/content/Intent;I)V

    .line 92
    iget-object v0, p0, Lcom/listpro/xyon/pro/MainActivity$WebAppInterface;->this$0:Lcom/listpro/xyon/pro/MainActivity;

    const-string v1, "backup"

    const/4 v2, 0x0

    invoke-virtual {v0, v1, v2}, Lcom/listpro/xyon/pro/MainActivity;->getSharedPreferences(Ljava/lang/String;I)Landroid/content/SharedPreferences;

    move-result-object v0

    invoke-interface {v0}, Landroid/content/SharedPreferences;->edit()Landroid/content/SharedPreferences$Editor;

    move-result-object v0

    const-string v1, "data"

    invoke-interface {v0, v1, p1}, Landroid/content/SharedPreferences$Editor;->putString(Ljava/lang/String;Ljava/lang/String;)Landroid/content/SharedPreferences$Editor;

    move-result-object p1

    invoke-interface {p1}, Landroid/content/SharedPreferences$Editor;->apply()V

    return-void
.end method
