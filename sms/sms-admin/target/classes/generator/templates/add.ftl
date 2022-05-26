<style>
    #${className?uncap_first}-add {
        padding: 20px 25px 25px 0;
    }
    #${className?uncap_first}-add .layui-treeSelect .ztree li a, .ztree li span {
        margin: 0 0 2px 3px !important;
    }
</style>
<div class="layui-fluid" id="${className?uncap_first}-add">
    <form class="layui-form" action="" lay-filter="${className?uncap_first}-add-form">
    <#list columns as column>
        <div class="layui-form-item">
            <label class="layui-form-label">${column.remark}：</label>
            <div class="layui-input-block">
                <input type="text" name="${column.field?uncap_first}" class="layui-input">
            </div>
        </div>
    </#list>
        <div class="layui-form-item febs-hide">
            <button class="layui-btn" lay-submit="" lay-filter="${className?uncap_first}-add-form-submit" id="submit"></button>
            <button type="reset" class="layui-btn" id="reset"></button>
        </div>
    </form>
</div>

<script>
    layui.use(['febs', 'form', 'validate'], function () {
        var $ = layui.$,
            febs = layui.febs,
            layer = layui.layer,
            form = layui.form,
            $view = $('#${className?uncap_first}-add'),
            validate = layui.validate;

        form.verify(validate);
        form.render();
        form.on('submit(${className?uncap_first}-add-form-submit)', function (data) {
            febs.post(ctx + '${className?uncap_first}', data.field, function () {
                layer.closeAll();
                febs.alert.success('数据新增成功！');
                $('#febs-${className?uncap_first}').find('#query').click();
            });
            return false;
        });
    });
</script>