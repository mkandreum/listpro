package androidx.transition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import androidx.core.content.res.TypedArrayUtils;
import androidx.transition.Transition;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/* loaded from: classes.dex */
public abstract class Visibility extends Transition {
    public static final int MODE_IN = 1;
    public static final int MODE_OUT = 2;
    private static final String PROPNAME_SCREEN_LOCATION = "android:visibility:screenLocation";
    private int mMode;
    static final String PROPNAME_VISIBILITY = "android:visibility:visibility";
    private static final String PROPNAME_PARENT = "android:visibility:parent";
    private static final String[] sTransitionProperties = {PROPNAME_VISIBILITY, PROPNAME_PARENT};

    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {
    }

    public Animator onAppear(ViewGroup viewGroup, View view, TransitionValues transitionValues, TransitionValues transitionValues2) {
        return null;
    }

    public Animator onDisappear(ViewGroup viewGroup, View view, TransitionValues transitionValues, TransitionValues transitionValues2) {
        return null;
    }

    private static class VisibilityInfo {
        ViewGroup mEndParent;
        int mEndVisibility;
        boolean mFadeIn;
        ViewGroup mStartParent;
        int mStartVisibility;
        boolean mVisibilityChange;

        VisibilityInfo() {
        }
    }

    public Visibility() {
        this.mMode = 3;
    }

    public Visibility(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mMode = 3;
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, Styleable.VISIBILITY_TRANSITION);
        int namedInt = TypedArrayUtils.getNamedInt(obtainStyledAttributes, (XmlResourceParser) attributeSet, "transitionVisibilityMode", 0, 0);
        obtainStyledAttributes.recycle();
        if (namedInt != 0) {
            setMode(namedInt);
        }
    }

    public void setMode(int i) {
        if ((i & (-4)) != 0) {
            throw new IllegalArgumentException("Only MODE_IN and MODE_OUT flags are allowed");
        }
        this.mMode = i;
    }

    public int getMode() {
        return this.mMode;
    }

    @Override // androidx.transition.Transition
    public String[] getTransitionProperties() {
        return sTransitionProperties;
    }

    private void captureValues(TransitionValues transitionValues) {
        transitionValues.values.put(PROPNAME_VISIBILITY, Integer.valueOf(transitionValues.view.getVisibility()));
        transitionValues.values.put(PROPNAME_PARENT, transitionValues.view.getParent());
        int[] iArr = new int[2];
        transitionValues.view.getLocationOnScreen(iArr);
        transitionValues.values.put(PROPNAME_SCREEN_LOCATION, iArr);
    }

    @Override // androidx.transition.Transition
    public void captureStartValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    @Override // androidx.transition.Transition
    public void captureEndValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    public boolean isVisible(TransitionValues transitionValues) {
        if (transitionValues == null) {
            return false;
        }
        return ((Integer) transitionValues.values.get(PROPNAME_VISIBILITY)).intValue() == 0 && ((View) transitionValues.values.get(PROPNAME_PARENT)) != null;
    }

    private VisibilityInfo getVisibilityChangeInfo(TransitionValues transitionValues, TransitionValues transitionValues2) {
        VisibilityInfo visibilityInfo = new VisibilityInfo();
        visibilityInfo.mVisibilityChange = false;
        visibilityInfo.mFadeIn = false;
        if (transitionValues != null && transitionValues.values.containsKey(PROPNAME_VISIBILITY)) {
            visibilityInfo.mStartVisibility = ((Integer) transitionValues.values.get(PROPNAME_VISIBILITY)).intValue();
            visibilityInfo.mStartParent = (ViewGroup) transitionValues.values.get(PROPNAME_PARENT);
        } else {
            visibilityInfo.mStartVisibility = -1;
            visibilityInfo.mStartParent = null;
        }
        if (transitionValues2 != null && transitionValues2.values.containsKey(PROPNAME_VISIBILITY)) {
            visibilityInfo.mEndVisibility = ((Integer) transitionValues2.values.get(PROPNAME_VISIBILITY)).intValue();
            visibilityInfo.mEndParent = (ViewGroup) transitionValues2.values.get(PROPNAME_PARENT);
        } else {
            visibilityInfo.mEndVisibility = -1;
            visibilityInfo.mEndParent = null;
        }
        if (transitionValues != null && transitionValues2 != null) {
            if (visibilityInfo.mStartVisibility != visibilityInfo.mEndVisibility || visibilityInfo.mStartParent != visibilityInfo.mEndParent) {
                if (visibilityInfo.mStartVisibility != visibilityInfo.mEndVisibility) {
                    if (visibilityInfo.mStartVisibility == 0) {
                        visibilityInfo.mFadeIn = false;
                        visibilityInfo.mVisibilityChange = true;
                        return visibilityInfo;
                    }
                    if (visibilityInfo.mEndVisibility == 0) {
                        visibilityInfo.mFadeIn = true;
                        visibilityInfo.mVisibilityChange = true;
                        return visibilityInfo;
                    }
                } else {
                    if (visibilityInfo.mEndParent == null) {
                        visibilityInfo.mFadeIn = false;
                        visibilityInfo.mVisibilityChange = true;
                        return visibilityInfo;
                    }
                    if (visibilityInfo.mStartParent == null) {
                        visibilityInfo.mFadeIn = true;
                        visibilityInfo.mVisibilityChange = true;
                        return visibilityInfo;
                    }
                }
            }
        } else {
            if (transitionValues == null && visibilityInfo.mEndVisibility == 0) {
                visibilityInfo.mFadeIn = true;
                visibilityInfo.mVisibilityChange = true;
                return visibilityInfo;
            }
            if (transitionValues2 == null && visibilityInfo.mStartVisibility == 0) {
                visibilityInfo.mFadeIn = false;
                visibilityInfo.mVisibilityChange = true;
            }
        }
        return visibilityInfo;
    }

    @Override // androidx.transition.Transition
    public Animator createAnimator(ViewGroup viewGroup, TransitionValues transitionValues, TransitionValues transitionValues2) {
        VisibilityInfo visibilityChangeInfo = getVisibilityChangeInfo(transitionValues, transitionValues2);
        if (!visibilityChangeInfo.mVisibilityChange) {
            return null;
        }
        if (visibilityChangeInfo.mStartParent == null && visibilityChangeInfo.mEndParent == null) {
            return null;
        }
        if (visibilityChangeInfo.mFadeIn) {
            return onAppear(viewGroup, transitionValues, visibilityChangeInfo.mStartVisibility, transitionValues2, visibilityChangeInfo.mEndVisibility);
        }
        return onDisappear(viewGroup, transitionValues, visibilityChangeInfo.mStartVisibility, transitionValues2, visibilityChangeInfo.mEndVisibility);
    }

    public Animator onAppear(ViewGroup viewGroup, TransitionValues transitionValues, int i, TransitionValues transitionValues2, int i2) {
        if ((this.mMode & 1) != 1 || transitionValues2 == null) {
            return null;
        }
        if (transitionValues == null) {
            View view = (View) transitionValues2.view.getParent();
            if (getVisibilityChangeInfo(getMatchedTransitionValues(view, false), getTransitionValues(view, false)).mVisibilityChange) {
                return null;
            }
        }
        return onAppear(viewGroup, transitionValues2.view, transitionValues, transitionValues2);
    }

    /* JADX WARN: Code restructure failed: missing block: B:53:0x007f, code lost:
    
        if (r9.mCanRemoveViews != false) goto L43;
     */
    /* JADX WARN: Removed duplicated region for block: B:39:0x0040  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public Animator onDisappear(ViewGroup viewGroup, TransitionValues transitionValues, int i, TransitionValues transitionValues2, int i2) {
        View view;
        boolean z;
        View view2;
        boolean z2;
        if ((this.mMode & 2) != 2 || transitionValues == null) {
            return null;
        }
        View view3 = transitionValues.view;
        View view4 = transitionValues2 != null ? transitionValues2.view : null;
        View view5 = (View) view3.getTag(R.id.save_overlay_view);
        if (view5 != null) {
            view2 = null;
            z2 = true;
        } else if (view4 == null || view4.getParent() == null) {
            if (view4 != null) {
                view = null;
                z = false;
                if (z) {
                    if (view3.getParent() != null) {
                        if (view3.getParent() instanceof View) {
                            View view6 = (View) view3.getParent();
                            if (!getVisibilityChangeInfo(getTransitionValues(view6, true), getMatchedTransitionValues(view6, true)).mVisibilityChange) {
                                view4 = TransitionUtils.copyViewImage(viewGroup, view3, view6);
                            } else {
                                int id = view6.getId();
                                if (view6.getParent() == null) {
                                    if (id != -1) {
                                        if (viewGroup.findViewById(id) != null) {
                                        }
                                    }
                                }
                            }
                        }
                    }
                    view2 = view;
                    z2 = false;
                    view5 = view3;
                }
                View view7 = view;
                view5 = view4;
                view2 = view7;
                z2 = false;
            }
            view4 = null;
            view = null;
            z = true;
            if (z) {
            }
            View view72 = view;
            view5 = view4;
            view2 = view72;
            z2 = false;
        } else {
            if (i2 == 4 || view3 == view4) {
                view = view4;
                z = false;
                view4 = null;
                if (z) {
                }
                View view722 = view;
                view5 = view4;
                view2 = view722;
                z2 = false;
            }
            view4 = null;
            view = null;
            z = true;
            if (z) {
            }
            View view7222 = view;
            view5 = view4;
            view2 = view7222;
            z2 = false;
        }
        if (view5 == null) {
            if (view2 == null) {
                return null;
            }
            int visibility = view2.getVisibility();
            ViewUtils.setTransitionVisibility(view2, 0);
            Animator onDisappear = onDisappear(viewGroup, view2, transitionValues, transitionValues2);
            if (onDisappear != null) {
                DisappearListener disappearListener = new DisappearListener(view2, i2, true);
                onDisappear.addListener(disappearListener);
                getRootTransition().addListener(disappearListener);
                return onDisappear;
            }
            ViewUtils.setTransitionVisibility(view2, visibility);
            return onDisappear;
        }
        if (!z2) {
            int[] iArr = (int[]) transitionValues.values.get(PROPNAME_SCREEN_LOCATION);
            int i3 = iArr[0];
            int i4 = iArr[1];
            int[] iArr2 = new int[2];
            viewGroup.getLocationOnScreen(iArr2);
            view5.offsetLeftAndRight((i3 - iArr2[0]) - view5.getLeft());
            view5.offsetTopAndBottom((i4 - iArr2[1]) - view5.getTop());
            viewGroup.getOverlay().add(view5);
        }
        Animator onDisappear2 = onDisappear(viewGroup, view5, transitionValues, transitionValues2);
        if (!z2) {
            if (onDisappear2 == null) {
                viewGroup.getOverlay().remove(view5);
                return onDisappear2;
            }
            view3.setTag(R.id.save_overlay_view, view5);
            OverlayListener overlayListener = new OverlayListener(viewGroup, view5, view3);
            onDisappear2.addListener(overlayListener);
            onDisappear2.addPauseListener(overlayListener);
            getRootTransition().addListener(overlayListener);
        }
        return onDisappear2;
    }

    @Override // androidx.transition.Transition
    public boolean isTransitionRequired(TransitionValues transitionValues, TransitionValues transitionValues2) {
        if (transitionValues == null && transitionValues2 == null) {
            return false;
        }
        if (transitionValues != null && transitionValues2 != null && transitionValues2.values.containsKey(PROPNAME_VISIBILITY) != transitionValues.values.containsKey(PROPNAME_VISIBILITY)) {
            return false;
        }
        VisibilityInfo visibilityChangeInfo = getVisibilityChangeInfo(transitionValues, transitionValues2);
        return visibilityChangeInfo.mVisibilityChange && (visibilityChangeInfo.mStartVisibility == 0 || visibilityChangeInfo.mEndVisibility == 0);
    }

    private static class DisappearListener extends AnimatorListenerAdapter implements Transition.TransitionListener {
        boolean mCanceled = false;
        private final int mFinalVisibility;
        private boolean mLayoutSuppressed;
        private final ViewGroup mParent;
        private final boolean mSuppressLayout;
        private final View mView;

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationRepeat(Animator animator) {
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationStart(Animator animator) {
        }

        @Override // androidx.transition.Transition.TransitionListener
        public void onTransitionCancel(Transition transition) {
        }

        @Override // androidx.transition.Transition.TransitionListener
        public void onTransitionStart(Transition transition) {
        }

        DisappearListener(View view, int i, boolean z) {
            this.mView = view;
            this.mFinalVisibility = i;
            this.mParent = (ViewGroup) view.getParent();
            this.mSuppressLayout = z;
            suppressLayout(true);
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationCancel(Animator animator) {
            this.mCanceled = true;
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator) {
            hideViewWhenNotCanceled();
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationStart(Animator animator, boolean z) {
            if (z) {
                ViewUtils.setTransitionVisibility(this.mView, 0);
                ViewGroup viewGroup = this.mParent;
                if (viewGroup != null) {
                    viewGroup.invalidate();
                }
            }
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator, boolean z) {
            if (z) {
                return;
            }
            hideViewWhenNotCanceled();
        }

        @Override // androidx.transition.Transition.TransitionListener
        public void onTransitionEnd(Transition transition) {
            transition.removeListener(this);
        }

        @Override // androidx.transition.Transition.TransitionListener
        public void onTransitionPause(Transition transition) {
            suppressLayout(false);
            if (this.mCanceled) {
                return;
            }
            ViewUtils.setTransitionVisibility(this.mView, this.mFinalVisibility);
        }

        @Override // androidx.transition.Transition.TransitionListener
        public void onTransitionResume(Transition transition) {
            suppressLayout(true);
            if (this.mCanceled) {
                return;
            }
            ViewUtils.setTransitionVisibility(this.mView, 0);
        }

        private void hideViewWhenNotCanceled() {
            if (!this.mCanceled) {
                ViewUtils.setTransitionVisibility(this.mView, this.mFinalVisibility);
                ViewGroup viewGroup = this.mParent;
                if (viewGroup != null) {
                    viewGroup.invalidate();
                }
            }
            suppressLayout(false);
        }

        private void suppressLayout(boolean z) {
            ViewGroup viewGroup;
            if (!this.mSuppressLayout || this.mLayoutSuppressed == z || (viewGroup = this.mParent) == null) {
                return;
            }
            this.mLayoutSuppressed = z;
            ViewGroupUtils.suppressLayout(viewGroup, z);
        }
    }

    private class OverlayListener extends AnimatorListenerAdapter implements Transition.TransitionListener {
        private boolean mHasOverlay = true;
        private final ViewGroup mOverlayHost;
        private final View mOverlayView;
        private final View mStartView;

        @Override // androidx.transition.Transition.TransitionListener
        public void onTransitionPause(Transition transition) {
        }

        @Override // androidx.transition.Transition.TransitionListener
        public void onTransitionResume(Transition transition) {
        }

        @Override // androidx.transition.Transition.TransitionListener
        public void onTransitionStart(Transition transition) {
        }

        OverlayListener(ViewGroup viewGroup, View view, View view2) {
            this.mOverlayHost = viewGroup;
            this.mOverlayView = view;
            this.mStartView = view2;
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorPauseListener
        public void onAnimationPause(Animator animator) {
            this.mOverlayHost.getOverlay().remove(this.mOverlayView);
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorPauseListener
        public void onAnimationResume(Animator animator) {
            if (this.mOverlayView.getParent() == null) {
                this.mOverlayHost.getOverlay().add(this.mOverlayView);
            } else {
                Visibility.this.cancel();
            }
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationStart(Animator animator, boolean z) {
            if (z) {
                this.mStartView.setTag(R.id.save_overlay_view, this.mOverlayView);
                this.mOverlayHost.getOverlay().add(this.mOverlayView);
                this.mHasOverlay = true;
            }
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator) {
            removeFromOverlay();
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator, boolean z) {
            if (z) {
                return;
            }
            removeFromOverlay();
        }

        @Override // androidx.transition.Transition.TransitionListener
        public void onTransitionEnd(Transition transition) {
            transition.removeListener(this);
        }

        @Override // androidx.transition.Transition.TransitionListener
        public void onTransitionCancel(Transition transition) {
            if (this.mHasOverlay) {
                removeFromOverlay();
            }
        }

        private void removeFromOverlay() {
            this.mStartView.setTag(R.id.save_overlay_view, null);
            this.mOverlayHost.getOverlay().remove(this.mOverlayView);
            this.mHasOverlay = false;
        }
    }
}
