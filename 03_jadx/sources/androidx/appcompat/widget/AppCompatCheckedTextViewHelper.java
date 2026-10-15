package androidx.appcompat.widget;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.CheckedTextView;
import androidx.appcompat.R;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.CheckedTextViewCompat;

/* loaded from: classes.dex */
class AppCompatCheckedTextViewHelper {
    private ColorStateList mCheckMarkTintList = null;
    private PorterDuff.Mode mCheckMarkTintMode = null;
    private boolean mHasCheckMarkTint = false;
    private boolean mHasCheckMarkTintMode = false;
    private boolean mSkipNextApply;
    private final CheckedTextView mView;

    AppCompatCheckedTextViewHelper(CheckedTextView checkedTextView) {
        this.mView = checkedTextView;
    }

    /* JADX WARN: Removed duplicated region for block: B:11:0x0062 A[Catch: all -> 0x008a, TryCatch #1 {all -> 0x008a, blocks: (B:3:0x001f, B:5:0x0027, B:8:0x002f, B:9:0x005a, B:11:0x0062, B:12:0x006d, B:14:0x0075, B:21:0x003d, B:23:0x0045, B:25:0x004d), top: B:2:0x001f }] */
    /* JADX WARN: Removed duplicated region for block: B:14:0x0075 A[Catch: all -> 0x008a, TRY_LEAVE, TryCatch #1 {all -> 0x008a, blocks: (B:3:0x001f, B:5:0x0027, B:8:0x002f, B:9:0x005a, B:11:0x0062, B:12:0x006d, B:14:0x0075, B:21:0x003d, B:23:0x0045, B:25:0x004d), top: B:2:0x001f }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    void loadFromAttributes(AttributeSet attributeSet, int i) {
        int resourceId;
        int resourceId2;
        TintTypedArray obtainStyledAttributes = TintTypedArray.obtainStyledAttributes(this.mView.getContext(), attributeSet, R.styleable.CheckedTextView, i, 0);
        CheckedTextView checkedTextView = this.mView;
        ViewCompat.saveAttributeDataForStyleable(checkedTextView, checkedTextView.getContext(), R.styleable.CheckedTextView, attributeSet, obtainStyledAttributes.getWrappedTypeArray(), i, 0);
        try {
            if (obtainStyledAttributes.hasValue(R.styleable.CheckedTextView_checkMarkCompat) && (resourceId2 = obtainStyledAttributes.getResourceId(R.styleable.CheckedTextView_checkMarkCompat, 0)) != 0) {
                try {
                    CheckedTextView checkedTextView2 = this.mView;
                    checkedTextView2.setCheckMarkDrawable(AppCompatResources.getDrawable(checkedTextView2.getContext(), resourceId2));
                } catch (Resources.NotFoundException unused) {
                }
                if (obtainStyledAttributes.hasValue(R.styleable.CheckedTextView_checkMarkTint)) {
                    CheckedTextViewCompat.setCheckMarkTintList(this.mView, obtainStyledAttributes.getColorStateList(R.styleable.CheckedTextView_checkMarkTint));
                }
                if (obtainStyledAttributes.hasValue(R.styleable.CheckedTextView_checkMarkTintMode)) {
                    CheckedTextViewCompat.setCheckMarkTintMode(this.mView, DrawableUtils.parseTintMode(obtainStyledAttributes.getInt(R.styleable.CheckedTextView_checkMarkTintMode, -1), null));
                }
            }
            if (obtainStyledAttributes.hasValue(R.styleable.CheckedTextView_android_checkMark) && (resourceId = obtainStyledAttributes.getResourceId(R.styleable.CheckedTextView_android_checkMark, 0)) != 0) {
                CheckedTextView checkedTextView3 = this.mView;
                checkedTextView3.setCheckMarkDrawable(AppCompatResources.getDrawable(checkedTextView3.getContext(), resourceId));
            }
            if (obtainStyledAttributes.hasValue(R.styleable.CheckedTextView_checkMarkTint)) {
            }
            if (obtainStyledAttributes.hasValue(R.styleable.CheckedTextView_checkMarkTintMode)) {
            }
        } finally {
            obtainStyledAttributes.recycle();
        }
    }

    void setSupportCheckMarkTintList(ColorStateList colorStateList) {
        this.mCheckMarkTintList = colorStateList;
        this.mHasCheckMarkTint = true;
        applyCheckMarkTint();
    }

    ColorStateList getSupportCheckMarkTintList() {
        return this.mCheckMarkTintList;
    }

    void setSupportCheckMarkTintMode(PorterDuff.Mode mode) {
        this.mCheckMarkTintMode = mode;
        this.mHasCheckMarkTintMode = true;
        applyCheckMarkTint();
    }

    PorterDuff.Mode getSupportCheckMarkTintMode() {
        return this.mCheckMarkTintMode;
    }

    void onSetCheckMarkDrawable() {
        if (this.mSkipNextApply) {
            this.mSkipNextApply = false;
        } else {
            this.mSkipNextApply = true;
            applyCheckMarkTint();
        }
    }

    void applyCheckMarkTint() {
        Drawable checkMarkDrawable = CheckedTextViewCompat.getCheckMarkDrawable(this.mView);
        if (checkMarkDrawable != null) {
            if (this.mHasCheckMarkTint || this.mHasCheckMarkTintMode) {
                Drawable mutate = DrawableCompat.wrap(checkMarkDrawable).mutate();
                if (this.mHasCheckMarkTint) {
                    DrawableCompat.setTintList(mutate, this.mCheckMarkTintList);
                }
                if (this.mHasCheckMarkTintMode) {
                    DrawableCompat.setTintMode(mutate, this.mCheckMarkTintMode);
                }
                if (mutate.isStateful()) {
                    mutate.setState(this.mView.getDrawableState());
                }
                this.mView.setCheckMarkDrawable(mutate);
            }
        }
    }
}
