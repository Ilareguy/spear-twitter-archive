package com.ilareguy.spear.twitter;

import com.ilareguy.spear.twitter.data.User;

@SuppressWarnings("WeakerAccess")
public abstract class UserPicturesURLParser {

    // See https://developer.twitter.com/en/docs/accounts-and-users/user-profile-images-and-banners

    // Profile picture variants
    public static final int VARIANT_PROFILE_NORMAL = 0; // 48x48
    public static final int VARIANT_PROFILE_BIGGER = 1; // 73x73
    public static final int VARIANT_PROFILE_MINI = 2; // 24x24
    public static final int VARIANT_PROFILE_ORIGINAL = 3; // original, biggest

    // Profile banner variants
    public static final int VARIANT_BANNER_1500_500 = 0; // 1500x500
    public static final int VARIANT_BANNER_600_200 = 1; // 600x200
    public static final int VARIANT_BANNER_300_100 = 2; // 300x100
    public static final int VARIANT_BANNER_WEB = 3; // 520x260
    public static final int VARIANT_BANNER_WEB_RETINA = 4; // 1040x520
    public static final int VARIANT_BANNER_IPAD = 5; // 626x313
    public static final int VARIANT_BANNER_IPAD_RETINA = 6; // 1252x626
    public static final int VARIANT_BANNER_MOBILE = 7; // 320x160
    public static final int VARIANT_BANNER_MOBILE_RETINA = 8; // 640x320
    public static final int VARIANT_BANNER_ORIGINAL = 9;

    public static String getProfilePictureVariantURL(User for_user, int variant) {
        final String default_url = for_user.getProfileImageDefaultUrl();
        final String file_extension = default_url.substring(default_url.lastIndexOf("."));
        final String base_url = default_url.substring(0, default_url.lastIndexOf("_"));
        String variant_str = "";

        switch (variant) {
            case VARIANT_PROFILE_NORMAL:
                variant_str = "_normal";
                break;
            case VARIANT_PROFILE_BIGGER:
                variant_str = "_bigger";
                break;
            case VARIANT_PROFILE_MINI:
                variant_str = "_mini";
                break;
            case VARIANT_PROFILE_ORIGINAL:
                variant_str = "";
                break;
        }

        return (base_url + variant_str + file_extension);
    }

    public static String getProfileBannerVariantURL(User user, int variant) {
        final String base_url = user.getProfileBannerDefaultUrl();

        switch (variant) {
            case VARIANT_BANNER_1500_500:
                return (base_url + "/1500x500");
            case VARIANT_BANNER_600_200:
                return (base_url + "/600x200");
            case VARIANT_BANNER_300_100:
                return (base_url + "/300x100");
            case VARIANT_BANNER_WEB:
                return (base_url + "/web");
            case VARIANT_BANNER_WEB_RETINA:
                return (base_url + "/web_retina");
            case VARIANT_BANNER_IPAD:
                return (base_url + "/ipad");
            case VARIANT_BANNER_IPAD_RETINA:
                return (base_url + "/ipad_retina");
            case VARIANT_BANNER_MOBILE:
                return (base_url + "/mobile");
            case VARIANT_BANNER_MOBILE_RETINA:
                return (base_url + "/mobile_retina");
        }

        return base_url;
    }

}
