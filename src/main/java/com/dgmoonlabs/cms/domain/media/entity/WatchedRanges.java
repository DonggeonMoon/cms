package com.dgmoonlabs.cms.domain.media.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

// 실제 재생한 구간을 합쳐 두어 다시 보기나 건너뛰기로 진도가 부풀지 않게 한다.
public final class WatchedRanges {
    private final List<int[]> ranges;

    private WatchedRanges(final List<int[]> ranges) {
        this.ranges = ranges;
    }

    public static WatchedRanges parse(final String text) {
        List<int[]> ranges = new ArrayList<>();
        if (text != null && !text.isBlank()) {
            for (String part : text.split(",")) {
                String[] bounds = part.split("-");
                ranges.add(new int[]{Integer.parseInt(bounds[0]), Integer.parseInt(bounds[1])});
            }
        }
        return new WatchedRanges(ranges);
    }

    public WatchedRanges add(final int start, final int end, final int durationSeconds) {
        int from = Math.clamp(start, 0, durationSeconds);
        int to = Math.clamp(end, 0, durationSeconds);
        if (to <= from) {
            return this;
        }
        List<int[]> all = new ArrayList<>(ranges);
        all.add(new int[]{from, to});
        all.sort(Comparator.comparingInt(range -> range[0]));
        List<int[]> merged = new ArrayList<>();
        for (int[] range : all) {
            int[] last = merged.isEmpty() ? null : merged.getLast();
            if (last != null && range[0] <= last[1]) {
                last[1] = Math.max(last[1], range[1]);
            } else {
                merged.add(new int[]{range[0], range[1]});
            }
        }
        return new WatchedRanges(merged);
    }

    public int totalSeconds() {
        return ranges.stream().mapToInt(range -> range[1] - range[0]).sum();
    }

    public String format() {
        return ranges.stream().map(range -> range[0] + "-" + range[1]).collect(Collectors.joining(","));
    }
}
