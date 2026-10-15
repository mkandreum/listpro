package kotlinx.coroutines.channels;

import androidx.constraintlayout.widget.ConstraintLayout;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;

/* JADX INFO: Add missing generic type declarations: [E] */
/* compiled from: Deprecated.kt */
@Metadata(d1 = {"\u0000\f\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u0002*\b\u0012\u0004\u0012\u0002H\u00020\u0003H\u008a@"}, d2 = {"<anonymous>", "", "E", "Lkotlinx/coroutines/channels/ProducerScope;"}, k = 3, mv = {1, 6, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
@DebugMetadata(c = "kotlinx.coroutines.channels.ChannelsKt__DeprecatedKt$dropWhile$1", f = "Deprecated.kt", i = {0, 1, 1, 2, 3, 4}, l = {181, 182, 183, 187, 188}, m = "invokeSuspend", n = {"$this$produce", "$this$produce", "e", "$this$produce", "$this$produce", "$this$produce"}, s = {"L$0", "L$0", "L$2", "L$0", "L$0", "L$0"})
/* loaded from: classes.dex */
final class ChannelsKt__DeprecatedKt$dropWhile$1<E> extends SuspendLambda implements Function2<ProducerScope<? super E>, Continuation<? super Unit>, Object> {
    final /* synthetic */ Function2<E, Continuation<? super Boolean>, Object> $predicate;
    final /* synthetic */ ReceiveChannel<E> $this_dropWhile;
    private /* synthetic */ Object L$0;
    Object L$1;
    Object L$2;
    int label;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    /* JADX WARN: Multi-variable type inference failed */
    ChannelsKt__DeprecatedKt$dropWhile$1(ReceiveChannel<? extends E> receiveChannel, Function2<? super E, ? super Continuation<? super Boolean>, ? extends Object> function2, Continuation<? super ChannelsKt__DeprecatedKt$dropWhile$1> continuation) {
        super(2, continuation);
        this.$this_dropWhile = receiveChannel;
        this.$predicate = function2;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        ChannelsKt__DeprecatedKt$dropWhile$1 channelsKt__DeprecatedKt$dropWhile$1 = new ChannelsKt__DeprecatedKt$dropWhile$1(this.$this_dropWhile, this.$predicate, continuation);
        channelsKt__DeprecatedKt$dropWhile$1.L$0 = obj;
        return channelsKt__DeprecatedKt$dropWhile$1;
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(ProducerScope<? super E> producerScope, Continuation<? super Unit> continuation) {
        return ((ChannelsKt__DeprecatedKt$dropWhile$1) create(producerScope, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Code restructure failed: missing block: B:16:0x00f9, code lost:
    
        if (r4.send(r1.next(), r11) == r0) goto L45;
     */
    /* JADX WARN: Code restructure failed: missing block: B:35:0x0082, code lost:
    
        if (r12 != r0) goto L18;
     */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:12:0x00dc  */
    /* JADX WARN: Removed duplicated region for block: B:15:0x00e8  */
    /* JADX WARN: Removed duplicated region for block: B:17:0x00fc  */
    /* JADX WARN: Removed duplicated region for block: B:30:0x00b0  */
    /* JADX WARN: Removed duplicated region for block: B:33:0x00c4  */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:16:0x00f9 -> B:9:0x0023). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:41:0x00a3 -> B:27:0x0054). Please report as a decompilation issue!!! */
    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final Object invokeSuspend(Object obj) {
        ChannelIterator<E> it;
        ProducerScope producerScope;
        ProducerScope producerScope2;
        ChannelIterator<E> channelIterator;
        E e;
        ProducerScope producerScope3;
        ChannelIterator<E> it2;
        ProducerScope producerScope4;
        ChannelIterator<E> channelIterator2;
        Object hasNext;
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        int i = this.label;
        if (i == 0) {
            ResultKt.throwOnFailure(obj);
            ProducerScope producerScope5 = (ProducerScope) this.L$0;
            it = this.$this_dropWhile.iterator();
            producerScope = producerScope5;
            this.L$0 = producerScope;
            this.L$1 = it;
            this.L$2 = null;
            this.label = 1;
            obj = it.hasNext(this);
        } else {
            if (i == 1) {
                it = (ChannelIterator) this.L$1;
                producerScope = (ProducerScope) this.L$0;
                ResultKt.throwOnFailure(obj);
                ProducerScope producerScope6 = producerScope;
                ChannelIterator<E> channelIterator3 = it;
                producerScope2 = producerScope6;
                if (((Boolean) obj).booleanValue()) {
                    E next = channelIterator3.next();
                    Function2<E, Continuation<? super Boolean>, Object> function2 = this.$predicate;
                    this.L$0 = producerScope2;
                    this.L$1 = channelIterator3;
                    this.L$2 = next;
                    this.label = 2;
                    Object invoke = function2.invoke(next, this);
                    if (invoke != coroutine_suspended) {
                        channelIterator = channelIterator3;
                        e = next;
                        obj = invoke;
                        producerScope3 = producerScope2;
                        it = channelIterator;
                        if (((Boolean) obj).booleanValue()) {
                        }
                    }
                    return coroutine_suspended;
                }
                it2 = this.$this_dropWhile.iterator();
                this.L$0 = producerScope2;
                this.L$1 = it2;
                this.label = 4;
                hasNext = it2.hasNext(this);
                if (hasNext != coroutine_suspended) {
                }
                return coroutine_suspended;
            }
            if (i == 2) {
                Object obj2 = this.L$2;
                ChannelIterator<E> channelIterator4 = (ChannelIterator) this.L$1;
                producerScope3 = (ProducerScope) this.L$0;
                ResultKt.throwOnFailure(obj);
                channelIterator = channelIterator4;
                e = obj2;
                it = channelIterator;
                if (((Boolean) obj).booleanValue()) {
                    this.L$0 = producerScope3;
                    this.L$1 = null;
                    this.L$2 = null;
                    this.label = 3;
                    if (producerScope3.send(e, this) != coroutine_suspended) {
                        producerScope2 = producerScope3;
                        it2 = this.$this_dropWhile.iterator();
                        this.L$0 = producerScope2;
                        this.L$1 = it2;
                        this.label = 4;
                        hasNext = it2.hasNext(this);
                        if (hasNext != coroutine_suspended) {
                        }
                    }
                    return coroutine_suspended;
                }
                producerScope = producerScope3;
                this.L$0 = producerScope;
                this.L$1 = it;
                this.L$2 = null;
                this.label = 1;
                obj = it.hasNext(this);
            } else {
                if (i == 3) {
                    producerScope2 = (ProducerScope) this.L$0;
                    ResultKt.throwOnFailure(obj);
                    it2 = this.$this_dropWhile.iterator();
                    this.L$0 = producerScope2;
                    this.L$1 = it2;
                    this.label = 4;
                    hasNext = it2.hasNext(this);
                    if (hasNext != coroutine_suspended) {
                    }
                    return coroutine_suspended;
                }
                if (i != 4) {
                    if (i != 5) {
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                    }
                    channelIterator2 = (ChannelIterator) this.L$1;
                    producerScope4 = (ProducerScope) this.L$0;
                    ResultKt.throwOnFailure(obj);
                    it2 = channelIterator2;
                    producerScope2 = producerScope4;
                    this.L$0 = producerScope2;
                    this.L$1 = it2;
                    this.label = 4;
                    hasNext = it2.hasNext(this);
                    if (hasNext != coroutine_suspended) {
                        ProducerScope producerScope7 = producerScope2;
                        channelIterator2 = it2;
                        obj = hasNext;
                        producerScope4 = producerScope7;
                        if (((Boolean) obj).booleanValue()) {
                            return Unit.INSTANCE;
                        }
                        this.L$0 = producerScope4;
                        this.L$1 = channelIterator2;
                        this.label = 5;
                    }
                    return coroutine_suspended;
                }
                channelIterator2 = (ChannelIterator) this.L$1;
                producerScope4 = (ProducerScope) this.L$0;
                ResultKt.throwOnFailure(obj);
                if (((Boolean) obj).booleanValue()) {
                }
            }
        }
    }
}
