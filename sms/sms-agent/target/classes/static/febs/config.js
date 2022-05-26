layui.define(function(exports) {
  exports('conf', {
    container: 'febs',
    containerBody: 'febs-body',
    v: '2.0',
    base: layui.cache.base,
    css: layui.cache.base + 'css/',
    views: layui.cache.base + 'views/',
    viewLoadBar: true,
    debug: layui.cache.debug,
    name: 'febs',
    entry: '/index',
    engine: '',
    loadedIndex:true, //sw首页是否是刷新页 默认是
    isChangeHash:false, //sw首页点击首次加载需改变hash设置用到
    eventName: 'febs-event',
    tableName: 'febs',
    requestUrl: './'
  })
});
