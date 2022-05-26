layui.define([],function(exports){
    exports('api',{
        getMenus: 'organizationUserMenu/' + currentUser.userAccount + '?invalid_ie_cache=' + new Date().getTime()
    });
});