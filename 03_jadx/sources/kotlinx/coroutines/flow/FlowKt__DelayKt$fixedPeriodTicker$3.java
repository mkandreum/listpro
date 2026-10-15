package kotlinx.coroutines.flow;

import androidx.constraintlayout.core.motion.utils.TypedValues;
import androidx.constraintlayout.widget.ConstraintLayout;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlinx.coroutines.channels.ProducerScope;

/* compiled from: Delay.kt */
@Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\b\u0012\u0004\u0012\u00020\u00010\u0002H\u008a@"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/channels/ProducerScope;"}, k = 3, mv = {1, 6, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
@DebugMetadata(c = "kotlinx.coroutines.flow.FlowKt__DelayKt$fixedPeriodTicker$3", f = "Delay.kt", i = {0, 1, 2}, l = {314, TypedValues.AttributesType.TYPE_PATH_ROTATE, TypedValues.AttributesType.TYPE_EASING}, m = "invokeSuspend", n = {"$this$produce", "$this$produce", "$this$produce"}, s = {"L$0", "L$0", "L$0"})
/* loaded from: classes.dex */
final class FlowKt__DelayKt$fixedPeriodTicker$3 extends SuspendLambda implements Function2<ProducerScope<? super Unit>, Continuation<? super Unit>, Object> {
    final /* synthetic */ long $delayMillis;
    final /* synthetic */ long $initialDelayMillis;
    private /* synthetic */ Object L$0;
    int label;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    FlowKt__DelayKt$fixedPeriodTicker$3(long j, long j2, Continuation<? super FlowKt__DelayKt$fixedPeriodTicker$3> continuation) {
        super(2, continuation);
        this.$initialDelayMillis = j;
        this.$delayMillis = j2;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        FlowKt__DelayKt$fixedPeriodTicker$3 flowKt__DelayKt$fixedPeriodTicker$3 = new FlowKt__DelayKt$fixedPeriodTicker$3(this.$initialDelayMillis, this.$delayMillis, continuation);
        flowKt__DelayKt$fixedPeriodTicker$3.L$0 = obj;
        return flowKt__DelayKt$fixedPeriodTicker$3;
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(ProducerScope<? super Unit> producerScope, Continuation<? super Unit> continuation) {
        return ((FlowKt__DelayKt$fixedPeriodTicker$3) create(producerScope, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Code restructure failed: missing block: B:11:0x0063, code lost:
    
        if (kotlinx.coroutines.DelayKt.delay(r7.$delayMillis, r7) == r0) goto L20;
     */
    /* JADX WARN: Code restructure failed: missing block: B:13:0x0053, code lost:
    
        if (r1.getChannel().send(kotlin.Unit.INSTANCE, r7) == r0) goto L20;
     */
    /* JADX WARN: Code restructure failed: missing block: B:14:0x0065, code lost:
    
        return r0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:17:0x003f, code lost:
    
        if (kotlinx.coroutines.DelayKt.delay(r7.$initialDelayMillis, r7) == r0) goto L20;
     */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:11:0x0063 -> B:12:0x0042). Please report as a decompilation issue!!! */
    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final Object invokeSuspend(Object obj) {
        ProducerScope producerScope;
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        int i = this.label;
        if (i != 0) {
            if (i != 1) {
                if (i == 2) {
                    producerScope = (ProducerScope) this.L$0;
                    ResultKt.throwOnFailure(obj);
                    this.L$0 = producerScope;
                    this.label = 3;
                } else if (i != 3) {
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
            }
            producerScope = (ProducerScope) this.L$0;
            ResultKt.throwOnFailure(obj);
        } else {
            ResultKt.throwOnFailure(obj);
            producerScope = (ProducerScope) this.L$0;
            this.L$0 = producerScope;
            this.label = 1;
        }
        this.L$0 = producerScope;
        this.label = 2;
    }
}
