package kotlinx.coroutines.flow;

import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.functions.Function3;

/* JADX INFO: Add missing generic type declarations: [R] */
/* compiled from: Emitters.kt */
@Metadata(d1 = {"\u0000\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u0002\"\u0004\b\u0001\u0010\u0003*\b\u0012\u0004\u0012\u0002H\u00030\u0004H\u008a@"}, d2 = {"<anonymous>", "", "T", "R", "Lkotlinx/coroutines/flow/FlowCollector;"}, k = 3, mv = {1, 6, 0}, xi = 176)
@DebugMetadata(c = "kotlinx.coroutines.flow.FlowKt__EmittersKt$transform$1", f = "Emitters.kt", i = {}, l = {40}, m = "invokeSuspend", n = {}, s = {})
/* loaded from: classes.dex */
public final class FlowKt__EmittersKt$transform$1<R> extends SuspendLambda implements Function2<FlowCollector<? super R>, Continuation<? super Unit>, Object> {
    final /* synthetic */ Flow<T> $this_transform;
    final /* synthetic */ Function3<FlowCollector<? super R>, T, Continuation<? super Unit>, Object> $transform;
    private /* synthetic */ Object L$0;
    int label;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    /* JADX WARN: Multi-variable type inference failed */
    public FlowKt__EmittersKt$transform$1(Flow<? extends T> flow, Function3<? super FlowCollector<? super R>, ? super T, ? super Continuation<? super Unit>, ? extends Object> function3, Continuation<? super FlowKt__EmittersKt$transform$1> continuation) {
        super(2, continuation);
        this.$this_transform = flow;
        this.$transform = function3;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        FlowKt__EmittersKt$transform$1 flowKt__EmittersKt$transform$1 = new FlowKt__EmittersKt$transform$1(this.$this_transform, this.$transform, continuation);
        flowKt__EmittersKt$transform$1.L$0 = obj;
        return flowKt__EmittersKt$transform$1;
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(FlowCollector<? super R> flowCollector, Continuation<? super Unit> continuation) {
        return ((FlowKt__EmittersKt$transform$1) create(flowCollector, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    /* compiled from: Emitters.kt */
    @Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0005\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u0002\"\u0004\b\u0001\u0010\u00032\u0006\u0010\u0004\u001a\u0002H\u0002H\u008a@¢\u0006\u0004\b\u0005\u0010\u0006"}, d2 = {"<anonymous>", "", "T", "R", "value", "emit", "(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;"}, k = 3, mv = {1, 6, 0}, xi = 176)
    /* renamed from: kotlinx.coroutines.flow.FlowKt__EmittersKt$transform$1$1, reason: invalid class name */
    public static final class AnonymousClass1<T> implements FlowCollector {
        final /* synthetic */ FlowCollector<R> $$this$flow;
        final /* synthetic */ Function3<FlowCollector<? super R>, T, Continuation<? super Unit>, Object> $transform;

        /* JADX WARN: Multi-variable type inference failed */
        public AnonymousClass1(Function3<? super FlowCollector<? super R>, ? super T, ? super Continuation<? super Unit>, ? extends Object> function3, FlowCollector<? super R> flowCollector) {
            this.$transform = function3;
            this.$$this$flow = flowCollector;
        }

        /* JADX WARN: Removed duplicated region for block: B:15:0x0032  */
        /* JADX WARN: Removed duplicated region for block: B:8:0x0024  */
        @Override // kotlinx.coroutines.flow.FlowCollector
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        public final Object emit(T t, Continuation<? super Unit> continuation) {
            FlowKt__EmittersKt$transform$1$1$emit$1 flowKt__EmittersKt$transform$1$1$emit$1;
            int i;
            if (continuation instanceof FlowKt__EmittersKt$transform$1$1$emit$1) {
                flowKt__EmittersKt$transform$1$1$emit$1 = (FlowKt__EmittersKt$transform$1$1$emit$1) continuation;
                if ((flowKt__EmittersKt$transform$1$1$emit$1.label & Integer.MIN_VALUE) != 0) {
                    flowKt__EmittersKt$transform$1$1$emit$1.label -= Integer.MIN_VALUE;
                    Object obj = flowKt__EmittersKt$transform$1$1$emit$1.result;
                    Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                    i = flowKt__EmittersKt$transform$1$1$emit$1.label;
                    if (i != 0) {
                        ResultKt.throwOnFailure(obj);
                        Function3<FlowCollector<? super R>, T, Continuation<? super Unit>, Object> function3 = this.$transform;
                        FlowCollector<R> flowCollector = this.$$this$flow;
                        flowKt__EmittersKt$transform$1$1$emit$1.label = 1;
                        if (function3.invoke(flowCollector, t, flowKt__EmittersKt$transform$1$1$emit$1) == coroutine_suspended) {
                            return coroutine_suspended;
                        }
                    } else {
                        if (i != 1) {
                            throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                        }
                        ResultKt.throwOnFailure(obj);
                    }
                    return Unit.INSTANCE;
                }
            }
            flowKt__EmittersKt$transform$1$1$emit$1 = new FlowKt__EmittersKt$transform$1$1$emit$1(this, continuation);
            Object obj2 = flowKt__EmittersKt$transform$1$1$emit$1.result;
            Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
            i = flowKt__EmittersKt$transform$1$1$emit$1.label;
            if (i != 0) {
            }
            return Unit.INSTANCE;
        }

        public final Object emit$$forInline(T t, Continuation<? super Unit> continuation) {
            new FlowKt__EmittersKt$transform$1$1$emit$1(this, continuation);
            this.$transform.invoke(this.$$this$flow, t, continuation);
            return Unit.INSTANCE;
        }
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Object invokeSuspend(Object obj) {
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        int i = this.label;
        if (i == 0) {
            ResultKt.throwOnFailure(obj);
            FlowCollector flowCollector = (FlowCollector) this.L$0;
            this.label = 1;
            if (this.$this_transform.collect(new AnonymousClass1(this.$transform, flowCollector), this) == coroutine_suspended) {
                return coroutine_suspended;
            }
        } else {
            if (i != 1) {
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
            ResultKt.throwOnFailure(obj);
        }
        return Unit.INSTANCE;
    }

    public final Object invokeSuspend$$forInline(Object obj) {
        this.$this_transform.collect(new AnonymousClass1(this.$transform, (FlowCollector) this.L$0), this);
        return Unit.INSTANCE;
    }
}
