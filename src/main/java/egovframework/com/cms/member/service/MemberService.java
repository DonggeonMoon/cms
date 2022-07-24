package egovframework.com.cms.member.service;

import egovframework.com.cms.member.dto.*;
import egovframework.com.cms.member.model.Member;

import java.io.File;
import java.util.Date;
import java.util.List;

public interface MemberService {
    String insertMember(Member member);

    Member getMember(Member member);

    Member getMember(String memberId);

    List<Member> getMemberList(MemberSearch memberSearch);

    int getMemberListTotal(MemberSearch memberSearch);

    int updateMember(Member member);

    int deleteMember(String memberId);

    int deleteMembers(String[] memberIds);

    int updateMemberPassword(Password passwordForm);

    File updateMemberProfileImage(Member member);

    int confirmMemberEmail(String memberId);

    boolean sendSignUpConfirmEmail(Member fromDB);

    boolean confirmMemberEmail(String memberId, String memberSignUpKey);

    Member getRecoverMember(Recover recoverForm);

    void sendPasswordResetMail(Member recoverMember);

    String getAdminEmail();

    int insertMemberDelete(MemberDelete memberDeleteForm);

    MemberDelete getMemberDelete(String memberId);

    List<MemberDelete> getMemberDeleteList(MemberDeleteSearch memberDeleteSearch);

    int getMemberDeleteListTotal(MemberDeleteSearch memberDeleteSearch);

    int updateMemberLastLoginDate(Member member);

    List<Member> getLatestSignUpMemberList(Date date);

    List<MemberDelete> getLatestDeleteMemberList(Date date);

    List<Member> getMemberWithCapList(String capId);

    String[] getEmailArrayFromMemberList(List<Member> memberList);
}
