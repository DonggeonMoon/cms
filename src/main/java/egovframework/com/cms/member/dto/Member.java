package egovframework.com.cms.member.dto;

import egovframework.com.cms.capability.dto.Capability;
import egovframework.com.cms.group.dto.Group;
import egovframework.com.cms.role.dto.Role;
import egovframework.com.cms.support.exception.EcmsException;
import egovframework.com.cms.support.util.Utils;
import egovframework.com.cms.watchdog.aop.WatchDog;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jsoup.safety.Whitelist;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class Member implements Serializable {
    @WatchDog
    private String memberId = "";
    private String memberPassword = "";
    private String memberPasswordCheck = "";//컬럼 없음
    @WatchDog
    private String memberName = "";
    private String memberLastName = "";
    private String memberFirstName = "";
    private String memberEmail = "";
    private boolean memberMailing = true;
    private boolean memberSms = true;
    private boolean memberActive = true;
    private Date memberRegDate;
    @WatchDog
    private Role memberRole;
    @WatchDog
    private String memberLevel = "BASIC";//회원레벨 코드에 있는값이어야 함 MEM_LEVEL

    private String memberTel1 = "";
    private String memberTel2 = "";
    private String memberTel3 = "";
    private String memberMobile1 = "";
    private String memberMobile2 = "";
    private String memberMobile3 = "";

    private String memberDupInfo = "";//GPIN, 실명인증 매칭 키값

    private String memberSignUpKey;//가입신청 후 가입확인 메일 체크용 키
    private boolean memberSignUpKeyChecked;//가입확인메일 체크되었나

    private MultipartFile memberProfileImage;
    private boolean memberProfileImageRemove;

    private List<Group> groups;

    private Date memberLastLoginDate;

    //추가필드 2014. 8. 7. cwsong
    private String memberTel;//쪼개서 넣을때말고 사용
    private String memberMobile;//쪼개서 넣을때말고 사용
    private String memberGender;//성별
    private String memberBirthday;//생년월일
    private boolean memberBirthdaySolar = true;//양력/음력
    private String memberOrganization;//소속
    private String memberDepartment;
    private String memberPosition;
    private String memberFax;
    private String memberAddress;
    private String memberAddressSub;
    private String memberCityArea;//시군구할때 구 20151202 추가
    private String memberCityState;
    private String memberCountry;
    private String memberZipCode;

    private String memberSns;
    private String memberSnsId;
    private String memberSnsHome;

    //가입2년 체크 필드
    private Date termCheckDate;//약관 동의 일시 = 최초가입시는 가입일시와 동일
    private boolean termCheckEmailSended;//약관 동의 안내 메일 발송 여부

    //캡차에러필드용
    private String memberCaptcha;

    //비밀번호 마지막 변경일시
    private Date memberPWLastUpdated;

    //비밀번호 틀린 횟수
    private int wrongPWCount;

    //로그인 제한 날짜
    private Date loginLimitDate;

    private boolean validPw = true;

    private final int maximumInvalidCount = 5;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((memberId == null) ? 0 : memberId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Member other = (Member) obj;
        if (memberId == null) {
            if (other.memberId != null)
                return false;
        } else if (!memberId.equals(other.memberId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    public boolean isMaximumInvalid() {
        return maximumInvalidCount <= wrongPWCount;
    }

    public boolean isLoginLimit() {
        if (loginLimitDate == null) {
            return false;
        }

        return new Date().before(loginLimitDate);
    }

    public String getRemainLoginLimitTime() {
        if (loginLimitDate == null) {
            return "0:0";
        }

        Date now = new Date();
        long remainSeconds = (loginLimitDate.getTime() - now.getTime()) / 1000;
        if (remainSeconds < 60) {
            return String.format("%d초", remainSeconds);
        }

        return String.format("%d분 %d초", Long.valueOf(remainSeconds / 60).intValue(), Long.valueOf(remainSeconds % 60).intValue());
    }

    public void increaseWrongPwCount() {
        wrongPWCount++;
        validPw = false;
    }

    public String getMemberTel123() {
        if (StringUtils.isNotBlank(this.getMemberTel1())
                && StringUtils.isNotBlank(this.getMemberTel2())
                && StringUtils.isNotBlank(this.getMemberTel3())) {
            return this.getMemberTel1() + "-" + this.getMemberTel2() + "-" + this.getMemberTel3();
        } else {
            return null;
        }
    }

    public String getMemberMobile123() {
        if (StringUtils.isNotBlank(this.getMemberMobile1())
                && StringUtils.isNotBlank(this.getMemberMobile2())
                && StringUtils.isNotBlank(this.getMemberMobile3())) {
            return this.getMemberMobile1() + "-" + this.getMemberMobile2() + "-" + this.getMemberMobile3();
        } else {
            return null;
        }
    }

    public String getMemberFullName() {
        if (StringUtils.isNotBlank(this.getMemberName())) {
            return this.getMemberName();
        } else {
            if (StringUtils.isNotBlank(this.getMemberLastName()) && StringUtils.isNotBlank(this.getMemberFirstName())) {
                return this.getMemberFirstName() + " " + this.getMemberLastName();
            } else {
                return StringUtils.defaultString(StringUtils.defaultString(this.getMemberFirstName()) + StringUtils.defaultString(this.getMemberLastName()), "Name is required.");
            }
        }
    }

    public boolean isSocialMember() {
        return StringUtils.isNotBlank(this.getMemberSns());
    }

    public boolean isGuest() {
        //회원 아이디 없으면
        if (StringUtils.isBlank(this.getMemberId())) {
            return true;
        }
        //회원 아이디 있는데 guest_ 로 시작하면
        if (StringUtils.isNotBlank(this.getMemberId()) && this.getMemberId().startsWith("guest_")) {
            return true;
        }
        if (this.getMemberRole() != null) {
            if ("ROLE_GUEST".equals(this.getMemberRole().getRoleCode())) {
                return true;
            }
        } else {
            throw new EcmsException("[ECMS] CurrentUser is NULL.");
        }
        return false;
    }

    public boolean hasCapability(String capId) {
        if (this.getMemberRole() != null) {
            if (this.getMemberRole().isRoleAdmin()) {
                return true;
            }
            if (this.getMemberRole().getCapabilityList() != null && !this.getMemberRole().getCapabilityList().isEmpty()) {
                String capIdTarget = capId.trim();
                for (Capability cap : this.getMemberRole().getCapabilityList()) {
                    if (capIdTarget.equals(cap.getCapId())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void sanitizeValues() {
        this.setMemberName(Utils.getJsoupFilteredText(this.getMemberName(), Whitelist.none(), false, false));
        this.setMemberLastName(Utils.getJsoupFilteredText(this.getMemberLastName(), Whitelist.none(), false, false));
        this.setMemberFirstName(Utils.getJsoupFilteredText(this.getMemberFirstName(), Whitelist.none(), false, false));

        this.setMemberAddress(Utils.getJsoupFilteredText(this.getMemberAddress(), Whitelist.none(), false, false));
        this.setMemberAddressSub(Utils.getJsoupFilteredText(this.getMemberAddressSub(), Whitelist.none(), false, false));
        this.setMemberFax(Utils.getJsoupFilteredText(this.getMemberFax(), Whitelist.none(), false, false));
        this.setMemberOrganization(Utils.getJsoupFilteredText(this.getMemberOrganization(), Whitelist.none(), false, false));
        this.setMemberDepartment(Utils.getJsoupFilteredText(this.getMemberDepartment(), Whitelist.none(), false, false));
        this.setMemberCityState(Utils.getJsoupFilteredText(this.getMemberCityState(), Whitelist.none(), false, false));
        this.setMemberCountry(Utils.getJsoupFilteredText(this.getMemberCountry(), Whitelist.none(), false, false));
        this.setMemberZipCode(Utils.getJsoupFilteredText(this.getMemberZipCode(), Whitelist.none(), false, false));
    }
}
