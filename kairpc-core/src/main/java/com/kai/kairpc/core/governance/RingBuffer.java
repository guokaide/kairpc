package com.kai.kairpc.core.governance;

import lombok.ToString;

/**
 * 环形缓冲区：用于存储窗口内的数据点
 */
@ToString
public class RingBuffer {

    final int size;
    final int[] ring;

    public RingBuffer(int size) {
        // check size > 0
        this.size = size;
        this.ring = new int[this.size];
    }

    public int sum() {
        int sum = 0;
        for (int i = 0; i < this.size; i++) {
            sum += ring[i];
        }
        return sum;
    }

    public void reset() {
        for (int i = 0; i < this.size; i++) {
            ring[i] = 0;
        }
    }

    public void reset(int index, int step) {
        for (int i = index; i < index + step; i++) {
            ring[i % this.size] = 0;
        }
    }

    public void incr(int index, int delta) {
        ring[index % this.size] += delta;
    }
}
