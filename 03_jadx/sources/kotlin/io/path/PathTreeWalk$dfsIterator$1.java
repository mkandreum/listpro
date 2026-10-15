package kotlin.io.path;

import androidx.constraintlayout.widget.ConstraintLayout;
import java.nio.file.FileSystemLoopException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.collections.ArrayDeque;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.RestrictedSuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.sequences.SequenceScope;

/* compiled from: PathTreeWalk.kt */
@Metadata(d1 = {"\u0000\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\b\u0012\u0004\u0012\u00020\u00030\u0002H\u008a@"}, d2 = {"<anonymous>", "", "Lkotlin/sequences/SequenceScope;", "Ljava/nio/file/Path;"}, k = 3, mv = {1, 9, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
@DebugMetadata(c = "kotlin.io.path.PathTreeWalk$dfsIterator$1", f = "PathTreeWalk.kt", i = {0, 0, 0, 0, 0, 0, 1, 1, 1, 2, 2, 2, 2, 2, 2, 3, 3, 3}, l = {192, 198, 211, 217}, m = "invokeSuspend", n = {"$this$iterator", "stack", "entriesReader", "startNode", "this_$iv", "path$iv", "$this$iterator", "stack", "entriesReader", "$this$iterator", "stack", "entriesReader", "pathNode", "this_$iv", "path$iv", "$this$iterator", "stack", "entriesReader"}, s = {"L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$0", "L$1", "L$2", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$0", "L$1", "L$2"})
/* loaded from: classes.dex */
final class PathTreeWalk$dfsIterator$1 extends RestrictedSuspendLambda implements Function2<SequenceScope<? super Path>, Continuation<? super Unit>, Object> {
    private /* synthetic */ Object L$0;
    Object L$1;
    Object L$2;
    Object L$3;
    Object L$4;
    Object L$5;
    int label;
    final /* synthetic */ PathTreeWalk this$0;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    PathTreeWalk$dfsIterator$1(PathTreeWalk pathTreeWalk, Continuation<? super PathTreeWalk$dfsIterator$1> continuation) {
        super(2, continuation);
        this.this$0 = pathTreeWalk;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        PathTreeWalk$dfsIterator$1 pathTreeWalk$dfsIterator$1 = new PathTreeWalk$dfsIterator$1(this.this$0, continuation);
        pathTreeWalk$dfsIterator$1.L$0 = obj;
        return pathTreeWalk$dfsIterator$1;
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(SequenceScope<? super Path> sequenceScope, Continuation<? super Unit> continuation) {
        return ((PathTreeWalk$dfsIterator$1) create(sequenceScope, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Code restructure failed: missing block: B:76:0x0149, code lost:
    
        if (r8.yield(r11, r14) == r0) goto L66;
     */
    /* JADX WARN: Removed duplicated region for block: B:14:0x01e2  */
    /* JADX WARN: Removed duplicated region for block: B:18:0x0157  */
    /* JADX WARN: Removed duplicated region for block: B:57:0x010f  */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:13:0x01e0 -> B:14:0x014e). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:40:0x01e2 -> B:14:0x014e). Please report as a decompilation issue!!! */
    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final Object invokeSuspend(Object obj) {
        SequenceScope sequenceScope;
        ArrayDeque arrayDeque;
        DirectoryEntriesReader directoryEntriesReader;
        boolean followLinks;
        PathNode pathNode;
        Path path;
        Path path2;
        Object keyOf;
        PathTreeWalk pathTreeWalk;
        Path path3;
        boolean createsCycle;
        DirectoryEntriesReader directoryEntriesReader2;
        Path path4;
        SequenceScope sequenceScope2;
        PathNode pathNode2;
        PathTreeWalk pathTreeWalk2;
        ArrayDeque arrayDeque2;
        ArrayDeque arrayDeque3;
        LinkOption[] linkOptionArr;
        boolean createsCycle2;
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        int i = this.label;
        if (i == 0) {
            ResultKt.throwOnFailure(obj);
            sequenceScope = (SequenceScope) this.L$0;
            arrayDeque = new ArrayDeque();
            followLinks = this.this$0.getFollowLinks();
            directoryEntriesReader = new DirectoryEntriesReader(followLinks);
            path = this.this$0.start;
            path2 = this.this$0.start;
            keyOf = PathTreeWalkKt.keyOf(path2, this.this$0.getLinkOptions());
            pathNode = new PathNode(path, keyOf, null);
            pathTreeWalk = this.this$0;
            path3 = pathNode.getPath();
            if (pathNode.getParent() != null) {
                PathsKt.checkFileName(path3);
            }
            LinkOption[] linkOptions = pathTreeWalk.getLinkOptions();
            LinkOption[] linkOptionArr2 = (LinkOption[]) Arrays.copyOf(linkOptions, linkOptions.length);
            if (Files.isDirectory(path3, (LinkOption[]) Arrays.copyOf(linkOptionArr2, linkOptionArr2.length))) {
                createsCycle = PathTreeWalkKt.createsCycle(pathNode);
                if (!createsCycle) {
                    if (pathTreeWalk.getIncludeDirectories()) {
                        this.L$0 = sequenceScope;
                        this.L$1 = arrayDeque;
                        this.L$2 = directoryEntriesReader;
                        this.L$3 = pathNode;
                        this.L$4 = pathTreeWalk;
                        this.L$5 = path3;
                        this.label = 1;
                        if (sequenceScope.yield(path3, this) != coroutine_suspended) {
                            directoryEntriesReader2 = directoryEntriesReader;
                            path4 = path3;
                            sequenceScope2 = sequenceScope;
                            pathNode2 = pathNode;
                            pathTreeWalk2 = pathTreeWalk;
                            arrayDeque2 = arrayDeque;
                        }
                    }
                    LinkOption[] linkOptions2 = pathTreeWalk.getLinkOptions();
                    linkOptionArr = (LinkOption[]) Arrays.copyOf(linkOptions2, linkOptions2.length);
                    if (Files.isDirectory(path3, (LinkOption[]) Arrays.copyOf(linkOptionArr, linkOptionArr.length))) {
                        pathNode.setContentIterator(directoryEntriesReader.readEntries(pathNode).iterator());
                        arrayDeque.addLast(pathNode);
                    }
                    arrayDeque3 = arrayDeque;
                    while (!arrayDeque3.isEmpty()) {
                    }
                    return Unit.INSTANCE;
                }
                throw new FileSystemLoopException(path3.toString());
            }
            if (Files.exists(path3, (LinkOption[]) Arrays.copyOf(new LinkOption[]{LinkOption.NOFOLLOW_LINKS}, 1))) {
                this.L$0 = sequenceScope;
                this.L$1 = arrayDeque;
                this.L$2 = directoryEntriesReader;
                this.label = 2;
            }
            arrayDeque3 = arrayDeque;
            while (!arrayDeque3.isEmpty()) {
            }
            return Unit.INSTANCE;
            return coroutine_suspended;
        }
        if (i != 1) {
            if (i != 2) {
                if (i == 3) {
                    Path path5 = (Path) this.L$5;
                    PathTreeWalk pathTreeWalk3 = (PathTreeWalk) this.L$4;
                    PathNode pathNode3 = (PathNode) this.L$3;
                    DirectoryEntriesReader directoryEntriesReader3 = (DirectoryEntriesReader) this.L$2;
                    ArrayDeque arrayDeque4 = (ArrayDeque) this.L$1;
                    SequenceScope sequenceScope3 = (SequenceScope) this.L$0;
                    ResultKt.throwOnFailure(obj);
                    ArrayDeque arrayDeque5 = arrayDeque4;
                    Path path6 = path5;
                    directoryEntriesReader = directoryEntriesReader3;
                    PathTreeWalk pathTreeWalk4 = pathTreeWalk3;
                    arrayDeque3 = arrayDeque5;
                    PathNode next = pathNode3;
                    sequenceScope = sequenceScope3;
                    LinkOption[] linkOptions3 = pathTreeWalk4.getLinkOptions();
                    LinkOption[] linkOptionArr3 = (LinkOption[]) Arrays.copyOf(linkOptions3, linkOptions3.length);
                    if (Files.isDirectory(path6, (LinkOption[]) Arrays.copyOf(linkOptionArr3, linkOptionArr3.length))) {
                        next.setContentIterator(directoryEntriesReader.readEntries(next).iterator());
                        arrayDeque3.addLast(next);
                    }
                    while (!arrayDeque3.isEmpty()) {
                        Iterator<PathNode> contentIterator = ((PathNode) arrayDeque3.last()).getContentIterator();
                        Intrinsics.checkNotNull(contentIterator);
                        if (contentIterator.hasNext()) {
                            next = contentIterator.next();
                            pathTreeWalk4 = this.this$0;
                            path6 = next.getPath();
                            if (next.getParent() != null) {
                                PathsKt.checkFileName(path6);
                            }
                            LinkOption[] linkOptions4 = pathTreeWalk4.getLinkOptions();
                            LinkOption[] linkOptionArr4 = (LinkOption[]) Arrays.copyOf(linkOptions4, linkOptions4.length);
                            if (Files.isDirectory(path6, (LinkOption[]) Arrays.copyOf(linkOptionArr4, linkOptionArr4.length))) {
                                createsCycle2 = PathTreeWalkKt.createsCycle(next);
                                if (!createsCycle2) {
                                    if (pathTreeWalk4.getIncludeDirectories()) {
                                        this.L$0 = sequenceScope;
                                        this.L$1 = arrayDeque3;
                                        this.L$2 = directoryEntriesReader;
                                        this.L$3 = next;
                                        this.L$4 = pathTreeWalk4;
                                        this.L$5 = path6;
                                        this.label = 3;
                                        if (sequenceScope.yield(path6, this) != coroutine_suspended) {
                                            directoryEntriesReader3 = directoryEntriesReader;
                                            path5 = path6;
                                            arrayDeque4 = arrayDeque3;
                                            pathTreeWalk3 = pathTreeWalk4;
                                            sequenceScope3 = sequenceScope;
                                            pathNode3 = next;
                                            ArrayDeque arrayDeque52 = arrayDeque4;
                                            Path path62 = path5;
                                            directoryEntriesReader = directoryEntriesReader3;
                                            PathTreeWalk pathTreeWalk42 = pathTreeWalk3;
                                            arrayDeque3 = arrayDeque52;
                                            PathNode next2 = pathNode3;
                                            sequenceScope = sequenceScope3;
                                        }
                                    }
                                    LinkOption[] linkOptions32 = pathTreeWalk42.getLinkOptions();
                                    LinkOption[] linkOptionArr32 = (LinkOption[]) Arrays.copyOf(linkOptions32, linkOptions32.length);
                                    if (Files.isDirectory(path62, (LinkOption[]) Arrays.copyOf(linkOptionArr32, linkOptionArr32.length))) {
                                    }
                                    while (!arrayDeque3.isEmpty()) {
                                    }
                                } else {
                                    throw new FileSystemLoopException(path62.toString());
                                }
                            } else if (Files.exists(path62, (LinkOption[]) Arrays.copyOf(new LinkOption[]{LinkOption.NOFOLLOW_LINKS}, 1))) {
                                this.L$0 = sequenceScope;
                                this.L$1 = arrayDeque3;
                                this.L$2 = directoryEntriesReader;
                                this.L$3 = null;
                                this.L$4 = null;
                                this.L$5 = null;
                                this.label = 4;
                                if (sequenceScope.yield(path62, this) == coroutine_suspended) {
                                }
                            } else {
                                continue;
                            }
                            return coroutine_suspended;
                        }
                        arrayDeque3.removeLast();
                    }
                    return Unit.INSTANCE;
                }
                if (i != 4) {
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
            }
            directoryEntriesReader = (DirectoryEntriesReader) this.L$2;
            arrayDeque3 = (ArrayDeque) this.L$1;
            sequenceScope = (SequenceScope) this.L$0;
            ResultKt.throwOnFailure(obj);
            while (!arrayDeque3.isEmpty()) {
            }
            return Unit.INSTANCE;
        }
        path4 = (Path) this.L$5;
        pathTreeWalk2 = (PathTreeWalk) this.L$4;
        pathNode2 = (PathNode) this.L$3;
        directoryEntriesReader2 = (DirectoryEntriesReader) this.L$2;
        arrayDeque2 = (ArrayDeque) this.L$1;
        sequenceScope2 = (SequenceScope) this.L$0;
        ResultKt.throwOnFailure(obj);
        SequenceScope sequenceScope4 = sequenceScope2;
        path3 = path4;
        directoryEntriesReader = directoryEntriesReader2;
        pathNode = pathNode2;
        sequenceScope = sequenceScope4;
        arrayDeque = arrayDeque2;
        pathTreeWalk = pathTreeWalk2;
        LinkOption[] linkOptions22 = pathTreeWalk.getLinkOptions();
        linkOptionArr = (LinkOption[]) Arrays.copyOf(linkOptions22, linkOptions22.length);
        if (Files.isDirectory(path3, (LinkOption[]) Arrays.copyOf(linkOptionArr, linkOptionArr.length))) {
        }
        arrayDeque3 = arrayDeque;
        while (!arrayDeque3.isEmpty()) {
        }
        return Unit.INSTANCE;
    }
}
