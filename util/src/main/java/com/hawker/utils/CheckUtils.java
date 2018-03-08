package com.hawker.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class CheckUtils {
    /**
     * 手机号检查
     *
     * @param mobileNo
     * @return
     */
    public static boolean checkMobileNo(String mobileNo) {
        try {
            Long.valueOf(mobileNo);
        } catch (Exception ex) {
            return false;
        }

        if (mobileNo.length() != 11) {
            return false;
        }

        //2.手机号检查
        String[] prefixArray = new String[]{
                "130", "131", "132", "133", "134", "135", "136", "137", "138", "139",
                "150", "151", "152", "153", "154", "155", "156", "157", "158", "159",
                "170", "171", "172", "173", "174", "175", "176", "177", "178", "179",
                "180", "181", "182", "183", "184", "185", "186", "187", "188", "189"};

        for (String prefix : prefixArray) {
            if (mobileNo.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    static class Name {
        private String name;
        private int nameLength;

        Name(String name) {
            this.name = name;
            this.nameLength = getLength();
        }

        // 中文算2个字符
        private int getLength() {
            int chCharCount = 0;

            for (int i = 0; i < name.length(); i++) {
                if (isChChar.apply(name.charAt(i))) chCharCount++;
            }
            return name.length() + chCharCount;
        }

        // 汉字：[0x4e00,0x9fa5]（或十进制[19968,40869]） 不包含标点符号
        // 数字：[0x30,0x39]（或十进制[48, 57]）
        // 小写字母：[0x61,0x7a]（或十进制[97, 122]）
        // 大写字母：[0x41,0x5a]（或十进制[65, 90]

        Function<Character, Boolean> isChChar = c -> c >= 0x4E00 && c <= 0x9FA5;
        Function<Character, Boolean> isEnChar = c -> Character.isLowerCase(c) || Character.isUpperCase(c);
        Function<Character, Boolean> isDigitChar = Character::isDigit;
        Function<Character, Boolean> isDashOr_Char = c -> c == '-' || c == '_';

        List<Function<Character, Boolean>> canContainList = new ArrayList<>();
        List<String> errorMsg = new ArrayList<>();

        Name checkMin(int min) throws Exception {
            if (nameLength < min) throw new Exception("名字至少需要" + min + "个字符");
            return this;
        }

        Name checkMax(int max) throws Exception {
            if (nameLength > max) throw new Exception("名字不能超过" + max + "个字符, 中文算2个字符");
            return this;
        }

        Name canContainChChar() {
            canContainList.add(isChChar);
            errorMsg.add("中文");
            return this;
        }

        Name canContainEnChar() {
            canContainList.add(isEnChar);
            errorMsg.add("字母");
            return this;
        }

        Name canContainDigitChar() {
            canContainList.add(isDigitChar);
            errorMsg.add("数字");
            return this;
        }

        Name canContainDashOr_() {
            canContainList.add(isDashOr_Char);
            errorMsg.add("_或-");
            return this;
        }

        void check() throws Exception {
            for (int i = 0; i < name.length(); i++) {
                char c = name.charAt(i);

                boolean anyMatch = canContainList.stream().anyMatch(a -> a.apply(c));

                if (!anyMatch) {
                    throw new Exception("请输入包含" + String.join("、", errorMsg) + "的昵称");
                }
            }
        }
    }


    // =================================================================================== //

    static class PassWord {

        private String pwd;
        private int pwdLength;

        private int digit;
        private int special;
        private int loCount;
        private int upCount;


        PassWord(String password) {
            this.pwd = password;
            this.pwdLength = password.length();

            for (int i = 0; i < password.length(); i++) {
                char c = password.charAt(i);
                if (Character.isUpperCase(c)) {
                    upCount++;
                }
                if (Character.isLowerCase(c)) {
                    loCount++;
                }
                if (Character.isDigit(c)) {
                    digit++;
                }
                if (c >= 33 && c <= 46 || c == 64) {
                    special++;
                }
            }
        }

        PassWord checkMin(int min) throws Exception {
            if (pwdLength < min) throw new Exception("密码至少需要" + min + "个字符");
            return this;
        }

        PassWord checkMax(int max) throws Exception {
            if (pwdLength > max) throw new Exception("密码不能超过" + max + "个字符");
            return this;
        }

        PassWord checkLowerCharAtLeast(int lowerChar) throws Exception {
            if (loCount < lowerChar) throw new Exception("密码至少包含" + lowerChar + "个小写字符");
            return this;
        }

        PassWord checkUpperCharAtLeast(int upperChar) throws Exception {
            if (upCount < upperChar) throw new Exception("密码至少包含" + upperChar + "个大写字符");
            return this;
        }

        PassWord checkDigitAtLeast(int digitChar) throws Exception {
            if (digit < digitChar) throw new Exception("密码至少包含" + digitChar + "个数字");
            return this;
        }

        PassWord checkSpecialAtLeast(int specialChar) throws Exception {
            if (special < specialChar) throw new Exception("密码至少包含" + specialChar + "个特殊字符");
            return this;
        }


    }

    public static void main(String[] args) throws Exception {
//        System.out.println(checkMobileNo("13212$12115"));

        System.out.println("中国人".length());

        new CheckUtils.Name("中国asdf·").checkMin(6)
                .checkMax(20)
                .canContainChChar()
                .canContainDigitChar()
                .canContainEnChar()
                .canContainDashOr_()
                .check();

//        new CheckUtils.PassWord("A123211_23a").checkMin(6)
//                .checkMax(20).checkLowerCharAtLeast(1)
//                .checkUpperCharAtLeast(1)
//                .checkDigitAtLeast(1);
    }


}
