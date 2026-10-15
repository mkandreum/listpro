package kotlinx.coroutines.flow;

import androidx.constraintlayout.widget.ConstraintLayout;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.jvm.functions.Function3;
import kotlin.jvm.internal.Ref;
import kotlinx.coroutines.flow.internal.NullSurrogateKt;

/* compiled from: Transform.kt */
@Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u00022\u0006\u0010\u0003\u001a\u0002H\u0002H\u008a@¢\u0006\u0004\b\u0004\u0010\u0005"}, d2 = {"<anonymous>", "", "T", "value", "emit", "(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;"}, k = 3, mv = {1, 6, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes.dex */
final class FlowKt__TransformKt$runningReduce$1$1<T> implements FlowCollector {
    final /* synthetic */ Ref.ObjectRef<Object> $accumulator;
    final /* synthetic */ Function3<T, T, Continuation<? super T>, Object> $operation;
    final /* synthetic */ FlowCollector<T> $this_unsafeFlow;

    /* JADX WARN: Multi-variable type inference failed */
    FlowKt__TransformKt$runningReduce$1$1(Ref.ObjectRef<Object> objectRef, Function3<? super T, ? super T, ? super Continuation<? super T>, ? extends Object> function3, FlowCollector<? super T> flowCollector) {
        this.$accumulator = objectRef;
        this.$operation = function3;
        this.$this_unsafeFlow = flowCollector;
    }

    /* JADX WARN: Code restructure failed: missing block: B:19:0x007b, code lost:
    
        if (r8.emit(r9, r0) == r1) goto L26;
     */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:21:0x0041  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0025  */
    @Override // kotlinx.coroutines.flow.FlowCollector
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final Object emit(T t, Continuation<? super Unit> continuation) {
        FlowKt__TransformKt$runningReduce$1$1$emit$1 flowKt__TransformKt$runningReduce$1$1$emit$1;
        int i;
        Ref.ObjectRef<Object> objectRef;
        T t2;
        Ref.ObjectRef<Object> objectRef2;
        FlowKt__TransformKt$runningReduce$1$1<T> flowKt__TransformKt$runningReduce$1$1;
        if (continuation instanceof FlowKt__TransformKt$runningReduce$1$1$emit$1) {
            flowKt__TransformKt$runningReduce$1$1$emit$1 = (FlowKt__TransformKt$runningReduce$1$1$emit$1) continuation;
            if ((flowKt__TransformKt$runningReduce$1$1$emit$1.label & Integer.MIN_VALUE) != 0) {
                flowKt__TransformKt$runningReduce$1$1$emit$1.label -= Integer.MIN_VALUE;
                Object obj = flowKt__TransformKt$runningReduce$1$1$emit$1.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                i = flowKt__TransformKt$runningReduce$1$1$emit$1.label;
                if (i != 0) {
                    ResultKt.throwOnFailure(obj);
                    objectRef = this.$accumulator;
                    if (objectRef.element == NullSurrogateKt.NULL) {
                        flowKt__TransformKt$runningReduce$1$1 = this;
                        objectRef.element = t;
                        FlowCollector<T> flowCollector = flowKt__TransformKt$runningReduce$1$1.$this_unsafeFlow;
                        T t3 = flowKt__TransformKt$runningReduce$1$1.$accumulator.element;
                        flowKt__TransformKt$runningReduce$1$1$emit$1.L$0 = null;
                        flowKt__TransformKt$runningReduce$1$1$emit$1.L$1 = null;
                        flowKt__TransformKt$runningReduce$1$1$emit$1.label = 2;
                    } else {
                        Function3<T, T, Continuation<? super T>, Object> function3 = this.$operation;
                        T t4 = this.$accumulator.element;
                        flowKt__TransformKt$runningReduce$1$1$emit$1.L$0 = this;
                        flowKt__TransformKt$runningReduce$1$1$emit$1.L$1 = objectRef;
                        flowKt__TransformKt$runningReduce$1$1$emit$1.label = 1;
                        Object invoke = function3.invoke(t4, t, flowKt__TransformKt$runningReduce$1$1$emit$1);
                        if (invoke != coroutine_suspended) {
                            t2 = invoke;
                            objectRef2 = objectRef;
                            flowKt__TransformKt$runningReduce$1$1 = this;
                        }
                        return coroutine_suspended;
                    }
                } else {
                    if (i != 1) {
                        if (i == 2) {
                            ResultKt.throwOnFailure(obj);
                            return Unit.INSTANCE;
                        }
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                    }
                    objectRef2 = (Ref.ObjectRef) flowKt__TransformKt$runningReduce$1$1$emit$1.L$1;
                    flowKt__TransformKt$runningReduce$1$1 = (FlowKt__TransformKt$runningReduce$1$1) flowKt__TransformKt$runningReduce$1$1$emit$1.L$0;
                    ResultKt.throwOnFailure(obj);
                    t2 = obj;
                }
                T t5 = t2;
                objectRef = objectRef2;
                t = t5;
                objectRef.element = t;
                FlowCollector<T> flowCollector2 = flowKt__TransformKt$runningReduce$1$1.$this_unsafeFlow;
                T t32 = flowKt__TransformKt$runningReduce$1$1.$accumulator.element;
                flowKt__TransformKt$runningReduce$1$1$emit$1.L$0 = null;
                flowKt__TransformKt$runningReduce$1$1$emit$1.L$1 = null;
                flowKt__TransformKt$runningReduce$1$1$emit$1.label = 2;
            }
        }
        flowKt__TransformKt$runningReduce$1$1$emit$1 = new FlowKt__TransformKt$runningReduce$1$1$emit$1(this, continuation);
        Object obj2 = flowKt__TransformKt$runningReduce$1$1$emit$1.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        i = flowKt__TransformKt$runningReduce$1$1$emit$1.label;
        if (i != 0) {
        }
        T t52 = t2;
        objectRef = objectRef2;
        t = t52;
        objectRef.element = t;
        FlowCollector<T> flowCollector22 = flowKt__TransformKt$runningReduce$1$1.$this_unsafeFlow;
        T t322 = flowKt__TransformKt$runningReduce$1$1.$accumulator.element;
        flowKt__TransformKt$runningReduce$1$1$emit$1.L$0 = null;
        flowKt__TransformKt$runningReduce$1$1$emit$1.L$1 = null;
        flowKt__TransformKt$runningReduce$1$1$emit$1.label = 2;
    }
}
