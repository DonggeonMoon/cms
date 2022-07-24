package egovframework.com.cms.member.model;

import egovframework.com.cms.group.model.Group;
import egovframework.com.cms.role.model.Role;
import egovframework.com.cms.watchdog.aop.WatchDog;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class Member {
    @WatchDog
    private String memberId = "";
    private String memberPassword = "";
    private String memberPasswordCheck = "";
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
    private String memberLevel = "BASIC";

    private String memberTel1 = "";
    private String memberTel2 = "";
    private String memberTel3 = "";
    private String memberMobile1 = "";
    private String memberMobile2 = "";
    private String memberMobile3 = "";

    private String memberDupInfo = "";

    private String memberSignUpKey;
    private boolean memberSignUpKeyChecked;

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

    public boolean isGuest() {
        return true;
    }
}
