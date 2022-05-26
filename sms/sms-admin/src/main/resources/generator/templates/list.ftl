<div class="layui-fluid layui-anim febs-anim" id="febs-${className?uncap_first}" lay-title="用户管理">
    <div class="layui-row febs-container">
        <div class="layui-col-md12">
            <div class="layui-card">
                <div class="layui-card-body febs-table-full">
                    <form class="layui-form layui-table-form" lay-filter="user-table-form">
                        <div class="layui-row">
                            <div class="layui-col-md10">
                                <div class="layui-form-item">
                        <#if columns??>
                            <#list columns as column>
                                <#-- 日期模板 -->
                                <#if column.type = 'timestamp' || column.type = 'date' || column.type = 'datetime'||column.type = 'TIMESTAMP' || column.type = 'DATE' || column.type = 'DATETIME'>
                                    <div class="layui-inline">
                                        <label class="layui-form-label layui-form-label-sm">${column.remark}</label>
                                        <div class="layui-input-inline">
                                            <input type="text" name="${column.field?uncap_first}" id="user-${column.field?uncap_first}" class="layui-input">
                                        </div>
                                    </div>
                                <#else>
                                    <div class="layui-inline">
                                        <label class="layui-form-label layui-form-label-sm">${column.remark}</label>
                                        <div class="layui-input-inline">
                                            <input type="text" name="${column.field?uncap_first}" autocomplete="off" class="layui-input">
                                        </div>
                                    </div>
                                </#if>
                            </#list>
                        </#if>   
                                </div>
                            </div>
                            <div class="layui-col-md2 layui-col-sm12 layui-col-xs12 table-action-area">
                                <div class="layui-btn layui-btn-sm layui-btn-primary table-action" id="query">
                                    <i class="layui-icon">&#xe848;</i>
                                </div>
                                <div class="layui-btn layui-btn-sm layui-btn-primary table-action" id="reset">
                                    <i class="layui-icon">&#xe79b;</i>
                                </div>
                            </div>
                        </div>
                    </form>
                    <table lay-filter="${className?uncap_first}Table" lay-data="{id: '${className?uncap_first}Table'}"></table>
                </div>
            </div>
        </div>
    </div>
</div>
<script type="text/html" id="${className?uncap_first}-toolbar">
    <div class="layui-btn-container">
        <button class="layui-btn layui-btn-sm" lay-event="add" shiro:hasAnyPermissions="${className?uncap_first}:add">新增</button>
        <button class="layui-btn layui-btn-sm febs-bg-volcano" lay-event="del" shiro:hasAnyPermissions="${className?uncap_first}:delete">删除</button>
    </div>
</script>
<script type="text/html" id="${className?uncap_first}-option">
    <span shiro:lacksPermission="${className?uncap_first}:view,${className?uncap_first}:update,${className?uncap_first}:delete">
        <span class="layui-badge-dot febs-bg-orange"></span> 无权限
    </span>
    <a lay-event="edit" shiro:hasPermission="${className?uncap_first}:update"><i class="layui-icon febs-edit-area febs-blue" title="修改">&#xe7a4;</i></a>
</script>
<script data-th-inline="none" type="text/javascript">
    layui.use(['jquery', 'laydate', 'form', 'table', 'febs'], function () {
        var $ = layui.jquery,
            laydate = layui.laydate,
            febs = layui.febs,
            form = layui.form,
            table = layui.table,
            $view = $('#febs-${className?uncap_first}'),
            $query = $view.find('#query'),
            $reset = $view.find('#reset'),
            $searchForm = $view.find('form'),
            sortObject = {field: 'createTime', type: null},
            tableIns;

        form.render();

        initTable();

        // laydate.render({
        //     elem: '#${className?uncap_first}-createTime',
        //     range: true,
        //     trigger: 'click'
        // });

        //头工具栏事件
        table.on('toolbar(${className?uncap_first}Table)', function(obj){
            var checkStatus = table.checkStatus(obj.config.id);
            switch(obj.event){
                case 'add':
                    febs.modal.open('新增信息', '${className?uncap_first}/add', {
                        btn: ['提交', '重置'],
                        area: $(window).width() <= 750 ? '95%' : '50%',
                        yes: function(index, layero) {
                            $('#${className?uncap_first}-add').find('#submit').trigger('click');
                        },
                        btn2: function() {
                            $('#${className?uncap_first}-add').find('#reset').trigger('click');
                            return false;
                        }
                    });
                    break;
                case 'del':
                    if (!checkStatus.data.length) {
                        febs.alert.warn('请选择需要删除的记录');
                    } else {
                        febs.modal.confirm('删除记录', '确定删除该记录？', function() {
                            var ${className?uncap_first}Ids = [];
                            layui.each(checkStatus.data, function(key, item) {
                                ${className?uncap_first}Ids.push(item.${className?uncap_first}Id)
                            });
                            delete${className?uncap_first}s(${className?uncap_first}Ids.join(','));
                        });
                    }
                    break;
            };
        });

        table.on('tool(${className?uncap_first}Table)', function (obj) {
            var data = obj.data,
                layEvent = obj.event;
            if (layEvent === 'edit') {
                febs.modal.open('修改信息', 'system/${className?uncap_first}/update/' + data.${className?uncap_first}name, {
                    area: $(window).width() <= 750 ? '90%' : '50%',
                    btn: ['提交', '取消'],
                    yes: function (index, layero) {
                        $('#${className?uncap_first}-update').find('#submit').trigger('click');
                    },
                    btn2: function () {
                        layer.closeAll();
                    }
                });
            }
        });

        table.on('sort(${className?uncap_first}Table)', function (obj) {
            sortObject = obj;
            tableIns.reload({
                initSort: obj,
                where: $.extend(getQueryParams(), {
                    field: obj.field,
                    order: obj.type
                })
            });
        });

        $query.on('click', function () {
            var params = $.extend(getQueryParams(), {field: sortObject.field, order: sortObject.type});
            tableIns.reload({where: params, page: {curr: 1}});
        });

        $reset.on('click', function () {
            $searchForm[0].reset();
            tableIns.reload({where: getQueryParams(), page: {curr: 1}, initSort: sortObject});
        });

        function initTable() {
            tableIns = febs.table.init({
                elem: $view.find('table'),
                id: '${className?uncap_first}Table',
                url: ctx + '${className?uncap_first}/list',
                toolbar: '#${className?uncap_first}-toolbar',
                cols: [[
            <#list columns as column>
                <#if column_index == 0>
                    {type: 'checkbox'},
                <#else>
                    <#if column_has_next>
                    {field: '${column.field?uncap_first}', title: '${column.remark}'},
                    <#else >
                    {title: '操作', toolbar: '#${column.field?uncap_first}-option'}
                    </#if>
                </#if>
            </#list>    
                ]]
            });
        }

        function getQueryParams() {
            return {
                //organizationName: $searchForm.find('input[name="organizationName"]').val().trim(),
                invalidate_ie_cache: new Date()
            };
        }

        function delete${className?uncap_first}s(${className?uncap_first}Ids) {
            febs.get(ctx + '${className?uncap_first}/delete/' + ${className?uncap_first}Ids, null, function () {
                febs.alert.success('删除成功');
                $query.click();
            });
        }
    })
</script>