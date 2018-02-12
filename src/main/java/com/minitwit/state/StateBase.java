package com.minitwit.state;

import com.minitwit.factory.Page;

public interface StateBase {

    public Page getMainPage();
    public Page getLoginPage();
    public Page getRegisterPage();
    public Page getChannelPage();
    public Page getAddChannelPage();
}