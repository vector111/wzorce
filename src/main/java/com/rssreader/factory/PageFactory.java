package com.rssreader.factory;

import com.rssreader.factory.page.Page;

public interface PageFactory {

    public Page createMainPage();
    public Page createLoginPage();
    public Page createRegisterPage();
    public Page createChannelPage();
    public Page createAddChannelPage();
}