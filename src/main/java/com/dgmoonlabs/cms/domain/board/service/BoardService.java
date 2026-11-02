package com.dgmoonlabs.cms.domain.board.service;

import com.dgmoonlabs.cms.domain.board.dto.BoardResponse;
import com.dgmoonlabs.cms.domain.board.dto.BoardResponse.Permissions;
import com.dgmoonlabs.cms.domain.board.dto.BoardSaveRequest;
import com.dgmoonlabs.cms.domain.board.entity.aritcle.Board;
import com.dgmoonlabs.cms.domain.board.entity.aritcle.BoardConfig;
import com.dgmoonlabs.cms.domain.board.repository.BoardConfigRepository;
import com.dgmoonlabs.cms.domain.board.repository.BoardRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import com.dgmoonlabs.cms.global.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final BoardConfigRepository boardConfigRepository;

    @Transactional(readOnly = true)
    public List<BoardResponse> getBoards() {
        return boardRepository.findAllByDeletedAtIsNullOrderByIdAsc().stream()
                .map(board -> toResponse(board, findConfig(board.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public BoardResponse getBoard(final Long id) {
        Board board = findBoard(id);
        return toResponse(board, findConfig(id));
    }

    @Transactional
    public Long createBoard(final BoardSaveRequest request) {
        if (boardRepository.existsByName(request.name())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        Board board = boardRepository.save(Board.builder()
                .name(request.name())
                .description(request.description())
                .type(request.type())
                .build());
        boardConfigRepository.save(BoardConfig.of(board.getId(), request));
        return board.getId();
    }

    @Transactional
    public void updateBoard(final Long id, final BoardSaveRequest request) {
        if (boardRepository.existsByNameAndIdNot(request.name(), id)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        findBoard(id).update(request.name(), request.description(), request.type());
        findConfig(id).update(request);
    }

    @Transactional
    public void deleteBoard(final Long id) {
        findBoard(id).delete();
    }

    public Permissions permissionsOf(final BoardConfig config) {
        if (CurrentUser.isAdmin()) {
            return new Permissions(true, true, true, true);
        }
        if (CurrentUser.isMember()) {
            return new Permissions(config.isMemberAccessible(), config.isMemberReadable(), config.isMemberWritable(), false);
        }
        return new Permissions(config.isGuestAccessible(), config.isGuestReadable(), config.isGuestWritable(), false);
    }

    Board findBoard(final Long id) {
        return boardRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(BusinessException::notFound);
    }

    BoardConfig findConfig(final Long boardId) {
        return boardConfigRepository.findByBoardId(boardId).orElseThrow(BusinessException::notFound);
    }

    private BoardResponse toResponse(final Board board, final BoardConfig config) {
        return BoardResponse.of(board, config, permissionsOf(config));
    }
}
