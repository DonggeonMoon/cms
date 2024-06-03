package com.dgmoonlabs.cms.domain.common.user.service;

import com.dgmoonlabs.cms.domain.common.user.dto.MemberJoinRequest;
import com.dgmoonlabs.cms.domain.common.user.dto.MemberModifyRequest;
import com.dgmoonlabs.cms.domain.common.user.entity.member.Member;
import com.dgmoonlabs.cms.domain.common.user.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    public Long join(MemberJoinRequest request) {
        return memberRepository.save(request.toEntity())
                .getId();
    }

    public void modify(MemberModifyRequest request) {
        Member member = memberRepository.findById(request.getId()).orElseThrow(RuntimeException::new);
        member.update(request);
    }

    public void quit(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(RuntimeException::new);
        memberRepository.delete(member);
    }
}
