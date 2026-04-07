$gwx2_XC_5 = (function (
  _,
  _v,
  _n,
  _p,
  _s,
  _wp,
  _wl,
  $gwn,
  $gwl,
  $gwh,
  wh,
  $gstack,
  $gwrt,
  gra,
  grb,
  TestTest,
  wfor,
  _ca,
  _da,
  _r,
  _rz,
  _o,
  _oz,
  _1,
  _1z,
  _2,
  _2z,
  _m,
  _mz,
  nv_getDate,
  nv_getRegExp,
  nv_console,
  nv_parseInt,
  nv_parseFloat,
  nv_isNaN,
  nv_isFinite,
  nv_decodeURI,
  nv_decodeURIComponent,
  nv_encodeURI,
  nv_encodeURIComponent,
  $gdc,
  nv_JSON,
  _af,
  _gv,
  _ai,
  _grp,
  _gd,
  _gapi,
  $ixc,
  _ic,
  _w,
  _ev,
  _tsd
) {
  return function (path, global) {
    if (typeof global === "undefined") {
      if (typeof __GWX_GLOBAL__ === "undefined") global = {};
      else global = __GWX_GLOBAL__;
    }
    if (typeof __WXML_GLOBAL__ === "undefined") {
      __WXML_GLOBAL__ = {};
    }
    __WXML_GLOBAL__.modules = __WXML_GLOBAL__.modules || {};
    var e_ = {};
    if (typeof global.entrys === "undefined") global.entrys = {};
    e_ = global.entrys;
    var d_ = {};
    if (typeof global.defines === "undefined") global.defines = {};
    d_ = global.defines;
    var f_ = {};
    if (typeof global.modules === "undefined") global.modules = {};
    f_ = global.modules || {};
    var p_ = {};
    __WXML_GLOBAL__.ops_cached = __WXML_GLOBAL__.ops_cached || {};
    __WXML_GLOBAL__.ops_set = __WXML_GLOBAL__.ops_set || {};
    __WXML_GLOBAL__.ops_init = __WXML_GLOBAL__.ops_init || {};
    var z = __WXML_GLOBAL__.ops_set.$gwx2_XC_5 || [];
    function gz$gwx2_XC_5_1() {
      if (__WXML_GLOBAL__.ops_cached.$gwx2_XC_5_1)
        return __WXML_GLOBAL__.ops_cached.$gwx2_XC_5_1;
      __WXML_GLOBAL__.ops_cached.$gwx2_XC_5_1 = [];
      (function (z) {
        var a = 11;
        function Z(ops) {
          z.push(ops);
        }
      })(__WXML_GLOBAL__.ops_cached.$gwx2_XC_5_1);
      return __WXML_GLOBAL__.ops_cached.$gwx2_XC_5_1;
    }
    __WXML_GLOBAL__.ops_set.$gwx2_XC_5 = z;
    __WXML_GLOBAL__.ops_init.$gwx2_XC_5 = true;
    var x = ["./subpackages/info/pages/version/version.wxml"];
    d_[x[0]] = {};
    var m0 = function (e, s, r, gg) {
      var z = gz$gwx2_XC_5_1();
      return r;
    };
    e_[x[0]] = { f: m0, j: [], i: [], ti: [], ic: [] };
    if (path && e_[path]) {
      return function (env, dd, global) {
        $gwxc = 0;
        var root = { tag: "wx-page" };
        root.children = [];
        g = "$gwx2_XC_5";
        var main = e_[path].f;
        if (typeof global === "undefined") global = {};
        global.f = $gdc(f_[path], "", 1);
        try {
          main(env, {}, root, global);
          _tsd(root);
        } catch (err) {
          console.log(err);
        }
        g = "";
        return root;
      };
    }
  };
})(
  __g.a,
  __g.b,
  __g.c,
  __g.d,
  __g.e,
  __g.f,
  __g.g,
  __g.h,
  __g.i,
  __g.j,
  __g.k,
  __g.l,
  __g.m,
  __g.n,
  __g.o,
  __g.p,
  __g.q,
  __g.r,
  __g.s,
  __g.t,
  __g.u,
  __g.v,
  __g.w,
  __g.x,
  __g.y,
  __g.z,
  __g.A,
  __g.B,
  __g.C,
  __g.D,
  __g.E,
  __g.F,
  __g.G,
  __g.H,
  __g.I,
  __g.J,
  __g.K,
  __g.L,
  __g.M,
  __g.N,
  __g.O,
  __g.P,
  __g.Q,
  __g.R,
  __g.S,
  __g.T,
  __g.U,
  __g.V,
  __g.W,
  __g.X,
  __g.Y,
  __g.Z,
  __g.aa
);
if (__vd_version_info__.delayedGwx || false) $gwx2_XC_5();
if (__vd_version_info__.delayedGwx)
  __wxAppCode__["subpackages/info/pages/version/version.wxml"] = [
    $gwx2_XC_5,
    "./subpackages/info/pages/version/version.wxml",
  ];
else
  __wxAppCode__["subpackages/info/pages/version/version.wxml"] = $gwx2_XC_5(
    "./subpackages/info/pages/version/version.wxml"
  );
__wxRoute = "subpackages/info/pages/version/version";
__wxRouteBegin = true;
__wxAppCurrentFile__ = "subpackages/info/pages/version/version.js";
define(
  "subpackages/info/pages/version/version.js",
  function (
    require,
    module,
    exports,
    window,
    document,
    frames,
    self,
    location,
    navigator,
    localStorage,
    history,
    Caches,
    screen,
    alert,
    confirm,
    prompt,
    XMLHttpRequest,
    WebSocket,
    Reporter,
    webkit,
    WeixinJSCore
  ) {
    "use strict";
    require("../../common/vendor.js"),
      (global.webpackJsonp = global.webpackJsonp || []).push([
        ["subpackages/info/pages/version/version"],
        {
          172:
            /*!****************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/main.js?{"page":"subpackages%2Finfo%2Fpages%2Fversion%2Fversion"} ***!
    \****************************************************************************************************************/
            /*! no static exports found */
            function (n, e, t) {
              (function (n, e) {
                var o = t(
                  /*! @babel/runtime/helpers/interopRequireDefault */ 4
                );
                t(/*! uni-pages */ 26);
                o(t(/*! vue */ 25));
                var r = o(
                  t(/*! ./subpackages/info/pages/version/version.vue */ 173)
                );
                (n.__webpack_require_UNI_MP_PLUGIN__ = t), e(r.default);
              }).call(
                this,
                t(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/wx.js */ 1)
                  .default,
                t(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2)
                  .createPage
              );
            },
          173:
            /*!*****************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/info/pages/version/version.vue ***!
    \*****************************************************************************************/
            /*! no static exports found */
            function (n, e, t) {
              t.r(e);
              var o = t(
                  /*! ./version.vue?vue&type=template&id=4d133136&scoped=true& */ 174
                ),
                r = t(/*! ./version.vue?vue&type=script&lang=js& */ 176);
              for (var i in r)
                ["default"].indexOf(i) < 0 &&
                  (function (n) {
                    t.d(e, n, function () {
                      return r[n];
                    });
                  })(i);
              t(
                /*! ./version.vue?vue&type=style&index=0&id=4d133136&scoped=true&lang=css& */ 178
              );
              var c = t(
                  /*! ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/runtime/componentNormalizer.js */ 32
                ),
                u = Object(c.default)(
                  r.default,
                  o.render,
                  o.staticRenderFns,
                  !1,
                  null,
                  "4d133136",
                  null,
                  !1,
                  o.components,
                  void 0
                );
              (u.options.__file = "subpackages/info/pages/version/version.vue"),
                (e.default = u.exports);
            },
          174:
            /*!************************************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/info/pages/version/version.vue?vue&type=template&id=4d133136&scoped=true& ***!
    \************************************************************************************************************************************/
            /*! exports provided: render, staticRenderFns, recyclableRender, components */
            function (n, e, t) {
              t.r(e);
              var o = t(
                /*! -!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--17-0!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/template.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-uni-app-loader/page-meta.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./version.vue?vue&type=template&id=4d133136&scoped=true& */ 175
              );
              t.d(e, "render", function () {
                return o.render;
              }),
                t.d(e, "staticRenderFns", function () {
                  return o.staticRenderFns;
                }),
                t.d(e, "recyclableRender", function () {
                  return o.recyclableRender;
                }),
                t.d(e, "components", function () {
                  return o.components;
                });
            },
          175:
            /*!************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--17-0!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/template.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-uni-app-loader/page-meta.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/info/pages/version/version.vue?vue&type=template&id=4d133136&scoped=true& ***!
    \************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
            /*! exports provided: render, staticRenderFns, recyclableRender, components */
            function (n, e, t) {
              t.r(e),
                t.d(e, "render", function () {
                  return o;
                }),
                t.d(e, "staticRenderFns", function () {
                  return i;
                }),
                t.d(e, "recyclableRender", function () {
                  return r;
                }),
                t.d(e, "components", function () {});
              var o = function () {
                  var n = this.$createElement;
                  this._self._c;
                },
                r = !1,
                i = [];
              o._withStripped = !0;
            },
          176:
            /*!******************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/info/pages/version/version.vue?vue&type=script&lang=js& ***!
    \******************************************************************************************************************/
            /*! no static exports found */
            function (n, e, t) {
              t.r(e);
              var o = t(
                  /*! -!./node_modules/babel-loader/lib!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--13-1!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/script.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./version.vue?vue&type=script&lang=js& */ 177
                ),
                r = t.n(o);
              for (var i in o)
                ["default"].indexOf(i) < 0 &&
                  (function (n) {
                    t.d(e, n, function () {
                      return o[n];
                    });
                  })(i);
              e.default = r.a;
            },
          177:
            /*!*************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/babel-loader/lib!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--13-1!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/script.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/info/pages/version/version.vue?vue&type=script&lang=js& ***!
    \*************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
            /*! no static exports found */
            function (n, e, t) {
              (function (n) {
                Object.defineProperty(e, "__esModule", { value: !0 }),
                  (e.default = void 0);
                var o = t(/*! @/utils/i18n.js */ 40),
                  r = {
                    computed: {
                      $t: function () {
                        return o.t;
                      },
                    },
                    data: function () {
                      return {
                        updateRecords: [
                          {
                            version: "v2.0.17",
                            content: ["常规优化", "同步固件更新"],
                          },
                          {
                            version: "v2.0.16",
                            content: [
                              "细化一线通相关参数",
                              "优化蓝牙连接",
                              "优化不同版本的参数显示",
                              "增加控制器端口状态显示",
                              "UB系列固件升级",
                            ],
                          },
                          {
                            version: "v2.0.15",
                            content: ["全面支持84V电压平台", "新型号支持"],
                          },
                          {
                            version: "v2.0.14",
                            content: [
                              "新增编码器相关参数",
                              "售后查询页面放置到首页",
                            ],
                          },
                          { version: "v2.0.13", content: ["优化升级"] },
                          {
                            version: "v2.0.12",
                            content: [
                              "优化蓝牙密码输入",
                              "增加多种提示",
                              "新增快速切换电池配置",
                              "优化产品详情",
                            ],
                          },
                          {
                            version: "v2.0.11",
                            content: [
                              "优化固件恢复策略",
                              "新增售后查询功能",
                              "优化九宫格按钮",
                              "更新轮播图同时增加点击事件",
                              "优化内容显示",
                            ],
                          },
                          { version: "v2.0.10", content: ["优化AE的型号选择"] },
                          {
                            version: "v2.0.9",
                            content: [
                              "优化自学习时状态不显示",
                              "新增型号识别",
                              "优化启动动画",
                            ],
                          },
                          {
                            version: "v2.0.8",
                            content: ["UB/PD/GK系列26号固件发布"],
                          },
                          {
                            version: "v2.0.7",
                            content: [
                              "26号固件发布",
                              "调整恢复出厂按钮的位置",
                              "常规优化",
                            ],
                          },
                          {
                            version: "v2.0.6",
                            content: [
                              "修复24款AE4问题",
                              "新增TCS恢复时间",
                              "EBS介入退出线性度优化",
                            ],
                          },
                          {
                            version: "v2.0.5",
                            content: [
                              "优化蓝牙密码校验",
                              "优化恢复出厂设置的提示",
                              "新增写入参数时对部分参数合理性判断",
                              "新增陡坡缓降功能设置说明",
                              "AE系列发布26号固件",
                            ],
                          },
                          {
                            version: "v2.0.4",
                            content: [
                              "优化弹窗提示",
                              "完善是否完成自学习的检测",
                              "三速增加边框",
                              "优化SOC解析出错",
                              "ND系列发布26号固件",
                            ],
                          },
                          {
                            version: "v2.0.3",
                            content: [
                              "修复部分参数解析出错",
                              "修复新手参数模式下部分参数无法修改",
                              "优化调参助手功能，新增部分参数提示和快速设置",
                              "优化部分系列车型选择时展示的内容",
                              "新增刹车断电设置开关(需升级到26号固件)",
                              "优化故障解析支持快速跳转",
                              "新增自动读取参数失败的提示",
                              "新增检测到固件丢失后主动尝试修复",
                              "新增常见问题检索功能",
                            ],
                          },
                          {
                            version: "v2.0.2",
                            content: [
                              "修复部分参数解析出错",
                              "ND系列新增BMS协议启用",
                              "新增简易仪表模式",
                            ],
                          },
                          {
                            version: "v2.0.1",
                            content: [
                              "修复骑行模式设置异常",
                              "修复GK/PD系列读写异常",
                              "修复AE系列车型选择异常",
                              "弱磁电流调整到电机组",
                              "倒挡软启时间调整到倒车组",
                              "兼容不支持蓝牙密码的固件",
                            ],
                          },
                          {
                            version: "v2.0.0",
                            content: [
                              "大版本更新",
                              "优化了界面的交互体验",
                              "新增了多种好玩的功能",
                            ],
                          },
                          {
                            version: "v1.0.50",
                            content: ["改进了性能，提升了运行速度", "持续优化"],
                          },
                          {
                            version: "v1.0.0",
                            content: ["小程序正式上线，基础功能已具备"],
                          },
                        ],
                      };
                    },
                    onLoad: function (e) {
                      n.setNavigationBarTitle({
                        title: this.$t("home_page.grid.item5"),
                      });
                    },
                  };
                e.default = r;
              }).call(
                this,
                t(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2)
                  .default
              );
            },
          178:
            /*!**************************************************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/info/pages/version/version.vue?vue&type=style&index=0&id=4d133136&scoped=true&lang=css& ***!
    \**************************************************************************************************************************************************/
            /*! no static exports found */
            function (n, e, t) {
              t.r(e);
              var o = t(
                  /*! -!./node_modules/mini-css-extract-plugin/dist/loader.js??ref--6-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--6-oneOf-1-1!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--6-oneOf-1-2!./node_modules/postcss-loader/src??ref--6-oneOf-1-3!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./version.vue?vue&type=style&index=0&id=4d133136&scoped=true&lang=css& */ 179
                ),
                r = t.n(o);
              for (var i in o)
                ["default"].indexOf(i) < 0 &&
                  (function (n) {
                    t.d(e, n, function () {
                      return o[n];
                    });
                  })(i);
              e.default = r.a;
            },
          179:
            /*!******************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/mini-css-extract-plugin/dist/loader.js??ref--6-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--6-oneOf-1-1!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--6-oneOf-1-2!./node_modules/postcss-loader/src??ref--6-oneOf-1-3!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/info/pages/version/version.vue?vue&type=style&index=0&id=4d133136&scoped=true&lang=css& ***!
    \******************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
            /*! no static exports found */
            function (n, e, t) {},
        },
        [[172, "common/runtime", "common/vendor"]],
      ]);
  },
  {
    isPage: true,
    isComponent: true,
    currentFile: "subpackages/info/pages/version/version.js",
  }
);
require("subpackages/info/pages/version/version.js");
