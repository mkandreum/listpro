.class public final Lcom/google/android/material/carousel/MultiBrowseCarouselStrategy;
.super Lcom/google/android/material/carousel/CarouselStrategy;
.source "MultiBrowseCarouselStrategy.java"


# static fields
.field private static final MEDIUM_COUNTS:[I

.field private static final SMALL_COUNTS:[I


# instance fields
.field private keylineCount:I


# direct methods
.method static constructor <clinit>()V
    .locals 2

    const/4 v0, 0x1

    .line 51
    filled-new-array {v0}, [I

    move-result-object v1

    sput-object v1, Lcom/google/android/material/carousel/MultiBrowseCarouselStrategy;->SMALL_COUNTS:[I

    const/4 v1, 0x0

    .line 52
    filled-new-array {v0, v1}, [I

    move-result-object v0

    sput-object v0, Lcom/google/android/material/carousel/MultiBrowseCarouselStrategy;->MEDIUM_COUNTS:[I

    return-void
.end method

.method public constructor <init>()V
    .locals 1

    .line 49
    invoke-direct {p0}, Lcom/google/android/material/carousel/CarouselStrategy;-><init>()V

    const/4 v0, 0x0

    .line 56
    iput v0, p0, Lcom/google/android/material/carousel/MultiBrowseCarouselStrategy;->keylineCount:I

    return-void
.end method


# virtual methods
.method ensureArrangementFitsItemCount(Lcom/google/android/material/carousel/Arrangement;I)Z
    .locals 3

    .line 161
    invoke-virtual {p1}, Lcom/google/android/material/carousel/Arrangement;->getItemCount()I

    move-result v0

    sub-int/2addr v0, p2

    const/4 p2, 0x1

    if-lez v0, :cond_1

    .line 162
    iget v1, p1, Lcom/google/android/material/carousel/Arrangement;->smallCount:I

    if-gtz v1, :cond_0

    iget v1, p1, Lcom/google/android/material/carousel/Arrangement;->mediumCount:I

    if-le v1, p2, :cond_1

    :cond_0
    move v1, p2

    goto :goto_0

    :cond_1
    const/4 v1, 0x0

    :goto_0
    if-lez v0, :cond_4

    .line 166
    iget v2, p1, Lcom/google/android/material/carousel/Arrangement;->smallCount:I

    if-lez v2, :cond_2

    .line 167
    iget v2, p1, Lcom/google/android/material/carousel/Arrangement;->smallCount:I

    sub-int/2addr v2, p2

    iput v2, p1, Lcom/google/android/material/carousel/Arrangement;->smallCount:I

    goto :goto_1

    .line 168
    :cond_2
    iget v2, p1, Lcom/google/android/material/carousel/Arrangement;->mediumCount:I

    if-le v2, p2, :cond_3

    .line 170
    iget v2, p1, Lcom/google/android/material/carousel/Arrangement;->mediumCount:I

    sub-int/2addr v2, p2

    iput v2, p1, Lcom/google/android/material/carousel/Arrangement;->mediumCount:I

    :cond_3
    :goto_1
    add-int/lit8 v0, v0, -0x1

    goto :goto_0

    :cond_4
    return v1
.end method

.method onFirstChildMeasuredWithMargins(Lcom/google/android/material/carousel/Carousel;Landroid/view/View;)Lcom/google/android/material/carousel/KeylineState;
    .locals 17

    move-object/from16 v0, p0

    .line 61
    invoke-interface/range {p1 .. p1}, Lcom/google/android/material/carousel/Carousel;->getContainerHeight()I

    move-result v1

    int-to-float v1, v1

    .line 62
    invoke-interface/range {p1 .. p1}, Lcom/google/android/material/carousel/Carousel;->isHorizontal()Z

    move-result v2

    if-eqz v2, :cond_0

    .line 63
    invoke-interface/range {p1 .. p1}, Lcom/google/android/material/carousel/Carousel;->getContainerWidth()I

    move-result v1

    int-to-float v1, v1

    :cond_0
    move v2, v1

    .line 66
    invoke-virtual/range {p2 .. p2}, Landroid/view/View;->getLayoutParams()Landroid/view/ViewGroup$LayoutParams;

    move-result-object v1

    check-cast v1, Landroidx/recyclerview/widget/RecyclerView$LayoutParams;

    .line 67
    iget v3, v1, Landroidx/recyclerview/widget/RecyclerView$LayoutParams;->topMargin:I

    iget v4, v1, Landroidx/recyclerview/widget/RecyclerView$LayoutParams;->bottomMargin:I

    add-int/2addr v3, v4

    int-to-float v3, v3

    .line 68
    invoke-virtual/range {p2 .. p2}, Landroid/view/View;->getMeasuredHeight()I

    move-result v4

    int-to-float v4, v4

    .line 70
    invoke-interface/range {p1 .. p1}, Lcom/google/android/material/carousel/Carousel;->isHorizontal()Z

    move-result v5

    if-eqz v5, :cond_1

    .line 71
    iget v3, v1, Landroidx/recyclerview/widget/RecyclerView$LayoutParams;->leftMargin:I

    iget v1, v1, Landroidx/recyclerview/widget/RecyclerView$LayoutParams;->rightMargin:I

    add-int/2addr v3, v1

    int-to-float v3, v3

    .line 72
    invoke-virtual/range {p2 .. p2}, Landroid/view/View;->getMeasuredWidth()I

    move-result v1

    int-to-float v4, v1

    :cond_1
    move v1, v3

    .line 75
    invoke-virtual {v0}, Lcom/google/android/material/carousel/MultiBrowseCarouselStrategy;->getSmallItemSizeMin()F

    move-result v3

    add-float/2addr v3, v1

    .line 76
    invoke-virtual {v0}, Lcom/google/android/material/carousel/MultiBrowseCarouselStrategy;->getSmallItemSizeMax()F

    move-result v5

    add-float/2addr v5, v1

    .line 77
    invoke-static {v5, v3}, Ljava/lang/Math;->max(FF)F

    move-result v5

    add-float v6, v4, v1

    .line 79
    invoke-static {v6, v2}, Ljava/lang/Math;->min(FF)F

    move-result v9

    const/high16 v6, 0x40400000    # 3.0f

    div-float/2addr v4, v6

    add-float/2addr v4, v1

    add-float v6, v3, v1

    add-float v7, v5, v1

    .line 85
    invoke-static {v4, v6, v7}, Landroidx/core/math/MathUtils;->clamp(FFF)F

    move-result v4

    add-float v6, v9, v4

    const/high16 v7, 0x40000000    # 2.0f

    div-float/2addr v6, v7

    .line 96
    sget-object v8, Lcom/google/android/material/carousel/MultiBrowseCarouselStrategy;->SMALL_COUNTS:[I

    mul-float/2addr v7, v3

    cmpg-float v7, v2, v7

    const/4 v10, 0x0

    const/4 v11, 0x1

    if-gez v7, :cond_2

    .line 101
    new-array v8, v11, [I

    aput v10, v8, v10

    .line 104
    :cond_2
    sget-object v7, Lcom/google/android/material/carousel/MultiBrowseCarouselStrategy;->MEDIUM_COUNTS:[I

    .line 105
    invoke-interface/range {p1 .. p1}, Lcom/google/android/material/carousel/Carousel;->getCarouselAlignment()I

    move-result v12

    if-ne v12, v11, :cond_3

    .line 106
    invoke-static {v8}, Lcom/google/android/material/carousel/MultiBrowseCarouselStrategy;->doubleCounts([I)[I

    move-result-object v8

    .line 107
    invoke-static {v7}, Lcom/google/android/material/carousel/MultiBrowseCarouselStrategy;->doubleCounts([I)[I

    move-result-object v7

    :cond_3
    move-object/from16 v16, v7

    move v7, v6

    move-object v6, v8

    move-object/from16 v8, v16

    .line 114
    invoke-static {v8}, Lcom/google/android/material/carousel/CarouselStrategyHelper;->maxValue([I)I

    move-result v12

    int-to-float v12, v12

    mul-float/2addr v12, v7

    sub-float v12, v2, v12

    .line 115
    invoke-static {v6}, Lcom/google/android/material/carousel/CarouselStrategyHelper;->maxValue([I)I

    move-result v13

    int-to-float v13, v13

    mul-float/2addr v13, v5

    sub-float/2addr v12, v13

    div-float/2addr v12, v9

    float-to-double v12, v12

    .line 116
    invoke-static {v12, v13}, Ljava/lang/Math;->floor(D)D

    move-result-wide v12

    const-wide/high16 v14, 0x3ff0000000000000L    # 1.0

    invoke-static {v14, v15, v12, v13}, Ljava/lang/Math;->max(DD)D

    move-result-wide v12

    double-to-int v12, v12

    div-float v13, v2, v9

    float-to-double v13, v13

    .line 117
    invoke-static {v13, v14}, Ljava/lang/Math;->ceil(D)D

    move-result-wide v13

    double-to-int v13, v13

    sub-int v12, v13, v12

    add-int/2addr v12, v11

    move v11, v10

    .line 118
    new-array v10, v12, [I

    :goto_0
    if-ge v11, v12, :cond_4

    sub-int v14, v13, v11

    .line 120
    aput v14, v10, v11

    add-int/lit8 v11, v11, 0x1

    goto :goto_0

    :cond_4
    move/from16 v16, v4

    move v4, v3

    move/from16 v3, v16

    .line 123
    invoke-static/range {v2 .. v10}, Lcom/google/android/material/carousel/Arrangement;->findLowestCostArrangement(FFFF[IF[IF[I)Lcom/google/android/material/carousel/Arrangement;

    move-result-object v6

    .line 134
    invoke-virtual {v6}, Lcom/google/android/material/carousel/Arrangement;->getItemCount()I

    move-result v8

    iput v8, v0, Lcom/google/android/material/carousel/MultiBrowseCarouselStrategy;->keylineCount:I

    .line 136
    invoke-interface/range {p1 .. p1}, Lcom/google/android/material/carousel/Carousel;->getItemCount()I

    move-result v8

    invoke-virtual {v0, v6, v8}, Lcom/google/android/material/carousel/MultiBrowseCarouselStrategy;->ensureArrangementFitsItemCount(Lcom/google/android/material/carousel/Arrangement;I)Z

    move-result v8

    if-eqz v8, :cond_5

    .line 139
    iget v8, v6, Lcom/google/android/material/carousel/Arrangement;->smallCount:I

    filled-new-array {v8}, [I

    move-result-object v8

    iget v10, v6, Lcom/google/android/material/carousel/Arrangement;->mediumCount:I

    filled-new-array {v10}, [I

    move-result-object v10

    iget v6, v6, Lcom/google/android/material/carousel/Arrangement;->largeCount:I

    filled-new-array {v6}, [I

    move-result-object v6

    move-object/from16 v16, v10

    move-object v10, v6

    move-object v6, v8

    move-object/from16 v8, v16

    .line 140
    invoke-static/range {v2 .. v10}, Lcom/google/android/material/carousel/Arrangement;->findLowestCostArrangement(FFFF[IF[IF[I)Lcom/google/android/material/carousel/Arrangement;

    move-result-object v6

    .line 153
    :cond_5
    invoke-virtual/range {p2 .. p2}, Landroid/view/View;->getContext()Landroid/content/Context;

    move-result-object v3

    .line 157
    invoke-interface/range {p1 .. p1}, Lcom/google/android/material/carousel/Carousel;->getCarouselAlignment()I

    move-result v4

    .line 152
    invoke-static {v3, v1, v2, v6, v4}, Lcom/google/android/material/carousel/CarouselStrategyHelper;->createKeylineState(Landroid/content/Context;FFLcom/google/android/material/carousel/Arrangement;I)Lcom/google/android/material/carousel/KeylineState;

    move-result-object v1

    return-object v1
.end method

.method shouldRefreshKeylineState(Lcom/google/android/material/carousel/Carousel;I)Z
    .locals 2

    .line 182
    iget v0, p0, Lcom/google/android/material/carousel/MultiBrowseCarouselStrategy;->keylineCount:I

    if-ge p2, v0, :cond_0

    invoke-interface {p1}, Lcom/google/android/material/carousel/Carousel;->getItemCount()I

    move-result v0

    iget v1, p0, Lcom/google/android/material/carousel/MultiBrowseCarouselStrategy;->keylineCount:I

    if-ge v0, v1, :cond_1

    :cond_0
    iget v0, p0, Lcom/google/android/material/carousel/MultiBrowseCarouselStrategy;->keylineCount:I

    if-lt p2, v0, :cond_2

    .line 183
    invoke-interface {p1}, Lcom/google/android/material/carousel/Carousel;->getItemCount()I

    move-result p1

    iget p2, p0, Lcom/google/android/material/carousel/MultiBrowseCarouselStrategy;->keylineCount:I

    if-ge p1, p2, :cond_2

    :cond_1
    const/4 p1, 0x1

    return p1

    :cond_2
    const/4 p1, 0x0

    return p1
.end method
