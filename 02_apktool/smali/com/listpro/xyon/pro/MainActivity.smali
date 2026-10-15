.class public final Lcom/listpro/xyon/pro/MainActivity;
.super Landroidx/appcompat/app/AppCompatActivity;
.source "MainActivity.kt"


# annotations
.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lcom/listpro/xyon/pro/MainActivity$WebAppInterface;
    }
.end annotation

.annotation system Ldalvik/annotation/SourceDebugExtension;
    value = "SMAP\nMainActivity.kt\nKotlin\n*S Kotlin\n*F\n+ 1 MainActivity.kt\ncom/listpro/xyon/pro/MainActivity\n+ 2 fake.kt\nkotlin/jvm/internal/FakeKt\n+ 3 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,183:1\n1#2:184\n1#2:195\n1611#3,9:185\n1863#3:194\n1864#3:196\n1620#3:197\n1557#3:198\n1628#3,3:199\n*S KotlinDebug\n*F\n+ 1 MainActivity.kt\ncom/listpro/xyon/pro/MainActivity\n*L\n163#1:195\n163#1:185,9\n163#1:194\n163#1:196\n163#1:197\n60#1:198\n60#1:199,3\n*E\n"
.end annotation

.annotation runtime Lkotlin/Metadata;
    d1 = {
        "\u0000D\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0008\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0002\u0008\u0002\n\u0002\u0010\u0008\n\u0002\u0008\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0008\u0005\n\u0002\u0018\u0002\n\u0002\u0008\u0002\n\u0002\u0018\u0002\n\u0002\u0008\u0007\u0018\u00002\u00020\u0001:\u0001\u001fB\u0007\u00a2\u0006\u0004\u0008\u0002\u0010\u0003J\u0012\u0010\r\u001a\u00020\u000e2\u0008\u0010\u000f\u001a\u0004\u0018\u00010\u0010H\u0014J\u0008\u0010\u0011\u001a\u00020\u000eH\u0002J\"\u0010\u0012\u001a\u00020\u000e2\u0006\u0010\u0013\u001a\u00020\u000b2\u0006\u0010\u0014\u001a\u00020\u000b2\u0008\u0010\u0015\u001a\u0004\u0018\u00010\u0016H\u0014J\u0018\u0010\u0017\u001a\u00020\u000e2\u0006\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u0008H\u0002J\u0010\u0010\u001b\u001a\u00020\u00082\u0006\u0010\u0018\u001a\u00020\u0019H\u0002J$\u0010\u001c\u001a\u00020\u00082\u000c\u0010\u001d\u001a\u0008\u0012\u0004\u0012\u00020\u00080\u00072\u000c\u0010\u001e\u001a\u0008\u0012\u0004\u0012\u00020\u00080\u0007H\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082.\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0006\u001a\u0008\u0012\u0004\u0012\u00020\u00080\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\t\u001a\u0008\u0012\u0004\u0012\u00020\u00080\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000c\u001a\u00020\u000bX\u0082D\u00a2\u0006\u0002\n\u0000\u00a8\u0006 "
    }
    d2 = {
        "Lcom/listpro/xyon/pro/MainActivity;",
        "Landroidx/appcompat/app/AppCompatActivity;",
        "<init>",
        "()V",
        "webView",
        "Landroid/webkit/WebView;",
        "productLines",
        "",
        "",
        "csvHeaders",
        "CREATE_FILE_REQUEST_CODE",
        "",
        "PICK_FILE_REQUEST_CODE",
        "onCreate",
        "",
        "savedInstanceState",
        "Landroid/os/Bundle;",
        "loadCsvDataToMemory",
        "onActivityResult",
        "requestCode",
        "resultCode",
        "data",
        "Landroid/content/Intent;",
        "writeTextToUri",
        "uri",
        "Landroid/net/Uri;",
        "text",
        "readTextFromUri",
        "convertChunkToJson",
        "chunk",
        "headers",
        "WebAppInterface",
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
.field private final CREATE_FILE_REQUEST_CODE:I

.field private final PICK_FILE_REQUEST_CODE:I

.field private csvHeaders:Ljava/util/List;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/List<",
            "Ljava/lang/String;",
            ">;"
        }
    .end annotation
.end field

.field private productLines:Ljava/util/List;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/List<",
            "Ljava/lang/String;",
            ">;"
        }
    .end annotation
.end field

.field private webView:Landroid/webkit/WebView;


# direct methods
.method public static synthetic $r8$lambda$H3XkkeKZRqUa0M5nGX2ObKxgAaM(Lcom/listpro/xyon/pro/MainActivity;)V
    .locals 0

    invoke-static {p0}, Lcom/listpro/xyon/pro/MainActivity;->loadCsvDataToMemory$lambda$3(Lcom/listpro/xyon/pro/MainActivity;)V

    return-void
.end method

.method public constructor <init>()V
    .locals 1

    .line 15
    invoke-direct {p0}, Landroidx/appcompat/app/AppCompatActivity;-><init>()V

    .line 18
    invoke-static {}, Lkotlin/collections/CollectionsKt;->emptyList()Ljava/util/List;

    move-result-object v0

    iput-object v0, p0, Lcom/listpro/xyon/pro/MainActivity;->productLines:Ljava/util/List;

    .line 19
    invoke-static {}, Lkotlin/collections/CollectionsKt;->emptyList()Ljava/util/List;

    move-result-object v0

    iput-object v0, p0, Lcom/listpro/xyon/pro/MainActivity;->csvHeaders:Ljava/util/List;

    const/4 v0, 0x1

    .line 22
    iput v0, p0, Lcom/listpro/xyon/pro/MainActivity;->CREATE_FILE_REQUEST_CODE:I

    const/4 v0, 0x2

    .line 23
    iput v0, p0, Lcom/listpro/xyon/pro/MainActivity;->PICK_FILE_REQUEST_CODE:I

    return-void
.end method

.method public static final synthetic access$convertChunkToJson(Lcom/listpro/xyon/pro/MainActivity;Ljava/util/List;Ljava/util/List;)Ljava/lang/String;
    .locals 0

    .line 15
    invoke-direct {p0, p1, p2}, Lcom/listpro/xyon/pro/MainActivity;->convertChunkToJson(Ljava/util/List;Ljava/util/List;)Ljava/lang/String;

    move-result-object p0

    return-object p0
.end method

.method public static final synthetic access$getCREATE_FILE_REQUEST_CODE$p(Lcom/listpro/xyon/pro/MainActivity;)I
    .locals 0

    .line 15
    iget p0, p0, Lcom/listpro/xyon/pro/MainActivity;->CREATE_FILE_REQUEST_CODE:I

    return p0
.end method

.method public static final synthetic access$getCsvHeaders$p(Lcom/listpro/xyon/pro/MainActivity;)Ljava/util/List;
    .locals 0

    .line 15
    iget-object p0, p0, Lcom/listpro/xyon/pro/MainActivity;->csvHeaders:Ljava/util/List;

    return-object p0
.end method

.method public static final synthetic access$getPICK_FILE_REQUEST_CODE$p(Lcom/listpro/xyon/pro/MainActivity;)I
    .locals 0

    .line 15
    iget p0, p0, Lcom/listpro/xyon/pro/MainActivity;->PICK_FILE_REQUEST_CODE:I

    return p0
.end method

.method public static final synthetic access$getProductLines$p(Lcom/listpro/xyon/pro/MainActivity;)Ljava/util/List;
    .locals 0

    .line 15
    iget-object p0, p0, Lcom/listpro/xyon/pro/MainActivity;->productLines:Ljava/util/List;

    return-object p0
.end method

.method public static final synthetic access$getWebView$p(Lcom/listpro/xyon/pro/MainActivity;)Landroid/webkit/WebView;
    .locals 0

    .line 15
    iget-object p0, p0, Lcom/listpro/xyon/pro/MainActivity;->webView:Landroid/webkit/WebView;

    return-object p0
.end method

.method private final convertChunkToJson(Ljava/util/List;Ljava/util/List;)Ljava/lang/String;
    .locals 28
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Ljava/util/List<",
            "Ljava/lang/String;",
            ">;",
            "Ljava/util/List<",
            "Ljava/lang/String;",
            ">;)",
            "Ljava/lang/String;"
        }
    .end annotation

    move-object/from16 v0, p2

    .line 156
    const-string v1, "id"

    invoke-interface {v0, v1}, Ljava/util/List;->indexOf(Ljava/lang/Object;)I

    move-result v1

    .line 157
    const-string v2, "Category"

    invoke-interface {v0, v2}, Ljava/util/List;->indexOf(Ljava/lang/Object;)I

    move-result v2

    .line 158
    const-string v3, "name"

    invoke-interface {v0, v3}, Ljava/util/List;->indexOf(Ljava/lang/Object;)I

    move-result v3

    .line 159
    const-string v4, "subtitle"

    invoke-interface {v0, v4}, Ljava/util/List;->indexOf(Ljava/lang/Object;)I

    move-result v4

    .line 160
    const-string v5, "price"

    invoke-interface {v0, v5}, Ljava/util/List;->indexOf(Ljava/lang/Object;)I

    move-result v5

    .line 161
    const-string v6, "main_image_url"

    invoke-interface {v0, v6}, Ljava/util/List;->indexOf(Ljava/lang/Object;)I

    move-result v6

    .line 163
    move-object/from16 v7, p1

    check-cast v7, Ljava/lang/Iterable;

    .line 185
    new-instance v8, Ljava/util/ArrayList;

    invoke-direct {v8}, Ljava/util/ArrayList;-><init>()V

    check-cast v8, Ljava/util/Collection;

    .line 194
    invoke-interface {v7}, Ljava/lang/Iterable;->iterator()Ljava/util/Iterator;

    move-result-object v7

    :goto_0
    invoke-interface {v7}, Ljava/util/Iterator;->hasNext()Z

    move-result v9

    if-eqz v9, :cond_c

    invoke-interface {v7}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v9

    .line 193
    check-cast v9, Ljava/lang/String;

    .line 164
    check-cast v9, Ljava/lang/CharSequence;

    new-instance v10, Lkotlin/text/Regex;

    const-string v11, ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"

    invoke-direct {v10, v11}, Lkotlin/text/Regex;-><init>(Ljava/lang/String;)V

    const/4 v11, 0x0

    invoke-virtual {v10, v9, v11}, Lkotlin/text/Regex;->split(Ljava/lang/CharSequence;I)Ljava/util/List;

    move-result-object v9

    .line 165
    invoke-interface {v9}, Ljava/util/List;->size()I

    move-result v10

    invoke-interface {v0}, Ljava/util/List;->size()I

    move-result v11

    const/4 v12, 0x0

    if-ge v10, v11, :cond_1

    :cond_0
    move/from16 v21, v1

    goto/16 :goto_7

    .line 167
    :cond_1
    invoke-static {v9, v1}, Lkotlin/collections/CollectionsKt;->getOrNull(Ljava/util/List;I)Ljava/lang/Object;

    move-result-object v10

    check-cast v10, Ljava/lang/String;

    if-eqz v10, :cond_0

    check-cast v10, Ljava/lang/CharSequence;

    invoke-static {v10}, Lkotlin/text/StringsKt;->trim(Ljava/lang/CharSequence;)Ljava/lang/CharSequence;

    move-result-object v10

    invoke-virtual {v10}, Ljava/lang/Object;->toString()Ljava/lang/String;

    move-result-object v10

    if-eqz v10, :cond_0

    invoke-static {v10}, Lkotlin/text/StringsKt;->toIntOrNull(Ljava/lang/String;)Ljava/lang/Integer;

    move-result-object v10

    if-eqz v10, :cond_0

    invoke-virtual {v10}, Ljava/lang/Integer;->intValue()I

    move-result v10

    .line 168
    invoke-static {v9, v2}, Lkotlin/collections/CollectionsKt;->getOrNull(Ljava/util/List;I)Ljava/lang/Object;

    move-result-object v11

    check-cast v11, Ljava/lang/String;

    const-string v12, ""

    if-eqz v11, :cond_3

    check-cast v11, Ljava/lang/CharSequence;

    invoke-static {v11}, Lkotlin/text/StringsKt;->trim(Ljava/lang/CharSequence;)Ljava/lang/CharSequence;

    move-result-object v11

    invoke-virtual {v11}, Ljava/lang/Object;->toString()Ljava/lang/String;

    move-result-object v13

    if-eqz v13, :cond_3

    const/16 v17, 0x4

    const/16 v18, 0x0

    const-string v14, "\""

    const-string v15, ""

    const/16 v16, 0x0

    invoke-static/range {v13 .. v18}, Lkotlin/text/StringsKt;->replace$default(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZILjava/lang/Object;)Ljava/lang/String;

    move-result-object v11

    if-nez v11, :cond_2

    goto :goto_1

    :cond_2
    move-object v13, v11

    goto :goto_2

    :cond_3
    :goto_1
    move-object v13, v12

    .line 169
    :goto_2
    invoke-static {v9, v3}, Lkotlin/collections/CollectionsKt;->getOrNull(Ljava/util/List;I)Ljava/lang/Object;

    move-result-object v11

    check-cast v11, Ljava/lang/String;

    if-eqz v11, :cond_5

    check-cast v11, Ljava/lang/CharSequence;

    invoke-static {v11}, Lkotlin/text/StringsKt;->trim(Ljava/lang/CharSequence;)Ljava/lang/CharSequence;

    move-result-object v11

    invoke-virtual {v11}, Ljava/lang/Object;->toString()Ljava/lang/String;

    move-result-object v14

    if-eqz v14, :cond_5

    const/16 v18, 0x4

    const/16 v19, 0x0

    const-string v15, "\""

    const-string v16, ""

    const/16 v17, 0x0

    invoke-static/range {v14 .. v19}, Lkotlin/text/StringsKt;->replace$default(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZILjava/lang/Object;)Ljava/lang/String;

    move-result-object v11

    if-nez v11, :cond_4

    goto :goto_3

    :cond_4
    move-object v14, v11

    goto :goto_4

    :cond_5
    :goto_3
    move-object v14, v12

    .line 170
    :goto_4
    invoke-static {v9, v4}, Lkotlin/collections/CollectionsKt;->getOrNull(Ljava/util/List;I)Ljava/lang/Object;

    move-result-object v11

    check-cast v11, Ljava/lang/String;

    if-eqz v11, :cond_6

    check-cast v11, Ljava/lang/CharSequence;

    invoke-static {v11}, Lkotlin/text/StringsKt;->trim(Ljava/lang/CharSequence;)Ljava/lang/CharSequence;

    move-result-object v11

    invoke-virtual {v11}, Ljava/lang/Object;->toString()Ljava/lang/String;

    move-result-object v15

    if-eqz v15, :cond_6

    const/16 v19, 0x4

    const/16 v20, 0x0

    const-string v16, "\""

    const-string v17, ""

    const/16 v18, 0x0

    invoke-static/range {v15 .. v20}, Lkotlin/text/StringsKt;->replace$default(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZILjava/lang/Object;)Ljava/lang/String;

    move-result-object v11

    if-nez v11, :cond_7

    :cond_6
    move-object v11, v12

    .line 171
    :cond_7
    invoke-static {v9, v5}, Lkotlin/collections/CollectionsKt;->getOrNull(Ljava/util/List;I)Ljava/lang/Object;

    move-result-object v15

    check-cast v15, Ljava/lang/String;

    if-eqz v15, :cond_8

    check-cast v15, Ljava/lang/CharSequence;

    invoke-static {v15}, Lkotlin/text/StringsKt;->trim(Ljava/lang/CharSequence;)Ljava/lang/CharSequence;

    move-result-object v15

    invoke-virtual {v15}, Ljava/lang/Object;->toString()Ljava/lang/String;

    move-result-object v15

    if-eqz v15, :cond_8

    invoke-static {v15}, Lkotlin/text/StringsKt;->toDoubleOrNull(Ljava/lang/String;)Ljava/lang/Double;

    move-result-object v15

    if-eqz v15, :cond_8

    invoke-virtual {v15}, Ljava/lang/Double;->doubleValue()D

    move-result-wide v15

    goto :goto_5

    :cond_8
    const-wide/16 v15, 0x0

    :goto_5
    move/from16 v21, v1

    move-wide v0, v15

    .line 172
    invoke-static {v9, v6}, Lkotlin/collections/CollectionsKt;->getOrNull(Ljava/util/List;I)Ljava/lang/Object;

    move-result-object v9

    check-cast v9, Ljava/lang/String;

    if-eqz v9, :cond_a

    check-cast v9, Ljava/lang/CharSequence;

    invoke-static {v9}, Lkotlin/text/StringsKt;->trim(Ljava/lang/CharSequence;)Ljava/lang/CharSequence;

    move-result-object v9

    invoke-virtual {v9}, Ljava/lang/Object;->toString()Ljava/lang/String;

    move-result-object v9

    if-nez v9, :cond_9

    goto :goto_6

    :cond_9
    move-object v12, v9

    :cond_a
    :goto_6
    const/16 v18, 0x4

    const/16 v19, 0x0

    .line 174
    const-string v15, "\\"

    const-string v16, "\\\\"

    const/16 v17, 0x0

    invoke-static/range {v14 .. v19}, Lkotlin/text/StringsKt;->replace$default(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZILjava/lang/Object;)Ljava/lang/String;

    move-result-object v22

    const/16 v26, 0x4

    const/16 v27, 0x0

    const-string v23, "\""

    const-string v24, "\\\""

    const/16 v25, 0x0

    invoke-static/range {v22 .. v27}, Lkotlin/text/StringsKt;->replace$default(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZILjava/lang/Object;)Ljava/lang/String;

    move-result-object v9

    const/16 v19, 0x4

    const/16 v20, 0x0

    .line 175
    const-string v16, "\\"

    const-string v17, "\\\\"

    const/16 v18, 0x0

    move-object v15, v11

    invoke-static/range {v15 .. v20}, Lkotlin/text/StringsKt;->replace$default(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZILjava/lang/Object;)Ljava/lang/String;

    move-result-object v22

    const-string v23, "\""

    const-string v24, "\\\""

    invoke-static/range {v22 .. v27}, Lkotlin/text/StringsKt;->replace$default(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZILjava/lang/Object;)Ljava/lang/String;

    move-result-object v11

    const/16 v17, 0x4

    const/16 v18, 0x0

    .line 176
    const-string v14, "\\"

    const-string v15, "\\\\"

    const/16 v16, 0x0

    invoke-static/range {v13 .. v18}, Lkotlin/text/StringsKt;->replace$default(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZILjava/lang/Object;)Ljava/lang/String;

    move-result-object v22

    const-string v23, "\""

    const-string v24, "\\\""

    invoke-static/range {v22 .. v27}, Lkotlin/text/StringsKt;->replace$default(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZILjava/lang/Object;)Ljava/lang/String;

    move-result-object v13

    .line 178
    new-instance v14, Ljava/lang/StringBuilder;

    const-string v15, "{\"id\":"

    invoke-direct {v14, v15}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {v14, v10}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v10

    const-string v14, ",\"category\":\""

    invoke-virtual {v10, v14}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v10

    invoke-virtual {v10, v13}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v10

    const-string v13, "\",\"name\":\""

    invoke-virtual {v10, v13}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v10

    invoke-virtual {v10, v9}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v9

    const-string v10, "\",\"brand\":\""

    invoke-virtual {v9, v10}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v9

    invoke-virtual {v9, v11}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v9

    const-string v10, "\",\"price\":"

    invoke-virtual {v9, v10}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v9

    invoke-virtual {v9, v0, v1}, Ljava/lang/StringBuilder;->append(D)Ljava/lang/StringBuilder;

    move-result-object v0

    const-string v1, ",\"imageUrl\":\""

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v0

    invoke-virtual {v0, v12}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v0

    const-string v1, "\"}"

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v0

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v12

    :goto_7
    if-eqz v12, :cond_b

    .line 193
    invoke-interface {v8, v12}, Ljava/util/Collection;->add(Ljava/lang/Object;)Z

    :cond_b
    move-object/from16 v0, p2

    move/from16 v1, v21

    goto/16 :goto_0

    .line 197
    :cond_c
    check-cast v8, Ljava/util/List;

    .line 185
    move-object v9, v8

    check-cast v9, Ljava/lang/Iterable;

    .line 179
    const-string v0, ","

    move-object v10, v0

    check-cast v10, Ljava/lang/CharSequence;

    const/16 v16, 0x3e

    const/16 v17, 0x0

    const/4 v11, 0x0

    const/4 v12, 0x0

    const/4 v13, 0x0

    const/4 v14, 0x0

    const/4 v15, 0x0

    invoke-static/range {v9 .. v17}, Lkotlin/collections/CollectionsKt;->joinToString$default(Ljava/lang/Iterable;Ljava/lang/CharSequence;Ljava/lang/CharSequence;Ljava/lang/CharSequence;ILjava/lang/CharSequence;Lkotlin/jvm/functions/Function1;ILjava/lang/Object;)Ljava/lang/String;

    move-result-object v0

    .line 181
    new-instance v1, Ljava/lang/StringBuilder;

    const-string v2, "["

    invoke-direct {v1, v2}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {v1, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v0

    const-string v1, "]"

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v0

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    return-object v0
.end method

.method private final loadCsvDataToMemory()V
    .locals 2

    .line 54
    new-instance v0, Ljava/lang/Thread;

    .line 68
    new-instance v1, Lcom/listpro/xyon/pro/MainActivity$$ExternalSyntheticLambda0;

    invoke-direct {v1, p0}, Lcom/listpro/xyon/pro/MainActivity$$ExternalSyntheticLambda0;-><init>(Lcom/listpro/xyon/pro/MainActivity;)V

    .line 54
    invoke-direct {v0, v1}, Ljava/lang/Thread;-><init>(Ljava/lang/Runnable;)V

    .line 68
    invoke-virtual {v0}, Ljava/lang/Thread;->start()V

    return-void
.end method

.method private static final loadCsvDataToMemory$lambda$3(Lcom/listpro/xyon/pro/MainActivity;)V
    .locals 12

    .line 56
    :try_start_0
    invoke-virtual {p0}, Lcom/listpro/xyon/pro/MainActivity;->getAssets()Landroid/content/res/AssetManager;

    move-result-object v0

    const-string v1, "products_macro.csv"

    invoke-virtual {v0, v1}, Landroid/content/res/AssetManager;->open(Ljava/lang/String;)Ljava/io/InputStream;

    move-result-object v0

    move-object v1, v0

    check-cast v1, Ljava/io/Closeable;
    :try_end_0
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_0} :catch_0

    :try_start_1
    move-object v0, v1

    check-cast v0, Ljava/io/InputStream;

    .line 57
    new-instance v2, Ljava/io/BufferedReader;

    new-instance v3, Ljava/io/InputStreamReader;

    const-string v4, "UTF-8"

    invoke-static {v4}, Ljava/nio/charset/Charset;->forName(Ljava/lang/String;)Ljava/nio/charset/Charset;

    move-result-object v4

    invoke-direct {v3, v0, v4}, Ljava/io/InputStreamReader;-><init>(Ljava/io/InputStream;Ljava/nio/charset/Charset;)V

    check-cast v3, Ljava/io/Reader;

    invoke-direct {v2, v3}, Ljava/io/BufferedReader;-><init>(Ljava/io/Reader;)V

    check-cast v2, Ljava/io/Closeable;
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_2

    :try_start_2
    move-object v0, v2

    check-cast v0, Ljava/io/BufferedReader;

    .line 58
    check-cast v0, Ljava/io/Reader;

    invoke-static {v0}, Lkotlin/io/TextStreamsKt;->readLines(Ljava/io/Reader;)Ljava/util/List;

    move-result-object v0

    .line 59
    invoke-interface {v0}, Ljava/util/List;->size()I

    move-result v3

    const/4 v4, 0x1

    if-le v3, v4, :cond_1

    const/4 v3, 0x0

    .line 60
    invoke-interface {v0, v3}, Ljava/util/List;->get(I)Ljava/lang/Object;

    move-result-object v5

    move-object v6, v5

    check-cast v6, Ljava/lang/CharSequence;

    new-array v7, v4, [C

    const/16 v5, 0x2c

    aput-char v5, v7, v3

    const/4 v10, 0x6

    const/4 v11, 0x0

    const/4 v8, 0x0

    const/4 v9, 0x0

    invoke-static/range {v6 .. v11}, Lkotlin/text/StringsKt;->split$default(Ljava/lang/CharSequence;[CZIILjava/lang/Object;)Ljava/util/List;

    move-result-object v3

    check-cast v3, Ljava/lang/Iterable;

    .line 198
    new-instance v5, Ljava/util/ArrayList;

    const/16 v6, 0xa

    invoke-static {v3, v6}, Lkotlin/collections/CollectionsKt;->collectionSizeOrDefault(Ljava/lang/Iterable;I)I

    move-result v6

    invoke-direct {v5, v6}, Ljava/util/ArrayList;-><init>(I)V

    check-cast v5, Ljava/util/Collection;

    .line 199
    invoke-interface {v3}, Ljava/lang/Iterable;->iterator()Ljava/util/Iterator;

    move-result-object v3

    :goto_0
    invoke-interface {v3}, Ljava/util/Iterator;->hasNext()Z

    move-result v6

    if-eqz v6, :cond_0

    invoke-interface {v3}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v6

    .line 200
    check-cast v6, Ljava/lang/String;

    .line 60
    check-cast v6, Ljava/lang/CharSequence;

    invoke-static {v6}, Lkotlin/text/StringsKt;->trim(Ljava/lang/CharSequence;)Ljava/lang/CharSequence;

    move-result-object v6

    invoke-virtual {v6}, Ljava/lang/Object;->toString()Ljava/lang/String;

    move-result-object v6

    .line 200
    invoke-interface {v5, v6}, Ljava/util/Collection;->add(Ljava/lang/Object;)Z

    goto :goto_0

    .line 201
    :cond_0
    check-cast v5, Ljava/util/List;

    .line 60
    iput-object v5, p0, Lcom/listpro/xyon/pro/MainActivity;->csvHeaders:Ljava/util/List;

    .line 61
    check-cast v0, Ljava/lang/Iterable;

    invoke-static {v0, v4}, Lkotlin/collections/CollectionsKt;->drop(Ljava/lang/Iterable;I)Ljava/util/List;

    move-result-object v0

    iput-object v0, p0, Lcom/listpro/xyon/pro/MainActivity;->productLines:Ljava/util/List;

    .line 63
    :cond_1
    sget-object p0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    :try_end_2
    .catchall {:try_start_2 .. :try_end_2} :catchall_0

    const/4 p0, 0x0

    .line 57
    :try_start_3
    invoke-static {v2, p0}, Lkotlin/io/CloseableKt;->closeFinally(Ljava/io/Closeable;Ljava/lang/Throwable;)V

    .line 64
    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    :try_end_3
    .catchall {:try_start_3 .. :try_end_3} :catchall_2

    .line 56
    :try_start_4
    invoke-static {v1, p0}, Lkotlin/io/CloseableKt;->closeFinally(Ljava/io/Closeable;Ljava/lang/Throwable;)V
    :try_end_4
    .catch Ljava/lang/Exception; {:try_start_4 .. :try_end_4} :catch_0

    return-void

    :catchall_0
    move-exception v0

    move-object p0, v0

    .line 57
    :try_start_5
    throw p0
    :try_end_5
    .catchall {:try_start_5 .. :try_end_5} :catchall_1

    :catchall_1
    move-exception v0

    :try_start_6
    invoke-static {v2, p0}, Lkotlin/io/CloseableKt;->closeFinally(Ljava/io/Closeable;Ljava/lang/Throwable;)V

    throw v0
    :try_end_6
    .catchall {:try_start_6 .. :try_end_6} :catchall_2

    :catchall_2
    move-exception v0

    move-object p0, v0

    .line 56
    :try_start_7
    throw p0
    :try_end_7
    .catchall {:try_start_7 .. :try_end_7} :catchall_3

    :catchall_3
    move-exception v0

    :try_start_8
    invoke-static {v1, p0}, Lkotlin/io/CloseableKt;->closeFinally(Ljava/io/Closeable;Ljava/lang/Throwable;)V

    throw v0
    :try_end_8
    .catch Ljava/lang/Exception; {:try_start_8 .. :try_end_8} :catch_0

    :catch_0
    move-exception v0

    move-object p0, v0

    .line 66
    invoke-virtual {p0}, Ljava/lang/Exception;->printStackTrace()V

    return-void
.end method

.method private final readTextFromUri(Landroid/net/Uri;)Ljava/lang/String;
    .locals 4

    .line 139
    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    .line 141
    :try_start_0
    invoke-virtual {p0}, Lcom/listpro/xyon/pro/MainActivity;->getContentResolver()Landroid/content/ContentResolver;

    move-result-object v1

    invoke-virtual {v1, p1}, Landroid/content/ContentResolver;->openInputStream(Landroid/net/Uri;)Ljava/io/InputStream;

    move-result-object p1

    if-eqz p1, :cond_1

    check-cast p1, Ljava/io/Closeable;
    :try_end_0
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_0} :catch_0

    :try_start_1
    move-object v1, p1

    check-cast v1, Ljava/io/InputStream;

    .line 142
    new-instance v2, Ljava/io/BufferedReader;

    new-instance v3, Ljava/io/InputStreamReader;

    invoke-direct {v3, v1}, Ljava/io/InputStreamReader;-><init>(Ljava/io/InputStream;)V

    check-cast v3, Ljava/io/Reader;

    invoke-direct {v2, v3}, Ljava/io/BufferedReader;-><init>(Ljava/io/Reader;)V

    check-cast v2, Ljava/io/Closeable;
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_2

    :try_start_2
    move-object v1, v2

    check-cast v1, Ljava/io/BufferedReader;

    .line 144
    :goto_0
    invoke-virtual {v1}, Ljava/io/BufferedReader;->readLine()Ljava/lang/String;

    move-result-object v3

    if-eqz v3, :cond_0

    .line 145
    invoke-virtual {v0, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    goto :goto_0

    .line 147
    :cond_0
    sget-object v1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    :try_end_2
    .catchall {:try_start_2 .. :try_end_2} :catchall_0

    const/4 v1, 0x0

    .line 142
    :try_start_3
    invoke-static {v2, v1}, Lkotlin/io/CloseableKt;->closeFinally(Ljava/io/Closeable;Ljava/lang/Throwable;)V

    .line 148
    sget-object v2, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    :try_end_3
    .catchall {:try_start_3 .. :try_end_3} :catchall_2

    .line 141
    :try_start_4
    invoke-static {p1, v1}, Lkotlin/io/CloseableKt;->closeFinally(Ljava/io/Closeable;Ljava/lang/Throwable;)V
    :try_end_4
    .catch Ljava/lang/Exception; {:try_start_4 .. :try_end_4} :catch_0

    goto :goto_1

    :catchall_0
    move-exception v1

    .line 142
    :try_start_5
    throw v1
    :try_end_5
    .catchall {:try_start_5 .. :try_end_5} :catchall_1

    :catchall_1
    move-exception v3

    :try_start_6
    invoke-static {v2, v1}, Lkotlin/io/CloseableKt;->closeFinally(Ljava/io/Closeable;Ljava/lang/Throwable;)V

    throw v3
    :try_end_6
    .catchall {:try_start_6 .. :try_end_6} :catchall_2

    :catchall_2
    move-exception v1

    .line 141
    :try_start_7
    throw v1
    :try_end_7
    .catchall {:try_start_7 .. :try_end_7} :catchall_3

    :catchall_3
    move-exception v2

    :try_start_8
    invoke-static {p1, v1}, Lkotlin/io/CloseableKt;->closeFinally(Ljava/io/Closeable;Ljava/lang/Throwable;)V

    throw v2
    :try_end_8
    .catch Ljava/lang/Exception; {:try_start_8 .. :try_end_8} :catch_0

    :catch_0
    move-exception p1

    .line 150
    invoke-virtual {p1}, Ljava/lang/Exception;->printStackTrace()V

    .line 152
    :cond_1
    :goto_1
    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    const-string v0, "toString(...)"

    invoke-static {p1, v0}, Lkotlin/jvm/internal/Intrinsics;->checkNotNullExpressionValue(Ljava/lang/Object;Ljava/lang/String;)V

    return-object p1
.end method

.method private final writeTextToUri(Landroid/net/Uri;Ljava/lang/String;)V
    .locals 2

    .line 130
    :try_start_0
    invoke-virtual {p0}, Lcom/listpro/xyon/pro/MainActivity;->getContentResolver()Landroid/content/ContentResolver;

    move-result-object v0

    invoke-virtual {v0, p1}, Landroid/content/ContentResolver;->openOutputStream(Landroid/net/Uri;)Ljava/io/OutputStream;

    move-result-object p1

    if-eqz p1, :cond_0

    check-cast p1, Ljava/io/Closeable;
    :try_end_0
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_0} :catch_0

    :try_start_1
    move-object v0, p1

    check-cast v0, Ljava/io/OutputStream;

    .line 131
    sget-object v1, Lkotlin/text/Charsets;->UTF_8:Ljava/nio/charset/Charset;

    invoke-virtual {p2, v1}, Ljava/lang/String;->getBytes(Ljava/nio/charset/Charset;)[B

    move-result-object p2

    const-string v1, "getBytes(...)"

    invoke-static {p2, v1}, Lkotlin/jvm/internal/Intrinsics;->checkNotNullExpressionValue(Ljava/lang/Object;Ljava/lang/String;)V

    invoke-virtual {v0, p2}, Ljava/io/OutputStream;->write([B)V

    .line 132
    sget-object p2, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    const/4 p2, 0x0

    .line 130
    :try_start_2
    invoke-static {p1, p2}, Lkotlin/io/CloseableKt;->closeFinally(Ljava/io/Closeable;Ljava/lang/Throwable;)V
    :try_end_2
    .catch Ljava/lang/Exception; {:try_start_2 .. :try_end_2} :catch_0

    return-void

    :catchall_0
    move-exception p2

    :try_start_3
    throw p2
    :try_end_3
    .catchall {:try_start_3 .. :try_end_3} :catchall_1

    :catchall_1
    move-exception v0

    :try_start_4
    invoke-static {p1, p2}, Lkotlin/io/CloseableKt;->closeFinally(Ljava/io/Closeable;Ljava/lang/Throwable;)V

    throw v0
    :try_end_4
    .catch Ljava/lang/Exception; {:try_start_4 .. :try_end_4} :catch_0

    :cond_0
    return-void

    :catch_0
    move-exception p1

    .line 134
    invoke-virtual {p1}, Ljava/lang/Exception;->printStackTrace()V

    return-void
.end method


# virtual methods
.method protected onActivityResult(IILandroid/content/Intent;)V
    .locals 7

    .line 106
    invoke-super {p0, p1, p2, p3}, Landroidx/appcompat/app/AppCompatActivity;->onActivityResult(IILandroid/content/Intent;)V

    const/4 v0, -0x1

    if-ne p2, v0, :cond_2

    .line 109
    iget p2, p0, Lcom/listpro/xyon/pro/MainActivity;->CREATE_FILE_REQUEST_CODE:I

    const/4 v0, 0x0

    if-ne p1, p2, :cond_0

    if-eqz p3, :cond_2

    .line 110
    invoke-virtual {p3}, Landroid/content/Intent;->getData()Landroid/net/Uri;

    move-result-object p1

    if-eqz p1, :cond_2

    .line 111
    const-string p2, "backup"

    const/4 p3, 0x0

    invoke-virtual {p0, p2, p3}, Lcom/listpro/xyon/pro/MainActivity;->getSharedPreferences(Ljava/lang/String;I)Landroid/content/SharedPreferences;

    move-result-object p2

    const-string p3, "data"

    invoke-interface {p2, p3, v0}, Landroid/content/SharedPreferences;->getString(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    move-result-object p2

    if-eqz p2, :cond_2

    .line 113
    invoke-direct {p0, p1, p2}, Lcom/listpro/xyon/pro/MainActivity;->writeTextToUri(Landroid/net/Uri;Ljava/lang/String;)V

    return-void

    .line 117
    :cond_0
    iget p2, p0, Lcom/listpro/xyon/pro/MainActivity;->PICK_FILE_REQUEST_CODE:I

    if-ne p1, p2, :cond_2

    if-eqz p3, :cond_2

    .line 118
    invoke-virtual {p3}, Landroid/content/Intent;->getData()Landroid/net/Uri;

    move-result-object p1

    if-eqz p1, :cond_2

    .line 119
    invoke-direct {p0, p1}, Lcom/listpro/xyon/pro/MainActivity;->readTextFromUri(Landroid/net/Uri;)Ljava/lang/String;

    move-result-object v1

    const/4 v5, 0x4

    const/4 v6, 0x0

    .line 120
    const-string v2, "\'"

    const-string v3, "\\\'"

    const/4 v4, 0x0

    invoke-static/range {v1 .. v6}, Lkotlin/text/StringsKt;->replace$default(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZILjava/lang/Object;)Ljava/lang/String;

    move-result-object p1

    .line 121
    iget-object p2, p0, Lcom/listpro/xyon/pro/MainActivity;->webView:Landroid/webkit/WebView;

    if-nez p2, :cond_1

    const-string p2, "webView"

    invoke-static {p2}, Lkotlin/jvm/internal/Intrinsics;->throwUninitializedPropertyAccessException(Ljava/lang/String;)V

    move-object p2, v0

    :cond_1
    new-instance p3, Ljava/lang/StringBuilder;

    const-string v1, "javascript:onBackupFileLoaded(\'"

    invoke-direct {p3, v1}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {p3, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object p1

    const-string p3, "\');"

    invoke-virtual {p1, p3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object p1

    invoke-virtual {p1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    invoke-virtual {p2, p1, v0}, Landroid/webkit/WebView;->evaluateJavascript(Ljava/lang/String;Landroid/webkit/ValueCallback;)V

    :cond_2
    return-void
.end method

.method protected onCreate(Landroid/os/Bundle;)V
    .locals 4

    .line 26
    invoke-super {p0, p1}, Landroidx/appcompat/app/AppCompatActivity;->onCreate(Landroid/os/Bundle;)V

    .line 27
    sget p1, Lcom/listpro/xyon/pro/R$layout;->activity_main:I

    invoke-virtual {p0, p1}, Lcom/listpro/xyon/pro/MainActivity;->setContentView(I)V

    .line 29
    sget p1, Lcom/listpro/xyon/pro/R$id;->webView:I

    invoke-virtual {p0, p1}, Lcom/listpro/xyon/pro/MainActivity;->findViewById(I)Landroid/view/View;

    move-result-object p1

    check-cast p1, Landroid/webkit/WebView;

    iput-object p1, p0, Lcom/listpro/xyon/pro/MainActivity;->webView:Landroid/webkit/WebView;

    const/4 v0, 0x0

    .line 31
    const-string v1, "webView"

    if-nez p1, :cond_0

    invoke-static {v1}, Lkotlin/jvm/internal/Intrinsics;->throwUninitializedPropertyAccessException(Ljava/lang/String;)V

    move-object p1, v0

    :cond_0
    invoke-virtual {p1}, Landroid/webkit/WebView;->getSettings()Landroid/webkit/WebSettings;

    move-result-object p1

    const/4 v2, 0x1

    invoke-virtual {p1, v2}, Landroid/webkit/WebSettings;->setJavaScriptEnabled(Z)V

    .line 32
    invoke-static {v2}, Landroid/webkit/WebView;->setWebContentsDebuggingEnabled(Z)V

    .line 36
    iget-object p1, p0, Lcom/listpro/xyon/pro/MainActivity;->webView:Landroid/webkit/WebView;

    if-nez p1, :cond_1

    invoke-static {v1}, Lkotlin/jvm/internal/Intrinsics;->throwUninitializedPropertyAccessException(Ljava/lang/String;)V

    move-object p1, v0

    :cond_1
    const/4 v2, 0x0

    invoke-virtual {p1, v2}, Landroid/webkit/WebView;->setVerticalScrollBarEnabled(Z)V

    .line 37
    iget-object p1, p0, Lcom/listpro/xyon/pro/MainActivity;->webView:Landroid/webkit/WebView;

    if-nez p1, :cond_2

    invoke-static {v1}, Lkotlin/jvm/internal/Intrinsics;->throwUninitializedPropertyAccessException(Ljava/lang/String;)V

    move-object p1, v0

    :cond_2
    invoke-virtual {p1, v2}, Landroid/webkit/WebView;->setHorizontalScrollBarEnabled(Z)V

    .line 40
    iget-object p1, p0, Lcom/listpro/xyon/pro/MainActivity;->webView:Landroid/webkit/WebView;

    if-nez p1, :cond_3

    invoke-static {v1}, Lkotlin/jvm/internal/Intrinsics;->throwUninitializedPropertyAccessException(Ljava/lang/String;)V

    move-object p1, v0

    :cond_3
    new-instance v2, Lcom/listpro/xyon/pro/MainActivity$WebAppInterface;

    invoke-direct {v2, p0}, Lcom/listpro/xyon/pro/MainActivity$WebAppInterface;-><init>(Lcom/listpro/xyon/pro/MainActivity;)V

    const-string v3, "Android"

    invoke-virtual {p1, v2, v3}, Landroid/webkit/WebView;->addJavascriptInterface(Ljava/lang/Object;Ljava/lang/String;)V

    .line 42
    iget-object p1, p0, Lcom/listpro/xyon/pro/MainActivity;->webView:Landroid/webkit/WebView;

    if-nez p1, :cond_4

    invoke-static {v1}, Lkotlin/jvm/internal/Intrinsics;->throwUninitializedPropertyAccessException(Ljava/lang/String;)V

    move-object p1, v0

    :cond_4
    new-instance v2, Lcom/listpro/xyon/pro/MainActivity$onCreate$1;

    invoke-direct {v2, p0}, Lcom/listpro/xyon/pro/MainActivity$onCreate$1;-><init>(Lcom/listpro/xyon/pro/MainActivity;)V

    check-cast v2, Landroid/webkit/WebViewClient;

    invoke-virtual {p1, v2}, Landroid/webkit/WebView;->setWebViewClient(Landroid/webkit/WebViewClient;)V

    .line 49
    invoke-direct {p0}, Lcom/listpro/xyon/pro/MainActivity;->loadCsvDataToMemory()V

    .line 50
    iget-object p1, p0, Lcom/listpro/xyon/pro/MainActivity;->webView:Landroid/webkit/WebView;

    if-nez p1, :cond_5

    invoke-static {v1}, Lkotlin/jvm/internal/Intrinsics;->throwUninitializedPropertyAccessException(Ljava/lang/String;)V

    goto :goto_0

    :cond_5
    move-object v0, p1

    :goto_0
    const-string p1, "file:///android_asset/index.html"

    invoke-virtual {v0, p1}, Landroid/webkit/WebView;->loadUrl(Ljava/lang/String;)V

    return-void
.end method
