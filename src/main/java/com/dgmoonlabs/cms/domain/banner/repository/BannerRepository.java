package com.dgmoonlabs.cms.domain.banner.repository;


import com.dgmoonlabs.cms.domain.banner.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BannerRepository extends JpaRepository<Banner, Long>, BannerCustomRepository {
}
