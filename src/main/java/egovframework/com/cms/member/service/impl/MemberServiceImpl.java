package egovframework.com.cms.member.service.impl;

import egovframework.com.cms.member.dto.*;
import egovframework.com.cms.member.model.Member;
import egovframework.com.cms.member.service.MemberService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Date;
import java.util.List;

@Service
public class MemberServiceImpl implements MemberService {
    @Override
    public String insertMember(Member member) {
        return null;
    }

    @Override
    public Member getMember(Member member) {
        return null;
    }

    @Override
    public Member getMember(String memberId) {
        return null;
    }

    @Override
    public List<Member> getMemberList(MemberSearch memberSearch) {
        return null;
    }

    @Override
    public int getMemberListTotal(MemberSearch memberSearch) {
        return 0;
    }

    @Override
    public int updateMember(Member member) {
        return 0;
    }

    @Override
    public int deleteMember(String memberId) {
        return 0;
    }

    @Override
    public int deleteMembers(String[] memberIds) {
        return 0;
    }

    @Override
    public int updateMemberPassword(Password passwordForm) {
        return 0;
    }

    @Override
    public File updateMemberProfileImage(Member member) {
        return null;
    }

    @Override
    public int confirmMemberEmail(String memberId) {
        return 0;
    }

    @Override
    public boolean sendSignUpConfirmEmail(Member fromDB) {
        return false;
    }

    @Override
    public boolean confirmMemberEmail(String memberId, String memberSignUpKey) {
        return false;
    }

    @Override
    public Member getRecoverMember(Recover recoverForm) {
        return null;
    }

    @Override
    public void sendPasswordResetMail(Member recoverMember) {

    }

    @Override
    public String getAdminEmail() {
        return null;
    }

    @Override
    public int insertMemberDelete(MemberDelete memberDeleteForm) {
        return 0;
    }

    @Override
    public MemberDelete getMemberDelete(String memberId) {
        return null;
    }

    @Override
    public List<MemberDelete> getMemberDeleteList(MemberDeleteSearch memberDeleteSearch) {
        return null;
    }

    @Override
    public int getMemberDeleteListTotal(MemberDeleteSearch memberDeleteSearch) {
        return 0;
    }

    @Override
    public int updateMemberLastLoginDate(Member member) {
        return 0;
    }

    @Override
    public List<Member> getLatestSignUpMemberList(Date date) {
        return null;
    }

    @Override
    public List<MemberDelete> getLatestDeleteMemberList(Date date) {
        return null;
    }

    @Override
    public List<Member> getMemberWithCapList(String capId) {
        return null;
    }

    @Override
    public String[] getEmailArrayFromMemberList(List<Member> memberList) {
        return new String[0];
    }
}
