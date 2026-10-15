package androidx.constraintlayout.core.widgets;

import androidx.constraintlayout.core.LinearSystem;
import androidx.constraintlayout.core.widgets.ConstraintWidget;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/* loaded from: classes.dex */
public class Flow extends VirtualLayout {
    public static final int HORIZONTAL_ALIGN_CENTER = 2;
    public static final int HORIZONTAL_ALIGN_END = 1;
    public static final int HORIZONTAL_ALIGN_START = 0;
    public static final int VERTICAL_ALIGN_BASELINE = 3;
    public static final int VERTICAL_ALIGN_BOTTOM = 1;
    public static final int VERTICAL_ALIGN_CENTER = 2;
    public static final int VERTICAL_ALIGN_TOP = 0;
    public static final int WRAP_ALIGNED = 2;
    public static final int WRAP_CHAIN = 1;
    public static final int WRAP_CHAIN_NEW = 3;
    public static final int WRAP_NONE = 0;
    private ConstraintWidget[] mDisplayedWidgets;
    private int mHorizontalStyle = -1;
    private int mVerticalStyle = -1;
    private int mFirstHorizontalStyle = -1;
    private int mFirstVerticalStyle = -1;
    private int mLastHorizontalStyle = -1;
    private int mLastVerticalStyle = -1;
    private float mHorizontalBias = 0.5f;
    private float mVerticalBias = 0.5f;
    private float mFirstHorizontalBias = 0.5f;
    private float mFirstVerticalBias = 0.5f;
    private float mLastHorizontalBias = 0.5f;
    private float mLastVerticalBias = 0.5f;
    private int mHorizontalGap = 0;
    private int mVerticalGap = 0;
    private int mHorizontalAlign = 2;
    private int mVerticalAlign = 2;
    private int mWrapMode = 0;
    private int mMaxElementsWrap = -1;
    private int mOrientation = 0;
    private ArrayList<WidgetsList> mChainList = new ArrayList<>();
    private ConstraintWidget[] mAlignedBiggestElementsInRows = null;
    private ConstraintWidget[] mAlignedBiggestElementsInCols = null;
    private int[] mAlignedDimensions = null;
    private int mDisplayedWidgetsCount = 0;

    @Override // androidx.constraintlayout.core.widgets.HelperWidget, androidx.constraintlayout.core.widgets.ConstraintWidget
    public void copy(ConstraintWidget constraintWidget, HashMap<ConstraintWidget, ConstraintWidget> hashMap) {
        super.copy(constraintWidget, hashMap);
        Flow flow = (Flow) constraintWidget;
        this.mHorizontalStyle = flow.mHorizontalStyle;
        this.mVerticalStyle = flow.mVerticalStyle;
        this.mFirstHorizontalStyle = flow.mFirstHorizontalStyle;
        this.mFirstVerticalStyle = flow.mFirstVerticalStyle;
        this.mLastHorizontalStyle = flow.mLastHorizontalStyle;
        this.mLastVerticalStyle = flow.mLastVerticalStyle;
        this.mHorizontalBias = flow.mHorizontalBias;
        this.mVerticalBias = flow.mVerticalBias;
        this.mFirstHorizontalBias = flow.mFirstHorizontalBias;
        this.mFirstVerticalBias = flow.mFirstVerticalBias;
        this.mLastHorizontalBias = flow.mLastHorizontalBias;
        this.mLastVerticalBias = flow.mLastVerticalBias;
        this.mHorizontalGap = flow.mHorizontalGap;
        this.mVerticalGap = flow.mVerticalGap;
        this.mHorizontalAlign = flow.mHorizontalAlign;
        this.mVerticalAlign = flow.mVerticalAlign;
        this.mWrapMode = flow.mWrapMode;
        this.mMaxElementsWrap = flow.mMaxElementsWrap;
        this.mOrientation = flow.mOrientation;
    }

    public void setOrientation(int i) {
        this.mOrientation = i;
    }

    public void setFirstHorizontalStyle(int i) {
        this.mFirstHorizontalStyle = i;
    }

    public void setFirstVerticalStyle(int i) {
        this.mFirstVerticalStyle = i;
    }

    public void setLastHorizontalStyle(int i) {
        this.mLastHorizontalStyle = i;
    }

    public void setLastVerticalStyle(int i) {
        this.mLastVerticalStyle = i;
    }

    public void setHorizontalStyle(int i) {
        this.mHorizontalStyle = i;
    }

    public void setVerticalStyle(int i) {
        this.mVerticalStyle = i;
    }

    public void setHorizontalBias(float f) {
        this.mHorizontalBias = f;
    }

    public void setVerticalBias(float f) {
        this.mVerticalBias = f;
    }

    public void setFirstHorizontalBias(float f) {
        this.mFirstHorizontalBias = f;
    }

    public void setFirstVerticalBias(float f) {
        this.mFirstVerticalBias = f;
    }

    public void setLastHorizontalBias(float f) {
        this.mLastHorizontalBias = f;
    }

    public void setLastVerticalBias(float f) {
        this.mLastVerticalBias = f;
    }

    public void setHorizontalAlign(int i) {
        this.mHorizontalAlign = i;
    }

    public void setVerticalAlign(int i) {
        this.mVerticalAlign = i;
    }

    public void setWrapMode(int i) {
        this.mWrapMode = i;
    }

    public void setHorizontalGap(int i) {
        this.mHorizontalGap = i;
    }

    public void setVerticalGap(int i) {
        this.mVerticalGap = i;
    }

    public void setMaxElementsWrap(int i) {
        this.mMaxElementsWrap = i;
    }

    public float getMaxElementsWrap() {
        return this.mMaxElementsWrap;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final int getWidgetWidth(ConstraintWidget constraintWidget, int i) {
        ConstraintWidget constraintWidget2;
        if (constraintWidget == null) {
            return 0;
        }
        if (constraintWidget.getHorizontalDimensionBehaviour() != ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT) {
            constraintWidget2 = constraintWidget;
        } else {
            if (constraintWidget.mMatchConstraintDefaultWidth == 0) {
                return 0;
            }
            if (constraintWidget.mMatchConstraintDefaultWidth == 2) {
                int i2 = (int) (constraintWidget.mMatchConstraintPercentWidth * i);
                if (i2 != constraintWidget.getWidth()) {
                    constraintWidget.setMeasureRequested(true);
                    measure(constraintWidget, ConstraintWidget.DimensionBehaviour.FIXED, i2, constraintWidget.getVerticalDimensionBehaviour(), constraintWidget.getHeight());
                }
                return i2;
            }
            constraintWidget2 = constraintWidget;
            if (constraintWidget2.mMatchConstraintDefaultWidth == 1) {
                return constraintWidget2.getWidth();
            }
            if (constraintWidget2.mMatchConstraintDefaultWidth == 3) {
                return (int) ((constraintWidget2.getHeight() * constraintWidget2.mDimensionRatio) + 0.5f);
            }
        }
        return constraintWidget2.getWidth();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final int getWidgetHeight(ConstraintWidget constraintWidget, int i) {
        ConstraintWidget constraintWidget2;
        if (constraintWidget == null) {
            return 0;
        }
        if (constraintWidget.getVerticalDimensionBehaviour() != ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT) {
            constraintWidget2 = constraintWidget;
        } else {
            if (constraintWidget.mMatchConstraintDefaultHeight == 0) {
                return 0;
            }
            if (constraintWidget.mMatchConstraintDefaultHeight == 2) {
                int i2 = (int) (constraintWidget.mMatchConstraintPercentHeight * i);
                if (i2 != constraintWidget.getHeight()) {
                    constraintWidget.setMeasureRequested(true);
                    measure(constraintWidget, constraintWidget.getHorizontalDimensionBehaviour(), constraintWidget.getWidth(), ConstraintWidget.DimensionBehaviour.FIXED, i2);
                }
                return i2;
            }
            constraintWidget2 = constraintWidget;
            if (constraintWidget2.mMatchConstraintDefaultHeight == 1) {
                return constraintWidget2.getHeight();
            }
            if (constraintWidget2.mMatchConstraintDefaultHeight == 3) {
                return (int) ((constraintWidget2.getWidth() * constraintWidget2.mDimensionRatio) + 0.5f);
            }
        }
        return constraintWidget2.getHeight();
    }

    @Override // androidx.constraintlayout.core.widgets.VirtualLayout
    public void measure(int i, int i2, int i3, int i4) {
        ConstraintWidget[] constraintWidgetArr;
        if (this.mWidgetsCount > 0 && !measureChildren()) {
            setMeasure(0, 0);
            needsCallbackFromSolver(false);
            return;
        }
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int[] iArr = new int[2];
        int i5 = (i2 - paddingLeft) - paddingRight;
        int i6 = this.mOrientation;
        if (i6 == 1) {
            i5 = (i4 - paddingTop) - paddingBottom;
        }
        int i7 = i5;
        if (i6 == 0) {
            if (this.mHorizontalStyle == -1) {
                this.mHorizontalStyle = 0;
            }
            if (this.mVerticalStyle == -1) {
                this.mVerticalStyle = 0;
            }
        } else {
            if (this.mHorizontalStyle == -1) {
                this.mHorizontalStyle = 0;
            }
            if (this.mVerticalStyle == -1) {
                this.mVerticalStyle = 0;
            }
        }
        ConstraintWidget[] constraintWidgetArr2 = this.mWidgets;
        int i8 = 0;
        for (int i9 = 0; i9 < this.mWidgetsCount; i9++) {
            if (this.mWidgets[i9].getVisibility() == 8) {
                i8++;
            }
        }
        int i10 = this.mWidgetsCount;
        if (i8 > 0) {
            ConstraintWidget[] constraintWidgetArr3 = new ConstraintWidget[this.mWidgetsCount - i8];
            i10 = 0;
            int i11 = 0;
            while (i11 < this.mWidgetsCount) {
                ConstraintWidget constraintWidget = this.mWidgets[i11];
                ConstraintWidget[] constraintWidgetArr4 = constraintWidgetArr3;
                if (constraintWidget.getVisibility() != 8) {
                    constraintWidgetArr4[i10] = constraintWidget;
                    i10++;
                }
                i11++;
                constraintWidgetArr3 = constraintWidgetArr4;
            }
            constraintWidgetArr = constraintWidgetArr3;
        } else {
            constraintWidgetArr = constraintWidgetArr2;
        }
        int i12 = i10;
        this.mDisplayedWidgets = constraintWidgetArr;
        this.mDisplayedWidgetsCount = i12;
        int i13 = this.mWrapMode;
        if (i13 == 0) {
            measureNoWrap(constraintWidgetArr, i12, this.mOrientation, i7, iArr);
        } else if (i13 == 1) {
            measureChainWrap(constraintWidgetArr, i12, this.mOrientation, i7, iArr);
        } else if (i13 == 2) {
            measureAligned(constraintWidgetArr, i12, this.mOrientation, i7, iArr);
        } else if (i13 == 3) {
            measureChainWrap_new(constraintWidgetArr, i12, this.mOrientation, i7, iArr);
        }
        int i14 = iArr[0] + paddingLeft + paddingRight;
        int i15 = iArr[1] + paddingTop + paddingBottom;
        if (i == 1073741824) {
            i14 = i2;
        } else if (i == Integer.MIN_VALUE) {
            i14 = Math.min(i14, i2);
        } else if (i != 0) {
            i14 = 0;
        }
        if (i3 == 1073741824) {
            i15 = i4;
        } else if (i3 == Integer.MIN_VALUE) {
            i15 = Math.min(i15, i4);
        } else if (i3 != 0) {
            i15 = 0;
        }
        setMeasure(i14, i15);
        setWidth(i14);
        setHeight(i15);
        needsCallbackFromSolver(this.mWidgetsCount > 0);
    }

    private class WidgetsList {
        private ConstraintAnchor mBottom;
        private ConstraintAnchor mLeft;
        private int mMax;
        private int mOrientation;
        private int mPaddingBottom;
        private int mPaddingLeft;
        private int mPaddingRight;
        private int mPaddingTop;
        private ConstraintAnchor mRight;
        private ConstraintAnchor mTop;
        private ConstraintWidget biggest = null;
        int biggestDimension = 0;
        private int mWidth = 0;
        private int mHeight = 0;
        private int mStartIndex = 0;
        private int mCount = 0;
        private int mNbMatchConstraintsWidgets = 0;

        public WidgetsList(int i, ConstraintAnchor constraintAnchor, ConstraintAnchor constraintAnchor2, ConstraintAnchor constraintAnchor3, ConstraintAnchor constraintAnchor4, int i2) {
            this.mPaddingLeft = 0;
            this.mPaddingTop = 0;
            this.mPaddingRight = 0;
            this.mPaddingBottom = 0;
            this.mMax = 0;
            this.mOrientation = i;
            this.mLeft = constraintAnchor;
            this.mTop = constraintAnchor2;
            this.mRight = constraintAnchor3;
            this.mBottom = constraintAnchor4;
            this.mPaddingLeft = Flow.this.getPaddingLeft();
            this.mPaddingTop = Flow.this.getPaddingTop();
            this.mPaddingRight = Flow.this.getPaddingRight();
            this.mPaddingBottom = Flow.this.getPaddingBottom();
            this.mMax = i2;
        }

        public void setup(int i, ConstraintAnchor constraintAnchor, ConstraintAnchor constraintAnchor2, ConstraintAnchor constraintAnchor3, ConstraintAnchor constraintAnchor4, int i2, int i3, int i4, int i5, int i6) {
            this.mOrientation = i;
            this.mLeft = constraintAnchor;
            this.mTop = constraintAnchor2;
            this.mRight = constraintAnchor3;
            this.mBottom = constraintAnchor4;
            this.mPaddingLeft = i2;
            this.mPaddingTop = i3;
            this.mPaddingRight = i4;
            this.mPaddingBottom = i5;
            this.mMax = i6;
        }

        public void clear() {
            this.biggestDimension = 0;
            this.biggest = null;
            this.mWidth = 0;
            this.mHeight = 0;
            this.mStartIndex = 0;
            this.mCount = 0;
            this.mNbMatchConstraintsWidgets = 0;
        }

        public void setStartIndex(int i) {
            this.mStartIndex = i;
        }

        public int getWidth() {
            if (this.mOrientation == 0) {
                return this.mWidth - Flow.this.mHorizontalGap;
            }
            return this.mWidth;
        }

        public int getHeight() {
            if (this.mOrientation == 1) {
                return this.mHeight - Flow.this.mVerticalGap;
            }
            return this.mHeight;
        }

        public void add(ConstraintWidget constraintWidget) {
            if (this.mOrientation == 0) {
                int widgetWidth = Flow.this.getWidgetWidth(constraintWidget, this.mMax);
                if (constraintWidget.getHorizontalDimensionBehaviour() == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT) {
                    this.mNbMatchConstraintsWidgets++;
                    widgetWidth = 0;
                }
                this.mWidth += widgetWidth + (constraintWidget.getVisibility() != 8 ? Flow.this.mHorizontalGap : 0);
                int widgetHeight = Flow.this.getWidgetHeight(constraintWidget, this.mMax);
                if (this.biggest == null || this.biggestDimension < widgetHeight) {
                    this.biggest = constraintWidget;
                    this.biggestDimension = widgetHeight;
                    this.mHeight = widgetHeight;
                }
            } else {
                int widgetWidth2 = Flow.this.getWidgetWidth(constraintWidget, this.mMax);
                int widgetHeight2 = Flow.this.getWidgetHeight(constraintWidget, this.mMax);
                if (constraintWidget.getVerticalDimensionBehaviour() == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT) {
                    this.mNbMatchConstraintsWidgets++;
                    widgetHeight2 = 0;
                }
                this.mHeight += widgetHeight2 + (constraintWidget.getVisibility() != 8 ? Flow.this.mVerticalGap : 0);
                if (this.biggest == null || this.biggestDimension < widgetWidth2) {
                    this.biggest = constraintWidget;
                    this.biggestDimension = widgetWidth2;
                    this.mWidth = widgetWidth2;
                }
            }
            this.mCount++;
        }

        public void createConstraints(boolean z, int i, boolean z2) {
            ConstraintWidget constraintWidget;
            int i2;
            char c;
            float f;
            float f2;
            int i3 = this.mCount;
            for (int i4 = 0; i4 < i3 && this.mStartIndex + i4 < Flow.this.mDisplayedWidgetsCount; i4++) {
                ConstraintWidget constraintWidget2 = Flow.this.mDisplayedWidgets[this.mStartIndex + i4];
                if (constraintWidget2 != null) {
                    constraintWidget2.resetAnchors();
                }
            }
            if (i3 == 0 || this.biggest == null) {
                return;
            }
            boolean z3 = z2 && i == 0;
            int i5 = -1;
            int i6 = -1;
            for (int i7 = 0; i7 < i3; i7++) {
                int i8 = z ? (i3 - 1) - i7 : i7;
                if (this.mStartIndex + i8 >= Flow.this.mDisplayedWidgetsCount) {
                    break;
                }
                ConstraintWidget constraintWidget3 = Flow.this.mDisplayedWidgets[this.mStartIndex + i8];
                if (constraintWidget3 != null && constraintWidget3.getVisibility() == 0) {
                    if (i5 == -1) {
                        i5 = i7;
                    }
                    i6 = i7;
                }
            }
            ConstraintWidget constraintWidget4 = null;
            if (this.mOrientation == 0) {
                ConstraintWidget constraintWidget5 = this.biggest;
                constraintWidget5.setVerticalChainStyle(Flow.this.mVerticalStyle);
                int i9 = this.mPaddingTop;
                if (i > 0) {
                    i9 += Flow.this.mVerticalGap;
                }
                constraintWidget5.mTop.connect(this.mTop, i9);
                if (z2) {
                    constraintWidget5.mBottom.connect(this.mBottom, this.mPaddingBottom);
                }
                if (i > 0) {
                    this.mTop.mOwner.mBottom.connect(constraintWidget5.mTop, 0);
                }
                char c2 = 3;
                if (Flow.this.mVerticalAlign == 3 && !constraintWidget5.hasBaseline()) {
                    for (int i10 = 0; i10 < i3; i10++) {
                        int i11 = z ? (i3 - 1) - i10 : i10;
                        if (this.mStartIndex + i11 >= Flow.this.mDisplayedWidgetsCount) {
                            break;
                        }
                        constraintWidget = Flow.this.mDisplayedWidgets[this.mStartIndex + i11];
                        if (constraintWidget.hasBaseline()) {
                            break;
                        }
                    }
                }
                constraintWidget = constraintWidget5;
                int i12 = 0;
                while (i12 < i3) {
                    int i13 = z ? (i3 - 1) - i12 : i12;
                    if (this.mStartIndex + i13 >= Flow.this.mDisplayedWidgetsCount) {
                        return;
                    }
                    ConstraintWidget constraintWidget6 = Flow.this.mDisplayedWidgets[this.mStartIndex + i13];
                    if (constraintWidget6 == null) {
                        constraintWidget6 = constraintWidget4;
                        c = c2;
                    } else {
                        if (i12 == 0) {
                            i2 = 1;
                            constraintWidget6.connect(constraintWidget6.mLeft, this.mLeft, this.mPaddingLeft);
                        } else {
                            i2 = 1;
                        }
                        if (i13 == 0) {
                            int i14 = Flow.this.mHorizontalStyle;
                            float f3 = Flow.this.mHorizontalBias;
                            if (z) {
                                f3 = 1.0f - f3;
                            }
                            if (this.mStartIndex != 0 || Flow.this.mFirstHorizontalStyle == -1) {
                                if (z2 && Flow.this.mLastHorizontalStyle != -1) {
                                    i14 = Flow.this.mLastHorizontalStyle;
                                    if (z) {
                                        f2 = Flow.this.mLastHorizontalBias;
                                        f = 1.0f - f2;
                                        f3 = f;
                                    } else {
                                        f = Flow.this.mLastHorizontalBias;
                                        f3 = f;
                                    }
                                }
                            } else {
                                i14 = Flow.this.mFirstHorizontalStyle;
                                if (z) {
                                    f2 = Flow.this.mFirstHorizontalBias;
                                    f = 1.0f - f2;
                                    f3 = f;
                                } else {
                                    f = Flow.this.mFirstHorizontalBias;
                                    f3 = f;
                                }
                            }
                            constraintWidget6.setHorizontalChainStyle(i14);
                            constraintWidget6.setHorizontalBiasPercent(f3);
                        }
                        if (i12 == i3 - 1) {
                            constraintWidget6.connect(constraintWidget6.mRight, this.mRight, this.mPaddingRight);
                        }
                        if (constraintWidget4 != null) {
                            constraintWidget6.mLeft.connect(constraintWidget4.mRight, Flow.this.mHorizontalGap);
                            if (i12 == i5) {
                                constraintWidget6.mLeft.setGoneMargin(this.mPaddingLeft);
                            }
                            constraintWidget4.mRight.connect(constraintWidget6.mLeft, 0);
                            if (i12 == i6 + 1) {
                                constraintWidget4.mRight.setGoneMargin(this.mPaddingRight);
                            }
                        }
                        if (constraintWidget6 != constraintWidget5) {
                            c = 3;
                            if (Flow.this.mVerticalAlign != 3 || !constraintWidget.hasBaseline() || constraintWidget6 == constraintWidget || !constraintWidget6.hasBaseline()) {
                                int i15 = Flow.this.mVerticalAlign;
                                if (i15 == 0) {
                                    constraintWidget6.mTop.connect(constraintWidget5.mTop, 0);
                                } else if (i15 == i2) {
                                    constraintWidget6.mBottom.connect(constraintWidget5.mBottom, 0);
                                } else if (z3) {
                                    constraintWidget6.mTop.connect(this.mTop, this.mPaddingTop);
                                    constraintWidget6.mBottom.connect(this.mBottom, this.mPaddingBottom);
                                } else {
                                    constraintWidget6.mTop.connect(constraintWidget5.mTop, 0);
                                    constraintWidget6.mBottom.connect(constraintWidget5.mBottom, 0);
                                }
                            } else {
                                constraintWidget6.mBaseline.connect(constraintWidget.mBaseline, 0);
                            }
                        } else {
                            c = 3;
                        }
                    }
                    i12++;
                    c2 = c;
                    constraintWidget4 = constraintWidget6;
                }
                return;
            }
            ConstraintWidget constraintWidget7 = this.biggest;
            constraintWidget7.setHorizontalChainStyle(Flow.this.mHorizontalStyle);
            int i16 = this.mPaddingLeft;
            if (i > 0) {
                i16 += Flow.this.mHorizontalGap;
            }
            if (z) {
                constraintWidget7.mRight.connect(this.mRight, i16);
                if (z2) {
                    constraintWidget7.mLeft.connect(this.mLeft, this.mPaddingRight);
                }
                if (i > 0) {
                    this.mRight.mOwner.mLeft.connect(constraintWidget7.mRight, 0);
                }
            } else {
                constraintWidget7.mLeft.connect(this.mLeft, i16);
                if (z2) {
                    constraintWidget7.mRight.connect(this.mRight, this.mPaddingRight);
                }
                if (i > 0) {
                    this.mLeft.mOwner.mRight.connect(constraintWidget7.mLeft, 0);
                }
            }
            for (int i17 = 0; i17 < i3 && this.mStartIndex + i17 < Flow.this.mDisplayedWidgetsCount; i17++) {
                ConstraintWidget constraintWidget8 = Flow.this.mDisplayedWidgets[this.mStartIndex + i17];
                if (constraintWidget8 != null) {
                    if (i17 == 0) {
                        constraintWidget8.connect(constraintWidget8.mTop, this.mTop, this.mPaddingTop);
                        int i18 = Flow.this.mVerticalStyle;
                        float f4 = Flow.this.mVerticalBias;
                        if (this.mStartIndex != 0 || Flow.this.mFirstVerticalStyle == -1) {
                            if (z2 && Flow.this.mLastVerticalStyle != -1) {
                                i18 = Flow.this.mLastVerticalStyle;
                                f4 = Flow.this.mLastVerticalBias;
                            }
                        } else {
                            i18 = Flow.this.mFirstVerticalStyle;
                            f4 = Flow.this.mFirstVerticalBias;
                        }
                        constraintWidget8.setVerticalChainStyle(i18);
                        constraintWidget8.setVerticalBiasPercent(f4);
                    }
                    if (i17 == i3 - 1) {
                        constraintWidget8.connect(constraintWidget8.mBottom, this.mBottom, this.mPaddingBottom);
                    }
                    if (constraintWidget4 != null) {
                        constraintWidget8.mTop.connect(constraintWidget4.mBottom, Flow.this.mVerticalGap);
                        if (i17 == i5) {
                            constraintWidget8.mTop.setGoneMargin(this.mPaddingTop);
                        }
                        constraintWidget4.mBottom.connect(constraintWidget8.mTop, 0);
                        if (i17 == i6 + 1) {
                            constraintWidget4.mBottom.setGoneMargin(this.mPaddingBottom);
                        }
                    }
                    if (constraintWidget8 != constraintWidget7) {
                        if (z) {
                            int i19 = Flow.this.mHorizontalAlign;
                            if (i19 == 0) {
                                constraintWidget8.mRight.connect(constraintWidget7.mRight, 0);
                            } else if (i19 == 1) {
                                constraintWidget8.mLeft.connect(constraintWidget7.mLeft, 0);
                            } else if (i19 == 2) {
                                constraintWidget8.mLeft.connect(constraintWidget7.mLeft, 0);
                                constraintWidget8.mRight.connect(constraintWidget7.mRight, 0);
                            }
                        } else {
                            int i20 = Flow.this.mHorizontalAlign;
                            if (i20 == 0) {
                                constraintWidget8.mLeft.connect(constraintWidget7.mLeft, 0);
                            } else if (i20 == 1) {
                                constraintWidget8.mRight.connect(constraintWidget7.mRight, 0);
                            } else if (i20 == 2) {
                                if (z3) {
                                    constraintWidget8.mLeft.connect(this.mLeft, this.mPaddingLeft);
                                    constraintWidget8.mRight.connect(this.mRight, this.mPaddingRight);
                                } else {
                                    constraintWidget8.mLeft.connect(constraintWidget7.mLeft, 0);
                                    constraintWidget8.mRight.connect(constraintWidget7.mRight, 0);
                                }
                            }
                            constraintWidget4 = constraintWidget8;
                        }
                    }
                    constraintWidget4 = constraintWidget8;
                }
            }
        }

        public void measureMatchConstraints(int i) {
            int i2 = this.mNbMatchConstraintsWidgets;
            if (i2 == 0) {
                return;
            }
            int i3 = this.mCount;
            int i4 = i / i2;
            for (int i5 = 0; i5 < i3 && this.mStartIndex + i5 < Flow.this.mDisplayedWidgetsCount; i5++) {
                ConstraintWidget constraintWidget = Flow.this.mDisplayedWidgets[this.mStartIndex + i5];
                if (this.mOrientation == 0) {
                    if (constraintWidget != null && constraintWidget.getHorizontalDimensionBehaviour() == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT && constraintWidget.mMatchConstraintDefaultWidth == 0) {
                        Flow.this.measure(constraintWidget, ConstraintWidget.DimensionBehaviour.FIXED, i4, constraintWidget.getVerticalDimensionBehaviour(), constraintWidget.getHeight());
                    }
                } else if (constraintWidget != null && constraintWidget.getVerticalDimensionBehaviour() == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT && constraintWidget.mMatchConstraintDefaultHeight == 0) {
                    int i6 = i4;
                    Flow.this.measure(constraintWidget, constraintWidget.getHorizontalDimensionBehaviour(), constraintWidget.getWidth(), ConstraintWidget.DimensionBehaviour.FIXED, i6);
                    i4 = i6;
                }
            }
            recomputeDimensions();
        }

        private void recomputeDimensions() {
            this.mWidth = 0;
            this.mHeight = 0;
            this.biggest = null;
            this.biggestDimension = 0;
            int i = this.mCount;
            for (int i2 = 0; i2 < i && this.mStartIndex + i2 < Flow.this.mDisplayedWidgetsCount; i2++) {
                ConstraintWidget constraintWidget = Flow.this.mDisplayedWidgets[this.mStartIndex + i2];
                if (this.mOrientation != 0) {
                    int widgetWidth = Flow.this.getWidgetWidth(constraintWidget, this.mMax);
                    int widgetHeight = Flow.this.getWidgetHeight(constraintWidget, this.mMax);
                    int i3 = Flow.this.mVerticalGap;
                    if (constraintWidget.getVisibility() == 8) {
                        i3 = 0;
                    }
                    this.mHeight += widgetHeight + i3;
                    if (this.biggest == null || this.biggestDimension < widgetWidth) {
                        this.biggest = constraintWidget;
                        this.biggestDimension = widgetWidth;
                        this.mWidth = widgetWidth;
                    }
                } else {
                    int width = constraintWidget.getWidth();
                    int i4 = Flow.this.mHorizontalGap;
                    if (constraintWidget.getVisibility() == 8) {
                        i4 = 0;
                    }
                    this.mWidth += width + i4;
                    int widgetHeight2 = Flow.this.getWidgetHeight(constraintWidget, this.mMax);
                    if (this.biggest == null || this.biggestDimension < widgetHeight2) {
                        this.biggest = constraintWidget;
                        this.biggestDimension = widgetHeight2;
                        this.mHeight = widgetHeight2;
                    }
                }
            }
        }
    }

    private void measureChainWrap(ConstraintWidget[] constraintWidgetArr, int i, int i2, int i3, int[] iArr) {
        int i4;
        Flow flow;
        int i5;
        ConstraintAnchor constraintAnchor;
        int i6;
        Flow flow2 = this;
        if (i == 0) {
            return;
        }
        flow2.mChainList.clear();
        int i7 = i3;
        WidgetsList widgetsList = flow2.new WidgetsList(i2, flow2.mLeft, flow2.mTop, flow2.mRight, flow2.mBottom, i7);
        flow2.mChainList.add(widgetsList);
        if (i2 == 0) {
            i4 = 0;
            int i8 = 0;
            int i9 = 0;
            while (i9 < i) {
                ConstraintWidget constraintWidget = constraintWidgetArr[i9];
                int widgetWidth = flow2.getWidgetWidth(constraintWidget, i7);
                if (constraintWidget.getHorizontalDimensionBehaviour() == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT) {
                    i4++;
                }
                int i10 = i4;
                boolean z = (i8 == i7 || (flow2.mHorizontalGap + i8) + widgetWidth > i7) && widgetsList.biggest != null;
                if (!z && i9 > 0 && (i6 = flow2.mMaxElementsWrap) > 0 && i9 % i6 == 0) {
                    z = true;
                }
                if (z) {
                    widgetsList = flow2.new WidgetsList(i2, flow2.mLeft, flow2.mTop, flow2.mRight, flow2.mBottom, i7);
                    widgetsList.setStartIndex(i9);
                    flow2.mChainList.add(widgetsList);
                } else if (i9 > 0) {
                    i8 += flow2.mHorizontalGap + widgetWidth;
                    widgetsList.add(constraintWidget);
                    i9++;
                    i4 = i10;
                }
                i8 = widgetWidth;
                widgetsList.add(constraintWidget);
                i9++;
                i4 = i10;
            }
        } else {
            i4 = 0;
            int i11 = 0;
            int i12 = 0;
            while (i12 < i) {
                ConstraintWidget constraintWidget2 = constraintWidgetArr[i12];
                int widgetHeight = flow2.getWidgetHeight(constraintWidget2, i7);
                if (constraintWidget2.getVerticalDimensionBehaviour() == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT) {
                    i4++;
                }
                int i13 = i4;
                boolean z2 = (i11 == i7 || (flow2.mVerticalGap + i11) + widgetHeight > i7) && widgetsList.biggest != null;
                if (!z2 && i12 > 0 && (i5 = flow2.mMaxElementsWrap) > 0 && i12 % i5 == 0) {
                    z2 = true;
                }
                if (z2) {
                    widgetsList = flow2.new WidgetsList(i2, flow2.mLeft, flow2.mTop, flow2.mRight, flow2.mBottom, i7);
                    flow = flow2;
                    widgetsList.setStartIndex(i12);
                    flow.mChainList.add(widgetsList);
                } else {
                    flow = flow2;
                    if (i12 > 0) {
                        i11 += flow.mVerticalGap + widgetHeight;
                        widgetsList.add(constraintWidget2);
                        i12++;
                        i7 = i3;
                        i4 = i13;
                        flow2 = flow;
                    }
                }
                i11 = widgetHeight;
                widgetsList.add(constraintWidget2);
                i12++;
                i7 = i3;
                i4 = i13;
                flow2 = flow;
            }
        }
        Flow flow3 = flow2;
        int size = flow3.mChainList.size();
        ConstraintAnchor constraintAnchor2 = flow3.mLeft;
        ConstraintAnchor constraintAnchor3 = flow3.mTop;
        ConstraintAnchor constraintAnchor4 = flow3.mRight;
        ConstraintAnchor constraintAnchor5 = flow3.mBottom;
        int paddingLeft = flow3.getPaddingLeft();
        int paddingTop = flow3.getPaddingTop();
        int paddingRight = flow3.getPaddingRight();
        int paddingBottom = flow3.getPaddingBottom();
        boolean z3 = flow3.getHorizontalDimensionBehaviour() == ConstraintWidget.DimensionBehaviour.WRAP_CONTENT || flow3.getVerticalDimensionBehaviour() == ConstraintWidget.DimensionBehaviour.WRAP_CONTENT;
        if (i4 > 0 && z3) {
            for (int i14 = 0; i14 < size; i14++) {
                WidgetsList widgetsList2 = flow3.mChainList.get(i14);
                if (i2 == 0) {
                    widgetsList2.measureMatchConstraints(i3 - widgetsList2.getWidth());
                } else {
                    widgetsList2.measureMatchConstraints(i3 - widgetsList2.getHeight());
                }
            }
        }
        ConstraintAnchor constraintAnchor6 = constraintAnchor2;
        int i15 = paddingBottom;
        int i16 = 0;
        int i17 = paddingRight;
        int i18 = paddingTop;
        int i19 = paddingLeft;
        ConstraintAnchor constraintAnchor7 = constraintAnchor5;
        ConstraintAnchor constraintAnchor8 = constraintAnchor4;
        ConstraintAnchor constraintAnchor9 = constraintAnchor3;
        int i20 = 0;
        for (int i21 = 0; i21 < size; i21++) {
            WidgetsList widgetsList3 = flow3.mChainList.get(i21);
            if (i2 == 0) {
                if (i21 < size - 1) {
                    constraintAnchor7 = flow3.mChainList.get(i21 + 1).biggest.mTop;
                    i15 = 0;
                } else {
                    constraintAnchor7 = flow3.mBottom;
                    i15 = flow3.getPaddingBottom();
                }
                ConstraintAnchor constraintAnchor10 = widgetsList3.biggest.mBottom;
                int i22 = i20;
                widgetsList3.setup(i2, constraintAnchor6, constraintAnchor9, constraintAnchor8, constraintAnchor7, i19, i18, i17, i15, i3);
                int max = Math.max(i16, widgetsList3.getWidth());
                int height = widgetsList3.getHeight() + i22;
                if (i21 > 0) {
                    height += flow3.mVerticalGap;
                }
                i20 = height;
                i16 = max;
                constraintAnchor9 = constraintAnchor10;
                i18 = 0;
            } else {
                int i23 = i16;
                int i24 = i20;
                if (i21 < size - 1) {
                    constraintAnchor = flow3.mChainList.get(i21 + 1).biggest.mLeft;
                    i17 = 0;
                } else {
                    constraintAnchor = flow3.mRight;
                    i17 = flow3.getPaddingRight();
                }
                constraintAnchor8 = constraintAnchor;
                ConstraintAnchor constraintAnchor11 = widgetsList3.biggest.mRight;
                widgetsList3.setup(i2, constraintAnchor6, constraintAnchor9, constraintAnchor8, constraintAnchor7, i19, i18, i17, i15, i3);
                int width = widgetsList3.getWidth() + i23;
                int max2 = Math.max(i24, widgetsList3.getHeight());
                if (i21 > 0) {
                    width += flow3.mHorizontalGap;
                }
                int i25 = width;
                i20 = max2;
                i16 = i25;
                i19 = 0;
                constraintAnchor6 = constraintAnchor11;
            }
        }
        iArr[0] = i16;
        iArr[1] = i20;
    }

    private void measureChainWrap_new(ConstraintWidget[] constraintWidgetArr, int i, int i2, int i3, int[] iArr) {
        int i4;
        Flow flow;
        int i5;
        boolean z;
        ConstraintAnchor constraintAnchor;
        int i6;
        Flow flow2 = this;
        if (i == 0) {
            return;
        }
        flow2.mChainList.clear();
        int i7 = i3;
        WidgetsList widgetsList = flow2.new WidgetsList(i2, flow2.mLeft, flow2.mTop, flow2.mRight, flow2.mBottom, i7);
        flow2.mChainList.add(widgetsList);
        boolean z2 = true;
        if (i2 == 0) {
            int i8 = 0;
            i4 = 0;
            int i9 = 0;
            int i10 = 0;
            while (i10 < i) {
                int i11 = i8 + 1;
                ConstraintWidget constraintWidget = constraintWidgetArr[i10];
                int widgetWidth = flow2.getWidgetWidth(constraintWidget, i7);
                if (constraintWidget.getHorizontalDimensionBehaviour() == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT) {
                    i4++;
                }
                int i12 = i4;
                boolean z3 = (i9 == i7 || (flow2.mHorizontalGap + i9) + widgetWidth > i7) && widgetsList.biggest != null;
                if (!z3 && i10 > 0 && (i6 = flow2.mMaxElementsWrap) > 0 && i11 > i6) {
                    z3 = true;
                }
                if (z3) {
                    widgetsList = flow2.new WidgetsList(i2, flow2.mLeft, flow2.mTop, flow2.mRight, flow2.mBottom, i7);
                    widgetsList.setStartIndex(i10);
                    flow2.mChainList.add(widgetsList);
                    i8 = i11;
                    i9 = widgetWidth;
                } else {
                    i9 = i10 > 0 ? i9 + flow2.mHorizontalGap + widgetWidth : widgetWidth;
                    i8 = 0;
                }
                widgetsList.add(constraintWidget);
                i10++;
                i4 = i12;
            }
        } else {
            int i13 = 0;
            i4 = 0;
            int i14 = 0;
            while (i14 < i) {
                ConstraintWidget constraintWidget2 = constraintWidgetArr[i14];
                int widgetHeight = flow2.getWidgetHeight(constraintWidget2, i7);
                if (constraintWidget2.getVerticalDimensionBehaviour() == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT) {
                    i4++;
                }
                int i15 = i4;
                boolean z4 = (i13 == i7 || (flow2.mVerticalGap + i13) + widgetHeight > i7) && widgetsList.biggest != null;
                if (!z4 && i14 > 0 && (i5 = flow2.mMaxElementsWrap) > 0 && i5 < 0) {
                    z4 = true;
                }
                if (z4) {
                    widgetsList = flow2.new WidgetsList(i2, flow2.mLeft, flow2.mTop, flow2.mRight, flow2.mBottom, i7);
                    flow = flow2;
                    widgetsList.setStartIndex(i14);
                    flow.mChainList.add(widgetsList);
                } else {
                    flow = flow2;
                    if (i14 > 0) {
                        i13 += flow.mVerticalGap + widgetHeight;
                        widgetsList.add(constraintWidget2);
                        i14++;
                        i7 = i3;
                        i4 = i15;
                        flow2 = flow;
                    }
                }
                i13 = widgetHeight;
                widgetsList.add(constraintWidget2);
                i14++;
                i7 = i3;
                i4 = i15;
                flow2 = flow;
            }
        }
        Flow flow3 = flow2;
        int size = flow3.mChainList.size();
        ConstraintAnchor constraintAnchor2 = flow3.mLeft;
        ConstraintAnchor constraintAnchor3 = flow3.mTop;
        ConstraintAnchor constraintAnchor4 = flow3.mRight;
        ConstraintAnchor constraintAnchor5 = flow3.mBottom;
        int paddingLeft = flow3.getPaddingLeft();
        int paddingTop = flow3.getPaddingTop();
        int paddingRight = flow3.getPaddingRight();
        int paddingBottom = flow3.getPaddingBottom();
        boolean z5 = flow3.getHorizontalDimensionBehaviour() == ConstraintWidget.DimensionBehaviour.WRAP_CONTENT || flow3.getVerticalDimensionBehaviour() == ConstraintWidget.DimensionBehaviour.WRAP_CONTENT;
        if (i4 > 0 && z5) {
            for (int i16 = 0; i16 < size; i16++) {
                WidgetsList widgetsList2 = flow3.mChainList.get(i16);
                if (i2 == 0) {
                    widgetsList2.measureMatchConstraints(i3 - widgetsList2.getWidth());
                } else {
                    widgetsList2.measureMatchConstraints(i3 - widgetsList2.getHeight());
                }
            }
        }
        ConstraintAnchor constraintAnchor6 = constraintAnchor3;
        int i17 = paddingBottom;
        int i18 = 0;
        int i19 = 0;
        int i20 = paddingRight;
        int i21 = paddingTop;
        int i22 = paddingLeft;
        ConstraintAnchor constraintAnchor7 = constraintAnchor5;
        ConstraintAnchor constraintAnchor8 = constraintAnchor4;
        ConstraintAnchor constraintAnchor9 = constraintAnchor2;
        int i23 = 0;
        while (i19 < size) {
            WidgetsList widgetsList3 = flow3.mChainList.get(i19);
            if (i2 == 0) {
                if (i19 < size - 1) {
                    constraintAnchor7 = flow3.mChainList.get(i19 + 1).biggest.mTop;
                    i17 = 0;
                } else {
                    constraintAnchor7 = flow3.mBottom;
                    i17 = flow3.getPaddingBottom();
                }
                z = z2;
                ConstraintAnchor constraintAnchor10 = widgetsList3.biggest.mBottom;
                int i24 = i18;
                widgetsList3.setup(i2, constraintAnchor9, constraintAnchor6, constraintAnchor8, constraintAnchor7, i22, i21, i20, i17, i3);
                int max = Math.max(i23, widgetsList3.getWidth());
                int height = widgetsList3.getHeight() + i24;
                if (i19 > 0) {
                    height += flow3.mVerticalGap;
                }
                i18 = height;
                i23 = max;
                constraintAnchor6 = constraintAnchor10;
                i21 = 0;
            } else {
                int i25 = i23;
                z = z2;
                int i26 = i18;
                if (i19 < size - 1) {
                    constraintAnchor = flow3.mChainList.get(i19 + 1).biggest.mLeft;
                    i20 = 0;
                } else {
                    constraintAnchor = flow3.mRight;
                    i20 = flow3.getPaddingRight();
                }
                constraintAnchor8 = constraintAnchor;
                ConstraintAnchor constraintAnchor11 = widgetsList3.biggest.mRight;
                widgetsList3.setup(i2, constraintAnchor9, constraintAnchor6, constraintAnchor8, constraintAnchor7, i22, i21, i20, i17, i3);
                int width = widgetsList3.getWidth() + i25;
                int max2 = Math.max(i26, widgetsList3.getHeight());
                if (i19 > 0) {
                    width += flow3.mHorizontalGap;
                }
                int i27 = width;
                i18 = max2;
                i23 = i27;
                i22 = 0;
                constraintAnchor9 = constraintAnchor11;
            }
            i19++;
            z2 = z;
        }
        iArr[0] = i23;
        iArr[z2 ? 1 : 0] = i18;
    }

    private void measureNoWrap(ConstraintWidget[] constraintWidgetArr, int i, int i2, int i3, int[] iArr) {
        WidgetsList widgetsList;
        if (i == 0) {
            return;
        }
        if (this.mChainList.size() == 0) {
            widgetsList = new WidgetsList(i2, this.mLeft, this.mTop, this.mRight, this.mBottom, i3);
            this.mChainList.add(widgetsList);
        } else {
            WidgetsList widgetsList2 = this.mChainList.get(0);
            widgetsList2.clear();
            widgetsList2.setup(i2, this.mLeft, this.mTop, this.mRight, this.mBottom, getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom(), i3);
            widgetsList = widgetsList2;
        }
        for (int i4 = 0; i4 < i; i4++) {
            widgetsList.add(constraintWidgetArr[i4]);
        }
        iArr[0] = widgetsList.getWidth();
        iArr[1] = widgetsList.getHeight();
    }

    /* JADX WARN: Removed duplicated region for block: B:29:0x005e  */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:77:0x010d -> B:22:0x0059). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:78:0x010f -> B:22:0x0059). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:80:0x0115 -> B:22:0x0059). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:81:0x0117 -> B:22:0x0059). Please report as a decompilation issue!!! */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private void measureAligned(ConstraintWidget[] constraintWidgetArr, int i, int i2, int i3, int[] iArr) {
        int i4;
        int i5;
        boolean z;
        ConstraintWidget constraintWidget;
        if (i2 == 0) {
            int i6 = this.mMaxElementsWrap;
            if (i6 <= 0) {
                i6 = 0;
                int i7 = 0;
                for (int i8 = 0; i8 < i; i8++) {
                    if (i8 > 0) {
                        i7 += this.mHorizontalGap;
                    }
                    ConstraintWidget constraintWidget2 = constraintWidgetArr[i8];
                    if (constraintWidget2 != null) {
                        i7 += getWidgetWidth(constraintWidget2, i3);
                        if (i7 > i3) {
                            break;
                        } else {
                            i6++;
                        }
                    }
                }
            }
            i5 = i6;
            i4 = 0;
        } else {
            i4 = this.mMaxElementsWrap;
            if (i4 <= 0) {
                i4 = 0;
                int i9 = 0;
                for (int i10 = 0; i10 < i; i10++) {
                    if (i10 > 0) {
                        i9 += this.mVerticalGap;
                    }
                    ConstraintWidget constraintWidget3 = constraintWidgetArr[i10];
                    if (constraintWidget3 != null) {
                        i9 += getWidgetHeight(constraintWidget3, i3);
                        if (i9 > i3) {
                            break;
                        } else {
                            i4++;
                        }
                    }
                }
            }
            i5 = 0;
        }
        if (this.mAlignedDimensions == null) {
            this.mAlignedDimensions = new int[2];
        }
        if ((i4 != 0 || i2 != 1) && (i5 != 0 || i2 != 0)) {
            z = false;
            while (!z) {
                if (i2 == 0) {
                    i4 = (int) Math.ceil(i / i5);
                } else {
                    i5 = (int) Math.ceil(i / i4);
                }
                ConstraintWidget[] constraintWidgetArr2 = this.mAlignedBiggestElementsInCols;
                if (constraintWidgetArr2 == null || constraintWidgetArr2.length < i5) {
                    this.mAlignedBiggestElementsInCols = new ConstraintWidget[i5];
                } else {
                    Arrays.fill(constraintWidgetArr2, (Object) null);
                }
                ConstraintWidget[] constraintWidgetArr3 = this.mAlignedBiggestElementsInRows;
                if (constraintWidgetArr3 == null || constraintWidgetArr3.length < i4) {
                    this.mAlignedBiggestElementsInRows = new ConstraintWidget[i4];
                } else {
                    Arrays.fill(constraintWidgetArr3, (Object) null);
                }
                for (int i11 = 0; i11 < i5; i11++) {
                    for (int i12 = 0; i12 < i4; i12++) {
                        int i13 = (i12 * i5) + i11;
                        if (i2 == 1) {
                            i13 = (i11 * i4) + i12;
                        }
                        if (i13 < constraintWidgetArr.length && (constraintWidget = constraintWidgetArr[i13]) != null) {
                            int widgetWidth = getWidgetWidth(constraintWidget, i3);
                            ConstraintWidget constraintWidget4 = this.mAlignedBiggestElementsInCols[i11];
                            if (constraintWidget4 == null || constraintWidget4.getWidth() < widgetWidth) {
                                this.mAlignedBiggestElementsInCols[i11] = constraintWidget;
                            }
                            int widgetHeight = getWidgetHeight(constraintWidget, i3);
                            ConstraintWidget constraintWidget5 = this.mAlignedBiggestElementsInRows[i12];
                            if (constraintWidget5 == null || constraintWidget5.getHeight() < widgetHeight) {
                                this.mAlignedBiggestElementsInRows[i12] = constraintWidget;
                            }
                        }
                    }
                }
                int i14 = 0;
                for (int i15 = 0; i15 < i5; i15++) {
                    ConstraintWidget constraintWidget6 = this.mAlignedBiggestElementsInCols[i15];
                    if (constraintWidget6 != null) {
                        if (i15 > 0) {
                            i14 += this.mHorizontalGap;
                        }
                        i14 += getWidgetWidth(constraintWidget6, i3);
                    }
                }
                int i16 = 0;
                for (int i17 = 0; i17 < i4; i17++) {
                    ConstraintWidget constraintWidget7 = this.mAlignedBiggestElementsInRows[i17];
                    if (constraintWidget7 != null) {
                        if (i17 > 0) {
                            i16 += this.mVerticalGap;
                        }
                        i16 += getWidgetHeight(constraintWidget7, i3);
                    }
                }
                iArr[0] = i14;
                iArr[1] = i16;
                if (i2 != 0) {
                    if (i16 > i3 && i4 > 1) {
                        i4--;
                    }
                } else if (i14 > i3 && i5 > 1) {
                    i5--;
                }
                while (!z) {
                }
            }
            int[] iArr2 = this.mAlignedDimensions;
            iArr2[0] = i5;
            iArr2[1] = i4;
        }
        z = true;
        while (!z) {
        }
        int[] iArr22 = this.mAlignedDimensions;
        iArr22[0] = i5;
        iArr22[1] = i4;
    }

    private void createAlignedConstraints(boolean z) {
        ConstraintWidget constraintWidget;
        float f;
        int i;
        if (this.mAlignedDimensions == null || this.mAlignedBiggestElementsInCols == null || this.mAlignedBiggestElementsInRows == null) {
            return;
        }
        for (int i2 = 0; i2 < this.mDisplayedWidgetsCount; i2++) {
            this.mDisplayedWidgets[i2].resetAnchors();
        }
        int[] iArr = this.mAlignedDimensions;
        int i3 = iArr[0];
        int i4 = iArr[1];
        float f2 = this.mHorizontalBias;
        ConstraintWidget constraintWidget2 = null;
        int i5 = 0;
        while (i5 < i3) {
            if (z) {
                i = (i3 - i5) - 1;
                f = 1.0f - this.mHorizontalBias;
            } else {
                f = f2;
                i = i5;
            }
            ConstraintWidget constraintWidget3 = this.mAlignedBiggestElementsInCols[i];
            if (constraintWidget3 != null && constraintWidget3.getVisibility() != 8) {
                if (i5 == 0) {
                    constraintWidget3.connect(constraintWidget3.mLeft, this.mLeft, getPaddingLeft());
                    constraintWidget3.setHorizontalChainStyle(this.mHorizontalStyle);
                    constraintWidget3.setHorizontalBiasPercent(f);
                }
                if (i5 == i3 - 1) {
                    constraintWidget3.connect(constraintWidget3.mRight, this.mRight, getPaddingRight());
                }
                if (i5 > 0 && constraintWidget2 != null) {
                    constraintWidget3.connect(constraintWidget3.mLeft, constraintWidget2.mRight, this.mHorizontalGap);
                    constraintWidget2.connect(constraintWidget2.mRight, constraintWidget3.mLeft, 0);
                }
                constraintWidget2 = constraintWidget3;
            }
            i5++;
            f2 = f;
        }
        for (int i6 = 0; i6 < i4; i6++) {
            ConstraintWidget constraintWidget4 = this.mAlignedBiggestElementsInRows[i6];
            if (constraintWidget4 != null && constraintWidget4.getVisibility() != 8) {
                if (i6 == 0) {
                    constraintWidget4.connect(constraintWidget4.mTop, this.mTop, getPaddingTop());
                    constraintWidget4.setVerticalChainStyle(this.mVerticalStyle);
                    constraintWidget4.setVerticalBiasPercent(this.mVerticalBias);
                }
                if (i6 == i4 - 1) {
                    constraintWidget4.connect(constraintWidget4.mBottom, this.mBottom, getPaddingBottom());
                }
                if (i6 > 0 && constraintWidget2 != null) {
                    constraintWidget4.connect(constraintWidget4.mTop, constraintWidget2.mBottom, this.mVerticalGap);
                    constraintWidget2.connect(constraintWidget2.mBottom, constraintWidget4.mTop, 0);
                }
                constraintWidget2 = constraintWidget4;
            }
        }
        for (int i7 = 0; i7 < i3; i7++) {
            for (int i8 = 0; i8 < i4; i8++) {
                int i9 = (i8 * i3) + i7;
                if (this.mOrientation == 1) {
                    i9 = (i7 * i4) + i8;
                }
                ConstraintWidget[] constraintWidgetArr = this.mDisplayedWidgets;
                if (i9 < constraintWidgetArr.length && (constraintWidget = constraintWidgetArr[i9]) != null && constraintWidget.getVisibility() != 8) {
                    ConstraintWidget constraintWidget5 = this.mAlignedBiggestElementsInCols[i7];
                    ConstraintWidget constraintWidget6 = this.mAlignedBiggestElementsInRows[i8];
                    if (constraintWidget != constraintWidget5) {
                        constraintWidget.connect(constraintWidget.mLeft, constraintWidget5.mLeft, 0);
                        constraintWidget.connect(constraintWidget.mRight, constraintWidget5.mRight, 0);
                    }
                    if (constraintWidget != constraintWidget6) {
                        constraintWidget.connect(constraintWidget.mTop, constraintWidget6.mTop, 0);
                        constraintWidget.connect(constraintWidget.mBottom, constraintWidget6.mBottom, 0);
                    }
                }
            }
        }
    }

    @Override // androidx.constraintlayout.core.widgets.ConstraintWidget
    public void addToSolver(LinearSystem linearSystem, boolean z) {
        super.addToSolver(linearSystem, z);
        boolean z2 = getParent() != null && ((ConstraintWidgetContainer) getParent()).isRtl();
        int i = this.mWrapMode;
        if (i != 0) {
            if (i == 1) {
                int size = this.mChainList.size();
                int i2 = 0;
                while (i2 < size) {
                    this.mChainList.get(i2).createConstraints(z2, i2, i2 == size + (-1));
                    i2++;
                }
            } else if (i == 2) {
                createAlignedConstraints(z2);
            } else if (i == 3) {
                int size2 = this.mChainList.size();
                int i3 = 0;
                while (i3 < size2) {
                    this.mChainList.get(i3).createConstraints(z2, i3, i3 == size2 + (-1));
                    i3++;
                }
            }
        } else if (this.mChainList.size() > 0) {
            this.mChainList.get(0).createConstraints(z2, 0, true);
        }
        needsCallbackFromSolver(false);
    }
}
