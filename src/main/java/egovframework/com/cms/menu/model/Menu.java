package egovframework.com.cms.menu.model;

import egovframework.com.cms.support.Constant;
import egovframework.com.cms.watchdog.aop.WatchDog;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class Menu {
    @WatchDog
    private String menuId;
    private String menuName = "";
    private String menuPlainName;
    private String menuType = "content";

    private String menuGnbType = "text";
    private String menuGnbExtOn = "";
    private String menuGnbExtOff = "";
    private String menuSnbType = "text";
    private String menuSnbExtOn = "";
    private String menuSnbExtOff = "";
    private String menuTitleType = "text";
    private String menuTitleExt = "";
    private String menuTitleSubText = "";

    private String menuLink = "";
    private boolean menuNewWin = false;
    private Menu menuParent;
    private List<Menu> menuChildren;
    private Integer menuDepth = 1;
    private Integer menuOrder = 999;
    private String menuHeader = "header.jsp";
    private String menuTemplate = "";
    private String menuFooter = "footer.jsp";
    private String menuStatus = "public";
    private Date menuRegDate;
    private Date menuLastModified;

    private String menuManager = "";
    private String menuDepartment = "";
    private String menuPhone = "";
    private String menuEmail = "";
    private String menuEtc = "";

    private boolean menuUseSatisfaction;
    private String menuLocation = "";

    private MultipartFile menuGnbImageOnFile;
    private MultipartFile menuGnbImageOffFile;
    private MultipartFile menuSnbTopFile;
    private MultipartFile menuSnbImageOnFile;
    private MultipartFile menuSnbImageOffFile;
    private MultipartFile menuTitleImageFile;

    private boolean menuGnbImageOnDelete;
    private boolean menuGnbImageOffDelete;
    private boolean menuSnbImageOnDelete;
    private boolean menuSnbImageOffDelete;
    private boolean menuTitleImageDelete;

    private String menuTitleImageUrl = "";
    private String menuGnbImageOnUrl = "";
    private String menuGnbImageOffUrl = "";
    private String menuSnbImageOnUrl = "";
    private String menuSnbImageOffUrl = "";

    public static final int ONLY_CHILD = -1;
    public static final int FIRST_CHILD = 0;
    public static final int MIDDLE_CHILD = 5;
    public static final int LAST_CHILD = 9;

    public int getMenuPosition(List<Menu> menuList) {
        int orderMin = 999;
        int orderMax = 0;

        for (Menu menu : menuList) {
            if (!"public".equals(menu.getMenuStatus())) {
                continue;
            }
            if ((this.getMenuParent() == null || StringUtils.isBlank(this.getMenuParent().getMenuId()))
                    && (menu.getMenuParent() == null || StringUtils.isBlank(menu.getMenuId()))
                    && menu.getMenuOrder() < orderMin
            ) {
                orderMin = menu.getMenuOrder();
            }
            if ((this.getMenuParent() == null || StringUtils.isBlank(this.getMenuParent().getMenuId()))
                    && (menu.getMenuParent() == null || StringUtils.isBlank(menu.getMenuId()))
                    && menu.getMenuOrder() > orderMax
            ) {
                orderMax = menu.getMenuOrder();
            }
            if (!((this.getMenuParent() == null || StringUtils.isBlank(this.getMenuParent().getMenuId()))
                    && (menu.getMenuParent() == null || StringUtils.isBlank(menu.getMenuId())))
                    && getMenuParent() != null && menu.getMenuParent() != null
                    && menu.getMenuParent().getMenuId().equals(getMenuParent().getMenuId())
                    && menu.getMenuOrder() < orderMin
            ) {
                orderMin = menu.getMenuOrder();
            }
            if (!((this.getMenuParent() == null || StringUtils.isBlank(this.getMenuParent().getMenuId()))
                    && (menu.getMenuParent() == null || StringUtils.isBlank(menu.getMenuId())))
                    && this.getMenuParent() != null && menu.getMenuParent() != null
                    && menu.getMenuParent().getMenuId().equals(this.getMenuParent().getMenuId())
                    && menu.getMenuOrder() > orderMax
            ) {
                orderMax = menu.getMenuOrder();
            }
        }
        if (this.getMenuOrder() == orderMin && this.getMenuOrder() == orderMax) {
            return Menu.ONLY_CHILD;
        } else if (this.getMenuOrder() == orderMin && this.getMenuOrder() != orderMax) {
            return Menu.FIRST_CHILD;
        } else if (this.getMenuOrder() != orderMin && this.getMenuOrder() == orderMax) {
            return Menu.LAST_CHILD;
        } else {
            return Menu.MIDDLE_CHILD;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((menuId == null) ? 0 : menuId.hashCode());
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
        Menu other = (Menu) obj;
        if (menuId == null && other.menuId != null) {
            return false;
        }
        if (menuId == null) {
            return true;
        }
        return menuId.equals(other.menuId);
    }

    public String getMenuImageUrl(String siteId, String theme, String suffix) {
        return Constant.UPLOAD_PATH + "/" + siteId + "/theme/" + theme + "/menu/" + this.getMenuId() + suffix;
    }

    public boolean isHome() {
        return this.getMenuOrder() == 0 && this.getMenuDepth() == 1;
    }

    public Menu populateMenuImageUrl(String siteId, String theme, boolean forceTextMenu) {
        if (forceTextMenu) {
            this.setMenuGnbType("text");
            this.setMenuSnbType("text");
            this.setMenuTitleType("text");
        }
        if ("image".equals(this.getMenuGnbType())) {
            this.setMenuGnbImageOnUrl(this.getMenuImageUrl(siteId, theme, this.getMenuGnbExtOn()));
            this.setMenuGnbImageOffUrl(this.getMenuImageUrl(siteId, theme, this.getMenuGnbExtOff()));
        }
        if ("image".equals(this.getMenuSnbType())) {
            this.setMenuSnbImageOnUrl(this.getMenuImageUrl(siteId, theme, this.getMenuSnbExtOn()));
            this.setMenuSnbImageOffUrl(this.getMenuImageUrl(siteId, theme, this.getMenuSnbExtOff()));
        }
        if ("image".equals(this.getMenuTitleType())) {
            this.setMenuTitleImageUrl(this.getMenuImageUrl(siteId, theme, this.getMenuTitleExt()));
        }
        return this;
    }
}
