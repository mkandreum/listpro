package kotlin.text;

import androidx.constraintlayout.widget.ConstraintLayout;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.RestrictedSuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.sequences.SequenceScope;

/* compiled from: Regex.kt */
@Metadata(d1 = {"\u0000\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\u0010\u0000\u001a\u00020\u0001*\b\u0012\u0004\u0012\u00020\u00030\u0002H\u008a@"}, d2 = {"<anonymous>", "", "Lkotlin/sequences/SequenceScope;", ""}, k = 3, mv = {1, 9, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
@DebugMetadata(c = "kotlin.text.Regex$splitToSequence$1", f = "Regex.kt", i = {1, 1, 1}, l = {275, 283, 287}, m = "invokeSuspend", n = {"$this$sequence", "matcher", "splitCount"}, s = {"L$0", "L$1", "I$0"})
/* loaded from: classes.dex */
final class Regex$splitToSequence$1 extends RestrictedSuspendLambda implements Function2<SequenceScope<? super String>, Continuation<? super Unit>, Object> {
    final /* synthetic */ CharSequence $input;
    final /* synthetic */ int $limit;
    int I$0;
    private /* synthetic */ Object L$0;
    Object L$1;
    int label;
    final /* synthetic */ Regex this$0;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    Regex$splitToSequence$1(Regex regex, CharSequence charSequence, int i, Continuation<? super Regex$splitToSequence$1> continuation) {
        super(2, continuation);
        this.this$0 = regex;
        this.$input = charSequence;
        this.$limit = i;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        Regex$splitToSequence$1 regex$splitToSequence$1 = new Regex$splitToSequence$1(this.this$0, this.$input, this.$limit, continuation);
        regex$splitToSequence$1.L$0 = obj;
        return regex$splitToSequence$1;
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(SequenceScope<? super String> sequenceScope, Continuation<? super Unit> continuation) {
        return ((Regex$splitToSequence$1) create(sequenceScope, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Code restructure failed: missing block: B:18:0x0071, code lost:
    
        if (r6.yield(r9.$input.subSequence(r5, r10.start()).toString(), r9) != r0) goto L21;
     */
    /* JADX WARN: Code restructure failed: missing block: B:19:0x00b7, code lost:
    
        return r0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:21:0x00a0, code lost:
    
        if (r6.yield(r10.subSequence(r5, r10.length()).toString(), r9) == r0) goto L32;
     */
    /* JADX WARN: Code restructure failed: missing block: B:31:0x00b5, code lost:
    
        if (r10.yield(r9.$input.toString(), r9) == r0) goto L32;
     */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:18:0x0071 -> B:13:0x0074). Please report as a decompilation issue!!! */
    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final Object invokeSuspend(Object obj) {
        Pattern pattern;
        int i;
        SequenceScope sequenceScope;
        Matcher matcher;
        int i2;
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        int i3 = this.label;
        if (i3 == 0) {
            ResultKt.throwOnFailure(obj);
            SequenceScope sequenceScope2 = (SequenceScope) this.L$0;
            pattern = this.this$0.nativePattern;
            Matcher matcher2 = pattern.matcher(this.$input);
            if (this.$limit == 1 || !matcher2.find()) {
                this.label = 1;
            } else {
                i = 0;
                sequenceScope = sequenceScope2;
                matcher = matcher2;
                i2 = 0;
                this.L$0 = sequenceScope;
                this.L$1 = matcher;
                this.I$0 = i2;
                this.label = 2;
            }
        } else if (i3 == 1) {
            ResultKt.throwOnFailure(obj);
        } else {
            if (i3 != 2) {
                if (i3 != 3) {
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
                ResultKt.throwOnFailure(obj);
                return Unit.INSTANCE;
            }
            i2 = this.I$0;
            Matcher matcher3 = (Matcher) this.L$1;
            sequenceScope = (SequenceScope) this.L$0;
            ResultKt.throwOnFailure(obj);
            matcher = matcher3;
            i = matcher.end();
            i2++;
            if (i2 == this.$limit - 1 || !matcher.find()) {
                CharSequence charSequence = this.$input;
                this.L$0 = null;
                this.L$1 = null;
                this.label = 3;
            }
            this.L$0 = sequenceScope;
            this.L$1 = matcher;
            this.I$0 = i2;
            this.label = 2;
        }
        return Unit.INSTANCE;
    }
}
