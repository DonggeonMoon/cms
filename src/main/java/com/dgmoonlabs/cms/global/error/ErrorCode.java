package com.dgmoonlabs.cms.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "대상을 찾을 수 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),

    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),
    ACCOUNT_LOCKED(HttpStatus.LOCKED, "로그인 시도 횟수를 초과해 계정이 잠겼습니다. 관리자에게 문의하세요."),
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "비밀번호 확인이 일치하지 않습니다."),
    GUEST_CONTACT_REQUIRED(HttpStatus.BAD_REQUEST, "연락받을 연락처나 이메일을 입력해 주세요."),
    GUEST_WRITE_NOT_ALLOWED(HttpStatus.FORBIDDEN, "로그인한 회원만 등록할 수 있습니다. 로그인 후 다시 시도해 주세요."),
    PRIVACY_CONSENT_REQUIRED(HttpStatus.BAD_REQUEST, "개인정보 수집·이용에 동의해야 가입할 수 있습니다."),
    WRONG_PASSWORD(HttpStatus.BAD_REQUEST, "현재 비밀번호가 올바르지 않습니다."),
    REAUTHENTICATION_REQUIRED(HttpStatus.FORBIDDEN, "보안을 위해 비밀번호를 다시 입력해 주세요."),
    REAUTHENTICATION_FAILED(HttpStatus.BAD_REQUEST, "비밀번호가 올바르지 않습니다."),

    CODE_HAS_CHILDREN(HttpStatus.CONFLICT, "하위 코드가 있어 삭제할 수 없습니다."),
    MENU_HAS_CHILDREN(HttpStatus.CONFLICT, "하위 메뉴가 있어 삭제할 수 없습니다."),
    MENU_BUILT_IN(HttpStatus.CONFLICT, "기능 메뉴 아래에는 하위 메뉴를 둘 수 없습니다."),
    DUPLICATE_CONFIG_KEY(HttpStatus.CONFLICT, "이미 등록된 설정 키입니다."),
    DUPLICATE_API_KEY_NAME(HttpStatus.CONFLICT, "이미 등록된 API 키 이름입니다."),
    API_KEY_IN_USE(HttpStatus.CONFLICT, "지도 설정 등에서 사용 중인 API 키는 삭제할 수 없습니다. 사용하는 곳에서 다른 키로 바꾼 뒤 삭제하세요."),
    API_KEY_NOT_FOUND(HttpStatus.BAD_REQUEST, "선택한 API 키를 찾을 수 없습니다. API 키 관리에서 등록한 키를 선택하세요."),
    INVALID_PERIOD(HttpStatus.BAD_REQUEST, "종료 일시는 시작 일시보다 뒤여야 합니다."),
    FEATURE_DISABLED(HttpStatus.NOT_FOUND, "현재 사용할 수 없는 기능입니다."),
    MAP_LOCATION_REQUIRED(HttpStatus.BAD_REQUEST, "오시는 길을 사용하려면 본점 주소와 좌표(위도·경도)를 입력해야 합니다."),
    BRANCHES_DISABLED(HttpStatus.CONFLICT, "지점 안내가 사용 중지되어 있습니다. 지점 안내를 켠 뒤 등록해 주세요."),
    MENU_FEATURE_DISABLED(HttpStatus.CONFLICT, "사용 중지된 기능은 메뉴로 설정할 수 없습니다. 먼저 기능을 켜 주세요."),

    BOARD_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "이 게시판에 대한 권한이 없습니다."),
    ARTICLE_PASSWORD_MISMATCH(HttpStatus.FORBIDDEN, "게시글 비밀번호가 올바르지 않습니다."),
    GUEST_PASSWORD_REQUIRED(HttpStatus.BAD_REQUEST, "비회원은 작성자 이름과 비밀번호를 입력해야 합니다."),
    COMMENT_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "댓글을 지원하지 않는 게시판입니다."),
    FEATURE_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "이 게시판에서 지원하지 않는 기능입니다."),
    ALREADY_ANSWERED(HttpStatus.CONFLICT, "이미 답변이 등록되었습니다."),

    PRODUCT_NOT_AVAILABLE(HttpStatus.CONFLICT, "판매 중인 상품이 아닙니다."),
    OUT_OF_STOCK(HttpStatus.CONFLICT, "재고가 부족합니다."),
    PROGRAM_NOT_OPEN(HttpStatus.CONFLICT, "신청 기간이 아니거나 신청할 수 없는 프로그램입니다."),
    PROGRAM_FULL(HttpStatus.CONFLICT, "신청 가능 인원을 초과했습니다."),
    INVALID_STATUS_TRANSITION(HttpStatus.CONFLICT, "현재 상태에서는 처리할 수 없습니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "결제 금액이 주문 금액과 일치하지 않습니다."),
    ALREADY_SUBMITTED(HttpStatus.CONFLICT, "이미 참여했습니다."),

    EMPTY_FILE(HttpStatus.BAD_REQUEST, "업로드할 파일이 비어 있습니다."),
    FILE_EXTENSION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "허용되지 않는 파일 형식입니다."),
    FILE_STORAGE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 저장에 실패했습니다.");

    private final HttpStatus status;
    private final String message;
}
