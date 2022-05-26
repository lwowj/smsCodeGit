layui.define([],function(exports){
    exports('api',{
        getMenus: 'agentMenu/' + currentUser.agentAccount + '?invalid_ie_cache=' + new Date().getTime()
    });
});