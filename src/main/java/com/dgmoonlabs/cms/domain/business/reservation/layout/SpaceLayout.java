package com.dgmoonlabs.cms.domain.business.reservation.layout;

import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;

import java.math.BigDecimal;
import java.util.*;

public record SpaceLayout(int rows, int cols, List<Cell> cells) {
    public static final int MAX_SIZE = 40;

    public enum CellKind {
        SEAT, ROOM, SPACE, AISLE, STAGE, EMPTY;

        public boolean isBookable() {
            return this == SEAT || this == ROOM || this == SPACE;
        }
    }

    public record Cell(String key, int row, int col, CellKind kind, String label, BigDecimal price, Integer capacity) {
    }

    public SpaceLayout {
        cells = cells == null ? List.of() : List.copyOf(cells);
    }

    public void validate() {
        if (rows < 1 || cols < 1 || rows > MAX_SIZE || cols > MAX_SIZE) {
            throw new BusinessException(ErrorCode.INVALID_LAYOUT);
        }
        Set<String> keys = new HashSet<>();
        for (Cell cell : cells) {
            if (cell.key() == null || cell.key().isBlank() || !keys.add(cell.key()) || cell.kind() == null
                    || cell.row() < 0 || cell.row() >= rows || cell.col() < 0 || cell.col() >= cols) {
                throw new BusinessException(ErrorCode.INVALID_LAYOUT);
            }
        }
    }

    public List<Cell> bookable(final Collection<String> keys) {
        Map<String, Cell> byKey = new HashMap<>();
        cells.stream().filter(cell -> cell.kind().isBookable()).forEach(cell -> byKey.put(cell.key(), cell));
        List<Cell> selected = new ArrayList<>();
        for (String key : new LinkedHashSet<>(keys)) {
            Cell cell = byKey.get(key);
            if (cell == null) {
                throw new BusinessException(ErrorCode.INVALID_CELL_SELECTION);
            }
            selected.add(cell);
        }
        if (selected.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_CELL_SELECTION);
        }
        return selected;
    }
}
