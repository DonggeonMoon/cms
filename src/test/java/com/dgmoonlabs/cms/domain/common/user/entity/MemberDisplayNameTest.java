package com.dgmoonlabs.cms.domain.common.user.entity;

import com.dgmoonlabs.cms.domain.common.user.entity.member.Member;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemberDisplayNameTest {
    @Test
    void prefersNicknameThenUsernameThenName() {
        assertThat(Member.builder().nickname("하늘이").username("happybuyer").name("김하늘").build().displayName())
                .isEqualTo("하늘이");
        assertThat(Member.builder().nickname(" ").username("happybuyer").name("김하늘").build().displayName())
                .isEqualTo("happybuyer");
        assertThat(Member.builder().name("김하늘").build().displayName()).isEqualTo("김하늘");
    }
}
