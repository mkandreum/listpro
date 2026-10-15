package androidx.recyclerview.widget;

import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
public class DiffUtil {
    private static final Comparator<Snake> SNAKE_COMPARATOR = new Comparator<Snake>() { // from class: androidx.recyclerview.widget.DiffUtil.1
        @Override // java.util.Comparator
        public int compare(Snake snake, Snake snake2) {
            int i = snake.x - snake2.x;
            return i == 0 ? snake.y - snake2.y : i;
        }
    };

    public static abstract class Callback {
        public abstract boolean areContentsTheSame(int i, int i2);

        public abstract boolean areItemsTheSame(int i, int i2);

        public Object getChangePayload(int i, int i2) {
            return null;
        }

        public abstract int getNewListSize();

        public abstract int getOldListSize();
    }

    public static abstract class ItemCallback<T> {
        public abstract boolean areContentsTheSame(T t, T t2);

        public abstract boolean areItemsTheSame(T t, T t2);

        public Object getChangePayload(T t, T t2) {
            return null;
        }
    }

    private DiffUtil() {
    }

    public static DiffResult calculateDiff(Callback callback) {
        return calculateDiff(callback, true);
    }

    public static DiffResult calculateDiff(Callback callback, boolean z) {
        int oldListSize = callback.getOldListSize();
        int newListSize = callback.getNewListSize();
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        arrayList2.add(new Range(0, oldListSize, 0, newListSize));
        int abs = oldListSize + newListSize + Math.abs(oldListSize - newListSize);
        int i = abs * 2;
        int[] iArr = new int[i];
        int[] iArr2 = new int[i];
        ArrayList arrayList3 = new ArrayList();
        while (!arrayList2.isEmpty()) {
            Range range = (Range) arrayList2.remove(arrayList2.size() - 1);
            int[] iArr3 = iArr2;
            int[] iArr4 = iArr;
            Callback callback2 = callback;
            Snake diffPartial = diffPartial(callback2, range.oldListStart, range.oldListEnd, range.newListStart, range.newListEnd, iArr4, iArr3, abs);
            iArr = iArr4;
            if (diffPartial != null) {
                if (diffPartial.size > 0) {
                    arrayList.add(diffPartial);
                }
                diffPartial.x += range.oldListStart;
                diffPartial.y += range.newListStart;
                Range range2 = arrayList3.isEmpty() ? new Range() : (Range) arrayList3.remove(arrayList3.size() - 1);
                range2.oldListStart = range.oldListStart;
                range2.newListStart = range.newListStart;
                if (diffPartial.reverse) {
                    range2.oldListEnd = diffPartial.x;
                    range2.newListEnd = diffPartial.y;
                } else if (diffPartial.removal) {
                    range2.oldListEnd = diffPartial.x - 1;
                    range2.newListEnd = diffPartial.y;
                } else {
                    range2.oldListEnd = diffPartial.x;
                    range2.newListEnd = diffPartial.y - 1;
                }
                arrayList2.add(range2);
                if (diffPartial.reverse) {
                    if (diffPartial.removal) {
                        range.oldListStart = diffPartial.x + diffPartial.size + 1;
                        range.newListStart = diffPartial.y + diffPartial.size;
                    } else {
                        range.oldListStart = diffPartial.x + diffPartial.size;
                        range.newListStart = diffPartial.y + diffPartial.size + 1;
                    }
                } else {
                    range.oldListStart = diffPartial.x + diffPartial.size;
                    range.newListStart = diffPartial.y + diffPartial.size;
                }
                arrayList2.add(range);
            } else {
                arrayList3.add(range);
            }
            callback = callback2;
            iArr2 = iArr3;
        }
        Callback callback3 = callback;
        Collections.sort(arrayList, SNAKE_COMPARATOR);
        return new DiffResult(callback3, arrayList, iArr, iArr2, z);
    }

    /* JADX WARN: Code restructure failed: missing block: B:14:0x0042, code lost:
    
        if (r1[r13 - 1] < r1[r13 + r5]) goto L20;
     */
    /* JADX WARN: Code restructure failed: missing block: B:52:0x00c1, code lost:
    
        if (r23[r13 - 1] < r23[r13 + 1]) goto L49;
     */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:21:0x0065 A[LOOP:2: B:17:0x0055->B:21:0x0065, LOOP_END] */
    /* JADX WARN: Removed duplicated region for block: B:22:0x006f A[EDGE_INSN: B:22:0x006f->B:23:0x006f BREAK  A[LOOP:2: B:17:0x0055->B:21:0x0065], SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private static Snake diffPartial(Callback callback, int i, int i2, int i3, int i4, int[] iArr, int[] iArr2, int i5) {
        int i6;
        boolean z;
        int i7;
        int i8;
        boolean z2;
        int i9;
        int i10;
        int[] iArr3 = iArr;
        int i11 = i2 - i;
        int i12 = i4 - i3;
        int i13 = 1;
        if (i11 < 1 || i12 < 1) {
            return null;
        }
        int i14 = i11 - i12;
        int i15 = ((i11 + i12) + 1) / 2;
        int i16 = (i5 - i15) - 1;
        int i17 = i5 + i15 + 1;
        boolean z3 = false;
        Arrays.fill(iArr3, i16, i17, 0);
        Arrays.fill(iArr2, i16 + i14, i17 + i14, i11);
        boolean z4 = i14 % 2 != 0;
        int i18 = 0;
        while (i18 <= i15) {
            int i19 = -i18;
            int i20 = i19;
            while (i20 <= i18) {
                if (i20 != i19) {
                    if (i20 != i18) {
                        int i21 = i5 + i20;
                    }
                    i8 = iArr3[(i5 + i20) - i13] + i13;
                    z2 = i13;
                    i9 = i8 - i20;
                    while (i8 < i11 && i9 < i12) {
                        i10 = i13;
                        if (callback.areItemsTheSame(i + i8, i3 + i9)) {
                            break;
                        }
                        i8++;
                        i9++;
                        i13 = i10;
                    }
                    i10 = i13;
                    int i22 = i5 + i20;
                    iArr3[i22] = i8;
                    if (!z4 && i20 >= (i14 - i18) + 1 && i20 <= (i14 + i18) - 1 && i8 >= iArr2[i22]) {
                        Snake snake = new Snake();
                        snake.x = iArr2[i22];
                        snake.y = snake.x - i20;
                        snake.size = iArr3[i22] - iArr2[i22];
                        snake.removal = z2;
                        snake.reverse = false;
                        return snake;
                    }
                    i20 += 2;
                    z3 = false;
                    i13 = i10;
                }
                i8 = iArr3[i5 + i20 + i13];
                z2 = z3;
                i9 = i8 - i20;
                while (i8 < i11) {
                    i10 = i13;
                    if (callback.areItemsTheSame(i + i8, i3 + i9)) {
                    }
                }
                i10 = i13;
                int i222 = i5 + i20;
                iArr3[i222] = i8;
                if (!z4) {
                }
                i20 += 2;
                z3 = false;
                i13 = i10;
            }
            int i23 = i13;
            boolean z5 = z3;
            int i24 = i19;
            while (i24 <= i18) {
                int i25 = i24 + i14;
                if (i25 != i18 + i14) {
                    if (i25 != i19 + i14) {
                        int i26 = i5 + i25;
                    }
                    i6 = iArr2[(i5 + i25) + 1] - 1;
                    z = i23;
                    for (i7 = i6 - i25; i6 > 0 && i7 > 0 && callback.areItemsTheSame((i + i6) - 1, (i3 + i7) - 1); i7--) {
                        i6--;
                    }
                    int i27 = i5 + i25;
                    iArr2[i27] = i6;
                    if (z4 && i25 >= i19 && i25 <= i18 && iArr[i27] >= i6) {
                        Snake snake2 = new Snake();
                        snake2.x = iArr2[i27];
                        snake2.y = snake2.x - i25;
                        snake2.size = iArr[i27] - iArr2[i27];
                        snake2.removal = z;
                        snake2.reverse = i23;
                        return snake2;
                    }
                    i24 += 2;
                    i23 = i23;
                    z5 = false;
                }
                i6 = iArr2[(i5 + i25) - 1];
                z = z5;
                while (i6 > 0) {
                    i6--;
                }
                int i272 = i5 + i25;
                iArr2[i272] = i6;
                if (z4) {
                }
                i24 += 2;
                i23 = i23;
                z5 = false;
            }
            i18++;
            i13 = i23;
            z3 = false;
            iArr3 = iArr;
        }
        throw new IllegalStateException("DiffUtil hit an unexpected case while trying to calculate the optimal path. Please make sure your data is not changing during the diff calculation.");
    }

    static class Snake {
        boolean removal;
        boolean reverse;
        int size;
        int x;
        int y;

        Snake() {
        }
    }

    static class Range {
        int newListEnd;
        int newListStart;
        int oldListEnd;
        int oldListStart;

        public Range() {
        }

        public Range(int i, int i2, int i3, int i4) {
            this.oldListStart = i;
            this.oldListEnd = i2;
            this.newListStart = i3;
            this.newListEnd = i4;
        }
    }

    public static class DiffResult {
        private static final int FLAG_CHANGED = 2;
        private static final int FLAG_IGNORE = 16;
        private static final int FLAG_MASK = 31;
        private static final int FLAG_MOVED_CHANGED = 4;
        private static final int FLAG_MOVED_NOT_CHANGED = 8;
        private static final int FLAG_NOT_CHANGED = 1;
        private static final int FLAG_OFFSET = 5;
        public static final int NO_POSITION = -1;
        private final Callback mCallback;
        private final boolean mDetectMoves;
        private final int[] mNewItemStatuses;
        private final int mNewListSize;
        private final int[] mOldItemStatuses;
        private final int mOldListSize;
        private final List<Snake> mSnakes;

        DiffResult(Callback callback, List<Snake> list, int[] iArr, int[] iArr2, boolean z) {
            this.mSnakes = list;
            this.mOldItemStatuses = iArr;
            this.mNewItemStatuses = iArr2;
            Arrays.fill(iArr, 0);
            Arrays.fill(iArr2, 0);
            this.mCallback = callback;
            this.mOldListSize = callback.getOldListSize();
            this.mNewListSize = callback.getNewListSize();
            this.mDetectMoves = z;
            addRootSnake();
            findMatchingItems();
        }

        private void addRootSnake() {
            Snake snake = this.mSnakes.isEmpty() ? null : this.mSnakes.get(0);
            if (snake != null && snake.x == 0 && snake.y == 0) {
                return;
            }
            Snake snake2 = new Snake();
            snake2.x = 0;
            snake2.y = 0;
            snake2.removal = false;
            snake2.size = 0;
            snake2.reverse = false;
            this.mSnakes.add(0, snake2);
        }

        private void findMatchingItems() {
            int i = this.mOldListSize;
            int i2 = this.mNewListSize;
            for (int size = this.mSnakes.size() - 1; size >= 0; size--) {
                Snake snake = this.mSnakes.get(size);
                int i3 = snake.x + snake.size;
                int i4 = snake.y + snake.size;
                if (this.mDetectMoves) {
                    while (i > i3) {
                        findAddition(i, i2, size);
                        i--;
                    }
                    while (i2 > i4) {
                        findRemoval(i, i2, size);
                        i2--;
                    }
                }
                for (int i5 = 0; i5 < snake.size; i5++) {
                    int i6 = snake.x + i5;
                    int i7 = snake.y + i5;
                    int i8 = this.mCallback.areContentsTheSame(i6, i7) ? 1 : 2;
                    this.mOldItemStatuses[i6] = (i7 << 5) | i8;
                    this.mNewItemStatuses[i7] = (i6 << 5) | i8;
                }
                i = snake.x;
                i2 = snake.y;
            }
        }

        private void findAddition(int i, int i2, int i3) {
            if (this.mOldItemStatuses[i - 1] != 0) {
                return;
            }
            findMatchingItem(i, i2, i3, false);
        }

        private void findRemoval(int i, int i2, int i3) {
            if (this.mNewItemStatuses[i2 - 1] != 0) {
                return;
            }
            findMatchingItem(i, i2, i3, true);
        }

        public int convertOldPositionToNew(int i) {
            if (i < 0 || i >= this.mOldListSize) {
                throw new IndexOutOfBoundsException("Index out of bounds - passed position = " + i + ", old list size = " + this.mOldListSize);
            }
            int i2 = this.mOldItemStatuses[i];
            if ((i2 & 31) == 0) {
                return -1;
            }
            return i2 >> 5;
        }

        public int convertNewPositionToOld(int i) {
            if (i < 0 || i >= this.mNewListSize) {
                throw new IndexOutOfBoundsException("Index out of bounds - passed position = " + i + ", new list size = " + this.mNewListSize);
            }
            int i2 = this.mNewItemStatuses[i];
            if ((i2 & 31) == 0) {
                return -1;
            }
            return i2 >> 5;
        }

        private boolean findMatchingItem(int i, int i2, int i3, boolean z) {
            int i4;
            int i5;
            int i6;
            if (z) {
                i2--;
                i5 = i;
                i4 = i2;
            } else {
                i4 = i - 1;
                i5 = i4;
            }
            while (i3 >= 0) {
                Snake snake = this.mSnakes.get(i3);
                int i7 = snake.x + snake.size;
                int i8 = snake.y + snake.size;
                if (z) {
                    for (int i9 = i5 - 1; i9 >= i7; i9--) {
                        if (this.mCallback.areItemsTheSame(i9, i4)) {
                            i6 = this.mCallback.areContentsTheSame(i9, i4) ? 8 : 4;
                            this.mNewItemStatuses[i4] = (i9 << 5) | 16;
                            this.mOldItemStatuses[i9] = (i4 << 5) | i6;
                            return true;
                        }
                    }
                } else {
                    for (int i10 = i2 - 1; i10 >= i8; i10--) {
                        if (this.mCallback.areItemsTheSame(i4, i10)) {
                            i6 = this.mCallback.areContentsTheSame(i4, i10) ? 8 : 4;
                            int i11 = i - 1;
                            this.mOldItemStatuses[i11] = (i10 << 5) | 16;
                            this.mNewItemStatuses[i10] = (i11 << 5) | i6;
                            return true;
                        }
                    }
                }
                i5 = snake.x;
                i2 = snake.y;
                i3--;
            }
            return false;
        }

        public void dispatchUpdatesTo(RecyclerView.Adapter adapter) {
            dispatchUpdatesTo(new AdapterListUpdateCallback(adapter));
        }

        public void dispatchUpdatesTo(ListUpdateCallback listUpdateCallback) {
            BatchingListUpdateCallback batchingListUpdateCallback;
            DiffResult diffResult;
            if (listUpdateCallback instanceof BatchingListUpdateCallback) {
                batchingListUpdateCallback = (BatchingListUpdateCallback) listUpdateCallback;
            } else {
                batchingListUpdateCallback = new BatchingListUpdateCallback(listUpdateCallback);
            }
            ArrayList arrayList = new ArrayList();
            int i = this.mOldListSize;
            int i2 = this.mNewListSize;
            for (int size = this.mSnakes.size() - 1; size >= 0; size--) {
                Snake snake = this.mSnakes.get(size);
                int i3 = snake.size;
                int i4 = snake.x + i3;
                int i5 = snake.y + i3;
                if (i4 < i) {
                    dispatchRemovals(arrayList, batchingListUpdateCallback, i4, i - i4, i4);
                }
                if (i5 < i2) {
                    diffResult = this;
                    diffResult.dispatchAdditions(arrayList, batchingListUpdateCallback, i4, i2 - i5, i5);
                } else {
                    diffResult = this;
                }
                for (int i6 = i3 - 1; i6 >= 0; i6--) {
                    if ((diffResult.mOldItemStatuses[snake.x + i6] & 31) == 2) {
                        batchingListUpdateCallback.onChanged(snake.x + i6, 1, diffResult.mCallback.getChangePayload(snake.x + i6, snake.y + i6));
                    }
                }
                i = snake.x;
                i2 = snake.y;
            }
            batchingListUpdateCallback.dispatchLastEvent();
        }

        private static PostponedUpdate removePostponedUpdate(List<PostponedUpdate> list, int i, boolean z) {
            int size = list.size() - 1;
            while (size >= 0) {
                PostponedUpdate postponedUpdate = list.get(size);
                if (postponedUpdate.posInOwnerList == i && postponedUpdate.removal == z) {
                    list.remove(size);
                    while (size < list.size()) {
                        list.get(size).currentPos += z ? 1 : -1;
                        size++;
                    }
                    return postponedUpdate;
                }
                size--;
            }
            return null;
        }

        private void dispatchAdditions(List<PostponedUpdate> list, ListUpdateCallback listUpdateCallback, int i, int i2, int i3) {
            if (!this.mDetectMoves) {
                listUpdateCallback.onInserted(i, i2);
                return;
            }
            for (int i4 = i2 - 1; i4 >= 0; i4--) {
                int i5 = i3 + i4;
                int i6 = this.mNewItemStatuses[i5];
                int i7 = i6 & 31;
                if (i7 == 0) {
                    listUpdateCallback.onInserted(i, 1);
                    Iterator<PostponedUpdate> it = list.iterator();
                    while (it.hasNext()) {
                        it.next().currentPos++;
                    }
                } else if (i7 == 4 || i7 == 8) {
                    int i8 = i6 >> 5;
                    listUpdateCallback.onMoved(removePostponedUpdate(list, i8, true).currentPos, i);
                    if (i7 == 4) {
                        listUpdateCallback.onChanged(i, 1, this.mCallback.getChangePayload(i8, i5));
                    }
                } else if (i7 == 16) {
                    list.add(new PostponedUpdate(i5, i, false));
                } else {
                    throw new IllegalStateException("unknown flag for pos " + i5 + " " + Long.toBinaryString(i7));
                }
            }
        }

        private void dispatchRemovals(List<PostponedUpdate> list, ListUpdateCallback listUpdateCallback, int i, int i2, int i3) {
            if (!this.mDetectMoves) {
                listUpdateCallback.onRemoved(i, i2);
                return;
            }
            for (int i4 = i2 - 1; i4 >= 0; i4--) {
                int i5 = i3 + i4;
                int i6 = this.mOldItemStatuses[i5];
                int i7 = i6 & 31;
                if (i7 == 0) {
                    listUpdateCallback.onRemoved(i + i4, 1);
                    Iterator<PostponedUpdate> it = list.iterator();
                    while (it.hasNext()) {
                        it.next().currentPos--;
                    }
                } else if (i7 == 4 || i7 == 8) {
                    int i8 = i6 >> 5;
                    PostponedUpdate removePostponedUpdate = removePostponedUpdate(list, i8, false);
                    listUpdateCallback.onMoved(i + i4, removePostponedUpdate.currentPos - 1);
                    if (i7 == 4) {
                        listUpdateCallback.onChanged(removePostponedUpdate.currentPos - 1, 1, this.mCallback.getChangePayload(i5, i8));
                    }
                } else if (i7 == 16) {
                    list.add(new PostponedUpdate(i5, i + i4, true));
                } else {
                    throw new IllegalStateException("unknown flag for pos " + i5 + " " + Long.toBinaryString(i7));
                }
            }
        }

        List<Snake> getSnakes() {
            return this.mSnakes;
        }
    }

    private static class PostponedUpdate {
        int currentPos;
        int posInOwnerList;
        boolean removal;

        public PostponedUpdate(int i, int i2, boolean z) {
            this.posInOwnerList = i;
            this.currentPos = i2;
            this.removal = z;
        }
    }
}
