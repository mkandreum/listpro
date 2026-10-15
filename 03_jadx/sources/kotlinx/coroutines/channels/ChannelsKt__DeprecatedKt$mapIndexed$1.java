package kotlinx.coroutines.channels;

import androidx.constraintlayout.widget.ConstraintLayout;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.Boxing;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.functions.Function3;

/* JADX INFO: Add missing generic type declarations: [R] */
/* compiled from: Deprecated.kt */
@Metadata(d1 = {"\u0000\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u0002\"\u0004\b\u0001\u0010\u0003*\b\u0012\u0004\u0012\u0002H\u00030\u0004H\u008a@"}, d2 = {"<anonymous>", "", "E", "R", "Lkotlinx/coroutines/channels/ProducerScope;"}, k = 3, mv = {1, 6, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
@DebugMetadata(c = "kotlinx.coroutines.channels.ChannelsKt__DeprecatedKt$mapIndexed$1", f = "Deprecated.kt", i = {0, 0, 1, 1, 2, 2}, l = {344, 345, 345}, m = "invokeSuspend", n = {"$this$produce", "index", "$this$produce", "index", "$this$produce", "index"}, s = {"L$0", "I$0", "L$0", "I$0", "L$0", "I$0"})
/* loaded from: classes.dex */
final class ChannelsKt__DeprecatedKt$mapIndexed$1<R> extends SuspendLambda implements Function2<ProducerScope<? super R>, Continuation<? super Unit>, Object> {
    final /* synthetic */ ReceiveChannel<E> $this_mapIndexed;
    final /* synthetic */ Function3<Integer, E, Continuation<? super R>, Object> $transform;
    int I$0;
    private /* synthetic */ Object L$0;
    Object L$1;
    Object L$2;
    int label;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    /* JADX WARN: Multi-variable type inference failed */
    ChannelsKt__DeprecatedKt$mapIndexed$1(ReceiveChannel<? extends E> receiveChannel, Function3<? super Integer, ? super E, ? super Continuation<? super R>, ? extends Object> function3, Continuation<? super ChannelsKt__DeprecatedKt$mapIndexed$1> continuation) {
        super(2, continuation);
        this.$this_mapIndexed = receiveChannel;
        this.$transform = function3;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        ChannelsKt__DeprecatedKt$mapIndexed$1 channelsKt__DeprecatedKt$mapIndexed$1 = new ChannelsKt__DeprecatedKt$mapIndexed$1(this.$this_mapIndexed, this.$transform, continuation);
        channelsKt__DeprecatedKt$mapIndexed$1.L$0 = obj;
        return channelsKt__DeprecatedKt$mapIndexed$1;
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(ProducerScope<? super R> producerScope, Continuation<? super Unit> continuation) {
        return ((ChannelsKt__DeprecatedKt$mapIndexed$1) create(producerScope, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Removed duplicated region for block: B:12:0x0076  */
    /* JADX WARN: Removed duplicated region for block: B:17:0x00ac  */
    /* JADX WARN: Removed duplicated region for block: B:18:0x00af  */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:17:0x00ac -> B:7:0x0059). Please report as a decompilation issue!!! */
    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final Object invokeSuspend(Object obj) {
        ProducerScope producerScope;
        ChannelIterator it;
        int i;
        ProducerScope producerScope2;
        ChannelIterator channelIterator;
        ProducerScope producerScope3;
        ProducerScope producerScope4;
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        int i2 = this.label;
        if (i2 == 0) {
            ResultKt.throwOnFailure(obj);
            producerScope = (ProducerScope) this.L$0;
            it = this.$this_mapIndexed.iterator();
            i = 0;
        } else {
            if (i2 == 1) {
                i = this.I$0;
                it = (ChannelIterator) this.L$1;
                producerScope4 = (ProducerScope) this.L$0;
                ResultKt.throwOnFailure(obj);
                if (!((Boolean) obj).booleanValue()) {
                    Object next = it.next();
                    Function3<Integer, E, Continuation<? super R>, Object> function3 = this.$transform;
                    int i3 = i + 1;
                    Integer boxInt = Boxing.boxInt(i);
                    this.L$0 = producerScope4;
                    this.L$1 = it;
                    this.L$2 = producerScope4;
                    this.I$0 = i3;
                    this.label = 2;
                    obj = function3.invoke(boxInt, next, this);
                    if (obj != coroutine_suspended) {
                        producerScope2 = producerScope4;
                        i = i3;
                        channelIterator = it;
                        producerScope3 = producerScope2;
                        this.L$0 = producerScope2;
                        this.L$1 = channelIterator;
                        this.L$2 = null;
                        this.I$0 = i;
                        this.label = 3;
                        if (producerScope3.send(obj, this) != coroutine_suspended) {
                        }
                    }
                    return coroutine_suspended;
                }
                return Unit.INSTANCE;
            }
            if (i2 == 2) {
                i = this.I$0;
                producerScope3 = (ProducerScope) this.L$2;
                channelIterator = (ChannelIterator) this.L$1;
                producerScope2 = (ProducerScope) this.L$0;
                ResultKt.throwOnFailure(obj);
                this.L$0 = producerScope2;
                this.L$1 = channelIterator;
                this.L$2 = null;
                this.I$0 = i;
                this.label = 3;
                if (producerScope3.send(obj, this) != coroutine_suspended) {
                    it = channelIterator;
                    producerScope = producerScope2;
                }
                return coroutine_suspended;
            }
            if (i2 != 3) {
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
            i = this.I$0;
            it = (ChannelIterator) this.L$1;
            ProducerScope producerScope5 = (ProducerScope) this.L$0;
            ResultKt.throwOnFailure(obj);
            producerScope = producerScope5;
        }
        this.L$0 = producerScope;
        this.L$1 = it;
        this.I$0 = i;
        this.label = 1;
        Object hasNext = it.hasNext(this);
        if (hasNext != coroutine_suspended) {
            producerScope4 = producerScope;
            obj = hasNext;
            if (!((Boolean) obj).booleanValue()) {
            }
        }
        return coroutine_suspended;
    }
}
