package androidx.fragment.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import androidx.collection.ArrayMap;
import androidx.core.app.SharedElementCallback;
import androidx.core.os.CancellationSignal;
import androidx.core.util.Preconditions;
import androidx.core.view.OneShotPreDrawListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewGroupCompat;
import androidx.fragment.app.FragmentAnim;
import androidx.fragment.app.SpecialEffectsController;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/* loaded from: classes.dex */
class DefaultSpecialEffectsController extends SpecialEffectsController {
    DefaultSpecialEffectsController(ViewGroup viewGroup) {
        super(viewGroup);
    }

    @Override // androidx.fragment.app.SpecialEffectsController
    void executeOperations(List<SpecialEffectsController.Operation> list, boolean z) {
        SpecialEffectsController.Operation operation = null;
        SpecialEffectsController.Operation operation2 = null;
        for (SpecialEffectsController.Operation operation3 : list) {
            SpecialEffectsController.Operation.State from = SpecialEffectsController.Operation.State.from(operation3.getFragment().mView);
            int i = AnonymousClass10.$SwitchMap$androidx$fragment$app$SpecialEffectsController$Operation$State[operation3.getFinalState().ordinal()];
            if (i == 1 || i == 2 || i == 3) {
                if (from == SpecialEffectsController.Operation.State.VISIBLE && operation == null) {
                    operation = operation3;
                }
            } else if (i == 4 && from != SpecialEffectsController.Operation.State.VISIBLE) {
                operation2 = operation3;
            }
        }
        if (FragmentManager.isLoggingEnabled(2)) {
            Log.v(FragmentManager.TAG, "Executing operations from " + operation + " to " + operation2);
        }
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        final ArrayList arrayList3 = new ArrayList(list);
        syncAnimations(list);
        for (final SpecialEffectsController.Operation operation4 : list) {
            CancellationSignal cancellationSignal = new CancellationSignal();
            operation4.markStartedSpecialEffect(cancellationSignal);
            arrayList.add(new AnimationInfo(operation4, cancellationSignal, z));
            CancellationSignal cancellationSignal2 = new CancellationSignal();
            operation4.markStartedSpecialEffect(cancellationSignal2);
            boolean z2 = false;
            if (z) {
                if (operation4 != operation) {
                    arrayList2.add(new TransitionInfo(operation4, cancellationSignal2, z, z2));
                    operation4.addCompletionListener(new Runnable() { // from class: androidx.fragment.app.DefaultSpecialEffectsController.1
                        @Override // java.lang.Runnable
                        public void run() {
                            if (arrayList3.contains(operation4)) {
                                arrayList3.remove(operation4);
                                DefaultSpecialEffectsController.this.applyContainerChanges(operation4);
                            }
                        }
                    });
                }
                z2 = true;
                arrayList2.add(new TransitionInfo(operation4, cancellationSignal2, z, z2));
                operation4.addCompletionListener(new Runnable() { // from class: androidx.fragment.app.DefaultSpecialEffectsController.1
                    @Override // java.lang.Runnable
                    public void run() {
                        if (arrayList3.contains(operation4)) {
                            arrayList3.remove(operation4);
                            DefaultSpecialEffectsController.this.applyContainerChanges(operation4);
                        }
                    }
                });
            } else {
                if (operation4 != operation2) {
                    arrayList2.add(new TransitionInfo(operation4, cancellationSignal2, z, z2));
                    operation4.addCompletionListener(new Runnable() { // from class: androidx.fragment.app.DefaultSpecialEffectsController.1
                        @Override // java.lang.Runnable
                        public void run() {
                            if (arrayList3.contains(operation4)) {
                                arrayList3.remove(operation4);
                                DefaultSpecialEffectsController.this.applyContainerChanges(operation4);
                            }
                        }
                    });
                }
                z2 = true;
                arrayList2.add(new TransitionInfo(operation4, cancellationSignal2, z, z2));
                operation4.addCompletionListener(new Runnable() { // from class: androidx.fragment.app.DefaultSpecialEffectsController.1
                    @Override // java.lang.Runnable
                    public void run() {
                        if (arrayList3.contains(operation4)) {
                            arrayList3.remove(operation4);
                            DefaultSpecialEffectsController.this.applyContainerChanges(operation4);
                        }
                    }
                });
            }
        }
        Map<SpecialEffectsController.Operation, Boolean> startTransitions = startTransitions(arrayList2, arrayList3, z, operation, operation2);
        startAnimations(arrayList, arrayList3, startTransitions.containsValue(true), startTransitions);
        Iterator<SpecialEffectsController.Operation> it = arrayList3.iterator();
        while (it.hasNext()) {
            applyContainerChanges(it.next());
        }
        arrayList3.clear();
        if (FragmentManager.isLoggingEnabled(2)) {
            Log.v(FragmentManager.TAG, "Completed executing operations from " + operation + " to " + operation2);
        }
    }

    /* renamed from: androidx.fragment.app.DefaultSpecialEffectsController$10, reason: invalid class name */
    static /* synthetic */ class AnonymousClass10 {
        static final /* synthetic */ int[] $SwitchMap$androidx$fragment$app$SpecialEffectsController$Operation$State;

        static {
            int[] iArr = new int[SpecialEffectsController.Operation.State.values().length];
            $SwitchMap$androidx$fragment$app$SpecialEffectsController$Operation$State = iArr;
            try {
                iArr[SpecialEffectsController.Operation.State.GONE.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$androidx$fragment$app$SpecialEffectsController$Operation$State[SpecialEffectsController.Operation.State.INVISIBLE.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$androidx$fragment$app$SpecialEffectsController$Operation$State[SpecialEffectsController.Operation.State.REMOVED.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$androidx$fragment$app$SpecialEffectsController$Operation$State[SpecialEffectsController.Operation.State.VISIBLE.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
        }
    }

    private void syncAnimations(List<SpecialEffectsController.Operation> list) {
        Fragment fragment = list.get(list.size() - 1).getFragment();
        for (SpecialEffectsController.Operation operation : list) {
            operation.getFragment().mAnimationInfo.mEnterAnim = fragment.mAnimationInfo.mEnterAnim;
            operation.getFragment().mAnimationInfo.mExitAnim = fragment.mAnimationInfo.mExitAnim;
            operation.getFragment().mAnimationInfo.mPopEnterAnim = fragment.mAnimationInfo.mPopEnterAnim;
            operation.getFragment().mAnimationInfo.mPopExitAnim = fragment.mAnimationInfo.mPopExitAnim;
        }
    }

    private void startAnimations(List<AnimationInfo> list, List<SpecialEffectsController.Operation> list2, boolean z, Map<SpecialEffectsController.Operation, Boolean> map) {
        final SpecialEffectsController.Operation operation;
        final AnimationInfo animationInfo;
        final View view;
        final ViewGroup container = getContainer();
        Context context = container.getContext();
        ArrayList arrayList = new ArrayList();
        boolean z2 = false;
        for (final AnimationInfo animationInfo2 : list) {
            if (animationInfo2.isVisibilityUnchanged()) {
                animationInfo2.completeSpecialEffect();
            } else {
                FragmentAnim.AnimationOrAnimator animation = animationInfo2.getAnimation(context);
                if (animation == null) {
                    animationInfo2.completeSpecialEffect();
                } else {
                    final Animator animator = animation.animator;
                    if (animator == null) {
                        arrayList.add(animationInfo2);
                    } else {
                        final SpecialEffectsController.Operation operation2 = animationInfo2.getOperation();
                        Fragment fragment = operation2.getFragment();
                        if (Boolean.TRUE.equals(map.get(operation2))) {
                            if (FragmentManager.isLoggingEnabled(2)) {
                                Log.v(FragmentManager.TAG, "Ignoring Animator set on " + fragment + " as this Fragment was involved in a Transition.");
                            }
                            animationInfo2.completeSpecialEffect();
                        } else {
                            final boolean z3 = operation2.getFinalState() == SpecialEffectsController.Operation.State.GONE;
                            if (z3) {
                                list2.remove(operation2);
                            }
                            final View view2 = fragment.mView;
                            container.startViewTransition(view2);
                            final ViewGroup viewGroup = container;
                            container = viewGroup;
                            animator.addListener(new AnimatorListenerAdapter() { // from class: androidx.fragment.app.DefaultSpecialEffectsController.2
                                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                                public void onAnimationEnd(Animator animator2) {
                                    viewGroup.endViewTransition(view2);
                                    if (z3) {
                                        operation2.getFinalState().applyState(view2);
                                    }
                                    animationInfo2.completeSpecialEffect();
                                    if (FragmentManager.isLoggingEnabled(2)) {
                                        Log.v(FragmentManager.TAG, "Animator from operation " + operation2 + " has ended.");
                                    }
                                }
                            });
                            animator.setTarget(view2);
                            animator.start();
                            if (FragmentManager.isLoggingEnabled(2)) {
                                Log.v(FragmentManager.TAG, "Animator from operation " + operation2 + " has started.");
                            }
                            animationInfo2.getSignal().setOnCancelListener(new CancellationSignal.OnCancelListener() { // from class: androidx.fragment.app.DefaultSpecialEffectsController.3
                                @Override // androidx.core.os.CancellationSignal.OnCancelListener
                                public void onCancel() {
                                    animator.end();
                                    if (FragmentManager.isLoggingEnabled(2)) {
                                        Log.v(FragmentManager.TAG, "Animator from operation " + operation2 + " has been canceled.");
                                    }
                                }
                            });
                            z2 = true;
                        }
                    }
                }
            }
        }
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            AnimationInfo animationInfo3 = (AnimationInfo) it.next();
            SpecialEffectsController.Operation operation3 = animationInfo3.getOperation();
            Fragment fragment2 = operation3.getFragment();
            if (z) {
                if (FragmentManager.isLoggingEnabled(2)) {
                    Log.v(FragmentManager.TAG, "Ignoring Animation set on " + fragment2 + " as Animations cannot run alongside Transitions.");
                }
                animationInfo3.completeSpecialEffect();
            } else if (z2) {
                if (FragmentManager.isLoggingEnabled(2)) {
                    Log.v(FragmentManager.TAG, "Ignoring Animation set on " + fragment2 + " as Animations cannot run alongside Animators.");
                }
                animationInfo3.completeSpecialEffect();
            } else {
                View view3 = fragment2.mView;
                Animation animation2 = (Animation) Preconditions.checkNotNull(((FragmentAnim.AnimationOrAnimator) Preconditions.checkNotNull(animationInfo3.getAnimation(context))).animation);
                if (operation3.getFinalState() != SpecialEffectsController.Operation.State.REMOVED) {
                    view3.startAnimation(animation2);
                    animationInfo3.completeSpecialEffect();
                    operation = operation3;
                    animationInfo = animationInfo3;
                    view = view3;
                } else {
                    container.startViewTransition(view3);
                    FragmentAnim.EndViewTransitionAnimation endViewTransitionAnimation = new FragmentAnim.EndViewTransitionAnimation(animation2, container, view3);
                    operation = operation3;
                    animationInfo = animationInfo3;
                    view = view3;
                    endViewTransitionAnimation.setAnimationListener(new Animation.AnimationListener() { // from class: androidx.fragment.app.DefaultSpecialEffectsController.4
                        @Override // android.view.animation.Animation.AnimationListener
                        public void onAnimationRepeat(Animation animation3) {
                        }

                        @Override // android.view.animation.Animation.AnimationListener
                        public void onAnimationStart(Animation animation3) {
                            if (FragmentManager.isLoggingEnabled(2)) {
                                Log.v(FragmentManager.TAG, "Animation from operation " + operation + " has reached onAnimationStart.");
                            }
                        }

                        @Override // android.view.animation.Animation.AnimationListener
                        public void onAnimationEnd(Animation animation3) {
                            container.post(new Runnable() { // from class: androidx.fragment.app.DefaultSpecialEffectsController.4.1
                                @Override // java.lang.Runnable
                                public void run() {
                                    container.endViewTransition(view);
                                    animationInfo.completeSpecialEffect();
                                }
                            });
                            if (FragmentManager.isLoggingEnabled(2)) {
                                Log.v(FragmentManager.TAG, "Animation from operation " + operation + " has ended.");
                            }
                        }
                    });
                    view.startAnimation(endViewTransitionAnimation);
                    if (FragmentManager.isLoggingEnabled(2)) {
                        Log.v(FragmentManager.TAG, "Animation from operation " + operation + " has started.");
                    }
                }
                CancellationSignal signal = animationInfo.getSignal();
                final AnimationInfo animationInfo4 = animationInfo;
                final SpecialEffectsController.Operation operation4 = operation;
                final View view4 = view;
                signal.setOnCancelListener(new CancellationSignal.OnCancelListener() { // from class: androidx.fragment.app.DefaultSpecialEffectsController.5
                    @Override // androidx.core.os.CancellationSignal.OnCancelListener
                    public void onCancel() {
                        view4.clearAnimation();
                        container.endViewTransition(view4);
                        animationInfo4.completeSpecialEffect();
                        if (FragmentManager.isLoggingEnabled(2)) {
                            Log.v(FragmentManager.TAG, "Animation from operation " + operation4 + " has been cancelled.");
                        }
                    }
                });
            }
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    private Map<SpecialEffectsController.Operation, Boolean> startTransitions(List<TransitionInfo> list, List<SpecialEffectsController.Operation> list2, boolean z, SpecialEffectsController.Operation operation, SpecialEffectsController.Operation operation2) {
        View view;
        String str;
        String str2;
        Object obj;
        HashMap hashMap;
        ArrayList<View> arrayList;
        View view2;
        Object obj2;
        String str3;
        ArrayList<View> arrayList2;
        ArrayList arrayList3;
        View view3;
        Object obj3;
        SpecialEffectsController.Operation operation3;
        int i;
        View view4;
        final Rect rect;
        ArrayMap arrayMap;
        ArrayList<View> arrayList4;
        HashMap hashMap2;
        ArrayList<View> arrayList5;
        SharedElementCallback sharedElementCallback;
        SharedElementCallback sharedElementCallback2;
        Rect rect2;
        int i2;
        final View view5;
        String findKeyForValue;
        int i3;
        final boolean z2 = z;
        final SpecialEffectsController.Operation operation4 = operation;
        final SpecialEffectsController.Operation operation5 = operation2;
        HashMap hashMap3 = new HashMap();
        final FragmentTransitionImpl fragmentTransitionImpl = null;
        for (TransitionInfo transitionInfo : list) {
            if (!transitionInfo.isVisibilityUnchanged()) {
                FragmentTransitionImpl handlingImpl = transitionInfo.getHandlingImpl();
                if (fragmentTransitionImpl == null) {
                    fragmentTransitionImpl = handlingImpl;
                } else if (handlingImpl != null && fragmentTransitionImpl != handlingImpl) {
                    throw new IllegalArgumentException("Mixing framework transitions and AndroidX transitions is not allowed. Fragment " + transitionInfo.getOperation().getFragment() + " returned Transition " + transitionInfo.getTransition() + " which uses a different Transition  type than other Fragments.");
                }
            }
        }
        int i4 = 0;
        if (fragmentTransitionImpl == null) {
            for (TransitionInfo transitionInfo2 : list) {
                hashMap3.put(transitionInfo2.getOperation(), false);
                transitionInfo2.completeSpecialEffect();
            }
            return hashMap3;
        }
        View view6 = new View(getContainer().getContext());
        Rect rect3 = new Rect();
        ArrayList<View> arrayList6 = new ArrayList<>();
        ArrayList<View> arrayList7 = new ArrayList<>();
        ArrayMap arrayMap2 = new ArrayMap();
        Iterator<TransitionInfo> it = list.iterator();
        boolean z3 = false;
        Object obj4 = null;
        View view7 = null;
        ArrayMap arrayMap3 = arrayMap2;
        while (true) {
            boolean hasNext = it.hasNext();
            view = view7;
            str = FragmentManager.TAG;
            if (!hasNext) {
                break;
            }
            TransitionInfo next = it.next();
            if (!next.hasSharedElementTransition() || operation4 == null || operation5 == null) {
                i = i4;
                view4 = view6;
                rect = rect3;
                arrayMap = arrayMap3;
                arrayList4 = arrayList7;
                hashMap2 = hashMap3;
                arrayList5 = arrayList6;
            } else {
                Object wrapTransitionInSet = fragmentTransitionImpl.wrapTransitionInSet(fragmentTransitionImpl.cloneTransition(next.getSharedElementTransition()));
                ArrayList<String> sharedElementSourceNames = operation5.getFragment().getSharedElementSourceNames();
                ArrayList<String> sharedElementSourceNames2 = operation4.getFragment().getSharedElementSourceNames();
                ArrayList<String> sharedElementTargetNames = operation4.getFragment().getSharedElementTargetNames();
                HashMap hashMap4 = hashMap3;
                int i5 = 0;
                while (i5 < sharedElementTargetNames.size()) {
                    int indexOf = sharedElementSourceNames.indexOf(sharedElementTargetNames.get(i5));
                    ArrayList<String> arrayList8 = sharedElementTargetNames;
                    if (indexOf != -1) {
                        sharedElementSourceNames.set(indexOf, sharedElementSourceNames2.get(i5));
                    }
                    i5++;
                    sharedElementTargetNames = arrayList8;
                }
                ArrayList<String> sharedElementTargetNames2 = operation5.getFragment().getSharedElementTargetNames();
                if (!z2) {
                    sharedElementCallback2 = operation4.getFragment().getExitTransitionCallback();
                    sharedElementCallback = operation5.getFragment().getEnterTransitionCallback();
                } else {
                    sharedElementCallback2 = operation4.getFragment().getEnterTransitionCallback();
                    sharedElementCallback = operation5.getFragment().getExitTransitionCallback();
                }
                View view8 = view6;
                int i6 = 0;
                for (int size = sharedElementSourceNames.size(); i6 < size; size = size) {
                    arrayMap3.put(sharedElementSourceNames.get(i6), sharedElementTargetNames2.get(i6));
                    i6++;
                }
                if (FragmentManager.isLoggingEnabled(2)) {
                    Log.v(FragmentManager.TAG, ">>> entering view names <<<");
                    Iterator<String> it2 = sharedElementTargetNames2.iterator();
                    while (true) {
                        Iterator<String> it3 = it2;
                        if (!it2.hasNext()) {
                            break;
                        }
                        Log.v(FragmentManager.TAG, "Name: " + it3.next());
                        it2 = it3;
                        rect3 = rect3;
                    }
                    rect2 = rect3;
                    Log.v(FragmentManager.TAG, ">>> exiting view names <<<");
                    for (Iterator<String> it4 = sharedElementSourceNames.iterator(); it4.hasNext(); it4 = it4) {
                        Log.v(FragmentManager.TAG, "Name: " + it4.next());
                    }
                } else {
                    rect2 = rect3;
                }
                ArrayMap<String, View> arrayMap4 = new ArrayMap<>();
                findNamedViews(arrayMap4, operation4.getFragment().mView);
                arrayMap4.retainAll(sharedElementSourceNames);
                if (sharedElementCallback2 != null) {
                    if (FragmentManager.isLoggingEnabled(2)) {
                        Log.v(FragmentManager.TAG, "Executing exit callback for operation " + operation4);
                    }
                    sharedElementCallback2.onMapSharedElements(sharedElementSourceNames, arrayMap4);
                    int size2 = sharedElementSourceNames.size() - 1;
                    while (size2 >= 0) {
                        String str4 = sharedElementSourceNames.get(size2);
                        View view9 = arrayMap4.get(str4);
                        if (view9 == null) {
                            arrayMap3.remove(str4);
                            i3 = size2;
                        } else {
                            i3 = size2;
                            if (!str4.equals(ViewCompat.getTransitionName(view9))) {
                                arrayMap3.put(ViewCompat.getTransitionName(view9), (String) arrayMap3.remove(str4));
                            }
                        }
                        size2 = i3 - 1;
                    }
                } else {
                    arrayMap3.retainAll(arrayMap4.keySet());
                }
                final ArrayMap<String, View> arrayMap5 = new ArrayMap<>();
                findNamedViews(arrayMap5, operation5.getFragment().mView);
                arrayMap5.retainAll(sharedElementTargetNames2);
                arrayMap5.retainAll(arrayMap3.values());
                if (sharedElementCallback != null) {
                    if (FragmentManager.isLoggingEnabled(2)) {
                        Log.v(FragmentManager.TAG, "Executing enter callback for operation " + operation5);
                    }
                    sharedElementCallback.onMapSharedElements(sharedElementTargetNames2, arrayMap5);
                    for (int size3 = sharedElementTargetNames2.size() - 1; size3 >= 0; size3--) {
                        String str5 = sharedElementTargetNames2.get(size3);
                        View view10 = arrayMap5.get(str5);
                        if (view10 == null) {
                            String findKeyForValue2 = FragmentTransition.findKeyForValue(arrayMap3, str5);
                            if (findKeyForValue2 != null) {
                                arrayMap3.remove(findKeyForValue2);
                            }
                        } else if (!str5.equals(ViewCompat.getTransitionName(view10)) && (findKeyForValue = FragmentTransition.findKeyForValue(arrayMap3, str5)) != null) {
                            arrayMap3.put(findKeyForValue, ViewCompat.getTransitionName(view10));
                        }
                    }
                } else {
                    FragmentTransition.retainValues(arrayMap3, arrayMap5);
                }
                retainMatchingViews(arrayMap4, arrayMap3.keySet());
                retainMatchingViews(arrayMap5, arrayMap3.values());
                if (arrayMap3.isEmpty()) {
                    arrayList6.clear();
                    arrayList7.clear();
                    arrayList5 = arrayList6;
                    arrayMap = arrayMap3;
                    arrayList4 = arrayList7;
                    view7 = view;
                    hashMap2 = hashMap4;
                    view4 = view8;
                    rect = rect2;
                    obj4 = null;
                    i = 0;
                    view6 = view4;
                    arrayList6 = arrayList5;
                    arrayList7 = arrayList4;
                    hashMap3 = hashMap2;
                    arrayMap3 = arrayMap;
                    i4 = i;
                    rect3 = rect;
                    z2 = z;
                } else {
                    FragmentTransition.callSharedElementStartEnd(operation5.getFragment(), operation4.getFragment(), z2, arrayMap4, true);
                    OneShotPreDrawListener.add(getContainer(), new Runnable() { // from class: androidx.fragment.app.DefaultSpecialEffectsController.6
                        @Override // java.lang.Runnable
                        public void run() {
                            FragmentTransition.callSharedElementStartEnd(operation5.getFragment(), operation4.getFragment(), z2, arrayMap5, false);
                        }
                    });
                    arrayList6.addAll(arrayMap4.values());
                    if (sharedElementSourceNames.isEmpty()) {
                        i2 = 0;
                    } else {
                        i2 = 0;
                        View view11 = arrayMap4.get(sharedElementSourceNames.get(0));
                        fragmentTransitionImpl.setEpicenter(wrapTransitionInSet, view11);
                        view = view11;
                    }
                    arrayList7.addAll(arrayMap5.values());
                    if (sharedElementTargetNames2.isEmpty() || (view5 = arrayMap5.get(sharedElementTargetNames2.get(i2))) == null) {
                        rect = rect2;
                    } else {
                        rect = rect2;
                        OneShotPreDrawListener.add(getContainer(), new Runnable() { // from class: androidx.fragment.app.DefaultSpecialEffectsController.7
                            @Override // java.lang.Runnable
                            public void run() {
                                fragmentTransitionImpl.getBoundsOnScreen(view5, rect);
                            }
                        });
                        z3 = true;
                    }
                    fragmentTransitionImpl.setSharedElementTargets(wrapTransitionInSet, view8, arrayList6);
                    arrayMap = arrayMap3;
                    arrayList5 = arrayList6;
                    view4 = view8;
                    i = i2;
                    fragmentTransitionImpl.scheduleRemoveTargets(wrapTransitionInSet, null, null, null, null, wrapTransitionInSet, arrayList7);
                    arrayList4 = arrayList7;
                    hashMap2 = hashMap4;
                    hashMap2.put(operation4, true);
                    hashMap2.put(operation5, true);
                    obj4 = wrapTransitionInSet;
                }
            }
            view7 = view;
            view6 = view4;
            arrayList6 = arrayList5;
            arrayList7 = arrayList4;
            hashMap3 = hashMap2;
            arrayMap3 = arrayMap;
            i4 = i;
            rect3 = rect;
            z2 = z;
        }
        int i7 = i4;
        View view12 = view6;
        Rect rect4 = rect3;
        ArrayMap arrayMap6 = arrayMap3;
        ArrayList<View> arrayList9 = arrayList7;
        HashMap hashMap5 = hashMap3;
        ArrayList<View> arrayList10 = arrayList6;
        ArrayList arrayList11 = new ArrayList();
        Object obj5 = null;
        Object obj6 = null;
        for (TransitionInfo transitionInfo3 : list) {
            if (transitionInfo3.isVisibilityUnchanged()) {
                boolean z4 = i7;
                hashMap5.put(transitionInfo3.getOperation(), Boolean.valueOf(z4));
                transitionInfo3.completeSpecialEffect();
                i7 = z4 ? 1 : 0;
            } else {
                boolean z5 = i7;
                Object cloneTransition = fragmentTransitionImpl.cloneTransition(transitionInfo3.getTransition());
                SpecialEffectsController.Operation operation6 = transitionInfo3.getOperation();
                boolean z6 = (obj4 == null || !(operation6 == operation4 || operation6 == operation5)) ? z5 ? 1 : 0 : true;
                if (cloneTransition == null) {
                    if (!z6) {
                        hashMap5.put(operation6, Boolean.valueOf(z5));
                        transitionInfo3.completeSpecialEffect();
                    }
                    view3 = view12;
                    arrayList = arrayList9;
                    hashMap = hashMap5;
                    str3 = str;
                    view2 = view;
                    arrayList2 = arrayList10;
                    arrayList3 = arrayList11;
                } else {
                    HashMap hashMap6 = hashMap5;
                    final ArrayList<View> arrayList12 = new ArrayList<>();
                    ArrayList arrayList13 = arrayList11;
                    captureTransitioningViews(arrayList12, operation6.getFragment().mView);
                    if (z6) {
                        if (operation6 == operation4) {
                            arrayList12.removeAll(arrayList10);
                        } else {
                            arrayList12.removeAll(arrayList9);
                        }
                    }
                    if (arrayList12.isEmpty()) {
                        fragmentTransitionImpl.addTarget(cloneTransition, view12);
                        ArrayList<View> arrayList14 = arrayList9;
                        obj = cloneTransition;
                        hashMap = hashMap6;
                        arrayList = arrayList14;
                        view3 = view12;
                        obj3 = obj5;
                        obj2 = obj6;
                        str3 = str;
                        operation3 = operation6;
                        view2 = view;
                        arrayList2 = arrayList10;
                        arrayList3 = arrayList13;
                    } else {
                        fragmentTransitionImpl.addTargets(cloneTransition, arrayList12);
                        ArrayList<View> arrayList15 = arrayList9;
                        obj = cloneTransition;
                        hashMap = hashMap6;
                        arrayList = arrayList15;
                        view2 = view;
                        obj2 = obj6;
                        str3 = str;
                        arrayList2 = arrayList10;
                        arrayList3 = arrayList13;
                        view3 = view12;
                        obj3 = obj5;
                        fragmentTransitionImpl.scheduleRemoveTargets(obj, cloneTransition, arrayList12, null, null, null, null);
                        if (operation6.getFinalState() == SpecialEffectsController.Operation.State.GONE) {
                            operation3 = operation6;
                            list2.remove(operation3);
                            ArrayList<View> arrayList16 = new ArrayList<>(arrayList12);
                            arrayList16.remove(operation3.getFragment().mView);
                            fragmentTransitionImpl.scheduleHideFragmentView(obj, operation3.getFragment().mView, arrayList16);
                            OneShotPreDrawListener.add(getContainer(), new Runnable() { // from class: androidx.fragment.app.DefaultSpecialEffectsController.8
                                @Override // java.lang.Runnable
                                public void run() {
                                    FragmentTransition.setViewVisibility(arrayList12, 4);
                                }
                            });
                        } else {
                            operation3 = operation6;
                        }
                    }
                    if (operation3.getFinalState() == SpecialEffectsController.Operation.State.VISIBLE) {
                        arrayList3.addAll(arrayList12);
                        if (z3) {
                            fragmentTransitionImpl.setEpicenter(obj, rect4);
                        }
                    } else {
                        fragmentTransitionImpl.setEpicenter(obj, view2);
                    }
                    hashMap.put(operation3, true);
                    if (transitionInfo3.isOverlapAllowed()) {
                        obj3 = fragmentTransitionImpl.mergeTransitionsTogether(obj3, obj, null);
                    } else {
                        obj2 = fragmentTransitionImpl.mergeTransitionsTogether(obj2, obj, null);
                    }
                    obj6 = obj2;
                    obj5 = obj3;
                }
                operation5 = operation2;
                hashMap5 = hashMap;
                arrayList11 = arrayList3;
                arrayList10 = arrayList2;
                i7 = z5 ? 1 : 0;
                view12 = view3;
                arrayList9 = arrayList;
                str = str3;
                view = view2;
                operation4 = operation;
            }
        }
        int i8 = i7;
        ArrayList<View> arrayList17 = arrayList10;
        ArrayList<View> arrayList18 = arrayList9;
        ArrayList arrayList19 = arrayList11;
        HashMap hashMap7 = hashMap5;
        String str6 = str;
        Object mergeTransitionsInSequence = fragmentTransitionImpl.mergeTransitionsInSequence(obj5, obj6, obj4);
        if (mergeTransitionsInSequence == null) {
            return hashMap7;
        }
        for (final TransitionInfo transitionInfo4 : list) {
            if (!transitionInfo4.isVisibilityUnchanged()) {
                Object transition = transitionInfo4.getTransition();
                final SpecialEffectsController.Operation operation7 = transitionInfo4.getOperation();
                int i9 = (obj4 == null || !(operation7 == operation || operation7 == operation2)) ? i8 : 1;
                if (transition == null && i9 == 0) {
                    str2 = str6;
                } else if (!ViewCompat.isLaidOut(getContainer())) {
                    if (FragmentManager.isLoggingEnabled(2)) {
                        str2 = str6;
                        Log.v(str2, "SpecialEffectsController: Container " + getContainer() + " has not been laid out. Completing operation " + operation7);
                    } else {
                        str2 = str6;
                    }
                    transitionInfo4.completeSpecialEffect();
                } else {
                    str2 = str6;
                    fragmentTransitionImpl.setListenerForTransitionEnd(transitionInfo4.getOperation().getFragment(), mergeTransitionsInSequence, transitionInfo4.getSignal(), new Runnable() { // from class: androidx.fragment.app.DefaultSpecialEffectsController.9
                        @Override // java.lang.Runnable
                        public void run() {
                            transitionInfo4.completeSpecialEffect();
                            if (FragmentManager.isLoggingEnabled(2)) {
                                Log.v(FragmentManager.TAG, "Transition for operation " + operation7 + "has completed");
                            }
                        }
                    });
                }
                str6 = str2;
            }
        }
        String str7 = str6;
        if (!ViewCompat.isLaidOut(getContainer())) {
            return hashMap7;
        }
        FragmentTransition.setViewVisibility(arrayList19, 4);
        ArrayList<String> prepareSetNameOverridesReordered = fragmentTransitionImpl.prepareSetNameOverridesReordered(arrayList18);
        if (FragmentManager.isLoggingEnabled(2)) {
            Log.v(str7, ">>>>> Beginning transition <<<<<");
            Log.v(str7, ">>>>> SharedElementFirstOutViews <<<<<");
            Iterator<View> it5 = arrayList17.iterator();
            while (it5.hasNext()) {
                View next2 = it5.next();
                Log.v(str7, "View: " + next2 + " Name: " + ViewCompat.getTransitionName(next2));
            }
            Log.v(str7, ">>>>> SharedElementLastInViews <<<<<");
            Iterator<View> it6 = arrayList18.iterator();
            while (it6.hasNext()) {
                View next3 = it6.next();
                Log.v(str7, "View: " + next3 + " Name: " + ViewCompat.getTransitionName(next3));
            }
        }
        fragmentTransitionImpl.beginDelayedTransition(getContainer(), mergeTransitionsInSequence);
        fragmentTransitionImpl.setNameOverridesReordered(getContainer(), arrayList17, arrayList18, prepareSetNameOverridesReordered, arrayMap6);
        FragmentTransition.setViewVisibility(arrayList19, i8);
        fragmentTransitionImpl.swapSharedElementTargets(obj4, arrayList17, arrayList18);
        return hashMap7;
    }

    void retainMatchingViews(ArrayMap<String, View> arrayMap, Collection<String> collection) {
        Iterator<Map.Entry<String, View>> it = arrayMap.entrySet().iterator();
        while (it.hasNext()) {
            if (!collection.contains(ViewCompat.getTransitionName(it.next().getValue()))) {
                it.remove();
            }
        }
    }

    void captureTransitioningViews(ArrayList<View> arrayList, View view) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            if (ViewGroupCompat.isTransitionGroup(viewGroup)) {
                if (arrayList.contains(view)) {
                    return;
                }
                arrayList.add(viewGroup);
                return;
            }
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = viewGroup.getChildAt(i);
                if (childAt.getVisibility() == 0) {
                    captureTransitioningViews(arrayList, childAt);
                }
            }
            return;
        }
        if (arrayList.contains(view)) {
            return;
        }
        arrayList.add(view);
    }

    void findNamedViews(Map<String, View> map, View view) {
        String transitionName = ViewCompat.getTransitionName(view);
        if (transitionName != null) {
            map.put(transitionName, view);
        }
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = viewGroup.getChildAt(i);
                if (childAt.getVisibility() == 0) {
                    findNamedViews(map, childAt);
                }
            }
        }
    }

    void applyContainerChanges(SpecialEffectsController.Operation operation) {
        operation.getFinalState().applyState(operation.getFragment().mView);
    }

    private static class SpecialEffectsInfo {
        private final SpecialEffectsController.Operation mOperation;
        private final CancellationSignal mSignal;

        SpecialEffectsInfo(SpecialEffectsController.Operation operation, CancellationSignal cancellationSignal) {
            this.mOperation = operation;
            this.mSignal = cancellationSignal;
        }

        SpecialEffectsController.Operation getOperation() {
            return this.mOperation;
        }

        CancellationSignal getSignal() {
            return this.mSignal;
        }

        boolean isVisibilityUnchanged() {
            SpecialEffectsController.Operation.State from = SpecialEffectsController.Operation.State.from(this.mOperation.getFragment().mView);
            SpecialEffectsController.Operation.State finalState = this.mOperation.getFinalState();
            if (from != finalState) {
                return (from == SpecialEffectsController.Operation.State.VISIBLE || finalState == SpecialEffectsController.Operation.State.VISIBLE) ? false : true;
            }
            return true;
        }

        void completeSpecialEffect() {
            this.mOperation.completeSpecialEffect(this.mSignal);
        }
    }

    private static class AnimationInfo extends SpecialEffectsInfo {
        private FragmentAnim.AnimationOrAnimator mAnimation;
        private boolean mIsPop;
        private boolean mLoadedAnim;

        AnimationInfo(SpecialEffectsController.Operation operation, CancellationSignal cancellationSignal, boolean z) {
            super(operation, cancellationSignal);
            this.mLoadedAnim = false;
            this.mIsPop = z;
        }

        FragmentAnim.AnimationOrAnimator getAnimation(Context context) {
            if (this.mLoadedAnim) {
                return this.mAnimation;
            }
            FragmentAnim.AnimationOrAnimator loadAnimation = FragmentAnim.loadAnimation(context, getOperation().getFragment(), getOperation().getFinalState() == SpecialEffectsController.Operation.State.VISIBLE, this.mIsPop);
            this.mAnimation = loadAnimation;
            this.mLoadedAnim = true;
            return loadAnimation;
        }
    }

    private static class TransitionInfo extends SpecialEffectsInfo {
        private final boolean mOverlapAllowed;
        private final Object mSharedElementTransition;
        private final Object mTransition;

        TransitionInfo(SpecialEffectsController.Operation operation, CancellationSignal cancellationSignal, boolean z, boolean z2) {
            super(operation, cancellationSignal);
            Object exitTransition;
            Object enterTransition;
            boolean allowEnterTransitionOverlap;
            if (operation.getFinalState() == SpecialEffectsController.Operation.State.VISIBLE) {
                if (z) {
                    enterTransition = operation.getFragment().getReenterTransition();
                } else {
                    enterTransition = operation.getFragment().getEnterTransition();
                }
                this.mTransition = enterTransition;
                if (z) {
                    allowEnterTransitionOverlap = operation.getFragment().getAllowReturnTransitionOverlap();
                } else {
                    allowEnterTransitionOverlap = operation.getFragment().getAllowEnterTransitionOverlap();
                }
                this.mOverlapAllowed = allowEnterTransitionOverlap;
            } else {
                if (z) {
                    exitTransition = operation.getFragment().getReturnTransition();
                } else {
                    exitTransition = operation.getFragment().getExitTransition();
                }
                this.mTransition = exitTransition;
                this.mOverlapAllowed = true;
            }
            if (!z2) {
                this.mSharedElementTransition = null;
            } else if (z) {
                this.mSharedElementTransition = operation.getFragment().getSharedElementReturnTransition();
            } else {
                this.mSharedElementTransition = operation.getFragment().getSharedElementEnterTransition();
            }
        }

        Object getTransition() {
            return this.mTransition;
        }

        boolean isOverlapAllowed() {
            return this.mOverlapAllowed;
        }

        public boolean hasSharedElementTransition() {
            return this.mSharedElementTransition != null;
        }

        public Object getSharedElementTransition() {
            return this.mSharedElementTransition;
        }

        FragmentTransitionImpl getHandlingImpl() {
            FragmentTransitionImpl handlingImpl = getHandlingImpl(this.mTransition);
            FragmentTransitionImpl handlingImpl2 = getHandlingImpl(this.mSharedElementTransition);
            if (handlingImpl == null || handlingImpl2 == null || handlingImpl == handlingImpl2) {
                return handlingImpl != null ? handlingImpl : handlingImpl2;
            }
            throw new IllegalArgumentException("Mixing framework transitions and AndroidX transitions is not allowed. Fragment " + getOperation().getFragment() + " returned Transition " + this.mTransition + " which uses a different Transition  type than its shared element transition " + this.mSharedElementTransition);
        }

        private FragmentTransitionImpl getHandlingImpl(Object obj) {
            if (obj == null) {
                return null;
            }
            if (FragmentTransition.PLATFORM_IMPL != null && FragmentTransition.PLATFORM_IMPL.canHandle(obj)) {
                return FragmentTransition.PLATFORM_IMPL;
            }
            if (FragmentTransition.SUPPORT_IMPL != null && FragmentTransition.SUPPORT_IMPL.canHandle(obj)) {
                return FragmentTransition.SUPPORT_IMPL;
            }
            throw new IllegalArgumentException("Transition " + obj + " for fragment " + getOperation().getFragment() + " is not a valid framework Transition or AndroidX Transition");
        }
    }
}
