package kotlinx.coroutines.flow;

import androidx.constraintlayout.widget.ConstraintLayout;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function3;

/* compiled from: SharingStarted.kt */
@Metadata(d1 = {"\u0000\u0014\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\u0010\u0000\u001a\u00020\u0001*\b\u0012\u0004\u0012\u00020\u00030\u00022\u0006\u0010\u0004\u001a\u00020\u0005H\u008a@"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/flow/FlowCollector;", "Lkotlinx/coroutines/flow/SharingCommand;", "count", ""}, k = 3, mv = {1, 6, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
@DebugMetadata(c = "kotlinx.coroutines.flow.StartedWhileSubscribed$command$1", f = "SharingStarted.kt", i = {1, 2, 3}, l = {178, 180, 182, 183, 185}, m = "invokeSuspend", n = {"$this$transformLatest", "$this$transformLatest", "$this$transformLatest"}, s = {"L$0", "L$0", "L$0"})
/* loaded from: classes.dex */
final class StartedWhileSubscribed$command$1 extends SuspendLambda implements Function3<FlowCollector<? super SharingCommand>, Integer, Continuation<? super Unit>, Object> {
    /* synthetic */ int I$0;
    private /* synthetic */ Object L$0;
    int label;
    final /* synthetic */ StartedWhileSubscribed this$0;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    StartedWhileSubscribed$command$1(StartedWhileSubscribed startedWhileSubscribed, Continuation<? super StartedWhileSubscribed$command$1> continuation) {
        super(3, continuation);
        this.this$0 = startedWhileSubscribed;
    }

    @Override // kotlin.jvm.functions.Function3
    public /* bridge */ /* synthetic */ Object invoke(FlowCollector<? super SharingCommand> flowCollector, Integer num, Continuation<? super Unit> continuation) {
        return invoke(flowCollector, num.intValue(), continuation);
    }

    public final Object invoke(FlowCollector<? super SharingCommand> flowCollector, int i, Continuation<? super Unit> continuation) {
        StartedWhileSubscribed$command$1 startedWhileSubscribed$command$1 = new StartedWhileSubscribed$command$1(this.this$0, continuation);
        startedWhileSubscribed$command$1.L$0 = flowCollector;
        startedWhileSubscribed$command$1.I$0 = i;
        return startedWhileSubscribed$command$1.invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Code restructure failed: missing block: B:13:0x00aa, code lost:
    
        if (r1.emit(kotlinx.coroutines.flow.SharingCommand.STOP_AND_RESET_REPLAY_CACHE, r9) != r0) goto L35;
     */
    /* JADX WARN: Code restructure failed: missing block: B:19:0x0099, code lost:
    
        if (kotlinx.coroutines.DelayKt.delay(r4, r9) == r0) goto L34;
     */
    /* JADX WARN: Code restructure failed: missing block: B:24:0x0085, code lost:
    
        if (r1.emit(kotlinx.coroutines.flow.SharingCommand.STOP, r9) == r0) goto L34;
     */
    /* JADX WARN: Code restructure failed: missing block: B:29:0x0055, code lost:
    
        if (r1.emit(kotlinx.coroutines.flow.SharingCommand.START, r9) == r0) goto L34;
     */
    /* JADX WARN: Code restructure failed: missing block: B:31:0x0069, code lost:
    
        if (kotlinx.coroutines.DelayKt.delay(r6, r9) == r0) goto L34;
     */
    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final Object invokeSuspend(Object obj) {
        FlowCollector flowCollector;
        long j;
        long j2;
        long j3;
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        int i = this.label;
        if (i == 0) {
            ResultKt.throwOnFailure(obj);
            flowCollector = (FlowCollector) this.L$0;
            if (this.I$0 > 0) {
                this.label = 1;
            } else {
                j = this.this$0.stopTimeout;
                this.L$0 = flowCollector;
                this.label = 2;
            }
            return coroutine_suspended;
        }
        if (i != 1) {
            if (i == 2) {
                flowCollector = (FlowCollector) this.L$0;
                ResultKt.throwOnFailure(obj);
                j2 = this.this$0.replayExpiration;
                if (j2 > 0) {
                    this.L$0 = flowCollector;
                    this.label = 3;
                }
                this.L$0 = null;
                this.label = 5;
            } else if (i == 3) {
                flowCollector = (FlowCollector) this.L$0;
                ResultKt.throwOnFailure(obj);
                j3 = this.this$0.replayExpiration;
                this.L$0 = flowCollector;
                this.label = 4;
            } else if (i == 4) {
                flowCollector = (FlowCollector) this.L$0;
                ResultKt.throwOnFailure(obj);
                this.L$0 = null;
                this.label = 5;
            } else if (i != 5) {
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
        }
        ResultKt.throwOnFailure(obj);
        return Unit.INSTANCE;
    }
}
