package com.kai.kairpc.core.governance;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * SlidingTimeWindow implement based on RingBuffer and TS(timestamp).
 * Use TS/1000->SecondNumber to mapping an index slot in a RingBuffer.
 */
@Slf4j
@ToString
public class SlidingTimeWindow {

    public static final int DEFAULT_SIZE = 30;

    // 滑动窗口的大小，默认 30
    private final int size;
    // 滑动窗口的 RingBuffer
    private final RingBuffer ringBuffer;
    // 滑动窗口内所有数据点的累加值
    private int sum = 0;
    // 当前时间戳对应的 RingBuffer index
    private int currIndex = -1;
    // 开始时间戳
    private long startTs = -1L;
    // 当前时间戳
    private long currTs = -1L;

    public SlidingTimeWindow() {
        this(DEFAULT_SIZE);
    }

    public SlidingTimeWindow(int size) {
        this.size = size;
        this.ringBuffer = new RingBuffer(this.size);
    }

    /**
     * record current ts millis.
     *
     * @param millis 当前时间戳（System.currentTimeMillis()）
     */
    public synchronized void record(long millis) {
        log.debug("window before: {}", this);
        log.debug("window.record({})", millis);
        long ts = millis / 1000;
        if (startTs == -1L) {
            initRing(ts);
        } else {
            if (ts == currTs) {
                log.debug("window ts: {}, currTs: {}, size: {}", ts, currTs, size);
                this.ringBuffer.incr(currIndex, 1);
            } else if (ts > currTs && ts < currTs + size) {
                int offset = (int) (ts - currTs);
                log.debug("window ts: {}, currTs: {}, size: {}, offset: {}", ts, currTs, size, offset);
                this.ringBuffer.reset(currIndex + 1, offset);
                this.ringBuffer.incr(currIndex + offset, 1);
                currTs = ts;
                currIndex = (currIndex + offset) % size;
            } else if (ts >= currTs + size) {
                log.debug("window ts: {}, currTs: {}, size: {}", ts, currTs, size);
                this.ringBuffer.reset();
                initRing(ts);
            }
        }
        this.sum = this.ringBuffer.sum();
        log.debug("window after: " + this);
    }

    private void initRing(long ts) {
        log.debug("window initRing ts: {}", ts);
        this.startTs = ts;
        this.currTs = ts;
        this.currIndex = 0;
        this.ringBuffer.incr(0, 1);
    }

    public int getSum() {
        return sum;
    }

    public int calcSum() {
        long ts = System.currentTimeMillis() / 1000;
        if (ts > currTs && ts < currTs + size) {
            int offset = (int) (ts - currTs);
            log.debug("window ts: {}, currTs: {}, size: {}, offset: {}", ts, currTs, size, offset);
            this.ringBuffer.reset(currIndex + 1, offset);
            currTs = ts;
            currIndex = (currIndex + offset) % size;
        } else if (ts >= currTs + size) {
            log.debug("window ts: {}, currTs: {}, size: {}", ts, currTs, size);
            this.ringBuffer.reset();
            initRing(ts);
        }
        log.debug("calc sum for window: {}", this);
        return this.ringBuffer.sum();
    }

    public int getSize() {
        return size;
    }

    public RingBuffer getRingBuffer() {
        return ringBuffer;
    }

    public int getCurrIndex() {
        return currIndex;
    }

    public long getStartTs() {
        return startTs;
    }

    public long getCurrTs() {
        return currTs;
    }

}
