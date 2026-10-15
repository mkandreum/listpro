package com.listpro.xyon.pro;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.core.motion.utils.TypedValues;
import androidx.constraintlayout.widget.ConstraintLayout;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.io.CloseableKt;
import kotlin.io.TextStreamsKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.RangesKt;
import kotlin.text.Charsets;
import kotlin.text.Regex;
import kotlin.text.StringsKt;

/* compiled from: MainActivity.kt */
@Metadata(d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0007\u0018\u00002\u00020\u0001:\u0001\u001fB\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u0012\u0010\r\u001a\u00020\u000e2\b\u0010\u000f\u001a\u0004\u0018\u00010\u0010H\u0014J\b\u0010\u0011\u001a\u00020\u000eH\u0002J\"\u0010\u0012\u001a\u00020\u000e2\u0006\u0010\u0013\u001a\u00020\u000b2\u0006\u0010\u0014\u001a\u00020\u000b2\b\u0010\u0015\u001a\u0004\u0018\u00010\u0016H\u0014J\u0018\u0010\u0017\u001a\u00020\u000e2\u0006\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\bH\u0002J\u0010\u0010\u001b\u001a\u00020\b2\u0006\u0010\u0018\u001a\u00020\u0019H\u0002J$\u0010\u001c\u001a\u00020\b2\f\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\b0\u00072\f\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\b0\u0007H\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082.¢\u0006\u0002\n\u0000R\u0014\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u0007X\u0082\u000e¢\u0006\u0002\n\u0000R\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\b0\u0007X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082D¢\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u000bX\u0082D¢\u0006\u0002\n\u0000¨\u0006 "}, d2 = {"Lcom/listpro/xyon/pro/MainActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "<init>", "()V", "webView", "Landroid/webkit/WebView;", "productLines", "", "", "csvHeaders", "CREATE_FILE_REQUEST_CODE", "", "PICK_FILE_REQUEST_CODE", "onCreate", "", "savedInstanceState", "Landroid/os/Bundle;", "loadCsvDataToMemory", "onActivityResult", "requestCode", "resultCode", "data", "Landroid/content/Intent;", "writeTextToUri", "uri", "Landroid/net/Uri;", "text", "readTextFromUri", "convertChunkToJson", "chunk", "headers", "WebAppInterface", "app_release"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes.dex */
public final class MainActivity extends AppCompatActivity {
    private WebView webView;
    private List<String> productLines = CollectionsKt.emptyList();
    private List<String> csvHeaders = CollectionsKt.emptyList();
    private final int CREATE_FILE_REQUEST_CODE = 1;
    private final int PICK_FILE_REQUEST_CODE = 2;

    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WebView webView = (WebView) findViewById(R.id.webView);
        this.webView = webView;
        WebView webView2 = null;
        if (webView == null) {
            Intrinsics.throwUninitializedPropertyAccessException("webView");
            webView = null;
        }
        webView.getSettings().setJavaScriptEnabled(true);
        WebView.setWebContentsDebuggingEnabled(true);
        WebView webView3 = this.webView;
        if (webView3 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("webView");
            webView3 = null;
        }
        webView3.setVerticalScrollBarEnabled(false);
        WebView webView4 = this.webView;
        if (webView4 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("webView");
            webView4 = null;
        }
        webView4.setHorizontalScrollBarEnabled(false);
        WebView webView5 = this.webView;
        if (webView5 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("webView");
            webView5 = null;
        }
        webView5.addJavascriptInterface(new WebAppInterface(), "Android");
        WebView webView6 = this.webView;
        if (webView6 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("webView");
            webView6 = null;
        }
        webView6.setWebViewClient(new WebViewClient() { // from class: com.listpro.xyon.pro.MainActivity$onCreate$1
            @Override // android.webkit.WebViewClient
            public void onPageFinished(WebView view, String url) {
                WebView webView7;
                super.onPageFinished(view, url);
                webView7 = MainActivity.this.webView;
                if (webView7 == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("webView");
                    webView7 = null;
                }
                webView7.evaluateJavascript("javascript:onProductsReady();", null);
            }
        });
        loadCsvDataToMemory();
        WebView webView7 = this.webView;
        if (webView7 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("webView");
        } else {
            webView2 = webView7;
        }
        webView2.loadUrl("file:///android_asset/index.html");
    }

    private final void loadCsvDataToMemory() {
        new Thread(new Runnable() { // from class: com.listpro.xyon.pro.MainActivity$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.loadCsvDataToMemory$lambda$3(MainActivity.this);
            }
        }).start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void loadCsvDataToMemory$lambda$3(MainActivity mainActivity) {
        try {
            BufferedReader open = mainActivity.getAssets().open("products_macro.csv");
            try {
                open = new BufferedReader(new InputStreamReader(open, Charset.forName("UTF-8")));
                try {
                    List<String> readLines = TextStreamsKt.readLines(open);
                    if (readLines.size() > 1) {
                        List split$default = StringsKt.split$default((CharSequence) readLines.get(0), new char[]{','}, false, 0, 6, (Object) null);
                        ArrayList arrayList = new ArrayList(CollectionsKt.collectionSizeOrDefault(split$default, 10));
                        Iterator it = split$default.iterator();
                        while (it.hasNext()) {
                            arrayList.add(StringsKt.trim((CharSequence) it.next()).toString());
                        }
                        mainActivity.csvHeaders = arrayList;
                        mainActivity.productLines = CollectionsKt.drop(readLines, 1);
                    }
                    Unit unit = Unit.INSTANCE;
                    CloseableKt.closeFinally(open, null);
                    Unit unit2 = Unit.INSTANCE;
                    CloseableKt.closeFinally(open, null);
                } finally {
                }
            } finally {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* compiled from: MainActivity.kt */
    @Metadata(d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0003\b\u0086\u0004\u0018\u00002\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\b\u0010\u0004\u001a\u00020\u0005H\u0007J\u0018\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\u00052\u0006\u0010\t\u001a\u00020\u0005H\u0007J\u0010\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u0007H\u0007J\b\u0010\r\u001a\u00020\u000bH\u0007¨\u0006\u000e"}, d2 = {"Lcom/listpro/xyon/pro/MainActivity$WebAppInterface;", "", "<init>", "(Lcom/listpro/xyon/pro/MainActivity;)V", "getProductCount", "", "getProductChunk", "", TypedValues.CycleType.S_WAVE_OFFSET, "limit", "saveBackup", "", "jsonData", "requestBackupFile", "app_release"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    public final class WebAppInterface {
        public WebAppInterface() {
        }

        @JavascriptInterface
        public final int getProductCount() {
            return MainActivity.this.productLines.size();
        }

        @JavascriptInterface
        public final String getProductChunk(int offset, int limit) {
            if (offset >= MainActivity.this.productLines.size()) {
                return "[]";
            }
            List subList = MainActivity.this.productLines.subList(offset, RangesKt.coerceAtMost(limit + offset, MainActivity.this.productLines.size()));
            MainActivity mainActivity = MainActivity.this;
            return mainActivity.convertChunkToJson(subList, mainActivity.csvHeaders);
        }

        @JavascriptInterface
        public final void saveBackup(String jsonData) {
            Intrinsics.checkNotNullParameter(jsonData, "jsonData");
            Intent intent = new Intent("android.intent.action.CREATE_DOCUMENT");
            intent.addCategory("android.intent.category.OPENABLE");
            intent.setType("application/json");
            intent.putExtra("android.intent.extra.TITLE", "listpro_backup.json");
            MainActivity mainActivity = MainActivity.this;
            mainActivity.startActivityForResult(intent, mainActivity.CREATE_FILE_REQUEST_CODE);
            MainActivity.this.getSharedPreferences("backup", 0).edit().putString("data", jsonData).apply();
        }

        @JavascriptInterface
        public final void requestBackupFile() {
            Intent intent = new Intent("android.intent.action.OPEN_DOCUMENT");
            intent.addCategory("android.intent.category.OPENABLE");
            intent.setType("application/json");
            MainActivity mainActivity = MainActivity.this;
            mainActivity.startActivityForResult(intent, mainActivity.PICK_FILE_REQUEST_CODE);
        }
    }

    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, android.app.Activity
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Uri data2;
        Uri data3;
        String string;
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1) {
            if (requestCode == this.CREATE_FILE_REQUEST_CODE) {
                if (data == null || (data3 = data.getData()) == null || (string = getSharedPreferences("backup", 0).getString("data", null)) == null) {
                    return;
                }
                writeTextToUri(data3, string);
                return;
            }
            if (requestCode != this.PICK_FILE_REQUEST_CODE || data == null || (data2 = data.getData()) == null) {
                return;
            }
            String replace$default = StringsKt.replace$default(readTextFromUri(data2), "'", "\\'", false, 4, (Object) null);
            WebView webView = this.webView;
            if (webView == null) {
                Intrinsics.throwUninitializedPropertyAccessException("webView");
                webView = null;
            }
            webView.evaluateJavascript("javascript:onBackupFileLoaded('" + replace$default + "');", null);
        }
    }

    private final void writeTextToUri(Uri uri, String text) {
        try {
            OutputStream openOutputStream = getContentResolver().openOutputStream(uri);
            if (openOutputStream == null) {
                return;
            }
            OutputStream outputStream = openOutputStream;
            try {
                byte[] bytes = text.getBytes(Charsets.UTF_8);
                Intrinsics.checkNotNullExpressionValue(bytes, "getBytes(...)");
                outputStream.write(bytes);
                Unit unit = Unit.INSTANCE;
                CloseableKt.closeFinally(outputStream, null);
            } finally {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final String readTextFromUri(Uri uri) {
        StringBuilder sb = new StringBuilder();
        try {
            InputStream openInputStream = getContentResolver().openInputStream(uri);
            if (openInputStream != null) {
                BufferedReader bufferedReader = openInputStream;
                try {
                    bufferedReader = new BufferedReader(new InputStreamReader(bufferedReader));
                    try {
                        BufferedReader bufferedReader2 = bufferedReader;
                        while (true) {
                            String readLine = bufferedReader2.readLine();
                            if (readLine == null) {
                                break;
                            }
                            sb.append(readLine);
                        }
                        Unit unit = Unit.INSTANCE;
                        CloseableKt.closeFinally(bufferedReader, null);
                        Unit unit2 = Unit.INSTANCE;
                        CloseableKt.closeFinally(bufferedReader, null);
                    } finally {
                    }
                } finally {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String sb2 = sb.toString();
        Intrinsics.checkNotNullExpressionValue(sb2, "toString(...)");
        return sb2;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final String convertChunkToJson(List<String> chunk, List<String> headers) {
        String str;
        String obj;
        Integer intOrNull;
        String str2;
        int i;
        String obj2;
        String obj3;
        Double doubleOrNull;
        String obj4;
        String obj5;
        String replace$default;
        String obj6;
        String replace$default2;
        List<String> list = headers;
        int indexOf = list.indexOf("id");
        int indexOf2 = list.indexOf("Category");
        int indexOf3 = list.indexOf("name");
        int indexOf4 = list.indexOf("subtitle");
        int indexOf5 = list.indexOf("price");
        int indexOf6 = list.indexOf("main_image_url");
        ArrayList arrayList = new ArrayList();
        Iterator<T> it = chunk.iterator();
        while (it.hasNext()) {
            List<String> split = new Regex(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)").split((String) it.next(), 0);
            String str3 = null;
            if (split.size() < list.size() || (str = (String) CollectionsKt.getOrNull(split, indexOf)) == null || (obj = StringsKt.trim((CharSequence) str).toString()) == null || (intOrNull = StringsKt.toIntOrNull(obj)) == null) {
                i = indexOf;
            } else {
                int intValue = intOrNull.intValue();
                String str4 = (String) CollectionsKt.getOrNull(split, indexOf2);
                String str5 = "";
                String str6 = (str4 == null || (obj6 = StringsKt.trim((CharSequence) str4).toString()) == null || (replace$default2 = StringsKt.replace$default(obj6, "\"", "", false, 4, (Object) null)) == null) ? "" : replace$default2;
                String str7 = (String) CollectionsKt.getOrNull(split, indexOf3);
                String str8 = (str7 == null || (obj5 = StringsKt.trim((CharSequence) str7).toString()) == null || (replace$default = StringsKt.replace$default(obj5, "\"", "", false, 4, (Object) null)) == null) ? "" : replace$default;
                String str9 = (String) CollectionsKt.getOrNull(split, indexOf4);
                if (str9 == null || (obj4 = StringsKt.trim((CharSequence) str9).toString()) == null || (str2 = StringsKt.replace$default(obj4, "\"", "", false, 4, (Object) null)) == null) {
                    str2 = "";
                }
                String str10 = (String) CollectionsKt.getOrNull(split, indexOf5);
                i = indexOf;
                double doubleValue = (str10 == null || (obj3 = StringsKt.trim((CharSequence) str10).toString()) == null || (doubleOrNull = StringsKt.toDoubleOrNull(obj3)) == null) ? 0.0d : doubleOrNull.doubleValue();
                String str11 = (String) CollectionsKt.getOrNull(split, indexOf6);
                if (str11 != null && (obj2 = StringsKt.trim((CharSequence) str11).toString()) != null) {
                    str5 = obj2;
                }
                str3 = "{\"id\":" + intValue + ",\"category\":\"" + StringsKt.replace$default(StringsKt.replace$default(str6, "\\", "\\\\", false, 4, (Object) null), "\"", "\\\"", false, 4, (Object) null) + "\",\"name\":\"" + StringsKt.replace$default(StringsKt.replace$default(str8, "\\", "\\\\", false, 4, (Object) null), "\"", "\\\"", false, 4, (Object) null) + "\",\"brand\":\"" + StringsKt.replace$default(StringsKt.replace$default(str2, "\\", "\\\\", false, 4, (Object) null), "\"", "\\\"", false, 4, (Object) null) + "\",\"price\":" + doubleValue + ",\"imageUrl\":\"" + str5 + "\"}";
            }
            if (str3 != null) {
                arrayList.add(str3);
            }
            list = headers;
            indexOf = i;
        }
        return "[" + CollectionsKt.joinToString$default(arrayList, ",", null, null, 0, null, null, 62, null) + "]";
    }
}
