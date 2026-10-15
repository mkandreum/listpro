package kotlinx.coroutines.channels;

import androidx.constraintlayout.widget.ConstraintLayout;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.collections.IndexedValue;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;

/* JADX INFO: Add missing generic type declarations: [E] */
/* compiled from: Deprecated.kt */
@Metadata(d1 = {"\u0000\u0010\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u0002*\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002H\u00020\u00040\u0003H\u008a@"}, d2 = {"<anonymous>", "", "E", "Lkotlinx/coroutines/channels/ProducerScope;", "Lkotlin/collections/IndexedValue;"}, k = 3, mv = {1, 6, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
@DebugMetadata(c = "kotlinx.coroutines.channels.ChannelsKt__DeprecatedKt$withIndex$1", f = "Deprecated.kt", i = {0, 0, 1, 1}, l = {370, 371}, m = "invokeSuspend", n = {"$this$produce", "index", "$this$produce", "index"}, s = {"L$0", "I$0", "L$0", "I$0"})
/* loaded from: classes.dex */
final class ChannelsKt__DeprecatedKt$withIndex$1<E> extends SuspendLambda implements Function2<ProducerScope<? super IndexedValue<? extends E>>, Continuation<? super Unit>, Object> {
    final /* synthetic */ ReceiveChannel<E> $this_withIndex;
    int I$0;
    private /* synthetic */ Object L$0;
    Object L$1;
    int label;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    /* JADX WARN: Multi-variable type inference failed */
    ChannelsKt__DeprecatedKt$withIndex$1(ReceiveChannel<? extends E> receiveChannel, Continuation<? super ChannelsKt__DeprecatedKt$withIndex$1> continuation) {
        super(2, continuation);
        this.$this_withIndex = receiveChannel;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        ChannelsKt__DeprecatedKt$withIndex$1 channelsKt__DeprecatedKt$withIndex$1 = new ChannelsKt__DeprecatedKt$withIndex$1(this.$this_withIndex, continuation);
        channelsKt__DeprecatedKt$withIndex$1.L$0 = obj;
        return channelsKt__DeprecatedKt$withIndex$1;
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(ProducerScope<? super IndexedValue<? extends E>> producerScope, Continuation<? super Unit> continuation) {
        return ((ChannelsKt__DeprecatedKt$withIndex$1) create(producerScope, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Removed duplicated region for block: B:12:0x0064  */
    /* JADX WARN: Removed duplicated region for block: B:15:0x0084  */
    /* JADX WARN: Removed duplicated region for block: B:9:0x0056  */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:14:0x0081 -> B:6:0x001e). Please report as a decompilation issue!!! */
    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final Object invokeSuspend(Object obj) {
        ProducerScope producerScope;
        ChannelIterator<E> it;
        int i;
        ProducerScope producerScope2;
        ChannelIterator<E> channelIterator;
        int i2;
        Object hasNext;
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        int i3 = this.label;
        if (i3 == 0) {
            ResultKt.throwOnFailure(obj);
            producerScope = (ProducerScope) this.L$0;
            it = this.$this_withIndex.iterator();
            i = 0;
            this.L$0 = producerScope;
            this.L$1 = it;
            this.I$0 = i;
            this.label = 1;
            hasNext = it.hasNext(this);
            if (hasNext != coroutine_suspended) {
            }
            return coroutine_suspended;
        }
        if (i3 != 1) {
            if (i3 != 2) {
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
            int i4 = this.I$0;
            ChannelIterator<E> channelIterator2 = (ChannelIterator) this.L$1;
            producerScope2 = (ProducerScope) this.L$0;
            ResultKt.throwOnFailure(obj);
            i = i4;
            it = channelIterator2;
            producerScope = producerScope2;
            this.L$0 = producerScope;
            this.L$1 = it;
            this.I$0 = i;
            this.label = 1;
            hasNext = it.hasNext(this);
            if (hasNext != coroutine_suspended) {
                producerScope2 = producerScope;
                obj = hasNext;
                int i5 = i;
                channelIterator = it;
                i2 = i5;
                if (!((Boolean) obj).booleanValue()) {
                    int i6 = i2 + 1;
                    this.L$0 = producerScope2;
                    this.L$1 = channelIterator;
                    this.I$0 = i6;
                    this.label = 2;
                    if (producerScope2.send(new IndexedValue(i2, channelIterator.next()), this) != coroutine_suspended) {
                        it = channelIterator;
                        i = i6;
                        producerScope = producerScope2;
                        this.L$0 = producerScope;
                        this.L$1 = it;
                        this.I$0 = i;
                        this.label = 1;
                        hasNext = it.hasNext(this);
                        if (hasNext != coroutine_suspended) {
                        }
                    }
                } else {
                    return Unit.INSTANCE;
                }
            }
            return coroutine_suspended;
        }
        i2 = this.I$0;
        channelIterator = (ChannelIterator) this.L$1;
        producerScope2 = (ProducerScope) this.L$0;
        ResultKt.throwOnFailure(obj);
        if (!((Boolean) obj).booleanValue()) {
        }
    }
}
