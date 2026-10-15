package com.listpro.xyon.pro.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.listpro.xyon.pro.R;

/* loaded from: classes.dex */
public final class ActivityMainBinding implements ViewBinding {
    private final ConstraintLayout rootView;
    public final WebView webView;

    private ActivityMainBinding(ConstraintLayout constraintLayout, WebView webView) {
        this.rootView = constraintLayout;
        this.webView = webView;
    }

    @Override // androidx.viewbinding.ViewBinding
    public ConstraintLayout getRoot() {
        return this.rootView;
    }

    public static ActivityMainBinding inflate(LayoutInflater layoutInflater) {
        return inflate(layoutInflater, null, false);
    }

    public static ActivityMainBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z) {
        View inflate = layoutInflater.inflate(R.layout.activity_main, viewGroup, false);
        if (z) {
            viewGroup.addView(inflate);
        }
        return bind(inflate);
    }

    public static ActivityMainBinding bind(View view) {
        int i = R.id.webView;
        WebView webView = (WebView) ViewBindings.findChildViewById(view, i);
        if (webView != null) {
            return new ActivityMainBinding((ConstraintLayout) view, webView);
        }
        throw new NullPointerException("Missing required view with ID: ".concat(view.getResources().getResourceName(i)));
    }
}
