package egovframework.com.cms.menu.dto;

import egovframework.com.cms.site.dto.MultiSiteVO;
import egovframework.com.cms.support.Constant;
import egovframework.com.cms.watchdog.aop.WatchDog;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class Menu extends MultiSiteVO implements Serializable {
    @WatchDog
    private String menuId;
    private String menuName = "";
    private String menuPlainName;//메뉴이름에 포함된 특수문자 제거 - 디비컬럼은 없음
    private String menuType = "content";//content|program|link

    private String menuGnbType = "text";//image/text
    private String menuGnbExtOn = "";
    private String menuGnbExtOff = "";
    private String menuSnbType = "text";//image/text
    private String menuSnbExtOn = "";
    private String menuSnbExtOff = "";
    private String menuTitleType = "text";//image/text
    private String menuTitleExt = "";
    private String menuTitleSubText = "";//메뉴타이틀 옆에 작게 표시되는 설명글 있을 경우 사용

    private String menuLink = "";
    private boolean menuNewWin = false;
    private Menu menuParent;
    private List<Menu> menuChildren;
    private Integer menuDepth = 1;
    private Integer menuOrder = 999;
    private String menuHeader = "header.jsp";
    private String menuTemplate = "";//특정 jsp 파일을 뷰로 지정해서 사용
    private String menuFooter = "footer.jsp";
    private String menuStatus = "public"; //public(공개)|hidden(접근은 가능하지만 메뉴트리에는 안나옴)|locked(접근도 안되고 메뉴에도 안나옴)
    private Date menuRegDate;
    private Date menuLastModified;

    private String menuManager = "";
    private String menuDepartment = "";
    private String menuPhone = "";
    private String menuEmail = "";
    private String menuEtc = "";

    private boolean menuUseSatisfaction;
    private String menuLocation = "";    //메뉴위치

    //--컬럼 없음
    private MultipartFile menuGnbImageOnFile;
    private MultipartFile menuGnbImageOffFile;
    private MultipartFile menuSnbTopFile;
    private MultipartFile menuSnbImageOnFile;
    private MultipartFile menuSnbImageOffFile;
    private MultipartFile menuTitleImageFile;

    //이미지 지울때--컬럼 없음
    private boolean menuGnbImageOnDelete;
    private boolean menuGnbImageOffDelete;
    private boolean menuSnbImageOnDelete;
    private boolean menuSnbImageOffDelete;
    private boolean menuTitleImageDelete;

    //이미지url--컬럼 없음
    private String menuTitleImageUrl = "";
    private String menuGnbImageOnUrl = "";
    private String menuGnbImageOffUrl = "";
    private String menuSnbImageOnUrl = "";
    private String menuSnbImageOffUrl = "";

    public Menu(String menuId, String menuLink) {
        this.menuId = menuId;
        this.menuLink = menuLink;
    }

    public MultipartFile getMenuSnbTopFile() {
        return menuSnbTopFile;
    }

    public void setMenuSnbTopFile(MultipartFile menuSnbTopFile) {
        this.menuSnbTopFile = menuSnbTopFile;
    }

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
            //1차메뉴
            if ((this.getMenuParent() == null || StringUtils.isBlank(this.getMenuParent().getMenuId())) && (menu.getMenuParent() == null || StringUtils.isBlank(menu.getMenuId()))) {
                if (menu.getMenuOrder() < orderMin) {
                    orderMin = menu.getMenuOrder();
                }
                if (menu.getMenuOrder() > orderMax) {
                    orderMax = menu.getMenuOrder();
                }
            } else {
                if (this.getMenuParent() != null && menu.getMenuParent() != null) {
                    if (menu.getMenuParent().getMenuId().equals(this.getMenuParent().getMenuId())) {
                        if (menu.getMenuOrder() < orderMin) {
                            orderMin = menu.getMenuOrder();
                        }
                        if (menu.getMenuOrder() > orderMax) {
                            orderMax = menu.getMenuOrder();
                        }
                    }
                }
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
        if (menuId == null) {
            if (other.menuId != null)
                return false;
        } else if (!menuId.equals(other.menuId))
            return false;
        return true;
    }

    public String getMenuImageUrl(String siteId, String theme, String suffix) {

        StringBuilder sb = new StringBuilder();
        sb.append(Constant.UPLOAD_PATH);
        sb.append("/");
        sb.append(siteId);
        sb.append("/theme/");
        sb.append(theme);
        sb.append("/menu/");
        sb.append(this.getMenuId());
        sb.append(suffix);
        return sb.toString();
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
