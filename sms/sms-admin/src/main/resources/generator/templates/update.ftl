<style>
    #${className?uncap_first}-update {
        padding: 20px 25px 25px 0;
    }

    #${className?uncap_first}-update .layui-treeSelect .ztree li a, .ztree li span {
        margin: 0 0 2px 3px !important;
    }
</style>
<div class="layui-fluid" id="${className?uncap_first}-update">
    <form class="layui-form" action="" lay-filter="${className?uncap_first}-update-form">
    <#list columns as column>
        <div class="layui-form-item">
            <label class="layui-form-label febs-form-item-require">${column.remark}：</label>
            <div class="layui-input-block">
                <input type="text" name="${column.field?uncap_first}" class="layui-input">
            </div>
        </div>
    </#list>
        <div class="layui-form-item febs-hide">
            <button class="layui-btn" lay-submit="" lay-filter="${className?uncap_first}-update-form-submit" id="submit"></button>
        </div>
    </form>
</div>

<script data-th-inline="javascript">
    layui.use(['febs', 'form', 'validate'], function () {
        var $ = layui.$,
            febs = layui.febs,
            layer = layui.layer,
            formSelects = layui.formSelects,
            treeSelect = layui.treeSelect,
            form = layui.form,
            ${className?uncap_first} = [[${'${' + className?uncap_first + '}'}]],
            $view = $('#${className?uncap_first}-update'),
            validate = layui.validate;

        form.verify(validate);
        form.render();

        init${className?uncap_first}Value();

        function init${className?uncap_first}Value() {
            form.val("${className?uncap_first}-update-form", {
                //"mobile": ${className?uncap_first}.mobile
            });
        }

        form.on('submit(${className?uncap_first}-update-form-submit)', function (data) {
            if (febs.nativeEqual(data.field, ${className?uncap_first})) {
                febs.alert.warn('数据未作任何修改！');
                return false;
            }
            febs.post(ctx + '${className?uncap_first}/update', data.field, function () {
                layer.closeAll();
                febs.alert.success('数据修改成功！');
                $('#febs-${className?uncap_first}').find('#query').click();
            });
            return false;
        });
    });
</script>