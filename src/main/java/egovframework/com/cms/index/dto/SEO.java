package egovframework.com.cms.index.dto;

import egovframework.com.cms.menu.model.Menu;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class SEO {
    private String title;
    private String type;
    private String keywords = "";
    private String description;
    private Menu currentMenu;
    private List<Menu> menuList;
    private String location;

    public String getLocation(String divider, String cssClass) {
        divider = StringUtils.defaultString(divider);
        if (StringUtils.isNotBlank(divider)) {
            divider = "<span class=\"location-divider\">" + divider + "</span>";
        }
        cssClass = StringUtils.defaultString(cssClass);
        if (StringUtils.isNotBlank(divider)) {
            divider = " " + divider;
        }
        Menu home = new Menu();
        home.setMenuName("Home");
        home.setMenuLink("/");
        home.setMenuDepth(1);
        home.setMenuOrder(0);
        home.setMenuId("home");
        if (this.menuList == null || this.menuList.isEmpty() || this.currentMenu == null || this.currentMenu.getMenuParent() == null) {
            return "<ul class=\"menu-location nav-location seo-location breadcrumb" + cssClass + "\"><li><a href=\"/\">" + home.getMenuName() + "</a></li><li>" + divider + this.title + "</li></ul>";
        }
        for (Menu menu : menuList) {
            if (menu.isHome()) {
                home = menu;
            }
        }
        List<Menu> result = new ArrayList<>();
        result.add(currentMenu);
        this.traverseMenuList(result, menuList, currentMenu);
        result.add(home);
        Collections.reverse(result);
        StringBuilder sb = new StringBuilder(500);
        sb.append("<ul class=\"menu-location nav-location seo-location breadcrumb");
        sb.append(cssClass);
        sb.append("\">");
        for (Menu menu : result) {
            sb.append("<li>");
            if (!menu.isHome()) {
                sb.append(divider);
            }
            sb.append("<a href=\"");
            sb.append(menu.getMenuLink());
            sb.append("\">");
            sb.append(menu.getMenuName());
            sb.append("</a>");
            sb.append("</li>");
        }
        sb.append("</ul>");

        this.location = sb.toString();
        return location;
    }

    public String getLocation() {
        return this.getLocation(null, null);
    }

    private void traverseMenuList(List<Menu> result, List<Menu> menuList, Menu currentMenu) {
        for (Menu parentCandidate : menuList) {
            Menu findMenu = this.findMenu(menuList, parentCandidate.getMenuId());
            if (currentMenu.getMenuParent() != null && currentMenu.getMenuParent().equals(parentCandidate)) {
                result.add(findMenu);
            }
            if (currentMenu.getMenuParent() != null && currentMenu.getMenuParent().equals(parentCandidate)
                    && findMenu.getMenuDepth() > 1) {
                this.traverseMenuList(result, menuList, parentCandidate);
            }
            if (currentMenu.getMenuParent() != null && currentMenu.getMenuParent().equals(parentCandidate)
                    && !(findMenu.getMenuDepth() > 1)) {
                break;
            }
        }
    }

    private Menu findMenu(List<Menu> menuList, String menuId) {
        for (Menu menu : menuList) {
            if (menu.getMenuId().equals(menuId)) {
                return menu;
            }
        }
        return null;
    }

    private List<Menu> findXDepthMenus(int xDepth, String parentId, List<Menu> menuList) {
        List<Menu> xDepthMenuList = new ArrayList<>();
        if (parentId == null) {
            parentId = "";
        }
        for (Menu menu : menuList) {
            if ("public".equals(menu.getMenuStatus())
                    && (menu.getMenuDepth() == 1 && xDepth == 1 && "link".equals(menu.getMenuType()))) {
                xDepthMenuList.add(menu);
            }
            Menu parent = menu.getMenuParent();
            if ("public".equals(menu.getMenuStatus())
                    && !((menu.getMenuDepth() == 1 && xDepth == 1 && "link".equals(menu.getMenuType())))
                    && (menu.getMenuDepth() == xDepth && menu.getMenuParent() != null)
                    && parent.getMenuId() != null && parentId.equals(parent.getMenuId())) {
                xDepthMenuList.add(menu);
            }
        }
        return xDepthMenuList;
    }
}
