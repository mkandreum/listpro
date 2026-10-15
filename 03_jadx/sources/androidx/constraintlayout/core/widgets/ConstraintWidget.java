package androidx.constraintlayout.core.widgets;

import androidx.constraintlayout.core.Cache;
import androidx.constraintlayout.core.LinearSystem;
import androidx.constraintlayout.core.SolverVariable;
import androidx.constraintlayout.core.state.WidgetFrame;
import androidx.constraintlayout.core.widgets.ConstraintAnchor;
import androidx.constraintlayout.core.widgets.analyzer.ChainRun;
import androidx.constraintlayout.core.widgets.analyzer.HorizontalWidgetRun;
import androidx.constraintlayout.core.widgets.analyzer.VerticalWidgetRun;
import androidx.constraintlayout.core.widgets.analyzer.WidgetRun;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/* loaded from: classes.dex */
public class ConstraintWidget {
    public static final int ANCHOR_BASELINE = 4;
    public static final int ANCHOR_BOTTOM = 3;
    public static final int ANCHOR_LEFT = 0;
    public static final int ANCHOR_RIGHT = 1;
    public static final int ANCHOR_TOP = 2;
    private static final boolean AUTOTAG_CENTER = false;
    public static final int BOTH = 2;
    public static final int CHAIN_PACKED = 2;
    public static final int CHAIN_SPREAD = 0;
    public static final int CHAIN_SPREAD_INSIDE = 1;
    public static float DEFAULT_BIAS = 0.5f;
    static final int DIMENSION_HORIZONTAL = 0;
    static final int DIMENSION_VERTICAL = 1;
    protected static final int DIRECT = 2;
    public static final int GONE = 8;
    public static final int HORIZONTAL = 0;
    public static final int INVISIBLE = 4;
    public static final int MATCH_CONSTRAINT_PERCENT = 2;
    public static final int MATCH_CONSTRAINT_RATIO = 3;
    public static final int MATCH_CONSTRAINT_RATIO_RESOLVED = 4;
    public static final int MATCH_CONSTRAINT_SPREAD = 0;
    public static final int MATCH_CONSTRAINT_WRAP = 1;
    protected static final int SOLVER = 1;
    public static final int UNKNOWN = -1;
    private static final boolean USE_WRAP_DIMENSION_FOR_SPREAD = false;
    public static final int VERTICAL = 1;
    public static final int VISIBLE = 0;
    private static final int WRAP = -2;
    public static final int WRAP_BEHAVIOR_HORIZONTAL_ONLY = 1;
    public static final int WRAP_BEHAVIOR_INCLUDED = 0;
    public static final int WRAP_BEHAVIOR_SKIPPED = 3;
    public static final int WRAP_BEHAVIOR_VERTICAL_ONLY = 2;
    private boolean OPTIMIZE_WRAP;
    private boolean OPTIMIZE_WRAP_ON_RESOLVED;
    public WidgetFrame frame;
    private boolean hasBaseline;
    public ChainRun horizontalChainRun;
    public int horizontalGroup;
    public HorizontalWidgetRun horizontalRun;
    private boolean horizontalSolvingPass;
    private boolean inPlaceholder;
    public boolean[] isTerminalWidget;
    protected ArrayList<ConstraintAnchor> mAnchors;
    private boolean mAnimated;
    public ConstraintAnchor mBaseline;
    int mBaselineDistance;
    public ConstraintAnchor mBottom;
    boolean mBottomHasCentered;
    public ConstraintAnchor mCenter;
    ConstraintAnchor mCenterX;
    ConstraintAnchor mCenterY;
    private float mCircleConstraintAngle;
    private Object mCompanionWidget;
    private int mContainerItemSkip;
    private String mDebugName;
    public float mDimensionRatio;
    protected int mDimensionRatioSide;
    int mDistToBottom;
    int mDistToLeft;
    int mDistToRight;
    int mDistToTop;
    boolean mGroupsToSolver;
    int mHeight;
    private int mHeightOverride;
    float mHorizontalBiasPercent;
    boolean mHorizontalChainFixedPosition;
    int mHorizontalChainStyle;
    ConstraintWidget mHorizontalNextWidget;
    public int mHorizontalResolution;
    boolean mHorizontalWrapVisited;
    private boolean mInVirtualLayout;
    public boolean mIsHeightWrapContent;
    private boolean[] mIsInBarrier;
    public boolean mIsWidthWrapContent;
    private int mLastHorizontalMeasureSpec;
    private int mLastVerticalMeasureSpec;
    public ConstraintAnchor mLeft;
    boolean mLeftHasCentered;
    public ConstraintAnchor[] mListAnchors;
    public DimensionBehaviour[] mListDimensionBehaviors;
    protected ConstraintWidget[] mListNextMatchConstraintsWidget;
    public int mMatchConstraintDefaultHeight;
    public int mMatchConstraintDefaultWidth;
    public int mMatchConstraintMaxHeight;
    public int mMatchConstraintMaxWidth;
    public int mMatchConstraintMinHeight;
    public int mMatchConstraintMinWidth;
    public float mMatchConstraintPercentHeight;
    public float mMatchConstraintPercentWidth;
    private int[] mMaxDimension;
    private boolean mMeasureRequested;
    protected int mMinHeight;
    protected int mMinWidth;
    protected ConstraintWidget[] mNextChainWidget;
    protected int mOffsetX;
    protected int mOffsetY;
    public ConstraintWidget mParent;
    int mRelX;
    int mRelY;
    float mResolvedDimensionRatio;
    int mResolvedDimensionRatioSide;
    boolean mResolvedHasRatio;
    public int[] mResolvedMatchConstraintDefault;
    public ConstraintAnchor mRight;
    boolean mRightHasCentered;
    public ConstraintAnchor mTop;
    boolean mTopHasCentered;
    private String mType;
    float mVerticalBiasPercent;
    boolean mVerticalChainFixedPosition;
    int mVerticalChainStyle;
    ConstraintWidget mVerticalNextWidget;
    public int mVerticalResolution;
    boolean mVerticalWrapVisited;
    private int mVisibility;
    public float[] mWeight;
    int mWidth;
    private int mWidthOverride;
    private int mWrapBehaviorInParent;
    protected int mX;
    protected int mY;
    public boolean measured;
    private boolean resolvedHorizontal;
    private boolean resolvedVertical;
    public WidgetRun[] run;
    public String stringId;
    public ChainRun verticalChainRun;
    public int verticalGroup;
    public VerticalWidgetRun verticalRun;
    private boolean verticalSolvingPass;

    public enum DimensionBehaviour {
        FIXED,
        WRAP_CONTENT,
        MATCH_CONSTRAINT,
        MATCH_PARENT
    }

    public WidgetRun getRun(int i) {
        if (i == 0) {
            return this.horizontalRun;
        }
        if (i == 1) {
            return this.verticalRun;
        }
        return null;
    }

    public void setFinalFrame(int i, int i2, int i3, int i4, int i5, int i6) {
        setFrame(i, i2, i3, i4);
        setBaselineDistance(i5);
        if (i6 == 0) {
            this.resolvedHorizontal = true;
            this.resolvedVertical = false;
        } else if (i6 == 1) {
            this.resolvedHorizontal = false;
            this.resolvedVertical = true;
        } else if (i6 == 2) {
            this.resolvedHorizontal = true;
            this.resolvedVertical = true;
        } else {
            this.resolvedHorizontal = false;
            this.resolvedVertical = false;
        }
    }

    public void setFinalLeft(int i) {
        this.mLeft.setFinalValue(i);
        this.mX = i;
    }

    public void setFinalTop(int i) {
        this.mTop.setFinalValue(i);
        this.mY = i;
    }

    public void resetSolvingPassFlag() {
        this.horizontalSolvingPass = false;
        this.verticalSolvingPass = false;
    }

    public boolean isHorizontalSolvingPassDone() {
        return this.horizontalSolvingPass;
    }

    public boolean isVerticalSolvingPassDone() {
        return this.verticalSolvingPass;
    }

    public void markHorizontalSolvingPassDone() {
        this.horizontalSolvingPass = true;
    }

    public void markVerticalSolvingPassDone() {
        this.verticalSolvingPass = true;
    }

    public void setFinalHorizontal(int i, int i2) {
        if (this.resolvedHorizontal) {
            return;
        }
        this.mLeft.setFinalValue(i);
        this.mRight.setFinalValue(i2);
        this.mX = i;
        this.mWidth = i2 - i;
        this.resolvedHorizontal = true;
    }

    public void setFinalVertical(int i, int i2) {
        if (this.resolvedVertical) {
            return;
        }
        this.mTop.setFinalValue(i);
        this.mBottom.setFinalValue(i2);
        this.mY = i;
        this.mHeight = i2 - i;
        if (this.hasBaseline) {
            this.mBaseline.setFinalValue(i + this.mBaselineDistance);
        }
        this.resolvedVertical = true;
    }

    public void setFinalBaseline(int i) {
        if (this.hasBaseline) {
            int i2 = i - this.mBaselineDistance;
            int i3 = this.mHeight + i2;
            this.mY = i2;
            this.mTop.setFinalValue(i2);
            this.mBottom.setFinalValue(i3);
            this.mBaseline.setFinalValue(i);
            this.resolvedVertical = true;
        }
    }

    public boolean isResolvedHorizontally() {
        if (this.resolvedHorizontal) {
            return true;
        }
        return this.mLeft.hasFinalValue() && this.mRight.hasFinalValue();
    }

    public boolean isResolvedVertically() {
        if (this.resolvedVertical) {
            return true;
        }
        return this.mTop.hasFinalValue() && this.mBottom.hasFinalValue();
    }

    public void resetFinalResolution() {
        this.resolvedHorizontal = false;
        this.resolvedVertical = false;
        this.horizontalSolvingPass = false;
        this.verticalSolvingPass = false;
        int size = this.mAnchors.size();
        for (int i = 0; i < size; i++) {
            this.mAnchors.get(i).resetFinalResolution();
        }
    }

    public void ensureMeasureRequested() {
        this.mMeasureRequested = true;
    }

    public boolean hasDependencies() {
        int size = this.mAnchors.size();
        for (int i = 0; i < size; i++) {
            if (this.mAnchors.get(i).hasDependents()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasDanglingDimension(int i) {
        if (i == 0) {
            return (this.mLeft.mTarget != null ? 1 : 0) + (this.mRight.mTarget != null ? 1 : 0) < 2;
        }
        return ((this.mTop.mTarget != null ? 1 : 0) + (this.mBottom.mTarget != null ? 1 : 0)) + (this.mBaseline.mTarget != null ? 1 : 0) < 2;
    }

    public boolean hasResolvedTargets(int i, int i2) {
        if (i == 0) {
            return this.mLeft.mTarget != null && this.mLeft.mTarget.hasFinalValue() && this.mRight.mTarget != null && this.mRight.mTarget.hasFinalValue() && (this.mRight.mTarget.getFinalValue() - this.mRight.getMargin()) - (this.mLeft.mTarget.getFinalValue() + this.mLeft.getMargin()) >= i2;
        }
        if (this.mTop.mTarget != null && this.mTop.mTarget.hasFinalValue() && this.mBottom.mTarget != null && this.mBottom.mTarget.hasFinalValue() && (this.mBottom.mTarget.getFinalValue() - this.mBottom.getMargin()) - (this.mTop.mTarget.getFinalValue() + this.mTop.getMargin()) >= i2) {
            return true;
        }
        return false;
    }

    public boolean isInVirtualLayout() {
        return this.mInVirtualLayout;
    }

    public void setInVirtualLayout(boolean z) {
        this.mInVirtualLayout = z;
    }

    public int getMaxHeight() {
        return this.mMaxDimension[1];
    }

    public int getMaxWidth() {
        return this.mMaxDimension[0];
    }

    public void setMaxWidth(int i) {
        this.mMaxDimension[0] = i;
    }

    public void setMaxHeight(int i) {
        this.mMaxDimension[1] = i;
    }

    public boolean isSpreadWidth() {
        return this.mMatchConstraintDefaultWidth == 0 && this.mDimensionRatio == 0.0f && this.mMatchConstraintMinWidth == 0 && this.mMatchConstraintMaxWidth == 0 && this.mListDimensionBehaviors[0] == DimensionBehaviour.MATCH_CONSTRAINT;
    }

    public boolean isSpreadHeight() {
        return this.mMatchConstraintDefaultHeight == 0 && this.mDimensionRatio == 0.0f && this.mMatchConstraintMinHeight == 0 && this.mMatchConstraintMaxHeight == 0 && this.mListDimensionBehaviors[1] == DimensionBehaviour.MATCH_CONSTRAINT;
    }

    public void setHasBaseline(boolean z) {
        this.hasBaseline = z;
    }

    public boolean getHasBaseline() {
        return this.hasBaseline;
    }

    public boolean isInPlaceholder() {
        return this.inPlaceholder;
    }

    public void setInPlaceholder(boolean z) {
        this.inPlaceholder = z;
    }

    protected void setInBarrier(int i, boolean z) {
        this.mIsInBarrier[i] = z;
    }

    public boolean isInBarrier(int i) {
        return this.mIsInBarrier[i];
    }

    public void setMeasureRequested(boolean z) {
        this.mMeasureRequested = z;
    }

    public boolean isMeasureRequested() {
        return this.mMeasureRequested && this.mVisibility != 8;
    }

    public void setWrapBehaviorInParent(int i) {
        if (i < 0 || i > 3) {
            return;
        }
        this.mWrapBehaviorInParent = i;
    }

    public int getWrapBehaviorInParent() {
        return this.mWrapBehaviorInParent;
    }

    public int getLastHorizontalMeasureSpec() {
        return this.mLastHorizontalMeasureSpec;
    }

    public int getLastVerticalMeasureSpec() {
        return this.mLastVerticalMeasureSpec;
    }

    public void setLastMeasureSpec(int i, int i2) {
        this.mLastHorizontalMeasureSpec = i;
        this.mLastVerticalMeasureSpec = i2;
        setMeasureRequested(false);
    }

    public void reset() {
        this.mLeft.reset();
        this.mTop.reset();
        this.mRight.reset();
        this.mBottom.reset();
        this.mBaseline.reset();
        this.mCenterX.reset();
        this.mCenterY.reset();
        this.mCenter.reset();
        this.mParent = null;
        this.mCircleConstraintAngle = 0.0f;
        this.mWidth = 0;
        this.mHeight = 0;
        this.mDimensionRatio = 0.0f;
        this.mDimensionRatioSide = -1;
        this.mX = 0;
        this.mY = 0;
        this.mOffsetX = 0;
        this.mOffsetY = 0;
        this.mBaselineDistance = 0;
        this.mMinWidth = 0;
        this.mMinHeight = 0;
        float f = DEFAULT_BIAS;
        this.mHorizontalBiasPercent = f;
        this.mVerticalBiasPercent = f;
        this.mListDimensionBehaviors[0] = DimensionBehaviour.FIXED;
        this.mListDimensionBehaviors[1] = DimensionBehaviour.FIXED;
        this.mCompanionWidget = null;
        this.mContainerItemSkip = 0;
        this.mVisibility = 0;
        this.mType = null;
        this.mHorizontalWrapVisited = false;
        this.mVerticalWrapVisited = false;
        this.mHorizontalChainStyle = 0;
        this.mVerticalChainStyle = 0;
        this.mHorizontalChainFixedPosition = false;
        this.mVerticalChainFixedPosition = false;
        float[] fArr = this.mWeight;
        fArr[0] = -1.0f;
        fArr[1] = -1.0f;
        this.mHorizontalResolution = -1;
        this.mVerticalResolution = -1;
        int[] iArr = this.mMaxDimension;
        iArr[0] = Integer.MAX_VALUE;
        iArr[1] = Integer.MAX_VALUE;
        this.mMatchConstraintDefaultWidth = 0;
        this.mMatchConstraintDefaultHeight = 0;
        this.mMatchConstraintPercentWidth = 1.0f;
        this.mMatchConstraintPercentHeight = 1.0f;
        this.mMatchConstraintMaxWidth = Integer.MAX_VALUE;
        this.mMatchConstraintMaxHeight = Integer.MAX_VALUE;
        this.mMatchConstraintMinWidth = 0;
        this.mMatchConstraintMinHeight = 0;
        this.mResolvedHasRatio = false;
        this.mResolvedDimensionRatioSide = -1;
        this.mResolvedDimensionRatio = 1.0f;
        this.mGroupsToSolver = false;
        boolean[] zArr = this.isTerminalWidget;
        zArr[0] = true;
        zArr[1] = true;
        this.mInVirtualLayout = false;
        boolean[] zArr2 = this.mIsInBarrier;
        zArr2[0] = false;
        zArr2[1] = false;
        this.mMeasureRequested = true;
        int[] iArr2 = this.mResolvedMatchConstraintDefault;
        iArr2[0] = 0;
        iArr2[1] = 0;
        this.mWidthOverride = -1;
        this.mHeightOverride = -1;
    }

    private void serializeAnchor(StringBuilder sb, String str, ConstraintAnchor constraintAnchor) {
        if (constraintAnchor.mTarget == null) {
            return;
        }
        sb.append(str);
        sb.append(" : [ '");
        sb.append(constraintAnchor.mTarget);
        sb.append("',");
        sb.append(constraintAnchor.mMargin);
        sb.append(",");
        sb.append(constraintAnchor.mGoneMargin);
        sb.append(",");
        sb.append(" ] ,\n");
    }

    private void serializeCircle(StringBuilder sb, ConstraintAnchor constraintAnchor, float f) {
        if (constraintAnchor.mTarget == null) {
            return;
        }
        sb.append("circle : [ '");
        sb.append(constraintAnchor.mTarget);
        sb.append("',");
        sb.append(constraintAnchor.mMargin);
        sb.append(",");
        sb.append(f);
        sb.append(",");
        sb.append(" ] ,\n");
    }

    private void serializeAttribute(StringBuilder sb, String str, float f, float f2) {
        if (f == f2) {
            return;
        }
        sb.append(str);
        sb.append(" :   ");
        sb.append(f);
        sb.append(",\n");
    }

    private void serializeAttribute(StringBuilder sb, String str, int i, int i2) {
        if (i == i2) {
            return;
        }
        sb.append(str);
        sb.append(" :   ");
        sb.append(i);
        sb.append(",\n");
    }

    private void serializeDimensionRatio(StringBuilder sb, String str, float f, int i) {
        if (f == 0.0f) {
            return;
        }
        sb.append(str);
        sb.append(" :  [");
        sb.append(f);
        sb.append(",");
        sb.append(i);
        sb.append("");
        sb.append("],\n");
    }

    private void serializeSize(StringBuilder sb, String str, int i, int i2, int i3, int i4, int i5, int i6, float f, float f2) {
        sb.append(str);
        sb.append(" :  {\n");
        serializeAttribute(sb, "size", i, Integer.MIN_VALUE);
        serializeAttribute(sb, "min", i2, 0);
        serializeAttribute(sb, "max", i3, Integer.MAX_VALUE);
        serializeAttribute(sb, "matchMin", i5, 0);
        serializeAttribute(sb, "matchDef", i6, 0);
        serializeAttribute(sb, "matchPercent", i6, 1);
        sb.append("},\n");
    }

    public StringBuilder serialize(StringBuilder sb) {
        sb.append("{\n");
        serializeAnchor(sb, "left", this.mLeft);
        serializeAnchor(sb, "top", this.mTop);
        serializeAnchor(sb, "right", this.mRight);
        serializeAnchor(sb, "bottom", this.mBottom);
        serializeAnchor(sb, "baseline", this.mBaseline);
        serializeAnchor(sb, "centerX", this.mCenterX);
        serializeAnchor(sb, "centerY", this.mCenterY);
        serializeCircle(sb, this.mCenter, this.mCircleConstraintAngle);
        serializeSize(sb, "width", this.mWidth, this.mMinWidth, this.mMaxDimension[0], this.mWidthOverride, this.mMatchConstraintMinWidth, this.mMatchConstraintDefaultWidth, this.mMatchConstraintPercentWidth, this.mWeight[0]);
        serializeSize(sb, "height", this.mHeight, this.mMinHeight, this.mMaxDimension[1], this.mHeightOverride, this.mMatchConstraintMinHeight, this.mMatchConstraintDefaultHeight, this.mMatchConstraintPercentHeight, this.mWeight[1]);
        serializeDimensionRatio(sb, "dimensionRatio", this.mDimensionRatio, this.mDimensionRatioSide);
        serializeAttribute(sb, "horizontalBias", this.mHorizontalBiasPercent, DEFAULT_BIAS);
        serializeAttribute(sb, "verticalBias", this.mVerticalBiasPercent, DEFAULT_BIAS);
        sb.append("}\n");
        return sb;
    }

    public boolean oppositeDimensionDependsOn(int i) {
        char c = i == 0 ? (char) 1 : (char) 0;
        DimensionBehaviour[] dimensionBehaviourArr = this.mListDimensionBehaviors;
        return dimensionBehaviourArr[i] == DimensionBehaviour.MATCH_CONSTRAINT && dimensionBehaviourArr[c] == DimensionBehaviour.MATCH_CONSTRAINT;
    }

    public boolean oppositeDimensionsTied() {
        return this.mListDimensionBehaviors[0] == DimensionBehaviour.MATCH_CONSTRAINT && this.mListDimensionBehaviors[1] == DimensionBehaviour.MATCH_CONSTRAINT;
    }

    public boolean hasDimensionOverride() {
        return (this.mWidthOverride == -1 && this.mHeightOverride == -1) ? false : true;
    }

    public ConstraintWidget() {
        this.measured = false;
        this.run = new WidgetRun[2];
        this.horizontalRun = null;
        this.verticalRun = null;
        this.isTerminalWidget = new boolean[]{true, true};
        this.mResolvedHasRatio = false;
        this.mMeasureRequested = true;
        this.OPTIMIZE_WRAP = false;
        this.OPTIMIZE_WRAP_ON_RESOLVED = true;
        this.mWidthOverride = -1;
        this.mHeightOverride = -1;
        this.frame = new WidgetFrame(this);
        this.resolvedHorizontal = false;
        this.resolvedVertical = false;
        this.horizontalSolvingPass = false;
        this.verticalSolvingPass = false;
        this.mHorizontalResolution = -1;
        this.mVerticalResolution = -1;
        this.mWrapBehaviorInParent = 0;
        this.mMatchConstraintDefaultWidth = 0;
        this.mMatchConstraintDefaultHeight = 0;
        this.mResolvedMatchConstraintDefault = new int[2];
        this.mMatchConstraintMinWidth = 0;
        this.mMatchConstraintMaxWidth = 0;
        this.mMatchConstraintPercentWidth = 1.0f;
        this.mMatchConstraintMinHeight = 0;
        this.mMatchConstraintMaxHeight = 0;
        this.mMatchConstraintPercentHeight = 1.0f;
        this.mResolvedDimensionRatioSide = -1;
        this.mResolvedDimensionRatio = 1.0f;
        this.mMaxDimension = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE};
        this.mCircleConstraintAngle = 0.0f;
        this.hasBaseline = false;
        this.mInVirtualLayout = false;
        this.mLastHorizontalMeasureSpec = 0;
        this.mLastVerticalMeasureSpec = 0;
        this.mLeft = new ConstraintAnchor(this, ConstraintAnchor.Type.LEFT);
        this.mTop = new ConstraintAnchor(this, ConstraintAnchor.Type.TOP);
        this.mRight = new ConstraintAnchor(this, ConstraintAnchor.Type.RIGHT);
        this.mBottom = new ConstraintAnchor(this, ConstraintAnchor.Type.BOTTOM);
        this.mBaseline = new ConstraintAnchor(this, ConstraintAnchor.Type.BASELINE);
        this.mCenterX = new ConstraintAnchor(this, ConstraintAnchor.Type.CENTER_X);
        this.mCenterY = new ConstraintAnchor(this, ConstraintAnchor.Type.CENTER_Y);
        ConstraintAnchor constraintAnchor = new ConstraintAnchor(this, ConstraintAnchor.Type.CENTER);
        this.mCenter = constraintAnchor;
        this.mListAnchors = new ConstraintAnchor[]{this.mLeft, this.mRight, this.mTop, this.mBottom, this.mBaseline, constraintAnchor};
        this.mAnchors = new ArrayList<>();
        this.mIsInBarrier = new boolean[2];
        this.mListDimensionBehaviors = new DimensionBehaviour[]{DimensionBehaviour.FIXED, DimensionBehaviour.FIXED};
        this.mParent = null;
        this.mWidth = 0;
        this.mHeight = 0;
        this.mDimensionRatio = 0.0f;
        this.mDimensionRatioSide = -1;
        this.mX = 0;
        this.mY = 0;
        this.mRelX = 0;
        this.mRelY = 0;
        this.mOffsetX = 0;
        this.mOffsetY = 0;
        this.mBaselineDistance = 0;
        float f = DEFAULT_BIAS;
        this.mHorizontalBiasPercent = f;
        this.mVerticalBiasPercent = f;
        this.mContainerItemSkip = 0;
        this.mVisibility = 0;
        this.mAnimated = false;
        this.mDebugName = null;
        this.mType = null;
        this.mGroupsToSolver = false;
        this.mHorizontalChainStyle = 0;
        this.mVerticalChainStyle = 0;
        this.mWeight = new float[]{-1.0f, -1.0f};
        this.mListNextMatchConstraintsWidget = new ConstraintWidget[]{null, null};
        this.mNextChainWidget = new ConstraintWidget[]{null, null};
        this.mHorizontalNextWidget = null;
        this.mVerticalNextWidget = null;
        this.horizontalGroup = -1;
        this.verticalGroup = -1;
        addAnchors();
    }

    public ConstraintWidget(String str) {
        this.measured = false;
        this.run = new WidgetRun[2];
        this.horizontalRun = null;
        this.verticalRun = null;
        this.isTerminalWidget = new boolean[]{true, true};
        this.mResolvedHasRatio = false;
        this.mMeasureRequested = true;
        this.OPTIMIZE_WRAP = false;
        this.OPTIMIZE_WRAP_ON_RESOLVED = true;
        this.mWidthOverride = -1;
        this.mHeightOverride = -1;
        this.frame = new WidgetFrame(this);
        this.resolvedHorizontal = false;
        this.resolvedVertical = false;
        this.horizontalSolvingPass = false;
        this.verticalSolvingPass = false;
        this.mHorizontalResolution = -1;
        this.mVerticalResolution = -1;
        this.mWrapBehaviorInParent = 0;
        this.mMatchConstraintDefaultWidth = 0;
        this.mMatchConstraintDefaultHeight = 0;
        this.mResolvedMatchConstraintDefault = new int[2];
        this.mMatchConstraintMinWidth = 0;
        this.mMatchConstraintMaxWidth = 0;
        this.mMatchConstraintPercentWidth = 1.0f;
        this.mMatchConstraintMinHeight = 0;
        this.mMatchConstraintMaxHeight = 0;
        this.mMatchConstraintPercentHeight = 1.0f;
        this.mResolvedDimensionRatioSide = -1;
        this.mResolvedDimensionRatio = 1.0f;
        this.mMaxDimension = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE};
        this.mCircleConstraintAngle = 0.0f;
        this.hasBaseline = false;
        this.mInVirtualLayout = false;
        this.mLastHorizontalMeasureSpec = 0;
        this.mLastVerticalMeasureSpec = 0;
        this.mLeft = new ConstraintAnchor(this, ConstraintAnchor.Type.LEFT);
        this.mTop = new ConstraintAnchor(this, ConstraintAnchor.Type.TOP);
        this.mRight = new ConstraintAnchor(this, ConstraintAnchor.Type.RIGHT);
        this.mBottom = new ConstraintAnchor(this, ConstraintAnchor.Type.BOTTOM);
        this.mBaseline = new ConstraintAnchor(this, ConstraintAnchor.Type.BASELINE);
        this.mCenterX = new ConstraintAnchor(this, ConstraintAnchor.Type.CENTER_X);
        this.mCenterY = new ConstraintAnchor(this, ConstraintAnchor.Type.CENTER_Y);
        ConstraintAnchor constraintAnchor = new ConstraintAnchor(this, ConstraintAnchor.Type.CENTER);
        this.mCenter = constraintAnchor;
        this.mListAnchors = new ConstraintAnchor[]{this.mLeft, this.mRight, this.mTop, this.mBottom, this.mBaseline, constraintAnchor};
        this.mAnchors = new ArrayList<>();
        this.mIsInBarrier = new boolean[2];
        this.mListDimensionBehaviors = new DimensionBehaviour[]{DimensionBehaviour.FIXED, DimensionBehaviour.FIXED};
        this.mParent = null;
        this.mWidth = 0;
        this.mHeight = 0;
        this.mDimensionRatio = 0.0f;
        this.mDimensionRatioSide = -1;
        this.mX = 0;
        this.mY = 0;
        this.mRelX = 0;
        this.mRelY = 0;
        this.mOffsetX = 0;
        this.mOffsetY = 0;
        this.mBaselineDistance = 0;
        float f = DEFAULT_BIAS;
        this.mHorizontalBiasPercent = f;
        this.mVerticalBiasPercent = f;
        this.mContainerItemSkip = 0;
        this.mVisibility = 0;
        this.mAnimated = false;
        this.mDebugName = null;
        this.mType = null;
        this.mGroupsToSolver = false;
        this.mHorizontalChainStyle = 0;
        this.mVerticalChainStyle = 0;
        this.mWeight = new float[]{-1.0f, -1.0f};
        this.mListNextMatchConstraintsWidget = new ConstraintWidget[]{null, null};
        this.mNextChainWidget = new ConstraintWidget[]{null, null};
        this.mHorizontalNextWidget = null;
        this.mVerticalNextWidget = null;
        this.horizontalGroup = -1;
        this.verticalGroup = -1;
        addAnchors();
        setDebugName(str);
    }

    public ConstraintWidget(int i, int i2, int i3, int i4) {
        this.measured = false;
        this.run = new WidgetRun[2];
        this.horizontalRun = null;
        this.verticalRun = null;
        this.isTerminalWidget = new boolean[]{true, true};
        this.mResolvedHasRatio = false;
        this.mMeasureRequested = true;
        this.OPTIMIZE_WRAP = false;
        this.OPTIMIZE_WRAP_ON_RESOLVED = true;
        this.mWidthOverride = -1;
        this.mHeightOverride = -1;
        this.frame = new WidgetFrame(this);
        this.resolvedHorizontal = false;
        this.resolvedVertical = false;
        this.horizontalSolvingPass = false;
        this.verticalSolvingPass = false;
        this.mHorizontalResolution = -1;
        this.mVerticalResolution = -1;
        this.mWrapBehaviorInParent = 0;
        this.mMatchConstraintDefaultWidth = 0;
        this.mMatchConstraintDefaultHeight = 0;
        this.mResolvedMatchConstraintDefault = new int[2];
        this.mMatchConstraintMinWidth = 0;
        this.mMatchConstraintMaxWidth = 0;
        this.mMatchConstraintPercentWidth = 1.0f;
        this.mMatchConstraintMinHeight = 0;
        this.mMatchConstraintMaxHeight = 0;
        this.mMatchConstraintPercentHeight = 1.0f;
        this.mResolvedDimensionRatioSide = -1;
        this.mResolvedDimensionRatio = 1.0f;
        this.mMaxDimension = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE};
        this.mCircleConstraintAngle = 0.0f;
        this.hasBaseline = false;
        this.mInVirtualLayout = false;
        this.mLastHorizontalMeasureSpec = 0;
        this.mLastVerticalMeasureSpec = 0;
        this.mLeft = new ConstraintAnchor(this, ConstraintAnchor.Type.LEFT);
        this.mTop = new ConstraintAnchor(this, ConstraintAnchor.Type.TOP);
        this.mRight = new ConstraintAnchor(this, ConstraintAnchor.Type.RIGHT);
        this.mBottom = new ConstraintAnchor(this, ConstraintAnchor.Type.BOTTOM);
        this.mBaseline = new ConstraintAnchor(this, ConstraintAnchor.Type.BASELINE);
        this.mCenterX = new ConstraintAnchor(this, ConstraintAnchor.Type.CENTER_X);
        this.mCenterY = new ConstraintAnchor(this, ConstraintAnchor.Type.CENTER_Y);
        ConstraintAnchor constraintAnchor = new ConstraintAnchor(this, ConstraintAnchor.Type.CENTER);
        this.mCenter = constraintAnchor;
        this.mListAnchors = new ConstraintAnchor[]{this.mLeft, this.mRight, this.mTop, this.mBottom, this.mBaseline, constraintAnchor};
        this.mAnchors = new ArrayList<>();
        this.mIsInBarrier = new boolean[2];
        this.mListDimensionBehaviors = new DimensionBehaviour[]{DimensionBehaviour.FIXED, DimensionBehaviour.FIXED};
        this.mParent = null;
        this.mDimensionRatio = 0.0f;
        this.mDimensionRatioSide = -1;
        this.mRelX = 0;
        this.mRelY = 0;
        this.mOffsetX = 0;
        this.mOffsetY = 0;
        this.mBaselineDistance = 0;
        float f = DEFAULT_BIAS;
        this.mHorizontalBiasPercent = f;
        this.mVerticalBiasPercent = f;
        this.mContainerItemSkip = 0;
        this.mVisibility = 0;
        this.mAnimated = false;
        this.mDebugName = null;
        this.mType = null;
        this.mGroupsToSolver = false;
        this.mHorizontalChainStyle = 0;
        this.mVerticalChainStyle = 0;
        this.mWeight = new float[]{-1.0f, -1.0f};
        this.mListNextMatchConstraintsWidget = new ConstraintWidget[]{null, null};
        this.mNextChainWidget = new ConstraintWidget[]{null, null};
        this.mHorizontalNextWidget = null;
        this.mVerticalNextWidget = null;
        this.horizontalGroup = -1;
        this.verticalGroup = -1;
        this.mX = i;
        this.mY = i2;
        this.mWidth = i3;
        this.mHeight = i4;
        addAnchors();
    }

    public ConstraintWidget(String str, int i, int i2, int i3, int i4) {
        this(i, i2, i3, i4);
        setDebugName(str);
    }

    public ConstraintWidget(int i, int i2) {
        this(0, 0, i, i2);
    }

    public void ensureWidgetRuns() {
        if (this.horizontalRun == null) {
            this.horizontalRun = new HorizontalWidgetRun(this);
        }
        if (this.verticalRun == null) {
            this.verticalRun = new VerticalWidgetRun(this);
        }
    }

    public ConstraintWidget(String str, int i, int i2) {
        this(i, i2);
        setDebugName(str);
    }

    public void resetSolverVariables(Cache cache) {
        this.mLeft.resetSolverVariable(cache);
        this.mTop.resetSolverVariable(cache);
        this.mRight.resetSolverVariable(cache);
        this.mBottom.resetSolverVariable(cache);
        this.mBaseline.resetSolverVariable(cache);
        this.mCenter.resetSolverVariable(cache);
        this.mCenterX.resetSolverVariable(cache);
        this.mCenterY.resetSolverVariable(cache);
    }

    private void addAnchors() {
        this.mAnchors.add(this.mLeft);
        this.mAnchors.add(this.mTop);
        this.mAnchors.add(this.mRight);
        this.mAnchors.add(this.mBottom);
        this.mAnchors.add(this.mCenterX);
        this.mAnchors.add(this.mCenterY);
        this.mAnchors.add(this.mCenter);
        this.mAnchors.add(this.mBaseline);
    }

    public boolean isRoot() {
        return this.mParent == null;
    }

    public ConstraintWidget getParent() {
        return this.mParent;
    }

    public void setParent(ConstraintWidget constraintWidget) {
        this.mParent = constraintWidget;
    }

    public void setWidthWrapContent(boolean z) {
        this.mIsWidthWrapContent = z;
    }

    public boolean isWidthWrapContent() {
        return this.mIsWidthWrapContent;
    }

    public void setHeightWrapContent(boolean z) {
        this.mIsHeightWrapContent = z;
    }

    public boolean isHeightWrapContent() {
        return this.mIsHeightWrapContent;
    }

    public void connectCircularConstraint(ConstraintWidget constraintWidget, float f, int i) {
        immediateConnect(ConstraintAnchor.Type.CENTER, constraintWidget, ConstraintAnchor.Type.CENTER, i, 0);
        this.mCircleConstraintAngle = f;
    }

    public String getType() {
        return this.mType;
    }

    public void setType(String str) {
        this.mType = str;
    }

    public void setVisibility(int i) {
        this.mVisibility = i;
    }

    public int getVisibility() {
        return this.mVisibility;
    }

    public void setAnimated(boolean z) {
        this.mAnimated = z;
    }

    public boolean isAnimated() {
        return this.mAnimated;
    }

    public String getDebugName() {
        return this.mDebugName;
    }

    public void setDebugName(String str) {
        this.mDebugName = str;
    }

    public void setDebugSolverName(LinearSystem linearSystem, String str) {
        this.mDebugName = str;
        SolverVariable createObjectVariable = linearSystem.createObjectVariable(this.mLeft);
        SolverVariable createObjectVariable2 = linearSystem.createObjectVariable(this.mTop);
        SolverVariable createObjectVariable3 = linearSystem.createObjectVariable(this.mRight);
        SolverVariable createObjectVariable4 = linearSystem.createObjectVariable(this.mBottom);
        createObjectVariable.setName(str + ".left");
        createObjectVariable2.setName(str + ".top");
        createObjectVariable3.setName(str + ".right");
        createObjectVariable4.setName(str + ".bottom");
        linearSystem.createObjectVariable(this.mBaseline).setName(str + ".baseline");
    }

    public void createObjectVariables(LinearSystem linearSystem) {
        linearSystem.createObjectVariable(this.mLeft);
        linearSystem.createObjectVariable(this.mTop);
        linearSystem.createObjectVariable(this.mRight);
        linearSystem.createObjectVariable(this.mBottom);
        if (this.mBaselineDistance > 0) {
            linearSystem.createObjectVariable(this.mBaseline);
        }
    }

    public String toString() {
        return (this.mType != null ? "type: " + this.mType + " " : "") + (this.mDebugName != null ? "id: " + this.mDebugName + " " : "") + "(" + this.mX + ", " + this.mY + ") - (" + this.mWidth + " x " + this.mHeight + ")";
    }

    public int getX() {
        ConstraintWidget constraintWidget = this.mParent;
        if (constraintWidget != null && (constraintWidget instanceof ConstraintWidgetContainer)) {
            return ((ConstraintWidgetContainer) constraintWidget).mPaddingLeft + this.mX;
        }
        return this.mX;
    }

    public int getY() {
        ConstraintWidget constraintWidget = this.mParent;
        if (constraintWidget != null && (constraintWidget instanceof ConstraintWidgetContainer)) {
            return ((ConstraintWidgetContainer) constraintWidget).mPaddingTop + this.mY;
        }
        return this.mY;
    }

    public int getWidth() {
        if (this.mVisibility == 8) {
            return 0;
        }
        return this.mWidth;
    }

    public int getOptimizerWrapWidth() {
        int i = this.mWidth;
        int i2 = 0;
        if (this.mListDimensionBehaviors[0] != DimensionBehaviour.MATCH_CONSTRAINT) {
            return i;
        }
        if (this.mMatchConstraintDefaultWidth == 1) {
            i2 = Math.max(this.mMatchConstraintMinWidth, i);
        } else {
            int i3 = this.mMatchConstraintMinWidth;
            if (i3 > 0) {
                this.mWidth = i3;
                i2 = i3;
            }
        }
        int i4 = this.mMatchConstraintMaxWidth;
        return (i4 <= 0 || i4 >= i2) ? i2 : i4;
    }

    public int getOptimizerWrapHeight() {
        int i = this.mHeight;
        if (this.mListDimensionBehaviors[1] == DimensionBehaviour.MATCH_CONSTRAINT) {
            if (this.mMatchConstraintDefaultHeight == 1) {
                i = Math.max(this.mMatchConstraintMinHeight, i);
            } else {
                i = this.mMatchConstraintMinHeight;
                if (i > 0) {
                    this.mHeight = i;
                } else {
                    i = 0;
                }
            }
            int i2 = this.mMatchConstraintMaxHeight;
            if (i2 > 0 && i2 < i) {
                return i2;
            }
        }
        return i;
    }

    public int getHeight() {
        if (this.mVisibility == 8) {
            return 0;
        }
        return this.mHeight;
    }

    public int getLength(int i) {
        if (i == 0) {
            return getWidth();
        }
        if (i == 1) {
            return getHeight();
        }
        return 0;
    }

    protected int getRootX() {
        return this.mX + this.mOffsetX;
    }

    protected int getRootY() {
        return this.mY + this.mOffsetY;
    }

    public int getMinWidth() {
        return this.mMinWidth;
    }

    public int getMinHeight() {
        return this.mMinHeight;
    }

    public int getLeft() {
        return getX();
    }

    public int getTop() {
        return getY();
    }

    public int getRight() {
        return getX() + this.mWidth;
    }

    public int getBottom() {
        return getY() + this.mHeight;
    }

    public int getHorizontalMargin() {
        ConstraintAnchor constraintAnchor = this.mLeft;
        int i = constraintAnchor != null ? constraintAnchor.mMargin : 0;
        ConstraintAnchor constraintAnchor2 = this.mRight;
        return constraintAnchor2 != null ? i + constraintAnchor2.mMargin : i;
    }

    public int getVerticalMargin() {
        int i = this.mLeft != null ? this.mTop.mMargin : 0;
        return this.mRight != null ? i + this.mBottom.mMargin : i;
    }

    public float getHorizontalBiasPercent() {
        return this.mHorizontalBiasPercent;
    }

    public float getVerticalBiasPercent() {
        return this.mVerticalBiasPercent;
    }

    public float getBiasPercent(int i) {
        if (i == 0) {
            return this.mHorizontalBiasPercent;
        }
        if (i == 1) {
            return this.mVerticalBiasPercent;
        }
        return -1.0f;
    }

    public boolean hasBaseline() {
        return this.hasBaseline;
    }

    public int getBaselineDistance() {
        return this.mBaselineDistance;
    }

    public Object getCompanionWidget() {
        return this.mCompanionWidget;
    }

    public ArrayList<ConstraintAnchor> getAnchors() {
        return this.mAnchors;
    }

    public void setX(int i) {
        this.mX = i;
    }

    public void setY(int i) {
        this.mY = i;
    }

    public void setOrigin(int i, int i2) {
        this.mX = i;
        this.mY = i2;
    }

    public void setOffset(int i, int i2) {
        this.mOffsetX = i;
        this.mOffsetY = i2;
    }

    public void setGoneMargin(ConstraintAnchor.Type type, int i) {
        int i2 = AnonymousClass1.$SwitchMap$androidx$constraintlayout$core$widgets$ConstraintAnchor$Type[type.ordinal()];
        if (i2 == 1) {
            this.mLeft.mGoneMargin = i;
            return;
        }
        if (i2 == 2) {
            this.mTop.mGoneMargin = i;
            return;
        }
        if (i2 == 3) {
            this.mRight.mGoneMargin = i;
        } else if (i2 == 4) {
            this.mBottom.mGoneMargin = i;
        } else {
            if (i2 != 5) {
                return;
            }
            this.mBaseline.mGoneMargin = i;
        }
    }

    public void setWidth(int i) {
        this.mWidth = i;
        int i2 = this.mMinWidth;
        if (i < i2) {
            this.mWidth = i2;
        }
    }

    public void setHeight(int i) {
        this.mHeight = i;
        int i2 = this.mMinHeight;
        if (i < i2) {
            this.mHeight = i2;
        }
    }

    public void setLength(int i, int i2) {
        if (i2 == 0) {
            setWidth(i);
        } else if (i2 == 1) {
            setHeight(i);
        }
    }

    public void setHorizontalMatchStyle(int i, int i2, int i3, float f) {
        this.mMatchConstraintDefaultWidth = i;
        this.mMatchConstraintMinWidth = i2;
        if (i3 == Integer.MAX_VALUE) {
            i3 = 0;
        }
        this.mMatchConstraintMaxWidth = i3;
        this.mMatchConstraintPercentWidth = f;
        if (f <= 0.0f || f >= 1.0f || i != 0) {
            return;
        }
        this.mMatchConstraintDefaultWidth = 2;
    }

    public void setVerticalMatchStyle(int i, int i2, int i3, float f) {
        this.mMatchConstraintDefaultHeight = i;
        this.mMatchConstraintMinHeight = i2;
        if (i3 == Integer.MAX_VALUE) {
            i3 = 0;
        }
        this.mMatchConstraintMaxHeight = i3;
        this.mMatchConstraintPercentHeight = f;
        if (f <= 0.0f || f >= 1.0f || i != 0) {
            return;
        }
        this.mMatchConstraintDefaultHeight = 2;
    }

    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:38:0x0086 -> B:31:0x0087). Please report as a decompilation issue!!! */
    public void setDimensionRatio(String str) {
        float f;
        int i = 0;
        if (str == null || str.length() == 0) {
            this.mDimensionRatio = 0.0f;
            return;
        }
        int length = str.length();
        int indexOf = str.indexOf(44);
        int i2 = 0;
        int i3 = -1;
        if (indexOf > 0 && indexOf < length - 1) {
            String substring = str.substring(0, indexOf);
            if (!substring.equalsIgnoreCase("W")) {
                i2 = substring.equalsIgnoreCase("H") ? 1 : -1;
            }
            i3 = i2;
            i2 = indexOf + 1;
        }
        int indexOf2 = str.indexOf(58);
        if (indexOf2 >= 0 && indexOf2 < length - 1) {
            String substring2 = str.substring(i2, indexOf2);
            String substring3 = str.substring(indexOf2 + 1);
            if (substring2.length() > 0 && substring3.length() > 0) {
                float parseFloat = Float.parseFloat(substring2);
                float parseFloat2 = Float.parseFloat(substring3);
                if (parseFloat > 0.0f && parseFloat2 > 0.0f) {
                    if (i3 == 1) {
                        f = Math.abs(parseFloat2 / parseFloat);
                    } else {
                        f = Math.abs(parseFloat / parseFloat2);
                    }
                }
            }
            f = i;
        } else {
            String substring4 = str.substring(i2);
            if (substring4.length() > 0) {
                f = Float.parseFloat(substring4);
            }
            f = i;
        }
        i = (f > i ? 1 : (f == i ? 0 : -1));
        if (i > 0) {
            this.mDimensionRatio = f;
            this.mDimensionRatioSide = i3;
        }
    }

    public void setDimensionRatio(float f, int i) {
        this.mDimensionRatio = f;
        this.mDimensionRatioSide = i;
    }

    public float getDimensionRatio() {
        return this.mDimensionRatio;
    }

    public int getDimensionRatioSide() {
        return this.mDimensionRatioSide;
    }

    public void setHorizontalBiasPercent(float f) {
        this.mHorizontalBiasPercent = f;
    }

    public void setVerticalBiasPercent(float f) {
        this.mVerticalBiasPercent = f;
    }

    public void setMinWidth(int i) {
        if (i < 0) {
            this.mMinWidth = 0;
        } else {
            this.mMinWidth = i;
        }
    }

    public void setMinHeight(int i) {
        if (i < 0) {
            this.mMinHeight = 0;
        } else {
            this.mMinHeight = i;
        }
    }

    public void setDimension(int i, int i2) {
        this.mWidth = i;
        int i3 = this.mMinWidth;
        if (i < i3) {
            this.mWidth = i3;
        }
        this.mHeight = i2;
        int i4 = this.mMinHeight;
        if (i2 < i4) {
            this.mHeight = i4;
        }
    }

    public void setFrame(int i, int i2, int i3, int i4) {
        int i5;
        int i6;
        int i7 = i3 - i;
        int i8 = i4 - i2;
        this.mX = i;
        this.mY = i2;
        if (this.mVisibility == 8) {
            this.mWidth = 0;
            this.mHeight = 0;
            return;
        }
        if (this.mListDimensionBehaviors[0] == DimensionBehaviour.FIXED && i7 < (i6 = this.mWidth)) {
            i7 = i6;
        }
        if (this.mListDimensionBehaviors[1] == DimensionBehaviour.FIXED && i8 < (i5 = this.mHeight)) {
            i8 = i5;
        }
        this.mWidth = i7;
        this.mHeight = i8;
        int i9 = this.mMinHeight;
        if (i8 < i9) {
            this.mHeight = i9;
        }
        int i10 = this.mMinWidth;
        if (i7 < i10) {
            this.mWidth = i10;
        }
        if (this.mMatchConstraintMaxWidth > 0 && this.mListDimensionBehaviors[0] == DimensionBehaviour.MATCH_CONSTRAINT) {
            this.mWidth = Math.min(this.mWidth, this.mMatchConstraintMaxWidth);
        }
        if (this.mMatchConstraintMaxHeight > 0 && this.mListDimensionBehaviors[1] == DimensionBehaviour.MATCH_CONSTRAINT) {
            this.mHeight = Math.min(this.mHeight, this.mMatchConstraintMaxHeight);
        }
        int i11 = this.mWidth;
        if (i7 != i11) {
            this.mWidthOverride = i11;
        }
        int i12 = this.mHeight;
        if (i8 != i12) {
            this.mHeightOverride = i12;
        }
    }

    public void setFrame(int i, int i2, int i3) {
        if (i3 == 0) {
            setHorizontalDimension(i, i2);
        } else if (i3 == 1) {
            setVerticalDimension(i, i2);
        }
    }

    public void setHorizontalDimension(int i, int i2) {
        this.mX = i;
        int i3 = i2 - i;
        this.mWidth = i3;
        int i4 = this.mMinWidth;
        if (i3 < i4) {
            this.mWidth = i4;
        }
    }

    public void setVerticalDimension(int i, int i2) {
        this.mY = i;
        int i3 = i2 - i;
        this.mHeight = i3;
        int i4 = this.mMinHeight;
        if (i3 < i4) {
            this.mHeight = i4;
        }
    }

    int getRelativePositioning(int i) {
        if (i == 0) {
            return this.mRelX;
        }
        if (i == 1) {
            return this.mRelY;
        }
        return 0;
    }

    void setRelativePositioning(int i, int i2) {
        if (i2 == 0) {
            this.mRelX = i;
        } else if (i2 == 1) {
            this.mRelY = i;
        }
    }

    public void setBaselineDistance(int i) {
        this.mBaselineDistance = i;
        this.hasBaseline = i > 0;
    }

    public void setCompanionWidget(Object obj) {
        this.mCompanionWidget = obj;
    }

    public void setContainerItemSkip(int i) {
        if (i >= 0) {
            this.mContainerItemSkip = i;
        } else {
            this.mContainerItemSkip = 0;
        }
    }

    public int getContainerItemSkip() {
        return this.mContainerItemSkip;
    }

    public void setHorizontalWeight(float f) {
        this.mWeight[0] = f;
    }

    public void setVerticalWeight(float f) {
        this.mWeight[1] = f;
    }

    public void setHorizontalChainStyle(int i) {
        this.mHorizontalChainStyle = i;
    }

    public int getHorizontalChainStyle() {
        return this.mHorizontalChainStyle;
    }

    public void setVerticalChainStyle(int i) {
        this.mVerticalChainStyle = i;
    }

    public int getVerticalChainStyle() {
        return this.mVerticalChainStyle;
    }

    public boolean allowedInBarrier() {
        return this.mVisibility != 8;
    }

    public void immediateConnect(ConstraintAnchor.Type type, ConstraintWidget constraintWidget, ConstraintAnchor.Type type2, int i, int i2) {
        getAnchor(type).connect(constraintWidget.getAnchor(type2), i, i2, true);
    }

    public void connect(ConstraintAnchor constraintAnchor, ConstraintAnchor constraintAnchor2, int i) {
        if (constraintAnchor.getOwner() == this) {
            connect(constraintAnchor.getType(), constraintAnchor2.getOwner(), constraintAnchor2.getType(), i);
        }
    }

    public void connect(ConstraintAnchor.Type type, ConstraintWidget constraintWidget, ConstraintAnchor.Type type2) {
        connect(type, constraintWidget, type2, 0);
    }

    public void connect(ConstraintAnchor.Type type, ConstraintWidget constraintWidget, ConstraintAnchor.Type type2, int i) {
        boolean z;
        if (type == ConstraintAnchor.Type.CENTER) {
            if (type2 == ConstraintAnchor.Type.CENTER) {
                ConstraintAnchor anchor = getAnchor(ConstraintAnchor.Type.LEFT);
                ConstraintAnchor anchor2 = getAnchor(ConstraintAnchor.Type.RIGHT);
                ConstraintAnchor anchor3 = getAnchor(ConstraintAnchor.Type.TOP);
                ConstraintAnchor anchor4 = getAnchor(ConstraintAnchor.Type.BOTTOM);
                boolean z2 = true;
                if ((anchor == null || !anchor.isConnected()) && (anchor2 == null || !anchor2.isConnected())) {
                    connect(ConstraintAnchor.Type.LEFT, constraintWidget, ConstraintAnchor.Type.LEFT, 0);
                    connect(ConstraintAnchor.Type.RIGHT, constraintWidget, ConstraintAnchor.Type.RIGHT, 0);
                    z = true;
                } else {
                    z = false;
                }
                if ((anchor3 == null || !anchor3.isConnected()) && (anchor4 == null || !anchor4.isConnected())) {
                    connect(ConstraintAnchor.Type.TOP, constraintWidget, ConstraintAnchor.Type.TOP, 0);
                    connect(ConstraintAnchor.Type.BOTTOM, constraintWidget, ConstraintAnchor.Type.BOTTOM, 0);
                } else {
                    z2 = false;
                }
                if (z && z2) {
                    getAnchor(ConstraintAnchor.Type.CENTER).connect(constraintWidget.getAnchor(ConstraintAnchor.Type.CENTER), 0);
                    return;
                } else if (z) {
                    getAnchor(ConstraintAnchor.Type.CENTER_X).connect(constraintWidget.getAnchor(ConstraintAnchor.Type.CENTER_X), 0);
                    return;
                } else {
                    if (z2) {
                        getAnchor(ConstraintAnchor.Type.CENTER_Y).connect(constraintWidget.getAnchor(ConstraintAnchor.Type.CENTER_Y), 0);
                        return;
                    }
                    return;
                }
            }
            if (type2 == ConstraintAnchor.Type.LEFT || type2 == ConstraintAnchor.Type.RIGHT) {
                connect(ConstraintAnchor.Type.LEFT, constraintWidget, type2, 0);
                connect(ConstraintAnchor.Type.RIGHT, constraintWidget, type2, 0);
                getAnchor(ConstraintAnchor.Type.CENTER).connect(constraintWidget.getAnchor(type2), 0);
                return;
            } else {
                if (type2 == ConstraintAnchor.Type.TOP || type2 == ConstraintAnchor.Type.BOTTOM) {
                    connect(ConstraintAnchor.Type.TOP, constraintWidget, type2, 0);
                    connect(ConstraintAnchor.Type.BOTTOM, constraintWidget, type2, 0);
                    getAnchor(ConstraintAnchor.Type.CENTER).connect(constraintWidget.getAnchor(type2), 0);
                    return;
                }
                return;
            }
        }
        if (type == ConstraintAnchor.Type.CENTER_X && (type2 == ConstraintAnchor.Type.LEFT || type2 == ConstraintAnchor.Type.RIGHT)) {
            ConstraintAnchor anchor5 = getAnchor(ConstraintAnchor.Type.LEFT);
            ConstraintAnchor anchor6 = constraintWidget.getAnchor(type2);
            ConstraintAnchor anchor7 = getAnchor(ConstraintAnchor.Type.RIGHT);
            anchor5.connect(anchor6, 0);
            anchor7.connect(anchor6, 0);
            getAnchor(ConstraintAnchor.Type.CENTER_X).connect(anchor6, 0);
            return;
        }
        if (type == ConstraintAnchor.Type.CENTER_Y && (type2 == ConstraintAnchor.Type.TOP || type2 == ConstraintAnchor.Type.BOTTOM)) {
            ConstraintAnchor anchor8 = constraintWidget.getAnchor(type2);
            getAnchor(ConstraintAnchor.Type.TOP).connect(anchor8, 0);
            getAnchor(ConstraintAnchor.Type.BOTTOM).connect(anchor8, 0);
            getAnchor(ConstraintAnchor.Type.CENTER_Y).connect(anchor8, 0);
            return;
        }
        if (type == ConstraintAnchor.Type.CENTER_X && type2 == ConstraintAnchor.Type.CENTER_X) {
            getAnchor(ConstraintAnchor.Type.LEFT).connect(constraintWidget.getAnchor(ConstraintAnchor.Type.LEFT), 0);
            getAnchor(ConstraintAnchor.Type.RIGHT).connect(constraintWidget.getAnchor(ConstraintAnchor.Type.RIGHT), 0);
            getAnchor(ConstraintAnchor.Type.CENTER_X).connect(constraintWidget.getAnchor(type2), 0);
            return;
        }
        if (type == ConstraintAnchor.Type.CENTER_Y && type2 == ConstraintAnchor.Type.CENTER_Y) {
            getAnchor(ConstraintAnchor.Type.TOP).connect(constraintWidget.getAnchor(ConstraintAnchor.Type.TOP), 0);
            getAnchor(ConstraintAnchor.Type.BOTTOM).connect(constraintWidget.getAnchor(ConstraintAnchor.Type.BOTTOM), 0);
            getAnchor(ConstraintAnchor.Type.CENTER_Y).connect(constraintWidget.getAnchor(type2), 0);
            return;
        }
        ConstraintAnchor anchor9 = getAnchor(type);
        ConstraintAnchor anchor10 = constraintWidget.getAnchor(type2);
        if (anchor9.isValidConnection(anchor10)) {
            if (type == ConstraintAnchor.Type.BASELINE) {
                ConstraintAnchor anchor11 = getAnchor(ConstraintAnchor.Type.TOP);
                ConstraintAnchor anchor12 = getAnchor(ConstraintAnchor.Type.BOTTOM);
                if (anchor11 != null) {
                    anchor11.reset();
                }
                if (anchor12 != null) {
                    anchor12.reset();
                }
            } else if (type == ConstraintAnchor.Type.TOP || type == ConstraintAnchor.Type.BOTTOM) {
                ConstraintAnchor anchor13 = getAnchor(ConstraintAnchor.Type.BASELINE);
                if (anchor13 != null) {
                    anchor13.reset();
                }
                ConstraintAnchor anchor14 = getAnchor(ConstraintAnchor.Type.CENTER);
                if (anchor14.getTarget() != anchor10) {
                    anchor14.reset();
                }
                ConstraintAnchor opposite = getAnchor(type).getOpposite();
                ConstraintAnchor anchor15 = getAnchor(ConstraintAnchor.Type.CENTER_Y);
                if (anchor15.isConnected()) {
                    opposite.reset();
                    anchor15.reset();
                }
            } else if (type == ConstraintAnchor.Type.LEFT || type == ConstraintAnchor.Type.RIGHT) {
                ConstraintAnchor anchor16 = getAnchor(ConstraintAnchor.Type.CENTER);
                if (anchor16.getTarget() != anchor10) {
                    anchor16.reset();
                }
                ConstraintAnchor opposite2 = getAnchor(type).getOpposite();
                ConstraintAnchor anchor17 = getAnchor(ConstraintAnchor.Type.CENTER_X);
                if (anchor17.isConnected()) {
                    opposite2.reset();
                    anchor17.reset();
                }
            }
            anchor9.connect(anchor10, i);
        }
    }

    public void resetAllConstraints() {
        resetAnchors();
        setVerticalBiasPercent(DEFAULT_BIAS);
        setHorizontalBiasPercent(DEFAULT_BIAS);
    }

    public void resetAnchor(ConstraintAnchor constraintAnchor) {
        if (getParent() != null && (getParent() instanceof ConstraintWidgetContainer) && ((ConstraintWidgetContainer) getParent()).handlesInternalConstraints()) {
            return;
        }
        ConstraintAnchor anchor = getAnchor(ConstraintAnchor.Type.LEFT);
        ConstraintAnchor anchor2 = getAnchor(ConstraintAnchor.Type.RIGHT);
        ConstraintAnchor anchor3 = getAnchor(ConstraintAnchor.Type.TOP);
        ConstraintAnchor anchor4 = getAnchor(ConstraintAnchor.Type.BOTTOM);
        ConstraintAnchor anchor5 = getAnchor(ConstraintAnchor.Type.CENTER);
        ConstraintAnchor anchor6 = getAnchor(ConstraintAnchor.Type.CENTER_X);
        ConstraintAnchor anchor7 = getAnchor(ConstraintAnchor.Type.CENTER_Y);
        if (constraintAnchor == anchor5) {
            if (anchor.isConnected() && anchor2.isConnected() && anchor.getTarget() == anchor2.getTarget()) {
                anchor.reset();
                anchor2.reset();
            }
            if (anchor3.isConnected() && anchor4.isConnected() && anchor3.getTarget() == anchor4.getTarget()) {
                anchor3.reset();
                anchor4.reset();
            }
            this.mHorizontalBiasPercent = 0.5f;
            this.mVerticalBiasPercent = 0.5f;
        } else if (constraintAnchor == anchor6) {
            if (anchor.isConnected() && anchor2.isConnected() && anchor.getTarget().getOwner() == anchor2.getTarget().getOwner()) {
                anchor.reset();
                anchor2.reset();
            }
            this.mHorizontalBiasPercent = 0.5f;
        } else if (constraintAnchor == anchor7) {
            if (anchor3.isConnected() && anchor4.isConnected() && anchor3.getTarget().getOwner() == anchor4.getTarget().getOwner()) {
                anchor3.reset();
                anchor4.reset();
            }
            this.mVerticalBiasPercent = 0.5f;
        } else if (constraintAnchor == anchor || constraintAnchor == anchor2) {
            if (anchor.isConnected() && anchor.getTarget() == anchor2.getTarget()) {
                anchor5.reset();
            }
        } else if ((constraintAnchor == anchor3 || constraintAnchor == anchor4) && anchor3.isConnected() && anchor3.getTarget() == anchor4.getTarget()) {
            anchor5.reset();
        }
        constraintAnchor.reset();
    }

    public void resetAnchors() {
        ConstraintWidget parent = getParent();
        if (parent != null && (parent instanceof ConstraintWidgetContainer) && ((ConstraintWidgetContainer) getParent()).handlesInternalConstraints()) {
            return;
        }
        int size = this.mAnchors.size();
        for (int i = 0; i < size; i++) {
            this.mAnchors.get(i).reset();
        }
    }

    public ConstraintAnchor getAnchor(ConstraintAnchor.Type type) {
        switch (AnonymousClass1.$SwitchMap$androidx$constraintlayout$core$widgets$ConstraintAnchor$Type[type.ordinal()]) {
            case 1:
                return this.mLeft;
            case 2:
                return this.mTop;
            case 3:
                return this.mRight;
            case 4:
                return this.mBottom;
            case 5:
                return this.mBaseline;
            case 6:
                return this.mCenter;
            case 7:
                return this.mCenterX;
            case 8:
                return this.mCenterY;
            case 9:
                return null;
            default:
                throw new AssertionError(type.name());
        }
    }

    public DimensionBehaviour getHorizontalDimensionBehaviour() {
        return this.mListDimensionBehaviors[0];
    }

    public DimensionBehaviour getVerticalDimensionBehaviour() {
        return this.mListDimensionBehaviors[1];
    }

    public DimensionBehaviour getDimensionBehaviour(int i) {
        if (i == 0) {
            return getHorizontalDimensionBehaviour();
        }
        if (i == 1) {
            return getVerticalDimensionBehaviour();
        }
        return null;
    }

    public void setHorizontalDimensionBehaviour(DimensionBehaviour dimensionBehaviour) {
        this.mListDimensionBehaviors[0] = dimensionBehaviour;
    }

    public void setVerticalDimensionBehaviour(DimensionBehaviour dimensionBehaviour) {
        this.mListDimensionBehaviors[1] = dimensionBehaviour;
    }

    public boolean isInHorizontalChain() {
        if (this.mLeft.mTarget == null || this.mLeft.mTarget.mTarget != this.mLeft) {
            return this.mRight.mTarget != null && this.mRight.mTarget.mTarget == this.mRight;
        }
        return true;
    }

    public ConstraintWidget getPreviousChainMember(int i) {
        if (i == 0) {
            if (this.mLeft.mTarget == null) {
                return null;
            }
            ConstraintAnchor constraintAnchor = this.mLeft.mTarget.mTarget;
            ConstraintAnchor constraintAnchor2 = this.mLeft;
            if (constraintAnchor == constraintAnchor2) {
                return constraintAnchor2.mTarget.mOwner;
            }
            return null;
        }
        if (i != 1 || this.mTop.mTarget == null) {
            return null;
        }
        ConstraintAnchor constraintAnchor3 = this.mTop.mTarget.mTarget;
        ConstraintAnchor constraintAnchor4 = this.mTop;
        if (constraintAnchor3 == constraintAnchor4) {
            return constraintAnchor4.mTarget.mOwner;
        }
        return null;
    }

    public ConstraintWidget getNextChainMember(int i) {
        if (i == 0) {
            if (this.mRight.mTarget == null) {
                return null;
            }
            ConstraintAnchor constraintAnchor = this.mRight.mTarget.mTarget;
            ConstraintAnchor constraintAnchor2 = this.mRight;
            if (constraintAnchor == constraintAnchor2) {
                return constraintAnchor2.mTarget.mOwner;
            }
            return null;
        }
        if (i != 1 || this.mBottom.mTarget == null) {
            return null;
        }
        ConstraintAnchor constraintAnchor3 = this.mBottom.mTarget.mTarget;
        ConstraintAnchor constraintAnchor4 = this.mBottom;
        if (constraintAnchor3 == constraintAnchor4) {
            return constraintAnchor4.mTarget.mOwner;
        }
        return null;
    }

    public ConstraintWidget getHorizontalChainControlWidget() {
        if (!isInHorizontalChain()) {
            return null;
        }
        ConstraintWidget constraintWidget = this;
        ConstraintWidget constraintWidget2 = null;
        while (constraintWidget2 == null && constraintWidget != null) {
            ConstraintAnchor anchor = constraintWidget.getAnchor(ConstraintAnchor.Type.LEFT);
            ConstraintAnchor target = anchor == null ? null : anchor.getTarget();
            ConstraintWidget owner = target == null ? null : target.getOwner();
            if (owner == getParent()) {
                return constraintWidget;
            }
            ConstraintAnchor target2 = owner == null ? null : owner.getAnchor(ConstraintAnchor.Type.RIGHT).getTarget();
            if (target2 == null || target2.getOwner() == constraintWidget) {
                constraintWidget = owner;
            } else {
                constraintWidget2 = constraintWidget;
            }
        }
        return constraintWidget2;
    }

    public boolean isInVerticalChain() {
        if (this.mTop.mTarget == null || this.mTop.mTarget.mTarget != this.mTop) {
            return this.mBottom.mTarget != null && this.mBottom.mTarget.mTarget == this.mBottom;
        }
        return true;
    }

    public ConstraintWidget getVerticalChainControlWidget() {
        if (!isInVerticalChain()) {
            return null;
        }
        ConstraintWidget constraintWidget = this;
        ConstraintWidget constraintWidget2 = null;
        while (constraintWidget2 == null && constraintWidget != null) {
            ConstraintAnchor anchor = constraintWidget.getAnchor(ConstraintAnchor.Type.TOP);
            ConstraintAnchor target = anchor == null ? null : anchor.getTarget();
            ConstraintWidget owner = target == null ? null : target.getOwner();
            if (owner == getParent()) {
                return constraintWidget;
            }
            ConstraintAnchor target2 = owner == null ? null : owner.getAnchor(ConstraintAnchor.Type.BOTTOM).getTarget();
            if (target2 == null || target2.getOwner() == constraintWidget) {
                constraintWidget = owner;
            } else {
                constraintWidget2 = constraintWidget;
            }
        }
        return constraintWidget2;
    }

    private boolean isChainHead(int i) {
        int i2 = i * 2;
        if (this.mListAnchors[i2].mTarget == null) {
            return false;
        }
        ConstraintAnchor constraintAnchor = this.mListAnchors[i2].mTarget.mTarget;
        ConstraintAnchor[] constraintAnchorArr = this.mListAnchors;
        if (constraintAnchor == constraintAnchorArr[i2]) {
            return false;
        }
        int i3 = i2 + 1;
        return constraintAnchorArr[i3].mTarget != null && this.mListAnchors[i3].mTarget.mTarget == this.mListAnchors[i3];
    }

    /* JADX WARN: Code restructure failed: missing block: B:15:0x004c, code lost:
    
        if (r13 != 3) goto L23;
     */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:101:0x022f  */
    /* JADX WARN: Removed duplicated region for block: B:104:0x0236  */
    /* JADX WARN: Removed duplicated region for block: B:107:0x023f  */
    /* JADX WARN: Removed duplicated region for block: B:110:0x024e  */
    /* JADX WARN: Removed duplicated region for block: B:134:0x0314  */
    /* JADX WARN: Removed duplicated region for block: B:139:0x0323  */
    /* JADX WARN: Removed duplicated region for block: B:145:0x0339  */
    /* JADX WARN: Removed duplicated region for block: B:149:0x034a  */
    /* JADX WARN: Removed duplicated region for block: B:152:0x0366  */
    /* JADX WARN: Removed duplicated region for block: B:172:0x03bb  */
    /* JADX WARN: Removed duplicated region for block: B:175:0x03c8  */
    /* JADX WARN: Removed duplicated region for block: B:178:0x040e  */
    /* JADX WARN: Removed duplicated region for block: B:181:0x0450  */
    /* JADX WARN: Removed duplicated region for block: B:197:0x04b9  */
    /* JADX WARN: Removed duplicated region for block: B:199:0x04be  */
    /* JADX WARN: Removed duplicated region for block: B:223:0x059f  */
    /* JADX WARN: Removed duplicated region for block: B:229:0x05d1  */
    /* JADX WARN: Removed duplicated region for block: B:248:0x04bb  */
    /* JADX WARN: Removed duplicated region for block: B:252:0x0411  */
    /* JADX WARN: Removed duplicated region for block: B:253:0x03cf  */
    /* JADX WARN: Removed duplicated region for block: B:254:0x03c2  */
    /* JADX WARN: Removed duplicated region for block: B:256:0x034c  */
    /* JADX WARN: Removed duplicated region for block: B:260:0x031e  */
    /* JADX WARN: Removed duplicated region for block: B:282:0x0250  */
    /* JADX WARN: Removed duplicated region for block: B:283:0x0241  */
    /* JADX WARN: Removed duplicated region for block: B:286:0x0226  */
    /* JADX WARN: Removed duplicated region for block: B:287:0x0107  */
    /* JADX WARN: Removed duplicated region for block: B:289:0x007b  */
    /* JADX WARN: Removed duplicated region for block: B:299:0x00ac  */
    /* JADX WARN: Removed duplicated region for block: B:34:0x00fc  */
    /* JADX WARN: Removed duplicated region for block: B:69:0x01a9  */
    /* JADX WARN: Removed duplicated region for block: B:72:0x01b5  */
    /* JADX WARN: Type inference failed for: r13v21 */
    /* JADX WARN: Type inference failed for: r13v22 */
    /* JADX WARN: Type inference failed for: r13v50 */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void addToSolver(LinearSystem linearSystem, boolean z) {
        boolean z2;
        boolean z3;
        boolean z4;
        ConstraintWidget constraintWidget;
        ConstraintWidget constraintWidget2;
        int i;
        boolean z5;
        boolean z6;
        int i2;
        int i3;
        int i4;
        int i5;
        float f;
        int i6;
        SolverVariable solverVariable;
        int i7;
        int i8;
        boolean z7;
        int i9;
        boolean z8;
        boolean z9;
        boolean z10;
        boolean z11;
        int i10;
        boolean z12;
        boolean z13;
        SolverVariable solverVariable2;
        SolverVariable solverVariable3;
        boolean z14;
        SolverVariable solverVariable4;
        boolean z15;
        boolean z16;
        int i11;
        SolverVariable solverVariable5;
        SolverVariable solverVariable6;
        int i12;
        boolean z17;
        SolverVariable solverVariable7;
        SolverVariable solverVariable8;
        SolverVariable solverVariable9;
        int i13;
        int i14;
        ?? r13;
        int i15;
        ConstraintWidget constraintWidget3;
        SolverVariable solverVariable10;
        SolverVariable solverVariable11;
        LinearSystem linearSystem2;
        VerticalWidgetRun verticalWidgetRun;
        boolean z18;
        HorizontalWidgetRun horizontalWidgetRun;
        int i16;
        boolean isInHorizontalChain;
        int i17;
        HorizontalWidgetRun horizontalWidgetRun2;
        boolean[] zArr;
        LinearSystem linearSystem3 = linearSystem;
        SolverVariable createObjectVariable = linearSystem3.createObjectVariable(this.mLeft);
        SolverVariable createObjectVariable2 = linearSystem3.createObjectVariable(this.mRight);
        SolverVariable createObjectVariable3 = linearSystem3.createObjectVariable(this.mTop);
        SolverVariable createObjectVariable4 = linearSystem3.createObjectVariable(this.mBottom);
        SolverVariable createObjectVariable5 = linearSystem3.createObjectVariable(this.mBaseline);
        ConstraintWidget constraintWidget4 = this.mParent;
        if (constraintWidget4 != null) {
            z2 = constraintWidget4 != null && constraintWidget4.mListDimensionBehaviors[0] == DimensionBehaviour.WRAP_CONTENT;
            ConstraintWidget constraintWidget5 = this.mParent;
            z3 = constraintWidget5 != null && constraintWidget5.mListDimensionBehaviors[1] == DimensionBehaviour.WRAP_CONTENT;
            int i18 = this.mWrapBehaviorInParent;
            if (i18 == 1) {
                z3 = false;
            } else if (i18 == 2) {
                z2 = false;
            }
            if (this.mVisibility == 8 && !this.mAnimated && !hasDependencies()) {
                zArr = this.mIsInBarrier;
                if (!zArr[0] && !zArr[1]) {
                    return;
                }
            }
            z4 = this.resolvedHorizontal;
            if (!z4 || this.resolvedVertical) {
                if (z4) {
                    linearSystem3.addEquality(createObjectVariable, this.mX);
                    linearSystem3.addEquality(createObjectVariable2, this.mX + this.mWidth);
                    if (z2 && (constraintWidget2 = this.mParent) != null) {
                        if (this.OPTIMIZE_WRAP_ON_RESOLVED) {
                            ConstraintWidgetContainer constraintWidgetContainer = (ConstraintWidgetContainer) constraintWidget2;
                            constraintWidgetContainer.addHorizontalWrapMinVariable(this.mLeft);
                            constraintWidgetContainer.addHorizontalWrapMaxVariable(this.mRight);
                        } else {
                            linearSystem3.addGreaterThan(linearSystem3.createObjectVariable(constraintWidget2.mRight), createObjectVariable2, 0, 5);
                        }
                    }
                }
                if (this.resolvedVertical) {
                    linearSystem3.addEquality(createObjectVariable3, this.mY);
                    linearSystem3.addEquality(createObjectVariable4, this.mY + this.mHeight);
                    if (this.mBaseline.hasDependents()) {
                        linearSystem3.addEquality(createObjectVariable5, this.mY + this.mBaselineDistance);
                    }
                    if (z3 && (constraintWidget = this.mParent) != null) {
                        if (this.OPTIMIZE_WRAP_ON_RESOLVED) {
                            ConstraintWidgetContainer constraintWidgetContainer2 = (ConstraintWidgetContainer) constraintWidget;
                            constraintWidgetContainer2.addVerticalWrapMinVariable(this.mTop);
                            constraintWidgetContainer2.addVerticalWrapMaxVariable(this.mBottom);
                        } else {
                            linearSystem3.addGreaterThan(linearSystem3.createObjectVariable(constraintWidget.mBottom), createObjectVariable4, 0, 5);
                        }
                    }
                }
                if (this.resolvedHorizontal && this.resolvedVertical) {
                    this.resolvedHorizontal = false;
                    this.resolvedVertical = false;
                    return;
                }
            }
            if (LinearSystem.sMetrics == null) {
                i = 1;
                LinearSystem.sMetrics.widgets++;
            } else {
                i = 1;
            }
            if (!z && (horizontalWidgetRun2 = this.horizontalRun) != null && this.verticalRun != null && horizontalWidgetRun2.start.resolved && this.horizontalRun.end.resolved && this.verticalRun.start.resolved && this.verticalRun.end.resolved) {
                if (LinearSystem.sMetrics != null) {
                    LinearSystem.sMetrics.graphSolved++;
                }
                linearSystem3.addEquality(createObjectVariable, this.horizontalRun.start.value);
                linearSystem3.addEquality(createObjectVariable2, this.horizontalRun.end.value);
                linearSystem3.addEquality(createObjectVariable3, this.verticalRun.start.value);
                linearSystem3.addEquality(createObjectVariable4, this.verticalRun.end.value);
                linearSystem3.addEquality(createObjectVariable5, this.verticalRun.baseline.value);
                if (this.mParent != null) {
                    if (z2 && this.isTerminalWidget[0] && !isInHorizontalChain()) {
                        linearSystem3.addGreaterThan(linearSystem3.createObjectVariable(this.mParent.mRight), createObjectVariable2, 0, 8);
                    }
                    if (z3 && this.isTerminalWidget[i] && !isInVerticalChain()) {
                        linearSystem3.addGreaterThan(linearSystem3.createObjectVariable(this.mParent.mBottom), createObjectVariable4, 0, 8);
                    }
                }
                this.resolvedHorizontal = false;
                this.resolvedVertical = false;
                return;
            }
            if (LinearSystem.sMetrics != null) {
                LinearSystem.sMetrics.linearSolved++;
            }
            if (this.mParent == null) {
                if (isChainHead(0)) {
                    ((ConstraintWidgetContainer) this.mParent).addChain(this, 0);
                    isInHorizontalChain = i;
                    i17 = isInHorizontalChain;
                } else {
                    isInHorizontalChain = isInHorizontalChain();
                    i17 = i;
                }
                if (isChainHead(i17)) {
                    ((ConstraintWidgetContainer) this.mParent).addChain(this, i17);
                    z5 = true;
                } else {
                    z5 = isInVerticalChain();
                }
                if (!isInHorizontalChain && z2 && this.mVisibility != 8 && this.mLeft.mTarget == null && this.mRight.mTarget == null) {
                    linearSystem3.addGreaterThan(linearSystem3.createObjectVariable(this.mParent.mRight), createObjectVariable2, 0, 1);
                }
                if (!z5 && z3 && this.mVisibility != 8 && this.mTop.mTarget == null && this.mBottom.mTarget == null && this.mBaseline == null) {
                    linearSystem3.addGreaterThan(linearSystem3.createObjectVariable(this.mParent.mBottom), createObjectVariable4, 0, 1);
                }
                z6 = isInHorizontalChain;
            } else {
                z5 = false;
                z6 = false;
            }
            i2 = this.mWidth;
            i3 = this.mMinWidth;
            if (i2 < i3) {
                i2 = i3;
            }
            i4 = this.mHeight;
            i5 = this.mMinHeight;
            if (i4 < i5) {
                i4 = i5;
            }
            boolean z19 = this.mListDimensionBehaviors[0] == DimensionBehaviour.MATCH_CONSTRAINT;
            boolean z20 = this.mListDimensionBehaviors[1] == DimensionBehaviour.MATCH_CONSTRAINT;
            this.mResolvedDimensionRatioSide = this.mDimensionRatioSide;
            f = this.mDimensionRatio;
            this.mResolvedDimensionRatio = f;
            int i19 = this.mMatchConstraintDefaultWidth;
            i6 = this.mMatchConstraintDefaultHeight;
            if (f <= 0.0f && this.mVisibility != 8) {
                if (this.mListDimensionBehaviors[0] == DimensionBehaviour.MATCH_CONSTRAINT && i19 == 0) {
                    i19 = 3;
                }
                i8 = (this.mListDimensionBehaviors[1] == DimensionBehaviour.MATCH_CONSTRAINT && i6 == 0) ? 3 : i6;
                solverVariable = createObjectVariable4;
                if (this.mListDimensionBehaviors[0] == DimensionBehaviour.MATCH_CONSTRAINT && this.mListDimensionBehaviors[1] == DimensionBehaviour.MATCH_CONSTRAINT && i19 == 3 && i8 == 3) {
                    setupDimensionRatio(z2, z3, z19, z20);
                } else if (this.mListDimensionBehaviors[0] == DimensionBehaviour.MATCH_CONSTRAINT && i19 == 3) {
                    this.mResolvedDimensionRatioSide = 0;
                    i2 = (int) (this.mResolvedDimensionRatio * this.mHeight);
                    i7 = i4;
                    if (this.mListDimensionBehaviors[1] == DimensionBehaviour.MATCH_CONSTRAINT) {
                        z7 = true;
                        int[] iArr = this.mResolvedMatchConstraintDefault;
                        iArr[0] = i19;
                        iArr[1] = i8;
                        this.mResolvedHasRatio = z7;
                        if (z7) {
                            i9 = -1;
                        } else {
                            int i20 = this.mResolvedDimensionRatioSide;
                            i9 = -1;
                            if (i20 == 0 || i20 == -1) {
                                z8 = true;
                                boolean z21 = !z7 && ((i16 = this.mResolvedDimensionRatioSide) == 1 || i16 == i9);
                                if (this.mListDimensionBehaviors[0] == DimensionBehaviour.WRAP_CONTENT || !(this instanceof ConstraintWidgetContainer)) {
                                    z9 = z6;
                                    z10 = z5;
                                    z11 = false;
                                } else {
                                    z9 = z6;
                                    z10 = z5;
                                    z11 = true;
                                }
                                i10 = z11 ? 0 : i2;
                                z12 = !this.mCenter.isConnected();
                                boolean[] zArr2 = this.mIsInBarrier;
                                z13 = zArr2[0];
                                boolean z22 = zArr2[1];
                                if (this.mHorizontalResolution != 2 && !this.resolvedHorizontal) {
                                    if (z || (horizontalWidgetRun = this.horizontalRun) == null || !horizontalWidgetRun.start.resolved || !this.horizontalRun.end.resolved) {
                                        ConstraintWidget constraintWidget6 = this.mParent;
                                        SolverVariable createObjectVariable6 = constraintWidget6 == null ? linearSystem3.createObjectVariable(constraintWidget6.mRight) : null;
                                        ConstraintWidget constraintWidget7 = this.mParent;
                                        SolverVariable createObjectVariable7 = constraintWidget7 == null ? linearSystem3.createObjectVariable(constraintWidget7.mLeft) : null;
                                        boolean z23 = this.isTerminalWidget[0];
                                        DimensionBehaviour[] dimensionBehaviourArr = this.mListDimensionBehaviors;
                                        z15 = z2;
                                        SolverVariable solverVariable12 = createObjectVariable6;
                                        DimensionBehaviour dimensionBehaviour = dimensionBehaviourArr[0];
                                        ConstraintAnchor constraintAnchor = this.mLeft;
                                        z16 = z9;
                                        ConstraintAnchor constraintAnchor2 = this.mRight;
                                        i12 = i8;
                                        z17 = z3;
                                        int i21 = this.mX;
                                        i11 = i19;
                                        int i22 = this.mMinWidth;
                                        int i23 = this.mMaxDimension[0];
                                        float f2 = this.mHorizontalBiasPercent;
                                        solverVariable2 = createObjectVariable;
                                        boolean z24 = dimensionBehaviourArr[1] != DimensionBehaviour.MATCH_CONSTRAINT;
                                        z14 = z7;
                                        solverVariable4 = createObjectVariable5;
                                        solverVariable3 = createObjectVariable2;
                                        solverVariable6 = solverVariable;
                                        solverVariable5 = createObjectVariable3;
                                        linearSystem3 = linearSystem;
                                        applyConstraints(linearSystem3, true, z15, z17, z23, createObjectVariable7, solverVariable12, dimensionBehaviour, z11, constraintAnchor, constraintAnchor2, i21, i10, i22, i23, f2, z8, z24, z16, z10, z13, i11, i12, this.mMatchConstraintMinWidth, this.mMatchConstraintMaxWidth, this.mMatchConstraintPercentWidth, z12);
                                        if (z || (verticalWidgetRun = this.verticalRun) == null || !verticalWidgetRun.start.resolved || !this.verticalRun.end.resolved) {
                                            solverVariable7 = solverVariable5;
                                            solverVariable8 = solverVariable6;
                                            solverVariable9 = solverVariable4;
                                            i13 = 0;
                                            i14 = 8;
                                            r13 = 1;
                                            i15 = 1;
                                        } else {
                                            solverVariable7 = solverVariable5;
                                            linearSystem3.addEquality(solverVariable7, this.verticalRun.start.value);
                                            solverVariable8 = solverVariable6;
                                            linearSystem3.addEquality(solverVariable8, this.verticalRun.end.value);
                                            solverVariable9 = solverVariable4;
                                            linearSystem3.addEquality(solverVariable9, this.verticalRun.baseline.value);
                                            ConstraintWidget constraintWidget8 = this.mParent;
                                            if (constraintWidget8 == null || z10 || !z17) {
                                                i13 = 0;
                                                i14 = 8;
                                                z18 = true;
                                            } else {
                                                z18 = true;
                                                z18 = true;
                                                if (this.isTerminalWidget[1]) {
                                                    i13 = 0;
                                                    i14 = 8;
                                                    linearSystem3.addGreaterThan(linearSystem3.createObjectVariable(constraintWidget8.mBottom), solverVariable8, 0, 8);
                                                } else {
                                                    i13 = 0;
                                                    i14 = 8;
                                                }
                                            }
                                            i15 = i13;
                                            r13 = z18;
                                        }
                                        if ((this.mVerticalResolution == 2 ? i13 : i15) != 0 || this.resolvedVertical) {
                                            constraintWidget3 = this;
                                            solverVariable10 = solverVariable7;
                                            solverVariable11 = solverVariable8;
                                        } else {
                                            boolean z25 = (this.mListDimensionBehaviors[r13] == DimensionBehaviour.WRAP_CONTENT && (this instanceof ConstraintWidgetContainer)) ? r13 : i13;
                                            int i24 = z25 ? i13 : i7;
                                            ConstraintWidget constraintWidget9 = this.mParent;
                                            SolverVariable createObjectVariable8 = constraintWidget9 != null ? linearSystem3.createObjectVariable(constraintWidget9.mBottom) : null;
                                            ConstraintWidget constraintWidget10 = this.mParent;
                                            SolverVariable createObjectVariable9 = constraintWidget10 != null ? linearSystem3.createObjectVariable(constraintWidget10.mTop) : null;
                                            if (this.mBaselineDistance > 0 || this.mVisibility == i14) {
                                                if (this.mBaseline.mTarget != null) {
                                                    linearSystem3.addEquality(solverVariable9, solverVariable7, getBaselineDistance(), i14);
                                                    linearSystem3.addEquality(solverVariable9, linearSystem3.createObjectVariable(this.mBaseline.mTarget), this.mBaseline.getMargin(), i14);
                                                    if (z17) {
                                                        linearSystem3.addGreaterThan(createObjectVariable8, linearSystem3.createObjectVariable(this.mBottom), i13, 5);
                                                    }
                                                    z12 = i13;
                                                } else if (this.mVisibility == i14) {
                                                    linearSystem3.addEquality(solverVariable9, solverVariable7, this.mBaseline.getMargin(), i14);
                                                } else {
                                                    linearSystem3.addEquality(solverVariable9, solverVariable7, getBaselineDistance(), i14);
                                                }
                                            }
                                            boolean z26 = this.isTerminalWidget[r13];
                                            DimensionBehaviour[] dimensionBehaviourArr2 = this.mListDimensionBehaviors;
                                            int i25 = i13;
                                            boolean z27 = r13;
                                            solverVariable10 = solverVariable7;
                                            solverVariable11 = solverVariable8;
                                            applyConstraints(linearSystem, false, z17, z15, z26, createObjectVariable9, createObjectVariable8, dimensionBehaviourArr2[r13], z25, this.mTop, this.mBottom, this.mY, i24, this.mMinHeight, this.mMaxDimension[z27 ? 1 : 0], this.mVerticalBiasPercent, z21, dimensionBehaviourArr2[i25] == DimensionBehaviour.MATCH_CONSTRAINT ? z27 ? 1 : 0 : i25, z10, z16, z22, i12, i11, this.mMatchConstraintMinHeight, this.mMatchConstraintMaxHeight, this.mMatchConstraintPercentHeight, z12);
                                            constraintWidget3 = this;
                                        }
                                        if (z14) {
                                            if (constraintWidget3.mResolvedDimensionRatioSide == 1) {
                                                linearSystem.addRatio(solverVariable11, solverVariable10, solverVariable3, solverVariable2, constraintWidget3.mResolvedDimensionRatio, 8);
                                            } else {
                                                linearSystem.addRatio(solverVariable3, solverVariable2, solverVariable11, solverVariable10, constraintWidget3.mResolvedDimensionRatio, 8);
                                                linearSystem2 = linearSystem;
                                                if (constraintWidget3.mCenter.isConnected()) {
                                                    linearSystem2.addCenterPoint(constraintWidget3, constraintWidget3.mCenter.getTarget().getOwner(), (float) Math.toRadians(constraintWidget3.mCircleConstraintAngle + 90.0f), constraintWidget3.mCenter.getMargin());
                                                }
                                                constraintWidget3.resolvedHorizontal = false;
                                                constraintWidget3.resolvedVertical = false;
                                            }
                                        }
                                        linearSystem2 = linearSystem;
                                        if (constraintWidget3.mCenter.isConnected()) {
                                        }
                                        constraintWidget3.resolvedHorizontal = false;
                                        constraintWidget3.resolvedVertical = false;
                                    }
                                    if (z) {
                                        linearSystem3.addEquality(createObjectVariable, this.horizontalRun.start.value);
                                        linearSystem3.addEquality(createObjectVariable2, this.horizontalRun.end.value);
                                        if (this.mParent != null && z2 && this.isTerminalWidget[0] && !isInHorizontalChain()) {
                                            linearSystem3.addGreaterThan(linearSystem3.createObjectVariable(this.mParent.mRight), createObjectVariable2, 0, 8);
                                        }
                                    }
                                }
                                solverVariable2 = createObjectVariable;
                                solverVariable3 = createObjectVariable2;
                                z14 = z7;
                                solverVariable4 = createObjectVariable5;
                                z15 = z2;
                                z16 = z9;
                                i11 = i19;
                                solverVariable5 = createObjectVariable3;
                                solverVariable6 = solverVariable;
                                i12 = i8;
                                z17 = z3;
                                if (z) {
                                }
                                solverVariable7 = solverVariable5;
                                solverVariable8 = solverVariable6;
                                solverVariable9 = solverVariable4;
                                i13 = 0;
                                i14 = 8;
                                r13 = 1;
                                i15 = 1;
                                if ((this.mVerticalResolution == 2 ? i13 : i15) != 0) {
                                }
                                constraintWidget3 = this;
                                solverVariable10 = solverVariable7;
                                solverVariable11 = solverVariable8;
                                if (z14) {
                                }
                                linearSystem2 = linearSystem;
                                if (constraintWidget3.mCenter.isConnected()) {
                                }
                                constraintWidget3.resolvedHorizontal = false;
                                constraintWidget3.resolvedVertical = false;
                            }
                        }
                        z8 = false;
                        if (z7) {
                        }
                        if (this.mListDimensionBehaviors[0] == DimensionBehaviour.WRAP_CONTENT) {
                        }
                        z9 = z6;
                        z10 = z5;
                        z11 = false;
                        if (z11) {
                        }
                        z12 = !this.mCenter.isConnected();
                        boolean[] zArr22 = this.mIsInBarrier;
                        z13 = zArr22[0];
                        boolean z222 = zArr22[1];
                        if (this.mHorizontalResolution != 2) {
                            if (z) {
                            }
                            ConstraintWidget constraintWidget62 = this.mParent;
                            if (constraintWidget62 == null) {
                            }
                            ConstraintWidget constraintWidget72 = this.mParent;
                            if (constraintWidget72 == null) {
                            }
                            boolean z232 = this.isTerminalWidget[0];
                            DimensionBehaviour[] dimensionBehaviourArr3 = this.mListDimensionBehaviors;
                            z15 = z2;
                            SolverVariable solverVariable122 = createObjectVariable6;
                            DimensionBehaviour dimensionBehaviour2 = dimensionBehaviourArr3[0];
                            ConstraintAnchor constraintAnchor3 = this.mLeft;
                            z16 = z9;
                            ConstraintAnchor constraintAnchor22 = this.mRight;
                            i12 = i8;
                            z17 = z3;
                            int i212 = this.mX;
                            i11 = i19;
                            int i222 = this.mMinWidth;
                            int i232 = this.mMaxDimension[0];
                            float f22 = this.mHorizontalBiasPercent;
                            solverVariable2 = createObjectVariable;
                            if (dimensionBehaviourArr3[1] != DimensionBehaviour.MATCH_CONSTRAINT) {
                            }
                            z14 = z7;
                            solverVariable4 = createObjectVariable5;
                            solverVariable3 = createObjectVariable2;
                            solverVariable6 = solverVariable;
                            solverVariable5 = createObjectVariable3;
                            linearSystem3 = linearSystem;
                            applyConstraints(linearSystem3, true, z15, z17, z232, createObjectVariable7, solverVariable122, dimensionBehaviour2, z11, constraintAnchor3, constraintAnchor22, i212, i10, i222, i232, f22, z8, z24, z16, z10, z13, i11, i12, this.mMatchConstraintMinWidth, this.mMatchConstraintMaxWidth, this.mMatchConstraintPercentWidth, z12);
                            if (z) {
                            }
                            solverVariable7 = solverVariable5;
                            solverVariable8 = solverVariable6;
                            solverVariable9 = solverVariable4;
                            i13 = 0;
                            i14 = 8;
                            r13 = 1;
                            i15 = 1;
                            if ((this.mVerticalResolution == 2 ? i13 : i15) != 0) {
                            }
                            constraintWidget3 = this;
                            solverVariable10 = solverVariable7;
                            solverVariable11 = solverVariable8;
                            if (z14) {
                            }
                            linearSystem2 = linearSystem;
                            if (constraintWidget3.mCenter.isConnected()) {
                            }
                            constraintWidget3.resolvedHorizontal = false;
                            constraintWidget3.resolvedVertical = false;
                        }
                        solverVariable2 = createObjectVariable;
                        solverVariable3 = createObjectVariable2;
                        z14 = z7;
                        solverVariable4 = createObjectVariable5;
                        z15 = z2;
                        z16 = z9;
                        i11 = i19;
                        solverVariable5 = createObjectVariable3;
                        solverVariable6 = solverVariable;
                        i12 = i8;
                        z17 = z3;
                        if (z) {
                        }
                        solverVariable7 = solverVariable5;
                        solverVariable8 = solverVariable6;
                        solverVariable9 = solverVariable4;
                        i13 = 0;
                        i14 = 8;
                        r13 = 1;
                        i15 = 1;
                        if ((this.mVerticalResolution == 2 ? i13 : i15) != 0) {
                        }
                        constraintWidget3 = this;
                        solverVariable10 = solverVariable7;
                        solverVariable11 = solverVariable8;
                        if (z14) {
                        }
                        linearSystem2 = linearSystem;
                        if (constraintWidget3.mCenter.isConnected()) {
                        }
                        constraintWidget3.resolvedHorizontal = false;
                        constraintWidget3.resolvedVertical = false;
                    }
                    i19 = 4;
                } else if (this.mListDimensionBehaviors[1] == DimensionBehaviour.MATCH_CONSTRAINT && i8 == 3) {
                    this.mResolvedDimensionRatioSide = 1;
                    if (this.mDimensionRatioSide == -1) {
                        this.mResolvedDimensionRatio = 1.0f / this.mResolvedDimensionRatio;
                    }
                    i4 = (int) (this.mResolvedDimensionRatio * this.mWidth);
                    if (this.mListDimensionBehaviors[0] != DimensionBehaviour.MATCH_CONSTRAINT) {
                        i7 = i4;
                        i8 = 4;
                    }
                }
                i7 = i4;
                z7 = true;
                int[] iArr2 = this.mResolvedMatchConstraintDefault;
                iArr2[0] = i19;
                iArr2[1] = i8;
                this.mResolvedHasRatio = z7;
                if (z7) {
                }
                z8 = false;
                if (z7) {
                }
                if (this.mListDimensionBehaviors[0] == DimensionBehaviour.WRAP_CONTENT) {
                }
                z9 = z6;
                z10 = z5;
                z11 = false;
                if (z11) {
                }
                z12 = !this.mCenter.isConnected();
                boolean[] zArr222 = this.mIsInBarrier;
                z13 = zArr222[0];
                boolean z2222 = zArr222[1];
                if (this.mHorizontalResolution != 2) {
                }
                solverVariable2 = createObjectVariable;
                solverVariable3 = createObjectVariable2;
                z14 = z7;
                solverVariable4 = createObjectVariable5;
                z15 = z2;
                z16 = z9;
                i11 = i19;
                solverVariable5 = createObjectVariable3;
                solverVariable6 = solverVariable;
                i12 = i8;
                z17 = z3;
                if (z) {
                }
                solverVariable7 = solverVariable5;
                solverVariable8 = solverVariable6;
                solverVariable9 = solverVariable4;
                i13 = 0;
                i14 = 8;
                r13 = 1;
                i15 = 1;
                if ((this.mVerticalResolution == 2 ? i13 : i15) != 0) {
                }
                constraintWidget3 = this;
                solverVariable10 = solverVariable7;
                solverVariable11 = solverVariable8;
                if (z14) {
                }
                linearSystem2 = linearSystem;
                if (constraintWidget3.mCenter.isConnected()) {
                }
                constraintWidget3.resolvedHorizontal = false;
                constraintWidget3.resolvedVertical = false;
            }
            solverVariable = createObjectVariable4;
            i7 = i4;
            i8 = i6;
            z7 = false;
            int[] iArr22 = this.mResolvedMatchConstraintDefault;
            iArr22[0] = i19;
            iArr22[1] = i8;
            this.mResolvedHasRatio = z7;
            if (z7) {
            }
            z8 = false;
            if (z7) {
            }
            if (this.mListDimensionBehaviors[0] == DimensionBehaviour.WRAP_CONTENT) {
            }
            z9 = z6;
            z10 = z5;
            z11 = false;
            if (z11) {
            }
            z12 = !this.mCenter.isConnected();
            boolean[] zArr2222 = this.mIsInBarrier;
            z13 = zArr2222[0];
            boolean z22222 = zArr2222[1];
            if (this.mHorizontalResolution != 2) {
            }
            solverVariable2 = createObjectVariable;
            solverVariable3 = createObjectVariable2;
            z14 = z7;
            solverVariable4 = createObjectVariable5;
            z15 = z2;
            z16 = z9;
            i11 = i19;
            solverVariable5 = createObjectVariable3;
            solverVariable6 = solverVariable;
            i12 = i8;
            z17 = z3;
            if (z) {
            }
            solverVariable7 = solverVariable5;
            solverVariable8 = solverVariable6;
            solverVariable9 = solverVariable4;
            i13 = 0;
            i14 = 8;
            r13 = 1;
            i15 = 1;
            if ((this.mVerticalResolution == 2 ? i13 : i15) != 0) {
            }
            constraintWidget3 = this;
            solverVariable10 = solverVariable7;
            solverVariable11 = solverVariable8;
            if (z14) {
            }
            linearSystem2 = linearSystem;
            if (constraintWidget3.mCenter.isConnected()) {
            }
            constraintWidget3.resolvedHorizontal = false;
            constraintWidget3.resolvedVertical = false;
        }
        z2 = false;
        z3 = false;
        if (this.mVisibility == 8) {
            zArr = this.mIsInBarrier;
            if (!zArr[0]) {
                return;
            }
        }
        z4 = this.resolvedHorizontal;
        if (!z4) {
        }
        if (z4) {
        }
        if (this.resolvedVertical) {
        }
        if (this.resolvedHorizontal) {
            this.resolvedHorizontal = false;
            this.resolvedVertical = false;
            return;
        }
        if (LinearSystem.sMetrics == null) {
        }
        if (!z) {
        }
        if (LinearSystem.sMetrics != null) {
        }
        if (this.mParent == null) {
        }
        i2 = this.mWidth;
        i3 = this.mMinWidth;
        if (i2 < i3) {
        }
        i4 = this.mHeight;
        i5 = this.mMinHeight;
        if (i4 < i5) {
        }
        if (this.mListDimensionBehaviors[0] == DimensionBehaviour.MATCH_CONSTRAINT) {
        }
        if (this.mListDimensionBehaviors[1] == DimensionBehaviour.MATCH_CONSTRAINT) {
        }
        this.mResolvedDimensionRatioSide = this.mDimensionRatioSide;
        f = this.mDimensionRatio;
        this.mResolvedDimensionRatio = f;
        int i192 = this.mMatchConstraintDefaultWidth;
        i6 = this.mMatchConstraintDefaultHeight;
        if (f <= 0.0f) {
        }
        solverVariable = createObjectVariable4;
        i7 = i4;
        i8 = i6;
        z7 = false;
        int[] iArr222 = this.mResolvedMatchConstraintDefault;
        iArr222[0] = i192;
        iArr222[1] = i8;
        this.mResolvedHasRatio = z7;
        if (z7) {
        }
        z8 = false;
        if (z7) {
        }
        if (this.mListDimensionBehaviors[0] == DimensionBehaviour.WRAP_CONTENT) {
        }
        z9 = z6;
        z10 = z5;
        z11 = false;
        if (z11) {
        }
        z12 = !this.mCenter.isConnected();
        boolean[] zArr22222 = this.mIsInBarrier;
        z13 = zArr22222[0];
        boolean z222222 = zArr22222[1];
        if (this.mHorizontalResolution != 2) {
        }
        solverVariable2 = createObjectVariable;
        solverVariable3 = createObjectVariable2;
        z14 = z7;
        solverVariable4 = createObjectVariable5;
        z15 = z2;
        z16 = z9;
        i11 = i192;
        solverVariable5 = createObjectVariable3;
        solverVariable6 = solverVariable;
        i12 = i8;
        z17 = z3;
        if (z) {
        }
        solverVariable7 = solverVariable5;
        solverVariable8 = solverVariable6;
        solverVariable9 = solverVariable4;
        i13 = 0;
        i14 = 8;
        r13 = 1;
        i15 = 1;
        if ((this.mVerticalResolution == 2 ? i13 : i15) != 0) {
        }
        constraintWidget3 = this;
        solverVariable10 = solverVariable7;
        solverVariable11 = solverVariable8;
        if (z14) {
        }
        linearSystem2 = linearSystem;
        if (constraintWidget3.mCenter.isConnected()) {
        }
        constraintWidget3.resolvedHorizontal = false;
        constraintWidget3.resolvedVertical = false;
    }

    boolean addFirst() {
        return (this instanceof VirtualLayout) || (this instanceof Guideline);
    }

    public void setupDimensionRatio(boolean z, boolean z2, boolean z3, boolean z4) {
        if (this.mResolvedDimensionRatioSide == -1) {
            if (z3 && !z4) {
                this.mResolvedDimensionRatioSide = 0;
            } else if (!z3 && z4) {
                this.mResolvedDimensionRatioSide = 1;
                if (this.mDimensionRatioSide == -1) {
                    this.mResolvedDimensionRatio = 1.0f / this.mResolvedDimensionRatio;
                }
            }
        }
        if (this.mResolvedDimensionRatioSide == 0 && (!this.mTop.isConnected() || !this.mBottom.isConnected())) {
            this.mResolvedDimensionRatioSide = 1;
        } else if (this.mResolvedDimensionRatioSide == 1 && (!this.mLeft.isConnected() || !this.mRight.isConnected())) {
            this.mResolvedDimensionRatioSide = 0;
        }
        if (this.mResolvedDimensionRatioSide == -1 && (!this.mTop.isConnected() || !this.mBottom.isConnected() || !this.mLeft.isConnected() || !this.mRight.isConnected())) {
            if (this.mTop.isConnected() && this.mBottom.isConnected()) {
                this.mResolvedDimensionRatioSide = 0;
            } else if (this.mLeft.isConnected() && this.mRight.isConnected()) {
                this.mResolvedDimensionRatio = 1.0f / this.mResolvedDimensionRatio;
                this.mResolvedDimensionRatioSide = 1;
            }
        }
        if (this.mResolvedDimensionRatioSide == -1) {
            int i = this.mMatchConstraintMinWidth;
            if (i > 0 && this.mMatchConstraintMinHeight == 0) {
                this.mResolvedDimensionRatioSide = 0;
            } else {
                if (i != 0 || this.mMatchConstraintMinHeight <= 0) {
                    return;
                }
                this.mResolvedDimensionRatio = 1.0f / this.mResolvedDimensionRatio;
                this.mResolvedDimensionRatioSide = 1;
            }
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:114:? A[ADDED_TO_REGION, RETURN, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:320:0x0514  */
    /* JADX WARN: Removed duplicated region for block: B:322:0x00e3  */
    /* JADX WARN: Removed duplicated region for block: B:33:0x009e  */
    /* JADX WARN: Removed duplicated region for block: B:35:0x00a3  */
    /* JADX WARN: Removed duplicated region for block: B:43:0x00bc  */
    /* JADX WARN: Removed duplicated region for block: B:53:0x01cf  */
    /* JADX WARN: Removed duplicated region for block: B:56:0x051d A[ADDED_TO_REGION] */
    /* JADX WARN: Removed duplicated region for block: B:83:? A[ADDED_TO_REGION, RETURN, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:91:0x04e6 A[ADDED_TO_REGION] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private void applyConstraints(LinearSystem linearSystem, boolean z, boolean z2, boolean z3, boolean z4, SolverVariable solverVariable, SolverVariable solverVariable2, DimensionBehaviour dimensionBehaviour, boolean z5, ConstraintAnchor constraintAnchor, ConstraintAnchor constraintAnchor2, int i, int i2, int i3, int i4, float f, boolean z6, boolean z7, boolean z8, boolean z9, boolean z10, int i5, int i6, int i7, int i8, float f2, boolean z11) {
        int i9;
        boolean z12;
        int i10;
        int i11;
        int i12;
        SolverVariable solverVariable3;
        int i13;
        SolverVariable solverVariable4;
        int i14;
        boolean z13;
        boolean z14;
        int i15;
        SolverVariable createObjectVariable;
        SolverVariable createObjectVariable2;
        SolverVariable solverVariable5;
        SolverVariable solverVariable6;
        int i16;
        int i17;
        boolean z15;
        int i18;
        SolverVariable solverVariable7;
        SolverVariable solverVariable8;
        SolverVariable solverVariable9;
        SolverVariable solverVariable10;
        int i19;
        int i20;
        int i21;
        boolean z16;
        boolean z17;
        boolean z18;
        boolean z19;
        SolverVariable solverVariable11;
        SolverVariable solverVariable12;
        SolverVariable solverVariable13;
        ConstraintWidget constraintWidget;
        ConstraintWidget constraintWidget2;
        ConstraintWidget constraintWidget3;
        int i22;
        boolean z20;
        int i23;
        int i24;
        boolean z21;
        int i25;
        int i26;
        ConstraintWidget constraintWidget4;
        int i27;
        boolean z22;
        int i28;
        int i29;
        int i30;
        boolean z23;
        int i31;
        boolean z24;
        ConstraintWidget constraintWidget5;
        int i32;
        ConstraintWidget constraintWidget6;
        LinearSystem linearSystem2 = linearSystem;
        SolverVariable createObjectVariable3 = linearSystem2.createObjectVariable(constraintAnchor);
        SolverVariable createObjectVariable4 = linearSystem2.createObjectVariable(constraintAnchor2);
        SolverVariable createObjectVariable5 = linearSystem2.createObjectVariable(constraintAnchor.getTarget());
        SolverVariable createObjectVariable6 = linearSystem2.createObjectVariable(constraintAnchor2.getTarget());
        if (LinearSystem.getMetrics() != null) {
            LinearSystem.getMetrics().nonresolvedWidgets++;
        }
        boolean isConnected = constraintAnchor.isConnected();
        boolean isConnected2 = constraintAnchor2.isConnected();
        boolean isConnected3 = this.mCenter.isConnected();
        int i33 = isConnected2 ? (isConnected ? 1 : 0) + 1 : isConnected ? 1 : 0;
        if (isConnected3) {
            i33++;
        }
        int i34 = z6 ? 3 : i5;
        int i35 = AnonymousClass1.$SwitchMap$androidx$constraintlayout$core$widgets$ConstraintWidget$DimensionBehaviour[dimensionBehaviour.ordinal()];
        if (i35 == 1 || i35 == 2 || i35 == 3 || i35 != 4) {
            i9 = i34;
        } else {
            i9 = i34;
            if (i9 != 4) {
                z12 = true;
                i10 = this.mWidthOverride;
                if (i10 == -1 && z) {
                    this.mWidthOverride = -1;
                    z12 = false;
                } else {
                    i10 = i2;
                }
                i11 = this.mHeightOverride;
                if (i11 != -1 || z) {
                    i11 = i10;
                } else {
                    this.mHeightOverride = -1;
                    z12 = false;
                }
                if (this.mVisibility == 8) {
                    i11 = 0;
                    z12 = false;
                }
                if (z11) {
                    if (!isConnected && !isConnected2 && !isConnected3) {
                        linearSystem2.addEquality(createObjectVariable3, i);
                    } else if (isConnected && !isConnected2) {
                        linearSystem2.addEquality(createObjectVariable3, createObjectVariable5, constraintAnchor.getMargin(), 8);
                    }
                }
                if (z12) {
                    if (z5) {
                        linearSystem2.addEquality(createObjectVariable4, createObjectVariable3, 0, 3);
                        if (i3 > 0) {
                            linearSystem2.addGreaterThan(createObjectVariable4, createObjectVariable3, i3, 8);
                        }
                        if (i4 < Integer.MAX_VALUE) {
                            linearSystem2.addLowerThan(createObjectVariable4, createObjectVariable3, i4, 8);
                        }
                    } else {
                        linearSystem2.addEquality(createObjectVariable4, createObjectVariable3, i11, 8);
                    }
                    i15 = i8;
                    solverVariable3 = createObjectVariable4;
                    i13 = i33;
                    z13 = z12;
                    solverVariable4 = createObjectVariable6;
                    z14 = z4;
                } else {
                    if (i33 == 2 || z6 || !(i9 == 1 || i9 == 0)) {
                        int i36 = i7 == -2 ? i11 : i7;
                        int i37 = i8 == -2 ? i11 : i8;
                        if (i11 > 0 && i9 != 1) {
                            i11 = 0;
                        }
                        if (i36 > 0) {
                            linearSystem2.addGreaterThan(createObjectVariable4, createObjectVariable3, i36, 8);
                            i11 = Math.max(i11, i36);
                        }
                        if (i37 > 0) {
                            if (!z2 || i9 != 1) {
                                linearSystem2.addLowerThan(createObjectVariable4, createObjectVariable3, i37, 8);
                            }
                            i11 = Math.min(i11, i37);
                        }
                        if (i9 == 1) {
                            if (z2) {
                                linearSystem2.addEquality(createObjectVariable4, createObjectVariable3, i11, 8);
                            } else if (z8) {
                                linearSystem2.addEquality(createObjectVariable4, createObjectVariable3, i11, 5);
                                linearSystem2.addLowerThan(createObjectVariable4, createObjectVariable3, i11, 8);
                            } else {
                                linearSystem2.addEquality(createObjectVariable4, createObjectVariable3, i11, 5);
                                linearSystem2.addLowerThan(createObjectVariable4, createObjectVariable3, i11, 8);
                            }
                            solverVariable3 = createObjectVariable4;
                            z13 = z12;
                            solverVariable4 = createObjectVariable6;
                            z14 = z4;
                            i14 = i36;
                            i15 = i37;
                            i13 = i33;
                        } else {
                            if (i9 == 2) {
                                if (constraintAnchor.getType() == ConstraintAnchor.Type.TOP || constraintAnchor.getType() == ConstraintAnchor.Type.BOTTOM) {
                                    createObjectVariable = linearSystem2.createObjectVariable(this.mParent.getAnchor(ConstraintAnchor.Type.TOP));
                                    createObjectVariable2 = linearSystem2.createObjectVariable(this.mParent.getAnchor(ConstraintAnchor.Type.BOTTOM));
                                } else {
                                    createObjectVariable = linearSystem2.createObjectVariable(this.mParent.getAnchor(ConstraintAnchor.Type.LEFT));
                                    createObjectVariable2 = linearSystem2.createObjectVariable(this.mParent.getAnchor(ConstraintAnchor.Type.RIGHT));
                                }
                                SolverVariable solverVariable14 = createObjectVariable2;
                                solverVariable3 = createObjectVariable4;
                                i12 = i37;
                                i13 = i33;
                                solverVariable4 = createObjectVariable6;
                                linearSystem2.addConstraint(linearSystem2.createRow().createRowDimensionRatio(solverVariable3, createObjectVariable3, solverVariable14, createObjectVariable, f2));
                                if (z2) {
                                    z12 = false;
                                }
                                i14 = i36;
                                z13 = z12;
                                z14 = z4;
                            } else {
                                i12 = i37;
                                solverVariable3 = createObjectVariable4;
                                i13 = i33;
                                solverVariable4 = createObjectVariable6;
                                i14 = i36;
                                z13 = z12;
                                z14 = true;
                            }
                            i15 = i12;
                        }
                        if (!z11) {
                            solverVariable5 = solverVariable;
                            solverVariable6 = solverVariable2;
                            i16 = 8;
                            i17 = 2;
                        } else if (z8) {
                            solverVariable5 = solverVariable;
                            solverVariable6 = solverVariable2;
                            i17 = 2;
                            i16 = 8;
                        } else {
                            if (isConnected || isConnected2 || isConnected3) {
                                if (isConnected && !isConnected2) {
                                    z20 = z2;
                                    solverVariable12 = solverVariable4;
                                    i32 = (z2 && (constraintAnchor.mTarget.mOwner instanceof Barrier)) ? 8 : 5;
                                    if (z20) {
                                    }
                                } else if (!isConnected && isConnected2) {
                                    linearSystem2.addEquality(solverVariable3, solverVariable4, -constraintAnchor2.getMargin(), 8);
                                    if (z2) {
                                        if (this.OPTIMIZE_WRAP && createObjectVariable3.isFinalValue && (constraintWidget5 = this.mParent) != null) {
                                            ConstraintWidgetContainer constraintWidgetContainer = (ConstraintWidgetContainer) constraintWidget5;
                                            if (z) {
                                                constraintWidgetContainer.addHorizontalWrapMinVariable(constraintAnchor);
                                            } else {
                                                constraintWidgetContainer.addVerticalWrapMinVariable(constraintAnchor);
                                            }
                                        } else {
                                            linearSystem2.addGreaterThan(createObjectVariable3, solverVariable, 0, 5);
                                            i25 = 5;
                                            solverVariable12 = solverVariable4;
                                            z20 = z2;
                                            i32 = i25;
                                            if (z20) {
                                            }
                                        }
                                    }
                                } else if (isConnected && isConnected2) {
                                    ConstraintWidget constraintWidget7 = constraintAnchor.mTarget.mOwner;
                                    ConstraintWidget constraintWidget8 = constraintAnchor2.mTarget.mOwner;
                                    ConstraintWidget parent = getParent();
                                    int i38 = 6;
                                    if (!z13) {
                                        i18 = 8;
                                        if (createObjectVariable5.isFinalValue && solverVariable4.isFinalValue) {
                                            SolverVariable solverVariable15 = solverVariable4;
                                            linearSystem.addCentering(createObjectVariable3, createObjectVariable5, constraintAnchor.getMargin(), f, solverVariable15, solverVariable3, constraintAnchor2.getMargin(), 8);
                                            if (z2 && z14) {
                                                int margin = constraintAnchor2.mTarget != null ? constraintAnchor2.getMargin() : 0;
                                                if (solverVariable15 != solverVariable2) {
                                                    linearSystem.addGreaterThan(solverVariable2, solverVariable3, margin, 5);
                                                    return;
                                                }
                                                return;
                                            }
                                            return;
                                        }
                                        linearSystem2 = linearSystem;
                                        solverVariable7 = createObjectVariable3;
                                        solverVariable8 = solverVariable4;
                                        solverVariable9 = createObjectVariable5;
                                        solverVariable10 = solverVariable2;
                                        i19 = 6;
                                        i20 = 4;
                                        i21 = 5;
                                        z16 = true;
                                        z17 = true;
                                        z18 = false;
                                    } else if (i9 == 0) {
                                        if (i15 != 0 || i14 != 0) {
                                            i30 = 8;
                                            z23 = false;
                                            i31 = 5;
                                            i20 = 5;
                                            z24 = true;
                                            z17 = true;
                                        } else {
                                            if (createObjectVariable5.isFinalValue && solverVariable4.isFinalValue) {
                                                linearSystem2.addEquality(createObjectVariable3, createObjectVariable5, constraintAnchor.getMargin(), 8);
                                                linearSystem2.addEquality(solverVariable3, solverVariable4, -constraintAnchor2.getMargin(), 8);
                                                return;
                                            }
                                            i30 = 8;
                                            z24 = false;
                                            z17 = false;
                                            i31 = 8;
                                            i20 = 8;
                                            z23 = true;
                                        }
                                        if ((constraintWidget7 instanceof Barrier) || (constraintWidget8 instanceof Barrier)) {
                                            solverVariable7 = createObjectVariable3;
                                            solverVariable8 = solverVariable4;
                                            z18 = z23;
                                            i20 = 4;
                                        } else {
                                            solverVariable7 = createObjectVariable3;
                                            solverVariable8 = solverVariable4;
                                            z18 = z23;
                                        }
                                        solverVariable10 = solverVariable2;
                                        z16 = z24;
                                        i21 = i31;
                                        i18 = i30;
                                        solverVariable9 = createObjectVariable5;
                                        i19 = 6;
                                    } else {
                                        i18 = 8;
                                        if (i9 == 2) {
                                            if ((constraintWidget7 instanceof Barrier) || (constraintWidget8 instanceof Barrier)) {
                                                solverVariable7 = createObjectVariable3;
                                                solverVariable8 = solverVariable4;
                                                solverVariable9 = createObjectVariable5;
                                                i19 = 6;
                                                i20 = 4;
                                            } else {
                                                solverVariable7 = createObjectVariable3;
                                                solverVariable8 = solverVariable4;
                                                solverVariable9 = createObjectVariable5;
                                                i19 = 6;
                                                i20 = 5;
                                            }
                                            i21 = 5;
                                        } else if (i9 == 1) {
                                            solverVariable7 = createObjectVariable3;
                                            solverVariable8 = solverVariable4;
                                            solverVariable9 = createObjectVariable5;
                                            i19 = 6;
                                            i21 = 8;
                                            i20 = 4;
                                        } else if (i9 == 3) {
                                            if (this.mResolvedDimensionRatioSide == -1) {
                                                if (z9) {
                                                    linearSystem2 = linearSystem;
                                                    solverVariable7 = createObjectVariable3;
                                                    solverVariable8 = solverVariable4;
                                                    solverVariable9 = createObjectVariable5;
                                                    i21 = 8;
                                                    i19 = z2 ? 5 : 4;
                                                } else {
                                                    linearSystem2 = linearSystem;
                                                    solverVariable7 = createObjectVariable3;
                                                    solverVariable8 = solverVariable4;
                                                    solverVariable9 = createObjectVariable5;
                                                    i19 = 8;
                                                    i21 = 8;
                                                }
                                                i20 = 5;
                                            } else {
                                                if (z6) {
                                                    if (i6 == 2 || i6 == 1) {
                                                        i28 = 5;
                                                        i29 = 4;
                                                    } else {
                                                        i28 = 8;
                                                        i29 = 5;
                                                    }
                                                    i21 = i28;
                                                    i20 = i29;
                                                    solverVariable7 = createObjectVariable3;
                                                    solverVariable8 = solverVariable4;
                                                    solverVariable9 = createObjectVariable5;
                                                    i19 = 6;
                                                } else {
                                                    if (i15 > 0) {
                                                        linearSystem2 = linearSystem;
                                                        solverVariable7 = createObjectVariable3;
                                                        solverVariable8 = solverVariable4;
                                                        solverVariable9 = createObjectVariable5;
                                                        i19 = 6;
                                                        i20 = 5;
                                                    } else if (i15 != 0 || i14 != 0) {
                                                        linearSystem2 = linearSystem;
                                                        solverVariable7 = createObjectVariable3;
                                                        solverVariable8 = solverVariable4;
                                                        solverVariable9 = createObjectVariable5;
                                                        i19 = 6;
                                                        i20 = 4;
                                                    } else if (z9) {
                                                        i21 = (constraintWidget7 == parent || constraintWidget8 == parent) ? 5 : 4;
                                                        solverVariable7 = createObjectVariable3;
                                                        solverVariable8 = solverVariable4;
                                                        solverVariable9 = createObjectVariable5;
                                                        i19 = 6;
                                                        i20 = 4;
                                                    } else {
                                                        linearSystem2 = linearSystem;
                                                        solverVariable7 = createObjectVariable3;
                                                        solverVariable8 = solverVariable4;
                                                        solverVariable9 = createObjectVariable5;
                                                        i19 = 6;
                                                        i20 = 8;
                                                    }
                                                    i21 = 5;
                                                }
                                                z16 = true;
                                                z17 = true;
                                                z18 = true;
                                                linearSystem2 = linearSystem;
                                                solverVariable10 = solverVariable2;
                                            }
                                            z16 = true;
                                            z17 = true;
                                            z18 = true;
                                            solverVariable10 = solverVariable2;
                                        } else {
                                            linearSystem2 = linearSystem;
                                            solverVariable7 = createObjectVariable3;
                                            solverVariable8 = solverVariable4;
                                            solverVariable9 = createObjectVariable5;
                                            i19 = 6;
                                            i20 = 4;
                                            i21 = 5;
                                            z16 = false;
                                            z17 = false;
                                            z18 = false;
                                            solverVariable10 = solverVariable2;
                                        }
                                        z16 = true;
                                        z17 = true;
                                        z18 = false;
                                        solverVariable10 = solverVariable2;
                                    }
                                    if (z17 && solverVariable9 == solverVariable8 && constraintWidget7 != parent) {
                                        z17 = false;
                                        z19 = false;
                                    } else {
                                        z19 = true;
                                    }
                                    if (z16) {
                                        if (z13 || z7 || z9 || solverVariable9 != solverVariable || solverVariable8 != solverVariable10) {
                                            constraintWidget4 = parent;
                                            i27 = i19;
                                            z22 = z2;
                                        } else {
                                            constraintWidget4 = parent;
                                            i27 = i18;
                                            i21 = i27;
                                            z22 = false;
                                            z19 = false;
                                        }
                                        solverVariable13 = solverVariable;
                                        constraintWidget = constraintWidget7;
                                        z2 = z22;
                                        i22 = i18;
                                        constraintWidget3 = constraintWidget4;
                                        constraintWidget2 = constraintWidget8;
                                        SolverVariable solverVariable16 = solverVariable3;
                                        linearSystem2.addCentering(solverVariable7, solverVariable9, constraintAnchor.getMargin(), f, solverVariable8, solverVariable16, constraintAnchor2.getMargin(), i27);
                                        SolverVariable solverVariable17 = solverVariable8;
                                        solverVariable11 = solverVariable7;
                                        solverVariable12 = solverVariable17;
                                        solverVariable3 = solverVariable16;
                                    } else {
                                        SolverVariable solverVariable18 = solverVariable8;
                                        solverVariable11 = solverVariable7;
                                        solverVariable12 = solverVariable18;
                                        solverVariable13 = solverVariable;
                                        constraintWidget = constraintWidget7;
                                        constraintWidget2 = constraintWidget8;
                                        constraintWidget3 = parent;
                                        i22 = i18;
                                    }
                                    z20 = z2;
                                    if (this.mVisibility != i22 || constraintAnchor2.hasDependents()) {
                                        if (z17) {
                                            int i39 = (!z20 || solverVariable9 == solverVariable12 || z13 || !((constraintWidget instanceof Barrier) || (constraintWidget2 instanceof Barrier))) ? i21 : 6;
                                            linearSystem2.addGreaterThan(solverVariable11, solverVariable9, constraintAnchor.getMargin(), i39);
                                            linearSystem2.addLowerThan(solverVariable3, solverVariable12, -constraintAnchor2.getMargin(), i39);
                                            i21 = i39;
                                        }
                                        if (!z20 || !z10 || (constraintWidget instanceof Barrier) || (constraintWidget2 instanceof Barrier) || constraintWidget2 == constraintWidget3) {
                                            i23 = i20;
                                            i24 = i21;
                                            z21 = z19;
                                        } else {
                                            i23 = 6;
                                            i24 = 6;
                                            z21 = true;
                                        }
                                        if (z21) {
                                            if (z18 && (!z9 || z3)) {
                                                if (constraintWidget != constraintWidget3 && constraintWidget2 != constraintWidget3) {
                                                    i38 = i23;
                                                }
                                                if ((constraintWidget instanceof Guideline) || (constraintWidget2 instanceof Guideline)) {
                                                    i38 = 5;
                                                }
                                                if ((constraintWidget instanceof Barrier) || (constraintWidget2 instanceof Barrier)) {
                                                    i38 = 5;
                                                }
                                                i23 = Math.max(z9 ? 5 : i38, i23);
                                            }
                                            if (z20) {
                                                i23 = Math.min(i24, i23);
                                                if (z6 && !z9 && (constraintWidget == constraintWidget3 || constraintWidget2 == constraintWidget3)) {
                                                    i26 = 4;
                                                    linearSystem2.addEquality(solverVariable11, solverVariable9, constraintAnchor.getMargin(), i26);
                                                    linearSystem2.addEquality(solverVariable3, solverVariable12, -constraintAnchor2.getMargin(), i26);
                                                }
                                            }
                                            i26 = i23;
                                            linearSystem2.addEquality(solverVariable11, solverVariable9, constraintAnchor.getMargin(), i26);
                                            linearSystem2.addEquality(solverVariable3, solverVariable12, -constraintAnchor2.getMargin(), i26);
                                        }
                                        if (z20) {
                                            int margin2 = solverVariable13 == solverVariable9 ? constraintAnchor.getMargin() : 0;
                                            if (solverVariable9 != solverVariable13) {
                                                linearSystem2.addGreaterThan(solverVariable11, solverVariable13, margin2, 5);
                                            }
                                        }
                                        if (z20 && z13 && i3 == 0 && i14 == 0) {
                                            if (z13 && i9 == 3) {
                                                linearSystem2.addGreaterThan(solverVariable3, solverVariable11, 0, i22);
                                            } else {
                                                i25 = 5;
                                                linearSystem2.addGreaterThan(solverVariable3, solverVariable11, 0, 5);
                                                i32 = i25;
                                                if (z20 && z14) {
                                                    int margin3 = constraintAnchor2.mTarget != null ? constraintAnchor2.getMargin() : 0;
                                                    if (solverVariable12 != solverVariable2) {
                                                        if (this.OPTIMIZE_WRAP && solverVariable3.isFinalValue && (constraintWidget6 = this.mParent) != null) {
                                                            ConstraintWidgetContainer constraintWidgetContainer2 = (ConstraintWidgetContainer) constraintWidget6;
                                                            if (z) {
                                                                constraintWidgetContainer2.addHorizontalWrapMaxVariable(constraintAnchor2);
                                                                return;
                                                            } else {
                                                                constraintWidgetContainer2.addVerticalWrapMaxVariable(constraintAnchor2);
                                                                return;
                                                            }
                                                        }
                                                        linearSystem2.addGreaterThan(solverVariable2, solverVariable3, margin3, i32);
                                                        return;
                                                    }
                                                    return;
                                                }
                                                return;
                                            }
                                        }
                                        i25 = 5;
                                        i32 = i25;
                                        if (z20) {
                                            return;
                                        } else {
                                            return;
                                        }
                                    }
                                    return;
                                }
                            }
                            solverVariable12 = solverVariable4;
                            i25 = 5;
                            z20 = z2;
                            i32 = i25;
                            if (z20) {
                            }
                        }
                        if (i13 < i17 && z2 && z14) {
                            linearSystem2.addGreaterThan(createObjectVariable3, solverVariable5, 0, i16);
                            boolean z25 = z || this.mBaseline.mTarget == null;
                            if (z || this.mBaseline.mTarget == null) {
                                z15 = z25;
                            } else {
                                ConstraintWidget constraintWidget9 = this.mBaseline.mTarget.mOwner;
                                z15 = constraintWidget9.mDimensionRatio != 0.0f && constraintWidget9.mListDimensionBehaviors[0] == DimensionBehaviour.MATCH_CONSTRAINT && constraintWidget9.mListDimensionBehaviors[1] == DimensionBehaviour.MATCH_CONSTRAINT;
                            }
                            if (z15) {
                                linearSystem2.addGreaterThan(solverVariable6, solverVariable3, 0, i16);
                                return;
                            }
                            return;
                        }
                        return;
                    }
                    int max = Math.max(i7, i11);
                    if (i8 > 0) {
                        max = Math.min(i8, max);
                    }
                    linearSystem2.addEquality(createObjectVariable4, createObjectVariable3, max, 8);
                    z14 = z4;
                    i15 = i8;
                    solverVariable3 = createObjectVariable4;
                    i13 = i33;
                    solverVariable4 = createObjectVariable6;
                    z13 = false;
                }
                i14 = i7;
                if (!z11) {
                }
                if (i13 < i17) {
                    return;
                } else {
                    return;
                }
            }
        }
        z12 = false;
        i10 = this.mWidthOverride;
        if (i10 == -1) {
        }
        i10 = i2;
        i11 = this.mHeightOverride;
        if (i11 != -1) {
        }
        i11 = i10;
        if (this.mVisibility == 8) {
        }
        if (z11) {
        }
        if (z12) {
        }
        i14 = i7;
        if (!z11) {
        }
        if (i13 < i17) {
        }
    }

    /* renamed from: androidx.constraintlayout.core.widgets.ConstraintWidget$1, reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$androidx$constraintlayout$core$widgets$ConstraintAnchor$Type;
        static final /* synthetic */ int[] $SwitchMap$androidx$constraintlayout$core$widgets$ConstraintWidget$DimensionBehaviour;

        static {
            int[] iArr = new int[DimensionBehaviour.values().length];
            $SwitchMap$androidx$constraintlayout$core$widgets$ConstraintWidget$DimensionBehaviour = iArr;
            try {
                iArr[DimensionBehaviour.FIXED.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$androidx$constraintlayout$core$widgets$ConstraintWidget$DimensionBehaviour[DimensionBehaviour.WRAP_CONTENT.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$androidx$constraintlayout$core$widgets$ConstraintWidget$DimensionBehaviour[DimensionBehaviour.MATCH_PARENT.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$androidx$constraintlayout$core$widgets$ConstraintWidget$DimensionBehaviour[DimensionBehaviour.MATCH_CONSTRAINT.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            int[] iArr2 = new int[ConstraintAnchor.Type.values().length];
            $SwitchMap$androidx$constraintlayout$core$widgets$ConstraintAnchor$Type = iArr2;
            try {
                iArr2[ConstraintAnchor.Type.LEFT.ordinal()] = 1;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$androidx$constraintlayout$core$widgets$ConstraintAnchor$Type[ConstraintAnchor.Type.TOP.ordinal()] = 2;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$androidx$constraintlayout$core$widgets$ConstraintAnchor$Type[ConstraintAnchor.Type.RIGHT.ordinal()] = 3;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                $SwitchMap$androidx$constraintlayout$core$widgets$ConstraintAnchor$Type[ConstraintAnchor.Type.BOTTOM.ordinal()] = 4;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                $SwitchMap$androidx$constraintlayout$core$widgets$ConstraintAnchor$Type[ConstraintAnchor.Type.BASELINE.ordinal()] = 5;
            } catch (NoSuchFieldError unused9) {
            }
            try {
                $SwitchMap$androidx$constraintlayout$core$widgets$ConstraintAnchor$Type[ConstraintAnchor.Type.CENTER.ordinal()] = 6;
            } catch (NoSuchFieldError unused10) {
            }
            try {
                $SwitchMap$androidx$constraintlayout$core$widgets$ConstraintAnchor$Type[ConstraintAnchor.Type.CENTER_X.ordinal()] = 7;
            } catch (NoSuchFieldError unused11) {
            }
            try {
                $SwitchMap$androidx$constraintlayout$core$widgets$ConstraintAnchor$Type[ConstraintAnchor.Type.CENTER_Y.ordinal()] = 8;
            } catch (NoSuchFieldError unused12) {
            }
            try {
                $SwitchMap$androidx$constraintlayout$core$widgets$ConstraintAnchor$Type[ConstraintAnchor.Type.NONE.ordinal()] = 9;
            } catch (NoSuchFieldError unused13) {
            }
        }
    }

    public void updateFromSolver(LinearSystem linearSystem, boolean z) {
        VerticalWidgetRun verticalWidgetRun;
        HorizontalWidgetRun horizontalWidgetRun;
        int objectVariableValue = linearSystem.getObjectVariableValue(this.mLeft);
        int objectVariableValue2 = linearSystem.getObjectVariableValue(this.mTop);
        int objectVariableValue3 = linearSystem.getObjectVariableValue(this.mRight);
        int objectVariableValue4 = linearSystem.getObjectVariableValue(this.mBottom);
        if (z && (horizontalWidgetRun = this.horizontalRun) != null && horizontalWidgetRun.start.resolved && this.horizontalRun.end.resolved) {
            objectVariableValue = this.horizontalRun.start.value;
            objectVariableValue3 = this.horizontalRun.end.value;
        }
        if (z && (verticalWidgetRun = this.verticalRun) != null && verticalWidgetRun.start.resolved && this.verticalRun.end.resolved) {
            objectVariableValue2 = this.verticalRun.start.value;
            objectVariableValue4 = this.verticalRun.end.value;
        }
        int i = objectVariableValue4 - objectVariableValue2;
        if (objectVariableValue3 - objectVariableValue < 0 || i < 0 || objectVariableValue == Integer.MIN_VALUE || objectVariableValue == Integer.MAX_VALUE || objectVariableValue2 == Integer.MIN_VALUE || objectVariableValue2 == Integer.MAX_VALUE || objectVariableValue3 == Integer.MIN_VALUE || objectVariableValue3 == Integer.MAX_VALUE || objectVariableValue4 == Integer.MIN_VALUE || objectVariableValue4 == Integer.MAX_VALUE) {
            objectVariableValue = 0;
            objectVariableValue4 = 0;
            objectVariableValue2 = 0;
            objectVariableValue3 = 0;
        }
        setFrame(objectVariableValue, objectVariableValue2, objectVariableValue3, objectVariableValue4);
    }

    public void copy(ConstraintWidget constraintWidget, HashMap<ConstraintWidget, ConstraintWidget> hashMap) {
        this.mHorizontalResolution = constraintWidget.mHorizontalResolution;
        this.mVerticalResolution = constraintWidget.mVerticalResolution;
        this.mMatchConstraintDefaultWidth = constraintWidget.mMatchConstraintDefaultWidth;
        this.mMatchConstraintDefaultHeight = constraintWidget.mMatchConstraintDefaultHeight;
        int[] iArr = this.mResolvedMatchConstraintDefault;
        int[] iArr2 = constraintWidget.mResolvedMatchConstraintDefault;
        iArr[0] = iArr2[0];
        iArr[1] = iArr2[1];
        this.mMatchConstraintMinWidth = constraintWidget.mMatchConstraintMinWidth;
        this.mMatchConstraintMaxWidth = constraintWidget.mMatchConstraintMaxWidth;
        this.mMatchConstraintMinHeight = constraintWidget.mMatchConstraintMinHeight;
        this.mMatchConstraintMaxHeight = constraintWidget.mMatchConstraintMaxHeight;
        this.mMatchConstraintPercentHeight = constraintWidget.mMatchConstraintPercentHeight;
        this.mIsWidthWrapContent = constraintWidget.mIsWidthWrapContent;
        this.mIsHeightWrapContent = constraintWidget.mIsHeightWrapContent;
        this.mResolvedDimensionRatioSide = constraintWidget.mResolvedDimensionRatioSide;
        this.mResolvedDimensionRatio = constraintWidget.mResolvedDimensionRatio;
        int[] iArr3 = constraintWidget.mMaxDimension;
        this.mMaxDimension = Arrays.copyOf(iArr3, iArr3.length);
        this.mCircleConstraintAngle = constraintWidget.mCircleConstraintAngle;
        this.hasBaseline = constraintWidget.hasBaseline;
        this.inPlaceholder = constraintWidget.inPlaceholder;
        this.mLeft.reset();
        this.mTop.reset();
        this.mRight.reset();
        this.mBottom.reset();
        this.mBaseline.reset();
        this.mCenterX.reset();
        this.mCenterY.reset();
        this.mCenter.reset();
        this.mListDimensionBehaviors = (DimensionBehaviour[]) Arrays.copyOf(this.mListDimensionBehaviors, 2);
        this.mParent = this.mParent == null ? null : hashMap.get(constraintWidget.mParent);
        this.mWidth = constraintWidget.mWidth;
        this.mHeight = constraintWidget.mHeight;
        this.mDimensionRatio = constraintWidget.mDimensionRatio;
        this.mDimensionRatioSide = constraintWidget.mDimensionRatioSide;
        this.mX = constraintWidget.mX;
        this.mY = constraintWidget.mY;
        this.mRelX = constraintWidget.mRelX;
        this.mRelY = constraintWidget.mRelY;
        this.mOffsetX = constraintWidget.mOffsetX;
        this.mOffsetY = constraintWidget.mOffsetY;
        this.mBaselineDistance = constraintWidget.mBaselineDistance;
        this.mMinWidth = constraintWidget.mMinWidth;
        this.mMinHeight = constraintWidget.mMinHeight;
        this.mHorizontalBiasPercent = constraintWidget.mHorizontalBiasPercent;
        this.mVerticalBiasPercent = constraintWidget.mVerticalBiasPercent;
        this.mCompanionWidget = constraintWidget.mCompanionWidget;
        this.mContainerItemSkip = constraintWidget.mContainerItemSkip;
        this.mVisibility = constraintWidget.mVisibility;
        this.mAnimated = constraintWidget.mAnimated;
        this.mDebugName = constraintWidget.mDebugName;
        this.mType = constraintWidget.mType;
        this.mDistToTop = constraintWidget.mDistToTop;
        this.mDistToLeft = constraintWidget.mDistToLeft;
        this.mDistToRight = constraintWidget.mDistToRight;
        this.mDistToBottom = constraintWidget.mDistToBottom;
        this.mLeftHasCentered = constraintWidget.mLeftHasCentered;
        this.mRightHasCentered = constraintWidget.mRightHasCentered;
        this.mTopHasCentered = constraintWidget.mTopHasCentered;
        this.mBottomHasCentered = constraintWidget.mBottomHasCentered;
        this.mHorizontalWrapVisited = constraintWidget.mHorizontalWrapVisited;
        this.mVerticalWrapVisited = constraintWidget.mVerticalWrapVisited;
        this.mHorizontalChainStyle = constraintWidget.mHorizontalChainStyle;
        this.mVerticalChainStyle = constraintWidget.mVerticalChainStyle;
        this.mHorizontalChainFixedPosition = constraintWidget.mHorizontalChainFixedPosition;
        this.mVerticalChainFixedPosition = constraintWidget.mVerticalChainFixedPosition;
        float[] fArr = this.mWeight;
        float[] fArr2 = constraintWidget.mWeight;
        fArr[0] = fArr2[0];
        fArr[1] = fArr2[1];
        ConstraintWidget[] constraintWidgetArr = this.mListNextMatchConstraintsWidget;
        ConstraintWidget[] constraintWidgetArr2 = constraintWidget.mListNextMatchConstraintsWidget;
        constraintWidgetArr[0] = constraintWidgetArr2[0];
        constraintWidgetArr[1] = constraintWidgetArr2[1];
        ConstraintWidget[] constraintWidgetArr3 = this.mNextChainWidget;
        ConstraintWidget[] constraintWidgetArr4 = constraintWidget.mNextChainWidget;
        constraintWidgetArr3[0] = constraintWidgetArr4[0];
        constraintWidgetArr3[1] = constraintWidgetArr4[1];
        ConstraintWidget constraintWidget2 = constraintWidget.mHorizontalNextWidget;
        this.mHorizontalNextWidget = constraintWidget2 == null ? null : hashMap.get(constraintWidget2);
        ConstraintWidget constraintWidget3 = constraintWidget.mVerticalNextWidget;
        this.mVerticalNextWidget = constraintWidget3 != null ? hashMap.get(constraintWidget3) : null;
    }

    public void updateFromRuns(boolean z, boolean z2) {
        int i;
        int i2;
        boolean isResolved = z & this.horizontalRun.isResolved();
        boolean isResolved2 = z2 & this.verticalRun.isResolved();
        int i3 = this.horizontalRun.start.value;
        int i4 = this.verticalRun.start.value;
        int i5 = this.horizontalRun.end.value;
        int i6 = this.verticalRun.end.value;
        int i7 = i6 - i4;
        if (i5 - i3 < 0 || i7 < 0 || i3 == Integer.MIN_VALUE || i3 == Integer.MAX_VALUE || i4 == Integer.MIN_VALUE || i4 == Integer.MAX_VALUE || i5 == Integer.MIN_VALUE || i5 == Integer.MAX_VALUE || i6 == Integer.MIN_VALUE || i6 == Integer.MAX_VALUE) {
            i3 = 0;
            i4 = 0;
            i5 = 0;
            i6 = 0;
        }
        int i8 = i5 - i3;
        int i9 = i6 - i4;
        if (isResolved) {
            this.mX = i3;
        }
        if (isResolved2) {
            this.mY = i4;
        }
        if (this.mVisibility == 8) {
            this.mWidth = 0;
            this.mHeight = 0;
            return;
        }
        if (isResolved) {
            if (this.mListDimensionBehaviors[0] == DimensionBehaviour.FIXED && i8 < (i2 = this.mWidth)) {
                i8 = i2;
            }
            this.mWidth = i8;
            int i10 = this.mMinWidth;
            if (i8 < i10) {
                this.mWidth = i10;
            }
        }
        if (isResolved2) {
            if (this.mListDimensionBehaviors[1] == DimensionBehaviour.FIXED && i9 < (i = this.mHeight)) {
                i9 = i;
            }
            this.mHeight = i9;
            int i11 = this.mMinHeight;
            if (i9 < i11) {
                this.mHeight = i11;
            }
        }
    }

    public void addChildrenToSolverByDependency(ConstraintWidgetContainer constraintWidgetContainer, LinearSystem linearSystem, HashSet<ConstraintWidget> hashSet, int i, boolean z) {
        if (z) {
            if (!hashSet.contains(this)) {
                return;
            }
            Optimizer.checkMatchParent(constraintWidgetContainer, linearSystem, this);
            hashSet.remove(this);
            addToSolver(linearSystem, constraintWidgetContainer.optimizeFor(64));
        }
        if (i == 0) {
            HashSet<ConstraintAnchor> dependents = this.mLeft.getDependents();
            if (dependents != null) {
                Iterator<ConstraintAnchor> it = dependents.iterator();
                while (it.hasNext()) {
                    it.next().mOwner.addChildrenToSolverByDependency(constraintWidgetContainer, linearSystem, hashSet, i, true);
                }
            }
            HashSet<ConstraintAnchor> dependents2 = this.mRight.getDependents();
            if (dependents2 != null) {
                Iterator<ConstraintAnchor> it2 = dependents2.iterator();
                while (it2.hasNext()) {
                    it2.next().mOwner.addChildrenToSolverByDependency(constraintWidgetContainer, linearSystem, hashSet, i, true);
                }
                return;
            }
            return;
        }
        HashSet<ConstraintAnchor> dependents3 = this.mTop.getDependents();
        if (dependents3 != null) {
            Iterator<ConstraintAnchor> it3 = dependents3.iterator();
            while (it3.hasNext()) {
                it3.next().mOwner.addChildrenToSolverByDependency(constraintWidgetContainer, linearSystem, hashSet, i, true);
            }
        }
        HashSet<ConstraintAnchor> dependents4 = this.mBottom.getDependents();
        if (dependents4 != null) {
            Iterator<ConstraintAnchor> it4 = dependents4.iterator();
            while (it4.hasNext()) {
                it4.next().mOwner.addChildrenToSolverByDependency(constraintWidgetContainer, linearSystem, hashSet, i, true);
            }
        }
        HashSet<ConstraintAnchor> dependents5 = this.mBaseline.getDependents();
        if (dependents5 != null) {
            Iterator<ConstraintAnchor> it5 = dependents5.iterator();
            while (it5.hasNext()) {
                it5.next().mOwner.addChildrenToSolverByDependency(constraintWidgetContainer, linearSystem, hashSet, i, true);
            }
        }
    }

    public void getSceneString(StringBuilder sb) {
        sb.append("  " + this.stringId + ":{\n");
        sb.append("    actualWidth:" + this.mWidth);
        sb.append("\n");
        sb.append("    actualHeight:" + this.mHeight);
        sb.append("\n");
        sb.append("    actualLeft:" + this.mX);
        sb.append("\n");
        sb.append("    actualTop:" + this.mY);
        sb.append("\n");
        getSceneString(sb, "left", this.mLeft);
        getSceneString(sb, "top", this.mTop);
        getSceneString(sb, "right", this.mRight);
        getSceneString(sb, "bottom", this.mBottom);
        getSceneString(sb, "baseline", this.mBaseline);
        getSceneString(sb, "centerX", this.mCenterX);
        getSceneString(sb, "centerY", this.mCenterY);
        getSceneString(sb, "    width", this.mWidth, this.mMinWidth, this.mMaxDimension[0], this.mWidthOverride, this.mMatchConstraintMinWidth, this.mMatchConstraintDefaultWidth, this.mMatchConstraintPercentWidth, this.mWeight[0]);
        getSceneString(sb, "    height", this.mHeight, this.mMinHeight, this.mMaxDimension[1], this.mHeightOverride, this.mMatchConstraintMinHeight, this.mMatchConstraintDefaultHeight, this.mMatchConstraintPercentHeight, this.mWeight[1]);
        serializeDimensionRatio(sb, "    dimensionRatio", this.mDimensionRatio, this.mDimensionRatioSide);
        serializeAttribute(sb, "    horizontalBias", this.mHorizontalBiasPercent, DEFAULT_BIAS);
        serializeAttribute(sb, "    verticalBias", this.mVerticalBiasPercent, DEFAULT_BIAS);
        serializeAttribute(sb, "    horizontalChainStyle", this.mHorizontalChainStyle, 0);
        serializeAttribute(sb, "    verticalChainStyle", this.mVerticalChainStyle, 0);
        sb.append("  }");
    }

    private void getSceneString(StringBuilder sb, String str, int i, int i2, int i3, int i4, int i5, int i6, float f, float f2) {
        sb.append(str);
        sb.append(" :  {\n");
        serializeAttribute(sb, "      size", i, 0);
        serializeAttribute(sb, "      min", i2, 0);
        serializeAttribute(sb, "      max", i3, Integer.MAX_VALUE);
        serializeAttribute(sb, "      matchMin", i5, 0);
        serializeAttribute(sb, "      matchDef", i6, 0);
        serializeAttribute(sb, "      matchPercent", f, 1.0f);
        sb.append("    },\n");
    }

    private void getSceneString(StringBuilder sb, String str, ConstraintAnchor constraintAnchor) {
        if (constraintAnchor.mTarget == null) {
            return;
        }
        sb.append("    ");
        sb.append(str);
        sb.append(" : [ '");
        sb.append(constraintAnchor.mTarget);
        sb.append("'");
        if (constraintAnchor.mGoneMargin != Integer.MIN_VALUE || constraintAnchor.mMargin != 0) {
            sb.append(",");
            sb.append(constraintAnchor.mMargin);
            if (constraintAnchor.mGoneMargin != Integer.MIN_VALUE) {
                sb.append(",");
                sb.append(constraintAnchor.mGoneMargin);
                sb.append(",");
            }
        }
        sb.append(" ] ,\n");
    }
}
