package com.rssreader.factory.page.keyboardUser;

import com.rssreader.factory.page.Page;

public class RegisterPageKeyboardUser implements Page {

    String pageName = "keyboardUser/RegisterPageKeyboardUser.ftl";

    @Override
    public String getName() {
        return this.pageName;
    }
}