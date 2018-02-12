package com.minitwit.state;

import com.minitwit.factory.DefaultFactory;
import com.minitwit.factory.page.Page;
import com.minitwit.factory.PageFactory;

public class MainState implements StateBase {

    PageFactory pageFactory = new DefaultFactory();

    @Override
    public Page getMainPage() {
        return pageFactory.createMainPage();
    }

    @Override
    public Page getLoginPage() {
        return null;
    }

    @Override
    public Page getRegisterPage() {
        return null;
    }

    @Override
    public Page getChannelPage() {
        return null;
    }

    @Override
    public Page getAddChannelPage() {
        return null;
    }
}