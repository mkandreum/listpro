package kotlinx.coroutines.flow.internal;

import androidx.constraintlayout.widget.ConstraintLayout;
import java.util.concurrent.atomic.AtomicInteger;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.collections.ArraysKt;
import kotlin.collections.IndexedValue;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.functions.Function3;
import kotlinx.coroutines.BuildersKt__Builders_commonKt;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.channels.Channel;
import kotlinx.coroutines.channels.ChannelKt;
import kotlinx.coroutines.channels.ChannelResult;
import kotlinx.coroutines.channels.SendChannel;
import kotlinx.coroutines.flow.Flow;
import kotlinx.coroutines.flow.FlowCollector;

/* compiled from: Combine.kt */
@Metadata(d1 = {"\u0000\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u0002\"\u0004\b\u0001\u0010\u0003*\u00020\u0004H\u008a@"}, d2 = {"<anonymous>", "", "R", "T", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {1, 6, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
@DebugMetadata(c = "kotlinx.coroutines.flow.internal.CombineKt$combineInternal$2", f = "Combine.kt", i = {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2}, l = {57, 79, 82}, m = "invokeSuspend", n = {"latestValues", "resultChannel", "lastReceivedEpoch", "remainingAbsentValues", "currentEpoch", "latestValues", "resultChannel", "lastReceivedEpoch", "remainingAbsentValues", "currentEpoch", "latestValues", "resultChannel", "lastReceivedEpoch", "remainingAbsentValues", "currentEpoch"}, s = {"L$0", "L$1", "L$2", "I$0", "I$1", "L$0", "L$1", "L$2", "I$0", "I$1", "L$0", "L$1", "L$2", "I$0", "I$1"})
/* loaded from: classes.dex */
final class CombineKt$combineInternal$2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ Function0<T[]> $arrayFactory;
    final /* synthetic */ Flow<T>[] $flows;
    final /* synthetic */ FlowCollector<R> $this_combineInternal;
    final /* synthetic */ Function3<FlowCollector<? super R>, T[], Continuation<? super Unit>, Object> $transform;
    int I$0;
    int I$1;
    private /* synthetic */ Object L$0;
    Object L$1;
    Object L$2;
    int label;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    /* JADX WARN: Multi-variable type inference failed */
    CombineKt$combineInternal$2(Flow<? extends T>[] flowArr, Function0<T[]> function0, Function3<? super FlowCollector<? super R>, ? super T[], ? super Continuation<? super Unit>, ? extends Object> function3, FlowCollector<? super R> flowCollector, Continuation<? super CombineKt$combineInternal$2> continuation) {
        super(2, continuation);
        this.$flows = flowArr;
        this.$arrayFactory = function0;
        this.$transform = function3;
        this.$this_combineInternal = flowCollector;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        CombineKt$combineInternal$2 combineKt$combineInternal$2 = new CombineKt$combineInternal$2(this.$flows, this.$arrayFactory, this.$transform, this.$this_combineInternal, continuation);
        combineKt$combineInternal$2.L$0 = obj;
        return combineKt$combineInternal$2;
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return ((CombineKt$combineInternal$2) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Code restructure failed: missing block: B:30:0x010b, code lost:
    
        if (r10.invoke(r11, r9, r21) == r1) goto L43;
     */
    /* JADX WARN: Code restructure failed: missing block: B:32:0x012b, code lost:
    
        if (r11.invoke(r12, r10, r21) == r1) goto L43;
     */
    /* JADX WARN: Code restructure failed: missing block: B:34:0x012e, code lost:
    
        if (r6 != 0) goto L44;
     */
    /* JADX WARN: Removed duplicated region for block: B:13:0x00b4  */
    /* JADX WARN: Removed duplicated region for block: B:16:0x00c3  */
    /* JADX WARN: Removed duplicated region for block: B:18:0x00c6 A[LOOP:0: B:18:0x00c6->B:33:?, LOOP_START, PHI: r6 r10
      0x00c6: PHI (r6v4 int) = (r6v3 int), (r6v5 int) binds: [B:15:0x00c1, B:33:?] A[DONT_GENERATE, DONT_INLINE]
      0x00c6: PHI (r10v5 kotlin.collections.IndexedValue) = (r10v4 kotlin.collections.IndexedValue), (r10v12 kotlin.collections.IndexedValue) binds: [B:15:0x00c1, B:33:?] A[DONT_GENERATE, DONT_INLINE]] */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:29:0x010b -> B:10:0x012e). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:31:0x012b -> B:10:0x012e). Please report as a decompilation issue!!! */
    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final Object invokeSuspend(Object obj) {
        int length;
        int i;
        byte[] bArr;
        Object[] objArr;
        Channel channel;
        Object obj2;
        Object[] objArr2;
        byte[] bArr2;
        int i2;
        int i3;
        IndexedValue indexedValue;
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        int i4 = this.label;
        if (i4 == 0) {
            ResultKt.throwOnFailure(obj);
            CoroutineScope coroutineScope = (CoroutineScope) this.L$0;
            length = this.$flows.length;
            if (length == 0) {
                return Unit.INSTANCE;
            }
            Object[] objArr3 = new Object[length];
            ArraysKt.fill$default(objArr3, NullSurrogateKt.UNINITIALIZED, 0, 0, 6, (Object) null);
            Channel Channel$default = ChannelKt.Channel$default(length, null, null, 6, null);
            AtomicInteger atomicInteger = new AtomicInteger(length);
            i = 0;
            int i5 = 0;
            while (i5 < length) {
                AtomicInteger atomicInteger2 = atomicInteger;
                BuildersKt__Builders_commonKt.launch$default(coroutineScope, null, null, new AnonymousClass1(this.$flows, i5, atomicInteger2, Channel$default, null), 3, null);
                i5++;
                atomicInteger = atomicInteger2;
            }
            bArr = new byte[length];
            objArr = objArr3;
            channel = Channel$default;
            byte b = (byte) (i + 1);
            this.L$0 = objArr;
            this.L$1 = channel;
            this.L$2 = bArr;
            this.I$0 = length;
            this.I$1 = b;
            this.label = 1;
            obj2 = channel.mo1670receiveCatchingJP2dKIU(this);
            if (obj2 != coroutine_suspended) {
            }
            return coroutine_suspended;
        }
        if (i4 != 1) {
            if (i4 == 2 || i4 == 3) {
                i3 = this.I$1;
                i2 = this.I$0;
                bArr2 = (byte[]) this.L$2;
                channel = (Channel) this.L$1;
                objArr2 = (Object[]) this.L$0;
                ResultKt.throwOnFailure(obj);
                i = i3;
                length = i2;
                bArr = bArr2;
                objArr = objArr2;
                byte b2 = (byte) (i + 1);
                this.L$0 = objArr;
                this.L$1 = channel;
                this.L$2 = bArr;
                this.I$0 = length;
                this.I$1 = b2;
                this.label = 1;
                obj2 = channel.mo1670receiveCatchingJP2dKIU(this);
                if (obj2 != coroutine_suspended) {
                    byte[] bArr3 = bArr;
                    i2 = length;
                    i3 = b2;
                    objArr2 = objArr;
                    bArr2 = bArr3;
                    indexedValue = (IndexedValue) ChannelResult.m1682getOrNullimpl(obj2);
                    if (indexedValue == null) {
                        while (true) {
                            int index = indexedValue.getIndex();
                            Object obj3 = objArr2[index];
                            objArr2[index] = indexedValue.getValue();
                            if (obj3 == NullSurrogateKt.UNINITIALIZED) {
                                i2--;
                            }
                            if (bArr2[index] != i3) {
                                bArr2[index] = (byte) i3;
                                indexedValue = (IndexedValue) ChannelResult.m1682getOrNullimpl(channel.mo1671tryReceivePtdJZtk());
                                if (indexedValue != null) {
                                }
                            }
                            if (i2 == 0) {
                                Object[] objArr4 = (Object[]) this.$arrayFactory.invoke();
                                if (objArr4 == null) {
                                    Function3<FlowCollector<? super R>, T[], Continuation<? super Unit>, Object> function3 = this.$transform;
                                    Object obj4 = this.$this_combineInternal;
                                    this.L$0 = objArr2;
                                    this.L$1 = channel;
                                    this.L$2 = bArr2;
                                    this.I$0 = i2;
                                    this.I$1 = i3;
                                    this.label = 2;
                                } else {
                                    ArraysKt.copyInto$default(objArr2, objArr4, 0, 0, 0, 14, (Object) null);
                                    Function3<FlowCollector<? super R>, T[], Continuation<? super Unit>, Object> function32 = this.$transform;
                                    Object obj5 = this.$this_combineInternal;
                                    this.L$0 = objArr2;
                                    this.L$1 = channel;
                                    this.L$2 = bArr2;
                                    this.I$0 = i2;
                                    this.I$1 = i3;
                                    this.label = 3;
                                }
                                byte b22 = (byte) (i + 1);
                                this.L$0 = objArr;
                                this.L$1 = channel;
                                this.L$2 = bArr;
                                this.I$0 = length;
                                this.I$1 = b22;
                                this.label = 1;
                                obj2 = channel.mo1670receiveCatchingJP2dKIU(this);
                                if (obj2 != coroutine_suspended) {
                                }
                            }
                        }
                        i = i3;
                        length = i2;
                        bArr = bArr2;
                        objArr = objArr2;
                        byte b222 = (byte) (i + 1);
                        this.L$0 = objArr;
                        this.L$1 = channel;
                        this.L$2 = bArr;
                        this.I$0 = length;
                        this.I$1 = b222;
                        this.label = 1;
                        obj2 = channel.mo1670receiveCatchingJP2dKIU(this);
                        if (obj2 != coroutine_suspended) {
                        }
                    } else {
                        return Unit.INSTANCE;
                    }
                }
                return coroutine_suspended;
            }
            throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
        i3 = this.I$1;
        i2 = this.I$0;
        bArr2 = (byte[]) this.L$2;
        channel = (Channel) this.L$1;
        objArr2 = (Object[]) this.L$0;
        ResultKt.throwOnFailure(obj);
        obj2 = ((ChannelResult) obj).getHolder();
        indexedValue = (IndexedValue) ChannelResult.m1682getOrNullimpl(obj2);
        if (indexedValue == null) {
        }
    }

    /* compiled from: Combine.kt */
    @Metadata(d1 = {"\u0000\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u0002\"\u0004\b\u0001\u0010\u0003*\u00020\u0004H\u008a@"}, d2 = {"<anonymous>", "", "R", "T", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {1, 6, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "kotlinx.coroutines.flow.internal.CombineKt$combineInternal$2$1", f = "Combine.kt", i = {}, l = {34}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: kotlinx.coroutines.flow.internal.CombineKt$combineInternal$2$1, reason: invalid class name */
    static final class AnonymousClass1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
        final /* synthetic */ Flow<T>[] $flows;
        final /* synthetic */ int $i;
        final /* synthetic */ AtomicInteger $nonClosed;
        final /* synthetic */ Channel<IndexedValue<Object>> $resultChannel;
        int label;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        /* JADX WARN: Multi-variable type inference failed */
        AnonymousClass1(Flow<? extends T>[] flowArr, int i, AtomicInteger atomicInteger, Channel<IndexedValue<Object>> channel, Continuation<? super AnonymousClass1> continuation) {
            super(2, continuation);
            this.$flows = flowArr;
            this.$i = i;
            this.$nonClosed = atomicInteger;
            this.$resultChannel = channel;
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            return new AnonymousClass1(this.$flows, this.$i, this.$nonClosed, this.$resultChannel, continuation);
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
            return ((AnonymousClass1) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object obj) {
            AtomicInteger atomicInteger;
            Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
            int i = this.label;
            try {
                if (i == 0) {
                    ResultKt.throwOnFailure(obj);
                    this.label = 1;
                    if (this.$flows[this.$i].collect(new C00051(this.$resultChannel, this.$i), this) == coroutine_suspended) {
                        return coroutine_suspended;
                    }
                } else {
                    if (i != 1) {
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                    }
                    ResultKt.throwOnFailure(obj);
                }
                if (atomicInteger.decrementAndGet() == 0) {
                    SendChannel.DefaultImpls.close$default(this.$resultChannel, null, 1, null);
                }
                return Unit.INSTANCE;
            } finally {
                if (this.$nonClosed.decrementAndGet() == 0) {
                    SendChannel.DefaultImpls.close$default(this.$resultChannel, null, 1, null);
                }
            }
        }

        /* compiled from: Combine.kt */
        @Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0005\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u0002\"\u0004\b\u0001\u0010\u00032\u0006\u0010\u0004\u001a\u0002H\u0003H\u008a@¢\u0006\u0004\b\u0005\u0010\u0006"}, d2 = {"<anonymous>", "", "R", "T", "value", "emit", "(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;"}, k = 3, mv = {1, 6, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
        /* renamed from: kotlinx.coroutines.flow.internal.CombineKt$combineInternal$2$1$1, reason: invalid class name and collision with other inner class name */
        static final class C00051<T> implements FlowCollector {
            final /* synthetic */ int $i;
            final /* synthetic */ Channel<IndexedValue<Object>> $resultChannel;

            C00051(Channel<IndexedValue<Object>> channel, int i) {
                this.$resultChannel = channel;
                this.$i = i;
            }

            /* JADX WARN: Code restructure failed: missing block: B:18:0x0054, code lost:
            
                if (kotlinx.coroutines.YieldKt.yield(r0) != r1) goto L22;
             */
            /* JADX WARN: Code restructure failed: missing block: B:19:0x0056, code lost:
            
                return r1;
             */
            /* JADX WARN: Code restructure failed: missing block: B:21:0x004b, code lost:
            
                if (r8.send(r2, r0) == r1) goto L21;
             */
            /* JADX WARN: Removed duplicated region for block: B:20:0x0039  */
            /* JADX WARN: Removed duplicated region for block: B:8:0x0025  */
            @Override // kotlinx.coroutines.flow.FlowCollector
            /*
                Code decompiled incorrectly, please refer to instructions dump.
            */
            public final Object emit(T t, Continuation<? super Unit> continuation) {
                CombineKt$combineInternal$2$1$1$emit$1 combineKt$combineInternal$2$1$1$emit$1;
                int i;
                if (continuation instanceof CombineKt$combineInternal$2$1$1$emit$1) {
                    combineKt$combineInternal$2$1$1$emit$1 = (CombineKt$combineInternal$2$1$1$emit$1) continuation;
                    if ((combineKt$combineInternal$2$1$1$emit$1.label & Integer.MIN_VALUE) != 0) {
                        combineKt$combineInternal$2$1$1$emit$1.label -= Integer.MIN_VALUE;
                        Object obj = combineKt$combineInternal$2$1$1$emit$1.result;
                        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                        i = combineKt$combineInternal$2$1$1$emit$1.label;
                        if (i != 0) {
                            ResultKt.throwOnFailure(obj);
                            Channel<IndexedValue<Object>> channel = this.$resultChannel;
                            IndexedValue<Object> indexedValue = new IndexedValue<>(this.$i, t);
                            combineKt$combineInternal$2$1$1$emit$1.label = 1;
                        } else {
                            if (i != 1) {
                                if (i == 2) {
                                    ResultKt.throwOnFailure(obj);
                                    return Unit.INSTANCE;
                                }
                                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                            }
                            ResultKt.throwOnFailure(obj);
                        }
                        combineKt$combineInternal$2$1$1$emit$1.label = 2;
                    }
                }
                combineKt$combineInternal$2$1$1$emit$1 = new CombineKt$combineInternal$2$1$1$emit$1(this, continuation);
                Object obj2 = combineKt$combineInternal$2$1$1$emit$1.result;
                Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                i = combineKt$combineInternal$2$1$1$emit$1.label;
                if (i != 0) {
                }
                combineKt$combineInternal$2$1$1$emit$1.label = 2;
            }
        }
    }
}
