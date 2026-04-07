(global.webpackJsonp = global.webpackJsonp || []).push([
  ["subpackages/info/common/vendor"],
  {
    161:
      /*!***************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/utils/faqdata.js ***!
    \***************************************************************/
      /*! no static exports found */
      function (n, o, i) {
        Object.defineProperty(o, "__esModule", { value: !0 }),
          (o.default = void 0);
        var t = [
          {
            name: "蜂鸣器故障解析",
            description:
              "控制器出现故障后，会通过蜂鸣器进行故障提示，以下是对故障的解析说明",
            questions: [
              {
                question: "1长2短--霍尔故障",
                solution:
                  "检查电机霍尔线是否接触不好，或者霍尔电路有损坏，需要更换和维修",
                link: "",
              },
              {
                question: "1长3短--过流保护",
                solution:
                  "偶发可重新上电恢复，频报可尝试调小相线电流和调大软起时间或返厂维修",
                link: "",
              },
              {
                question: "1长4短--母线过压",
                solution:
                  "偶发可重新上电恢复，排查过压点是否设置过低，如果触发EBS后报的过压，可调低EBS充电电流和EBS电压上限",
                link: "",
              },
              {
                question: "1长5短--母线欠压",
                solution:
                  "偶发可重新上电恢复，排查欠压点设置是否过高，电池是否电量不足，可尝试调弱起步性能",
                link: "",
              },
              {
                question: "1长6短--控制器过温",
                solution: "停机等温度降下来即可",
                link: "",
              },
              {
                question: "1长7短--12V欠压",
                solution:
                  "偶发可重新上电恢复，频报需返厂检修，如果是蓝牙或NFC开机时触发可升级到最新固件版本尝试修复,也有可能是外部12V转换器异常导致",
                link: "",
              },
              {
                question: "1长8短--12V过压",
                solution: "偶发可重新上电恢复，频报需返厂维修",
                link: "",
              },
              {
                question: "1长9短--传感器故障",
                solution: "偶发可重新上电恢复，频报返厂维修",
                link: "",
              },
              {
                question: "1长10短--电流过大",
                solution: "偶发可重新上电恢复，可尝试调小相线电流",
                link: "",
              },
              {
                question: "1长11短--堵转保护",
                solution:
                  "偶发可重新上电恢复，松转把2秒后可恢复，需排查电机堵转原因",
                link: "",
              },
              {
                question: "1长12短--5V欠压",
                solution: "偶发可重新上电恢复，频报需返厂维修",
                link: "",
              },
              {
                question: "1长13短--5V过压",
                solution: "偶发可重新上电恢复，频报需返厂维修",
                link: "",
              },
              {
                question: "2长15短--高转把开机故障",
                solution:
                  "检测到开机时，转把拧到底或者开度较大，可以检查转把是否故障",
                link: "",
              },
              {
                question: "持续长鸣--转把故障",
                solution:
                  "偶发可重新上电恢复，先排查转把的信号线是否正常，再排查控制器转把的最大电压是否设置有误",
                link: "",
              },
            ],
          },
          {
            name: "电机/转把相关",
            description: "主要是列举和电机本身现象相关的一些问题",
            questions: [
              {
                question: "弱磁到底设置多少合适？",
                solution:
                  "弱磁的作用是为了让电机运行在更高的转速，但是当弱磁过大时，电机在极速时会不稳定或发生异音，此时说明弱磁已经超过极限了，车速也就有了理论上的最大值，所以在最大值范围内都是合理的，可根据自己对速度的要求来设置，如果用不了那么高的速度，则弱磁也不用设置太大",
                link: "",
              },
              {
                question: "弱磁会伤电机吗？",
                solution:
                  "第一弱磁本身不会影响电机的寿命。第二影响电机寿命的一定是过热。第三弱磁尽量设置在合理范围内。",
                link: "",
              },
              {
                question: "上电后不拧转把，电机会自动转",
                solution: "按照转把适配方法调整空转把电压",
                link: "https://mp.weixin.qq.com/s/40GbUQiXS2DK02s88QT1jQ?token=2043131861&lang=zh_CN",
              },
              {
                question: "满把拧下去后，控制器报飞车保护",
                solution: "按照转把适配方法调整转把最大电压",
                link: "https://mp.weixin.qq.com/s/40GbUQiXS2DK02s88QT1jQ?token=2043131861&lang=zh_CN",
              },
              {
                question: "电机极对数",
                solution:
                  "电机极对数需要联系电机卖家获取，测量方法比较麻烦，控制器内部转速是闭环控制，所以对电机极对数精确度要求不高，只要是相近就可以，极对数会影响控制器对转速的计算，即实际转速和控制器计算的转速有个固定的比例系数。",
                link: "https://mp.weixin.qq.com/s/AVBoUN-RsxuvAmjDSuF0kw?token=2043131861&lang=zh_CN",
              },
              {
                question: "电机的弱磁效果",
                solution:
                  "首先只有3挡有弱磁，其次不同的电机弱磁效果不一样，需要根据弱磁的设置方法进行适配",
                link: "https://mp.weixin.qq.com/s/FLxojsDIpdenjPhL7UErqQ?token=2043131861&lang=zh_CN",
              },
            ],
          },
          {
            name: "自学习相关",
            description: "主要介绍自学习时常见的问题和解决方法",
            questions: [
              {
                question: "什么情况下需要自学习",
                solution:
                  "1.更换电机或控制器 2.重新调整了电机的三相线的相许或更换了电机霍尔 3.操作了恢复出厂设置 4.误修改了自学习相关参数 5.升级固件后骑行有异常",
                link: "https://mp.weixin.qq.com/s/IB7YDOqoHinDt8H5WsE8gQ?token=2043131861&lang=zh_CN",
              },
              {
                question: "自学习提示电流搜索超时或偏小",
                solution:
                  "先恢复出厂设置，之后找到参数列表中的参考电流，并调小，之后保存参数，重新自学习，如果调到10以内还是不行，则有可能是缺相，需分别排查是控制器还是电机缺相",
                link: "https://mp.weixin.qq.com/s/JlmJMnAYh0VlIHISOkxXOA?token=2043131861&lang=zh_CN",
              },
              {
                question: "自学习提示电流偏大",
                solution:
                  "先恢复出厂设置，之后找到参数列表中的参考电流，并调大，步进10A，之后保存参数，重新自学习",
                link: "https://mp.weixin.qq.com/s/JlmJMnAYh0VlIHISOkxXOA?token=2043131861&lang=zh_CN",
              },
              {
                question: "自学习时电机异常或提示霍尔安装角度搜索失败",
                solution:
                  "先恢复出厂设置，之后尝试修改电机霍尔类型60度/120度，保存参数后重新自学习",
                link: "",
              },
            ],
          },
          {
            name: "性能相关",
            description: "主要介绍骑行时出现的现象以及解决方法",
            questions: [
              {
                question: "什么影响系统的性能",
                solution:
                  "性能是一个系统问题，需要三电(电池，电控，电机)的协调适配，三者相互影响，共同决定了系统的动力性能，所以在更换他们时需要综合考虑，调参时也一样。",
                link: "https://mp.weixin.qq.com/s/LI-Tsywr_C67ZlV19I7fwA?token=2043131861&lang=zh_CN",
              },
              {
                question: "起步偏弱，动力响应偏慢",
                solution:
                  "1.确保控制器固件版本号为24以上 2.骑行模式选择运动 3.软起时间设置到1以内 4.母线电流调整为保护板最大持续放电电流 5.用3挡起步",
                link: "",
              },
              {
                question:
                  "骑行经过颠簸路或坑洼路段时失去动力，需将速度降至很低才能恢复",
                solution:
                  "检查电机霍尔线的接插件是否稳定，同时尽量避免电机霍尔线和电机三相线靠的太近或缠绕到一起",
                link: "",
              },
              {
                question:
                  "骑行过程中，转速突然被限制到固定值，要松掉转把才能恢复",
                solution:
                  "说明触发了tcs,如果是平路上触发，可适当调大tcs的灵敏度",
                link: "",
              },
              {
                question: "三挡切换，但是转速效果依然维持一挡",
                solution:
                  "可能是误开启了预警功能，可在参数中将其关闭，部分车型需调整整车App设置可到常见问题车型相关中去查看",
                link: "",
              },
              {
                question: "助力推行动力不足",
                solution:
                  "目前助力推行的扭矩和倒车挡的扭矩相关，为倒车扭矩的一半，故可适当调大倒车扭矩",
                link: "",
              },
              {
                question:
                  "三挡情况下，车子在最高转速时，松转把后，没有明显减速",
                solution: "适当调低三挡的弱磁电流，可查看弱磁的适配方法",
                link: "",
              },
              {
                question: "快速满把起步时，保护板断电保护",
                solution:
                  "控制器的母线电流超过了电池保护板的持续放电电流，可适当调小控制器的母线电流",
                link: "",
              },
              {
                question: "定速巡航时速度不稳，坡道驻车时车子前后晃动",
                solution:
                  "调整控制器参数中的S_Ki。强制锁电机，自动驻车开启后，由于不同电机不同车型差异，在强制锁电机和自动驻坡中如果抖动剧烈，可适当减小s_ki值，如果感觉溜坡距离太长，可适当加大s_ki值",
                link: "",
              },
            ],
          },
          {
            name: "车型相关",
            description: "主要是针对个别具体车型的问题说明",
            questions: [
              {
                question: "nplay车型相关设置和仪表190故障",
                solution: "控制器车型中选择一线通，参数中的BMS协议选择开启",
                link: "",
              },
              {
                question: "UBE车型仪表设置",
                solution:
                  "找到控制器的仪表相关中一线通系数设置，旧款UBE设置104，新款UBE设置140，祖玛车型为100",
                link: "",
              },
              {
                question: "九号E系列，切换挡位，但是转速被限制",
                solution: "打开整车App，找到双电加速，并关闭",
                link: "",
              },
              {
                question: "九号Nz MIX，切换挡位，但是转速被限制",
                solution: "打开整车App，找到倒车功能，并关闭",
                link: "",
              },
            ],
          },
          {
            name: "其他问题",
            description: "未明确分类的其他问题",
            questions: [
              {
                question: "小程序蓝牙权限问题",
                solution:
                  "1.先将小程序删除。2.保证微信本身已经获取了蓝牙的权限。3.之后是小程序的蓝牙权限。4.定位权限。5.打开蓝牙功能。",
                link: "",
              },
              {
                question: "仪表车速不准",
                solution:
                  "记录仪表显示车速 A 和实际车速 B，计算 A/B 得出系数，将该系数与当前一线通系数相乘，把结果写入控制器即可",
                link: "",
              },
              {
                question: "续航不准,怎么调",
                solution:
                  "续航通常由计量模块来测算，计量模块需要适配当前的电池",
                link: "",
              },
              {
                question: "里程不准，怎么调",
                solution:
                  "里程不准，多数是由于表显车速不准导致，第一步先通过调整1挡的转速让实际车速控制在20以内。第二步调整控制器的一线通系数让表显车速和实际车速接近即可。",
                link: "",
              },
              {
                question: "预警功能说明",
                solution: "可以强制让控制器的三速失效",
                link: "https://mp.weixin.qq.com/s/0eGZ8uM7Qqy2NPuHRTSf7Q?token=2043131861&lang=zh_CN",
              },
              {
                question: "预警功能演示",
                solution: "可点击查看详情跳转到视频说明",
                link: "https://mp.weixin.qq.com/s/_syt56_n8wAICZr6rQ3UyA?token=2043131861&lang=zh_CN",
              },
              {
                question: "触发EBS后误报过压或过流故障",
                solution:
                  "可适当调低EBS的强度，具体为调低对应的触发转矩和充电电流，然后调低电压上限",
                link: "https://mp.weixin.qq.com/s/I4KV6pFVmqebLkdSXrP8OA?token=2043131861&lang=zh_CN",
              },
              {
                question: "控制器温度多少为正常？",
                solution:
                  "控制器内部有温度保护从90度开始降功率一直到105度直接宕机，正常运行时控制器的温度一般也会维持在70度左右，具体温度会和骑行路况有关系。",
                link: "",
              },
              {
                question: "如何打开EBS？",
                solution:
                  "第一步整车App里面要打开能量回收。第二步有保护板的情况下要允许反充电。第三步确保自己的电池没有加装禁止反充电的设备。第四步控制器参数EBS相关设置为5 5 10 100 90 第五步影响ebs强弱的因素很多，参数方面触发扭矩和充电电流要同步调大才能增强ebs效果",
                link: "",
              },
              {
                question: "如何关闭EBS？",
                solution: "EBS相关 参数设置为0 0 0 3000 0 即可关闭EBS",
                link: "",
              },
            ],
          },
        ];
        o.default = t;
      },
    202:
      /*!************************************************************************!*\
    !*** ./node_modules/@babel/runtime/helpers/objectWithoutProperties.js ***!
    \************************************************************************/
      /*! no static exports found */
      function (n, o, i) {
        var t = i(/*! ./objectWithoutPropertiesLoose.js */ 203);
        (n.exports = function (n, o) {
          if (null == n) return {};
          var i,
            s,
            e = t(n, o);
          if (Object.getOwnPropertySymbols) {
            var l = Object.getOwnPropertySymbols(n);
            for (s = 0; s < l.length; s++)
              (i = l[s]),
                o.indexOf(i) >= 0 ||
                  (Object.prototype.propertyIsEnumerable.call(n, i) &&
                    (e[i] = n[i]));
          }
          return e;
        }),
          (n.exports.__esModule = !0),
          (n.exports.default = n.exports);
      },
    203:
      /*!*****************************************************************************!*\
    !*** ./node_modules/@babel/runtime/helpers/objectWithoutPropertiesLoose.js ***!
    \*****************************************************************************/
      /*! no static exports found */
      function (n, o) {
        (n.exports = function (n, o) {
          if (null == n) return {};
          var i,
            t,
            s = {},
            e = Object.keys(n);
          for (t = 0; t < e.length; t++)
            (i = e[t]), o.indexOf(i) >= 0 || (s[i] = n[i]);
          return s;
        }),
          (n.exports.__esModule = !0),
          (n.exports.default = n.exports);
      },
  },
]);
