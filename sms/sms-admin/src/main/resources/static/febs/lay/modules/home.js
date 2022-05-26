/**
 * 首页
 */
layui.define(function(exports){
    var admin = layui.admin;

    layui.use(['apexcharts', 'febs', 'jquery', 'util'], function () {
        var $ = layui.jquery,
            util = layui.util,
            $view = $('#febs-index'),
            febs = layui.febs;

        febs.get(ctx + 'index/' + currentUser.username, null, function (r) {
            handleSuccess(r.data);
        });

        function handleSuccess(data) {
            var hour = new Date().getHours();
            var time = hour < 6 ? '早上好' : (hour <= 11 ? '上午好' : (hour <= 13 ? '中午好' : (hour <= 18 ? '下午好' : '晚上好')));
            var welcomeArr = [
                '喝杯咖啡休息下吧☕',
                '要不要和朋友打局LOL',
                '今天又写了几个Bug呢',
                '今天在群里吹水了吗',
                '今天吃了什么好吃的呢',
                '今天您微笑了吗😊',
                '今天帮助别人了吗',
                '准备吃些什么呢',
                '周末要不要去看电影？'
            ];
            var index = Math.floor((Math.random() * welcomeArr.length));
            var welcomeMessage = time + '，<a id="febs-index-user">' + currentUser.username + '</a>，' + welcomeArr[index];
            $view.find('#today-ip').text(data.todayIp).end()
                .find('#today-visit-count').text(data.todayVisitCount).end()
                .find('#total-visit-count').text(data.totalVisitCount).end()
                .find('#user-dept').text(currentUser.deptName ? currentUser.deptName : '暂无所属部门').end()
                .find('#user-role').text(currentUser.roleName ? currentUser.roleName : '暂无角色').end()
                .find('#last-login-time').text(currentUser.lastLoginTime ? currentUser.lastLoginTime : '第一次访问系统').end()
                .find('#welcome-message').html(welcomeMessage).end()
                .find('#user-avatar').attr('src', ctx + "febs/images/avatar/" + currentUser.avatar);

            var currentTime = new Date().getTime();
            var yourVisitCount = [];
            var totalVisitCount = [];
            var lastTenDays = [
                util.toDateString(new Date(currentTime - 1000 * 9 * 86400), 'MM-dd'),
                util.toDateString(new Date(currentTime - 1000 * 8 * 86400), 'MM-dd'),
                util.toDateString(new Date(currentTime - 1000 * 7 * 86400), 'MM-dd'),
                util.toDateString(new Date(currentTime - 1000 * 6 * 86400), 'MM-dd'),
                util.toDateString(new Date(currentTime - 1000 * 5 * 86400), 'MM-dd'),
                util.toDateString(new Date(currentTime - 1000 * 4 * 86400), 'MM-dd'),
                util.toDateString(new Date(currentTime - 1000 * 3 * 86400), 'MM-dd'),
                util.toDateString(new Date(currentTime - 1000 * 2 * 86400), 'MM-dd'),
                util.toDateString(new Date(currentTime - 1000 * 86400), 'MM-dd'),
                util.toDateString(new Date(currentTime), 'MM-dd')
            ];


            layui.each(lastTenDays, function (k, i) {
                var contain = false;
                layui.each(data.lastSevenUserVisitCount, function (key, item) {
                    if (i === item.days) {
                        yourVisitCount.push(item.count);
                        contain = true;
                    }
                });
                if (!contain) yourVisitCount.push(0);
                contain = false;
                layui.each(data.lastSevenVisitCount, function (key, item) {
                    if (i === item.days) {
                        totalVisitCount.push(item.count);
                        contain = true;
                    }
                });
                if (!contain) totalVisitCount.push(0);
            });
        }
    });

    //发送量
    layui.use(['febs','carousel', 'echarts'], function(){
        var $ = layui.$
            ,carousel = layui.carousel
            ,febs = layui.febs
            ,echarts = layui.echarts
            ,myChart1 = echarts.init(document.getElementById('myChart1'))
            ,myChart2 = echarts.init(document.getElementById('myChart2'))
            ,xAxis1 = []
            ,seriesData1 = []
            ,legendData2 = []
            ,seriesData2 = [];

        window.addEventListener('resize',function () {
            myChart1.resize();
            myChart2.resize();
        });

        myChart1.showLoading({
            text: '数据正在努力加载...',
            textStyle: { fontSize : 14 , color: '#444' },
            effectOption: {backgroundColor: 'rgba(0, 0, 0, 0)'}
        });
        myChart2.showLoading({
            text: '数据正在努力加载...',
            textStyle: { fontSize : 14 , color: '#444' },
            effectOption: {backgroundColor: 'rgba(0, 0, 0, 0)'}
        });
        febs.get(ctx + 'report/orgSendCountToday', null, function(res) {
            myChart1.hideLoading();
            if(res.code == 200){ //请求成功
                var data = res.data;
                if(data && data.length > 0){
                    layui.each(data,function (index,item) {
                        if(index < 10){ //取前10条数据
                            xAxis1.push(item.orgName);
                            seriesData1.push(item.value);
                        }
                    });

                    // 指定图表的配置项和数据
                    var option = {
                        tooltip: {},
                        color: ['#3398DB'],
                        xAxis: {
                            data: xAxis1,
                            axisLabel: {
                                interval: 0,//横轴信息全部显示
                                rotate: -10,//-15度角倾斜显示
                            }
                        },
                        calculable : true, //气泡
                        yAxis: {
                            type: 'value',
                            name:"发送量（条）"
                        },
                        series: [{
                            name: '发送量',
                            type: 'bar',
                            data: seriesData1
                        }]
                    };
                    // 使用刚指定的配置项和数据显示图表。
                    myChart1.setOption(option);
                }else{
                    $("#myChart1").hide();
                    $("#myChart1").siblings(".noData").show();
                }
            }
        });

        //请求数据 运营商
        febs.get(ctx + 'report/operatorSendCountToday  ', null, function(res) {
            myChart2.hideLoading();
            if(res.code == 200){ //请求成功
                var data = res.data;
                if(data && data.length > 0){
                    layui.each(data,function (index,item) {
                        seriesData2.push({value:item.value, name:item.operatorName});
                        legendData2.push(item.operatorName);
                    });

                    // 指定图表的配置项和数据
                    option = {
                        //提示框组件,鼠标移动上去显示的提示内容
                        tooltip: {
                            trigger: 'item',
                            formatter: "{a} <br/>{b}: {c} ({d}%)"//模板变量有 {a}、{b}、{c}、{d}，分别表示系列名，数据名，数据值，百分比。
                        },
                        legend: {
                            type: 'scroll',
                            orient: 'vertical',
                            x: 'left',
                            data:legendData2
                        },
                        calculable : true, //气泡
                        series: [
                            {
                                name:'运营商',
                                type:'pie',
                                avoidLabelOverlap: false,
                                label:{            //饼图图形上的文本标签
                                    normal:{
                                        show:true,
                                        position:'center', //标签的位置
                                        textStyle : {
                                            fontWeight : 300 ,
                                            fontSize : 16    //文字的字体大小
                                        },
                                        formatter: '{b}:{c}: ({d}%)'
                                    }
                                },
                                data:seriesData2
                            }
                        ]
                    }
                    // 使用刚指定的配置项和数据显示图表。
                    myChart2.setOption(option);
                }else{
                    $("#myChart2").hide();
                    $("#myChart2").siblings(".noData").show();
                }

            }
        });

    });

    //地图
    layui.use(['febs','laytpl','carousel', 'echarts'], function(){
        var $ = layui.$
            ,carousel = layui.carousel
            ,laytpl = layui.laytpl
            ,febs = layui.febs
            ,echarts = layui.echarts
            ,seriesData = []
            ,myChart3 = echarts.init(document.getElementById('myChart3'));

        window.addEventListener('resize',function () {
            myChart3.resize();
        })

        myChart3.showLoading({
            text: '数据正在努力加载...',
            textStyle: { fontSize : 14 , color: '#444' },
            effectOption: {backgroundColor: 'rgba(0, 0, 0, 0)'}
        });

        //请求数据 运营商
        febs.get(ctx + 'report/provinceSendCountToday  ', null, function(res) {
            myChart3.hideLoading();
            if(res.code == 200){ //请求成功
                var data = res.data;
                if(data){
                    layui.each(data,function (index,item) {
                        seriesData.push({value:item.value, name:item.provinceName});
                    });

                    var mapDataTpl = mapData.innerHTML
                        ,view = document.getElementById('mapDataView');
                    laytpl(mapDataTpl).render(data, function(html){
                        view.innerHTML = html;
                    });

                    // 指定图表的配置项和数据
                    var option =
                        {
                        title : {
                            text: '全国分布'
                        },
                        tooltip: {
                            trigger: 'item'
                        },
                        itemStyle: {
                            emphasis: {
                                areaStyle: {
                                    color: 'rgba(255,215,0,0.8)'
                                }
                            }
                        },
                        dataRange: {
                            orient: 'horizontal',
                            min: 0,
                            max: 60000,
                            text: ['高', '低'],
                            splitNumber: 0
                        },
                        series: [
                            {
                                name: '用户分布',
                                type: 'map',
                                mapType: 'china',
                                selectedMode: 'multiple',
                                itemStyle: {
                                    normal: {
                                        label: {
                                            show: true,
                                            textStyle: {
                                                color: '#333'  //地图区域文字颜色
                                            }
                                        }
                                    },
                                    emphasis: {
                                        label: {show: true}
                                    }
                                },
                                label: {
                                    fontSize: 50,
                                    color: 'blue'
                                },
                                data: seriesData
                            }
                        ]
                    }
                    // 使用刚指定的配置项和数据显示图表。
                    myChart3.setOption(option);
                }
            }
        });
    });
    exports('home', {})
});