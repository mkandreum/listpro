package com.google.android.material.carousel;

import android.view.View;
import androidx.core.math.MathUtils;
import androidx.recyclerview.widget.RecyclerView;

/* loaded from: classes.dex */
public final class MultiBrowseCarouselStrategy extends CarouselStrategy {
    private int keylineCount = 0;
    private static final int[] SMALL_COUNTS = {1};
    private static final int[] MEDIUM_COUNTS = {1, 0};

    @Override // com.google.android.material.carousel.CarouselStrategy
    KeylineState onFirstChildMeasuredWithMargins(Carousel carousel, View view) {
        float containerHeight = carousel.getContainerHeight();
        if (carousel.isHorizontal()) {
            containerHeight = carousel.getContainerWidth();
        }
        float f = containerHeight;
        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) view.getLayoutParams();
        float f2 = layoutParams.topMargin + layoutParams.bottomMargin;
        float measuredHeight = view.getMeasuredHeight();
        if (carousel.isHorizontal()) {
            f2 = layoutParams.leftMargin + layoutParams.rightMargin;
            measuredHeight = view.getMeasuredWidth();
        }
        float f3 = f2;
        float smallItemSizeMin = getSmallItemSizeMin() + f3;
        float max = Math.max(getSmallItemSizeMax() + f3, smallItemSizeMin);
        float min = Math.min(measuredHeight + f3, f);
        float clamp = MathUtils.clamp((measuredHeight / 3.0f) + f3, smallItemSizeMin + f3, max + f3);
        float f4 = (min + clamp) / 2.0f;
        int[] iArr = SMALL_COUNTS;
        if (f < 2.0f * smallItemSizeMin) {
            iArr = new int[]{0};
        }
        int[] iArr2 = MEDIUM_COUNTS;
        if (carousel.getCarouselAlignment() == 1) {
            iArr = doubleCounts(iArr);
            iArr2 = doubleCounts(iArr2);
        }
        int[] iArr3 = iArr2;
        int[] iArr4 = iArr;
        int max2 = (int) Math.max(1.0d, Math.floor(((f - (CarouselStrategyHelper.maxValue(iArr3) * f4)) - (CarouselStrategyHelper.maxValue(iArr4) * max)) / min));
        int ceil = (int) Math.ceil(f / min);
        int i = (ceil - max2) + 1;
        int[] iArr5 = new int[i];
        for (int i2 = 0; i2 < i; i2++) {
            iArr5[i2] = ceil - i2;
        }
        Arrangement findLowestCostArrangement = Arrangement.findLowestCostArrangement(f, clamp, smallItemSizeMin, max, iArr4, f4, iArr3, min, iArr5);
        this.keylineCount = findLowestCostArrangement.getItemCount();
        if (ensureArrangementFitsItemCount(findLowestCostArrangement, carousel.getItemCount())) {
            findLowestCostArrangement = Arrangement.findLowestCostArrangement(f, clamp, smallItemSizeMin, max, new int[]{findLowestCostArrangement.smallCount}, f4, new int[]{findLowestCostArrangement.mediumCount}, min, new int[]{findLowestCostArrangement.largeCount});
        }
        return CarouselStrategyHelper.createKeylineState(view.getContext(), f3, f, findLowestCostArrangement, carousel.getCarouselAlignment());
    }

    boolean ensureArrangementFitsItemCount(Arrangement arrangement, int i) {
        int itemCount = arrangement.getItemCount() - i;
        boolean z = itemCount > 0 && (arrangement.smallCount > 0 || arrangement.mediumCount > 1);
        while (itemCount > 0) {
            if (arrangement.smallCount > 0) {
                arrangement.smallCount--;
            } else if (arrangement.mediumCount > 1) {
                arrangement.mediumCount--;
            }
            itemCount--;
        }
        return z;
    }

    @Override // com.google.android.material.carousel.CarouselStrategy
    boolean shouldRefreshKeylineState(Carousel carousel, int i) {
        if (i >= this.keylineCount || carousel.getItemCount() < this.keylineCount) {
            return i >= this.keylineCount && carousel.getItemCount() < this.keylineCount;
        }
        return true;
    }
}
