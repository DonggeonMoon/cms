package com.dgmoonlabs.cms.global.error;

public class GradeRequiredException extends BusinessException {
    private final String message;

    public GradeRequiredException(final String gradeName) {
        super(ErrorCode.GRADE_REQUIRED);
        this.message = gradeName + " 등급 이상 회원만 이용할 수 있습니다.";
    }

    @Override
    public String getMessage() {
        return message;
    }
}
