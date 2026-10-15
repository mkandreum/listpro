package kotlinx.coroutines.flow;

import androidx.constraintlayout.widget.ConstraintLayout;
import kotlin.Deprecated;
import kotlin.DeprecationLevel;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.channels.BroadcastChannel;
import kotlinx.coroutines.channels.ChannelResult;
import kotlinx.coroutines.channels.ChannelsKt;
import kotlinx.coroutines.channels.ReceiveChannel;
import kotlinx.coroutines.flow.internal.ChannelFlowKt;

/* compiled from: Channels.kt */
@Metadata(d1 = {"\u00000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\u001a\u001e\u0010\u0000\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0001\"\u0004\b\u0000\u0010\u0002*\b\u0012\u0004\u0012\u0002H\u00020\u0003H\u0007\u001a\u001c\u0010\u0004\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0001\"\u0004\b\u0000\u0010\u0002*\b\u0012\u0004\u0012\u0002H\u00020\u0005\u001a/\u0010\u0006\u001a\u00020\u0007\"\u0004\b\u0000\u0010\u0002*\b\u0012\u0004\u0012\u0002H\u00020\b2\f\u0010\t\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0005H\u0086@ø\u0001\u0000¢\u0006\u0002\u0010\n\u001a9\u0010\u000b\u001a\u00020\u0007\"\u0004\b\u0000\u0010\u0002*\b\u0012\u0004\u0012\u0002H\u00020\b2\f\u0010\t\u001a\b\u0012\u0004\u0012\u0002H\u00020\u00052\u0006\u0010\f\u001a\u00020\rH\u0082@ø\u0001\u0000¢\u0006\u0004\b\u000e\u0010\u000f\u001a&\u0010\u0010\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0005\"\u0004\b\u0000\u0010\u0002*\b\u0012\u0004\u0012\u0002H\u00020\u00012\u0006\u0010\u0011\u001a\u00020\u0012H\u0007\u001a\u001c\u0010\u0013\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0001\"\u0004\b\u0000\u0010\u0002*\b\u0012\u0004\u0012\u0002H\u00020\u0005\u0082\u0002\u0004\n\u0002\b\u0019¨\u0006\u0014"}, d2 = {"asFlow", "Lkotlinx/coroutines/flow/Flow;", "T", "Lkotlinx/coroutines/channels/BroadcastChannel;", "consumeAsFlow", "Lkotlinx/coroutines/channels/ReceiveChannel;", "emitAll", "", "Lkotlinx/coroutines/flow/FlowCollector;", "channel", "(Lkotlinx/coroutines/flow/FlowCollector;Lkotlinx/coroutines/channels/ReceiveChannel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "emitAllImpl", "consume", "", "emitAllImpl$FlowKt__ChannelsKt", "(Lkotlinx/coroutines/flow/FlowCollector;Lkotlinx/coroutines/channels/ReceiveChannel;ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "produceIn", "scope", "Lkotlinx/coroutines/CoroutineScope;", "receiveAsFlow", "kotlinx-coroutines-core"}, k = 5, mv = {1, 6, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE, xs = "kotlinx/coroutines/flow/FlowKt")
/* loaded from: classes.dex */
final /* synthetic */ class FlowKt__ChannelsKt {
    public static final <T> Object emitAll(FlowCollector<? super T> flowCollector, ReceiveChannel<? extends T> receiveChannel, Continuation<? super Unit> continuation) {
        Object emitAllImpl$FlowKt__ChannelsKt = emitAllImpl$FlowKt__ChannelsKt(flowCollector, receiveChannel, true, continuation);
        return emitAllImpl$FlowKt__ChannelsKt == IntrinsicsKt.getCOROUTINE_SUSPENDED() ? emitAllImpl$FlowKt__ChannelsKt : Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Code restructure failed: missing block: B:30:0x0096, code lost:
    
        if (r10 == r1) goto L38;
     */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:17:0x006e  */
    /* JADX WARN: Removed duplicated region for block: B:20:0x0077 A[Catch: all -> 0x0057, TRY_LEAVE, TryCatch #0 {all -> 0x0057, blocks: (B:12:0x0034, B:18:0x0071, B:20:0x0077, B:27:0x0085, B:29:0x0086, B:45:0x004d), top: B:7:0x0024 }] */
    /* JADX WARN: Removed duplicated region for block: B:29:0x0086 A[Catch: all -> 0x0057, TRY_LEAVE, TryCatch #0 {all -> 0x0057, blocks: (B:12:0x0034, B:18:0x0071, B:20:0x0077, B:27:0x0085, B:29:0x0086, B:45:0x004d), top: B:7:0x0024 }] */
    /* JADX WARN: Removed duplicated region for block: B:46:0x0059  */
    /* JADX WARN: Removed duplicated region for block: B:9:0x0026  */
    /* JADX WARN: Type inference failed for: r7v0, types: [kotlinx.coroutines.flow.FlowCollector, kotlinx.coroutines.flow.FlowCollector<? super T>] */
    /* JADX WARN: Type inference failed for: r7v1 */
    /* JADX WARN: Type inference failed for: r7v17, types: [boolean] */
    /* JADX WARN: Type inference failed for: r7v18, types: [boolean] */
    /* JADX WARN: Type inference failed for: r7v2 */
    /* JADX WARN: Type inference failed for: r7v24 */
    /* JADX WARN: Type inference failed for: r7v25 */
    /* JADX WARN: Type inference failed for: r7v3, types: [boolean] */
    /* JADX WARN: Type inference failed for: r7v7 */
    /* JADX WARN: Type inference failed for: r9v16 */
    /* JADX WARN: Type inference failed for: r9v3, types: [java.lang.Object, kotlinx.coroutines.flow.FlowCollector] */
    /* JADX WARN: Type inference failed for: r9v7 */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:30:0x0096 -> B:13:0x0037). Please report as a decompilation issue!!! */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static final <T> Object emitAllImpl$FlowKt__ChannelsKt(FlowCollector<? super T> flowCollector, ReceiveChannel<? extends T> receiveChannel, boolean z, Continuation<? super Unit> continuation) {
        FlowKt__ChannelsKt$emitAllImpl$1 flowKt__ChannelsKt$emitAllImpl$1;
        int i;
        Object obj;
        ?? r9;
        Object obj2;
        try {
            if (continuation instanceof FlowKt__ChannelsKt$emitAllImpl$1) {
                flowKt__ChannelsKt$emitAllImpl$1 = (FlowKt__ChannelsKt$emitAllImpl$1) continuation;
                if ((flowKt__ChannelsKt$emitAllImpl$1.label & Integer.MIN_VALUE) != 0) {
                    flowKt__ChannelsKt$emitAllImpl$1.label -= Integer.MIN_VALUE;
                    Object obj3 = flowKt__ChannelsKt$emitAllImpl$1.result;
                    Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                    i = flowKt__ChannelsKt$emitAllImpl$1.label;
                    if (i != 0) {
                        ResultKt.throwOnFailure(obj3);
                        FlowKt.ensureActive(flowCollector);
                        obj2 = flowCollector;
                        flowKt__ChannelsKt$emitAllImpl$1.L$0 = obj2;
                        flowKt__ChannelsKt$emitAllImpl$1.L$1 = receiveChannel;
                        flowKt__ChannelsKt$emitAllImpl$1.Z$0 = z;
                        flowKt__ChannelsKt$emitAllImpl$1.label = 1;
                        obj = receiveChannel.mo1670receiveCatchingJP2dKIU(flowKt__ChannelsKt$emitAllImpl$1);
                        if (obj != coroutine_suspended) {
                        }
                        return coroutine_suspended;
                    }
                    if (i != 1) {
                        if (i == 2) {
                            boolean z2 = (FlowCollector<? super T>) flowKt__ChannelsKt$emitAllImpl$1.Z$0;
                            receiveChannel = (ReceiveChannel) flowKt__ChannelsKt$emitAllImpl$1.L$1;
                            FlowCollector flowCollector2 = (FlowCollector) flowKt__ChannelsKt$emitAllImpl$1.L$0;
                            ResultKt.throwOnFailure(obj3);
                            boolean z3 = z2;
                            FlowCollector flowCollector3 = flowCollector2;
                            FlowCollector flowCollector4 = flowCollector3;
                            z = z3;
                            obj2 = (FlowCollector<? super T>) flowCollector4;
                            try {
                                flowKt__ChannelsKt$emitAllImpl$1.L$0 = obj2;
                                flowKt__ChannelsKt$emitAllImpl$1.L$1 = receiveChannel;
                                flowKt__ChannelsKt$emitAllImpl$1.Z$0 = z;
                                flowKt__ChannelsKt$emitAllImpl$1.label = 1;
                                obj = receiveChannel.mo1670receiveCatchingJP2dKIU(flowKt__ChannelsKt$emitAllImpl$1);
                                if (obj != coroutine_suspended) {
                                    boolean z4 = z;
                                    r9 = obj2;
                                    flowCollector = (FlowCollector<? super T>) (z4 ? 1 : 0);
                                    if (!ChannelResult.m1685isClosedimpl(obj)) {
                                        Throwable m1681exceptionOrNullimpl = ChannelResult.m1681exceptionOrNullimpl(obj);
                                        if (m1681exceptionOrNullimpl != null) {
                                            throw m1681exceptionOrNullimpl;
                                        }
                                        if (flowCollector != 0) {
                                            ChannelsKt.cancelConsumed(receiveChannel, null);
                                        }
                                        return Unit.INSTANCE;
                                    }
                                    Object m1683getOrThrowimpl = ChannelResult.m1683getOrThrowimpl(obj);
                                    flowKt__ChannelsKt$emitAllImpl$1.L$0 = r9;
                                    flowKt__ChannelsKt$emitAllImpl$1.L$1 = receiveChannel;
                                    flowKt__ChannelsKt$emitAllImpl$1.Z$0 = (boolean) flowCollector;
                                    flowKt__ChannelsKt$emitAllImpl$1.label = 2;
                                    Object emit = r9.emit(m1683getOrThrowimpl, flowKt__ChannelsKt$emitAllImpl$1);
                                    z3 = flowCollector;
                                    flowCollector3 = r9;
                                }
                                return coroutine_suspended;
                            } catch (Throwable th) {
                                boolean z5 = z;
                                th = th;
                                flowCollector = z5;
                                try {
                                    throw th;
                                } catch (Throwable th2) {
                                    if (flowCollector != 0) {
                                        ChannelsKt.cancelConsumed(receiveChannel, th);
                                    }
                                    throw th2;
                                }
                            }
                        }
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                    }
                    boolean z6 = (FlowCollector<? super T>) flowKt__ChannelsKt$emitAllImpl$1.Z$0;
                    receiveChannel = (ReceiveChannel) flowKt__ChannelsKt$emitAllImpl$1.L$1;
                    FlowCollector flowCollector5 = (FlowCollector) flowKt__ChannelsKt$emitAllImpl$1.L$0;
                    ResultKt.throwOnFailure(obj3);
                    obj = ((ChannelResult) obj3).getHolder();
                    flowCollector = z6;
                    r9 = flowCollector5;
                    if (!ChannelResult.m1685isClosedimpl(obj)) {
                    }
                }
            }
            if (i != 0) {
            }
        } catch (Throwable th3) {
            th = th3;
        }
        flowKt__ChannelsKt$emitAllImpl$1 = new FlowKt__ChannelsKt$emitAllImpl$1(continuation);
        Object obj32 = flowKt__ChannelsKt$emitAllImpl$1.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        i = flowKt__ChannelsKt$emitAllImpl$1.label;
    }

    public static final <T> Flow<T> receiveAsFlow(ReceiveChannel<? extends T> receiveChannel) {
        return new ChannelAsFlow(receiveChannel, false, null, 0, null, 28, null);
    }

    public static final <T> Flow<T> consumeAsFlow(ReceiveChannel<? extends T> receiveChannel) {
        return new ChannelAsFlow(receiveChannel, true, null, 0, null, 28, null);
    }

    public static final <T> ReceiveChannel<T> produceIn(Flow<? extends T> flow, CoroutineScope coroutineScope) {
        return ChannelFlowKt.asChannelFlow(flow).produceImpl(coroutineScope);
    }

    @Deprecated(level = DeprecationLevel.WARNING, message = "'BroadcastChannel' is obsolete and all corresponding operators are deprecated in the favour of StateFlow and SharedFlow")
    public static final <T> Flow<T> asFlow(final BroadcastChannel<T> broadcastChannel) {
        return new Flow<T>() { // from class: kotlinx.coroutines.flow.FlowKt__ChannelsKt$asFlow$$inlined$unsafeFlow$1
            @Override // kotlinx.coroutines.flow.Flow
            public Object collect(FlowCollector<? super T> flowCollector, Continuation<? super Unit> continuation) {
                Object emitAll = FlowKt.emitAll(flowCollector, BroadcastChannel.this.openSubscription(), continuation);
                return emitAll == IntrinsicsKt.getCOROUTINE_SUSPENDED() ? emitAll : Unit.INSTANCE;
            }
        };
    }
}
