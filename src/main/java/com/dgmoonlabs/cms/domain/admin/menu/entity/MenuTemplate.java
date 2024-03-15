package com.dgmoonlabs.cms.domain.admin.menu.entity;

import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class MenuTemplate extends Menu {
    private String menuHeader = "header.mustache";

    private String menuTemplate = "";

    private String menuFooter = "footer.mustache";
}
